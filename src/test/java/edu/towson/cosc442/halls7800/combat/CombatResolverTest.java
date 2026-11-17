package edu.towson.cosc442.halls7800.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
