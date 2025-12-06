package com.june.ai.model;

import java.util.List;
import java.util.Map;

public class Cluster {
    public final String name;
    public final List<Map<String, Object>> members;

    public Cluster(String name, List<Map<String, Object>> members) {
        this.name = name;
        this.members = members;
    }
}
