package com.june.geospatial.service;

public interface IAgent {
    String getAgentName();
    void receiveMessage(String sender, String message);
}
