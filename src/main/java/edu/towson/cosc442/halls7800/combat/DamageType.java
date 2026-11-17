package edu.towson.cosc442.halls7800.combat;

/**
 * Kinds of damage. Some creatures are immune to particular kinds.
 */
public enum DamageType {
    /** Blades, claws, fists. Reduced by armor. */
    PHYSICAL,
    /** Flames. Not reduced by armor. */
    FIRE,
    /** Venom and toxins. Not reduced by armor. */
    POISON,
    /** Raw magic. Not reduced by armor. */
    ARCANE;

    /** @return {@code true} if armor reduces this kind of damage */
    public boolean reducedByArmor() {
        return this == PHYSICAL;
    }
}
