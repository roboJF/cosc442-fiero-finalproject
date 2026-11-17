package edu.towson.cosc442.halls7800.engine;

/**
 * Global game constants: timing, visibility and dungeon size.
 */
public final class GameRules {

    /** Ticks consumed by an ordinary action at normal speed. */
    public static final int NORMAL_ACTION_COST = 100;

    /** Extra ticks for wading through shallow water. */
    public static final int WATER_EXTRA_COST = 50;

    /** How far the player can see in tiles. */
    public static final int DEFAULT_SIGHT_RADIUS = 8;

    /** Maximum number of messages kept in the log. */
    public static final int MESSAGE_LOG_CAPACITY = 200;

    /** Safety limit on non-player actions between two player turns. */
    public static final int MAX_SCHEDULER_STEPS = 10_000;

    /** Depth of the Atrium hub. */
    public static final int HUB_DEPTH = 0;

    /** Depth of the final floor, where the Diploma is kept. */
    public static final int FINAL_DEPTH = 10;

    private GameRules() {
    }
}
