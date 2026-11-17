package edu.towson.cosc442.halls7800.world;

/**
 * The kinds of ground a floor tile can have, and the movement/visibility rules each one implies.
 *
 * <p>Each terrain has a <em>layout symbol</em> used in hand-written floor layouts and by the
 * ASCII renderer. Secret doors use {@code S} in layouts but are displayed as walls until found.
 */
public enum Terrain {
    FLOOR('.', true, false, false, false, "floor"),
    WALL('#', false, true, true, false, "wall"),
    DOOR_CLOSED('+', false, true, false, true, "closed door"),
    DOOR_OPEN('\'', true, false, false, true, "open door"),
    DOOR_LOCKED('=', false, true, false, true, "locked door"),
    SECRET_DOOR('S', false, true, true, false, "wall"),
    STAIRS_DOWN('>', true, false, false, false, "staircase down"),
    STAIRS_UP('<', true, false, false, false, "staircase up"),
    WATER('~', true, false, false, false, "shallow water"),
    CHASM(':', false, false, false, false, "chasm"),
    TALL_GRASS('"', true, true, false, false, "tall grass"),
    RUBBLE(',', true, false, false, false, "rubble"),
    PILLAR('O', false, true, true, false, "pillar"),
    ALTAR('_', true, false, false, false, "altar");

    private final char symbol;
    private final boolean passable;
    private final boolean blocksSight;
    private final boolean solid;
    private final boolean door;
    private final String description;

    Terrain(char symbol, boolean passable, boolean blocksSight, boolean solid, boolean door,
            String description) {
        this.symbol = symbol;
        this.passable = passable;
        this.blocksSight = blocksSight;
        this.solid = solid;
        this.door = door;
        this.description = description;
    }

    /** @return the character used for this terrain in floor layouts */
    public char symbol() {
        return symbol;
    }

    /** @return the character shown to the player (secret doors look like walls) */
    public char displaySymbol() {
        return appearance().symbol;
    }

    /** @return {@code true} if a creature may stand on this tile */
    public boolean isPassable() {
        return passable;
    }

    /** @return {@code true} if this tile stops line of sight */
    public boolean blocksSight() {
        return blocksSight;
    }

    /**
     * Solid terrain is a full-height obstruction. Creatures cannot cut diagonally past the corner
     * of a solid tile.
     *
     * @return {@code true} for walls, pillars, and hidden doors
     */
    public boolean isSolid() {
        return solid;
    }

    /** @return {@code true} for open, closed and locked doors (not undiscovered secret doors) */
    public boolean isDoor() {
        return door;
    }

    /** @return a short lower-case description, as the player perceives it */
    public String description() {
        return description;
    }

    /**
     * Returns how the terrain looks to the player. Undiscovered secret doors appear as walls.
     *
     * @return the visible terrain
     */
    public Terrain appearance() {
        return this == SECRET_DOOR ? WALL : this;
    }

    /**
     * Whether a path may go through this tile. Path planning treats closed doors as traversable
     * because a creature can open them by walking into them.
     *
     * @param allowClosedDoors whether closed (unlocked) doors count as traversable
     * @return {@code true} if a path may cross this tile
     */
    public boolean isTraversable(boolean allowClosedDoors) {
        return passable || (allowClosedDoors && this == DOOR_CLOSED);
    }

    /**
     * Looks up a terrain by its layout symbol.
     *
     * @param symbol the layout character
     * @return the matching terrain
     * @throws IllegalArgumentException if no terrain uses that symbol
     */
    public static Terrain fromSymbol(char symbol) {
        for (Terrain t : values()) {
            if (t.symbol == symbol) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown terrain symbol '" + symbol + "'");
    }
}
