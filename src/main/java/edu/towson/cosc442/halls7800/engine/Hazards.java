package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.effect.EffectType;
import edu.towson.cosc442.halls7800.entity.Creature;
import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.util.RandomStream;
import edu.towson.cosc442.halls7800.util.Rng;
import edu.towson.cosc442.halls7800.world.Direction;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Terrain;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spreading fire. Tall grass can catch fire (from a firebolt, a flame vent, or a burning creature
 * standing in it). Each turn a fire:
 * <ol>
 *   <li>sets alight any creature standing in it,</li>
 *   <li>may spread to orthogonally adjacent tall grass ({@value #SPREAD_PERCENT}% each),</li>
 *   <li>burns down by one turn, leaving bare floor when it goes out.</li>
 * </ol>
 * Water never burns, and wet creatures do not catch fire.
 */
public final class Hazards {

    /** Turns a patch of grass burns before going out. */
    public static final int FIRE_DURATION = 4;
    /** Percent chance per turn that fire spreads to each adjacent patch of grass. */
    public static final int SPREAD_PERCENT = 45;

    private Hazards() {
    }

    /**
     * Sets a tile on fire if it can burn.
     *
     * @param session the game
     * @param p the tile
     * @return {@code true} if a new fire started
     */
    public static boolean ignite(GameSession session, Position p) {
        FloorContents contents = session.contents();
        if (session.floor().terrainAt(p) != Terrain.TALL_GRASS || contents.fireAt(p) > 0) {
            return false;
        }
        contents.addFire(p, FIRE_DURATION);
        session.messageIfVisible(p, "The grass catches fire!");
        return true;
    }

    /**
     * Advances every fire on the current floor by one turn. Called once per player turn.
     *
     * @param session the game
     */
    public static void tick(GameSession session) {
        FloorContents contents = session.contents();
        Floor floor = session.floor();
        Rng rng = session.rng(RandomStream.EFFECTS);
        igniteUnderBurningCreatures(session);
        if (contents.fires().isEmpty()) {
            return;
        }
        List<Position> spreadTo = new ArrayList<>();
        for (Map.Entry<Position, Integer> fire : List.copyOf(contents.fires().entrySet())) {
            Position p = fire.getKey();
            Creature victim = creatureAt(session, p);
            if (victim != null) {
                victim.applyEffect(EffectType.BURNING, 3, 2);
            }
            for (Direction d : Direction.CARDINALS) {
                Position next = p.step(d);
                if (floor.terrainAt(next) == Terrain.TALL_GRASS && contents.fireAt(next) == 0
                        && !spreadTo.contains(next) && rng.percent(SPREAD_PERCENT)) {
                    spreadTo.add(next);
                }
            }
            int left = fire.getValue() - 1;
            if (left <= 0) {
                contents.removeFire(p);
                floor.setTerrain(p, Terrain.FLOOR);
            } else {
                contents.addFire(p, left);
            }
        }
        for (Position p : spreadTo) {
            contents.addFire(p, FIRE_DURATION);
        }
    }

    /**
     * Applies fire to a creature that steps into a burning tile.
     *
     * @param session the game
     * @param creature the creature
     * @param p where it stepped
     */
    public static void enter(GameSession session, Creature creature, Position p) {
        if (session.contents().fireAt(p) > 0) {
            creature.applyEffect(EffectType.BURNING, 3, 2);
            if (creature.isPlayer()) {
                session.message("You step into the flames!");
            }
        }
    }

    private static void igniteUnderBurningCreatures(GameSession session) {
        List<Creature> creatures = new ArrayList<>(session.monsters());
        creatures.add(session.player());
        if (session.companion() != null) {
            creatures.add(session.companion());
        }
        for (Creature c : creatures) {
            if (c.effects().has(EffectType.BURNING)) {
                ignite(session, c.position());
            }
        }
    }

    private static Creature creatureAt(GameSession session, Position p) {
        if (session.player().position().equals(p)) {
            return session.player();
        }
        if (session.companion() != null && session.companion().position().equals(p)) {
            return session.companion();
        }
        Monster m = session.monsterAt(p);
        return m;
    }
}
