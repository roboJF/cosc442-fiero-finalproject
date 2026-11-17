package edu.towson.cosc442.halls7800.engine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.towson.cosc442.halls7800.gen.FixedFloorSource;
import edu.towson.cosc442.halls7800.item.Potion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UseItemCommandTest {

    private GameSession game;

    @BeforeEach
    void setUp() {
        game = new GameSession(7L, new FixedFloorSource().add("Room", "#####", "#@..#", "#####"), "Tester");
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
}
