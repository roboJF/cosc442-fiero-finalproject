package edu.towson.cosc442.halls7800.gen;

import edu.towson.cosc442.halls7800.util.Rng;
import edu.towson.cosc442.halls7800.world.Direction;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Room;
import edu.towson.cosc442.halls7800.world.Terrain;
import edu.towson.cosc442.halls7800.world.Zone;
import java.util.ArrayList;
import java.util.List;

/**
 * Second generation step: connects rooms with L-shaped corridors and then turns suitable wall
 * openings into doors.
 *
 * <p>Rooms are first joined by a minimum spanning tree over their center distances, which
 * guarantees every room is connected. A few extra corridors are then added between near
 * neighbors so the floor has loops rather than only dead ends.
 */
public class CorridorCarver {

    /**
     * Connects all rooms and places doors.
     *
     * @param floor floor whose room interiors are already carved
     * @param rooms the rooms to connect
     * @param zone supplies loop and door chances
     * @param rng randomness source
     * @return the corridor edges as pairs of room ids, in carving order
     */
    public List<int[]> connect(Floor floor, List<Room> rooms, Zone zone, Rng rng) {
        List<int[]> edges = spanningTree(rooms);
        addLoops(rooms, edges, zone, rng);
        for (int[] edge : edges) {
            Room a = rooms.get(edge[0]);
            Room b = rooms.get(edge[1]);
            carve(floor, a.center(), b.center(), rng.chance(0.5));
            a.connectTo(b.id());
            b.connectTo(a.id());
        }
        placeDoors(floor, rooms, zone, rng);
        return edges;
    }

    /**
     * Prim's algorithm on Manhattan distance between room centers. Ties go to the lower room ids,
     * so the tree depends only on the room positions.
     */
    static List<int[]> spanningTree(List<Room> rooms) {
        List<int[]> edges = new ArrayList<>();
        if (rooms.isEmpty()) {
            return edges;
        }
        boolean[] inTree = new boolean[rooms.size()];
        inTree[0] = true;
        for (int added = 1; added < rooms.size(); added++) {
            int bestFrom = -1;
            int bestTo = -1;
            int bestDistance = Integer.MAX_VALUE;
            for (int i = 0; i < rooms.size(); i++) {
                if (!inTree[i]) {
                    continue;
                }
                for (int j = 0; j < rooms.size(); j++) {
                    if (inTree[j]) {
                        continue;
                    }
                    int d = rooms.get(i).center().manhattanDistanceTo(rooms.get(j).center());
                    if (d < bestDistance) {
                        bestDistance = d;
                        bestFrom = i;
                        bestTo = j;
                    }
                }
            }
            inTree[bestTo] = true;
            edges.add(new int[] {bestFrom, bestTo});
        }
        return edges;
    }

    private static void addLoops(List<Room> rooms, List<int[]> edges, Zone zone, Rng rng) {
        for (Room room : rooms) {
            Room nearest = null;
            int nearestDistance = Integer.MAX_VALUE;
            for (Room other : rooms) {
                if (other == room || hasEdge(edges, room.id(), other.id())) {
                    continue;
                }
                int d = room.center().manhattanDistanceTo(other.center());
                if (d < nearestDistance) {
                    nearestDistance = d;
                    nearest = other;
                }
            }
            if (nearest != null && rng.percent(zone.loopPercent())) {
                edges.add(new int[] {room.id(), nearest.id()});
            }
        }
    }

    private static boolean hasEdge(List<int[]> edges, int a, int b) {
        for (int[] e : edges) {
            if ((e[0] == a && e[1] == b) || (e[0] == b && e[1] == a)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Carves an L-shaped corridor between two points through solid rock. The outer edge of the
     * floor is never carved.
     *
     * @param floor the floor
     * @param from start point
     * @param to end point
     * @param horizontalFirst whether to move along the row first, then the column
     */
    static void carve(Floor floor, Position from, Position to, boolean horizontalFirst) {
        Position corner = horizontalFirst ? new Position(to.x(), from.y()) : new Position(from.x(), to.y());
        carveLine(floor, from, corner);
        carveLine(floor, corner, to);
    }

    private static void carveLine(Floor floor, Position from, Position to) {
        Position p = from;
        while (true) {
            if (floor.isInterior(p) && floor.terrainAt(p) == Terrain.WALL) {
                floor.setTerrain(p, Terrain.FLOOR);
            }
            if (p.equals(to)) {
                return;
            }
            p = p.step(Direction.toward(p, to).orElseThrow());
        }
    }

    /**
     * Records each room's openings and puts doors in the proper ones. A proper doorway is a single
     * broken wall tile with intact wall on both sides along the wall and open ground straight in
     * front of it on both sides.
     */
    private static void placeDoors(Floor floor, List<Room> rooms, Zone zone, Rng rng) {
        for (Room room : rooms) {
            int openings = 0;
            for (Position wall : room.wallPositions()) {
                if (!floor.terrainAt(wall).isPassable()) {
                    continue;
                }
                openings++;
                if (isProperDoorway(floor, room, wall)) {
                    room.addDoorway(wall);
                    if (rng.percent(zone.doorPercent())) {
                        floor.setTerrain(wall, Terrain.DOOR_CLOSED);
                    }
                }
            }
            room.setOpenings(openings);
        }
    }

    private static boolean isProperDoorway(Floor floor, Room room, Position wall) {
        Direction out = room.outwardFrom(wall);
        Direction along = out.clockwise().clockwise();
        boolean flanked = floor.terrainAt(wall.step(along)).isSolid()
                && floor.terrainAt(wall.step(along.opposite())).isSolid();
        boolean openOutside = floor.terrainAt(wall.step(out)).isPassable();
        boolean openInside = floor.terrainAt(wall.step(out.opposite())).isPassable();
        return flanked && openOutside && openInside;
    }
}
