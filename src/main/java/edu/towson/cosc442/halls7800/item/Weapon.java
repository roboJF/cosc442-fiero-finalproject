package edu.towson.cosc442.halls7800.item;

import edu.towson.cosc442.halls7800.combat.AttackProfile;
import java.util.Objects;

/**
 * A melee weapon. Each upgrade level adds 1 to both ends of the damage range and 1 to accuracy.
 * Heavy weapons take longer to swing.
 */
public class Weapon extends EquippableItem {

    /**
     * Weapon kinds. Columns: name, damage range, accuracy bonus, crit percent, ticks per attack,
     * shallowest depth found, spawn weight, value, attack verb.
     */
    public enum Kind {
        DAGGER("dagger", 2, 4, 2, 10, 100, 0, 8, 10, "stab"),
        SHORT_SWORD("short sword", 3, 6, 1, 5, 100, 1, 10, 25, "slash"),
        MACE("mace", 4, 7, 0, 5, 100, 2, 8, 30, "bash"),
        SPEAR("spear", 3, 8, 1, 5, 100, 3, 7, 35, "skewer"),
        RAPIER("rapier", 3, 7, 3, 15, 100, 4, 5, 50, "lunge at"),
        LONGSWORD("longsword", 5, 10, 0, 5, 100, 4, 6, 60, "cleave"),
        WAR_AXE("war axe", 6, 12, -1, 10, 100, 6, 5, 80, "hack"),
        GREAT_HAMMER("great hammer", 8, 16, -2, 5, 150, 7, 4, 100, "smash");

        private final String displayName;
        private final int minDamage;
        private final int maxDamage;
        private final int accuracyBonus;
        private final int critPercent;
        private final int attackCost;
        private final int minDepth;
        private final int spawnWeight;
        private final int value;
        private final String verb;

        Kind(String displayName, int minDamage, int maxDamage, int accuracyBonus, int critPercent, int attackCost,
             int minDepth, int spawnWeight, int value, String verb) {
            this.displayName = displayName;
            this.minDamage = minDamage;
            this.maxDamage = maxDamage;
            this.accuracyBonus = accuracyBonus;
            this.critPercent = critPercent;
            this.attackCost = attackCost;
            this.minDepth = minDepth;
            this.spawnWeight = spawnWeight;
            this.value = value;
            this.verb = verb;
        }

        /** @return display name */
        public String displayName() {
            return displayName;
        }

        /** @return smallest base damage */
        public int minDamage() {
            return minDamage;
        }

        /** @return largest base damage */
        public int maxDamage() {
            return maxDamage;
        }

        /** @return accuracy added by the weapon */
        public int accuracyBonus() {
            return accuracyBonus;
        }

        /** @return ticks per attack */
        public int attackCost() {
            return attackCost;
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
     * @param kind the weapon kind
     * @param upgrade upgrade level (negative = cursed)
     * @param known whether the upgrade level is known
     */
    public Weapon(Kind kind, int upgrade, boolean known) {
        super(upgrade, known);
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    /** @return the weapon kind */
    public Kind kind() {
        return kind;
    }

    @Override
    protected String kindName() {
        return kind.displayName;
    }

    @Override
    public ItemCategory category() {
        return ItemCategory.WEAPON;
    }

    /** @return accuracy this weapon adds, including upgrades */
    public int accuracyBonus() {
        return kind.accuracyBonus + upgrade();
    }

    /** @return ticks per attack with this weapon */
    public int attackCost() {
        return kind.attackCost;
    }

    /**
     * Builds the attack made with this weapon.
     *
     * @param wielderAccuracy the wielder's total accuracy (already including this weapon)
     * @return the attack profile
     */
    public AttackProfile profile(int wielderAccuracy) {
        int min = Math.max(0, kind.minDamage + upgrade());
        int max = Math.max(min, kind.maxDamage + upgrade());
        return AttackProfile.basic(wielderAccuracy, min, max, kind.critPercent, kind.verb);
    }

    @Override
    public int value() {
        return Math.max(1, kind.value + 10 * upgrade());
    }

    @Override
    protected Item copyWithQuantity(int newQuantity) {
        return new Weapon(kind, upgrade(), isKnown());
    }
}
