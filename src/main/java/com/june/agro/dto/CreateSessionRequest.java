package com.june.agro.dto;

import lombok.Data;

@Data
public class CreateSessionRequest {
    private String sessionId;
    private String owner;
    private String markerHash;
}
