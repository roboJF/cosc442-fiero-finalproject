package edu.towson.cosc442.halls7800.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.towson.cosc442.halls7800.effect.EffectType;
import edu.towson.cosc442.halls7800.engine.ActionRules;
import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.entity.MonsterType;
import edu.towson.cosc442.halls7800.entity.Player;
import edu.towson.cosc442.halls7800.util.ScriptedRng;
import edu.towson.cosc442.halls7800.world.Position;
import org.junit.jupiter.api.Test;

class CombatResolverTest {

    private final Player player = new Player("Tester", new Position(1, 1));
    private final Monster goblin = new Monster(MonsterType.OFF_BY_ONE_GOBLIN, new Position(2, 1), false);

    @Test
    void hitDealsRolledDamageMinusArmor() {
        // hit roll 10, no crit (50), damage roll index 3 -> 4, armor roll 1
        ScriptedRng rng = new ScriptedRng(10, 50, 3, 1);

        AttackResult result = CombatResolver.resolve(player, goblin, player.meleeAttack(), rng);

        assertTrue(result.hit());
        assertEquals(3, result.damage());
        assertEquals(MonsterType.OFF_BY_ONE_GOBLIN.maxHp() - 3, goblin.hp());
    }

    @Test
    void rollAtTheHitChanceMissesWithoutChangingHealth() {
        AttackProfile attack = AttackProfile.basic(goblin.evasion(), 4, 4, 0, "hit");

        AttackResult result = CombatResolver.resolve(player, goblin, attack, new ScriptedRng(70));

        assertFalse(result.hit());
        assertEquals(0, result.damage());
        assertEquals(goblin.maxHp(), goblin.hp());
    }

    @Test
    void sleepingDefenderTakesCriticalDamageWithoutAnArmorRoll() {
        goblin.setState(Monster.State.SLEEPING);
        AttackProfile attack = AttackProfile.basic(0, 3, 4, 0, "hit");

        AttackResult result = CombatResolver.resolve(player, goblin, attack, new ScriptedRng(0));

        assertTrue(result.hit());
        assertTrue(result.critical());
        assertEquals(5, result.damage());
        assertEquals(goblin.maxHp() - 5, goblin.hp());
    }

    @Test
    void fullShieldAbsorptionSpendsTheShieldInsteadOfHealth() {
        goblin.applyEffect(EffectType.SHIELDED, 3, 4);
        AttackProfile attack = AttackProfile.basic(0, 4, 4, 0, "hit");

        AttackResult result = CombatResolver.resolve(player, goblin, attack, new ScriptedRng(0, 99, 0, 0));

        assertTrue(result.hit());
        assertEquals(4, result.absorbed());
        assertEquals(0, result.damage());
        assertEquals(goblin.maxHp(), goblin.hp());
        assertFalse(goblin.effects().has(EffectType.SHIELDED));
    }

    @Test
    void fireImmunityPreventsDamageAndBurning() {
        Monster wyrm = new Monster(MonsterType.ZERO_DAY_WYRM, new Position(2, 1), false);
        AttackProfile fire = new AttackProfile(0, 4, 4, DamageType.FIRE, 0, EffectType.BURNING, 100, 3, 1, "burn");

        AttackResult result = CombatResolver.resolve(player, wyrm, fire, new ScriptedRng(0, 99, 0));

        assertTrue(result.hit());
        assertTrue(result.immune());
        assertEquals(0, result.damage());
        assertNull(result.inflicted());
        assertEquals(wyrm.maxHp(), wyrm.hp());
        assertFalse(wyrm.effects().has(EffectType.BURNING));
    }

    @Test
    void fireThatGetsThroughAShieldCanInflictBurning() {
        goblin.applyEffect(EffectType.SHIELDED, 3, 2);
        AttackProfile fire = new AttackProfile(0, 4, 4, DamageType.FIRE, 0, EffectType.BURNING, 100, 3, 1, "burn");

        AttackResult result = CombatResolver.resolve(player, goblin, fire, new ScriptedRng(0, 99, 0, 99));

        assertEquals(2, result.absorbed());
        assertEquals(2, result.damage());
        assertEquals(goblin.maxHp() - 2, goblin.hp());
        assertEquals(EffectType.BURNING, result.inflicted());
        assertTrue(goblin.effects().has(EffectType.BURNING));
    }

    @Test
    void fullyAbsorbedBiteDoesNotPoisonThePlayer() {
        player.applyEffect(EffectType.SHIELDED, 3, 4);
        Monster centipede = new Monster(MonsterType.LINKED_LIST_CENTIPEDE, new Position(2, 1), false);
        ScriptedRng rng = new ScriptedRng(0, 99, 2, 0);

        AttackResult result = CombatResolver.resolve(centipede, player, centipede.meleeAttack(), rng);

        assertTrue(result.hit());
        assertEquals(4, result.absorbed());
        assertEquals(0, result.damage());
        assertEquals(player.maxHp(), player.hp());
        assertNull(result.inflicted());
        assertFalse(player.effects().has(EffectType.POISONED));
        assertEquals(1, rng.remaining());
    }

    @Test
    void partiallyAbsorbedBiteCanStillPoisonThePlayer() {
        player.applyEffect(EffectType.SHIELDED, 3, 3);
        Monster centipede = new Monster(MonsterType.LINKED_LIST_CENTIPEDE, new Position(2, 1), false);

        AttackResult result = CombatResolver.resolve(centipede, player, centipede.meleeAttack(),
                new ScriptedRng(0, 99, 2, 0));

        assertEquals(3, result.absorbed());
        assertEquals(1, result.damage());
        assertEquals(player.maxHp() - 1, player.hp());
        assertEquals(EffectType.POISONED, result.inflicted());
        assertTrue(player.effects().has(EffectType.POISONED));
    }

    @Test
    void versionTwoPreservesPoisonAndTheCombatRollOnFullyAbsorbedBites() {
        player.applyEffect(EffectType.SHIELDED, 3, 4);
        Monster centipede = new Monster(MonsterType.LINKED_LIST_CENTIPEDE, new Position(2, 1), false);
        ScriptedRng rng = new ScriptedRng(0, 99, 2, 0);

        AttackResult result = CombatResolver.resolve(centipede, player, centipede.meleeAttack(), rng,
                ActionRules.VERSION_2);

        assertEquals(0, result.damage());
        assertEquals(EffectType.POISONED, result.inflicted());
        assertTrue(player.effects().has(EffectType.POISONED));
        assertEquals(0, rng.remaining());
    }
}
