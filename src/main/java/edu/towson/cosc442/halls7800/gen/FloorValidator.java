package edu.towson.cosc442.halls7800.gen;

import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.Pathfinder;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Room;
import edu.towson.cosc442.halls7800.world.Terrain;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Checks that a generated floor is playable. The generator retries when a floor fails.
 *
 * <p>The rules:
 * <ul>
 *   <li>the entrance is an up staircase and, if required, the exit is a down staircase;</li>
 *   <li>nothing walkable or door-like lies on the outer edge of the floor;</li>
 *   <li>the exit can be reached from the entrance without passing locked or secret doors;</li>
 *   <li>every room that is not a vault or hidden room can be reached from the entrance;</li>
 *   <li>enough of the floor is open ground.</li>
 * </ul>
 */
public class FloorValidator {

    /** Minimum fraction of the floor area that must be passable. */
    static final double MIN_OPEN_FRACTION = 0.12;

    /**
     * @param floor the floor to check
     * @param needsExit whether a down staircase is required
     * @return a list of problems; empty means the floor is valid
     */
    public List<String> validate(Floor floor, boolean needsExit) {
        List<String> problems = new ArrayList<>();
        Position entrance = floor.entrance();
        if (entrance == null || floor.terrainAt(entrance) != Terrain.STAIRS_UP) {
            problems.add("entrance is not an up staircase");
            return problems;
        }
        if (needsExit && (floor.exit() == null || floor.terrainAt(floor.exit()) != Terrain.STAIRS_DOWN)) {
            problems.add("exit is not a down staircase");
        }
        checkBorder(floor, problems);

        Set<Position> reachable = Pathfinder.reachableFrom(floor, entrance, true);
        if (needsExit && floor.exit() != null && !reachable.contains(floor.exit())) {
            problems.add("exit is unreachable from entrance");
        }
        for (Room room : floor.rooms()) {
            if (room.role() == Room.Role.VAULT || room.role() == Room.Role.HIDDEN) {
                continue;
            }
            boolean anyReachable = false;
            for (Position p : room.interior()) {
                if (reachable.contains(p)) {
                    anyReachable = true;
                    break;
                }
            }
            if (!anyReachable) {
                problems.add("room " + room.id() + " is unreachable");
            }
        }
        int open = floor.passablePositions().size();
        if (open < MIN_OPEN_FRACTION * floor.width() * floor.height()) {
            problems.add("only " + open + " open tiles");
        }
        return problems;
    }

    private static void checkBorder(Floor floor, List<String> problems) {
        for (int x = 0; x < floor.width(); x++) {
            checkEdge(floor, new Position(x, 0), problems);
            checkEdge(floor, new Position(x, floor.height() - 1), problems);
        }
        for (int y = 1; y < floor.height() - 1; y++) {
            checkEdge(floor, new Position(0, y), problems);
            checkEdge(floor, new Position(floor.width() - 1, y), problems);
        }
    }

    private static void checkEdge(Floor floor, Position p, List<String> problems) {
        Terrain t = floor.terrainAt(p);
        if (t.isPassable() || t.isDoor()) {
            problems.add("open tile on border at " + p);
        }
    }
}
