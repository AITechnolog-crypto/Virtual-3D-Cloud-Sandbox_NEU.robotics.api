package com.june.spineaid.model;

import com.june.spineaid.SpineAidNetSystem;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SpineAidSession {
    private String sessionId;
    private String patientName;
    private String status;
    private SpineAidNetSystem system;
    private Patient patientData;
    private List<String> logs;
    private boolean active;
    private boolean securityPassed;
    private int vrModules;
    private int emsDevices;
    private int tensDevices;
    private double currentPainLevel;
    private int treatmentsCompleted;
    private double walletBalance;
    private double fundsTransferred;

    public SpineAidSession(String sessionId, String patientName, SpineAidNetSystem system) {
        this.sessionId = sessionId;
        this.patientName = patientName;
        this.system = system;
        this.status = "INITIALISIERT";
        this.logs = new ArrayList<>();
        this.active = false;
        this.securityPassed = false;
        this.vrModules = 0;
        this.emsDevices = 0;
        this.tensDevices = 0;
        this.currentPainLevel = 8.0;
        this.treatmentsCompleted = 0;
        this.walletBalance = 5000 + Math.random() * 3000;
        this.fundsTransferred = 0.0;
    }

    public void addLog(String message) {
        logs.add(message);
        if (logs.size() > 200) logs.remove(0);
    }

    public synchronized void incrementVrModules() { vrModules++; }
    public synchronized void incrementEmsDevices() { emsDevices++; }
    public synchronized void incrementTensDevices() { tensDevices++; }
    public synchronized void incrementTreatmentsCompleted() { treatmentsCompleted++; }
    public synchronized void incrementFundsTransferred(double amount) { fundsTransferred += amount; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("sessionId", sessionId);
        map.put("patientName", patientName);
        map.put("status", status);
        map.put("active", active);
        map.put("securityPassed", securityPassed);
        map.put("vrModules", vrModules);
        map.put("emsDevices", emsDevices);
        map.put("tensDevices", tensDevices);
        map.put("currentPainLevel", currentPainLevel);
        map.put("treatmentsCompleted", treatmentsCompleted);
        map.put("walletBalance", walletBalance);
        map.put("fundsTransferred", fundsTransferred);
        map.put("logs", logs);
        return map;
    }
}
