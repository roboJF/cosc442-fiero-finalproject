package edu.towson.cosc442.halls7800.gen;

import edu.towson.cosc442.halls7800.util.Rng;
import edu.towson.cosc442.halls7800.util.SplitMixRng;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.Room;
import edu.towson.cosc442.halls7800.world.Terrain;
import edu.towson.cosc442.halls7800.world.Zone;
import java.util.List;

/**
 * Builds a random floor for a depth from a seed.
 *
 * <p>Generation runs in three steps — {@link RoomPlacer}, {@link CorridorCarver},
 * {@link RoomDecorator} — and the result is checked by {@link FloorValidator}. If a floor is
 * invalid, the generator tries again with the same random stream (so retries are also
 * reproducible). After {@link #MAX_ATTEMPTS} failures it returns a simple two-room fallback floor.
 *
 * <p>The same depth and seed always produce exactly the same floor.
 */
public class DungeonGenerator {

    /** Generation attempts before falling back to the simple layout. */
    public static final int MAX_ATTEMPTS = 25;

    /** Fewest rooms a generated floor may have. */
    static final int MIN_ROOMS = 3;

    private final RoomPlacer placer = new RoomPlacer();
    private final CorridorCarver carver = new CorridorCarver();
    private final RoomDecorator decorator = new RoomDecorator();
    private final FloorValidator validator = new FloorValidator();
    private int lastAttempts;
    private boolean lastUsedFallback;

    /**
     * Generates a floor.
     *
     * @param depth a depth in a generated zone
     * @param seed the layout seed for this floor
     * @param needsExit whether to place a down staircase
     * @return a valid floor
     * @throws IllegalArgumentException if the depth is not in a generated zone
     */
    public Floor generate(int depth, long seed, boolean needsExit) {
        Zone zone = Zone.forDepth(depth);
        if (!zone.isGenerated()) {
            throw new IllegalArgumentException(zone + " floors are hand-drawn, not generated");
        }
        Rng rng = new SplitMixRng(seed);
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            Floor floor = attempt(depth, zone, rng, needsExit);
            if (floor != null && validator.validate(floor, needsExit).isEmpty()) {
                lastAttempts = attempt;
                lastUsedFallback = false;
                return floor;
            }
        }
        lastAttempts = MAX_ATTEMPTS;
        lastUsedFallback = true;
        return fallback(depth, zone, needsExit);
    }

    private Floor attempt(int depth, Zone zone, Rng rng, boolean needsExit) {
        Floor floor = new Floor(zone.width(), zone.height(), depth, zone.floorName(depth));
        List<Room> rooms = placer.place(floor, zone, rng);
        if (rooms.size() < MIN_ROOMS) {
            return null;
        }
        carver.connect(floor, rooms, zone, rng);
        rooms.forEach(floor::addRoom);
        decorator.decorate(floor, rooms, zone, rng, needsExit);
        return floor;
    }

    /** Two plain rooms joined by a corridor. Always valid. */
    private static Floor fallback(int depth, Zone zone, boolean needsExit) {
        Floor floor = new Floor(zone.width(), zone.height(), depth, zone.floorName(depth));
        Room west = new Room(0, 3, 3, 8, 6);
        Room east = new Room(1, zone.width() - 12, zone.height() - 10, 8, 6);
        for (Room r : List.of(west, east)) {
            r.interior().forEach(p -> floor.setTerrain(p, Terrain.FLOOR));
            floor.addRoom(r);
        }
        CorridorCarver.carve(floor, west.center(), east.center(), true);
        west.setRole(Room.Role.ENTRANCE);
        floor.setTerrain(west.center(), Terrain.STAIRS_UP);
        floor.setEntrance(west.center());
        if (needsExit) {
            east.setRole(Room.Role.EXIT);
            floor.setTerrain(east.center(), Terrain.STAIRS_DOWN);
            floor.setExit(east.center());
        }
        return floor;
    }

    /** @return number of attempts the most recent {@link #generate} call needed */
    public int lastAttempts() {
        return lastAttempts;
    }

    /** @return {@code true} if the most recent call had to return the fallback layout */
    public boolean lastUsedFallback() {
        return lastUsedFallback;
    }
}
