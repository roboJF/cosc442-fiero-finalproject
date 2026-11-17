package edu.towson.cosc442.halls7800.gen;

import edu.towson.cosc442.halls7800.util.Rng;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Room;
import edu.towson.cosc442.halls7800.world.Terrain;
import edu.towson.cosc442.halls7800.world.Zone;
import java.util.ArrayList;
import java.util.List;

/**
 * First generation step: scatters non-overlapping rectangular rooms across an empty floor and
 * carves out their interiors.
 */
public class RoomPlacer {

    /** Minimum number of wall tiles between two rooms' walls. */
    static final int ROOM_MARGIN = 2;

    /** Placement attempts per requested room before giving up. */
    static final int ATTEMPTS_PER_ROOM = 40;

    /**
     * Places rooms by random trial: pick a size and position, keep the room if it does not come
     * too close to an existing room.
     *
     * @param floor an all-wall floor to carve into
     * @param zone supplies room count and size ranges
     * @param rng randomness source
     * @return the placed rooms, in placement order (ids 0, 1, 2, ...); may be fewer than requested
     */
    public List<Room> place(Floor floor, Zone zone, Rng rng) {
        int target = rng.nextInt(zone.minRooms(), zone.maxRooms());
        List<Room> rooms = new ArrayList<>();
        int attempts = target * ATTEMPTS_PER_ROOM;
        for (int i = 0; i < attempts && rooms.size() < target; i++) {
            int w = rng.nextInt(zone.minRoomWidth(), zone.maxRoomWidth());
            int h = rng.nextInt(zone.minRoomHeight(), zone.maxRoomHeight());
            int maxX = floor.width() - w - 2;
            int maxY = floor.height() - h - 2;
            if (maxX < 2 || maxY < 2) {
                continue;
            }
            Room candidate = new Room(rooms.size(), rng.nextInt(2, maxX), rng.nextInt(2, maxY), w, h);
            if (fits(candidate, rooms)) {
                rooms.add(candidate);
            }
        }
        for (Room room : rooms) {
            for (Position p : room.interior()) {
                floor.setTerrain(p, Terrain.FLOOR);
            }
        }
        return rooms;
    }

    private static boolean fits(Room candidate, List<Room> placed) {
        for (Room other : placed) {
            if (candidate.intersects(other, ROOM_MARGIN)) {
                return false;
            }
        }
        return true;
    }
}
