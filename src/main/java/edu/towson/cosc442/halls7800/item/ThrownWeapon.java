package edu.towson.cosc442.halls7800.item;

import edu.towson.cosc442.halls7800.combat.AttackProfile;
import java.util.Objects;

/**
 * Stackable missiles thrown at a distant target. A thrown missile lands where it hits or stops and
 * can be picked up again.
 */
public class ThrownWeapon extends Item {

    /** Maximum throwing distance in tiles. */
    public static final int RANGE = 7;

    /**
     * Missile kinds. Columns: singular, plural, damage range, accuracy bonus, shallowest depth,
     * spawn weight, value each.
     */
    public enum Kind {
        DART("dart", "darts", 1, 3, 2, 1, 10, 2),
        THROWING_KNIFE("throwing knife", "throwing knives", 2, 5, 1, 2, 7, 4),
        JAVELIN("javelin", "javelins", 4, 8, -1, 4, 5, 8);

        private final String singular;
        private final String plural;
        private final int minDamage;
        private final int maxDamage;
        private final int accuracyBonus;
        private final int minDepth;
        private final int spawnWeight;
        private final int value;

        Kind(String singular, String plural, int minDamage, int maxDamage, int accuracyBonus, int minDepth,
             int spawnWeight, int value) {
            this.singular = singular;
            this.plural = plural;
            this.minDamage = minDamage;
            this.maxDamage = maxDamage;
            this.accuracyBonus = accuracyBonus;
            this.minDepth = minDepth;
            this.spawnWeight = spawnWeight;
            this.value = value;
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
     * @param kind missile kind
     * @param quantity stack size
     */
    public ThrownWeapon(Kind kind, int quantity) {
        super(quantity);
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    /** @return the missile kind */
    public Kind kind() {
        return kind;
    }

    /**
     * @param throwerAccuracy the thrower's accuracy
     * @return the attack made by one missile
     */
    public AttackProfile profile(int throwerAccuracy) {
        return AttackProfile.basic(throwerAccuracy + kind.accuracyBonus, kind.minDamage, kind.maxDamage, 5,
                "throw " + Item.article(kind.singular) + " " + kind.singular + " at");
    }

    @Override
    public ItemCategory category() {
        return ItemCategory.THROWN;
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
        return other instanceof ThrownWeapon t && t.kind == kind;
    }

    @Override
    protected Item copyWithQuantity(int newQuantity) {
        return new ThrownWeapon(kind, newQuantity);
    }
}
