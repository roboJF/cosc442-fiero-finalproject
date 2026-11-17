package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.combat.AttackProfile;
import edu.towson.cosc442.halls7800.combat.Combat;
import edu.towson.cosc442.halls7800.combat.DamageType;
import edu.towson.cosc442.halls7800.effect.EffectManager;
import edu.towson.cosc442.halls7800.effect.EffectType;
import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.entity.Player;
import edu.towson.cosc442.halls7800.item.Wand;
import edu.towson.cosc442.halls7800.world.LineOfSight;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Terrain;
import java.util.List;

/**
 * Fires wand bolts. A bolt flies from the player toward the nearest visible monster within
 * {@link Wand#RANGE} and strikes the first monster on its path.
 */
public final class WandZapper {

    /** Accuracy bonus of wand bolts over the player's own accuracy. */
    public static final int BOLT_ACCURACY_BONUS = 4;

    private WandZapper() {
    }

    /**
     * Zaps a wand from the player's pack.
     *
     * @param session the game
     * @param wand the wand
     * @return the command result
     */
    public static CommandResult zap(GameSession session, Wand wand) {
        Player player = session.player();
        Monster aim = nearestTarget(session);
        if (aim == null) {
            return CommandResult.rejected("There is nothing in view to zap.");
        }
        if (!wand.useCharge()) {
            return CommandResult.acted(player.actionCost(), "The wand sputters. It has no charges left.");
        }
        session.signal(GameEvent.Type.WAND_ZAPPED, aim.position());
        List<Position> path = LineOfSight.trajectory(session.floor(), player.position(), aim.position(), Wand.RANGE);
        Monster struck = null;
        for (Position p : path) {
            if (wand.kind() == Wand.Kind.FIREBOLT && session.floor().terrainAt(p) == Terrain.TALL_GRASS) {
                Hazards.ignite(session, p);
            }
            Monster m = session.monsterAt(p);
            if (m != null) {
                struck = m;
                break;
            }
        }
        if (struck == null) {
            return CommandResult.acted(player.actionCost(), "The bolt flies wide and fizzles out.");
        }
        switch (wand.kind()) {
            case FIREBOLT -> Combat.attackWith(session, player, struck,
                    new AttackProfile(player.accuracy() + BOLT_ACCURACY_BONUS, 4, 9, DamageType.FIRE, 5,
                            EffectType.BURNING, 40, 3, 2, "blast"), true);
            case THROTTLING -> applyBolt(session, struck, EffectType.SLOWED, 8, "throttled");
            case INTERRUPTS -> {
                Combat.attackWith(session, player, struck,
                        new AttackProfile(player.accuracy() + BOLT_ACCURACY_BONUS, 1, 3, DamageType.ARCANE, 0,
                                EffectType.STUNNED, 100, 2, 0, "jolt"), true);
            }
            default -> throw new IllegalStateException("Unknown wand " + wand.kind());
        }
        return CommandResult.acted(player.actionCost());
    }

    private static void applyBolt(GameSession session, Monster target, EffectType effect, int turns, String word) {
        EffectManager.Outcome outcome = target.applyEffect(effect, turns, 0);
        target.alert();
        target.setLastKnownPlayerPosition(session.player().position());
        session.message(outcome == EffectManager.Outcome.RESISTED || outcome == EffectManager.Outcome.CANCELED
                ? target.subject() + " shrugs off the bolt."
                : target.subject() + " " + target.verb("are") + " " + word + "!");
    }

    private static Monster nearestTarget(GameSession session) {
        Position from = session.player().position();
        Monster best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Monster m : session.monsters()) {
            int d = from.distanceTo(m.position());
            if (d <= Wand.RANGE && d < bestDistance && session.currentFloor().isVisible(m.position())
                    && LineOfSight.isClear(session.floor(), from, m.position())) {
                best = m;
                bestDistance = d;
            }
        }
        return best;
    }
}
