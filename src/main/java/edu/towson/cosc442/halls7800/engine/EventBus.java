package edu.towson.cosc442.halls7800.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A minimal synchronous publish/subscribe hub for {@link GameEvent}s.
 */
public class EventBus {

    private final List<GameEventListener> listeners = new ArrayList<>();

    /**
     * Registers a listener.
     *
     * @param listener the listener to add
     */
    public void subscribe(GameEventListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /**
     * Unregisters a listener.
     *
     * @param listener the listener to remove
     * @return {@code true} if it was registered
     */
    public boolean unsubscribe(GameEventListener listener) {
        return listeners.remove(listener);
    }

    /**
     * Delivers an event to every listener. Listeners added or removed during delivery take
     * effect for the next event.
     *
     * @param event the event to deliver
     */
    public void publish(GameEvent event) {
        for (GameEventListener listener : List.copyOf(listeners)) {
            listener.onEvent(event);
        }
    }

    /** @return how many listeners are registered */
    public int listenerCount() {
        return listeners.size();
    }
}
