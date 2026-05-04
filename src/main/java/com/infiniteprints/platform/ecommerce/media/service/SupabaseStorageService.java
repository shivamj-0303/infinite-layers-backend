package com.infiniteprints.platform.ecommerce.media.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SupabaseStorageService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String serviceKey;

    @Value("${supabase.storage.bucket.products}")
    private String productsBucket;

    public String uploadProductImage(UUID productId, MultipartFile file, String type) throws Exception {

        String fileName = UUID.randomUUID() + getExtension(file.getOriginalFilename());

        String path = "products/" + productId + "/" + type + "/" + fileName;

        uploadToSupabase(productsBucket, path, file);

        return supabaseUrl + "/storage/v1/object/public/" + productsBucket + "/" + path;
    }

    public void deleteFileByUrl(String publicUrl) {
        String path = extractPathFromUrl(publicUrl);
        deleteFileByPath(path);
    }

    private void deleteFileByPath(String path) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceKey);
        headers.set("apikey", serviceKey);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        restTemplate.exchange(
                supabaseUrl + "/storage/v1/object/" + productsBucket + "/" + path,
                HttpMethod.DELETE,
                request,
                Void.class
        );
    }

    private void uploadToSupabase(String bucket, String path, MultipartFile file) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceKey);
        headers.set("apikey", serviceKey);
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));

        HttpEntity<byte[]> request = new HttpEntity<>(file.getBytes(), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                supabaseUrl + "/storage/v1/object/" + bucket + "/" + path,
                HttpMethod.POST,
                request,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Upload failed: " + response.getBody());
        }
    }

    private String buildPublicUrl(String path) {
        return supabaseUrl + "/storage/v1/object/public/" + productsBucket + "/" + path;
    }

    private String extractPathFromUrl(String url) {
        return url.replace(
                supabaseUrl + "/storage/v1/object/public/" + productsBucket + "/",
                ""
        );
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }
}