package com.june.geospatial.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgentCommunicationBus {

    private static final Logger logger = LoggerFactory.getLogger(AgentCommunicationBus.class);
    private final Map<String, IAgent> agents = new ConcurrentHashMap<>();

    public void register(IAgent agent) {
        agents.put(agent.getAgentName(), agent);
        logger.info("Agent '{}' hat sich am Kommunikations-Bus registriert.", agent.getAgentName());
    }

    public void sendMessage(String sender, String recipient, String message) {
        logger.info("Nachricht von '{}' an '{}': {}", sender, recipient, message);
        IAgent recipientAgent = agents.get(recipient);
        if (recipientAgent != null) {
            recipientAgent.receiveMessage(sender, message);
        } else {
            logger.warn("Empfänger-Agent '{}' nicht gefunden.", recipient);
        }
    }
}
