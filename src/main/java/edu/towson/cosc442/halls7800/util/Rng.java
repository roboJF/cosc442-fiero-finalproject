package edu.towson.cosc442.halls7800.util;

import java.util.List;

/**
 * Source of randomness for all game rules.
 *
 * <p>Game code must never call {@code Math.random()} or create {@code java.util.Random}; it asks
 * for an {@code Rng} instead. Production code uses {@link SplitMixRng} seeded from the run seed,
 * and other implementations can be supplied where specific values are needed.
 *
 * <p>Only {@link #nextLong()}, {@link #nextInt(int)} and {@link #nextDouble()} must be implemented;
 * the other methods are built on them.
 */
public interface Rng {

    /** @return a uniformly distributed 64-bit value */
    long nextLong();

    /**
     * @param bound exclusive upper bound; must be positive
     * @return a uniformly distributed value in {@code [0, bound)}
     */
    int nextInt(int bound);

    /** @return a uniformly distributed value in {@code [0.0, 1.0)} */
    double nextDouble();

    /**
     * @param min inclusive lower bound
     * @param maxInclusive inclusive upper bound; must be at least {@code min}
     * @return a uniformly distributed value in {@code [min, maxInclusive]}
     */
    default int nextInt(int min, int maxInclusive) {
        if (maxInclusive < min) {
            throw new IllegalArgumentException("max " + maxInclusive + " < min " + min);
        }
        return min + nextInt(maxInclusive - min + 1);
    }

    /**
     * @param probability chance of returning {@code true}, from 0.0 to 1.0
     * @return {@code true} with the given probability
     */
    default boolean chance(double probability) {
        if (probability <= 0) {
            return false;
        }
        if (probability >= 1) {
            return true;
        }
        return nextDouble() < probability;
    }

    /**
     * @param percent chance of returning {@code true}, from 0 to 100
     * @return {@code true} with the given percentage chance
     */
    default boolean percent(int percent) {
        return nextInt(100) < percent;
    }

    /**
     * @param items a non-empty list
     * @param <T> element type
     * @return a uniformly chosen element
     */
    default <T> T pick(List<T> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Cannot pick from an empty list");
        }
        return items.get(nextInt(items.size()));
    }

    /**
     * Shuffles a list in place (Fisher-Yates).
     *
     * @param items the list to shuffle
     * @param <T> element type
     */
    default <T> void shuffle(List<T> items) {
        for (int i = items.size() - 1; i > 0; i--) {
            int j = nextInt(i);
            T tmp = items.get(i);
            items.set(i, items.get(j));
            items.set(j, tmp);
        }
    }
}
