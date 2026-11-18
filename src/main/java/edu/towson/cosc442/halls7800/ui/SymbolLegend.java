package edu.towson.cosc442.halls7800.ui;

import edu.towson.cosc442.halls7800.entity.Companion;
import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.entity.MonsterType;
import edu.towson.cosc442.halls7800.entity.Npc;
import edu.towson.cosc442.halls7800.item.ItemCategory;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Terrain;
import edu.towson.cosc442.halls7800.world.Trap;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * The "Symbols" page of the help screen. Every symbol is drawn with the same {@link TileRenderer}
 * code as the map, so the legend always matches what the player sees.
 */
final class SymbolLegend {

    /** Draws one sample symbol at a position and size. */
    @FunctionalInterface
    private interface Painter {
        void paint(GraphicsContext g, double x, double y, double s);
    }

    /** One legend row: a group heading or a symbol with its label. */
    private record Entry(String label, Painter painter) {
    }

    private static final Position ORIGIN = new Position(0, 0);
    private static final List<Entry> ENTRIES = build();

    private SymbolLegend() {
    }

    private static List<Entry> build() {
        List<Entry> e = new ArrayList<>();
        Monster awake = new Monster(MonsterType.OFF_BY_ONE_GOBLIN, ORIGIN, false);
        Monster asleep = new Monster(MonsterType.GIANT_RAT, ORIGIN, true);
        Monster boss = new Monster(MonsterType.SPAGHETTI_HYDRA, ORIGIN, false);
        Companion doc = new Companion(ORIGIN);
        e.add(heading("Characters"));
        e.add(new Entry("You", TileRenderer::drawPlayer));
        e.add(new Entry("Doc, your tiger cub", (g, x, y, s) -> TileRenderer.drawCompanion(g, doc, x, y, s)));
        e.add(npc("A person (walk in to talk)", Npc.Role.ARCHIVIST));
        e.add(new Entry("Monster (letter = kind)", (g, x, y, s) -> TileRenderer.drawMonster(g, awake, x, y, s)));
        e.add(new Entry("Sleeping monster (z)", (g, x, y, s) -> TileRenderer.drawMonster(g, asleep, x, y, s)));
        e.add(new Entry("Boss (bold outline)", (g, x, y, s) -> TileRenderer.drawMonster(g, boss, x, y, s)));
        e.add(npc("Tiger statue", Npc.Role.TIGER_STATUE));
        e.add(npc("Sign / notice board", Npc.Role.SIGN));

        e.add(heading("Items"));
        e.add(item("Weapon", ItemCategory.WEAPON));
        e.add(item("Armor", ItemCategory.ARMOR));
        e.add(item("Thrown weapon", ItemCategory.THROWN));
        e.add(item("Wand", ItemCategory.WAND));
        e.add(item("Potion", ItemCategory.POTION));
        e.add(item("Scroll", ItemCategory.SCROLL));
        e.add(item("Food", ItemCategory.FOOD));
        e.add(item("Gold", ItemCategory.GOLD));
        e.add(item("Key", ItemCategory.KEY));
        e.add(item("Quest item", ItemCategory.QUEST));

        e.add(heading("Places"));
        e.add(terrain("Floor", Terrain.FLOOR));
        e.add(terrain("Wall", Terrain.WALL));
        e.add(terrain("Door (walk in to open)", Terrain.DOOR_CLOSED));
        e.add(terrain("Open door", Terrain.DOOR_OPEN));
        e.add(terrain("Locked door (needs key)", Terrain.DOOR_LOCKED));
        e.add(terrain("Stairs down  >", Terrain.STAIRS_DOWN));
        e.add(terrain("Stairs up  <", Terrain.STAIRS_UP));
        e.add(terrain("Shallow water (slow)", Terrain.WATER));
        e.add(terrain("Tall grass (hides, burns)", Terrain.TALL_GRASS));
        e.add(terrain("Chasm", Terrain.CHASM));
        e.add(terrain("Rubble", Terrain.RUBBLE));
        e.add(terrain("Pillar", Terrain.PILLAR));
        e.add(terrain("Altar", Terrain.ALTAR));
        e.add(new Entry("Trap you have found", (g, x, y, s) -> {
            TileRenderer.drawTerrain(g, Terrain.FLOOR, x, y, s, 7, false);
            TileRenderer.drawTrap(g, Trap.Type.SPIKE, x, y, s);
        }));
        e.add(new Entry("Fire", (g, x, y, s) -> {
            TileRenderer.drawTerrain(g, Terrain.TALL_GRASS, x, y, s, 7, false);
            TileRenderer.drawFire(g, x, y, s, 2);
        }));
        return e;
    }

    private static Entry heading(String text) {
        return new Entry(text, null);
    }

    private static Entry npc(String label, Npc.Role role) {
        Npc sample = new Npc(role, ORIGIN);
        return new Entry(label, (g, x, y, s) -> TileRenderer.drawNpc(g, sample, x, y, s));
    }

    private static Entry item(String label, ItemCategory category) {
        return new Entry(label, (g, x, y, s) -> {
            TileRenderer.drawTerrain(g, Terrain.FLOOR, x, y, s, 7, false);
            TileRenderer.drawItem(g, category, x, y, s);
        });
    }

    private static Entry terrain(String label, Terrain terrain) {
        return new Entry(label, (g, x, y, s) -> TileRenderer.drawTerrain(g, terrain, x, y, s, 7, true));
    }

    /**
     * Draws the legend over the map area.
     *
     * @param g graphics context
     * @param width area width
     * @param height area height
     */
    static void draw(GraphicsContext g, double width, double height) {
        g.setFill(Color.web("#0b0b10", 0.95));
        g.fillRect(20, 20, width - 40, height - 40);
        g.setFill(Color.web("#f2c14e"));
        g.setFont(Font.font("Monospaced", FontWeight.BOLD, 15));
        g.fillText("SYMBOLS   (Tab: controls, Esc: close)", 40, 48);

        int columns = width >= 900 ? 3 : 2;
        int perColumn = (ENTRIES.size() + columns - 1) / columns;
        double rowHeight = Math.max(18, Math.min(34, (height - 110) / perColumn));
        double icon = rowHeight - 3;
        double columnWidth = (width - 80) / columns;
        Font labelFont = Font.font("Monospaced", Math.max(10, Math.min(13, rowHeight * 0.45)));
        Font headingFont = Font.font("Monospaced", FontWeight.BOLD, Math.max(10, Math.min(13, rowHeight * 0.45)));
        for (int i = 0; i < ENTRIES.size(); i++) {
            Entry entry = ENTRIES.get(i);
            int column = i / perColumn;
            double x = 40 + column * columnWidth;
            double y = 64 + (i % perColumn) * rowHeight;
            if (entry.painter() == null) {
                g.setFill(Color.web("#f2c14e"));
                g.setFont(headingFont);
                g.fillText(entry.label(), x, y + rowHeight * 0.65);
                continue;
            }
            entry.painter().paint(g, x, y, icon);
            g.setFill(Color.web("#f4f4f8"));
            g.setFont(labelFont);
            g.fillText(entry.label(), x + icon + 10, y + icon * 0.65);
        }
        g.setFill(Color.web("#a8a4b8"));
        g.setFont(Font.font("Monospaced", 12));
        g.fillText("Dimmed, bluish areas are places you remember but cannot see right now.", 40, height - 32);
    }
}
