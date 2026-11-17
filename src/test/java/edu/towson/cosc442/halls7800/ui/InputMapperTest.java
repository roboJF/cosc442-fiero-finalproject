package edu.towson.cosc442.halls7800.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.gen.GameFactory;
import edu.towson.cosc442.halls7800.progression.Experience;
import edu.towson.cosc442.halls7800.util.RandomStream;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

class InputMapperTest {

    @Test
    void typedStairShortcutsUseTheSameKeysAsPageUpAndPageDown() {
        assertEquals(KeyCode.PAGE_UP, InputMapper.keyForTypedShortcut("<"));
        assertEquals(KeyCode.PAGE_DOWN, InputMapper.keyForTypedShortcut(">"));
        assertEquals("S:U", InputMapper.fromKey(InputMapper.keyForTypedShortcut("<")).encode());
        assertEquals("S:D", InputMapper.fromKey(InputMapper.keyForTypedShortcut(">")).encode());
    }

    @Test
    void examineAndPerkSelectionTakePriorityOverNormalInputOnStairs() {
        GameSession game = GameFactory.newGame(4L);
        game.player().setPosition(game.floor().exit());
        assertEquals(InputMapper.Mode.PLAY, InputMapper.mode(game, false, false));
        assertEquals(InputMapper.Mode.EXAMINE, InputMapper.mode(game, true, false));

        game.player().gainExperience(Experience.thresholdFor(3), game.rng(RandomStream.EFFECTS));

        assertFalse(game.player().perkChoices().isEmpty());
        assertEquals(InputMapper.Mode.PERK, InputMapper.mode(game, false, false));
        assertEquals(InputMapper.Mode.PERK, InputMapper.mode(game, true, false));
    }
}
