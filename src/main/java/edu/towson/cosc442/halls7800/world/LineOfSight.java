package edu.towson.cosc442.halls7800.world;

import java.util.ArrayList;
import java.util.List;

/**
 * Straight-line tracing between two grid positions, used for thrown items, ranged attacks and
 * targeting.
 */
public final class LineOfSight {

    private LineOfSight() {
    }

    /**
     * Returns the tiles on a straight line between two positions using Bresenham's algorithm.
     *
     * @param from start (included as the first element)
     * @param to end (included as the last element)
     * @return the ordered tiles of the line
     */
    public static List<Position> line(Position from, Position to) {
        List<Position> result = new ArrayList<>();
        int x = from.x();
        int y = from.y();
        int dx = Math.abs(to.x() - x);
        int dy = -Math.abs(to.y() - y);
        int sx = x < to.x() ? 1 : -1;
        int sy = y < to.y() ? 1 : -1;
        int err = dx + dy;
        while (true) {
            result.add(new Position(x, y));
            if (x == to.x() && y == to.y()) {
                break;
            }
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y += sy;
            }
        }
        return result;
    }

    /**
     * Checks whether nothing opaque lies strictly between two positions.
     *
     * @param floor the floor
     * @param from start
     * @param to end
     * @return {@code true} if every intermediate tile lets sight through
     */
    public static boolean isClear(FloorView floor, Position from, Position to) {
        List<Position> path = line(from, to);
        for (int i = 1; i < path.size() - 1; i++) {
            Position p = path.get(i);
            if (!floor.isInBounds(p) || floor.terrainAt(p).blocksSight()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Traces a projectile from {@code from} toward {@code to}, stopping at the first tile that
     * cannot be entered or at {@code maxRange}.
     *
     * @param floor the floor
     * @param from launch position (not included)
     * @param to aimed position
     * @param maxRange maximum number of tiles travelled
     * @return the tiles the projectile passes through; the last element is where it lands
     */
    public static List<Position> trajectory(FloorView floor, Position from, Position to, int maxRange) {
        List<Position> line = line(from, to);
        List<Position> result = new ArrayList<>();
        for (int i = 1; i < line.size() && result.size() < maxRange; i++) {
            Position p = line.get(i);
            if (!floor.isInBounds(p) || !floor.terrainAt(p).isPassable()) {
                break;
            }
            result.add(p);
        }
        return result;
    }
}
