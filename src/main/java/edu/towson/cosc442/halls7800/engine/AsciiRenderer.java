package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.world.FloorView;
import edu.towson.cosc442.halls7800.world.Position;
import java.util.Map;

/**
 * Renders a floor as text. Useful for debugging and logging.
 */
public final class AsciiRenderer {

    /** Character drawn for the player. */
    public static final char PLAYER = '@';

    /** Character drawn for tiles the player has never seen. */
    public static final char UNKNOWN = ' ';

    private AsciiRenderer() {
    }

    /**
     * Renders a floor with the player marked.
     *
     * @param floor the floor
     * @param player the player's position, or {@code null} to omit
     * @param fogOfWar if {@code true}, unexplored tiles are blank
     * @return one line per row, separated by {@code '\n'}
     */
    public static String render(FloorView floor, Position player, boolean fogOfWar) {
        return render(floor, player, fogOfWar, Map.of());
    }

    /**
     * Renders a floor with the player and additional markers (creatures, items).
     *
     * @param floor the floor
     * @param player the player's position, or {@code null} to omit
     * @param fogOfWar if {@code true}, unexplored tiles are blank
     * @param overlays characters to draw on top of the terrain; the player is drawn last
     * @return one line per row, separated by {@code '\n'}
     */
    public static String render(FloorView floor, Position player, boolean fogOfWar,
                                Map<Position, Character> overlays) {
        StringBuilder sb = new StringBuilder((floor.width() + 1) * floor.height());
        for (int y = 0; y < floor.height(); y++) {
            for (int x = 0; x < floor.width(); x++) {
                Position p = new Position(x, y);
                char c;
                if (p.equals(player)) {
                    c = PLAYER;
                } else if (fogOfWar && !floor.isExplored(p)) {
                    c = UNKNOWN;
                } else if (overlays.containsKey(p) && (!fogOfWar || floor.isVisible(p))) {
                    c = overlays.get(p);
                } else {
                    c = floor.terrainAt(p).displaySymbol();
                }
                sb.append(c);
            }
            if (y < floor.height() - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
