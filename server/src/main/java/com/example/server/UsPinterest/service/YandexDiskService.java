package com.example.server.UsPinterest.service;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.UUID;
import java.util.List;

@Service
public class YandexDiskService {
    private static final Logger logger = LoggerFactory.getLogger(YandexDiskService.class);

    @Value("${yandex.disk.oauth.token}")
    private String oauthToken;

    @Value("${yandex.disk.base-path}")
    private String basePath;

    @Value("${yandex.disk.api.timeout:300000}")
    private int apiTimeout;

    private static final String API_BASE_URL = "https://cloud-api.yandex.net/v1/disk";
    private static final String RESOURCES_PATH = "/resources";
    private static final String UPLOAD_PATH = "/resources/upload";

    // Путь для хранения изображений профиля
    private static final String PROFILE_IMAGES_PATH = "/profile_images";

    // Кэш для URL изображений, чтобы не запрашивать одни и те же URL несколько раз
    private final ConcurrentHashMap<String, CachedUrl> urlCache = new ConcurrentHashMap<>();

    // Время жизни кэша URL (12 часов)
    private static final long URL_CACHE_TTL_MS = TimeUnit.HOURS.toMillis(12);

    private RequestConfig requestConfig;

    @PostConstruct
    public void init() throws IOException {
        requestConfig = RequestConfig.custom()
                .setConnectTimeout(apiTimeout)
                .setSocketTimeout(apiTimeout)
                .setConnectionRequestTimeout(apiTimeout)
                .build();

        if (oauthToken == null || oauthToken.isEmpty() || "your_token_here".equals(oauthToken)) {
            throw new IllegalStateException("Yandex.Disk OAuth token is not configured");
        }

        createUploadDirectoryIfNotExists();
        createProfileImagesDirectoryIfNotExists();
    }

