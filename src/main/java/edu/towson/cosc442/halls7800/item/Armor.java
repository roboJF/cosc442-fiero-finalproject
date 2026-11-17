package edu.towson.cosc442.halls7800.item;

import java.util.Objects;

/**
 * Body armor. Heavier armor protects more but makes the wearer easier to hit.
 */
public class Armor extends EquippableItem {

    /**
     * Armor kinds. Columns: name, armor rating, evasion penalty, shallowest depth found, spawn
     * weight, value.
     */
    public enum Kind {
        /** Starting gear: warm, comfortable, gold and black. Not found in the Halls. */
        TU_HOODIE("worn TU hoodie", 1, 0, 99, 0, 5),
        LEATHER("leather armor", 2, 0, 1, 10, 20),
        STUDDED_LEATHER("studded leather", 3, 0, 2, 8, 35),
        CHAIN_MAIL("chain mail", 4, 1, 3, 7, 55),
        SCALE_MAIL("scale mail", 5, 2, 5, 5, 75),
        PLATE_ARMOR("plate armor", 7, 3, 7, 4, 110);

        private final String displayName;
        private final int armor;
        private final int evasionPenalty;
        private final int minDepth;
        private final int spawnWeight;
        private final int value;

        Kind(String displayName, int armor, int evasionPenalty, int minDepth, int spawnWeight, int value) {
            this.displayName = displayName;
            this.armor = armor;
            this.evasionPenalty = evasionPenalty;
            this.minDepth = minDepth;
            this.spawnWeight = spawnWeight;
            this.value = value;
        }

        /** @return display name */
        public String displayName() {
            return displayName;
        }

        /** @return base armor rating */
        public int armor() {
            return armor;
        }

        /** @return evasion lost while wearing it */
        public int evasionPenalty() {
            return evasionPenalty;
        }

        /** @return shallowest depth where it is found */
        public int minDepth() {
            return minDepth;
        }

        /** @return relative chance of being generated */
        public int spawnWeight() {
            return spawnWeight;
        }
    }

    private final Kind kind;

    /**
     * @param kind the armor kind
     * @param upgrade upgrade level (negative = cursed)
     * @param known whether the upgrade level is known
     */
    public Armor(Kind kind, int upgrade, boolean known) {
        super(upgrade, known);
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    /** @return the armor kind */
    public Kind kind() {
        return kind;
    }

    @Override
    protected String kindName() {
        return kind.displayName;
    }

    @Override
    public ItemCategory category() {
        return ItemCategory.ARMOR;
    }

    /** @return armor rating including upgrades (never negative) */
    public int armorValue() {
        return Math.max(0, kind.armor + upgrade());
    }

    /** @return evasion lost while wearing it */
    public int evasionPenalty() {
        return kind.evasionPenalty;
    }

    @Override
    public int value() {
        return Math.max(1, kind.value + 10 * upgrade());
    }

    @Override
    protected Item copyWithQuantity(int newQuantity) {
        return new Armor(kind, upgrade(), isKnown());
    }
}
