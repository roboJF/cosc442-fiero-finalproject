package edu.towson.cosc442.halls7800.quest;

import java.util.Objects;

/**
 * One quest and its progress.
 *
 * <pre>
 *   NOT_STARTED --start--> ACTIVE --goal reached--> READY --turned in--> COMPLETED
 *                            ^                        |
 *                            +---progress lost--------+
 * </pre>
 */
public final class Quest {

    /** The quests in the game. */
    public enum Id {
        DIPLOMA("The Diploma of Ascendance", "Archivist Dehlinger",
                "Recover the Diploma from the Sanctum at the bottom of the Halls and carry it out of the Atrium.", 1),
        LOST_REQUIREMENTS("Lost Requirements", "Archivist Dehlinger",
                "Find the three torn requirements pages scattered through the Undercroft.", 3),
        MUTATION_TRIAL("The Mutation Trial", "Warden Deng",
                "Kill every mutant the Warden releases.", 3),
        CARTOGRAPHY("A Survey of the Halls", "Loremaster Chakraborty",
                "Explore most of three different floors below the Atrium.", 3),
        SECURE_VAULTS("Secure by Design", "Locksmith Taylor",
                "Unlock three vaults in the Halls with their keys.", 3),
        TRANSLATION("Lost in Translation", "Scribe Irani",
                "Learn to read five different kinds of scroll.", 5);

        private final String title;
        private final String giver;
        private final String description;
        private final int goal;

        Id(String title, String giver, String description, int goal) {
            this.title = title;
            this.giver = giver;
            this.description = description;
            this.goal = goal;
        }

        /** @return quest title */
        public String title() {
            return title;
        }

        /** @return who gives the quest */
        public String giver() {
            return giver;
        }

        /** @return what to do */
        public String description() {
            return description;
        }

        /** @return progress needed to finish */
        public int goal() {
            return goal;
        }
    }

    /** Lifecycle of a quest. */
    public enum State {
        NOT_STARTED, ACTIVE, READY, COMPLETED
    }

    private final Id id;
    private State state = State.NOT_STARTED;
    private int progress;

    /**
     * @param id which quest
     */
    public Quest(Id id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    /** @return which quest */
    public Id id() {
        return id;
    }

    /** @return current state */
    public State state() {
        return state;
    }

    /** @return current progress toward the goal */
    public int progress() {
        return progress;
    }

    /** Starts the quest if it has not been started. */
    public void start() {
        if (state == State.NOT_STARTED) {
            state = State.ACTIVE;
        }
    }

    /**
     * Records progress. An active quest becomes ready when the goal is reached; a ready quest
     * goes back to active if progress drops (e.g. a quest item was dropped).
     *
     * @param value new progress (clamped to 0..goal)
     */
    public void setProgress(int value) {
        if (state != State.ACTIVE && state != State.READY) {
            return;
        }
        progress = Math.max(0, Math.min(value, id.goal()));
        state = progress >= id.goal() ? State.READY : State.ACTIVE;
    }

    /**
     * Marks the quest as finished and rewarded.
     *
     * @throws IllegalStateException if it is not ready
     */
    public void complete() {
        if (state != State.READY) {
            throw new IllegalStateException(id + " is not ready to complete (" + state + ")");
        }
        state = State.COMPLETED;
    }

    /** @return {@code true} once started and not yet completed */
    public boolean isOpen() {
        return state == State.ACTIVE || state == State.READY;
    }
}
