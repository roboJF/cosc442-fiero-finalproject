package edu.towson.cosc442.halls7800.persist;

import edu.towson.cosc442.halls7800.engine.CommandCodec;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.gen.GameFactory;

/**
 * Rebuilds a session from a {@link RunRecord} by starting a new run with the same seed,
 * difficulty and name, and re-applying every recorded action. A complete record reproduces the
 * original run when replayed with the same game rules.
 *
 * <p>Loading a saved game uses this.
 */
public final class ReplayRunner {

    private ReplayRunner() {
    }

    /**
     * Replays a whole record.
     *
     * @param record the run to rebuild
     * @return the session in the state the record leaves it
     * @throws IllegalArgumentException if the record contains an invalid action
     */
    public static GameSession replay(RunRecord record) {
        return replay(record, record.actions().size());
    }

    /**
     * Replays the first {@code steps} actions of a record. Useful for finding the exact step at
     * which something goes wrong.
     *
     * @param record the run to rebuild
     * @param steps how many actions to apply
     * @return the session after those actions
     * @throws IllegalArgumentException if an action in the replayed portion is invalid
     */
    public static GameSession replay(RunRecord record, int steps) {
        GameSession session = GameFactory.newGame(record.seed(), record.playerName(), record.difficulty());
        int limit = Math.min(steps, record.actions().size());
        for (int i = 0; i < limit; i++) {
            String token = record.actions().get(i);
            try {
                CommandCodec.apply(session, token);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid action " + (i + 1) + " ('" + token + "'): " + e.getMessage(), e);
            }
        }
        return session;
    }
}
