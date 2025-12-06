package com.june.cloud.service;

import com.june.cloud.components.Dataset;
import org.springframework.stereotype.Service;

/**
 * Platzhalter für DataService
 */
@Service
class DataService {
    public Dataset loadDataset(String name) {
        return new Dataset(name); // Simuliert
    }
}
