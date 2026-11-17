package edu.towson.cosc442.halls7800.world;

import edu.towson.cosc442.halls7800.util.WeightedTable;

/**
 * The themed regions of the dungeon and the generation parameters for each.
 *
 * <p>Room-type weights are listed in {@link RoomType} order: plain, pillared, flooded, overgrown,
 * collapsed, chasm rift, shrine.
 */
public enum Zone {
    ATRIUM("The Atrium of 7800", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            new int[] {1, 0, 0, 0, 0, 0, 0}),
    UNDERCROFT("The Undercroft", 1, 3, 56, 32, 6, 8, 4, 9, 4, 7, 70, 15, 30, 20,
            new int[] {50, 10, 15, 5, 15, 5, 5}),
    CLOCKWORK_STACKS("The Clockwork Stacks", 4, 6, 64, 36, 7, 10, 5, 11, 4, 8, 80, 25, 40, 25,
            new int[] {40, 25, 5, 5, 10, 10, 5}),
    EMBER_VAULTS("The Ember Vaults", 7, 9, 72, 40, 8, 12, 5, 12, 4, 9, 60, 30, 50, 30,
            new int[] {40, 10, 0, 5, 20, 20, 5}),
    SANCTUM("The Sanctum", 10, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            new int[] {1, 0, 0, 0, 0, 0, 0});

    private static final String[] NUMERALS = {"I", "II", "III", "IV", "V"};

    private final String title;
    private final int firstDepth;
    private final int lastDepth;
    private final int width;
    private final int height;
    private final int minRooms;
    private final int maxRooms;
    private final int minRoomWidth;
    private final int maxRoomWidth;
    private final int minRoomHeight;
    private final int maxRoomHeight;
    private final int doorPercent;
    private final int loopPercent;
    private final int vaultPercent;
    private final int secretPercent;
    private final int[] roomWeights;

    Zone(String title, int firstDepth, int lastDepth, int width, int height, int minRooms, int maxRooms,
         int minRoomWidth, int maxRoomWidth, int minRoomHeight, int maxRoomHeight,
         int doorPercent, int loopPercent, int vaultPercent, int secretPercent, int[] roomWeights) {
        this.title = title;
        this.firstDepth = firstDepth;
        this.lastDepth = lastDepth;
        this.width = width;
        this.height = height;
        this.minRooms = minRooms;
        this.maxRooms = maxRooms;
        this.minRoomWidth = minRoomWidth;
        this.maxRoomWidth = maxRoomWidth;
        this.minRoomHeight = minRoomHeight;
        this.maxRoomHeight = maxRoomHeight;
        this.doorPercent = doorPercent;
        this.loopPercent = loopPercent;
        this.vaultPercent = vaultPercent;
        this.secretPercent = secretPercent;
        this.roomWeights = roomWeights;
    }

    /**
     * @param depth a dungeon depth
     * @return the zone containing that depth
     * @throws IllegalArgumentException if no zone covers the depth
     */
    public static Zone forDepth(int depth) {
        for (Zone z : values()) {
            if (depth >= z.firstDepth && depth <= z.lastDepth) {
                return z;
            }
        }
        throw new IllegalArgumentException("No zone for depth " + depth);
    }

    /**
     * Builds the display name of a floor, e.g. "The Undercroft II".
     *
     * @param depth a depth within this zone
     * @return the floor name
     */
    public String floorName(int depth) {
        if (firstDepth == lastDepth) {
            return title;
        }
        int level = depth - firstDepth;
        return title + " " + (level < NUMERALS.length ? NUMERALS[level] : String.valueOf(level + 1));
    }

    /** @return the zone's title */
    public String title() {
        return title;
    }

    /** @return first depth in the zone */
    public int firstDepth() {
        return firstDepth;
    }

    /** @return last depth in the zone */
    public int lastDepth() {
        return lastDepth;
    }

    /** @return {@code true} if floors in this zone are generated rather than hand-drawn */
    public boolean isGenerated() {
        return width > 0;
    }

    /** @return generated floor width */
    public int width() {
        return width;
    }

    /** @return generated floor height */
    public int height() {
        return height;
    }

    /** @return fewest rooms to aim for */
    public int minRooms() {
        return minRooms;
    }

    /** @return most rooms to aim for */
    public int maxRooms() {
        return maxRooms;
    }

    /** @return smallest interior room width */
    public int minRoomWidth() {
        return minRoomWidth;
    }

    /** @return largest interior room width */
    public int maxRoomWidth() {
        return maxRoomWidth;
    }

    /** @return smallest interior room height */
    public int minRoomHeight() {
        return minRoomHeight;
    }

    /** @return largest interior room height */
    public int maxRoomHeight() {
        return maxRoomHeight;
    }

    /** @return percent chance that a proper doorway gets an actual door */
    public int doorPercent() {
        return doorPercent;
    }

    /** @return percent chance of adding an extra corridor that forms a loop */
    public int loopPercent() {
        return loopPercent;
    }

    /** @return percent chance that a floor gets a locked vault */
    public int vaultPercent() {
        return vaultPercent;
    }

    /** @return percent chance that a floor gets a hidden room */
    public int secretPercent() {
        return secretPercent;
    }

    /** @return a new table of room decorations weighted for this zone */
    public WeightedTable<RoomType> roomTypes() {
        WeightedTable<RoomType> table = new WeightedTable<>();
        RoomType[] types = RoomType.values();
        for (int i = 0; i < types.length; i++) {
            table.add(types[i], roomWeights[i]);
        }
        return table;
    }
}
