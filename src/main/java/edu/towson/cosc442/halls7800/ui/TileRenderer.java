package edu.towson.cosc442.halls7800.ui;

import edu.towson.cosc442.halls7800.entity.Companion;
import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.entity.Npc;
import edu.towson.cosc442.halls7800.item.ItemCategory;
import edu.towson.cosc442.halls7800.world.Terrain;
import edu.towson.cosc442.halls7800.world.Trap;
import edu.towson.cosc442.halls7800.world.Zone;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Draws the map with simple vector shapes, giving a retro look without image assets. Tiles get
 * small, position-based variations so floors and walls do not look like a flat grid; walls with
 * open floor below them are drawn as brick faces for a slight 3D effect. Lighting is applied
 * afterwards with {@link #shade}.
 */
final class TileRenderer {

    private static final Color BACKGROUND = Color.web("#07070c");
    private static final Color WOOD = Color.web("#8a5a2b");
    private static final Color WOOD_DARK = Color.web("#5e3b1a");
    private static final Color WOOD_LINE = Color.web("#3e2610");
    private static final Color BRASS = Color.web("#e0b84a");
    private static final Color STAIRS = Color.web("#d8d0b8");
    private static final Color WATER = Color.web("#2a5a8c");
    private static final Color WATER_WAVE = Color.web("#6fa4d8");
    private static final Color GRASS = Color.web("#2a5f30");
    private static final Color GRASS_BLADE = Color.web("#6cc463");
    private static final Color STONE = Color.web("#8d8a99");
    private static final Color STONE_DARK = Color.web("#5a5866");
    private static final Color TU_GOLD = Color.web("#f2c14e");
    private static final Color TU_BLACK = Color.web("#1a1a1f");

    // Floor and wall colors change with the zone; see usePalette.
    private static Color floorColor = Color.web("#2b2a33");
    private static Color floorDot = Color.web("#3d3b48");
    private static Color wallColor = Color.web("#5b4f72");
    private static Color wallEdge = Color.web("#7a6c96");

    private TileRenderer() {
    }

    /**
     * Switches floor and wall colors to match a dungeon zone.
     *
     * @param zone the zone being drawn
     */
    static void usePalette(Zone zone) {
        String[] c = switch (zone) {
            case ATRIUM -> new String[] {"#2e2c38", "#423f50", "#5b4f72", "#8a7ca8"};
            case UNDERCROFT -> new String[] {"#2c2a25", "#3f3a32", "#5e5547", "#857a66"};
            case CLOCKWORK_STACKS -> new String[] {"#23292c", "#333c40", "#3f6064", "#b08d4a"};
            case EMBER_VAULTS -> new String[] {"#2e2220", "#43312c", "#6e3b2c", "#c0643a"};
            case SANCTUM -> new String[] {"#20233a", "#2e3350", "#3b4170", "#d9c27a"};
        };
        floorColor = Color.web(c[0]);
        floorDot = Color.web(c[1]);
        wallColor = Color.web(c[2]);
        wallEdge = Color.web(c[3]);
    }

    static void clear(GraphicsContext g, double width, double height) {
        g.setFill(BACKGROUND);
        g.fillRect(0, 0, width, height);
    }

    /**
     * A stable pseudo-random number for a tile, so decorations stay put between frames.
     *
     * @param x tile column
     * @param y tile row
     * @return a non-negative hash
     */
    static int hash(int x, int y) {
        int h = x * 73856093 ^ y * 19349663;
        h ^= h >>> 13;
        h *= 0x5bd1e995;
        return (h ^ (h >>> 15)) & 0x7fffffff;
    }

    /**
     * Draws one terrain tile at full brightness.
     *
     * @param g graphics context
     * @param terrain the terrain
     * @param x left pixel
     * @param y top pixel
     * @param s tile size in pixels
     * @param seed {@link #hash} of the tile, for variation
     * @param wallFace for walls: {@code true} if open floor lies below, so the brick face shows
     */
    static void drawTerrain(GraphicsContext g, Terrain terrain, double x, double y, double s, int seed,
                            boolean wallFace) {
        switch (terrain.appearance()) {
            case WALL -> drawWall(g, x, y, s, seed, wallFace);
            case DOOR_CLOSED -> {
                floor(g, x, y, s, seed);
                planks(g, x + s * 0.1, y + s * 0.04, s * 0.8, s * 0.92, WOOD);
                g.setFill(BRASS);
                g.fillOval(x + s * 0.64, y + s * 0.48, s * 0.1, s * 0.1);
            }
            case DOOR_OPEN -> {
                floor(g, x, y, s, seed);
                fill(g, WOOD_DARK, x, y, s * 0.13, s);
                fill(g, WOOD_DARK, x + s * 0.87, y, s * 0.13, s);
                fill(g, WOOD_LINE, x, y, s, s * 0.08);
            }
            case DOOR_LOCKED -> {
                floor(g, x, y, s, seed);
                planks(g, x + s * 0.1, y + s * 0.04, s * 0.8, s * 0.92, WOOD_DARK);
                fill(g, BRASS, x + s * 0.36, y + s * 0.38, s * 0.28, s * 0.3);
                g.setFill(TU_BLACK);
                g.fillOval(x + s * 0.46, y + s * 0.44, s * 0.08, s * 0.08);
                g.fillRect(x + s * 0.485, y + s * 0.5, s * 0.03, s * 0.1);
            }
            case STAIRS_DOWN -> {
                floor(g, x, y, s, seed);
                fill(g, BACKGROUND, x + s * 0.1, y + s * 0.1, s * 0.8, s * 0.8);
                for (int i = 0; i < 4; i++) {
                    double inset = s * (0.1 + i * 0.07);
                    fill(g, STAIRS.deriveColor(0, 1, 1 - i * 0.2, 1), x + inset, y + s * (0.12 + i * 0.19),
                            s - 2 * inset, s * 0.13);
                }
            }
            case STAIRS_UP -> {
                floor(g, x, y, s, seed);
                for (int i = 0; i < 4; i++) {
                    double inset = s * (0.31 - i * 0.07);
                    fill(g, STAIRS.deriveColor(0, 1, 0.55 + i * 0.15, 1), x + inset, y + s * (0.12 + i * 0.19),
                            s - 2 * inset, s * 0.13);
                }
                g.setFill(TU_GOLD);
                g.fillPolygon(new double[] {x + s * 0.5, x + s * 0.4, x + s * 0.6},
                        new double[] {y + s * 0.02, y + s * 0.1, y + s * 0.1}, 3);
            }
            case WATER -> {
                fill(g, WATER.deriveColor(0, 1, 0.92 + (seed % 9) / 60.0, 1), x, y, s, s);
                g.setStroke(WATER_WAVE);
                g.setLineWidth(Math.max(1, s / 16));
                double shift = (seed % 5) * s * 0.06;
                g.strokeArc(x + s * 0.08 + shift, y + s * 0.22, s * 0.36, s * 0.2, 0, 180, ArcType.OPEN);
                g.strokeArc(x + s * 0.5 - shift, y + s * 0.6, s * 0.36, s * 0.2, 0, 180, ArcType.OPEN);
            }
            case CHASM -> {
                fill(g, Color.BLACK, x, y, s, s);
                g.setFill(STONE_DARK);
                for (int i = 0; i < 3; i++) {
                    double px = x + ((seed >> (i * 3)) % 7) / 7.0 * s * 0.8;
                    g.fillRect(px, y + (i % 2) * s * 0.9, s * 0.12, s * 0.06);
                }
            }
            case TALL_GRASS -> {
                fill(g, GRASS, x, y, s, s);
                g.setStroke(GRASS_BLADE);
                g.setLineWidth(Math.max(1, s / 14));
                for (int i = 0; i < 5; i++) {
                    double bx = x + s * (0.12 + i * 0.18) + ((seed >> i) % 3) * s * 0.02;
                    double lean = (((seed >> (i + 2)) % 3) - 1) * s * 0.08;
                    g.strokeLine(bx, y + s * 0.92, bx + lean, y + s * (0.2 + ((seed >> i) % 4) * 0.05));
                }
            }
            case RUBBLE -> {
                floor(g, x, y, s, seed);
                for (int i = 0; i < 3; i++) {
                    double rx = x + s * (0.1 + ((seed >> (i * 4)) % 6) * 0.12);
                    double ry = y + s * (0.2 + ((seed >> (i * 5 + 1)) % 5) * 0.13);
                    double rw = s * (0.18 + (i % 2) * 0.08);
                    g.setFill(STONE_DARK);
                    g.fillOval(rx, ry + s * 0.04, rw, rw * 0.75);
                    g.setFill(STONE);
                    g.fillOval(rx, ry, rw, rw * 0.7);
                }
            }
            case PILLAR -> {
                floor(g, x, y, s, seed);
                g.setFill(Color.web("#000000", 0.35));
                g.fillOval(x + s * 0.14, y + s * 0.62, s * 0.76, s * 0.3);
                g.setFill(new RadialGradient(0, 0, x + s * 0.4, y + s * 0.35, s * 0.5, false, CycleMethod.NO_CYCLE,
                        new Stop(0, STONE.brighter()), new Stop(1, STONE_DARK)));
                g.fillOval(x + s * 0.12, y + s * 0.1, s * 0.76, s * 0.76);
            }
            case ALTAR -> {
                floor(g, x, y, s, seed);
                fill(g, STONE_DARK, x + s * 0.06, y + s * 0.4, s * 0.88, s * 0.42);
                fill(g, Color.web("#c9c3d8"), x + s * 0.06, y + s * 0.3, s * 0.88, s * 0.18);
                fill(g, TU_GOLD, x + s * 0.4, y + s * 0.3, s * 0.2, s * 0.5);
            }
            default -> floor(g, x, y, s, seed);
        }
    }

    /**
     * Darkens a tile for lighting: visible tiles fade with distance from the player; remembered
     * tiles get a dim blue tint.
     *
     * @param g graphics context
     * @param x left pixel
     * @param y top pixel
     * @param s tile size
     * @param brightness 0 (dark) to 1 (fully lit), for visible tiles
     * @param remembered {@code true} if the tile is explored but not currently visible
     */
    static void shade(GraphicsContext g, double x, double y, double s, double brightness, boolean remembered) {
        if (remembered) {
            g.setFill(Color.web("#080a1c", 0.5));
            g.fillRect(x, y, s, s);
        } else if (brightness < 1) {
            g.setFill(Color.color(0, 0, 0, Math.min(0.6, 1 - brightness)));
            g.fillRect(x, y, s, s);
        }
    }

    private static void drawWall(GraphicsContext g, double x, double y, double s, int seed, boolean face) {
        if (!face) {
            fill(g, wallColor.deriveColor(0, 1, 0.62, 1), x, y, s, s);
            fill(g, wallColor.deriveColor(0, 1, 0.82, 1), x + s * 0.08, y + s * 0.08, s * 0.84, s * 0.84);
            g.setStroke(wallColor.deriveColor(0, 1, 0.6, 1));
            g.setLineWidth(1);
            double split = s * (0.35 + (seed % 3) * 0.1);
            g.strokeLine(x + s * 0.08, y + split, x + s * 0.92, y + split);
            return;
        }
        fill(g, wallColor, x, y, s, s);
        fill(g, wallEdge, x, y, s, s * 0.2);
        g.setStroke(wallColor.deriveColor(0, 1, 0.55, 1));
        g.setLineWidth(1);
        double row1 = y + s * 0.47;
        double row2 = y + s * 0.74;
        g.strokeLine(x, row1, x + s, row1);
        g.strokeLine(x, row2, x + s, row2);
        double offset = (seed % 2) * s * 0.25;
        g.strokeLine(x + s * 0.5 + offset, y + s * 0.2, x + s * 0.5 + offset, row1);
        g.strokeLine(x + s * 0.25 + offset, row1, x + s * 0.25 + offset, row2);
        g.strokeLine(x + s * 0.75 - offset, row2, x + s * 0.75 - offset, y + s);
        fill(g, Color.web("#000000", 0.25), x, y + s * 0.92, s, s * 0.08);
    }

    private static void floor(GraphicsContext g, double x, double y, double s, int seed) {
        double vary = 0.94 + (seed % 13) / 100.0;
        fill(g, floorColor.deriveColor(0, 1, vary, 1), x, y, s, s);
        g.setFill(floorDot);
        g.fillRect(x + s * ((seed % 7) / 8.0 + 0.05), y + s * (((seed >> 3) % 7) / 8.0 + 0.05), s * 0.08, s * 0.08);
        g.fillRect(x + s * (((seed >> 6) % 7) / 8.0 + 0.05), y + s * (((seed >> 9) % 7) / 8.0 + 0.05), s * 0.06, s * 0.06);
        if (seed % 11 == 0) {
            g.setStroke(floorDot);
            g.setLineWidth(1);
            g.strokeLine(x + s * 0.2, y + s * 0.3, x + s * 0.45, y + s * 0.55);
            g.strokeLine(x + s * 0.45, y + s * 0.55, x + s * 0.4, y + s * 0.8);
        }
        g.setStroke(Color.web("#000000", 0.18));
        g.setLineWidth(1);
        g.strokeRect(x + 0.5, y + 0.5, s - 1, s - 1);
    }

    private static void planks(GraphicsContext g, double x, double y, double w, double h, Color wood) {
        fill(g, wood, x, y, w, h);
        g.setStroke(WOOD_LINE);
        g.setLineWidth(1);
        g.strokeRect(x, y, w, h);
        g.strokeLine(x + w / 3, y, x + w / 3, y + h);
        g.strokeLine(x + 2 * w / 3, y, x + 2 * w / 3, y + h);
        g.strokeLine(x, y + h * 0.25, x + w, y + h * 0.25);
        g.strokeLine(x, y + h * 0.75, x + w, y + h * 0.75);
    }

    // ----- creatures -----

    /** Draws the player: a small adventurer in TU black and gold. */
    static void drawPlayer(GraphicsContext g, double x, double y, double s) {
        shadow(g, x, y, s);
        g.setFill(TU_GOLD);
        g.fillPolygon(new double[] {x + s * 0.28, x + s * 0.72, x + s * 0.8, x + s * 0.2},
                new double[] {y + s * 0.38, y + s * 0.38, y + s * 0.86, y + s * 0.86}, 4);
        g.setFill(TU_BLACK);
        g.fillRoundRect(x + s * 0.33, y + s * 0.4, s * 0.34, s * 0.42, s * 0.12, s * 0.12);
        g.setFill(TU_GOLD);
        g.fillRect(x + s * 0.33, y + s * 0.56, s * 0.34, s * 0.06);
        g.setFill(Color.web("#e9c49c"));
        g.fillOval(x + s * 0.35, y + s * 0.12, s * 0.3, s * 0.3);
        g.setFill(TU_BLACK);
        g.fillArc(x + s * 0.33, y + s * 0.08, s * 0.34, s * 0.26, 0, 180, ArcType.ROUND);
        g.setStroke(Color.web("#2a1e05"));
        g.setLineWidth(1);
        g.strokeOval(x + s * 0.35, y + s * 0.12, s * 0.3, s * 0.3);
    }

    /**
     * Draws a monster as a shaded disc with its glyph, plus a health bar when hurt and a "z" when
     * asleep. Bosses are drawn larger with a heavier outline.
     */
    static void drawMonster(GraphicsContext g, Monster m, double x, double y, double s) {
        Color color = Color.web(m.type().color());
        shadow(g, x, y, s);
        double inset = m.isBoss() ? 0.0 : 0.08;
        g.setFill(new RadialGradient(0, 0, x + s * 0.4, y + s * 0.35, s * 0.55, false, CycleMethod.NO_CYCLE,
                new Stop(0, color.deriveColor(0, 0.9, 0.55, 1)), new Stop(1, Color.web("#0b0b10"))));
        g.fillOval(x + s * inset, y + s * inset, s * (1 - 2 * inset), s * (1 - 2 * inset));
        g.setStroke(color);
        g.setLineWidth(m.isBoss() ? 2.5 : 1.5);
        g.strokeOval(x + s * inset, y + s * inset, s * (1 - 2 * inset), s * (1 - 2 * inset));
        glyph(g, String.valueOf(m.type().glyph()), color.brighter(), x, y, s, 0.62);
        if (m.hp() < m.maxHp()) {
            healthBar(g, x, y, s, (double) m.hp() / m.maxHp());
        }
        if (m.isAsleep()) {
            g.setFill(Color.web("#a0c8ff"));
            g.setFont(Font.font("Monospaced", FontWeight.BOLD, s * 0.38));
            g.fillText("z", x + s * 0.74, y + s * 0.28);
        }
    }

    /** Draws a friendly character as a small robed figure, or a fixture as an object. */
    static void drawNpc(GraphicsContext g, Npc npc, double x, double y, double s) {
        Color color = Color.web(npc.role().color());
        switch (npc.role()) {
            case TIGER_STATUE -> {
                fill(g, STONE_DARK, x + s * 0.15, y + s * 0.72, s * 0.7, s * 0.2);
                drawTigerFace(g, x, y - s * 0.08, s, TU_GOLD.deriveColor(0, 0.8, 0.9, 1));
            }
            case NOTICE_BOARD, SIGN -> {
                fill(g, WOOD_DARK, x + s * 0.45, y + s * 0.5, s * 0.1, s * 0.45);
                planks(g, x + s * 0.12, y + s * 0.12, s * 0.76, s * 0.45, WOOD);
                fill(g, Color.web("#efe6c8"), x + s * 0.22, y + s * 0.2, s * 0.24, s * 0.28);
                fill(g, Color.web("#efe6c8"), x + s * 0.52, y + s * 0.22, s * 0.26, s * 0.2);
            }
            default -> {
                shadow(g, x, y, s);
                g.setFill(color.deriveColor(0, 1, 0.7, 1));
                g.fillPolygon(new double[] {x + s * 0.5, x + s * 0.82, x + s * 0.18},
                        new double[] {y + s * 0.3, y + s * 0.9, y + s * 0.9}, 3);
                g.setFill(color);
                g.fillRect(x + s * 0.3, y + s * 0.6, s * 0.4, s * 0.05);
                g.setFill(npc.role() == Npc.Role.ZOMBIE_KID ? Color.web("#8fd36a") : Color.web("#d9b48f"));
                g.fillOval(x + s * 0.36, y + s * 0.1, s * 0.28, s * 0.28);
                g.setStroke(color);
                g.setLineWidth(1.5);
                g.strokeOval(x + s * 0.36, y + s * 0.1, s * 0.28, s * 0.28);
            }
        }
    }

    /** Draws Doc the tiger cub: a gold face with ears and stripes, and a health bar when hurt. */
    static void drawCompanion(GraphicsContext g, Companion doc, double x, double y, double s) {
        shadow(g, x, y, s);
        drawTigerFace(g, x, y, s, Color.web("#f2a93b"));
        if (doc.hp() < doc.maxHp()) {
            healthBar(g, x, y, s, (double) doc.hp() / doc.maxHp());
        }
    }

    private static void drawTigerFace(GraphicsContext g, double x, double y, double s, Color fur) {
        g.setFill(fur);
        g.fillPolygon(new double[] {x + s * 0.18, x + s * 0.3, x + s * 0.4},
                new double[] {y + s * 0.38, y + s * 0.12, y + s * 0.3}, 3);
        g.fillPolygon(new double[] {x + s * 0.82, x + s * 0.7, x + s * 0.6},
                new double[] {y + s * 0.38, y + s * 0.12, y + s * 0.3}, 3);
        g.fillOval(x + s * 0.18, y + s * 0.22, s * 0.64, s * 0.6);
        g.setStroke(TU_BLACK);
        g.setLineWidth(Math.max(1.5, s / 14));
        g.strokeLine(x + s * 0.5, y + s * 0.25, x + s * 0.5, y + s * 0.4);
        g.strokeLine(x + s * 0.22, y + s * 0.48, x + s * 0.32, y + s * 0.5);
        g.strokeLine(x + s * 0.78, y + s * 0.48, x + s * 0.68, y + s * 0.5);
        g.setFill(TU_BLACK);
        g.fillOval(x + s * 0.35, y + s * 0.44, s * 0.08, s * 0.08);
        g.fillOval(x + s * 0.57, y + s * 0.44, s * 0.08, s * 0.08);
        g.setFill(Color.web("#fff3dc"));
        g.fillOval(x + s * 0.36, y + s * 0.62, s * 0.28, s * 0.16);
        g.setFill(TU_BLACK);
        g.fillOval(x + s * 0.45, y + s * 0.6, s * 0.1, s * 0.07);
    }

    private static void shadow(GraphicsContext g, double x, double y, double s) {
        g.setFill(Color.web("#000000", 0.35));
        g.fillOval(x + s * 0.16, y + s * 0.78, s * 0.68, s * 0.18);
    }

    private static void healthBar(GraphicsContext g, double x, double y, double s, double frac) {
        double h = Math.max(3, s / 9);
        fill(g, Color.web("#400000"), x + 2, y, s - 4, h);
        fill(g, frac > 0.5 ? Color.web("#5fd35f") : frac > 0.25 ? Color.web("#e0c040") : Color.web("#e04040"),
                x + 2, y, (s - 4) * frac, h);
    }

    private static void glyph(GraphicsContext g, String text, Color color, double x, double y, double s, double size) {
        g.setFill(color);
        g.setFont(Font.font("Monospaced", FontWeight.BOLD, s * size));
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);
        g.fillText(text, x + s / 2, y + s / 2 + 1);
        g.setTextAlign(TextAlignment.LEFT);
        g.setTextBaseline(VPos.BASELINE);
    }

    // ----- features and items -----

    /** Draws flames over a burning tile. */
    static void drawFire(GraphicsContext g, double x, double y, double s, int turnsLeft) {
        g.setFill(Color.web("#ff6a00", 0.55));
        g.fillRect(x, y, s, s);
        g.setFill(Color.web("#ffd040"));
        double flicker = (turnsLeft % 2) * s * 0.08;
        g.fillPolygon(new double[] {x + s * 0.2, x + s * 0.35 + flicker, x + s * 0.5},
                new double[] {y + s * 0.9, y + s * 0.3, y + s * 0.9}, 3);
        g.fillPolygon(new double[] {x + s * 0.45, x + s * 0.65 - flicker, x + s * 0.85},
                new double[] {y + s * 0.9, y + s * 0.15, y + s * 0.9}, 3);
    }

    /** Draws a revealed trap as a pressure plate with a warning caret. */
    static void drawTrap(GraphicsContext g, Trap.Type trap, double x, double y, double s) {
        Color color = Color.web(trap.color());
        g.setFill(color.deriveColor(0, 0.6, 0.35, 0.8));
        g.fillRect(x + s * 0.18, y + s * 0.55, s * 0.64, s * 0.25);
        g.setStroke(color);
        g.setLineWidth(Math.max(2, s / 12));
        g.strokeLine(x + s * 0.25, y + s * 0.62, x + s * 0.5, y + s * 0.2);
        g.strokeLine(x + s * 0.5, y + s * 0.2, x + s * 0.75, y + s * 0.62);
    }

    /** Draws an item as a small icon for its category. */
    static void drawItem(GraphicsContext g, ItemCategory category, double x, double y, double s) {
        Color color = Color.web(category.color());
        g.setFill(Color.web("#000000", 0.3));
        g.fillOval(x + s * 0.22, y + s * 0.74, s * 0.56, s * 0.14);
        g.setLineWidth(Math.max(1.5, s / 14));
        switch (category) {
            case WEAPON -> {
                g.setStroke(color);
                g.strokeLine(x + s * 0.25, y + s * 0.75, x + s * 0.75, y + s * 0.2);
                g.setStroke(WOOD);
                g.strokeLine(x + s * 0.2, y + s * 0.6, x + s * 0.4, y + s * 0.8);
                g.strokeLine(x + s * 0.15, y + s * 0.85, x + s * 0.3, y + s * 0.7);
            }
            case ARMOR -> {
                g.setFill(color);
                g.fillPolygon(new double[] {x + s * 0.22, x + s * 0.78, x + s * 0.72, x + s * 0.5, x + s * 0.28},
                        new double[] {y + s * 0.2, y + s * 0.2, y + s * 0.55, y + s * 0.82, y + s * 0.55}, 5);
                g.setStroke(color.darker());
                g.strokeLine(x + s * 0.5, y + s * 0.24, x + s * 0.5, y + s * 0.76);
            }
            case THROWN -> {
                g.setStroke(color);
                g.strokeLine(x + s * 0.2, y + s * 0.75, x + s * 0.72, y + s * 0.28);
                g.setFill(STONE);
                g.fillPolygon(new double[] {x + s * 0.8, x + s * 0.62, x + s * 0.72},
                        new double[] {y + s * 0.2, y + s * 0.28, y + s * 0.38}, 3);
            }
            case WAND -> {
                g.setStroke(WOOD);
                g.strokeLine(x + s * 0.25, y + s * 0.78, x + s * 0.68, y + s * 0.3);
                g.setFill(color);
                g.fillOval(x + s * 0.6, y + s * 0.18, s * 0.18, s * 0.18);
            }
            case POTION -> {
                g.setFill(Color.web("#e0e0f0", 0.8));
                g.fillRect(x + s * 0.43, y + s * 0.18, s * 0.14, s * 0.18);
                g.setFill(color);
                g.fillOval(x + s * 0.27, y + s * 0.34, s * 0.46, s * 0.44);
                g.setFill(Color.web("#ffffff", 0.5));
                g.fillOval(x + s * 0.36, y + s * 0.42, s * 0.1, s * 0.1);
            }
            case SCROLL -> {
                fill(g, color, x + s * 0.24, y + s * 0.3, s * 0.52, s * 0.4);
                g.setFill(color.darker());
                g.fillOval(x + s * 0.18, y + s * 0.26, s * 0.14, s * 0.48);
                g.fillOval(x + s * 0.68, y + s * 0.26, s * 0.14, s * 0.48);
                g.setStroke(STONE_DARK);
                g.setLineWidth(1);
                g.strokeLine(x + s * 0.34, y + s * 0.42, x + s * 0.64, y + s * 0.42);
                g.strokeLine(x + s * 0.34, y + s * 0.54, x + s * 0.58, y + s * 0.54);
            }
            case FOOD -> {
                g.setFill(color);
                g.fillOval(x + s * 0.22, y + s * 0.36, s * 0.56, s * 0.38);
                g.setFill(color.brighter());
                g.fillOval(x + s * 0.3, y + s * 0.4, s * 0.2, s * 0.1);
            }
            case GOLD -> {
                for (int i = 0; i < 3; i++) {
                    g.setFill(color.deriveColor(0, 1, 0.8, 1));
                    g.fillOval(x + s * (0.24 + i * 0.08), y + s * (0.62 - i * 0.12), s * 0.42, s * 0.18);
                    g.setFill(color);
                    g.fillOval(x + s * (0.24 + i * 0.08), y + s * (0.6 - i * 0.12), s * 0.42, s * 0.16);
                }
            }
            case KEY -> {
                g.setStroke(color);
                g.strokeOval(x + s * 0.2, y + s * 0.3, s * 0.24, s * 0.24);
                g.strokeLine(x + s * 0.44, y + s * 0.42, x + s * 0.8, y + s * 0.42);
                g.strokeLine(x + s * 0.7, y + s * 0.42, x + s * 0.7, y + s * 0.56);
                g.strokeLine(x + s * 0.78, y + s * 0.42, x + s * 0.78, y + s * 0.54);
            }
            case QUEST -> {
                fill(g, Color.web("#f3ead2"), x + s * 0.2, y + s * 0.34, s * 0.6, s * 0.3);
                fill(g, TU_BLACK, x + s * 0.46, y + s * 0.34, s * 0.08, s * 0.3);
                g.setFill(color);
                g.fillOval(x + s * 0.42, y + s * 0.56, s * 0.16, s * 0.16);
            }
            default -> glyph(g, String.valueOf(category.glyph()), color, x, y, s, 0.7);
        }
    }

    private static void fill(GraphicsContext g, Color color, double x, double y, double w, double h) {
        g.setFill(color);
        g.fillRect(x, y, w, h);
    }
}
