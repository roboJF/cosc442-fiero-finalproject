package edu.towson.cosc442.halls7800.entity;

/**
 * Special behaviors a monster type can have.
 */
public enum MonsterTrait {
    /** Often moves in a random direction instead of the chosen one. */
    ERRATIC,
    /** Runs away when badly hurt. */
    FLEES,
    /** Slowly regains HP. */
    REGENERATES,
    /** Cannot open doors. */
    NO_DOORS,
    /** Attacks from a distance when it has a clear line. */
    RANGED,
    /** Appears in groups. */
    PACK,
    /** Lies dormant until something comes right next to it. */
    AMBUSH,
    /** Takes no fire damage and cannot burn. */
    FIRE_IMMUNE,
    /** Takes no poison damage and cannot be poisoned. */
    POISON_IMMUNE,
    /** A unique guardian: always wakes on sight, has phases, and seals the stairs down. */
    BOSS,
    /** Never moves or attacks (training dummies). */
    STATIONARY,
    /** Divides in two when wounded. */
    SPLITS,
    /** Steals gold from the player and flees. */
    THIEF
}
