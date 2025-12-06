package com.june.holo.mongo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WindowData {
    private String windowId;
    private String title;
    private String app;
    private Integer orbit;
    private Integer x;
    private Integer y;
    private Integer width;
    private Integer height;
    private Boolean focused;
    private Integer zIndex;
    private LocalDateTime createdAt;
}
