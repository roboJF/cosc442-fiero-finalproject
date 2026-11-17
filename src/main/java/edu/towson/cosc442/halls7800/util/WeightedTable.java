package edu.towson.cosc442.halls7800.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A list of choices with relative weights, for picking room types, monsters, loot, and so on.
 * An entry with weight 3 is picked three times as often as one with weight 1.
 *
 * @param <T> the type of choice
 */
public class WeightedTable<T> {

    private final List<T> items = new ArrayList<>();
    private final List<Integer> weights = new ArrayList<>();
    private int total;

    /**
     * Adds a choice. Entries with weight 0 are ignored.
     *
     * @param item the choice
     * @param weight relative weight (0 or more)
     * @return this table, for chaining
     */
    public WeightedTable<T> add(T item, int weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("weight must be non-negative: " + weight);
        }
        if (weight > 0) {
            items.add(item);
            weights.add(weight);
            total += weight;
        }
        return this;
    }

    /**
     * Picks a choice at random, in proportion to the weights.
     *
     * @param rng randomness source
     * @return the chosen item
     * @throws IllegalStateException if the table is empty
     */
    public T pick(Rng rng) {
        if (total == 0) {
            throw new IllegalStateException("Cannot pick from an empty table");
        }
        int roll = rng.nextInt(total);
        for (int i = 0; i < items.size(); i++) {
            roll -= weights.get(i);
            if (roll < 0) {
                return items.get(i);
            }
        }
        throw new AssertionError("unreachable");
    }

    /**
     * Returns a copy of this table without the given item.
     *
     * @param excluded the item to leave out
     * @return a new table
     */
    public WeightedTable<T> without(T excluded) {
        WeightedTable<T> copy = new WeightedTable<>();
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).equals(excluded)) {
                copy.add(items.get(i), weights.get(i));
            }
        }
        return copy;
    }

    /**
     * @param item a choice
     * @return its weight, or 0 if absent
     */
    public int weightOf(T item) {
        int index = items.indexOf(item);
        return index < 0 ? 0 : weights.get(index);
    }

    /** @return sum of all weights */
    public int totalWeight() {
        return total;
    }

    /** @return {@code true} if there is nothing to pick */
    public boolean isEmpty() {
        return total == 0;
    }

    /** @return the choices, in insertion order */
    public List<T> items() {
        return Collections.unmodifiableList(items);
    }
}
