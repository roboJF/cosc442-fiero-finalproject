package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.effect.EffectType;
import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.entity.Player;
import edu.towson.cosc442.halls7800.util.RandomStream;
import edu.towson.cosc442.halls7800.util.Rng;
import edu.towson.cosc442.halls7800.world.Direction;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Terrain;
import edu.towson.cosc442.halls7800.world.Trap;
import java.util.ArrayList;
import java.util.List;

/**
 * Trap effects and searching for hidden things.
 */
public final class Traps {

    /** Percent chance per search that an adjacent secret door is found. */
    public static final int SEARCH_DOOR_PERCENT = 35;
    /** Percent chance per search that an adjacent hidden trap is found. */
    public static final int SEARCH_TRAP_PERCENT = 50;
    /** Percent chance per step to notice an adjacent hidden trap without searching. */
    public static final int PASSIVE_TRAP_PERCENT = 10;

    private Traps() {
    }

    /**
     * Sets off a trap under the player.
     *
     * @param session the game
     * @param trap the trap
     */
    public static void trigger(GameSession session, Trap trap) {
        trap.reveal();
        session.stats().recordTrap();
        session.signal(GameEvent.Type.TRAP_TRIGGERED, session.player().position());
        Player player = session.player();
        int depth = session.depth();
        Rng rng = session.rng(RandomStream.EFFECTS);
        switch (trap.type()) {
            case SPIKE -> {
                int damage = player.takeDamage(rng.nextInt(2, 4 + depth / 2));
                session.message("Spikes shoot up from the floor! You take " + damage + " damage.");
            }
            case POISON_NEEDLE -> {
                player.applyEffect(EffectType.POISONED, 5, 1 + depth / 4);
                session.message("A tiny needle pricks your foot. You are poisoned.");
            }
            case FLAME_VENT -> {
                for (Position p : player.position().neighbors()) {
                    Hazards.ignite(session, p);
                }
                player.applyEffect(EffectType.BURNING, 3, 2);
                session.message(player.effects().has(EffectType.BURNING)
                        ? "A vent roars and engulfs you in flame!" : "A jet of flame hisses harmlessly off your wet clothes.");
            }
            case ALARM -> {
                for (Monster m : session.monsters()) {
                    m.alert();
                    m.setLastKnownPlayerPosition(player.position());
                }
                session.message("A klaxon blares: \"BUILD FAILED! BUILD FAILED!\" Everything on this floor heard that.");
            }
            case TELEPORTER -> {
                teleportPlayer(session, rng);
                session.message("The floor glows and the room shifts around you!");
            }
            case TRAPDOOR -> {
                int damage = player.takeDamage(3);
                if (player.isAlive() && depth < session.deepestDepth() - 1) {
                    session.message("The floor gives way beneath you! You fall, taking " + damage + " damage.");
                    session.changeFloor(depth + 1);
                } else {
                    session.message("A trapdoor creaks open beneath you, but it is jammed. You take " + damage + " damage.");
                }
            }
            case SNARE -> {
                player.applyEffect(EffectType.STUNNED, 2, 0);
                session.message("A snare snaps shut around your ankle. You're deadlocked!");
            }
            default -> throw new IllegalStateException("Unknown trap " + trap.type());
        }
        if (!player.isAlive()) {
            session.playerKilled("killed by a " + trap.type().displayName());
        }
    }

    private static void teleportPlayer(GameSession session, Rng rng) {
        Floor floor = session.floor();
        List<Position> options = new ArrayList<>();
        for (Position p : floor.passablePositions()) {
            if (!session.isOccupied(p) && session.contents().trapAt(p) == null
                    && p.distanceTo(session.player().position()) > 6) {
                options.add(p);
            }
        }
        if (!options.isEmpty()) {
            session.player().setPosition(rng.pick(options));
        }
    }

    /**
     * Searches the eight tiles around the player for secret doors and hidden traps.
     *
     * @param session the game
     * @return how many hidden things were found
     */
    public static int search(GameSession session) {
        return searchAround(session, SEARCH_DOOR_PERCENT, SEARCH_TRAP_PERCENT);
    }

    /**
     * The free, low-chance check made after every step for adjacent hidden traps.
     *
     * @param session the game
     */
    public static void passiveSearch(GameSession session) {
        searchAround(session, 0, PASSIVE_TRAP_PERCENT);
    }

    private static int searchAround(GameSession session, int doorPercent, int trapPercent) {
        Rng rng = session.rng(RandomStream.EFFECTS);
        Floor floor = session.floor();
        Position here = session.player().position();
        int found = 0;
        for (Direction d : Direction.values()) {
            Position p = here.step(d);
            if (floor.terrainAt(p) == Terrain.SECRET_DOOR && rng.percent(doorPercent)) {
                floor.setTerrain(p, Terrain.DOOR_CLOSED);
                session.message("You find a hidden door!");
                found++;
            }
            Trap trap = session.contents().trapAt(p);
            if (trap != null && trap.isHidden() && rng.percent(trapPercent)) {
                trap.reveal();
                session.message("You spot a " + trap.type().displayName() + ".");
                found++;
            }
        }
        return found;
    }
}
