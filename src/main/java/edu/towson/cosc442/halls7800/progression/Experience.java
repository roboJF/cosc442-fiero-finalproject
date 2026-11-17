package edu.towson.cosc442.halls7800.progression;

/**
 * The experience curve and level-up rewards.
 *
 * <p>Each level grants +{@value #HP_PER_LEVEL} maximum HP and +1 accuracy; every even level
 * also grants +1 evasion. At every level divisible by {@value #PERK_INTERVAL} the player chooses a
 * {@link Perk}.
 */
public final class Experience {

    /** Highest attainable level. */
    public static final int MAX_LEVEL = 15;
    /** Maximum HP gained per level. */
    public static final int HP_PER_LEVEL = 5;
    /** A perk is offered at every level that is a multiple of this. */
    public static final int PERK_INTERVAL = 3;
    /** Number of perks offered at once. */
    public static final int PERK_CHOICES = 3;

    /** Total experience needed to reach each level; index 1 is level 1. */
    private static final int[] THRESHOLDS = {
        0, 0, 8, 20, 36, 58, 88, 126, 174, 234, 308, 398, 506, 636, 792, 976
    };

    private Experience() {
    }

    /**
     * @param level a level from 1 to {@link #MAX_LEVEL}
     * @return total experience required to reach it
     */
    public static int thresholdFor(int level) {
        if (level < 1 || level > MAX_LEVEL) {
            throw new IllegalArgumentException("level must be 1.." + MAX_LEVEL);
        }
        return THRESHOLDS[level];
    }

    /**
     * @param experience total experience
     * @return the level that much experience earns
     */
    public static int levelFor(int experience) {
        int level = 1;
        while (level < MAX_LEVEL && experience >= THRESHOLDS[level + 1]) {
            level++;
        }
        return level;
    }

    /**
     * @param level the level just reached
     * @return {@code true} if reaching it grants a perk choice
     */
    public static boolean grantsPerk(int level) {
        return level % PERK_INTERVAL == 0;
    }

    /**
     * @param level the level just reached
     * @return {@code true} if reaching it grants +1 evasion
     */
    public static boolean grantsEvasion(int level) {
        return level % 2 == 0;
    }
}
