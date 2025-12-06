package com.june.cloud.gcp.dto;

import lombok.Data;

@Data
public class UploadRequest {
    private String filename;
    private String content;
}
