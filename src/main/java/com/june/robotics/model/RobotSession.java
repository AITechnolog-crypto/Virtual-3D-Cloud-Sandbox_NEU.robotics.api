package com.june.robotics.model;

import java.util.*;

public class RobotSession {
    private final String robotId;
    private final String robotName;
    private final long startTime;
    private final List<String> logs;
    private String status;
    private boolean running;
    private String hardwareStatus;
    private boolean aiControllerActive;
    private SensorData currentSensorData;
    private Map<String, Double> position;
    private double batteryLevel;
    private String lastAction;

    // Sensors
    private boolean ultrasonicActive;
    private boolean infraredActive;
    private boolean cameraActive;

    // Actuators
    private boolean motorActive;
    private boolean gripperActive;

    public RobotSession(String robotId, String robotName) {
        this.robotId = robotId;
        this.robotName = robotName;
        this.startTime = System.currentTimeMillis();
        this.status = "INITIALIZING";
        this.running = false;
        this.hardwareStatus = "OFFLINE";
        this.aiControllerActive = false;
        this.logs = Collections.synchronizedList(new ArrayList<>());
        this.currentSensorData = new SensorData();
        this.position = new HashMap<>();
        position.put("x", 0.0);
        position.put("y", 0.0);
        position.put("z", 0.0);
        this.batteryLevel = 100.0;
        this.lastAction = "IDLE";
    }

    public void initializeSensors() {
        this.ultrasonicActive = true;
        this.infraredActive = true;
        this.cameraActive = true;
    }

    public void initializeActuators() {
        this.motorActive = true;
        this.gripperActive = true;
    }

    public SensorData readSensors() {
        currentSensorData = new SensorData();
        currentSensorData.setUltrasoundDistance(Math.random() * 100);
        currentSensorData.setInfraredValue(Math.random() * 100);
        currentSensorData.setCameraImage("Camera_Frame_" + System.currentTimeMillis());
        return currentSensorData;
    }

    public Decision makeDecision(SensorData data) {
        // KI-Logik
        if (data.getUltrasoundDistance() < 20) {
            return new Decision("Hindernis umgehen");
        }
        if (data.getInfraredValue() < 10) {
            return new Decision("Objekt greifen");
        }
        return new Decision("Vorwärts bewegen");
    }

    public void executeAction(Decision decision) {
        String action = decision.getAction();
        this.lastAction = action;

        switch (action.toLowerCase()) {
            case "forward":
            case "vorwärts bewegen":
                addLog("🤖 Motor: Bewege vorwärts");
                position.put("z", position.get("z") + 2.0);  // Z-Achse vorwärts
                batteryLevel -= 0.5;
                break;

            case "backward":
            case "rückwärts":
                addLog("🤖 Motor: Bewege rückwärts");
                position.put("z", position.get("z") - 2.0);  // Z-Achse rückwärts
                batteryLevel -= 0.5;
                break;

            case "left":
            case "links":
                addLog("🤖 Motor: Bewege nach links");
                position.put("x", position.get("x") - 2.0);  // X-Achse links
                batteryLevel -= 0.5;
                break;

            case "right":
            case "rechts":
                addLog("🤖 Motor: Bewege nach rechts");
                position.put("x", position.get("x") + 2.0);  // X-Achse rechts
                batteryLevel -= 0.5;
                break;

            case "grip_open":
            case "griff öffnen":
                addLog("🤖 Greifer: Öffne Greifer");
                batteryLevel -= 0.1;
                break;

            case "grip_close":
            case "griff schließen":
                addLog("🤖 Greifer: Schließe Greifer");
                batteryLevel -= 0.1;
                break;

            case "stop":
                addLog("🤖 Motor: Stopp");
                break;

            case "hindernis umgehen":
                addLog("🤖 Motor: Umgehe Hindernis");
                position.put("y", position.get("y") + 0.5);
                batteryLevel -= 0.3;
                break;

            case "objekt greifen":
                addLog("🤖 Greifer: Greife Objekt");
                batteryLevel -= 0.2;
                break;

            default:
                addLog("🤖 Aktion: " + action);
        }

        batteryLevel = Math.max(0, batteryLevel);
    }

    public void addLog(String log) {
        logs.add(String.format("[%tT] %s", new Date(), log));
        if (logs.size() > 100) {
            logs.remove(0);
        }
    }

    // Getters/Setters
    public String getRobotId() { return robotId; }
    public String getRobotName() { return robotName; }
    public long getStartTime() { return startTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isRunning() { return running; }
    public void setRunning(boolean running) { this.running = running; }
    public String getHardwareStatus() { return hardwareStatus; }
    public void setHardwareStatus(String status) { this.hardwareStatus = status; }
    public boolean isAiControllerActive() { return aiControllerActive; }
    public void setAiControllerActive(boolean active) { this.aiControllerActive = active; }
    public List<String> getLogs() { return new ArrayList<>(logs); }
    public SensorData getCurrentSensorData() { return currentSensorData; }
    public Map<String, Double> getPosition() { return new HashMap<>(position); }
    public double getBatteryLevel() { return batteryLevel; }
    public String getLastAction() { return lastAction; }
}
