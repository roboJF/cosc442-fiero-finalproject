package edu.towson.cosc442.halls7800.quest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.towson.cosc442.halls7800.engine.ActionRules;
import edu.towson.cosc442.halls7800.engine.Difficulty;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.gen.GameFactory;
import edu.towson.cosc442.halls7800.item.Armor;
import edu.towson.cosc442.halls7800.item.Inventory;
import edu.towson.cosc442.halls7800.item.Item;
import edu.towson.cosc442.halls7800.item.Potion;
import edu.towson.cosc442.halls7800.item.Wand;
import edu.towson.cosc442.halls7800.item.Weapon;
import java.util.List;
import org.junit.jupiter.api.Test;

class ShopTest {

    private GameSession game;
    private Inventory pack;
    private List<Item> stock;

    private void openShopWithFullPack(ActionRules rules) {
        game = GameFactory.newGame(62L, "Ada", Difficulty.UNDERGRADUATE, rules);
        game.openShop();
        game.player().addGold(1_000);
        pack = game.player().inventory();
        while (!pack.isFull()) {
            assertTrue(pack.add(new Wand(Wand.Kind.THROTTLING, 1)));
        }
        stock = game.shop().stock();
    }

    @Test
    void rejectedArmorPurchaseKeepsGoldAndStock() {
        openShopWithFullPack(ActionRules.CURRENT);
        Item armor = stockedArmor();
        List<Item> before = List.copyOf(stock);
        int gold = game.player().gold();

        assertTrue(game.buy(stock.indexOf(armor)).contains("pack's full"));

        assertEquals(gold, game.player().gold());
        assertEquals(before, stock);
        assertFalse(pack.contains(armor));
    }

    @Test
    void fullPackAcceptsAPotionThatCanMerge() {
        openShopWithFullPack(ActionRules.CURRENT);
        Potion healing = pack.ofType(Potion.class).stream()
                .filter(potion -> potion.kind() == Potion.Kind.HEALING).findFirst().orElseThrow();
        Item offered = stock.get(0);
        int gold = game.player().gold();
        int quantity = healing.quantity();
        int stockCount = stock.size();

        assertTrue(game.buy(0).startsWith("You buy"));

        assertEquals(gold - Shop.buyPrice(offered), game.player().gold());
        assertEquals(stockCount - 1, stock.size());
        assertFalse(stock.contains(offered));
        assertEquals(pack.capacity(), pack.size());
        assertEquals(quantity + offered.quantity(), healing.quantity());
    }

    @Test
    void rejectedUnfamiliarPotionDoesNotRevealItsKind() {
        openShopWithFullPack(ActionRules.CURRENT);
        Item shielding = stock.stream()
                .filter(item -> item instanceof Potion potion && potion.kind() == Potion.Kind.SHIELDING)
                .findFirst().orElseThrow();
        assertFalse(game.identification().isKnown(Potion.Kind.SHIELDING));

        assertTrue(game.buy(stock.indexOf(shielding)).contains("pack's full"));

        assertFalse(game.identification().isKnown(Potion.Kind.SHIELDING));
    }

    @Test
    void versionTwoReplaysThePreviousFullPackPurchaseOutcome() {
        openShopWithFullPack(ActionRules.VERSION_2);
        Item armor = stockedArmor();
        int gold = game.player().gold();

        assertTrue(game.buy(stock.indexOf(armor)).contains("pack's full"));

        assertEquals(gold - Shop.buyPrice(armor), game.player().gold());
        assertFalse(stock.contains(armor));
        assertFalse(pack.contains(armor));
    }

    private Item stockedArmor() {
        return stock.stream().filter(Armor.class::isInstance).findFirst().orElseThrow();
    }

    @Test
    void shopLabelShowsTheUpgradeWithoutRevealingTheActualItem() {
        Weapon dagger = new Weapon(Weapon.Kind.DAGGER, 2, false);

        assertEquals("a +2 dagger", Shop.label(dagger));
        assertFalse(dagger.isKnown());
    }
}
