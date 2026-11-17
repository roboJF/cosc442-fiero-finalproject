package edu.towson.cosc442.halls7800.world;

/**
 * How a room's interior is decorated during generation.
 */
public enum RoomType {
    /** Bare floor. */
    PLAIN,
    /** A grid of stone pillars. */
    PILLARED,
    /** A pool of shallow water that slows movement. */
    FLOODED,
    /** Tall grass that blocks line of sight. */
    OVERGROWN,
    /** Scattered rubble from a partial collapse. */
    COLLAPSED,
    /** A chasm splits the room, crossed by a single bridge. */
    CHASM_RIFT,
    /** A small shrine with an altar. At most one per floor. */
    SHRINE
}
