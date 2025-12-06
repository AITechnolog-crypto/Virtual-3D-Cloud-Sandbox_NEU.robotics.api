package com.june.autonomy.action;

/**
 * Ressourcensammel-Aktion. In diesem Beispiel immer positiv.
 */
public class CollectResourceAction implements IAction {
    private final String resourceType;

    public CollectResourceAction(String resourceType) {
        this.resourceType = resourceType == null ? "Unknown" : resourceType;
    }

    public String getResourceType() {
        return resourceType;
    }

    @Override
    public boolean isNegative() {
        return false; // Sammeln ist hier stets positiv
    }

    @Override
    public String getDescription() {
        return "Sammle Ressource: " + resourceType;
    }
}
