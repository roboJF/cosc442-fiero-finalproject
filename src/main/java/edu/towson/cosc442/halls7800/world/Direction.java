package edu.towson.cosc442.halls7800.world;

import java.util.List;
import java.util.Optional;

/**
 * The eight compass directions a creature can step on the dungeon grid.
 *
 * <p>Rows grow toward the south, so {@link #NORTH} has a negative {@code dy}.
 */
public enum Direction {
    NORTH(0, -1),
    NORTH_EAST(1, -1),
    EAST(1, 0),
    SOUTH_EAST(1, 1),
    SOUTH(0, 1),
    SOUTH_WEST(-1, 1),
    WEST(-1, 0),
    NORTH_WEST(-1, -1);

    /** The four orthogonal directions, in clockwise order starting at north. */
    public static final List<Direction> CARDINALS = List.of(NORTH, EAST, SOUTH, WEST);

    /** The four diagonal directions, in clockwise order starting at north-east. */
    public static final List<Direction> DIAGONALS = List.of(NORTH_EAST, SOUTH_EAST, SOUTH_WEST, NORTH_WEST);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /** @return the column offset of one step in this direction */
    public int dx() {
        return dx;
    }

    /** @return the row offset of one step in this direction */
    public int dy() {
        return dy;
    }

    /** @return {@code true} if this direction changes both the row and the column */
    public boolean isDiagonal() {
        return dx != 0 && dy != 0;
    }

    /** @return the direction pointing the opposite way */
    public Direction opposite() {
        return fromDelta(-dx, -dy).orElseThrow();
    }

    /**
     * Returns the direction rotated 45 degrees clockwise.
     *
     * @return the next direction clockwise
     */
    public Direction clockwise() {
        return values()[(ordinal() + 1) % values().length];
    }

    /**
     * Returns the direction rotated 45 degrees counter-clockwise.
     *
     * @return the next direction counter-clockwise
     */
    public Direction counterClockwise() {
        return values()[(ordinal() + values().length - 1) % values().length];
    }

    /**
     * Finds the direction whose offset has the same sign as the given delta.
     * For example {@code fromDelta(5, -2)} is {@link #NORTH_EAST}.
     *
     * @param deltaX column difference
     * @param deltaY row difference
     * @return the matching direction, or empty if both deltas are zero
     */
    public static Optional<Direction> fromDelta(int deltaX, int deltaY) {
        int sx = Integer.signum(deltaX);
        int sy = Integer.signum(deltaY);
        for (Direction d : values()) {
            if (d.dx == sx && d.dy == sy) {
                return Optional.of(d);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the single step that moves {@code from} closest toward {@code to}.
     *
     * @param from starting position
     * @param to target position
     * @return the direction, or empty if the positions are equal
     */
    public static Optional<Direction> toward(Position from, Position to) {
        return fromDelta(to.x() - from.x(), to.y() - from.y());
    }
}
