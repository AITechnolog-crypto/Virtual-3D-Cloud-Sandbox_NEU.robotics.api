package com.june.energy;

import java.util.Objects;

/**
 * Repräsentiert eine Energiemenge eines Typs. Unveränderlich (immutable).
 *
 * @param amount in Wh oder abstrakten Einheiten
 */
public record Energy(EnergyType type, double amount) {
    public Energy {
        if (type == null) throw new IllegalArgumentException("EnergyType darf nicht null sein");
        if (!Double.isFinite(amount) || amount < 0)
            throw new IllegalArgumentException("amount muss >= 0 und endlich sein");
    }

    public Energy add(Energy other) {
        Objects.requireNonNull(other, "other");
        if (this.type != other.type) throw new IllegalArgumentException("Energietypen müssen übereinstimmen");
        return new Energy(this.type, this.amount + other.amount);
    }

    @Override
    public String toString() {
        return "Energy{" +
                "type=" + type +
                ", amount=" + amount +
                '}';
    }
}
