package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.progression.Perk;
import edu.towson.cosc442.halls7800.world.Direction;

/**
 * Converts recorded tokens back into actions. A run recording is a list of tokens, one per player
 * action, in the order they happened.
 *
 * <p>Timed commands use the token from {@link Command#encode()}. Instant menu actions, which
 * are not commands, use these tokens:
 * <ul>
 *   <li>{@code BUY:n} / {@code SELL:n} — shop purchase or sale</li>
 *   <li>{@code PERK:NAME} — perk choice</li>
 *   <li>{@code SHOP:CLOSE} — leaving the shop screen</li>
 * </ul>
 */
public final class CommandCodec {

    private CommandCodec() {
    }

    /**
     * Decodes a timed-command token.
     *
     * @param token a token such as "M:NORTH", "W" or "U:3"
     * @return the command, or {@code null} if the token is a menu action instead
     * @throws IllegalArgumentException if the token is malformed
     */
    public static Command decode(String token) {
        String[] parts = token.trim().split(":", 2);
        String code = parts[0];
        String arg = parts.length > 1 ? parts[1] : null;
        try {
            return switch (code) {
                case "M" -> new MoveCommand(Direction.valueOf(arg));
                case "W" -> new WaitCommand();
                case "F" -> new SearchCommand();
                case "G" -> new PickUpCommand();
                case "S" -> "D".equals(arg) ? StairsCommand.descend() : StairsCommand.ascend();
                case "X" -> new CloseDoorCommand("*".equals(arg) ? null : Direction.valueOf(arg));
                case "R" -> new DropCommand(Integer.parseInt(arg));
                case "U" -> new UseItemCommand(Integer.parseInt(arg));
                case "T" -> new ThrowCommand(Integer.parseInt(arg));
                case "BUY", "SELL", "PERK", "SHOP" -> null;
                default -> throw new IllegalArgumentException("Unknown token '" + token + "'");
            };
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Malformed token '" + token + "'", e);
        }
    }

    /**
     * Applies one recorded token to a session: submits it if it is a command, or performs the
     * menu action it names.
     *
     * @param session the game
     * @param token the recorded token
     */
    public static void apply(GameSession session, String token) {
        Command command = decode(token);
        if (command != null) {
            session.submit(command);
            return;
        }
        String[] parts = token.trim().split(":", 2);
        switch (parts[0]) {
            case "BUY" -> session.buy(Integer.parseInt(parts[1]));
            case "SELL" -> session.sell(Integer.parseInt(parts[1]));
            case "PERK" -> session.choosePerk(Perk.valueOf(parts[1]));
            case "SHOP" -> session.closeShop();
            default -> throw new IllegalArgumentException("Unknown token '" + token + "'");
        }
    }
}
