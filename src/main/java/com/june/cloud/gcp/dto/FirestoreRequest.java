package com.june.cloud.gcp.dto;

import lombok.Data;

import java.util.Map;

@Data
public class FirestoreRequest {
    private String collection;
    private String documentId;
    private Map<String, Object> data;
}
