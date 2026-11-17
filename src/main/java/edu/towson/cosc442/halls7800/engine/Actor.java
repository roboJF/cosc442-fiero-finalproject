package edu.towson.cosc442.halls7800.engine;

/**
 * Anything that takes turns: the player, monsters, and timed effects.
 *
 * <p>Time is measured in integer <em>ticks</em>. A normal action costs
 * {@link GameRules#NORMAL_ACTION_COST} ticks; faster actors spend fewer ticks per action and so act
 * more often. The {@link TurnScheduler} always runs the actor with the smallest next-turn time,
 * breaking ties by the order actors were scheduled.
 */
public abstract class Actor {

    private long nextTurn;
    private long sequence;

    /**
     * Performs one action.
     *
     * @param session the running game
     * @return the number of ticks the action took (must be positive)
     */
    protected abstract int act(GameSession session);

    /**
     * Whether the actor should stay in the schedule. Dead monsters and expired effects return
     * {@code false} and are removed after their action.
     *
     * @return {@code true} while the actor is still taking turns
     */
    public boolean isActive() {
        return true;
    }

    /** @return the tick at which this actor next acts */
    public long nextTurn() {
        return nextTurn;
    }

    void setNextTurn(long nextTurn) {
        this.nextTurn = nextTurn;
    }

    long sequence() {
        return sequence;
    }

    void setSequence(long sequence) {
        this.sequence = sequence;
    }
}
