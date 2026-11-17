package edu.towson.cosc442.halls7800.item;

import java.util.Objects;

/**
 * A drinkable potion. Until a kind is identified, potions of that kind are known only by their
 * color, which is shuffled every run.
 */
public class Potion extends Item {

    /**
     * Potion kinds. Columns: identified name, beneficial, spawn weight, value.
     */
    public enum Kind {
        HEALING("healing", true, 25, 30),
        VITALITY("vitality", true, 3, 120),
        HASTE("haste", true, 8, 40),
        SHIELDING("shielding", true, 8, 40),
        CLEANSING("cleansing", true, 8, 30),
        REGENERATION("regeneration", true, 8, 35),
        POISON("poison", false, 8, 10),
        CONFUSION("confusion", false, 7, 10),
        BLINDNESS("blindness", false, 6, 10),
        LIQUID_FIRE("liquid fire", false, 6, 15);

        private final String displayName;
        private final boolean beneficial;
        private final int spawnWeight;
        private final int value;

        Kind(String displayName, boolean beneficial, int spawnWeight, int value) {
            this.displayName = displayName;
            this.beneficial = beneficial;
            this.spawnWeight = spawnWeight;
            this.value = value;
        }

        /** @return identified name, e.g. "healing" */
        public String displayName() {
            return displayName;
        }

        /** @return {@code true} if drinking it helps */
        public boolean isBeneficial() {
            return beneficial;
        }

        /** @return relative chance of being generated */
        public int spawnWeight() {
            return spawnWeight;
        }
    }

    private final Kind kind;

    /**
     * @param kind the potion kind
     * @param quantity stack size
     */
    public Potion(Kind kind, int quantity) {
        super(quantity);
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    /** @return the potion kind */
    public Kind kind() {
        return kind;
    }

    @Override
    public ItemCategory category() {
        return ItemCategory.POTION;
    }

    @Override
    public String name(Identification id) {
        return id.isKnown(kind) ? "potion of " + kind.displayName : id.colorOf(kind) + " potion";
    }

    @Override
    public String pluralName(Identification id) {
        return id.isKnown(kind) ? "potions of " + kind.displayName : id.colorOf(kind) + " potions";
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
        return other instanceof Potion p && p.kind == kind;
    }

    @Override
    protected Item copyWithQuantity(int newQuantity) {
        return new Potion(kind, newQuantity);
    }
}
