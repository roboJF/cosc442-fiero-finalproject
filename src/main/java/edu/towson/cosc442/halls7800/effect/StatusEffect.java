package edu.towson.cosc442.halls7800.effect;

import java.util.Objects;

/**
 * One active status effect on a creature: its type, turns remaining, and strength.
 */
public final class StatusEffect {

    private final EffectType type;
    private int remaining;
    private int magnitude;

    /**
     * @param type the effect
     * @param remaining turns left (at least 1)
     * @param magnitude strength, meaning depends on the type (damage per turn, shield points, ...)
     */
    public StatusEffect(EffectType type, int remaining, int magnitude) {
        this.type = Objects.requireNonNull(type, "type");
        if (remaining < 1) {
            throw new IllegalArgumentException("duration must be at least 1");
        }
        this.remaining = Math.min(remaining, EffectType.MAX_DURATION);
        this.magnitude = magnitude;
    }

    /** @return the effect type */
    public EffectType type() {
        return type;
    }

    /** @return turns left */
    public int remaining() {
        return remaining;
    }

    /** @return the effect's strength */
    public int magnitude() {
        return magnitude;
    }

    void setRemaining(int remaining) {
        this.remaining = Math.max(0, Math.min(remaining, EffectType.MAX_DURATION));
    }

    void setMagnitude(int magnitude) {
        this.magnitude = magnitude;
    }

    /**
     * Counts down one turn.
     *
     * @return {@code true} if the effect has now expired
     */
    boolean countDown() {
        remaining--;
        return remaining <= 0;
    }

    @Override
    public String toString() {
        return type.displayName() + "(" + remaining + (magnitude != 0 ? ", " + magnitude : "") + ")";
    }
}
