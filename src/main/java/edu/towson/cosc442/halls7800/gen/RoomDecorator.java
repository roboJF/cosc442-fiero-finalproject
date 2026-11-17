package edu.towson.cosc442.halls7800.gen;

import edu.towson.cosc442.halls7800.util.Rng;
import edu.towson.cosc442.halls7800.util.WeightedTable;
import edu.towson.cosc442.halls7800.world.Direction;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.Pathfinder;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Room;
import edu.towson.cosc442.halls7800.world.RoomType;
import edu.towson.cosc442.halls7800.world.Terrain;
import edu.towson.cosc442.halls7800.world.Zone;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Third generation step: decides what each room is for, decorates interiors, and places the
 * staircases.
 */
public class RoomDecorator {

    /**
     * Assigns roles and decorations and places stairs.
     *
     * @param floor floor with rooms and corridors already carved
     * @param rooms the floor's rooms
     * @param zone supplies decoration weights and special-room chances
     * @param rng randomness source
     * @param needsExit whether to place a down staircase
     */
    public void decorate(Floor floor, List<Room> rooms, Zone zone, Rng rng, boolean needsExit) {
        Room entrance = rng.pick(rooms);
        entrance.setRole(Room.Role.ENTRANCE);
        Room exit = needsExit ? farthestRoom(floor, rooms, entrance) : null;
        if (exit != null) {
            exit.setRole(Room.Role.EXIT);
        }
        assignSpecialRooms(floor, rooms, zone, rng);
        assignTypes(rooms, zone, rng);
        for (Room room : rooms) {
            paint(floor, room, rng);
        }
        Position up = placeStairs(floor, entrance, Terrain.STAIRS_UP, rng);
        floor.setEntrance(up);
        if (exit != null) {
            floor.setExit(placeStairs(floor, exit, Terrain.STAIRS_DOWN, rng));
        }
    }

    /** The reachable room whose center is the most steps from the entrance's center. */
    private static Room farthestRoom(Floor floor, List<Room> rooms, Room from) {
        int[][] dist = Pathfinder.distanceMap(floor, from.center(), Integer.MAX_VALUE, true);
        Room best = null;
        int bestDistance = -1;
        for (Room r : rooms) {
            if (r == from) {
                continue;
            }
            int d = dist[r.center().y()][r.center().x()];
            if (d != Pathfinder.UNREACHABLE && d > bestDistance) {
                bestDistance = d;
                best = r;
            }
        }
        return best;
    }

    /** Turns at most one dead-end room into a vault and at most one into a hidden room. */
    private static void assignSpecialRooms(Floor floor, List<Room> rooms, Zone zone, Rng rng) {
        List<Room> deadEnds = new ArrayList<>();
        for (Room r : rooms) {
            if (r.role() == Room.Role.ORDINARY && r.isDeadEnd()) {
                deadEnds.add(r);
            }
        }
        rng.shuffle(deadEnds);
        int next = 0;
        if (next < deadEnds.size() && rng.percent(zone.vaultPercent())) {
            Room vault = deadEnds.get(next++);
            vault.setRole(Room.Role.VAULT);
            floor.setTerrain(vault.doorways().get(0), Terrain.DOOR_LOCKED);
        }
        if (next < deadEnds.size() && rng.percent(zone.secretPercent())) {
            Room hidden = deadEnds.get(next);
            hidden.setRole(Room.Role.HIDDEN);
            floor.setTerrain(hidden.doorways().get(0), Terrain.SECRET_DOOR);
        }
    }

    private static void assignTypes(List<Room> rooms, Zone zone, Rng rng) {
        WeightedTable<RoomType> table = zone.roomTypes();
        boolean shrineUsed = false;
        for (Room room : rooms) {
            WeightedTable<RoomType> choices = table;
            if (room.role() == Room.Role.ENTRANCE || room.role() == Room.Role.EXIT) {
                choices = choices.without(RoomType.CHASM_RIFT).without(RoomType.SHRINE);
            }
            if (shrineUsed) {
                choices = choices.without(RoomType.SHRINE);
            }
            RoomType type = choices.isEmpty() ? RoomType.PLAIN : choices.pick(rng);
            if (!fitsRoom(type, room)) {
                type = RoomType.PLAIN;
            }
            shrineUsed |= type == RoomType.SHRINE;
            room.setType(type);
        }
    }

    private static boolean fitsRoom(RoomType type, Room room) {
        return switch (type) {
            case PILLARED -> room.width() >= 5 && room.height() >= 5;
            case FLOODED -> room.width() >= 4 && room.height() >= 4;
            case CHASM_RIFT -> Math.max(room.width(), room.height()) >= 6 && Math.min(room.width(), room.height()) >= 3;
            case SHRINE -> room.width() >= 5 && room.height() >= 4;
            default -> true;
        };
    }

