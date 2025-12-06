package com.june.holo.db.dto;

import lombok.Data;

@Data
public class HoloStats {
    public final Long sessionCount;
    public final Long windowCount;

    public HoloStats(Long sessionCount, Long windowCount) {
        this.sessionCount = sessionCount;
        this.windowCount = windowCount;
    }
}
