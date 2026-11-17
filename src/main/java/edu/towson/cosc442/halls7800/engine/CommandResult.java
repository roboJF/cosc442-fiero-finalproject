package edu.towson.cosc442.halls7800.engine;

/**
 * The outcome of executing a {@link Command}.
 *
 * @param performed {@code true} if the action happened and used game time
 * @param timeCost ticks the action took (0 when not performed)
 * @param message text for the message log, or {@code null}
 */
public record CommandResult(boolean performed, int timeCost, String message) {

    /**
     * Validates the combination of fields.
     */
    public CommandResult {
        if (performed && timeCost <= 0) {
            throw new IllegalArgumentException("A performed action must cost time");
        }
        if (!performed && timeCost != 0) {
            throw new IllegalArgumentException("A rejected action cannot cost time");
        }
    }

    /**
     * @param cost ticks used
     * @return a successful result with no message
     */
    public static CommandResult acted(int cost) {
        return new CommandResult(true, cost, null);
    }

    /**
     * @param cost ticks used
     * @param message log text
     * @return a successful result with a message
     */
    public static CommandResult acted(int cost, String message) {
        return new CommandResult(true, cost, message);
    }

    /**
     * @param message explanation shown to the player
     * @return a result for an action that could not be done and used no time
     */
    public static CommandResult rejected(String message) {
        return new CommandResult(false, 0, message);
    }
}
