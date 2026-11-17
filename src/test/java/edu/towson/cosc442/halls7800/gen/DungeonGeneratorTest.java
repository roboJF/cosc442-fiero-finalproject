package edu.towson.cosc442.halls7800.gen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.towson.cosc442.halls7800.engine.AsciiRenderer;
import edu.towson.cosc442.halls7800.engine.Difficulty;
import edu.towson.cosc442.halls7800.engine.FloorContents;
import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.entity.MonsterType;
import edu.towson.cosc442.halls7800.item.KeyItem;
import edu.towson.cosc442.halls7800.item.QuestItem;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.Pathfinder;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Room;
import edu.towson.cosc442.halls7800.world.Terrain;
import edu.towson.cosc442.halls7800.world.Zone;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DungeonGeneratorTest {

    private static final long[] REGRESSION_SEEDS = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 62, Long.MIN_VALUE
    };

    @Test
    void sameSeedProducesSameFloor() {
        Floor first = new DungeonGenerator().generate(3, 12345L, true);
        Floor second = new DungeonGenerator().generate(3, 12345L, true);

        assertEquals(AsciiRenderer.render(first, null, false), AsciiRenderer.render(second, null, false));
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    void seededFloorsKeepProgressionReachable(Difficulty difficulty) {
        for (long seed : REGRESSION_SEEDS) {
            ProceduralFloorSource source = new ProceduralFloorSource(seed, difficulty);
            for (int depth = 1; depth <= 9; depth++) {
                Floor floor = source.createFloor(depth);
                String context = difficulty + ", seed " + seed + ", depth " + depth;
                Set<Position> reachable = assertReachableExit(floor, context);
                assertProgressionItemsAndGuardian(floor, source.populate(floor), reachable, context);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(names = {"UNDERCROFT", "CLOCKWORK_STACKS", "EMBER_VAULTS"})
    void fallbackKeepsBothRoomsAndProgressionReachable(Zone zone) throws ReflectiveOperationException {
        int depth = zone.lastDepth();
        String context = zone + " fallback";
        Floor floor = fallback(depth, zone, true);
        Set<Position> reachable = assertReachableExit(floor, context);

        assertFallbackLayout(floor, reachable);
        assertProgressionItemsAndGuardian(floor, new ProceduralFloorSource(62).populate(floor), reachable, context);
    }

    @Test
    void fallbackCanOmitTheDownStaircase() throws ReflectiveOperationException {
        Floor floor = fallback(1, Zone.UNDERCROFT, false);

        assertNotNull(floor.entrance());
        assertEquals(Terrain.STAIRS_UP, floor.terrainAt(floor.entrance()));
        assertNull(floor.exit());
        assertTrue(floor.positionsOf(Terrain.STAIRS_DOWN).isEmpty());
        assertFallbackLayout(floor, Pathfinder.reachableFrom(floor, floor.entrance(), true));
    }

    private static Floor fallback(int depth, Zone zone, boolean needsExit) throws ReflectiveOperationException {
        // Invoke the fallback directly because a seed that exhausts all retries could stop doing so.
        Method fallback = DungeonGenerator.class.getDeclaredMethod("fallback", int.class, Zone.class, boolean.class);
        fallback.setAccessible(true);
        return (Floor) fallback.invoke(null, depth, zone, needsExit);
    }

    private static void assertFallbackLayout(Floor floor, Set<Position> reachable) {
        assertTrue(floor.rooms().size() >= 2);
        for (Room room : floor.rooms()) {
            assertTrue(reachable.contains(room.center()), () -> "Unreachable fallback room " + room.id());
        }
        assertTrue(reachable.containsAll(floor.passablePositions()), "Disconnected walkable tiles in fallback");
        for (int y = 0; y < floor.height(); y++) {
            for (int x = 0; x < floor.width(); x++) {
                Position tile = new Position(x, y);
                if (!floor.isInterior(tile)) {
                    Terrain terrain = floor.terrainAt(tile);
                    assertFalse(terrain.isPassable() || terrain.isDoor(), () -> "Open fallback edge at " + tile);
                }
            }
        }
    }

    private static Set<Position> assertReachableExit(Floor floor, String context) {
        assertNotNull(floor.entrance(), context);
        assertEquals(Terrain.STAIRS_UP, floor.terrainAt(floor.entrance()), context);
        assertNotNull(floor.exit(), context);
        assertEquals(Terrain.STAIRS_DOWN, floor.terrainAt(floor.exit()), context);
        Set<Position> reachable = Pathfinder.reachableFrom(floor, floor.entrance(), true);
        assertTrue(reachable.contains(floor.exit()), () -> context + " has an unreachable exit");
        return reachable;
    }

    private static void assertProgressionItemsAndGuardian(Floor floor, FloorContents contents,
                                                         Set<Position> reachable, String context) {
        List<Monster> guardians = contents.monsters().stream().filter(Monster::isBoss).toList();
        List<MonsterType> expectedGuardians = switch (floor.depth()) {
            case 3 -> List.of(MonsterType.SPAGHETTI_HYDRA);
            case 6 -> List.of(MonsterType.DEADLOCK_COLOSSUS);
            case 9 -> List.of(MonsterType.ZERO_DAY_WYRM);
            default -> List.of();
        };
        assertEquals(expectedGuardians, guardians.stream().map(Monster::type).toList(), context);
        for (Monster guardian : guardians) {
            assertTrue(reachable.contains(guardian.position()), () -> context + " has an unreachable guardian");
        }

        int pages = 0;
        int keys = 0;
        for (var pile : contents.piles().entrySet()) {
            for (var item : pile.getValue()) {
                if (item instanceof QuestItem questItem && questItem.kind() == QuestItem.Kind.REQUIREMENTS_PAGE) {
                    assertTrue(reachable.contains(pile.getKey()), () -> context + " has an unreachable page");
                    pages += item.quantity();
                }
                if (item instanceof KeyItem) {
                    assertTrue(reachable.contains(pile.getKey()), () -> context + " has an unreachable vault key");
                    keys += item.quantity();
                }
            }
        }
        assertEquals(floor.depth() <= 3 ? 1 : 0, pages, context);
        assertEquals(floor.roomWithRole(Room.Role.VAULT) == null ? 0 : 1, keys, context);
    }
}
