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

        // Если это уже прямая ссылка на скачивание или предпросмотр
        if (imageUrl.contains("https://downloader.disk.yandex.ru/") ||
                imageUrl.contains("https://preview.disk.yandex.ru/")) {
            return imageUrl;
        }

        // Если это публичная ссылка, пытаемся получить прямую ссылку на предпросмотр
        if (imageUrl.contains("https://disk.yandex.ru/")) {
            try (CloseableHttpClient client = HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .build()) {

                String previewUrl = API_BASE_URL + "/resources/download?public_key=" +
                        URLEncoder.encode(imageUrl, StandardCharsets.UTF_8.toString()) + "&preview=true";

                HttpGet previewRequest = new HttpGet(previewUrl);
                previewRequest.setHeader("Authorization", "OAuth " + oauthToken);

                try (CloseableHttpResponse response = client.execute(previewRequest)) {
                    if (response.getStatusLine().getStatusCode() == 200) {
                        String jsonResponse = EntityUtils.toString(response.getEntity());
                        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

                        if (jsonObject.has("href")) {
                            String directPreviewUrl = jsonObject.get("href").getAsString();
                            logger.info("Updated image URL to preview URL: {} -> {}", imageUrl, directPreviewUrl);
                            return directPreviewUrl;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to get preview URL for {}: {}", imageUrl, e.getMessage());
            }
        }

        return imageUrl;
    }
}