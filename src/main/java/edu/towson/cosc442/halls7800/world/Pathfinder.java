package edu.towson.cosc442.halls7800.world;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Grid path planning over a floor's terrain using 8-way movement.
 *
 * <p>All searches are deterministic: ties are broken by discovery order and neighbors are visited
 * in {@link Direction} order, so the same floor always yields the same path.
 */
public final class Pathfinder {

    /** Distance value used in distance maps for unreachable tiles. */
    public static final int UNREACHABLE = Integer.MAX_VALUE;

    private Pathfinder() {
    }

    /**
     * Finds a shortest path using A* search.
     *
     * @param floor the floor
     * @param from starting position
     * @param to goal position
     * @param allowClosedDoors whether closed doors may be crossed
     * @param blocked extra positions to avoid (e.g. occupied by creatures); the goal is never
     *                treated as blocked
     * @return the steps from (but not including) {@code from} to {@code to}; empty if no path
     *         exists or the positions are equal
     */
    public static List<Position> findPath(FloorView floor, Position from, Position to,
                                          boolean allowClosedDoors, Predicate<Position> blocked) {
        if (from.equals(to) || !floor.isInBounds(to)) {
            return List.of();
        }
        Map<Position, Position> cameFrom = new HashMap<>();
        Map<Position, Integer> cost = new HashMap<>();
        PriorityQueue<Node> open = new PriorityQueue<>(
                Comparator.comparingInt(Node::priority).thenComparingInt(Node::order));
        int order = 0;
        cost.put(from, 0);
        open.add(new Node(from, from.distanceTo(to), order++));
        Set<Position> closed = new HashSet<>();

        while (!open.isEmpty()) {
            Node current = open.poll();
            if (!closed.add(current.position())) {
                continue;
            }
            if (current.position().equals(to)) {
                return rebuild(cameFrom, from, to);
            }
            int currentCost = cost.get(current.position());
            for (Direction d : Direction.values()) {
                if (!floor.canStep(current.position(), d, allowClosedDoors)) {
                    continue;
                }
                Position next = current.position().step(d);
                if (closed.contains(next) || (!next.equals(to) && blocked.test(next))) {
                    continue;
                }
                int newCost = currentCost + 1;
                Integer known = cost.get(next);
                if (known == null || newCost < known) {
                    cost.put(next, newCost);
                    cameFrom.put(next, current.position());
                    open.add(new Node(next, newCost + next.distanceTo(to), order++));
                }
            }
        }
        return List.of();
    }

    /**
     * Finds a shortest path, treating closed doors as passable and ignoring creatures.
     *
     * @param floor the floor
     * @param from start
     * @param to goal
     * @return the path, or an empty list if unreachable
     */
    public static List<Position> findPath(FloorView floor, Position from, Position to) {
        return findPath(floor, from, to, true, p -> false);
    }

    /**
     * Computes the step distance from {@code origin} to every reachable tile (a "Dijkstra map").
     * Monsters use this to approach or flee from the player.
     *
     * @param floor the floor
     * @param origin source of the distances
     * @param maxDistance stop expanding past this distance
     * @param allowClosedDoors whether closed doors may be crossed
     * @return a {@code [height][width]} array; unreachable tiles hold {@link #UNREACHABLE}
     */
    public static int[][] distanceMap(FloorView floor, Position origin, int maxDistance, boolean allowClosedDoors) {
        int[][] dist = new int[floor.height()][floor.width()];
        for (int[] row : dist) {
            java.util.Arrays.fill(row, UNREACHABLE);
        }
        if (!floor.isInBounds(origin)) {
            return dist;
        }
        ArrayDeque<Position> queue = new ArrayDeque<>();
        dist[origin.y()][origin.x()] = 0;
        queue.add(origin);
        while (!queue.isEmpty()) {
            Position p = queue.poll();
            int d = dist[p.y()][p.x()];
            if (d >= maxDistance) {
                continue;
            }
            for (Direction dir : Direction.values()) {
                if (!floor.canStep(p, dir, allowClosedDoors)) {
                    continue;
                }
                Position n = p.step(dir);
                if (dist[n.y()][n.x()] == UNREACHABLE) {
                    dist[n.y()][n.x()] = d + 1;
                    queue.add(n);
                }
            }
        }
        return dist;
    }

    /**
     * Flood-fills every position reachable from {@code origin}.
     *
     * @param floor the floor
     * @param origin starting position
     * @param allowClosedDoors whether closed doors may be crossed
     * @return reachable positions including the origin, in discovery order
     */
    public static Set<Position> reachableFrom(FloorView floor, Position origin, boolean allowClosedDoors) {
        Set<Position> seen = new LinkedHashSet<>();
        if (!floor.isInBounds(origin)) {
            return seen;
        }
        ArrayDeque<Position> queue = new ArrayDeque<>();
        seen.add(origin);
        queue.add(origin);
        while (!queue.isEmpty()) {
            Position p = queue.poll();
            for (Direction dir : Direction.values()) {
                if (floor.canStep(p, dir, allowClosedDoors)) {
                    Position n = p.step(dir);
                    if (seen.add(n)) {
                        queue.add(n);
                    }
                }
            }
        }
        return seen;
    }

    private static List<Position> rebuild(Map<Position, Position> cameFrom, Position from, Position to) {
        List<Position> path = new ArrayList<>();
        Position p = to;
        while (!p.equals(from)) {
            path.add(p);
            p = cameFrom.get(p);
        }
        Collections.reverse(path);
        return path;
    }

    private record Node(Position position, int priority, int order) {
    }
}
