package edu.towson.cosc442.halls7800.world;

/**
 * Builds a {@link Floor} from a hand-written text layout, one string per row.
 *
 * <p>Each character is a {@link Terrain#symbol()}, plus {@code @}, which marks the arrival point
 * (stored as floor). If there is no {@code @}, the up staircase is used as the entrance. The first
 * down staircase becomes the floor's exit. All rows must have the same length.
 */
public final class FloorParser {

    /** Layout character for the arrival point. */
    public static final char START = '@';

    private FloorParser() {
    }

    /**
     * Parses a layout.
     *
     * @param name floor name
     * @param depth floor depth
     * @param rows the layout rows
     * @return the parsed floor
     * @throws IllegalArgumentException if the layout is ragged, empty, uses an unknown symbol,
     *                                  or has no way to determine an entrance
     */
    public static Floor parse(String name, int depth, String... rows) {
        if (rows == null || rows.length == 0) {
            throw new IllegalArgumentException("Layout has no rows");
        }
        int width = rows[0].length();
        Floor floor = new Floor(width, rows.length, depth, name);
        Position start = null;
        Position upStairs = null;
        Position downStairs = null;
        for (int y = 0; y < rows.length; y++) {
            String row = rows[y];
            if (row.length() != width) {
                throw new IllegalArgumentException("Row " + y + " has length " + row.length() + ", expected " + width);
            }
            for (int x = 0; x < width; x++) {
                char c = row.charAt(x);
                Position p = new Position(x, y);
                if (c == START) {
                    start = p;
                    floor.setTerrain(p, Terrain.FLOOR);
                    continue;
                }
                Terrain t = Terrain.fromSymbol(c);
                floor.setTerrain(p, t);
                if (t == Terrain.STAIRS_UP && upStairs == null) {
                    upStairs = p;
                } else if (t == Terrain.STAIRS_DOWN && downStairs == null) {
                    downStairs = p;
                }
            }
        }
        Position entrance = start != null ? start : upStairs;
        if (entrance == null) {
            throw new IllegalArgumentException("Layout '" + name + "' needs an '@' or '<' entrance");
        }
        floor.setEntrance(entrance);
        floor.setExit(downStairs);
        return floor;
    }
}
