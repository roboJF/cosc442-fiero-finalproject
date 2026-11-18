package edu.towson.cosc442.halls7800.quest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.towson.cosc442.halls7800.engine.ActionRules;
import edu.towson.cosc442.halls7800.engine.Difficulty;
import edu.towson.cosc442.halls7800.engine.FloorContents;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.engine.MoveCommand;
import edu.towson.cosc442.halls7800.engine.PickUpCommand;
import edu.towson.cosc442.halls7800.entity.Npc;
import edu.towson.cosc442.halls7800.gen.FixedFloorSource;
import edu.towson.cosc442.halls7800.item.QuestItem;
import edu.towson.cosc442.halls7800.item.Scroll;
import edu.towson.cosc442.halls7800.world.Direction;
import edu.towson.cosc442.halls7800.world.Floor;
import java.util.List;
import org.junit.jupiter.api.Test;

class ArchivistTest {

    @Test
    void turnsInThreeStackedPagesAndRewardsThemOnce() {
        GameSession game = atrium(ActionRules.CURRENT);
        Quest quest = game.quests().get(Quest.Id.LOST_REQUIREMENTS);
        talkToArchivist(game);

        pickUpPage(game);
        pickUpPage(game);
        assertEquals(2, quest.progress());
        assertEquals(Quest.State.ACTIVE, quest.state());
        talkToArchivist(game);
        assertTrue(game.messages().lastText().contains("2 of 3 pages"));

        pickUpPage(game);
        List<QuestItem> pages = game.player().inventory().ofType(QuestItem.class);
        assertEquals(1, pages.size());
        assertEquals(3, pages.get(0).quantity());
        assertEquals(Quest.State.READY, quest.state());
        int gold = game.player().gold();

        talkToArchivist(game);
        assertEquals(Quest.State.COMPLETED, quest.state());
        assertTrue(game.player().inventory().ofType(QuestItem.class).isEmpty());
        assertEquals(gold + Conversations.REQUIREMENTS_GOLD, game.player().gold());
        assertEquals(1, rewardScrolls(game));
        assertTrue(game.identification().isKnown(Scroll.Kind.REFACTORING));

        talkToArchivist(game);
        assertEquals(Quest.State.COMPLETED, quest.state());
        assertEquals(gold + Conversations.REQUIREMENTS_GOLD, game.player().gold());
        assertEquals(1, rewardScrolls(game));
    }

    @Test
    void versionTwoCountsPageStacksAsBefore() {
        GameSession game = atrium(ActionRules.VERSION_2);
        talkToArchivist(game);

        for (int page = 0; page < 3; page++) {
            pickUpPage(game);
        }
        talkToArchivist(game);

        Quest quest = game.quests().get(Quest.Id.LOST_REQUIREMENTS);
        assertEquals(Quest.State.ACTIVE, quest.state());
        assertEquals(1, quest.progress());
        assertTrue(game.messages().lastText().contains("1 of 3 pages"));
        assertEquals(0, rewardScrolls(game));
    }

    private static GameSession atrium(ActionRules rules) {
        FixedFloorSource source = new FixedFloorSource() {
            @Override
            public FloorContents populate(Floor floor) {
                FloorContents contents = new FloorContents();
                contents.addNpc(new Npc(Npc.Role.ARCHIVIST, floor.entrance().step(Direction.EAST)));
                return contents;
            }
        }.add("Atrium", "#####", "#@..#", "#####");
        return new GameSession(62L, source, "Ada", Difficulty.UNDERGRADUATE, rules);
    }

    private static void talkToArchivist(GameSession game) {
        assertTrue(game.submit(new MoveCommand(Direction.EAST)).performed());
    }

    private static void pickUpPage(GameSession game) {
        game.placeItem(game.player().position(), new QuestItem(QuestItem.Kind.REQUIREMENTS_PAGE, 1));
        assertTrue(game.submit(new PickUpCommand()).performed());
    }

    private static int rewardScrolls(GameSession game) {
        return game.player().inventory().ofType(Scroll.class).stream()
                .filter(scroll -> scroll.kind() == Scroll.Kind.REFACTORING)
                .mapToInt(Scroll::quantity)
                .sum();
    }
}
