package edu.towson.cosc442.halls7800.world;

import java.util.Objects;

/**
 * A trap hidden in the floor. Traps start hidden; they are revealed when triggered or found by
 * searching. Only the player sets off traps.
 */
public final class Trap {

    /** Kinds of trap. */
    public enum Type {
        /** Spikes shoot up from the floor. */
        SPIKE("spike trap", "#c0c0c0"),
        /** A needle coated in venom. */
        POISON_NEEDLE("poison needle trap", "#70e070"),
        /** A jet of flame. */
        FLAME_VENT("flame vent", "#ff8040"),
        /** A klaxon that wakes the whole floor: BUILD FAILED. */
        ALARM("alarm trap", "#ff4040"),
        /** Sends the victim somewhere else on the floor. */
        TELEPORTER("teleport trap", "#b080ff"),
        /** Drops the victim to the floor below. */
        TRAPDOOR("trapdoor", "#a07040"),
        /** A snare that holds the victim in place. */
        SNARE("snare", "#d0b060");

        private final String displayName;
        private final String color;

        Type(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }

        /** @return display name, e.g. "spike trap" */
        public String displayName() {
            return displayName;
        }

        /** @return display color as a hex string */
        public String color() {
            return color;
        }
    }

    private final Type type;
    private boolean hidden;

    /**
     * @param type the kind of trap
     * @param hidden whether the player has not yet found it
     */
    public Trap(Type type, boolean hidden) {
        this.type = Objects.requireNonNull(type, "type");
        this.hidden = hidden;
    }

    /** @return the kind of trap */
    public Type type() {
        return type;
    }

    /** @return {@code true} if the player has not found it */
    public boolean isHidden() {
        return hidden;
    }

    /** Makes the trap visible to the player. */
    public void reveal() {
        hidden = false;
    }
}
