package edu.towson.cosc442.halls7800.gen;

import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.entity.MonsterTrait;
import edu.towson.cosc442.halls7800.entity.MonsterType;
import edu.towson.cosc442.halls7800.util.Rng;
import edu.towson.cosc442.halls7800.util.WeightedTable;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Room;
import edu.towson.cosc442.halls7800.world.Terrain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Places the starting monsters on a generated floor.
 *
 * <p>Monsters appear in ordinary and exit rooms, never in the entrance room, vaults or hidden
 * rooms, and never within {@link #SAFE_RADIUS} tiles of the arrival point. Pack monsters bring
 * up to {@link #PACK_EXTRA} companions.
 */
public class MonsterPopulator {

    /** No monster starts this close to the floor's entrance. */
    public static final int SAFE_RADIUS = 6;
    /** Extra members that may accompany a pack monster. */
    public static final int PACK_EXTRA = 2;
    /** Percent chance a non-ambusher starts asleep. */
    public static final int ASLEEP_PERCENT = 55;

    /**
     * @param depth a dungeon depth
     * @return how many monster groups to place at that depth
     */
    public static int groupCount(int depth) {
        return 3 + depth;
    }

    /**
     * @param depth a dungeon depth
     * @return a weighted table of monster types that appear at that depth
     */
    public static WeightedTable<MonsterType> tableFor(int depth) {
        WeightedTable<MonsterType> table = new WeightedTable<>();
        for (MonsterType type : MonsterType.values()) {
            if (type.appearsAt(depth)) {
                table.add(type, type.spawnWeight());
            }
        }
        return table;
    }

    /**
     * Creates monsters for a floor.
     *
     * @param floor a generated floor with rooms
     * @param rng randomness source (the floor's population stream)
     * @return the monsters, each on a distinct free tile
     */
    public List<Monster> populate(Floor floor, Rng rng) {
        return populate(floor, rng, 0);
    }

    /**
     * Creates monsters for a floor, adjusting the number of groups (for difficulty).
     *
     * @param floor a generated floor with rooms
     * @param rng randomness source (the floor's population stream)
     * @param extraGroups groups to add (or remove, if negative)
     * @return the monsters, each on a distinct free tile
     */
    public List<Monster> populate(Floor floor, Rng rng, int extraGroups) {
        List<Monster> monsters = new ArrayList<>();
        WeightedTable<MonsterType> table = tableFor(floor.depth());
        List<Position> spots = candidateSpots(floor);
        if (table.isEmpty() || spots.isEmpty()) {
            return monsters;
        }
        Set<Position> taken = new HashSet<>();
        int groups = Math.max(1, groupCount(floor.depth()) + extraGroups) + rng.nextInt(0, 1);
        for (int i = 0; i < groups && taken.size() < spots.size(); i++) {
            MonsterType type = table.pick(rng);
            Position spot = pickFree(spots, taken, rng);
            if (spot == null) {
                break;
            }
            boolean asleep = rng.percent(ASLEEP_PERCENT);
            monsters.add(place(type, spot, asleep, taken));
            if (type.has(MonsterTrait.PACK)) {
                int extra = rng.nextInt(1, PACK_EXTRA);
                for (Position near : spot.neighbors()) {
                    if (extra == 0) {
                        break;
                    }
                    if (spots.contains(near) && !taken.contains(near)) {
                        monsters.add(place(type, near, asleep, taken));
                        extra--;
                    }
                }
            }
        }
        return monsters;
    }

    private static Monster place(MonsterType type, Position spot, boolean asleep, Set<Position> taken) {
        taken.add(spot);
        return new Monster(type, spot, asleep);
    }

    private static Position pickFree(List<Position> spots, Set<Position> taken, Rng rng) {
        for (int tries = 0; tries < 50; tries++) {
            Position p = rng.pick(spots);
            if (!taken.contains(p)) {
                return p;
            }
        }
        for (Position p : spots) {
            if (!taken.contains(p)) {
                return p;
            }
        }
        return null;
    }

    private static List<Position> candidateSpots(Floor floor) {
        List<Position> spots = new ArrayList<>();
        Position entrance = floor.entrance();
        for (Room room : floor.rooms()) {
            if (room.role() != Room.Role.ORDINARY && room.role() != Room.Role.EXIT) {
                continue;
            }
            for (Position p : room.interior()) {
                Terrain t = floor.terrainAt(p);
                if (t.isPassable() && t != Terrain.STAIRS_DOWN && t != Terrain.STAIRS_UP
                        && (entrance == null || p.distanceTo(entrance) > SAFE_RADIUS)) {
                    spots.add(p);
                }
            }
        }
        return spots;
    }
}
