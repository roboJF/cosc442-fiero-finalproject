package edu.towson.cosc442.halls7800.world;

/**
 * Read-only view of a dungeon floor. The user interface and renderers depend on this interface
 * so that they cannot change game state directly.
 */
public interface FloorView {

    /** @return number of columns */
    int width();

    /** @return number of rows */
    int height();

    /** @return dungeon depth of this floor; 0 is the Atrium hub */
    int depth();

    /** @return display name of the floor */
    String name();

    /**
     * @param p a position
     * @return {@code true} if {@code p} lies inside the floor
     */
    boolean isInBounds(Position p);

    /**
     * Returns the true terrain at a position. Out-of-bounds positions are reported as walls.
     *
     * @param p a position
     * @return the terrain
     */
    Terrain terrainAt(Position p);

    /**
     * @param p a position
     * @return {@code true} if the player can currently see {@code p}
     */
    boolean isVisible(Position p);

    /**
     * @param p a position
     * @return {@code true} if the player has ever seen {@code p}
     */
    boolean isExplored(Position p);

    /** @return where a creature arrives when coming down from the floor above */
    Position entrance();

    /** @return the down staircase, or {@code null} on the deepest floor */
    Position exit();

    /**
     * Checks whether a single step from {@code from} in {@code direction} is legal on this floor's
     * terrain. Creatures are not considered.
     *
     * @param from starting position
     * @param direction direction of the step
     * @param allowClosedDoors whether closed doors count as passable (used for path planning)
     * @return {@code true} if the step is allowed
     */
    boolean canStep(Position from, Direction direction, boolean allowClosedDoors);
}
