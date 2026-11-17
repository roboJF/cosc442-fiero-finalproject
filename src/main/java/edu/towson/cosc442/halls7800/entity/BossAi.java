package edu.towson.cosc442.halls7800.entity;

import edu.towson.cosc442.halls7800.effect.EffectType;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.util.RandomStream;
import edu.towson.cosc442.halls7800.util.Rng;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Phase changes for bosses. Each boss has HP thresholds; when its HP first drops to or below the
 * next threshold, the boss enters a new phase and does something dramatic once.
 *
 * <ul>
 *   <li>Spaghetti Hydra (66%, 33%): two null-pointer imps wriggle free.</li>
 *   <li>Deadlock Colossus (50%): breaks its own lock and becomes hasted.</li>
 *   <li>Zero-Day Wyrm (50%): hardens its scales into a shield.</li>
 *   <li>Hollow Registrar (75%, 50%, 25%): summons registration holds, then teleports and
 *       summons more, then becomes hasted.</li>
 * </ul>
 */
public final class BossAi {

    /** Duration of effects bosses give themselves: as long as any effect can last. */
    private static final int LONG_EFFECT = EffectType.MAX_DURATION;

    private BossAi() {
    }

    /**
     * @param type a boss type
     * @return the HP percentages at which it changes phase, highest first (empty for non-bosses)
     */
    public static int[] thresholds(MonsterType type) {
        return switch (type) {
            case SPAGHETTI_HYDRA -> new int[] {66, 33};
            case DEADLOCK_COLOSSUS, ZERO_DAY_WYRM -> new int[] {50};
            case HOLLOW_REGISTRAR -> new int[] {75, 50, 25};
            default -> new int[0];
        };
    }

    /**
     * Enters any phases whose thresholds the boss's HP has crossed. Called at the start of each
     * of the boss's turns.
     *
     * @param boss the boss
     * @param session the game
     */
    public static void checkPhases(Monster boss, GameSession session) {
        int[] limits = thresholds(boss.type());
        int hpPercent = boss.hp() * 100 / boss.maxHp();
        while (boss.bossPhase() < limits.length && hpPercent <= limits[boss.bossPhase()]) {
            boss.advanceBossPhase();
            enterPhase(boss, boss.bossPhase(), session);
        }
    }

    private static void enterPhase(Monster boss, int phase, GameSession session) {
        switch (boss.type()) {
            case SPAGHETTI_HYDRA -> {
                int made = summon(boss, MonsterType.NULL_POINTER_IMP, 2, session);
                session.messageIfVisible(boss.position(), made > 0
                        ? "Severed strands of the Spaghetti Hydra wriggle free as null-pointer imps!"
                        : "The Spaghetti Hydra writhes, but its strands find no room.");
            }
            case DEADLOCK_COLOSSUS -> {
                boss.applyEffect(EffectType.HASTED, LONG_EFFECT, 0);
                session.messageIfVisible(boss.position(),
                        "The Deadlock Colossus releases its own lock and lurches forward with terrible speed!");
            }
            case ZERO_DAY_WYRM -> {
                boss.applyEffect(EffectType.SHIELDED, LONG_EFFECT, 25);
                session.messageIfVisible(boss.position(), "The Zero-Day Wyrm's scales harden into an unpatched shell!");
            }
            case HOLLOW_REGISTRAR -> registrarPhase(boss, phase, session);
            default -> {
                // not a boss with phases
            }
        }
    }

    private static void registrarPhase(Monster boss, int phase, GameSession session) {
        switch (phase) {
            case 1 -> {
                summon(boss, MonsterType.REGISTRATION_HOLD, 2, session);
                session.message("The Hollow Registrar raises a stamp: \"Your file is incomplete.\" Registration holds appear!");
            }
            case 2 -> {
                teleportAway(boss, session);
                summon(boss, MonsterType.REGISTRATION_HOLD, 2, session);
                session.message("The Hollow Registrar vanishes into the stacks of paperwork and reappears elsewhere!");
            }
            default -> {
                boss.applyEffect(EffectType.HASTED, LONG_EFFECT, 0);
                session.message("The Hollow Registrar shrieks: \"The office closes in five minutes!\" It speeds up!");
            }
        }
    }

    /**
     * Places up to {@code count} awake monsters on free tiles next to the boss.
     *
     * @return how many were placed
     */
    static int summon(Monster boss, MonsterType type, int count, GameSession session) {
        Floor floor = session.floor();
        int placed = 0;
        for (Position p : boss.position().neighbors()) {
            if (placed >= count) {
                break;
            }
            if (floor.isPassable(p) && !session.isOccupied(p)) {
                Monster minion = new Monster(type, p, false);
                minion.alert();
                minion.setLastKnownPlayerPosition(session.player().position());
                session.addMonster(minion);
                placed++;
            }
        }
        return placed;
    }

    private static void teleportAway(Monster boss, GameSession session) {
        Floor floor = session.floor();
        Rng rng = session.rng(RandomStream.AI);
        List<Position> options = new ArrayList<>();
        for (Position p : floor.passablePositions()) {
            if (!session.isOccupied(p) && p.distanceTo(session.player().position()) >= 6) {
                options.add(p);
            }
        }
        if (!options.isEmpty()) {
            session.moveCreature(boss, rng.pick(options));
        }
    }
}
