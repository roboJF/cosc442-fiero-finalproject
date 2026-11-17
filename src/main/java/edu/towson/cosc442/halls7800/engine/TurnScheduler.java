package edu.towson.cosc442.halls7800.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Orders the actions of all actors on the current floor by game time.
 *
 * <p>The player is just another actor, but the player's actions come from commands rather than
 * from {@link Actor#act}. After the player acts, {@link #runUntil} lets every other actor whose
 * turn comes first take its action, and returns as soon as it is the player's turn again.
 */
public class TurnScheduler {

    private final List<Actor> actors = new ArrayList<>();
    private long now;
    private long nextSequence;

    /** @return the current game time in ticks */
    public long now() {
        return now;
    }

    /**
     * Adds an actor that will act {@code delay} ticks from now.
     *
     * @param actor the actor; must not already be scheduled
     * @param delay ticks until its first action (0 or more)
     */
    public void add(Actor actor, long delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("delay must be non-negative: " + delay);
        }
        if (actors.contains(actor)) {
            throw new IllegalStateException("Actor already scheduled: " + actor);
        }
        actor.setNextTurn(now + delay);
        actor.setSequence(nextSequence++);
        actors.add(actor);
    }

    /**
     * Removes an actor. Removing an actor that is not scheduled has no effect.
     *
     * @param actor the actor
     * @return {@code true} if the actor was scheduled
     */
    public boolean remove(Actor actor) {
        return actors.remove(actor);
    }

    /**
     * @param actor an actor
     * @return {@code true} if the actor is scheduled
     */
    public boolean contains(Actor actor) {
        return actors.contains(actor);
    }

    /** @return the scheduled actors, in the order they were added */
    public List<Actor> actors() {
        return Collections.unmodifiableList(actors);
    }

    /**
     * Removes every actor except {@code keep}. Used when the player changes floors.
     *
     * @param keep the actor to retain (usually the player); may be {@code null}
     */
    public void clearExcept(Actor keep) {
        actors.removeIf(a -> a != keep);
    }

    /**
     * Records that an actor used {@code cost} ticks, pushing its next turn later.
     *
     * @param actor the actor that acted
     * @param cost ticks spent; must be positive
     */
    public void spend(Actor actor, int cost) {
        if (cost <= 0) {
            throw new IllegalArgumentException("Action cost must be positive: " + cost);
        }
        actor.setNextTurn(actor.nextTurn() + cost);
    }

    /**
     * Delays an actor without it acting (for example, when it is paralyzed).
     *
     * @param actor the actor
     * @param ticks ticks of delay; must be non-negative
     */
    public void delay(Actor actor, int ticks) {
        if (ticks < 0) {
            throw new IllegalArgumentException("ticks must be non-negative: " + ticks);
        }
        actor.setNextTurn(actor.nextTurn() + ticks);
    }

    /**
     * Returns the actor that acts next, without running it.
     *
     * @return the next actor, or {@code null} if nothing is scheduled
     */
    public Actor peekNext() {
        Actor best = null;
        for (Actor a : actors) {
            if (best == null
                    || a.nextTurn() < best.nextTurn()
                    || (a.nextTurn() == best.nextTurn() && a.sequence() < best.sequence())) {
                best = a;
            }
        }
        return best;
    }

    /**
     * Runs actors in time order until it is {@code stopAt}'s turn or the game is no longer in
     * progress.
     *
     * @param stopAt the actor whose turn ends the loop (normally the player)
     * @param session the game, passed to each actor
     * @throws IllegalStateException if {@code stopAt} is not scheduled, or if actors keep acting
     *                               without the player's turn arriving (a runaway loop)
     */
    public void runUntil(Actor stopAt, GameSession session) {
        if (!actors.contains(stopAt)) {
            throw new IllegalStateException("Actor to stop at is not scheduled");
        }
        int steps = 0;
        while (true) {
            Actor next = peekNext();
            now = Math.max(now, next.nextTurn());
            if (next == stopAt || session.phase() != GamePhase.PLAYING) {
                return;
            }
            if (++steps > GameRules.MAX_SCHEDULER_STEPS) {
                throw new IllegalStateException("Scheduler exceeded " + GameRules.MAX_SCHEDULER_STEPS + " steps");
            }
            int cost = next.act(session);
            if (!next.isActive()) {
                actors.remove(next);
            } else if (actors.contains(next)) {
                spend(next, Math.max(1, cost));
            }
        }
    }

    /**
     * Moves an already-scheduled actor's next turn to {@code delay} ticks from now. Used when the
     * player returns to a floor whose actors were scheduled at an older time.
     *
     * @param actor the actor to re-schedule
     * @param delay ticks from now until its next action
     */
    public void reschedule(Actor actor, long delay) {
        if (!actors.contains(actor)) {
            throw new IllegalStateException("Actor is not scheduled");
        }
        actor.setNextTurn(now + Math.max(0, delay));
    }
}
