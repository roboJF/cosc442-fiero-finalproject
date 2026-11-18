package edu.towson.cosc442.halls7800.item;

import java.util.Objects;

/**
 * A magic wand with a limited number of charges. Using a wand fires a bolt at the nearest monster
 * in view. Wands slowly regain charges while carried: one charge every
 * {@link #RECHARGE_TURNS} turns.
 */
public final class Wand extends Item {

    /** Turns needed to regain one charge. */
    public static final int RECHARGE_TURNS = 60;
    /** Maximum distance a bolt travels. */
    public static final int RANGE = 8;

    /**
     * Wand kinds. Columns: name, charge range, spawn weight, value.
     */
    public enum Kind {
        /** A bolt of fire that damages, may set the target alight, and ignites grass it crosses. */
        FIREBOLT("firebolt", 3, 5, 8, 60),
        /** Rate-limits the target: it is slowed for a while. */
        THROTTLING("throttling", 3, 6, 6, 50),
        /** Interrupts the target: a small shock that stuns it briefly. */
        INTERRUPTS("interrupts", 3, 5, 5, 55);

        private final String displayName;
        private final int minCharges;
        private final int maxCharges;
        private final int spawnWeight;
        private final int value;

        Kind(String displayName, int minCharges, int maxCharges, int spawnWeight, int value) {
            this.displayName = displayName;
            this.minCharges = minCharges;
            this.maxCharges = maxCharges;
            this.spawnWeight = spawnWeight;
            this.value = value;
        }

        /** @return identified name, e.g. "firebolt" */
        public String displayName() {
            return displayName;
        }

        /** @return fewest charges a new wand has */
        public int minCharges() {
            return minCharges;
        }

        /** @return most charges a new wand has */
        public int maxCharges() {
            return maxCharges;
        }

        /** @return relative chance of being generated */
        public int spawnWeight() {
            return spawnWeight;
        }
    }

    private final Kind kind;
    private final int capacity;
    private int charges;
    private int rechargeProgress;

    /**
     * @param kind the wand kind
     * @param charges starting (and maximum) charges
     */
    public Wand(Kind kind, int charges) {
        super(1);
        this.kind = Objects.requireNonNull(kind, "kind");
        if (charges < 1) {
            throw new IllegalArgumentException("A wand needs at least one charge");
        }
        this.capacity = charges;
        this.charges = charges;
    }

    /** @return the wand kind */
    public Kind kind() {
        return kind;
    }

    /** @return charges left */
    public int charges() {
        return charges;
    }

    /** @return most charges the wand can hold */
    public int capacity() {
        return capacity;
    }

    /**
     * Uses up one charge.
     *
     * @return {@code true} if a charge was available
     */
    public boolean useCharge() {
        if (charges <= 0) {
            return false;
        }
        charges--;
        return true;
    }

    /**
     * Advances recharging by one turn.
     *
     * @return {@code true} if a charge was regained this turn
     */
    public boolean tickRecharge() {
        if (charges >= capacity) {
            rechargeProgress = 0;
            return false;
        }
        rechargeProgress++;
        if (rechargeProgress >= RECHARGE_TURNS) {
            rechargeProgress = 0;
            charges++;
            return true;
        }
        return false;
    }

    @Override
    public ItemCategory category() {
        return ItemCategory.WAND;
    }

    @Override
    public String name(Identification id) {
        return "wand of " + kind.displayName + " (" + charges + "/" + capacity + ")";
    }

    @Override
    public int value() {
        return kind.value + 5 * charges;
    }

    @Override
    protected Item copyWithQuantity(int newQuantity) {
        Wand copy = new Wand(kind, capacity);
        copy.charges = charges;
        return copy;
    }
}
