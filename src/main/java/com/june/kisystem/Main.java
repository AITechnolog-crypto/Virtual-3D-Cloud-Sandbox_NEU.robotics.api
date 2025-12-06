package com.june.kisystem;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        KISystem kiSystem = new KISystem();
        kiSystem.addComponent("SolarPanel", new SolarPanel());
        kiSystem.addComponent("Battery", new Battery());
        kiSystem.addComponent("Drone", new Drone());

        kiSystem.decideAndAct(SystemState.SONNIG);
        kiSystem.decideAndAct(SystemState.NACHT);
        kiSystem.decideAndAct(SystemState.UEBERWACHUNG);
        kiSystem.decideAndAct(SystemState.WARTUNG);
        kiSystem.decideAndAct(SystemState.FEHLER);
        kiSystem.decideAndAct(SystemState.valueOf("SONNIG"));
        try {
            kiSystem.performComponentFunction("NichtExistierendeKomponente");
        } catch (ComponentNotFoundException e) {
            logger.log(Level.SEVERE, "Fehler in Main: " + e.getMessage(), e);
        }
    }
}
