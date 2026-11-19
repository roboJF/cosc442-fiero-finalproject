package edu.towson.cosc442.halls7800.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.towson.cosc442.halls7800.gen.FixedFloorSource;
import edu.towson.cosc442.halls7800.item.Armor;
import edu.towson.cosc442.halls7800.item.Item;
import edu.towson.cosc442.halls7800.item.Potion;
import edu.towson.cosc442.halls7800.item.QuestItem;
import edu.towson.cosc442.halls7800.item.Scroll;
import edu.towson.cosc442.halls7800.item.Weapon;
import edu.towson.cosc442.halls7800.quest.Quest;
import edu.towson.cosc442.halls7800.world.Direction;
import edu.towson.cosc442.halls7800.world.Position;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ShrineAltarsTest {

    private static final Position FIRST_ALTAR = new Position(2, 1);
    private static final Position SECOND_ALTAR = new Position(4, 1);

    @Test
    void unknownCurseTakesOneDropToRevealAndAnotherToCleanse() {
        GameSession game = atAltar(1);
        assertShows(game, FIRST_ALTAR, "3 charges remaining");
        Weapon sword = new Weapon(Weapon.Kind.SHORT_SWORD, -2, false);

        String reveal = drop(game, sword).message();
        assertTrue(reveal.contains("-2 short sword"));
        assertTrue(reveal.contains("It is cursed."));
        Weapon revealed = (Weapon) topOfPile(game);
        assertTrue(revealed.isKnown());
        assertEquals(-2, revealed.upgrade());
        assertShows(game, FIRST_ALTAR, "2 charges remaining");

        int pickupSlot = game.player().inventory().items().size();
        assertTrue(game.submit(new PickUpCommand()).performed());
        String cleanse = game.submit(new DropCommand(pickupSlot)).message();
        assertTrue(cleanse.contains("lifts the curse"));
        Weapon cleansed = (Weapon) topOfPile(game);
        assertTrue(cleansed.isKnown());
        assertEquals(0, cleansed.upgrade());
        assertShows(game, FIRST_ALTAR, "dormant");

        Potion potion = new Potion(Potion.Kind.POISON, 1);
        drop(game, potion);
        assertFalse(game.identification().isKnown(potion.kind()));
        assertShows(game, FIRST_ALTAR, "dormant");
    }

    @Test
    void learningPotionAndScrollKindsCostsOneChargePerKind() {
        GameSession game = atAltar(1);
        Quest translation = game.quests().get(Quest.Id.TRANSLATION);
        translation.start();
        Scroll scroll = new Scroll(Scroll.Kind.DOCUMENTATION, 2);
        Potion potion = new Potion(Potion.Kind.POISON, 1);

        assertTrue(drop(game, scroll).message().contains("scrolls of documentation"));
        assertEquals(2, topOfPile(game).quantity());
        assertTrue(game.identification().isKnown(scroll.kind()));
        assertEquals(1, translation.progress());
        assertShows(game, FIRST_ALTAR, "2 charges remaining");

        assertTrue(drop(game, potion).message().contains("potion of poison"));
        assertTrue(game.identification().isKnown(potion.kind()));
        assertShows(game, FIRST_ALTAR, "1 charge remaining");

        drop(game, new Scroll(scroll.kind(), 1));
        drop(game, new Potion(potion.kind(), 1));
        assertEquals(1, translation.progress());
        assertShows(game, FIRST_ALTAR, "1 charge remaining");
    }

    @Test
    void oneChargeCanIdentifyArmorButCannotCleanseIt() {
        GameSession game = atAltar(1);
        Armor cursed = new Armor(Armor.Kind.CHAIN_MAIL, -1, true);
        assertTrue(drop(game, cursed).message().contains("lifts the curse"));
        assertEquals(0, ((Armor) topOfPile(game)).upgrade());
        assertShows(game, FIRST_ALTAR, "1 charge remaining");

        Armor another = new Armor(Armor.Kind.LEATHER, -2, true);
        assertEquals("You drop a -2 leather armor.", drop(game, another).message());
        assertEquals(-2, ((Armor) topOfPile(game)).upgrade());
        assertShows(game, FIRST_ALTAR, "1 charge remaining");

        Armor upgraded = new Armor(Armor.Kind.STUDDED_LEATHER, 2, false);
        String reveal = drop(game, upgraded).message();
        assertTrue(reveal.contains("+2 studded leather"));
        assertTrue(reveal.contains("It is uncursed."));
        assertTrue(((Armor) topOfPile(game)).isKnown());
        assertShows(game, FIRST_ALTAR, "dormant");
    }

    @Test
    void ordinaryDropsAndRejectedDropsLeaveChargesAlone() {
        GameSession game = atAltar(1);
        QuestItem page = new QuestItem(QuestItem.Kind.REQUIREMENTS_PAGE, 1);
        assertFalse(drop(game, page).message().contains("shrine"));
        assertEquals(QuestItem.Kind.REQUIREMENTS_PAGE, ((QuestItem) topOfPile(game)).kind());
        assertFalse(drop(game, new Weapon(Weapon.Kind.SHORT_SWORD, 1, true)).message().contains("shrine"));

        Weapon cursed = new Weapon(Weapon.Kind.DAGGER, -1, true);
        assertTrue(game.player().inventory().add(cursed));
        int slot = game.player().inventory().indexOf(cursed);
        assertTrue(game.submit(new UseItemCommand(slot)).performed());
        assertFalse(game.submit(new DropCommand(slot)).performed());
        assertEquals(cursed, game.player().inventory().get(slot));
        assertShows(game, FIRST_ALTAR, "3 charges remaining");
    }

    @Test
    void eachAltarKeepsItsOwnChargesAcrossFloorVisits() {
        GameSession game = atAltar(1);
        assertShows(game, SECOND_ALTAR, "3 charges remaining");
        drop(game, new Scroll(Scroll.Kind.DOCUMENTATION, 1));
        assertShows(game, SECOND_ALTAR, "3 charges remaining");

        game.changeFloor(2);
        assertShows(game, FIRST_ALTAR, "3 charges remaining");
        game.changeFloor(1);
        assertShows(game, FIRST_ALTAR, "2 charges remaining");

        game.floor().updateVisibility(List.of());
        assertShows(game, FIRST_ALTAR, "2 charges remaining");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 10})
    void atriumAndSanctumAltarsHaveNoShrinePower(int depth) {
        GameSession game = atAltar(depth);
        Scroll scroll = new Scroll(Scroll.Kind.DOCUMENTATION, 1);
        assertFalse(drop(game, scroll).message().contains("shrine"));
        assertFalse(game.identification().isKnown(scroll.kind()));
        assertFalse(Examiner.describe(game, FIRST_ALTAR).contains("shrine"));
        assertEquals(1, game.itemsAt(FIRST_ALTAR).size());
    }

    private static GameSession atAltar(int depth) {
        FixedFloorSource source = new FixedFloorSource();
        for (int floor = 0; floor <= GameRules.FINAL_DEPTH; floor++) {
            source.add("Floor " + floor, "#######", "#@_._.#", "#######");
        }
        GameSession game = new GameSession(1L, source, "Tester");
        if (depth != GameRules.HUB_DEPTH) {
            game.changeFloor(depth);
        }
        assertTrue(game.submit(new MoveCommand(Direction.EAST)).performed());
        assertEquals(FIRST_ALTAR, game.player().position());
        return game;
    }

    private static CommandResult drop(GameSession game, Item item) {
        assertTrue(game.player().inventory().add(item));
        CommandResult result = game.submit(new DropCommand(game.player().inventory().indexOf(item)));
        assertTrue(result.performed());
        return result;
    }

    private static void assertShows(GameSession game, Position position, String expected) {
        String description = Examiner.describe(game, position);
        assertTrue(description.contains(expected), description);
    }

    private static Item topOfPile(GameSession game) {
        List<Item> pile = game.itemsAt(FIRST_ALTAR);
        return pile.get(pile.size() - 1);
    }
}
