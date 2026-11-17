package edu.towson.cosc442.halls7800.util;

/**
 * Deterministic random number generator based on the SplitMix64 algorithm.
 *
 * <p>The whole generator state is a single {@code long}, which can be read with {@link #state()}
 * and restored with {@link #setState(long)}. Saving a game therefore preserves exactly where every
 * random stream was, and the same seed always produces the same sequence on every platform.
 */
public class SplitMixRng implements Rng {

    private static final long GOLDEN_GAMMA = 0x9E3779B97F4A7C15L;

    private long state;

    /**
     * @param seed initial state
     */
    public SplitMixRng(long seed) {
        this.state = seed;
    }

    @Override
    public long nextLong() {
        state += GOLDEN_GAMMA;
        return mix(state);
    }

    @Override
    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive: " + bound);
        }
        // Rejection sampling avoids modulo bias.
        long bits;
        long value;
        do {
            bits = nextLong() >>> 1;
            value = bits % bound;
        } while (bits - value + (bound - 1) < 0);
        return (int) value;
    }

    @Override
    public double nextDouble() {
        return (nextLong() >>> 11) * 0x1.0p-53;
    }

    /** @return the current internal state, for saving */
    public long state() {
        return state;
    }

    /**
     * Restores a state previously returned by {@link #state()}.
     *
     * @param state the state to restore
     */
    public void setState(long state) {
        this.state = state;
    }

    /**
     * The SplitMix64 finalizer: scrambles the bits of a value. Also used to derive seeds.
     *
     * @param z input value
     * @return well-mixed output
     */
    static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}
