package edu.towson.cosc442.halls7800.engine;

/**
 * A single player action. The user interface translates input into commands and submits them
 * with {@link GameSession#submit(Command)}.
 */
public interface Command {

    /**
     * Attempts the action. Implementations must not advance time themselves; the session does
     * that when the result says a turn was consumed.
     *
     * @param session the running game
     * @return what happened
     */
    CommandResult execute(GameSession session);

    /**
     * Encodes this command as a short token for run recordings (see {@link CommandCodec}).
     * Decoding the token must produce an equivalent command.
     *
     * @return the token, e.g. "M:NORTH"
     */
    String encode();
}
