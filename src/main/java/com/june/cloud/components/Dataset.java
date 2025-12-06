package com.june.cloud.components;

import java.util.*;

/**
 * 📊 Dataset
 *
 * Container für strukturierte Daten
 */
public class Dataset {

    private String name;
    private List<Map<String, Object>> data;
    private Map<String, Object> metadata;

    public Dataset(String name) {
        this.name = name;
        this.data = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Map<String, Object>> getData() { return data; }
    public void setData(List<Map<String, Object>> data) { this.data = data; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public int size() { return data.size(); }

    @Override
    public String toString() {
        return String.format("Dataset[name=%s, rows=%d]", name, data.size());
    }
}
