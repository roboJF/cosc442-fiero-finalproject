package edu.towson.cosc442.halls7800.effect;

/**
 * The status effects a creature can be under, with how repeated applications combine.
 *
 * <p>Message templates use the creature as the subject: "You are poisoned." / "The imp is
 * poisoned."
 */
public enum EffectType {
    /** Loses {@code magnitude} HP each turn. Repeated doses add to the magnitude. */
    POISONED("poisoned", "no longer poisoned", Stacking.INTENSIFY, true),
    /** Takes {@code magnitude} fire damage each turn. Put out by water; cannot start while wet. */
    BURNING("on fire", "no longer burning", Stacking.REFRESH, true),
    /** Soaked: immune to catching fire. Applying it puts out burning. */
    WET("soaked", "dry again", Stacking.REFRESH, false),
    /** Cannot act. Cannot be re-applied while already stunned. */
    STUNNED("stunned", "no longer stunned", Stacking.NONE, true),
    /** Actions take twice as long. Cancels haste. */
    SLOWED("slowed", "moving normally again", Stacking.EXTEND, true),
    /** Actions take half as long. Cancels slow. */
    HASTED("hasted", "back to normal speed", Stacking.EXTEND, false),
    /** Deals one third less damage. */
    WEAKENED("weakened", "no longer weakened", Stacking.REFRESH, true),
    /** Absorbs up to {@code magnitude} damage before breaking. */
    SHIELDED("shielded", "no longer shielded", Stacking.INTENSIFY, false),
    /** Regains {@code magnitude} HP each turn. */
    REGENERATING("regenerating", "no longer regenerating", Stacking.EXTEND, false),
    /** Sight shrinks to one tile and accuracy drops. */
    BLINDED("blinded", "able to see again", Stacking.REFRESH, true),
    /** Half of all moves go in a random direction. */
    CONFUSED("confused", "thinking clearly again", Stacking.REFRESH, true);

    /** How a new application combines with an existing effect of the same type. */
    public enum Stacking {
        /** Keep the longer of the two durations; magnitude takes the larger value. */
        REFRESH,
        /** Add the durations (up to {@link EffectType#MAX_DURATION}). */
        EXTEND,
        /** Add the magnitudes and keep the longer duration. */
        INTENSIFY,
        /** Ignore new applications while active. */
        NONE
    }

    /** Longest duration any effect can have, in turns. */
    public static final int MAX_DURATION = 50;

    private final String adjective;
    private final String endPhrase;
    private final Stacking stacking;
    private final boolean harmful;

    EffectType(String adjective, String endPhrase, Stacking stacking, boolean harmful) {
        this.adjective = adjective;
        this.endPhrase = endPhrase;
        this.stacking = stacking;
        this.harmful = harmful;
    }

    /** @return word used when the effect starts, e.g. "poisoned" */
    public String adjective() {
        return adjective;
    }

    /** @return phrase used when the effect ends, e.g. "no longer poisoned" */
    public String endPhrase() {
        return endPhrase;
    }

    /** @return how repeated applications combine */
    public Stacking stacking() {
        return stacking;
    }

    /** @return {@code true} for effects a cure should remove */
    public boolean isHarmful() {
        return harmful;
    }

    /** @return the name for display, e.g. "Poisoned" */
    public String displayName() {
        String lower = name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
