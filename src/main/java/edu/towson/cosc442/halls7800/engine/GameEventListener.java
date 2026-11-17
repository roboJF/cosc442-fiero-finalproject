package edu.towson.cosc442.halls7800.engine;

/**
 * Receives {@link GameEvent}s published by a {@link GameSession}.
 */
@FunctionalInterface
public interface GameEventListener {

    /**
     * Called once for each event, in the order events happen.
     *
     * @param event the event
     */
    void onEvent(GameEvent event);
}
