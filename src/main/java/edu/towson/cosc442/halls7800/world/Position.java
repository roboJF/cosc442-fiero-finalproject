package edu.towson.cosc442.halls7800.world;

import java.util.ArrayList;
import java.util.List;

/**
 * An immutable (column, row) coordinate on a dungeon floor grid.
 *
 * @param x the column, increasing to the east
 * @param y the row, increasing to the south
 */
public record Position(int x, int y) {

    /**
     * Returns the position one step away in the given direction.
     *
     * @param direction the direction to step
     * @return the neighboring position
     */
    public Position step(Direction direction) {
        return new Position(x + direction.dx(), y + direction.dy());
    }

    /**
     * Returns this position shifted by an arbitrary offset.
     *
     * @param dx column offset
     * @param dy row offset
     * @return the shifted position
     */
    public Position offset(int dx, int dy) {
        return new Position(x + dx, y + dy);
    }

    /**
     * Chebyshev ("king move") distance: the number of 8-way steps between two positions
     * on an open floor.
     *
     * @param other the other position
     * @return the Chebyshev distance
     */
    public int distanceTo(Position other) {
        return Math.max(Math.abs(x - other.x), Math.abs(y - other.y));
    }

    /**
     * Manhattan distance: the number of orthogonal steps between two positions.
     *
     * @param other the other position
     * @return the Manhattan distance
     */
    public int manhattanDistanceTo(Position other) {
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }

    /**
     * Squared Euclidean distance, useful for circular radius checks without square roots.
     *
     * @param other the other position
     * @return the squared straight-line distance
     */
    public int squaredDistanceTo(Position other) {
        int dx = x - other.x;
        int dy = y - other.y;
        return dx * dx + dy * dy;
    }

    /**
     * @param other the other position
     * @return {@code true} if the positions touch, including diagonally (but are not equal)
     */
    public boolean isAdjacentTo(Position other) {
        return distanceTo(other) == 1;
    }

    /** @return the eight surrounding positions in {@link Direction} order */
    public List<Position> neighbors() {
        List<Position> result = new ArrayList<>(8);
        for (Direction d : Direction.values()) {
            result.add(step(d));
        }
        return result;
    }

    /** @return the four orthogonally adjacent positions */
    public List<Position> orthogonalNeighbors() {
        List<Position> result = new ArrayList<>(4);
        for (Direction d : Direction.CARDINALS) {
            result.add(step(d));
        }
        return result;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
