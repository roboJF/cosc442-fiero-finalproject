package edu.towson.cosc442.halls7800.util;

/**
 * Independent purposes for randomness. Each purpose gets its own generator, so that (for
 * example) a change to combat rolls does not change dungeon layouts for the same seed.
 */
public enum RandomStream {
    /** Floor shapes: rooms, corridors, doors, decoration, stairs. */
    LAYOUT,
    /** What is placed on a new floor: monsters, items, traps. */
    POPULATION,
    /** Loot dropped by monsters and found in containers. */
    LOOT,
    /** Attack and damage rolls. */
    COMBAT,
    /** Monster decisions. */
    AI,
    /** Status effects and other per-turn chances. */
    EFFECTS,
    /** Appearance of unidentified items. */
    IDENTIFY,
    /** Where traps are placed on a new floor. */
    TRAPS,
    /** The Atrium shop's stock. */
    SHOP
}
