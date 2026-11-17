package edu.towson.cosc442.halls7800.gen;

import edu.towson.cosc442.halls7800.engine.FloorSource;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.FloorParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A {@link FloorSource} that serves a fixed list of hand-drawn layouts, one per depth starting at
 * depth 0. Useful for small, predictable hand-made floors.
 */
public class FixedFloorSource implements FloorSource {

    private final List<String> names = new ArrayList<>();
    private final List<String[]> layouts = new ArrayList<>();

    /**
     * Adds the next depth's layout.
     *
     * @param name floor name
     * @param layout layout rows
     * @return this source, for chaining
     */
    public FixedFloorSource add(String name, String... layout) {
        names.add(Objects.requireNonNull(name, "name"));
        layouts.add(layout.clone());
        return this;
    }

    @Override
    public Floor createFloor(int depth) {
        if (depth < 0 || depth >= layouts.size()) {
            throw new IllegalArgumentException("No layout for depth " + depth);
        }
        return FloorParser.parse(names.get(depth), depth, layouts.get(depth));
    }

    @Override
    public int deepestDepth() {
        return layouts.size() - 1;
    }
}
