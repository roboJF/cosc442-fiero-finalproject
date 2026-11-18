package edu.towson.cosc442.halls7800.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A bounded history of messages shown to the player. When full, the oldest message is dropped.
 * Repeating the same message on the same turn increments a counter instead of adding a line.
 */
public final class MessageLog {

    /**
     * One line of the log.
     *
     * @param turn the player turn on which the message was added
     * @param text the message
     * @param repeats how many times the message occurred (1 or more)
     */
    public record Entry(int turn, String text, int repeats) {

        /** @return the text, with a "(xN)" suffix if it repeated */
        public String display() {
            return repeats > 1 ? text + " (x" + repeats + ")" : text;
        }
    }

    private final int capacity;
    private final Deque<Entry> entries = new ArrayDeque<>();

    /**
     * @param capacity maximum number of entries kept (at least 1)
     */
    public MessageLog(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be at least 1");
        }
        this.capacity = capacity;
    }

    /**
     * Adds a message.
     *
     * @param turn current player turn
     * @param text message text; blank messages are ignored
     */
    public void add(int turn, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        Entry last = entries.peekLast();
        if (last != null && last.turn() == turn && last.text().equals(text)) {
            entries.pollLast();
            entries.addLast(new Entry(turn, text, last.repeats() + 1));
            return;
        }
        entries.addLast(new Entry(turn, text, 1));
        while (entries.size() > capacity) {
            entries.pollFirst();
        }
    }

    /**
     * @param count maximum number of entries to return
     * @return the most recent entries, oldest first
     */
    public List<Entry> recent(int count) {
        List<Entry> all = new ArrayList<>(entries);
        return all.subList(Math.max(0, all.size() - count), all.size());
    }

    /** @return every entry, oldest first */
    public List<Entry> all() {
        return new ArrayList<>(entries);
    }

    /** @return the most recent message text, or {@code null} if the log is empty */
    public String lastText() {
        Entry last = entries.peekLast();
        return last == null ? null : last.text();
    }

    /** @return number of entries */
    public int size() {
        return entries.size();
    }
}
