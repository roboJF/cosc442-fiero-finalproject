package edu.towson.cosc442.halls7800.world;

import java.util.HashSet;
import java.util.Set;

/**
 * Computes which tiles are visible from a point using recursive shadowcasting.
 *
 * <p>The area around the viewer is split into eight octants. Each octant is scanned row by row
 * moving outward. Opaque tiles cast "shadows" (ranges of slopes) that hide the tiles behind them.
 * Opaque tiles themselves are visible, so walls bordering a room are drawn.
 */
public final class FieldOfView {

    // Octant transforms: (col, row) in octant space -> (dx, dy) in grid space.
    private static final int[] XX = {1, 0, 0, -1, -1, 0, 0, 1};
    private static final int[] XY = {0, 1, -1, 0, 0, -1, 1, 0};
    private static final int[] YX = {0, 1, 1, 0, 0, -1, -1, 0};
    private static final int[] YY = {1, 0, 0, 1, -1, 0, 0, -1};

    private FieldOfView() {
    }

    /**
     * Computes every position visible from {@code origin} within a circular radius.
     *
     * @param floor the floor to look across
     * @param origin the viewer's position (always visible)
     * @param radius maximum sight distance in tiles; 0 means only the origin is visible
     * @return the set of visible positions, all inside the floor
     */
    public static Set<Position> compute(FloorView floor, Position origin, int radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("radius must be non-negative: " + radius);
        }
        Set<Position> visible = new HashSet<>();
        if (!floor.isInBounds(origin)) {
            return visible;
        }
        visible.add(origin);
        for (int octant = 0; octant < 8; octant++) {
            castLight(floor, origin, radius, 1, 1.0, 0.0, octant, visible);
        }
        return visible;
    }

    /**
     * Convenience check of whether {@code target} is visible from {@code origin}.
     *
     * @param floor the floor
     * @param origin viewer position
     * @param target position to test
     * @param radius sight radius
     * @return {@code true} if the target is within the computed field of view
     */
    public static boolean canSee(FloorView floor, Position origin, Position target, int radius) {
        if (origin.squaredDistanceTo(target) > radius * radius) {
            return false;
        }
        return compute(floor, origin, radius).contains(target);
    }

    private static void castLight(FloorView floor, Position origin, int radius, int row,
                                  double startSlope, double endSlope, int octant, Set<Position> visible) {
        if (startSlope < endSlope) {
            return;
        }
        int radiusSquared = radius * radius;
        double nextStart = startSlope;
        for (int distance = row; distance <= radius; distance++) {
            boolean blocked = false;
            int dy = -distance;
            for (int dx = -distance; dx <= 0; dx++) {
                double leftSlope = (dx - 0.5) / (dy + 0.5);
                double rightSlope = (dx + 0.5) / (dy - 0.5);
                if (startSlope < rightSlope) {
                    continue;
                }
                if (endSlope > leftSlope) {
                    break;
                }
                int gx = origin.x() + dx * XX[octant] + dy * XY[octant];
                int gy = origin.y() + dx * YX[octant] + dy * YY[octant];
                Position p = new Position(gx, gy);
                boolean inBounds = floor.isInBounds(p);
                if (inBounds && dx * dx + dy * dy <= radiusSquared) {
                    visible.add(p);
                }
                boolean opaque = !inBounds || floor.terrainAt(p).blocksSight();
                if (blocked) {
                    if (opaque) {
                        nextStart = rightSlope;
                    } else {
                        blocked = false;
                        startSlope = nextStart;
                    }
                } else if (opaque && distance < radius) {
                    blocked = true;
                    castLight(floor, origin, radius, distance + 1, startSlope, leftSlope, octant, visible);
                    nextStart = rightSlope;
                }
            }
            if (blocked) {
                break;
            }
        }
    }
}
