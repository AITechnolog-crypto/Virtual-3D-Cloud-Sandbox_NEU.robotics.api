package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class BackupForGkeService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public BackupForGkeService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String listBackupPlans(String location) {
        List<String> keys = apiKeysConfig.getGoogle().getBackupForGke().getKeys();
        return execute("backupforgke.backupplans.list", keys, location);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url = endpoint + "/v1/projects/your-project-id/locations/" + request + "/backupPlans?key=" + apiKey;
        logger.info("Calling Backup for GKE API: {}", url);
        // Simulate a successful response
        return "{ \"backupPlans\": [ { \"name\": \"your-backup-plan\", \"state\": \"READY\" } ] }";
    }
}
