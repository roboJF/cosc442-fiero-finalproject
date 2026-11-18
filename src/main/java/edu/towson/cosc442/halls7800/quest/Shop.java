package edu.towson.cosc442.halls7800.quest;

import edu.towson.cosc442.halls7800.engine.ActionRules;
import edu.towson.cosc442.halls7800.entity.Player;
import edu.towson.cosc442.halls7800.item.EquippableItem;
import edu.towson.cosc442.halls7800.item.Food;
import edu.towson.cosc442.halls7800.item.Identification;
import edu.towson.cosc442.halls7800.item.Item;
import edu.towson.cosc442.halls7800.item.ItemGenerator;
import edu.towson.cosc442.halls7800.item.Potion;
import edu.towson.cosc442.halls7800.item.QuestItem;
import edu.towson.cosc442.halls7800.item.Scroll;
import edu.towson.cosc442.halls7800.item.ThrownWeapon;
import edu.towson.cosc442.halls7800.item.Wand;
import edu.towson.cosc442.halls7800.util.Rng;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Conover's Curio &amp; Circuit, the Atrium shop. Items are sold at
 * {@value #BUY_PERCENT}% of their value and bought back at {@value #SELL_PERCENT}%. The
 * Tinkerer knows exactly what everything is, so wares are listed by their true names and buying
 * one identifies its kind.
 */
public final class Shop {

    /** Price paid by the player, as a percentage of item value. */
    public static final int BUY_PERCENT = 150;
    /** Price paid to the player, as a percentage of item value. */
    public static final int SELL_PERCENT = 50;

    private static final String PACK_FULL = "\"Your pack's full. Even I can't fit more in there.\"";

    private final List<Item> stock = new ArrayList<>();
    private final ActionRules actionRules;

    /**
     * Stocks the shop for a new run.
     *
     * @param generator item generator
     * @param rng randomness source (the run's shop stream)
     */
    public Shop(ItemGenerator generator, Rng rng) {
        this(generator, rng, ActionRules.CURRENT);
    }

    /**
     * Stocks the shop using the purchase rules associated with the run.
     *
     * @param generator item generator
     * @param rng randomness source (the run's shop stream)
     * @param actionRules purchase rules for this run
     */
    public Shop(ItemGenerator generator, Rng rng, ActionRules actionRules) {
        this.actionRules = Objects.requireNonNull(actionRules, "actionRules");
        stock.add(new Potion(Potion.Kind.HEALING, 1));
        stock.add(new Potion(Potion.Kind.HEALING, 1));
        stock.add(new Potion(Potion.Kind.SHIELDING, 1));
        stock.add(new Scroll(Scroll.Kind.INTROSPECTION, 1));
        stock.add(new Scroll(Scroll.Kind.REFACTORING, 1));
        stock.add(new Food(Food.Kind.TRAIL_RATION, 1));
        stock.add(new Food(Food.Kind.TRAIL_RATION, 1));
        stock.add(new ThrownWeapon(ThrownWeapon.Kind.DART, 10));
        stock.add(new Wand(Wand.Kind.THROTTLING, 4));
        EquippableItem weapon = generator.randomWeapon(3, rng);
        weapon.removeCurse();
        weapon.reveal();
        stock.add(weapon);
        EquippableItem armor = generator.randomArmor(3, rng);
        armor.removeCurse();
        armor.reveal();
        stock.add(armor);
        stock.add(generator.randomItem(2, rng));
    }

    /** @return the items for sale, in display order */
    public List<Item> stock() {
        return Collections.unmodifiableList(stock);
    }

    /**
     * @param item an item for sale
     * @return its price in gold
     */
    public static int buyPrice(Item item) {
        return Math.max(1, item.value() * item.quantity() * BUY_PERCENT / 100);
    }

    /**
     * @param item an item the player might sell
     * @return what the shop pays for the whole stack (0 for unsellable items)
     */
    public static int sellPrice(Item item) {
        if (item instanceof QuestItem) {
            return 0;
        }
        return item.value() * item.quantity() * SELL_PERCENT / 100;
    }

    /**
     * Sells a stocked item to the player.
     *
     * @param index position in {@link #stock()}
     * @param player the buyer
     * @param id identification state (the bought item's kind becomes known)
     * @return a message describing what happened
     */
    public String buy(int index, Player player, Identification id) {
        if (index < 0 || index >= stock.size()) {
            return "Conover shrugs. \"Don't have one of those.\"";
        }
        Item item = stock.get(index);
        int price = buyPrice(item);
        if (player.gold() < price) {
            return "\"That's " + price + " gold. Come back when the purse is heavier.\"";
        }
        boolean addBeforeCharging = actionRules.usesProgressFixes();
        if (addBeforeCharging && !player.inventory().add(item)) {
            return PACK_FULL;
        }
        player.spendGold(price);
        stock.remove(index);
        id.identify(item);
        if (!addBeforeCharging && !player.inventory().add(item)) {
            return PACK_FULL;
        }
        return "You buy " + item.describe(id) + " for " + price + " gold.";
    }

    /**
     * Buys a whole stack from the player.
     *
     * @param item a stack in the player's pack
     * @param player the seller
     * @param id identification state
     * @return a message describing what happened
     */
    public String sell(Item item, Player player, Identification id) {
        if (!player.inventory().contains(item)) {
            return "\"You don't seem to have that.\"";
        }
        if (player.isEquipped(item)) {
            return "\"Take it off first, friend.\"";
        }
        int price = sellPrice(item);
        if (price <= 0) {
            return "\"I couldn't put a price on that.\"";
        }
        String description = item.describe(id);
        player.inventory().remove(item, item.quantity());
        player.addGold(price);
        stock.add(item);
        return "You sell " + description + " for " + price + " gold.";
    }

    /**
     * Describes a stocked item by its true name.
     *
     * @param item an item for sale
     * @return e.g. "a potion of healing"
     */
    public static String label(Item item) {
        if (item instanceof Potion p) {
            return "potion of " + p.kind().displayName();
        }
        if (item instanceof Scroll s) {
            return "scroll of " + s.kind().displayName();
        }
        if (item instanceof EquippableItem equipment && !equipment.isKnown()) {
            EquippableItem revealed = (EquippableItem) equipment.copy();
            revealed.reveal();
            return revealed.describe(null);
        }
        return item.describe(null);
    }
}
