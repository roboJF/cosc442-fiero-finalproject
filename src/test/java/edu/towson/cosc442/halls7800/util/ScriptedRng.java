package edu.towson.cosc442.halls7800.util;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Test double for {@link Rng} that returns pre-arranged values in order. Fails loudly if the code
 * under test asks for more values than were scripted, or for a value outside the requested bound.
 */
public class ScriptedRng implements Rng {

    private final Deque<Integer> ints = new ArrayDeque<>();

    /**
     * @param values the values {@link #nextInt(int)} will return, in order
     */
    public ScriptedRng(int... values) {
        for (int v : values) {
            ints.add(v);
        }
    }

    @Override
    public int nextInt(int bound) {
        if (ints.isEmpty()) {
            throw new IllegalStateException("ScriptedRng ran out of values");
        }
        int v = ints.poll();
        if (v < 0 || v >= bound) {
            throw new IllegalStateException("Scripted value " + v + " outside bound " + bound);
        }
        return v;
    }

    @Override
    public long nextLong() {
        return nextInt(Integer.MAX_VALUE);
    }

    @Override
    public double nextDouble() {
        return nextInt(1000) / 1000.0;
    }

    /** @return how many scripted values have not been used */
    public int remaining() {
        return ints.size();
    }
}
