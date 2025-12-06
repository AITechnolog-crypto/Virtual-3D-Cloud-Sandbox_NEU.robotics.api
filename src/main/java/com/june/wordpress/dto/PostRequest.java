package com.june.wordpress.dto;

import lombok.Data;

@Data
public class PostRequest {
    private String title;
    private String content;
    private String status = "draft"; // draft, publish, private
}
