package com.june.kisystem;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KISystem {
    private final Logger logger = Logger.getLogger(KISystem.class.getName());
    private final Map<String, IComponent> components = new HashMap<>();

    public void addComponent(String name, IComponent component) {
        components.put(name, component);
    }

    public void performComponentFunction(String componentName) throws ComponentNotFoundException {
        if (components.containsKey(componentName)) {
            components.get(componentName).performFunction();
        } else {
            logger.log(Level.WARNING, "Komponente {0} nicht gefunden.", componentName);
            throw new ComponentNotFoundException("Komponente " + componentName + " nicht gefunden.");
        }
    }

    public void decideAndAct(SystemState state) {
        try {
            switch (state) {
                case SONNIG -> performComponentFunction("SolarPanel");
                case NACHT -> performComponentFunction("Battery");
                case UEBERWACHUNG -> performComponentFunction("Drone");
                case WARTUNG -> logger.info("System im Wartungsmodus.");
                case FEHLER -> logger.severe("System befindet sich im Fehlerzustand!");
                default -> logger.warning("Unbekannter Zustand: " + state);
            }
        } catch (ComponentNotFoundException e) {
            logger.log(Level.SEVERE, "Fehler bei der Ausführung der Komponentenfunktion: " + e.getMessage(), e);
        }
    }
}
