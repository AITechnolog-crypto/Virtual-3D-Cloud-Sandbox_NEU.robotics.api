package com.june.integrated;

/**
 * SystemIntegration: Startpunkt gemäß Anforderung.
 */
public class SystemIntegration {
    public static void main(String[] args) {
        IAncientTechnology ancientTech = new FireplanezTechnology();
        IModernTechnology modernTech = new ModernCloudSystem();
        IntegratedTechnologySystem integratedSystem = new IntegratedTechnologySystem(
                ancientTech,
                modernTech,
                new MachineLearningModel() // vorhandenes ML-Modell (wird in diesem Flow nicht zwingend genutzt)
        );
        integratedSystem.integrateAndOperate();
    }
}
