package edu.towson.cosc442.halls7800.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.towson.cosc442.halls7800.gen.FixedFloorSource;
import edu.towson.cosc442.halls7800.item.Gold;
import edu.towson.cosc442.halls7800.item.ItemCategory;
import edu.towson.cosc442.halls7800.world.Direction;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.FloorView;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Terrain;
import edu.towson.cosc442.halls7800.world.Trap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameSessionTest {

    private GameSession game;

    @BeforeEach
    void setUp() {
        FixedFloorSource source = new FixedFloorSource()
                .add("Hub",
                        "#######",
                        "#@.+.>#",
                        "#######")
                .add("Below",
                        "#####",
                        "#<..#",
                        "#####");
        game = new GameSession(1L, source, "Tester");
    }

    @Test
    void movingIntoOpenFloorTakesATurn() {
        CommandResult result = game.submit(new MoveCommand(Direction.EAST));

        assertTrue(result.performed());
        assertEquals(new Position(2, 1), game.player().position());
        assertEquals(1, game.turn());
    }

    @Test
    void walkingIntoAClosedDoorOpensIt() {
        FloorView view = game.currentFloor();
        Position door = new Position(3, 1);
        assertFalse(view instanceof Floor);
        assertEquals(Terrain.DOOR_CLOSED, view.terrainAt(door));

        game.submit(new MoveCommand(Direction.EAST));
        game.submit(new MoveCommand(Direction.EAST));

        assertEquals(Terrain.DOOR_OPEN, view.terrainAt(door));
        assertEquals(new Position(2, 1), game.player().position());
    }

    @Test
    void dropAndPickupUpdateTheFloorAndRecordedActions() {
        int slot = 2;
        Position at = game.player().position();
        GameSession.PackEntry original = game.packEntries().get(slot);
        List<GameSession.PackEntry> packBeforeDrop = game.packEntries();
        assertThrows(UnsupportedOperationException.class, packBeforeDrop::clear);

        assertTrue(game.submit(new DropCommand(slot)).performed());
        Map<Position, ItemCategory> icons = game.itemIcons();
        assertEquals(ItemCategory.FOOD, icons.get(at));
        assertThrows(UnsupportedOperationException.class, icons::clear);
        game.itemsAt(at).get(0).setQuantity(99);
        assertEquals(1, game.itemsAt(at).get(0).quantity());
        assertEquals(packBeforeDrop.size() - 1, game.packEntries().size());

        assertTrue(game.submit(new PickUpCommand()).performed());
        assertTrue(game.itemsAt(at).isEmpty());
        assertTrue(game.contents().piles().isEmpty());
        assertTrue(game.itemIcons().isEmpty());
        assertEquals(ItemCategory.FOOD, icons.get(at));
        assertEquals(original, game.packEntries().get(packBeforeDrop.size() - 1));
        assertEquals(List.of("R:2", "G"), game.recording());
    }

    @Test
    void walkingOntoGoldCollectsTheWholePile() {
        Position destination = new Position(2, 1);
        game.placeItem(destination, new Gold(7));
        game.placeItem(destination, new Gold(5));
        int before = game.player().gold();

        assertTrue(game.submit(new MoveCommand(Direction.EAST)).performed());

        assertEquals(before + 12, game.player().gold());
        assertTrue(game.itemIcons().isEmpty());
    }

    @Test
    void floorDisplayOmitsHiddenTrapsAndReturnsSnapshots() {
        Position at = new Position(2, 1);
        Trap trap = new Trap(Trap.Type.SPIKE, true);
        game.contents().addTrap(at, trap);
        game.contents().addFire(at, 3);
        Map<Position, Trap.Type> hidden = game.discoveredTraps();
        Map<Position, Integer> fire = game.burningTiles();
        assertTrue(hidden.isEmpty());
        assertEquals(Map.of(at, 3), fire);
        assertThrows(UnsupportedOperationException.class, fire::clear);

        trap.reveal();
        game.contents().removeFire(at);
        Map<Position, Trap.Type> discovered = game.discoveredTraps();
        assertEquals(Map.of(at, Trap.Type.SPIKE), discovered);
        assertThrows(UnsupportedOperationException.class, discovered::clear);
        assertTrue(hidden.isEmpty());
        assertTrue(game.burningTiles().isEmpty());
    }

    @Test
    void shopOffersUseTheSameSlotsAndPricesAsBuyCommands() {
        game.openShop();
        game.player().addGold(1_000);
        List<GameSession.ShopOffer> offers = game.shopOffers();
        assertThrows(UnsupportedOperationException.class, offers::clear);
        GameSession.ShopOffer first = offers.get(0);
        assertEquals("potion of healing", first.description());
        int gold = game.player().gold();

        assertTrue(game.buy(0).startsWith("You buy"));

        assertEquals(gold - first.price(), game.player().gold());
        assertEquals(offers.subList(1, offers.size()), game.shopOffers());
    }
}
