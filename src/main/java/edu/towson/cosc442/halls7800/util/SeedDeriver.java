package edu.towson.cosc442.halls7800.util;

/**
 * Derives independent seeds from a single run seed.
 *
 * <p>A seed is derived from (run seed, depth, stream). Floor-specific streams such as
 * {@link RandomStream#LAYOUT} use the floor's depth, so floor 5 looks the same no matter what the
 * player did on floors 1 to 4. Run-wide streams such as {@link RandomStream#COMBAT} use
 * {@link #RUN_WIDE} as the depth.
 */
public final class SeedDeriver {

    /** Depth value used for streams that span the whole run rather than one floor. */
    public static final int RUN_WIDE = -1;

    private SeedDeriver() {
    }

    /**
     * @param runSeed the seed of the whole run
     * @param depth floor depth, or {@link #RUN_WIDE}
     * @param stream purpose of the randomness
     * @return a derived seed
     */
    public static long derive(long runSeed, int depth, RandomStream stream) {
        long salt = SplitMixRng.mix((depth + 2L) * 0x9E3779B97F4A7C15L + (stream.ordinal() + 1L) * 0xC2B2AE3D27D4EB4FL);
        return SplitMixRng.mix(runSeed ^ salt);
    }

    /**
     * @param runSeed the seed of the whole run
     * @param depth floor depth, or {@link #RUN_WIDE}
     * @param stream purpose of the randomness
     * @return a new generator seeded for that purpose
     */
    public static SplitMixRng rngFor(long runSeed, int depth, RandomStream stream) {
        return new SplitMixRng(derive(runSeed, depth, stream));
    }
}
