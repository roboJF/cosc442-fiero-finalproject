package edu.towson.cosc442.halls7800.gen;

import edu.towson.cosc442.halls7800.engine.FloorContents;
import edu.towson.cosc442.halls7800.item.ItemGenerator;
import edu.towson.cosc442.halls7800.item.KeyItem;
import edu.towson.cosc442.halls7800.item.QuestItem;
import edu.towson.cosc442.halls7800.util.Rng;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Room;
import java.util.ArrayList;
import java.util.List;

/**
 * Scatters items and gold across a generated floor.
 *
 * <p>Ordinary rooms receive a depth-scaled number of random items and a few gold piles. A vault
 * holds {@link #VAULT_ITEMS} items generated as if two floors deeper, plus a large gold pile. A
 * hidden room holds one deeper item and some gold. A floor with a vault also gets the vault's key,
 * and each of the first three floors holds one requirements page.
 */
public class LootPlacer {

    /** Items placed in a vault. */
    public static final int VAULT_ITEMS = 3;
    /** One requirements page lies on each of the first this-many floors. */
    public static final int REQUIREMENTS_PAGE_FLOORS = 3;

    private final ItemGenerator generator = new ItemGenerator();

    /**
     * @param depth dungeon depth
     * @return the number of loose items placed in ordinary rooms
     */
    public static int itemCount(int depth) {
        return 3 + depth / 2;
    }

    /**
     * Adds loot to a floor's contents.
     *
     * @param floor a generated floor with rooms
     * @param contents where the items go
     * @param rng randomness source (the floor's loot stream)
     */
    public void place(Floor floor, FloorContents contents, Rng rng) {
        int depth = floor.depth();
        List<Position> open = new ArrayList<>();
        for (Room room : floor.rooms()) {
            switch (room.role()) {
                case VAULT -> {
                    for (int i = 0; i < VAULT_ITEMS; i++) {
                        contents.addItem(freeSpot(floor, room, rng), generator.randomItem(depth + 2, rng));
                    }
                    contents.addItem(freeSpot(floor, room, rng), generator.gold(depth + 3, rng));
                }
                case HIDDEN -> {
                    contents.addItem(freeSpot(floor, room, rng), generator.randomItem(depth + 1, rng));
                    contents.addItem(freeSpot(floor, room, rng), generator.gold(depth, rng));
                }
                default -> {
                    for (Position p : room.interior()) {
                        if (floor.terrainAt(p).isPassable() && !p.equals(floor.entrance()) && !p.equals(floor.exit())) {
                            open.add(p);
                        }
                    }
                }
            }
        }
        if (open.isEmpty()) {
            return;
        }
        int items = itemCount(depth) + rng.nextInt(0, 2);
        for (int i = 0; i < items; i++) {
            contents.addItem(rng.pick(open), generator.randomItem(depth, rng));
        }
        int piles = rng.nextInt(2, 4);
        for (int i = 0; i < piles; i++) {
            contents.addItem(rng.pick(open), generator.gold(depth, rng));
        }
        if (floor.roomWithRole(Room.Role.VAULT) != null) {
            contents.addItem(rng.pick(open), new KeyItem(depth));
        }
        if (depth >= 1 && depth <= REQUIREMENTS_PAGE_FLOORS) {
            contents.addItem(rng.pick(open), new QuestItem(QuestItem.Kind.REQUIREMENTS_PAGE, 1));
        }
    }

    private static Position freeSpot(Floor floor, Room room, Rng rng) {
        List<Position> spots = new ArrayList<>();
        for (Position p : room.interior()) {
            if (floor.terrainAt(p).isPassable()) {
                spots.add(p);
            }
        }
        return spots.isEmpty() ? room.center() : rng.pick(spots);
    }
}
