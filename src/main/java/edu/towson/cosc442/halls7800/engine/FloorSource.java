package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.world.Floor;

/**
 * Supplies floors to the game session the first time the player reaches each depth. Visited
 * floors are kept by the session, so each depth is requested at most once per run.
 */
public interface FloorSource {

    /**
     * Builds the floor for a depth.
     *
     * @param depth depth from {@link GameRules#HUB_DEPTH} to {@link #deepestDepth()}
     * @return a new floor
     */
    Floor createFloor(int depth);

    /** @return the deepest depth this source can build */
    int deepestDepth();

    /**
     * Creates the monsters and items that start on a newly created floor. Called once per floor,
     * right after {@link #createFloor(int)}.
     *
     * @param floor the new floor
     * @return the floor's starting contents (empty by default)
     */
    default FloorContents populate(Floor floor) {
        return new FloorContents();
    }

    /** @return the first message shown in a new run */
    default String welcomeMessage() {
        return "You stand in the Atrium of the Halls of 7800, deep beneath Towson University. "
                + "Somewhere far below lies the Diploma.";
    }

    /** @return {@code true} if this source builds the Orientation tutorial */
    default boolean isTutorial() {
        return false;
    }
}
