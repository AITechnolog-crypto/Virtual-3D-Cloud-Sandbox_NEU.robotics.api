package com.june.integrated;

/**
 * Kleiner Demo-Entry-Point zur Ausführung des integrierten Systems
 * ohne Abhängigkeit zu Spring Boot. Kann unabhängig gestartet werden.
 */
public class IntegratedTechnologyDemo {
    public static void main(String[] args) {
        IAncientTechnology ancient = new AncientTechnology();
        IModernTechnology modern = new ModernTechnology();
        IMachineLearningModel model = new MachineLearningModel();

        IntegratedTechnologySystem system = new IntegratedTechnologySystem(ancient, modern, model);
        system.integrateAndOperate();
    }
}
