package edu.towson.cosc442.halls7800.gen;

import edu.towson.cosc442.halls7800.engine.Difficulty;
import edu.towson.cosc442.halls7800.engine.GameSession;

/**
 * Creates fully configured game sessions.
 */
public final class GameFactory {

    /** Name given to the adventurer when none is chosen. */
    public static final String DEFAULT_PLAYER_NAME = "Adventurer";

    private GameFactory() {
    }

    /**
     * Starts a standard run: Atrium, nine generated floors, and the Sanctum.
     *
     * @param seed the run seed
     * @param playerName the adventurer's name
     * @return a new session, positioned in the Atrium
     */
    public static GameSession newGame(long seed, String playerName) {
        return newGame(seed, playerName, Difficulty.UNDERGRADUATE);
    }

    /**
     * Starts a standard run at a chosen difficulty.
     *
     * @param seed the run seed
     * @param playerName the adventurer's name
     * @param difficulty how hard the run is
     * @return a new session, positioned in the Atrium
     */
    public static GameSession newGame(long seed, String playerName, Difficulty difficulty) {
        return new GameSession(seed, new ProceduralFloorSource(seed, difficulty), playerName, difficulty);
    }

    /**
     * Starts the Orientation tutorial.
     *
     * @param playerName the adventurer's name
     * @return a new tutorial session
     */
    public static GameSession newTutorial(String playerName) {
        return new GameSession(TutorialFloorSource.SEED, new TutorialFloorSource(), playerName, Difficulty.FRESHMAN);
    }

    /**
     * Starts a standard run with the default adventurer name.
     *
     * @param seed the run seed
     * @return a new session
     */
    public static GameSession newGame(long seed) {
        return newGame(seed, DEFAULT_PLAYER_NAME);
    }
}