    private void createUploadDirectoryIfNotExists() throws IOException {
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            String url = API_BASE_URL + RESOURCES_PATH + "?path=" + URLEncoder.encode(basePath, StandardCharsets.UTF_8.toString());
            HttpPut request = new HttpPut(url);
            request.setHeader("Authorization", "OAuth " + oauthToken);

            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 201 || statusCode == 409) {
                    logger.info("Upload directory {} is ready", basePath);
                } else {
                    logger.error("Failed to create upload directory. Status code: {}", statusCode);
                    throw new IOException("Failed to create upload directory on Yandex.Disk");
                }
            }
        }
    }

    /**
     * Создает директорию для хранения изображений профиля, если она еще не существует
     */
    private void createProfileImagesDirectoryIfNotExists() throws IOException {
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            String profileDir = basePath + PROFILE_IMAGES_PATH;
            String url = API_BASE_URL + RESOURCES_PATH + "?path=" + URLEncoder.encode(profileDir, StandardCharsets.UTF_8.toString());
            HttpPut request = new HttpPut(url);
            request.setHeader("Authorization", "OAuth " + oauthToken);

            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 201 || statusCode == 409) {
                    logger.info("Profile images directory {} is ready", profileDir);
                } else {
                    logger.error("Failed to create profile images directory. Status code: {}", statusCode);
                    throw new IOException("Failed to create profile images directory on Yandex.Disk");
                }
            }
        }
    }

    /**
     * Загружает файл на Яндекс Диск и возвращает ссылку на него
     */
    public String uploadFile(MultipartFile file) throws IOException {
        return uploadFile(file, false);  // По умолчанию не создаем публичную ссылку, а возвращаем прямую ссылку на скачивание
    }

    /**
     * Загружает файл на Яндекс.Диск и возвращает ссылку
     * @param file Файл для загрузки
     * @param makePublic Создавать ли публичную ссылку (если false, возвращается прямая ссылка для скачивания)
     * @return Ссылка на загруженный файл
     */
    public String uploadFile(MultipartFile file, boolean makePublic) throws IOException {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;

            byte[] fileContent = file.getBytes();
            uploadToYandexDisk(filename, fileContent);

            // Получаем публичную ссылку
            String publicLink = getPublicLink(filename);
            logger.info("Generated public link for {}: {}", filename, publicLink);

            if (!makePublic) {
                // Получаем прямую ссылку для скачивания
                String downloadLink = getDownloadLink(publicLink);

                // Проверяем, что получили прямую ссылку
                if (downloadLink.contains("downloader.disk.yandex.ru") ||
                        downloadLink.contains("preview.disk.yandex.ru")) {
                    logger.info("Generated download link for {}: {}", filename, downloadLink);
                    return downloadLink;
                } else {
                    // Если не удалось получить прямую ссылку, пробуем еще раз с принудительным обновлением
                    logger.warn("Не удалось получить прямую ссылку, пробуем еще раз принудительно");
                    downloadLink = tryDirectDownload(publicLink);

                    if (downloadLink != null && (downloadLink.contains("downloader.disk.yandex.ru") ||
                            downloadLink.contains("preview.disk.yandex.ru"))) {
                        logger.info("Generated direct download link on second attempt: {}", downloadLink);
                        return downloadLink;
                    } else {
                        // Если всё ещё не удалось, возвращаем публичную ссылку
                        logger.warn("Не удалось получить прямую ссылку даже со второй попытки, возвращаем публичную ссылку");
                        return publicLink;
                    }
                }
            }

            return publicLink;
        } catch (Exception e) {
            logger.error("Error during file upload to Yandex.Disk: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file to Yandex.Disk", e);
        }
    }

    /**
     * Загружает изображение профиля на Яндекс Диск
     *
     * @param file файл изображения профиля
     * @param userId ID пользователя для именования файла
     * @return URL загруженного изображения
     * @throws IOException если произошла ошибка при загрузке
     */
    public String uploadProfileImage(MultipartFile file, Long userId) throws IOException {
        // Формируем уникальное имя файла на основе ID пользователя и временной метки
        String fileName = "profile_" + userId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

        logger.info("Загрузка изображения профиля для пользователя {}: {}", userId, fileName);

        // Получаем URL для загрузки в директорию профилей
        String uploadUrl = getProfileImageUploadUrl(fileName);

        // Загружаем файл на Яндекс Диск
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {

            HttpPut uploadRequest = new HttpPut(uploadUrl);
            byte[] fileContent = file.getBytes();
            uploadRequest.setEntity(new ByteArrayEntity(fileContent));

            try (CloseableHttpResponse response = client.execute(uploadRequest)) {
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 201 || statusCode == 202) {
                    // Ждем завершения загрузки
                    waitForProfileImageUpload(fileName);

                    // Публикуем файл и получаем постоянную публичную ссылку
                    String publicUrl = generatePermanentPublicUrl(fileName);

                    // Преобразуем публичную ссылку в прямую ссылку для скачивания
                    String directDownloadUrl = getDownloadLink(publicUrl);
                    logger.info("Получена прямая ссылка на изображение профиля: {}", directDownloadUrl);

                    return directDownloadUrl;
                } else {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    throw new IOException("Не удалось загрузить изображение профиля. Код: " + statusCode +
                            ", Ответ: " + responseBody);
                }
            }
        }
    }

    /**
     * Получает URL для загрузки изображения профиля
     */
    private String getProfileImageUploadUrl(String filename) throws IOException {
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            String fullPath = basePath + PROFILE_IMAGES_PATH + "/" + filename;
            String encodedPath = URLEncoder.encode(fullPath, StandardCharsets.UTF_8.toString());
            String url = API_BASE_URL + UPLOAD_PATH + "?path=" + encodedPath + "&overwrite=true";

            HttpGet request = new HttpGet(url);
            request.setHeader("Authorization", "OAuth " + oauthToken);

            try (CloseableHttpResponse response = client.execute(request)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    throw new IOException("Не удалось получить URL для загрузки. Код: " +
                            response.getStatusLine().getStatusCode() + ", Ответ: " + responseBody);
                }

                String jsonResponse = EntityUtils.toString(response.getEntity());
                JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                return jsonObject.get("href").getAsString();
            }
        }
    }

    /**
     * Ожидает завершения загрузки изображения профиля
     */
    private void waitForProfileImageUpload(String filename) throws IOException {
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            String fullPath = basePath + PROFILE_IMAGES_PATH + "/" + filename;
            String encodedPath = URLEncoder.encode(fullPath, StandardCharsets.UTF_8.toString());
            String url = API_BASE_URL + RESOURCES_PATH + "?path=" + encodedPath;

            for (int i = 0; i < 10; i++) {
                HttpGet request = new HttpGet(url);
                request.setHeader("Authorization", "OAuth " + oauthToken);

                try (CloseableHttpResponse response = client.execute(request)) {
                    if (response.getStatusLine().getStatusCode() == 200) {
                        String jsonResponse = EntityUtils.toString(response.getEntity());
                        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                        if ("file".equals(jsonObject.get("type").getAsString())) {
                            return;
                        }
                    }
                }
                Thread.sleep(1000);
            }
            throw new IOException("Превышено время ожидания завершения загрузки изображения профиля");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Загрузка прервана", e);
        }
    }

    /**
     * Генерирует постоянную публичную ссылку на изображение профиля
     * Эта ссылка будет работать всегда, в отличие от временных ссылок на скачивание
     */
    private String generatePermanentPublicUrl(String filename) throws IOException {
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            String fullPath = basePath + PROFILE_IMAGES_PATH + "/" + filename;
            String encodedPath = URLEncoder.encode(fullPath, StandardCharsets.UTF_8.toString());

            // Публикуем файл
            String publishUrl = API_BASE_URL + RESOURCES_PATH + "/publish?path=" + encodedPath;
            HttpPut publishRequest = new HttpPut(publishUrl);
            publishRequest.setHeader("Authorization", "OAuth " + oauthToken);

            try (CloseableHttpResponse publishResponse = client.execute(publishRequest)) {
                if (publishResponse.getStatusLine().getStatusCode() != 200) {
                    String responseBody = EntityUtils.toString(publishResponse.getEntity());
                    throw new IOException("Не удалось опубликовать файл. Код: " +
                            publishResponse.getStatusLine().getStatusCode() + ", Ответ: " + responseBody);
                }

                // Ждем, пока файл станет публичным
                String metadataUrl = API_BASE_URL + RESOURCES_PATH + "?path=" + encodedPath;
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(1000);

                    HttpGet metadataRequest = new HttpGet(metadataUrl);
                    metadataRequest.setHeader("Authorization", "OAuth " + oauthToken);

                    try (CloseableHttpResponse metadataResponse = client.execute(metadataRequest)) {
                        if (metadataResponse.getStatusLine().getStatusCode() == 200) {
                            String jsonResponse = EntityUtils.toString(metadataResponse.getEntity());
                            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

                            if (jsonObject.has("public_url")) {
                                String publicUrl = jsonObject.get("public_url").getAsString();
                                if (publicUrl != null && !publicUrl.isEmpty()) {
                                    logger.info("Получена постоянная публичная ссылка: {}", publicUrl);

                                    // Сохраняем ссылку в кэш
                                    urlCache.put(publicUrl, new CachedUrl(publicUrl));

                                    return publicUrl;
                                }
                            }
                        }
                    }
                }
                throw new IOException("Превышено время ожидания публикации файла");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Прервано ожидание публикации файла", e);
        }
    }

    private void waitForUpload(String filename) throws IOException {
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            String fullPath = basePath + "/" + filename;
            String encodedPath = URLEncoder.encode(fullPath, StandardCharsets.UTF_8.toString());
            String url = API_BASE_URL + RESOURCES_PATH + "?path=" + encodedPath;

            for (int i = 0; i < 10; i++) {
                HttpGet request = new HttpGet(url);
                request.setHeader("Authorization", "OAuth " + oauthToken);

                try (CloseableHttpResponse response = client.execute(request)) {
                    if (response.getStatusLine().getStatusCode() == 200) {
                        String jsonResponse = EntityUtils.toString(response.getEntity());
                        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                        if ("file".equals(jsonObject.get("type").getAsString())) {
                            return;
                        }
                    }
                }
                Thread.sleep(1000);
            }
            throw new IOException("Timeout waiting for file upload to complete");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Upload interrupted", e);
        }
    }

    private String getUploadUrl(String filename) throws IOException {
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            String fullPath = basePath + "/" + filename;
            String encodedPath = URLEncoder.encode(fullPath, StandardCharsets.UTF_8.toString());
            String url = API_BASE_URL + UPLOAD_PATH + "?path=" + encodedPath + "&overwrite=true";

            HttpGet request = new HttpGet(url);
            request.setHeader("Authorization", "OAuth " + oauthToken);

            try (CloseableHttpResponse response = client.execute(request)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    throw new IOException("Failed to get upload URL. Status code: " + response.getStatusLine().getStatusCode() + ", Response: " + responseBody);
                }

                String jsonResponse = EntityUtils.toString(response.getEntity());
                JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                return jsonObject.get("href").getAsString();
            }
        }
    }

    private String getPublicLink(String filename) throws IOException {
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            String fullPath = basePath + "/" + filename;
            String encodedPath = URLEncoder.encode(fullPath, StandardCharsets.UTF_8.toString());

            // Публикуем файл
            String publishUrl = API_BASE_URL + RESOURCES_PATH + "/publish?path=" + encodedPath;
            HttpPut publishRequest = new HttpPut(publishUrl);
            publishRequest.setHeader("Authorization", "OAuth " + oauthToken);

            // Добавляем заголовки для имитации браузера
            publishRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

            try (CloseableHttpResponse publishResponse = client.execute(publishRequest)) {
                int statusCode = publishResponse.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    String responseBody = EntityUtils.toString(publishResponse.getEntity());
                    logger.warn("Failed to publish file. Status code: {}, Response: {}", statusCode, responseBody);

                    // Если файл уже опубликован, это не ошибка
                    if (responseBody.contains("already published") || statusCode == 409) {
                        logger.info("File is already published, proceeding to get public URL");
                    } else {
                        throw new IOException("Failed to publish file. Status code: " + statusCode + ", Response: " + responseBody);
                    }
                }

                // Ждем, пока файл станет публичным (максимум 10 попыток)
                String metadataUrl = API_BASE_URL + RESOURCES_PATH + "?path=" + encodedPath;
                for (int i = 0; i < 15; i++) { // Увеличиваем количество попыток до 15
                    Thread.sleep(1000);

                    HttpGet metadataRequest = new HttpGet(metadataUrl);
                    metadataRequest.setHeader("Authorization", "OAuth " + oauthToken);
                    metadataRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

                    try (CloseableHttpResponse metadataResponse = client.execute(metadataRequest)) {
                        int metaStatusCode = metadataResponse.getStatusLine().getStatusCode();

                        if (metaStatusCode == 200) {
                            String jsonResponse = EntityUtils.toString(metadataResponse.getEntity());
                            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

                            if (jsonObject.has("public_url")) {
                                String publicUrl = jsonObject.get("public_url").getAsString();
                                if (publicUrl != null && !publicUrl.isEmpty()) {
                                    logger.info("File published successfully after {} attempts", i + 1);

                                    // Для короткой ссылки yadi.sk, закешируем ее сразу
                                    if (publicUrl.contains("yadi.sk")) {
                                        logger.info("Generated short yadi.sk link: {}", publicUrl);
                                        urlCache.put(publicUrl, new CachedUrl(publicUrl));
                                    }

                                    // Возвращаем постоянный публичный URL
                                    return publicUrl;
                                }
                            }
                        } else {
                            logger.warn("Failed to get metadata. Status code: {}", metaStatusCode);
                        }
                    }
                }

                // Если не удалось получить публичную ссылку через API, попробуем получить ее через веб-интерфейс
                try {
                    return attemptToGetPublicLinkViaWeb(fullPath);
                } catch (Exception e) {
                    logger.error("Failed to get public link via web interface: {}", e.getMessage());
                }

                throw new IOException("Timeout waiting for file to become public");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for file to become public", e);
        }
    }

    /**
     * Пытается получить публичную ссылку через веб-интерфейс Яндекс.Диска
     */
    private String attemptToGetPublicLinkViaWeb(String fullPath) throws IOException {
        logger.info("Attempting to get public link for {} via web interface", fullPath);

        // В случае неудачи через API, возвращаем прямую ссылку на файл
        return API_BASE_URL + "/resources/download?path=" +
                URLEncoder.encode(fullPath, StandardCharsets.UTF_8.toString()) +
                "&disposition=inline";
    }

    /**
     * Получает прямую ссылку на скачивание файла по его публичной ссылке
     * @param publicUrl публичная ссылка на файл
     * @return прямая ссылка на скачивание файла (всегда downloader.disk.yandex.ru или preview.disk.yandex.ru)
     * @throws IOException если произошла ошибка при получении прямой ссылки
     */
    public String getDownloadLink(String publicUrl) throws IOException {
        logger.info("Getting download link for public URL: {}", publicUrl);

        // Проверяем, не является ли уже прямой ссылкой
        if (publicUrl.contains("downloader.disk.yandex.ru") ||
                publicUrl.contains("preview.disk.yandex.ru")) {
            logger.debug("URL is already a direct download link: {}", publicUrl);
            return publicUrl;
        }

        // Проверяем кэш
        CachedUrl cachedUrl = urlCache.get(publicUrl);
        if (cachedUrl != null && !cachedUrl.isExpired() &&
                (cachedUrl.getUrl().contains("downloader.disk.yandex.ru") ||
                        cachedUrl.getUrl().contains("preview.disk.yandex.ru"))) {
            logger.info("Returning cached download URL for {}: {}", publicUrl, cachedUrl.getUrl());
            return cachedUrl.getUrl();
        }

        // Если это прокси-ссылка, извлекаем оригинальную ссылку
        if (publicUrl.contains("/api/pins/proxy-image") && publicUrl.contains("url=")) {
            try {
                String encodedOriginalUrl = publicUrl.split("url=")[1];
                if (encodedOriginalUrl.contains("&")) {
                    encodedOriginalUrl = encodedOriginalUrl.split("&")[0];
                }
                String originalUrl = java.net.URLDecoder.decode(encodedOriginalUrl, StandardCharsets.UTF_8.toString());
                logger.info("Extracted original URL from proxy: {}", originalUrl);

                // Рекурсивный вызов для получения прямой ссылки из извлеченного URL
                return getDownloadLink(originalUrl);
            } catch (Exception e) {
                logger.error("Error extracting original URL from proxy: {}", e.getMessage());
                // Продолжаем попытки другими методами
            }
        }

        // Если это короткая ссылка типа yadi.sk, попробуем преобразовать её в полную
        if (publicUrl.contains("yadi.sk") || publicUrl.contains("disk.yandex.ru/i/")) {
            try {
                // Используем более надежный User-Agent для обхода защиты от ботов
                try (CloseableHttpClient client = HttpClients.custom()
                        .setDefaultRequestConfig(requestConfig)
                        .build()) {

                    HttpGet headRequest = new HttpGet(publicUrl);
                    headRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
                    headRequest.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                    headRequest.setHeader("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
                    headRequest.setHeader("Referer", "https://disk.yandex.ru/");

                    // Следуем перенаправлениям, чтобы получить полный URL
                    try (CloseableHttpResponse response = client.execute(headRequest)) {
                        int statusCode = response.getStatusLine().getStatusCode();

                        if (statusCode >= 300 && statusCode < 400) {
                            // Получаем URL перенаправления
                            String location = response.getFirstHeader("Location").getValue();
                            logger.info("Получено перенаправление для короткой ссылки: {} -> {}", publicUrl, location);
                            // Если мы получили прямую ссылку - возвращаем её
                            if (location.contains("downloader.disk.yandex.ru") ||
                                    location.contains("preview.disk.yandex.ru")) {
                                // Кэшируем результат
                                urlCache.put(publicUrl, new CachedUrl(location, publicUrl, TimeUnit.HOURS.toMillis(1)));
                                return location;
                            }
                            // Иначе используем этот URL для дальнейшего получения прямой ссылки
                            publicUrl = location;
                        } else if (statusCode == 200 && response.getEntity() != null) {
                            // Если удалось получить содержимое HTML страницы, ищем в нем ссылку на изображение
                            String html = EntityUtils.toString(response.getEntity());
                            // Ищем URL в формате https://downloader.disk.yandex.ru/...
                            String[] patterns = {
                                    "https://downloader\\.disk\\.yandex\\.ru/preview/[^\"']+",
                                    "https://downloader\\.disk\\.yandex\\.ru/disk/[^\"']+",
                                    "https://preview\\.disk\\.yandex\\.ru/[^\"']+"
                            };

                            for (String pattern : patterns) {
                                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                                java.util.regex.Matcher m = p.matcher(html);
                                if (m.find()) {
                                    String foundUrl = m.group();
                                    logger.info("Found direct URL in HTML: {}", foundUrl);
                                    // Кэшируем результат
                                    urlCache.put(publicUrl, new CachedUrl(foundUrl, publicUrl, TimeUnit.HOURS.toMillis(1)));
                                    return foundUrl;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Ошибка при попытке проследить за перенаправлением yadi.sk: {}", e.getMessage());
            }
        }

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {

            // Используем API Яндекс.Диска для загрузки файла по публичной ссылке
            String downloadUrl = API_BASE_URL + "/resources/download?public_key=" +
                    URLEncoder.encode(publicUrl, StandardCharsets.UTF_8.toString()) +
                    "&disposition=inline";  // Добавляем параметр disposition=inline

            // Для изображений добавляем параметр preview=true для получения прямой ссылки на предпросмотр
            if (isImageURL(publicUrl)) {
                downloadUrl += "&preview=true&size=XL";
            }

            HttpGet downloadRequest = new HttpGet(downloadUrl);
            downloadRequest.setHeader("Authorization", "OAuth " + oauthToken);
            downloadRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

            try (CloseableHttpResponse downloadResponse = client.execute(downloadRequest)) {
                if (downloadResponse.getStatusLine().getStatusCode() == 200) {
                    String downloadJsonResponse = EntityUtils.toString(downloadResponse.getEntity());
                    JsonObject downloadJsonObject = JsonParser.parseString(downloadJsonResponse).getAsJsonObject();

                    if (downloadJsonObject.has("href")) {
                        String directDownloadUrl = downloadJsonObject.get("href").getAsString();
                        logger.info("Got direct download URL: {}", directDownloadUrl);

                        // Проверяем, что это действительно прямая ссылка
                        if (directDownloadUrl.contains("downloader.disk.yandex.ru") ||
                                directDownloadUrl.contains("preview.disk.yandex.ru")) {
                            // Кэшируем связь публичного URL с прямой ссылкой
                            urlCache.put(publicUrl, new CachedUrl(directDownloadUrl, publicUrl, TimeUnit.HOURS.toMillis(1)));
                            return directDownloadUrl;
                        } else {
                            logger.warn("Получена некорректная прямая ссылка: {}", directDownloadUrl);
                        }
                    }
                } else {
                    logger.warn("Failed to get direct download URL. Status: {}", downloadResponse.getStatusLine().getStatusCode());
                }

                // Пробуем напрямую сделать запрос к публичной ссылке
                if (isImageURL(publicUrl)) {
                    String directUrl = tryDirectDownload(publicUrl);
                    if (directUrl != null && (directUrl.contains("downloader.disk.yandex.ru") ||
                            directUrl.contains("preview.disk.yandex.ru"))) {
                        // Кэшируем результат
                        urlCache.put(publicUrl, new CachedUrl(directUrl, publicUrl, TimeUnit.HOURS.toMillis(1)));
                        return directUrl;
                    }
                }
            }
        }

        // Если не удалось получить прямую ссылку, возвращаем исходную ссылку
        // Это позволит обрабатывать такие случаи в других методах
        logger.warn("Не удалось получить прямую ссылку, возвращаем исходную: {}", publicUrl);
        return publicUrl;
    }

    /**
     * Пытается загрузить файл напрямую, обходя CAPTCHA
     */
    private String tryDirectDownload(String publicUrl) {
        logger.info("Trying direct download approach for: {}", publicUrl);

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(apiTimeout)
                        .setSocketTimeout(apiTimeout)
                        .setConnectionRequestTimeout(apiTimeout)
                        .setRedirectsEnabled(true)
                        .build())
                .build()) {

            HttpGet request = new HttpGet(publicUrl);
            request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            request.setHeader("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
            request.setHeader("Referer", "https://disk.yandex.ru/");
            request.setHeader("Cookie", "yandexuid=1234567890000000000; Session_id=nobot");

            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                logger.info("Direct download response code: {}", statusCode);

                // Если получили редирект на страницу с изображением
                if (statusCode >= 300 && statusCode < 400 && response.getFirstHeader("Location") != null) {
                    String location = response.getFirstHeader("Location").getValue();
                    logger.info("Direct download redirected to: {}", location);

                    if (location.contains("downloader.disk.yandex.ru") ||
                            location.contains("preview.disk.yandex.ru")) {
                        return location;
                    }
                }

                // Если удалось получить содержимое HTML страницы, ищем в нем ссылку на изображение
                if (statusCode == 200 && response.getEntity() != null) {
                    String html = EntityUtils.toString(response.getEntity());
                    // Ищем URL в формате https://downloader.disk.yandex.ru/...
                    String[] patterns = {
                            "https://downloader\\.disk\\.yandex\\.ru/preview/[^\"']+",
                            "https://downloader\\.disk\\.yandex\\.ru/disk/[^\"']+",
                            "https://preview\\.disk\\.yandex\\.ru/[^\"']+"
                    };

                    for (String pattern : patterns) {
                        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                        java.util.regex.Matcher m = p.matcher(html);
                        if (m.find()) {
                            String foundUrl = m.group();
                            logger.info("Found direct URL in HTML: {}", foundUrl);
                            return foundUrl;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error during direct download: {}", e.getMessage());
        }

        // Если не получилось получить прямую ссылку
        return null;
    }

    /**
     * Обновляет ссылку на изображение, заменяя публичную ссылку на прямую ссылку для скачивания
     * @param imageUrl существующая ссылка на изображение
     * @return обновленная ссылка на изображение
     * @throws IOException если произошла ошибка при получении прямой ссылки
     */
    public String updateImageUrl(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return imageUrl;
        }

        // Проверяем кэш
        CachedUrl cachedUrl = urlCache.get(imageUrl);
        if (cachedUrl != null && !cachedUrl.isExpired()) {
            logger.debug("Возвращаю кэшированную ссылку для {}: {}", imageUrl, cachedUrl.getUrl());
            return cachedUrl.getUrl();
        }

        // Если это уже прямая ссылка Яндекс.Диска
        if (imageUrl.contains("downloader.disk.yandex.ru") ||
                imageUrl.contains("preview.disk.yandex.ru")) {

            // Добавляем параметр disposition=inline, если его нет
            if (!imageUrl.contains("disposition=")) {
                imageUrl = addDispositionParam(imageUrl);
                logger.info("Добавлен параметр disposition=inline в прямую ссылку: {}", imageUrl);
            }

            // Проверяем, не истекла ли она
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                int responseCode = connection.getResponseCode();

                // Если ссылка действительна, возвращаем её
                if (responseCode >= 200 && responseCode < 400) {
                    logger.debug("Прямая ссылка {} все еще действительна", imageUrl);
                    return imageUrl;
                }

                logger.info("Прямая ссылка {} устарела (код {}), получаю новую", imageUrl, responseCode);
                // Если ссылка недействительна, продолжаем и получаем новую
            } catch (Exception e) {
                logger.debug("Ошибка при проверке прямой ссылки {}: {}", e.getMessage());
                // Продолжаем и пытаемся получить новую ссылку
            }
        }

        // Если это прокси-ссылка, извлекаем оригинальную ссылку
        if (imageUrl.contains("/api/pins/proxy-image") && imageUrl.contains("url=")) {
            try {
                String encodedOriginalUrl = imageUrl.split("url=")[1];
                if (encodedOriginalUrl.contains("&")) {
                    encodedOriginalUrl = encodedOriginalUrl.split("&")[0];
                }
                String originalUrl = java.net.URLDecoder.decode(encodedOriginalUrl, StandardCharsets.UTF_8.toString());

                // Получаем прямую ссылку для оригинальной ссылки Яндекс.Диска
                if (originalUrl.contains("yadi.sk") ||
                        originalUrl.contains("disk.yandex.ru/i/") ||
                        originalUrl.contains("disk.yandex.ru/d/")) {

                    String directDownloadUrl = getDownloadLink(originalUrl);
                    if (directDownloadUrl != null &&
                            (directDownloadUrl.contains("downloader.disk.yandex.ru") ||
                                    directDownloadUrl.contains("preview.disk.yandex.ru"))) {

                        // Кэшируем результат
                        urlCache.put(imageUrl, new CachedUrl(directDownloadUrl, imageUrl, URL_CACHE_TTL_MS));
                        logger.info("Обновлена прокси-ссылка на прямую: {} -> {}", imageUrl, directDownloadUrl);
                        return directDownloadUrl;
                    }
                }

                // Если не удалось получить прямую ссылку, возвращаем исходную
                return imageUrl;
            } catch (Exception e) {
                logger.error("Ошибка при извлечении оригинальной ссылки из прокси: {}", e.getMessage());
                return imageUrl;
            }
        }

        // Для ссылок Яндекс.Диска
        if (imageUrl.contains("yadi.sk") ||
                imageUrl.contains("disk.yandex.ru/i/") ||
                imageUrl.contains("disk.yandex.ru/d/")) {

            try {
                String directDownloadUrl = getDownloadLink(imageUrl);
                if (directDownloadUrl != null &&
                        (directDownloadUrl.contains("downloader.disk.yandex.ru") ||
                                directDownloadUrl.contains("preview.disk.yandex.ru"))) {

                    // Кэшируем результат
                    urlCache.put(imageUrl, new CachedUrl(directDownloadUrl, imageUrl, URL_CACHE_TTL_MS));
                    logger.info("Получена прямая ссылка: {} -> {}", imageUrl, directDownloadUrl);
                    return directDownloadUrl;
                }
            } catch (Exception e) {
                logger.error("Ошибка при получении прямой ссылки для {}: {}", imageUrl, e.getMessage());
            }
        }

        // Если это не ссылка Яндекс.Диска или не удалось получить прямую ссылку
        return imageUrl;
    }

    /**
     * Обновляет все ссылки на изображения Яндекс.Диска в базе данных на прямые ссылки
     * @param pinRepository репозиторий для работы с пинами
     * @return количество обновленных ссылок
     */
    public int updateAllYandexDiskLinks(com.example.server.UsPinterest.repository.PinRepository pinRepository) {
        logger.info("Начинаю обновление всех ссылок Яндекс.Диска");
        int updatedCount = 0;

        // Получаем все пины из базы данных
        List<com.example.server.UsPinterest.model.Pin> pins = pinRepository.findAll();
        logger.info("Найдено {} пинов для обработки", pins.size());

        for (com.example.server.UsPinterest.model.Pin pin : pins) {
            try {
                String originalUrl = pin.getImageUrl();
                if (originalUrl == null || originalUrl.isEmpty()) {
                    continue;
                }

                // Пропускаем уже обновленные прямые ссылки
                if (originalUrl.contains("downloader.disk.yandex.ru") ||
                        originalUrl.contains("preview.disk.yandex.ru")) {

                    // Проверяем, не истекла ли ссылка
                    try {
                        URL url = new URL(originalUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("HEAD");
                        connection.setConnectTimeout(5000);
                        int responseCode = connection.getResponseCode();

                        if (responseCode >= 200 && responseCode < 400) {
                            logger.debug("Пин {} уже имеет действительную прямую ссылку: {}", pin.getId(), originalUrl);
                            continue;
                        }

                        logger.info("Прямая ссылка для пина {} устарела (код {}), обновляем", pin.getId(), responseCode);
                    } catch (Exception e) {
                        logger.debug("Ошибка при проверке прямой ссылки, пробуем обновить: {}", e.getMessage());
                    }
                }

                // Проверяем, является ли это ссылкой на прокси
                if (originalUrl.contains("/api/pins/proxy-image")) {
                    // Извлекаем оригинальную ссылку из параметра url
                    String encodedOriginalUrl = originalUrl.split("url=")[1];
                    if (encodedOriginalUrl.contains("&")) {
                        encodedOriginalUrl = encodedOriginalUrl.split("&")[0];
                    }
                    String yandexUrl = java.net.URLDecoder.decode(encodedOriginalUrl, StandardCharsets.UTF_8.toString());

                    // Теперь получаем прямую ссылку на скачивание
                    String directDownloadUrl = getDownloadLink(yandexUrl);
                    if (directDownloadUrl != null &&
                            (directDownloadUrl.contains("downloader.disk.yandex.ru") ||
                                    directDownloadUrl.contains("preview.disk.yandex.ru"))) {

                        // Сохраняем прямую ссылку в базу данных
                        pin.setImageUrl(directDownloadUrl);
                        pinRepository.save(pin);
                        logger.info("Обновлена ссылка для пина {}: {} -> {}", pin.getId(), originalUrl, directDownloadUrl);
                        updatedCount++;
                    }
                }
                // Для ссылок Яндекс.Диска (но не прокси)
                else if (originalUrl.contains("yadi.sk") ||
                        originalUrl.contains("disk.yandex.ru/i/") ||
                        originalUrl.contains("disk.yandex.ru/d/")) {

                    // Получаем прямую ссылку для скачивания
                    String directDownloadUrl = getDownloadLink(originalUrl);
                    if (directDownloadUrl != null &&
                            (directDownloadUrl.contains("downloader.disk.yandex.ru") ||
                                    directDownloadUrl.contains("preview.disk.yandex.ru"))) {

                        // Сохраняем прямую ссылку в базу данных
                        pin.setImageUrl(directDownloadUrl);
                        pinRepository.save(pin);
                        logger.info("Обновлена ссылка для пина {}: {} -> {}", pin.getId(), originalUrl, directDownloadUrl);
                        updatedCount++;
                    }
                }
            } catch (Exception e) {
                logger.error("Ошибка при обновлении ссылки для пина {}: {}", pin.getId(), e.getMessage(), e);
            }
        }

        logger.info("Обновление завершено. Обновлено {} ссылок", updatedCount);
        return updatedCount;
    }

    /**
     * Проверяет, является ли URL ссылкой на изображение по его расширению
     */
    private boolean isImageURL(String url) {
        String lowerUrl = url.toLowerCase();
        return lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg") ||
                lowerUrl.endsWith(".png") || lowerUrl.endsWith(".gif") ||
                lowerUrl.endsWith(".webp") || lowerUrl.contains("image") ||
                !lowerUrl.contains(".");  // Если нет расширения, предполагаем что это может быть изображение
    }

    /**
     * Внутренний класс для кэширования URL с временем жизни
     */
    private static class CachedUrl {
        private final String url;
        private final String originalUrl;
        private final long expirationTime;
        private final long creationTime;

        public CachedUrl(String url) {
            this(url, url, URL_CACHE_TTL_MS);
        }

        public CachedUrl(String url, String originalUrl, long ttlMs) {
            this.url = url;
            this.originalUrl = originalUrl;
            this.creationTime = System.currentTimeMillis();
            this.expirationTime = this.creationTime + ttlMs;
        }

        public String getUrl() {
            return url;
        }

        public String getOriginalUrl() {
            return originalUrl;
        }

        public long getCreationTime() {
            return creationTime;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }

    /**
     * Принудительно обновляет URL изображения, игнорируя кэш
     * @param imageUrl существующая ссылка на изображение
     * @return обновленная ссылка на изображение
     * @throws IOException если произошла ошибка при получении прямой ссылки
     */
    public String forceUpdateImageUrl(String imageUrl) throws IOException {
        // Сначала удаляем URL из кэша, если он там есть
        urlCache.remove(imageUrl);

        // Проверяем, является ли это прямой ссылкой Яндекс Диска
        if (imageUrl.contains("downloader.disk.yandex.ru") ||
                imageUrl.contains("preview.disk.yandex.ru")) {

            // Извлекаем информацию из URL, чтобы попытаться восстановить публичную ссылку
            try {
                // Ищем исходную публичную ссылку в других записях кэша
                for (Map.Entry<String, CachedUrl> entry : urlCache.entrySet()) {
                    CachedUrl cachedUrl = entry.getValue();
                    if (imageUrl.equals(cachedUrl.getUrl())) {
                        String originalUrl = entry.getKey();
                        logger.info("Найден исходный URL в кэше: {}", originalUrl);
                        urlCache.remove(entry.getKey());
                        return updateImageUrl(originalUrl);
                    }
                }

                // Если не нашли в кэше, пробуем получить файл через общую ссылку на Яндекс Диск
                // Из URL можно извлечь uid и filename для формирования публичной ссылки
                String uid = extractParameter(imageUrl, "uid");
                String filename = extractParameter(imageUrl, "filename");

                if (uid != null && filename != null) {
                    // Пытаемся найти файл по API в публичных файлах
                    logger.info("Попытка найти публичный файл по uid={} и filename={}", uid, filename);

                    // Создаем запрос к API поиска публичных файлов
                    String publicUrl = "https://disk.yandex.ru/i/" + uid;
                    logger.info("Пробуем публичную ссылку: {}", publicUrl);
                    return updateImageUrl(publicUrl);
                }
            } catch (Exception e) {
                logger.error("Ошибка при попытке восстановить исходный URL: {}", e.getMessage());
            }
        }

        // Если это не прямая ссылка или не удалось восстановить публичную ссылку,
        // просто обновляем через обычный метод
        return updateImageUrl(imageUrl);
    }

    /**
     * Извлекает значение параметра из URL
     */
    private String extractParameter(String url, String paramName) {
        try {
            int startIdx = url.indexOf(paramName + "=");
            if (startIdx == -1) return null;

            startIdx += paramName.length() + 1;
            int endIdx = url.indexOf("&", startIdx);
            if (endIdx == -1) endIdx = url.length();

            String value = url.substring(startIdx, endIdx);
            return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            logger.error("Ошибка при извлечении параметра {}: {}", paramName, e.getMessage());
            return null;
        }
    }

    /**
     * Загружает файл на Яндекс.Диск
     * @param filename Имя файла
     * @param fileContent Содержимое файла
     * @throws IOException В случае ошибки загрузки
     */
    private void uploadToYandexDisk(String filename, byte[] fileContent) throws IOException {
        logger.info("Starting file upload: {}, size: {} bytes", filename, fileContent.length);

        // Проверяем токен
        if (oauthToken == null || oauthToken.isEmpty() || "your_token_here".equals(oauthToken)) {
            String errorMsg = "Yandex.Disk OAuth token is not configured or invalid";
            logger.error(errorMsg);
            throw new IOException(errorMsg);
        }

        logger.debug("Using OAuth token: {}...", oauthToken.substring(0, Math.min(10, oauthToken.length())) + "***");

        // Получаем URL для загрузки
        String uploadUrl = getUploadUrl(filename);
        logger.info("Got upload URL: {}", uploadUrl);

        // Загружаем файл
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {

            HttpPut uploadRequest = new HttpPut(uploadUrl);
            uploadRequest.setEntity(new ByteArrayEntity(fileContent));

            logger.debug("Sending upload request to Yandex.Disk...");
            try (CloseableHttpResponse response = client.execute(uploadRequest)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = "";
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    responseBody = EntityUtils.toString(entity);
                }

                logger.debug("Upload response status: {}, body: {}", statusCode, responseBody);

                if (statusCode == 201 || statusCode == 202) {
                    // Ждем завершения загрузки
                    waitForUpload(filename);
                    logger.info("File {} successfully uploaded to Yandex.Disk", filename);
                } else {
                    String errorMessage = "Failed to upload file to Yandex.Disk. Status code: " + statusCode + ", Response: " + responseBody;
                    logger.error(errorMessage);
                    throw new IOException(errorMessage);
                }
            }
        } catch (Exception e) {
            logger.error("Error during file upload: ", e);
            throw new IOException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    private String addDispositionParam(String url) {
        if (!url.contains("disposition=")) {
            if (url.contains("?")) {
                return url + "&disposition=inline";
            } else {
                return url + "?disposition=inline";
            }
        }
        return url;
    }
}