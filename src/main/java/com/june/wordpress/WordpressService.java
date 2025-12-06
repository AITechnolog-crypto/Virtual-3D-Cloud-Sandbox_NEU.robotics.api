package com.june.wordpress;

import com.june.config.ApiKeysConfig;
import com.june.wordpress.dto.PostRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
public class WordpressService {

    private final RestTemplate restTemplate;
    private final ApiKeysConfig apiKeysConfig;

    public WordpressService(RestTemplate restTemplate, ApiKeysConfig apiKeysConfig) {
        this.restTemplate = restTemplate;
        this.apiKeysConfig = apiKeysConfig;
    }

    public String createPost(PostRequest postRequest) {
        String url = apiKeysConfig.getWordpress().getUrl() + "/wp-json/wp/v2/posts";
        String username = apiKeysConfig.getWordpress().getUsername();
        String password = apiKeysConfig.getWordpress().getPassword();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader);

        HttpEntity<PostRequest> request = new HttpEntity<>(postRequest, headers);

        return restTemplate.postForObject(url, request, String.class);
    }
}
