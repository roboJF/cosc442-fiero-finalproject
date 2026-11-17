package edu.towson.cosc442.halls7800.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.towson.cosc442.halls7800.effect.EffectType;
import edu.towson.cosc442.halls7800.entity.CompanionAi;
import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.entity.MonsterAi;
import edu.towson.cosc442.halls7800.entity.MonsterType;
import edu.towson.cosc442.halls7800.gen.FixedFloorSource;
import edu.towson.cosc442.halls7800.world.Direction;
import edu.towson.cosc442.halls7800.world.Position;
import org.junit.jupiter.api.Test;

class ActionRulesTest {

    @Test
    void playerLosesTheTurnOnWhichTheStunExpires() {
        GameSession game = corridor(ActionRules.CURRENT);
        game.player().applyEffect(EffectType.STUNNED, 1, 0);
        MoveCommand move = new MoveCommand(Direction.EAST);

        assertTrue(game.submit(move).performed());
        assertEquals(1, game.turn());
        assertEquals(new Position(1, 1), game.player().position());
        assertFalse(game.player().effects().has(EffectType.STUNNED));

        assertTrue(game.submit(move).performed());
        assertEquals(new Position(2, 1), game.player().position());
    }

    @Test
    void monsterTicksEffectsBeforeSkippingItsStunnedAction() {
        GameSession game = corridor(ActionRules.CURRENT);
        Monster monster = huntingMonster(game);
        monster.applyEffect(EffectType.POISONED, 1, 1);
        int hp = monster.hp();

        assertEquals(monster.actionCost(), MonsterAi.takeTurn(monster, game));
        assertEquals(hp - 1, monster.hp());
        assertEquals(new Position(5, 1), monster.position());
        assertFalse(monster.effects().has(EffectType.STUNNED));

        MonsterAi.takeTurn(monster, game);
        assertEquals(new Position(4, 1), monster.position());
    }

    @Test
    void companionLosesTheTurnOnWhichTheStunExpires() {
        GameSession game = corridor(ActionRules.CURRENT);
        assertTrue(game.recruitCompanion());
        var companion = game.companion();
        companion.setPosition(new Position(5, 1));
        companion.applyEffect(EffectType.STUNNED, 1, 0);

        assertEquals(companion.actionCost(), CompanionAi.takeTurn(companion, game));
        assertEquals(new Position(5, 1), companion.position());
        assertFalse(companion.effects().has(EffectType.STUNNED));

        CompanionAi.takeTurn(companion, game);
        assertEquals(new Position(4, 1), companion.position());
    }

    @Test
    void legacyMonsterStillActsWhenTheStunExpires() {
        GameSession game = corridor(ActionRules.LEGACY);
        Monster monster = huntingMonster(game);

        MonsterAi.takeTurn(monster, game);

        assertEquals(new Position(4, 1), monster.position());
    }

    @Test
    void stunnedGuardianWaitsBeforeEnteringItsNextPhase() {
        GameSession game = corridor(ActionRules.CURRENT);
        Monster guardian = new Monster(MonsterType.SPAGHETTI_HYDRA, new Position(5, 1), false);
        guardian.takeDamage(guardian.maxHp() / 2);
        guardian.applyEffect(EffectType.STUNNED, 1, 0);
        game.addMonster(guardian);

        MonsterAi.takeTurn(guardian, game);
        assertEquals(0, guardian.bossPhase());
        assertEquals(1, game.monsters().size());

        MonsterAi.takeTurn(guardian, game);
        assertEquals(1, guardian.bossPhase());
        assertEquals(3, game.monsters().size());
    }

    private static GameSession corridor(ActionRules rules) {
        FixedFloorSource floors = new FixedFloorSource().add("Corridor",
                "########", "#@.....#", "########");
        return new GameSession(1L, floors, "Ada", Difficulty.UNDERGRADUATE, rules);
    }

    private static Monster huntingMonster(GameSession game) {
        Monster monster = new Monster(MonsterType.OFF_BY_ONE_GOBLIN, new Position(5, 1), false);
        monster.alert();
        monster.applyEffect(EffectType.STUNNED, 1, 0);
        game.addMonster(monster);
        return monster;
    }
}
