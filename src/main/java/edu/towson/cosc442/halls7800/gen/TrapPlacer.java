package edu.towson.cosc442.halls7800.gen;

import edu.towson.cosc442.halls7800.engine.FloorContents;
import edu.towson.cosc442.halls7800.engine.GameRules;
import edu.towson.cosc442.halls7800.util.Rng;
import edu.towson.cosc442.halls7800.util.WeightedTable;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Room;
import edu.towson.cosc442.halls7800.world.Terrain;
import edu.towson.cosc442.halls7800.world.Trap;
import java.util.ArrayList;
import java.util.List;

/**
 * Hides traps on a generated floor: on plain floor tiles in corridors and ordinary rooms, never in
 * the entrance room or within {@link #SAFE_RADIUS} tiles of the arrival point, and never on
 * stairs. Trapdoors are excluded when the next floor has a guardian and on the floor above
 * the Sanctum. The engine decides whether the player can fall through a trapdoor.
 */
public class TrapPlacer {

    /** No trap is placed this close to the floor's entrance. */
    public static final int SAFE_RADIUS = 4;

    /**
     * @param depth dungeon depth
     * @return the base number of traps for that depth
     */
    public static int trapCount(int depth) {
        return 1 + depth / 2;
    }

    /**
     * @param depth dungeon depth
     * @return trap types weighted for that depth
     */
    public static WeightedTable<Trap.Type> tableFor(int depth) {
        boolean bossBelow = ResidentPlacer.bossFor(depth + 1) != null;
        return new WeightedTable<Trap.Type>()
                .add(Trap.Type.SPIKE, 30)
                .add(Trap.Type.POISON_NEEDLE, depth >= 2 ? 20 : 0)
                .add(Trap.Type.FLAME_VENT, depth >= 4 ? 15 : 0)
                .add(Trap.Type.ALARM, 10)
                .add(Trap.Type.TELEPORTER, depth >= 3 ? 10 : 0)
                .add(Trap.Type.TRAPDOOR, !bossBelow && depth < GameRules.FINAL_DEPTH - 1 ? 8 : 0)
                .add(Trap.Type.SNARE, 10);
    }

    /**
     * Places hidden traps.
     *
     * @param floor a generated floor with rooms
     * @param contents where the traps go
     * @param rng randomness source (the floor's trap stream)
     */
    public void place(Floor floor, FloorContents contents, Rng rng) {
        List<Position> spots = candidateSpots(floor);
        if (spots.isEmpty()) {
            return;
        }
        WeightedTable<Trap.Type> table = tableFor(floor.depth());
        int count = trapCount(floor.depth()) + rng.nextInt(0, 1);
        for (int i = 0; i < count && !spots.isEmpty(); i++) {
            Position p = spots.remove(rng.nextInt(spots.size()));
            contents.addTrap(p, new Trap(table.pick(rng), true));
        }
    }

    private static List<Position> candidateSpots(Floor floor) {
        List<Position> spots = new ArrayList<>();
        Position entrance = floor.entrance();
        for (int y = 1; y < floor.height() - 1; y++) {
            for (int x = 1; x < floor.width() - 1; x++) {
                Position p = new Position(x, y);
                if (floor.terrainAt(p) != Terrain.FLOOR
                        || (entrance != null && p.distanceTo(entrance) <= SAFE_RADIUS)) {
                    continue;
                }
                Room room = floor.roomAt(p);
                if (room == null || room.role() == Room.Role.ORDINARY || room.role() == Room.Role.EXIT) {
                    spots.add(p);
                }
            }
        }
        return spots;
    }
}