    /**
     * Interior tiles directly inside a wall opening. Solid decorations are never put there, so
     * every doorway stays usable.
     */
    static Set<Position> approachTiles(Floor floor, Room room) {
        Set<Position> result = new HashSet<>();
        for (Position wall : room.wallPositions()) {
            Terrain t = floor.terrainAt(wall);
            if (t.isPassable() || t.isDoor() || t == Terrain.SECRET_DOOR) {
                result.add(wall.step(room.outwardFrom(wall).opposite()));
            }
        }
        return result;
    }

    private static void paint(Floor floor, Room room, Rng rng) {
        Set<Position> approach = approachTiles(floor, room);
        switch (room.type()) {
            case PILLARED -> paintPillars(floor, room, approach);
            case FLOODED -> paintPool(floor, room);
            case OVERGROWN -> scatter(floor, room, Terrain.TALL_GRASS, 0.55, rng);
            case COLLAPSED -> scatter(floor, room, Terrain.RUBBLE, 0.3, rng);
            case CHASM_RIFT -> paintRift(floor, room, approach, rng);
            case SHRINE -> paintShrine(floor, room, approach);
            default -> {
                // plain rooms are left bare
            }
        }
    }

    private static void paintPillars(Floor floor, Room room, Set<Position> approach) {
        for (Position p : room.interior()) {
            int ix = p.x() - room.left();
            int iy = p.y() - room.top();
            if (ix % 2 == 1 && iy % 2 == 1 && ix < room.width() - 1 && iy < room.height() - 1
                    && !approach.contains(p)) {
                floor.setTerrain(p, Terrain.PILLAR);
            }
        }
    }

    private static void paintPool(Floor floor, Room room) {
        double cx = room.left() + (room.width() - 1) / 2.0;
        double cy = room.top() + (room.height() - 1) / 2.0;
        double rx = Math.max(1.0, (room.width() - 1) / 2.0);
        double ry = Math.max(1.0, (room.height() - 1) / 2.0);
        for (Position p : room.interior()) {
            double nx = (p.x() - cx) / rx;
            double ny = (p.y() - cy) / ry;
            if (nx * nx + ny * ny <= 0.85) {
                floor.setTerrain(p, Terrain.WATER);
            }
        }
    }

    private static void scatter(Floor floor, Room room, Terrain terrain, double density, Rng rng) {
        for (Position p : room.interior()) {
            if (rng.chance(density)) {
                floor.setTerrain(p, terrain);
            }
        }
    }

    /** A one-tile-wide chasm across the room's longer axis, with a single bridge. */
    private static void paintRift(Floor floor, Room room, Set<Position> approach, Rng rng) {
        boolean vertical = room.width() >= room.height();
        List<Position> band = new ArrayList<>();
        if (vertical) {
            int x = room.left() + room.width() / 2;
            for (int y = room.top(); y <= room.bottom(); y++) {
                band.add(new Position(x, y));
            }
        } else {
            int y = room.top() + room.height() / 2;
            for (int x = room.left(); x <= room.right(); x++) {
                band.add(new Position(x, y));
            }
        }
        Position bridge = band.get(rng.nextInt(band.size()));
        for (Position p : band) {
            if (!p.equals(bridge) && !approach.contains(p)) {
                floor.setTerrain(p, Terrain.CHASM);
            }
        }
    }

    private static void paintShrine(Floor floor, Room room, Set<Position> approach) {
        floor.setTerrain(room.center(), Terrain.ALTAR);
        Position[] corners = {
            new Position(room.left(), room.top()),
            new Position(room.right(), room.top()),
            new Position(room.left(), room.bottom()),
            new Position(room.right(), room.bottom()),
        };
        for (Position corner : corners) {
            if (!approach.contains(corner)) {
                floor.setTerrain(corner, Terrain.PILLAR);
            }
        }
    }

    /**
     * Puts a staircase on a free tile of the room, preferring plain floor away from doorways.
     */
    private static Position placeStairs(Floor floor, Room room, Terrain stairs, Rng rng) {
        Set<Position> approach = approachTiles(floor, room);
        List<Position> preferred = new ArrayList<>();
        List<Position> acceptable = new ArrayList<>();
        for (Position p : room.interior()) {
            Terrain t = floor.terrainAt(p);
            if (approach.contains(p) || !t.isPassable() || t == Terrain.ALTAR || t.name().startsWith("STAIRS")) {
                continue;
            }
            if (t == Terrain.FLOOR && !nextToSolid(floor, p)) {
                preferred.add(p);
            } else {
                acceptable.add(p);
            }
        }
        List<Position> pool = !preferred.isEmpty() ? preferred : acceptable;
        Position spot = pool.isEmpty() ? room.center() : rng.pick(pool);
        floor.setTerrain(spot, stairs);
        return spot;
    }

    private static boolean nextToSolid(Floor floor, Position p) {
        for (Direction d : Direction.CARDINALS) {
            Terrain t = floor.terrainAt(p.step(d));
            if (t == Terrain.PILLAR || t == Terrain.CHASM) {
                return true;
            }
        }
        return false;
    }
}
