package edu.towson.cosc442.halls7800.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.towson.cosc442.halls7800.gen.FixedFloorSource;
import edu.towson.cosc442.halls7800.item.Potion;
import edu.towson.cosc442.halls7800.item.QuestItem;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UseItemCommandTest {

    private GameSession game;

    @BeforeEach
    void setUp() {
        game = new GameSession(7L, new FixedFloorSource()
                .add("Atrium", "#####", "#@..#", "#####")
                .add("Room", "#####", "#@..#", "#####"), "Tester");
    }

    @Test
    void drinkingAnUnknownPotionIdentifiesIt() {
        Potion potion = new Potion(Potion.Kind.HASTE, 1);
        game.player().inventory().add(potion);
        int slot = game.player().inventory().indexOf(potion);
        assertFalse(game.identification().isKnown(Potion.Kind.HASTE));

        CommandResult result = game.submit(new UseItemCommand(slot));

        assertTrue(result.performed());
        assertTrue(game.identification().isKnown(Potion.Kind.HASTE));
    }

    @Test
    void diplomaSignalsItemUseOnlyWhenItReturnsThePlayerToTheAtrium() {
        var inventory = game.player().inventory();
        var diploma = new QuestItem(QuestItem.Kind.DIPLOMA, 1);
        inventory.add(diploma);
        var use = new UseItemCommand(inventory.indexOf(diploma));
        var itemUses = new ArrayList<GameEvent>();
        game.events().subscribe(event -> {
            if (event.type() == GameEvent.Type.ITEM_USED) {
                itemUses.add(event);
            }
        });

        assertFalse(game.submit(use).performed());
        assertTrue(itemUses.isEmpty());

        game.changeFloor(1);
        assertTrue(game.submit(use).performed());
        assertEquals(List.of(new GameEvent(GameEvent.Type.ITEM_USED, null, game.player().position(),
                GameRules.HUB_DEPTH)), itemUses);
    }
}
