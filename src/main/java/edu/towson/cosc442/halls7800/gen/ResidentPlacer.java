package edu.towson.cosc442.halls7800.gen;

import edu.towson.cosc442.halls7800.engine.FloorContents;
import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.entity.MonsterType;
import edu.towson.cosc442.halls7800.entity.Npc;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Room;
import edu.towson.cosc442.halls7800.world.Terrain;

/**
 * Places the hand-picked characters of the Halls: the Atrium's residents, Warden Deng, the three
 * zone bosses, and the Hollow Registrar.
 */
public final class ResidentPlacer {

    /** Depth of Warden Deng's trial: the first floor of the Clockwork Stacks. */
    public static final int WARDEN_DEPTH = 4;

    private ResidentPlacer() {
    }

    /**
     * Puts the Atrium's residents in their rooms. Positions match {@link FixedLayouts#ATRIUM}.
     *
     * @param contents the Atrium's contents
     */
    public static void placeAtriumResidents(FloorContents contents) {
        contents.addNpc(new Npc(Npc.Role.ARCHIVIST, new Position(22, 6)));
        contents.addNpc(new Npc(Npc.Role.TINKERER, new Position(4, 7)));
        contents.addNpc(new Npc(Npc.Role.LOREMASTER, new Position(4, 2)));
        contents.addNpc(new Npc(Npc.Role.SCRIBE, new Position(38, 2)));
        contents.addNpc(new Npc(Npc.Role.TIGER_STATUE, new Position(4, 12)));
        contents.addNpc(new Npc(Npc.Role.NOTICE_BOARD, new Position(38, 12)));
        contents.addNpc(new Npc(Npc.Role.LOCKSMITH, new Position(39, 10)));
        contents.addNpc(new Npc(Npc.Role.ZOMBIE_KID, new Position(2, 14)));
    }

    /**
     * @param depth a dungeon depth
     * @return the boss guarding the stairs at that depth, or {@code null}
     */
    public static MonsterType bossFor(int depth) {
        return switch (depth) {
            case 3 -> MonsterType.SPAGHETTI_HYDRA;
            case 6 -> MonsterType.DEADLOCK_COLOSSUS;
            case 9 -> MonsterType.ZERO_DAY_WYRM;
            default -> null;
        };
    }

    /**
     * Puts the zone boss (if this depth has one) in the exit room, next to the stairs down.
     *
     * @param floor a generated floor
     * @param contents its contents
     */
    public static void placeZoneBoss(Floor floor, FloorContents contents) {
        MonsterType type = bossFor(floor.depth());
        Room exitRoom = floor.roomWithRole(Room.Role.EXIT);
        if (type == null || exitRoom == null) {
            return;
        }
        Position spot = freeSpotNear(floor, contents, floor.exit(), exitRoom);
        if (spot != null) {
            contents.addMonster(new Monster(type, spot, true));
        }
    }

    /**
     * Puts Warden Deng in the entrance room, beside the arrival point.
     *
     * @param floor the first floor of the Clockwork Stacks
     * @param contents its contents
     */
    public static void placeWarden(Floor floor, FloorContents contents) {
        Room entrance = floor.roomWithRole(Room.Role.ENTRANCE);
        if (entrance == null) {
            return;
        }
        Position spot = freeSpotNear(floor, contents, floor.entrance(), entrance);
        if (spot != null) {
            contents.addNpc(new Npc(Npc.Role.WARDEN, spot));
        }
    }

    /**
     * Puts the Hollow Registrar in the middle of the Sanctum's hall.
     *
     * @param floor the Sanctum
     * @param contents its contents
     */
    public static void placeRegistrar(Floor floor, FloorContents contents) {
        Position center = new Position(floor.width() / 2, floor.height() / 2);
        Position spot = floor.isPassable(center) ? center : floor.positionsOf(Terrain.FLOOR).get(0);
        contents.addMonster(new Monster(MonsterType.HOLLOW_REGISTRAR, spot, true));
    }

    /** The free interior tile of the room closest to {@code target} (but not on it). */
    private static Position freeSpotNear(Floor floor, FloorContents contents, Position target, Room room) {
        Position best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Position p : room.interior()) {
            Terrain t = floor.terrainAt(p);
            if (p.equals(target) || t != Terrain.FLOOR || contents.monsterAt(p) != null || contents.npcAt(p) != null) {
                continue;
            }
            int d = p.distanceTo(target);
            if (d < bestDistance) {
                bestDistance = d;
                best = p;
            }
        }
        return best;
    }
}
