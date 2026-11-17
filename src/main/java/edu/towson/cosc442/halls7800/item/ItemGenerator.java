package edu.towson.cosc442.halls7800.item;

import edu.towson.cosc442.halls7800.util.Rng;
import edu.towson.cosc442.halls7800.util.WeightedTable;
import java.util.List;

/**
 * Creates random items. Better weapons and armor appear deeper in the Halls, and deeper floors are
 * more likely to hold upgraded equipment.
 */
public class ItemGenerator {

    /** Percent chance that a defeated monster drops an item. */
    public static final int DROP_PERCENT = 22;
    /** Percent chance that a defeated monster drops gold (if it dropped no item). */
    public static final int GOLD_DROP_PERCENT = 20;

    /**
     * @param depth dungeon depth
     * @return weights for each category of random item at that depth
     */
    public WeightedTable<ItemCategory> categoryTable(int depth) {
        return new WeightedTable<ItemCategory>()
                .add(ItemCategory.POTION, 30)
                .add(ItemCategory.SCROLL, 24)
                .add(ItemCategory.FOOD, 18)
                .add(ItemCategory.WEAPON, 10 + depth / 3)
                .add(ItemCategory.ARMOR, 8 + depth / 3)
                .add(ItemCategory.THROWN, 10)
                .add(ItemCategory.WAND, depth >= 2 ? 5 : 0);
    }

    /**
     * Creates one random item suitable for a depth.
     *
     * @param depth dungeon depth
     * @param rng randomness source
     * @return the item
     */
    public Item randomItem(int depth, Rng rng) {
        ItemCategory category = categoryTable(depth).pick(rng);
        return switch (category) {
            case POTION -> randomPotion(rng);
            case SCROLL -> randomScroll(rng);
            case FOOD -> randomFood(rng);
            case WEAPON -> randomWeapon(depth, rng);
            case ARMOR -> randomArmor(depth, rng);
            case THROWN -> randomThrown(depth, rng);
            case WAND -> randomWand(rng);
            default -> throw new IllegalStateException("Unexpected category " + category);
        };
    }

    /**
     * @param rng randomness source
     * @return a random potion
     */
    public Potion randomPotion(Rng rng) {
        WeightedTable<Potion.Kind> table = new WeightedTable<>();
        for (Potion.Kind k : Potion.Kind.values()) {
            table.add(k, k.spawnWeight());
        }
        return new Potion(table.pick(rng), 1);
    }

    /**
     * @param rng randomness source
     * @return a random scroll
     */
    public Scroll randomScroll(Rng rng) {
        WeightedTable<Scroll.Kind> table = new WeightedTable<>();
        for (Scroll.Kind k : Scroll.Kind.values()) {
            table.add(k, k.spawnWeight());
        }
        return new Scroll(table.pick(rng), 1);
    }

    /**
     * @param rng randomness source
     * @return a random food item
     */
    public Food randomFood(Rng rng) {
        WeightedTable<Food.Kind> table = new WeightedTable<>();
        for (Food.Kind k : Food.Kind.values()) {
            table.add(k, k.spawnWeight());
        }
        return new Food(table.pick(rng), 1);
    }

    /**
     * @param depth dungeon depth
     * @param rng randomness source
     * @return a random weapon available at that depth, with an unknown upgrade level
     */
    public Weapon randomWeapon(int depth, Rng rng) {
        WeightedTable<Weapon.Kind> table = new WeightedTable<>();
        for (Weapon.Kind k : Weapon.Kind.values()) {
            if (k.minDepth() <= depth) {
                table.add(k, k.spawnWeight());
            }
        }
        return new Weapon(table.pick(rng), rollUpgrade(depth, rng), false);
    }

    /**
     * @param depth dungeon depth
     * @param rng randomness source
     * @return random armor available at that depth, with an unknown upgrade level
     */
    public Armor randomArmor(int depth, Rng rng) {
        WeightedTable<Armor.Kind> table = new WeightedTable<>();
        for (Armor.Kind k : Armor.Kind.values()) {
            if (k.minDepth() <= depth) {
                table.add(k, k.spawnWeight());
            }
        }
        if (table.isEmpty()) {
            table.add(Armor.Kind.LEATHER, 1);
        }
        return new Armor(table.pick(rng), rollUpgrade(depth, rng), false);
    }

    /**
     * @param depth dungeon depth
     * @param rng randomness source
     * @return a stack of 3 to 8 missiles available at that depth
     */
    public ThrownWeapon randomThrown(int depth, Rng rng) {
        WeightedTable<ThrownWeapon.Kind> table = new WeightedTable<>();
        for (ThrownWeapon.Kind k : ThrownWeapon.Kind.values()) {
            if (k.minDepth() <= depth) {
                table.add(k, k.spawnWeight());
            }
        }
        return new ThrownWeapon(table.pick(rng), rng.nextInt(3, 8));
    }

    /**
     * @param rng randomness source
     * @return a random wand with a random number of charges
     */
    public Wand randomWand(Rng rng) {
        WeightedTable<Wand.Kind> table = new WeightedTable<>();
        for (Wand.Kind k : Wand.Kind.values()) {
            table.add(k, k.spawnWeight());
        }
        Wand.Kind kind = table.pick(rng);
        return new Wand(kind, rng.nextInt(kind.minCharges(), kind.maxCharges()));
    }

    /**
     * Rolls an upgrade level for equipment: usually +0, sometimes better, occasionally cursed.
     * Deeper floors shift the positive results upward.
     *
     * @param depth dungeon depth
     * @param rng randomness source
     * @return the upgrade level
     */
    public int rollUpgrade(int depth, Rng rng) {
        int roll = rng.nextInt(100);
        if (roll < 10) {
            return -rng.nextInt(1, 2);
        }
        int bonus = depth / 4;
        if (roll < 70) {
            return bonus;
        }
        if (roll < 90) {
            return bonus + 1;
        }
        if (roll < 98) {
            return bonus + 2;
        }
        return bonus + 3;
    }

    /**
     * @param depth dungeon depth
     * @param rng randomness source
     * @return a gold pile sized for the depth
     */
    public Gold gold(int depth, Rng rng) {
        return new Gold(rng.nextInt(8, 20) + depth * 6);
    }

    /**
     * Decides what (if anything) a defeated monster leaves behind.
     *
     * @param depth dungeon depth
     * @param rng randomness source (the run's loot stream)
     * @return a dropped item or gold, or {@code null}
     */
    public Item monsterDrop(int depth, Rng rng) {
        if (rng.percent(DROP_PERCENT)) {
            return randomItem(depth, rng);
        }
        if (rng.percent(GOLD_DROP_PERCENT)) {
            return new Gold(rng.nextInt(3, 10) + depth * 2);
        }
        return null;
    }

    /**
     * @return the items every adventurer starts with: a dagger, the hoodie, food, and one
     *         potion of healing
     */
    public List<Item> startingKit() {
        return List.of(
                new Weapon(Weapon.Kind.DAGGER, 0, true),
                new Armor(Armor.Kind.TU_HOODIE, 0, true),
                new Food(Food.Kind.TRAIL_RATION, 1),
                new Food(Food.Kind.ENERGY_BAR, 1),
                new Potion(Potion.Kind.HEALING, 1));
    }
}
