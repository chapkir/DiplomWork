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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.Map;

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

    public String uploadFile(MultipartFile file, String filename) throws IOException {
        logger.info("Starting file upload: {}, size: {} bytes", filename, file.getSize());

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
            byte[] fileContent = file.getBytes();
            logger.debug("File content size: {} bytes", fileContent.length);

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

                    // Получаем метаданные файла для получения preview URL
                    String fullPath = basePath + "/" + filename;
                    String encodedPath = URLEncoder.encode(fullPath, StandardCharsets.UTF_8.toString());
                    String metadataUrl = API_BASE_URL + RESOURCES_PATH + "?path=" + encodedPath;

                    HttpGet metadataRequest = new HttpGet(metadataUrl);
                    metadataRequest.setHeader("Authorization", "OAuth " + oauthToken);

                    try (CloseableHttpResponse metadataResponse = client.execute(metadataRequest)) {
                        int metadataStatusCode = metadataResponse.getStatusLine().getStatusCode();
                        String jsonResponse = EntityUtils.toString(metadataResponse.getEntity());
                        logger.debug("Metadata response status: {}, body: {}", metadataStatusCode, jsonResponse);

                        if (metadataStatusCode == 200) {
                            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

                            if (jsonObject.has("file")) {
                                String fileUrl = jsonObject.get("file").getAsString();
                                logger.info("Got file URL: {}", fileUrl);
                                return fileUrl;
                            } else {
                                throw new IOException("File URL not found in metadata");
                            }
                        } else {
                            throw new IOException("Failed to get metadata. Status code: " + metadataStatusCode);
                        }
                    }
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

            try (CloseableHttpResponse publishResponse = client.execute(publishRequest)) {
                if (publishResponse.getStatusLine().getStatusCode() != 200) {
                    String responseBody = EntityUtils.toString(publishResponse.getEntity());
                    throw new IOException("Failed to publish file. Status code: " + publishResponse.getStatusLine().getStatusCode() + ", Response: " + responseBody);
                }

                // Ждем, пока файл станет публичным (максимум 10 попыток)
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
                                    logger.info("File published successfully after {} attempts", i + 1);

                                    // Получаем прямую ссылку на скачивание
                                    return getDownloadLink(publicUrl);
                                }
                            }
                        }
                    }
                }
                throw new IOException("Timeout waiting for file to become public");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for file to become public", e);
        }
    }

    /**
     * Получает прямую ссылку на скачивание файла по его публичной ссылке
     * @param publicUrl публичная ссылка на файл
     * @return прямая ссылка на скачивание файла
     * @throws IOException если произошла ошибка при получении ссылки
     */
    private String getDownloadLink(String publicUrl) throws IOException {
        logger.info("Getting download link for public URL: {}", publicUrl);

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {

            // Получаем прямую ссылку на скачивание
            String downloadUrl = API_BASE_URL + "/resources/download?public_key=" +
                    URLEncoder.encode(publicUrl, StandardCharsets.UTF_8.toString());

            HttpGet downloadRequest = new HttpGet(downloadUrl);
            downloadRequest.setHeader("Authorization", "OAuth " + oauthToken);

            try (CloseableHttpResponse downloadResponse = client.execute(downloadRequest)) {
                if (downloadResponse.getStatusLine().getStatusCode() == 200) {
                    String downloadJsonResponse = EntityUtils.toString(downloadResponse.getEntity());
                    JsonObject downloadJsonObject = JsonParser.parseString(downloadJsonResponse).getAsJsonObject();

                    if (downloadJsonObject.has("href")) {
                        String directDownloadUrl = downloadJsonObject.get("href").getAsString();
                        logger.info("Got direct download URL: {}", directDownloadUrl);
                        return directDownloadUrl;
                    }
                }

                logger.warn("Failed to get direct download URL, falling back to public URL: {}", publicUrl);
                return publicUrl;
            }
        }
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

        // Проверка является ли URL локальным прокси
        if (imageUrl.contains("/api/pins/proxy-image")) {
            // Извлекаем оригинальный URL из параметра
            String encodedOriginalUrl = imageUrl.split("url=")[1];
            try {
                String originalUrl = java.net.URLDecoder.decode(encodedOriginalUrl, StandardCharsets.UTF_8.toString());
                imageUrl = originalUrl;
            } catch (Exception e) {
                logger.warn("Не удалось декодировать URL из прокси: {}", imageUrl);
                // Если декодирование не удалось, используем как есть
            }
        }

        // Проверяем кэш и его актуальность
        CachedUrl cachedUrl = urlCache.get(imageUrl);

        // Проверяем, истек ли срок действия кэшированного URL или это URL от Яндекс Диска с прямой ссылкой
        boolean shouldRefresh = cachedUrl == null || cachedUrl.isExpired() ||
                (cachedUrl.getUrl().contains("downloader.disk.yandex.ru") &&
                        System.currentTimeMillis() - cachedUrl.getCreationTime() > TimeUnit.MINUTES.toMillis(30));

        if (!shouldRefresh) {
            logger.debug("Использую кэшированный URL для {}: {}", imageUrl, cachedUrl.getUrl());
            return cachedUrl.getUrl();
        }

        // Определяем тип URL
        if (imageUrl.contains("yadi.sk") || imageUrl.contains("disk.yandex.ru") ||
                imageUrl.startsWith("https://yandex") || imageUrl.contains("/d/")) {
            // Это публичная ссылка на Яндекс.Диск
            try (CloseableHttpClient client = HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .build()) {

                String downloadUrl = API_BASE_URL + "/resources/download?public_key=" +
                        URLEncoder.encode(imageUrl, StandardCharsets.UTF_8.toString());

                // Добавляем параметр preview=true только для изображений
                if (isImageURL(imageUrl)) {
                    downloadUrl += "&preview=true";
                }

                HttpGet downloadRequest = new HttpGet(downloadUrl);
                downloadRequest.setHeader("Authorization", "OAuth " + oauthToken);

                try (CloseableHttpResponse response = client.execute(downloadRequest)) {
                    if (response.getStatusLine().getStatusCode() == 200) {
                        String jsonResponse = EntityUtils.toString(response.getEntity());
                        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

                        if (jsonObject.has("href")) {
                            String directUrl = jsonObject.get("href").getAsString();
                            logger.info("Получена прямая ссылка для {}: {}", imageUrl, directUrl);

                            // Кэшируем обновленный URL
                            urlCache.put(imageUrl, new CachedUrl(directUrl));
                            return directUrl;
                        } else {
                            logger.warn("В ответе API Яндекс.Диска отсутствует поле 'href'");
                        }
                    } else {
                        logger.warn("Не удалось получить прямую ссылку. Код: {}", response.getStatusLine().getStatusCode());
                    }
                }
            } catch (Exception e) {
                logger.error("Ошибка при обновлении URL из Яндекс.Диска: {}", e.getMessage(), e);
            }
        } else if (imageUrl.contains("downloader.disk.yandex.ru") ||
                imageUrl.contains("preview.disk.yandex.ru")) {
            // Это уже прямая ссылка, но она могла устареть
            // Попробуем получить исходную публичную ссылку из метаданных, если она есть
            if (cachedUrl != null && cachedUrl.getOriginalUrl() != null) {
                logger.info("Прямая ссылка могла устареть, обновляем используя оригинальный URL: {}",
                        cachedUrl.getOriginalUrl());
                try {
                    return updateImageUrl(cachedUrl.getOriginalUrl());
                } catch (Exception e) {
                    logger.error("Не удалось обновить истекшую ссылку: {}", e.getMessage());
                }
            } else {
                // Если у нас нет исходной ссылки, кэшируем текущую на короткое время
                logger.info("Используем прямую ссылку без возможности обновления: {}", imageUrl);
                urlCache.put(imageUrl, new CachedUrl(imageUrl, imageUrl, TimeUnit.MINUTES.toMillis(5)));
            }
        }

        // Если не удалось получить прямую ссылку, возвращаем исходную и кэшируем её
        if (!urlCache.containsKey(imageUrl)) {
            urlCache.put(imageUrl, new CachedUrl(imageUrl, imageUrl, TimeUnit.MINUTES.toMillis(5)));
        }
        return imageUrl;
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
}