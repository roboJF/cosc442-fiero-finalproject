package edu.towson.cosc442.halls7800.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.entity.MonsterType;
import edu.towson.cosc442.halls7800.gen.GameFactory;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Trap;
import org.junit.jupiter.api.Test;

class DescentTest {

    private static final Position HYDRA_TRAPDOOR = new Position(49, 25);

    @Test
    void seedFourGuardianStopsStairsAndTrapdoorUntilDefeated() {
        GameSession game = hydraFloor(ActionRules.CURRENT);
        Monster guardian = game.livingBoss();
        assertEquals(MonsterType.SPAGHETTI_HYDRA, guardian.type());
        game.player().setPosition(game.floor().exit());

        assertFalse(game.submit(StairsCommand.descend()).performed());
        assertEquals(3, game.depth());

        int hp = game.player().hp();
        game.moveCreature(game.player(), HYDRA_TRAPDOOR);

        assertEquals(3, game.depth());
        assertEquals(hp - 3, game.player().hp());
        assertEquals(HYDRA_TRAPDOOR, game.player().position());
        assertTrue(game.messages().lastText().contains("seal blocks the trapdoor"));

        guardian.takeDamage(guardian.hp());
        game.killMonster(guardian, game.player());
        game.moveCreature(game.player(), HYDRA_TRAPDOOR);

        assertEquals(4, game.depth());
        assertEquals(game.floor().entrance(), game.player().position());
    }

    @Test
    void legacyRunRetainsTheSeedFourTrapdoorOutcome() {
        GameSession game = hydraFloor(ActionRules.LEGACY);
        Monster guardian = game.livingBoss();

        game.moveCreature(game.player(), HYDRA_TRAPDOOR);

        assertEquals(4, game.depth());
        assertTrue(guardian.isAlive());
    }

    private static GameSession hydraFloor(ActionRules rules) {
        GameSession game = GameFactory.newGame(4L, "Ada", Difficulty.UNDERGRADUATE, rules);
        game.changeFloor(3);
        assertEquals(Trap.Type.TRAPDOOR, game.contents().trapAt(HYDRA_TRAPDOOR).type());
        return game;
    }
}
