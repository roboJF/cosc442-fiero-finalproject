package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.item.QuestItem;
import edu.towson.cosc442.halls7800.quest.Quest;
import edu.towson.cosc442.halls7800.quest.QuestLog;
import edu.towson.cosc442.halls7800.world.Terrain;

/**
 * Takes the staircase the player is standing on, either down or up. A living boss seals the stairs
 * down on its floor. Climbing the Atrium's up staircase while carrying the Diploma wins the game.
 */
public final class StairsCommand implements Command {

    private final boolean down;

    private StairsCommand(boolean down) {
        this.down = down;
    }

    /** @return a command to go down the staircase under the player */
    public static StairsCommand descend() {
        return new StairsCommand(true);
    }

    /** @return a command to go up the staircase under the player */
    public static StairsCommand ascend() {
        return new StairsCommand(false);
    }

    /** @return {@code true} if this command goes down */
    public boolean isDown() {
        return down;
    }

    @Override
    public String encode() {
        return down ? "S:D" : "S:U";
    }

    @Override
    public CommandResult execute(GameSession session) {
        Terrain here = session.floor().terrainAt(session.player().position());
        int depth = session.depth();
        if (down) {
            if (here != Terrain.STAIRS_DOWN) {
                return CommandResult.rejected("There is no staircase leading down here.");
            }
            if (session.isTutorial()) {
                session.endGame(GamePhase.VICTORY, "Orientation complete! You are ready for the Halls of 7800.");
                return CommandResult.acted(session.player().actionCost());
            }
            if (depth >= session.deepestDepth()) {
                return CommandResult.rejected("The stairs end in a wall of rubble.");
            }
            Monster boss = session.livingBoss();
            if (boss != null) {
                return CommandResult.rejected("A shimmering seal blocks the stairs. " + boss.subject()
                        + " must be defeated first.");
            }
            session.changeFloor(depth + 1);
        } else {
            if (here != Terrain.STAIRS_UP) {
                return CommandResult.rejected("There is no staircase leading up here.");
            }
            if (depth == GameRules.HUB_DEPTH) {
                if (QuestLog.countQuestItems(session.player().inventory(), QuestItem.Kind.DIPLOMA) == 0) {
                    return CommandResult.rejected("You will not leave the Halls without the Diploma.");
                }
                session.quests().update(session);
                session.quests().get(Quest.Id.DIPLOMA).complete();
                session.endGame(GamePhase.VICTORY, "You climb into the daylight with the Diploma of Ascendance. "
                        + "Somewhere above, a crowd in black and gold begins to cheer. Congratulations, graduate!");
                return CommandResult.acted(session.player().actionCost());
            }
            session.changeFloor(depth - 1);
        }
        return CommandResult.acted(session.player().actionCost());
    }
}
