package edu.towson.cosc442.halls7800.item;

import java.util.Objects;

/**
 * A single-use magic scroll. Until a kind is identified, its scrolls are known only by the strange
 * label written on them, which is shuffled every run. Scrolls cannot be read while blind.
 */
public final class Scroll extends Item {

    /**
     * Scroll kinds. Columns: identified name, spawn weight, value.
     */
    public enum Kind {
        /** Identifies one unknown item in the pack. */
        INTROSPECTION("introspection", 20, 25),
        /** Upgrades the wielded weapon (or worn armor) by one level, lifting any curse. */
        REFACTORING("refactoring", 14, 60),
        /** Lifts the curses from everything carried. */
        DEBUGGING("debugging", 10, 35),
        /** Reveals the layout of the whole floor. */
        DOCUMENTATION("documentation", 10, 40),
        /** Teleports the reader to a random spot on the floor. */
        CONTEXT_SWITCHING("context switching", 9, 30),
        /** Terrifies every monster in view. */
        CODE_REVIEW("code review", 8, 35);

        private final String displayName;
        private final int spawnWeight;
        private final int value;

        Kind(String displayName, int spawnWeight, int value) {
            this.displayName = displayName;
            this.spawnWeight = spawnWeight;
            this.value = value;
        }

        /** @return identified name, e.g. "refactoring" */
        public String displayName() {
            return displayName;
        }

        /** @return relative chance of being generated */
        public int spawnWeight() {
            return spawnWeight;
        }
    }

    private final Kind kind;

    /**
     * @param kind the scroll kind
     * @param quantity stack size
     */
    public Scroll(Kind kind, int quantity) {
        super(quantity);
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    /** @return the scroll kind */
    public Kind kind() {
        return kind;
    }

    @Override
    public ItemCategory category() {
        return ItemCategory.SCROLL;
    }

    @Override
    public String name(Identification id) {
        return id.isKnown(kind) ? "scroll of " + kind.displayName : "scroll labeled " + id.labelOf(kind);
    }

    @Override
    public String pluralName(Identification id) {
        return id.isKnown(kind) ? "scrolls of " + kind.displayName : "scrolls labeled " + id.labelOf(kind);
    }

    @Override
    public int value() {
        return kind.value;
    }

    @Override
    public boolean isStackable() {
        return true;
    }

    @Override
    public boolean canStackWith(Item other) {
        return other instanceof Scroll s && s.kind == kind;
    }

    @Override
    protected Item copyWithQuantity(int newQuantity) {
        return new Scroll(kind, newQuantity);
    }
}
