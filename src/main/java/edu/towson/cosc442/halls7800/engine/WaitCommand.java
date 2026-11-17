package edu.towson.cosc442.halls7800.engine;

/**
 * Lets one normal action's worth of time pass without doing anything.
 */
public final class WaitCommand implements Command {

    @Override
    public String encode() {
        return "W";
    }

    @Override
    public CommandResult execute(GameSession session) {
        return CommandResult.acted(session.player().actionCost());
    }
}
