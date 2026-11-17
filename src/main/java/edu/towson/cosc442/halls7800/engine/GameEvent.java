package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.world.Position;

/**
 * Something noteworthy that happened during a turn. The user interface listens for events to
 * refresh the display, show messages and play sounds; the game rules never depend on who is
 * listening, and publishing an event never changes the game state.
 *
 * @param type what kind of event this is
 * @param message human-readable text, or {@code null} if the event is silent
 * @param position where it happened, or {@code null} if not location-specific
 * @param depth the floor depth at the time of the event
 */
public record GameEvent(Type type, String message, Position position, int depth) {

    /** Kinds of game events. */
    public enum Type {
        /** Plain informational text for the message log. */
        MESSAGE,
        /** The player moved to a new tile. */
        PLAYER_MOVED,
        /** A door was opened or closed. */
        DOOR_CHANGED,
        /** The player arrived on a different floor. */
        FLOOR_CHANGED,
        /** The player finished a turn and all other actors have acted. */
        TURN_ENDED,
        /** The run ended in death or victory. */
        GAME_OVER,
        /** The player or companion hit a monster. */
        ATTACK_HIT,
        /** An attack involving the player or companion missed. */
        ATTACK_MISSED,
        /** A monster hit the player. */
        PLAYER_HURT,
        /** A monster was defeated. */
        CREATURE_DIED,
        /** A boss was defeated. */
        BOSS_DEFEATED,
        /** A boss noticed the player. */
        BOSS_AWAKENED,
        /** The player picked up an item. */
        ITEM_PICKED_UP,
        /** The player picked up gold. */
        GOLD_PICKED_UP,
        /** The player drank a potion. */
        POTION_DRUNK,
        /** The player read a scroll. */
        SCROLL_READ,
        /** The player ate food. */
        FOOD_EATEN,
        /** The player equipped or removed equipment. */
        ITEM_EQUIPPED,
        /** The player zapped a wand. */
        WAND_ZAPPED,
        /** The player set off a trap. */
        TRAP_TRIGGERED,
        /** The player gained a level. */
        LEVEL_UP,
        /** The player talked to a character; the message holds the character's name. */
        TALKED,
        /** The player earned an achievement. */
        ACHIEVEMENT
    }
}
