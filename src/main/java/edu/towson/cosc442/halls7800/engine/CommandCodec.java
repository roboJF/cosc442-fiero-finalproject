package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.progression.Perk;
import edu.towson.cosc442.halls7800.world.Direction;
import java.util.function.Consumer;

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
        Command command = decodeCommand(parts);
        if (command == null) {
            decodeMenuAction(parts);
        }
        return command;
    }

    /**
     * Applies one recorded token to a session: submits it if it is a command, or performs the
     * menu action it names.
     *
     * @param session the game
     * @param token the recorded token
     */
    public static void apply(GameSession session, String token) {
        String[] parts = token.trim().split(":", 2);
        Command command = decodeCommand(parts);
        if (command != null) {
            session.submit(command);
        } else {
            decodeMenuAction(parts).accept(session);
        }
    }

    private static Command decodeCommand(String[] parts) {
        String arg = parts.length == 2 ? parts[1] : "";
        return switch (parts[0]) {
            case "M" -> new MoveCommand(Direction.valueOf(arg));
            case "W" -> withoutArgument(parts, new WaitCommand());
            case "F" -> withoutArgument(parts, new SearchCommand());
            case "G" -> withoutArgument(parts, new PickUpCommand());
            case "S" -> switch (arg) {
                case "D" -> StairsCommand.descend();
                case "U" -> StairsCommand.ascend();
                default -> throw new IllegalArgumentException("Stairs direction must be D or U");
            };
            case "X" -> new CloseDoorCommand("*".equals(arg) ? null : Direction.valueOf(arg));
            case "R" -> new DropCommand(Integer.parseInt(arg));
            case "U" -> new UseItemCommand(Integer.parseInt(arg));
            case "T" -> new ThrowCommand(Integer.parseInt(arg));
            case "BUY", "SELL", "PERK", "SHOP" -> null;
            default -> throw new IllegalArgumentException("Unknown action '" + parts[0] + "'");
        };
    }

    private static Command withoutArgument(String[] parts, Command command) {
        if (parts.length != 1) {
            throw new IllegalArgumentException(parts[0] + " takes no argument");
        }
        return command;
    }

    private static Consumer<GameSession> decodeMenuAction(String[] parts) {
        if (parts.length != 2 || parts[1].isEmpty()) {
            throw new IllegalArgumentException(parts[0] + " requires an argument");
        }
        String arg = parts[1];
        return switch (parts[0]) {
            case "BUY" -> {
                int index = Integer.parseInt(arg);
                yield session -> session.buy(index);
            }
            case "SELL" -> {
                int slot = Integer.parseInt(arg);
                yield session -> session.sell(slot);
            }
            case "PERK" -> {
                Perk perk = Perk.valueOf(arg);
                yield session -> session.choosePerk(perk);
            }
            case "SHOP" -> {
                if (!"CLOSE".equals(arg)) {
                    throw new IllegalArgumentException("Shop action must be CLOSE");
                }
                yield GameSession::closeShop;
            }
            default -> throw new IllegalArgumentException("Unknown menu action '" + parts[0] + "'");
        };
    }
}
