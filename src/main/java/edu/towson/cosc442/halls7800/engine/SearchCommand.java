package edu.towson.cosc442.halls7800.engine;

/**
 * Spends a turn carefully searching the surrounding tiles for secret doors and hidden traps.
 * Each search has only a chance to find each hidden thing, so it may take several tries.
 */
public final class SearchCommand implements Command {

    @Override
    public String encode() {
        return "F";
    }

    @Override
    public CommandResult execute(GameSession session) {
        int found = Traps.search(session);
        return CommandResult.acted(session.player().actionCost(),
                found == 0 ? "You search the area but find nothing." : null);
    }
}
