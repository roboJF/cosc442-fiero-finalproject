package edu.towson.cosc442.halls7800.item;

import java.util.Objects;

/**
 * Something to eat, which staves off hunger.
 */
public final class Food extends Item {

    /**
     * Food kinds. Columns: singular, plural, satiety restored, HP restored, spawn weight, value.
     */
    public enum Kind {
        ENERGY_BAR("energy bar", "energy bars", 500, 2, 10, 6),
        TRAIL_RATION("trail ration", "trail rations", 1000, 3, 8, 12),
        MYSTERY_MEAT("chunk of mystery meat", "chunks of mystery meat", 700, 0, 4, 4);

        private final String singular;
        private final String plural;
        private final int satiety;
        private final int healing;
        private final int spawnWeight;
        private final int value;

        Kind(String singular, String plural, int satiety, int healing, int spawnWeight, int value) {
            this.singular = singular;
            this.plural = plural;
            this.satiety = satiety;
            this.healing = healing;
            this.spawnWeight = spawnWeight;
            this.value = value;
        }

        /** @return hunger points restored */
        public int satiety() {
            return satiety;
        }

        /** @return HP restored */
        public int healing() {
            return healing;
        }

        /** @return relative chance of being generated */
        public int spawnWeight() {
            return spawnWeight;
        }
    }

    private final Kind kind;

    /**
     * @param kind food kind
     * @param quantity stack size
     */
    public Food(Kind kind, int quantity) {
        super(quantity);
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    /** @return the food kind */
    public Kind kind() {
        return kind;
    }

    @Override
    public ItemCategory category() {
        return ItemCategory.FOOD;
    }

    @Override
    public String name(Identification id) {
        return kind.singular;
    }

    @Override
    public String pluralName(Identification id) {
        return kind.plural;
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
        return other instanceof Food f && f.kind == kind;
    }

    @Override
    protected Item copyWithQuantity(int newQuantity) {
        return new Food(kind, newQuantity);
    }
}
