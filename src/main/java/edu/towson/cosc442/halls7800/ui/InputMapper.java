package edu.towson.cosc442.halls7800.ui;

import edu.towson.cosc442.halls7800.engine.CloseDoorCommand;
import edu.towson.cosc442.halls7800.engine.Command;
import edu.towson.cosc442.halls7800.engine.MoveCommand;
import edu.towson.cosc442.halls7800.engine.PickUpCommand;
import edu.towson.cosc442.halls7800.engine.SearchCommand;
import edu.towson.cosc442.halls7800.engine.StairsCommand;
import edu.towson.cosc442.halls7800.engine.WaitCommand;
import edu.towson.cosc442.halls7800.world.Direction;
import javafx.scene.input.KeyCode;

/**
 * Translates keyboard input into game commands.
 */
final class InputMapper {

    /** Help text listing the controls, shown in the side panel. */
    static final String CONTROLS = String.join("\n",
            "Move / attack   arrow keys or W A S D (walk into a monster to attack it)",
            "Diagonals       Q E Z C, or the numeric keypad",
            "Wait / rest     Space",
            "Close a door    X",
            "Search          F   (for secret doors and hidden traps; may take a few tries)",
            "Talk            walk into a character",
            "Stairs          >  down     <  up      (or Page Down / Page Up)",
            "",
            "Pick up         G",
            "Use an item     U, then its letter  (wield, wear, drink, read, eat, zap a wand)",
            "Swap with Doc   walk into Doc",
            "Throw           T, then its letter  (at the nearest monster in view)",
            "Drop            R, then its letter",
            "Quest journal   J",
            "Music / sound   M / N to switch on or off (F2 / F3 on the title screen)",
            "Examine         L, then move the cursor; describes whatever is there (L or Esc to stop)",
            "",
            "In the shop     letter = buy,  Tab = switch to selling,  Esc = leave",
            "Choosing a perk 1, 2 or 3",
            "",
            "Help            ? or F1  (Tab switches between Controls and Symbols)",
            "Close / cancel  Esc              Save and quit to title   Esc");

    private InputMapper() {
    }

    /**
     * Maps a movement key to its direction (used by examine mode to move the cursor).
     *
     * @param code the key
     * @return the direction, or {@code null} if the key is not a movement key
     */
    static Direction directionFor(KeyCode code) {
        return fromKey(code) instanceof MoveCommand move ? move.direction() : null;
    }

    /**
     * Maps a pressed key to a command.
     *
     * @param code the key
     * @return the command, or {@code null} if the key has no action
     */
    static Command fromKey(KeyCode code) {
        return switch (code) {
            case UP, W, NUMPAD8, KP_UP -> new MoveCommand(Direction.NORTH);
            case DOWN, S, NUMPAD2, KP_DOWN -> new MoveCommand(Direction.SOUTH);
            case LEFT, A, NUMPAD4, KP_LEFT -> new MoveCommand(Direction.WEST);
            case RIGHT, D, NUMPAD6, KP_RIGHT -> new MoveCommand(Direction.EAST);
            case Q, NUMPAD7 -> new MoveCommand(Direction.NORTH_WEST);
            case E, NUMPAD9 -> new MoveCommand(Direction.NORTH_EAST);
            case Z, NUMPAD1 -> new MoveCommand(Direction.SOUTH_WEST);
            case C, NUMPAD3 -> new MoveCommand(Direction.SOUTH_EAST);
            case SPACE, NUMPAD5 -> new WaitCommand();
            case X -> CloseDoorCommand.nearest();
            case G -> new PickUpCommand();
            case F -> new SearchCommand();
            case PAGE_DOWN -> StairsCommand.descend();
            case PAGE_UP -> StairsCommand.ascend();
            default -> null;
        };
    }

    /**
     * Maps a typed character to a command. Used for symbols such as {@code >} whose key code
     * differs between keyboard layouts.
     *
     * @param character the typed text
     * @return the command, or {@code null} if the character has no action
     */
    static Command fromTyped(String character) {
        return switch (character) {
            case ">" -> StairsCommand.descend();
            case "<" -> StairsCommand.ascend();
            default -> null;
        };
    }
}
