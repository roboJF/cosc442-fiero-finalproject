package edu.towson.cosc442.halls7800.ui;

import edu.towson.cosc442.halls7800.effect.StatusEffect;
import edu.towson.cosc442.halls7800.engine.Command;
import edu.towson.cosc442.halls7800.engine.GamePhase;
import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.engine.DropCommand;
import edu.towson.cosc442.halls7800.engine.Examiner;
import edu.towson.cosc442.halls7800.ui.audio.AudioPlayer;
import edu.towson.cosc442.halls7800.ui.audio.Tune;
import edu.towson.cosc442.halls7800.world.Direction;
import edu.towson.cosc442.halls7800.world.Terrain;
import edu.towson.cosc442.halls7800.engine.ThrowCommand;
import edu.towson.cosc442.halls7800.engine.UseItemCommand;
import edu.towson.cosc442.halls7800.entity.Player;
import edu.towson.cosc442.halls7800.item.Inventory;
import edu.towson.cosc442.halls7800.item.ItemCategory;
import edu.towson.cosc442.halls7800.entity.Companion;
import edu.towson.cosc442.halls7800.entity.Npc;
import edu.towson.cosc442.halls7800.engine.RunStats;
import edu.towson.cosc442.halls7800.progression.Experience;
import edu.towson.cosc442.halls7800.progression.ScoreCalculator;
import javafx.scene.Parent;
import edu.towson.cosc442.halls7800.progression.Perk;
import edu.towson.cosc442.halls7800.quest.Quest;
import edu.towson.cosc442.halls7800.world.Trap;
import java.util.Map;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.engine.MessageLog;
import edu.towson.cosc442.halls7800.world.FloorView;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Zone;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * The main game screen: the map in the center, a status panel on the right and the message log
 * at the bottom. All game logic lives in {@link GameSession}; this class only draws it and
 * forwards key presses as commands.
 */
public final class GameView extends BorderPane implements Screen {

    /** Receives the player's requests to leave the game screen. */
    public interface Listener {
        /**
         * The player pressed Esc to return to the title screen (the run should be saved).
         *
         * @param session the run in progress
         */
        void quitToTitle(GameSession session);

        /**
         * The run is over and the player has read the summary.
         *
         * @param session the finished run
         */
        void runFinished(GameSession session);
    }

    private static final int MIN_TILE = 24;
    private static final int MAX_TILE = 44;
    /** Roughly how many tiles fit top to bottom; larger windows get larger tiles. */
    private static final double TILES_HIGH = 21;
    /** How much darker the edge of the player's sight is than the player's own tile. */
    private static final double LIGHT_FALLOFF = 0.45;
    private static final int LOG_LINES = 6;
    private static final int MAX_LISTED_MONSTERS = 6;
    private static final double SIDE_WIDTH = 310;
    private static final String PANEL_STYLE = "-fx-background-color: #15141c;";
    private static final String TEXT_STYLE = "-fx-text-fill: #f4f4f8;";

    private final GameSession session;
    private final Listener listener;
    private final AudioPlayer audio;
    private final Label soundLabel = label(11, false);
    private final Canvas canvas = new Canvas();
    private final Label floorLabel = label(14, true);
    private final Label statusLabel = label(13, false);
    private final Label inventoryLabel = label(12, false);
    private final Label promptLabel = label(13, true);
    private final List<Label> logLines = new ArrayList<>();
    private ItemAction pending = ItemAction.NONE;
    private boolean showHelp;
    private boolean showSymbols;
    private boolean showJournal;
    private Position lookCursor;
    private boolean shopSelling;

    /** An item command waiting for the player to choose a pack letter. */
    private enum ItemAction {
        NONE(""), USE("Use which item?"), DROP("Drop which item?"), THROW("Throw which item?");

        private final String prompt;

        ItemAction(String prompt) {
            this.prompt = prompt;
        }
    }

    /**
     * @param session the game to display and control
     * @param listener receives requests to leave the game screen
     * @param audio plays music and sound effects
     */
    public GameView(GameSession session, Listener listener, AudioPlayer audio) {
        this.session = session;
        this.listener = listener;
        this.audio = audio;
        session.events().subscribe(audio::onEvent);

        Pane mapPane = new Pane(canvas);
        mapPane.setStyle("-fx-background-color: #0b0b10;");
        canvas.widthProperty().bind(mapPane.widthProperty());
        canvas.heightProperty().bind(mapPane.heightProperty());
        canvas.widthProperty().addListener((obs, oldV, newV) -> drawMap());
        canvas.heightProperty().addListener((obs, oldV, newV) -> drawMap());
        setCenter(mapPane);

        Label title = label(16, true);
        title.setText("THE HALLS OF 7800");
        Label hint = label(11, false);
        hint.setText("Press ? for help");
        VBox side = new VBox(10, title, floorLabel, statusLabel, inventoryLabel, hint, soundLabel);
        side.setPadding(new Insets(12));
        side.setStyle(PANEL_STYLE);
        for (Label l : List.of(floorLabel, statusLabel, inventoryLabel)) {
            l.setWrapText(true);
            l.setMaxWidth(SIDE_WIDTH - 40);
        }
        ScrollPane sideScroll = new ScrollPane(side);
        sideScroll.setFitToWidth(true);
        sideScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sideScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sideScroll.setFocusTraversable(false);
        sideScroll.setPrefWidth(SIDE_WIDTH);
        sideScroll.setMinWidth(SIDE_WIDTH);
        sideScroll.setStyle("-fx-background: #15141c; -fx-background-color: #15141c;");
        setRight(sideScroll);

        VBox log = new VBox(2);
        log.setPadding(new Insets(8, 12, 8, 12));
        log.setStyle(PANEL_STYLE);
        promptLabel.setStyle("-fx-text-fill: #f2c14e;");
        promptLabel.setWrapText(true);
        promptLabel.maxWidthProperty().bind(widthProperty().subtract(30));
        log.getChildren().add(promptLabel);
        for (int i = 0; i < LOG_LINES; i++) {
            Label line = label(13, false);
            line.setWrapText(true);
            line.maxWidthProperty().bind(widthProperty().subtract(30));
            logLines.add(line);
            log.getChildren().add(line);
        }
        setBottom(log);
    }

    @Override
    public Parent root() {
        return this;
    }

    @Override
    public void keyPressed(KeyCode code) {
        switch (InputMapper.mode(session, lookCursor != null, pending != ItemAction.NONE)) {
            case FINISHED -> {
                if (code == KeyCode.ENTER || code == KeyCode.ESCAPE) {
                    listener.runFinished(session);
                }
            }
            case SHOP -> onShopKey(code);
            case PERK -> onPerkKey(code);
            case EXAMINE -> onExamineKey(code);
            case ITEM -> onItemKey(code);
            case PLAY -> onPlayKey(code);
        }
    }

    @Override
    public void keyTyped(String character) {
        KeyCode code = InputMapper.keyForTypedShortcut(character);
        if (code != null) {
            keyPressed(code);
        }
    }

    private void onPerkKey(KeyCode code) {
        int choice = switch (code) {
            case DIGIT1, NUMPAD1 -> 0;
            case DIGIT2, NUMPAD2 -> 1;
            case DIGIT3, NUMPAD3 -> 2;
            default -> -1;
        };
        List<Perk> offered = session.player().perkChoices();
        if (choice >= 0 && choice < offered.size()) {
            session.choosePerk(offered.get(choice));
        }
        refresh();
    }

    private void onExamineKey(KeyCode code) {
        Direction direction = InputMapper.directionFor(code);
        if (direction != null) {
            Position next = lookCursor.step(direction);
            if (session.currentFloor().isInBounds(next)) {
                lookCursor = next;
            }
        } else if (code == KeyCode.ESCAPE || code == KeyCode.L) {
            lookCursor = null;
        }
        refresh();
    }

    private void onItemKey(KeyCode code) {
        if (code.isLetterKey()) {
            int slot = Inventory.indexFor(code.getChar().charAt(0));
            ItemAction action = pending;
            pending = ItemAction.NONE;
            submit(switch (action) {
                case USE -> new UseItemCommand(slot);
                case DROP -> new DropCommand(slot);
                case THROW -> new ThrowCommand(slot);
                default -> null;
            });
            return;
        }
        if (code == KeyCode.ESCAPE) {
            pending = ItemAction.NONE;
        }
        refresh();
    }

    private void onPlayKey(KeyCode code) {
        if (code == KeyCode.ESCAPE) {
            if (showHelp || showJournal) {
                showHelp = false;
                showJournal = false;
                refresh();
            } else {
                listener.quitToTitle(session);
            }
            return;
        }
        switch (code) {
            case U -> pending = ItemAction.USE;
            case R -> pending = ItemAction.DROP;
            case T -> pending = ItemAction.THROW;
            case F1 -> showHelp = !showHelp;
            case TAB -> showSymbols = showHelp ? !showSymbols : showSymbols;
            case J -> showJournal = !showJournal;
            case M -> audio.toggleMusic();
            case N -> audio.toggleSoundEffects();
            case L -> lookCursor = session.player().position();
            default -> {
                submit(InputMapper.fromKey(code));
                return;
            }
        }
        refresh();
    }

    private void onShopKey(KeyCode code) {
        if (code == KeyCode.ESCAPE) {
            session.closeShop();
            shopSelling = false;
        } else if (code == KeyCode.TAB) {
            shopSelling = !shopSelling;
        } else if (code.isLetterKey()) {
            int index = Inventory.indexFor(code.getChar().charAt(0));
            if (shopSelling) {
                session.sell(index);
            } else {
                session.buy(index);
            }
        }
        refresh();
    }

    private void submit(Command command) {
        if (command != null) {
            session.submit(command);
            refresh();
        }
    }

    /** Redraws the whole screen from the current game state. */
    public void refresh() {
        audio.playMusic(Tune.forSession(session));
        soundLabel.setText(!audio.isAvailable() ? "No audio device found"
                : "Music " + (audio.isMusicOn() ? "on" : "off") + " (M)   Sound " + (audio.isSoundOn() ? "on" : "off") + " (N)");
        FloorView floor = session.currentFloor();
        floorLabel.setText(floor.name() + "\nDepth " + floor.depth() + " of " + session.deepestDepth());
        Player player = session.player();
        int nextLevel = Math.min(player.level() + 1, Experience.MAX_LEVEL);
        StringBuilder status = new StringBuilder(player.name())
                .append("\nLevel   ").append(player.level())
                .append("\nHP      ").append(player.hp()).append(" / ").append(player.maxHp())
                .append("\nXP      ").append(player.experience()).append(" / ")
                .append(Experience.thresholdFor(nextLevel))
                .append("\nGold    ").append(player.gold())
                .append("\nAttack  ").append(player.meleeAttack().minDamage()).append('-')
                .append(player.meleeAttack().maxDamage()).append("  Armor ").append(player.armor())
                .append("\nKills   ").append(player.kills())
                .append(session.companion() != null
                        ? "\nDoc     " + session.companion().hp() + " / " + session.companion().maxHp()
                        : session.isCompanionResting() ? "\nDoc     resting in the Atrium" : "")
                .append("\nTurn    ").append(session.turn())
                .append("\nSeed    ").append(session.seed());
        if (player.hunger() != Player.Hunger.FED) {
            status.append("\n* ").append(player.hunger().name().charAt(0))
                    .append(player.hunger().name().substring(1).toLowerCase());
        }
        for (StatusEffect e : player.effects().active()) {
            status.append("\n* ").append(e.type().displayName()).append(" (").append(e.remaining()).append(')');
        }
        List<Monster> seen = session.monsters().stream().filter(m -> floor.isVisible(m.position())).toList();
        if (!seen.isEmpty()) {
            status.append("\n\nIn view:");
            for (Monster m : seen.subList(0, Math.min(seen.size(), MAX_LISTED_MONSTERS))) {
                status.append("\n").append(m.type().glyph()).append(' ').append(m.name())
                        .append(m.isAsleep() ? " (asleep)" : " " + m.hp() + "/" + m.maxHp());
            }
            if (seen.size() > MAX_LISTED_MONSTERS) {
                status.append("\n...and ").append(seen.size() - MAX_LISTED_MONSTERS).append(" more");
            }
        }
        statusLabel.setText(status.toString());
        inventoryLabel.setText(inventoryText());
        if (lookCursor != null) {
            promptLabel.setText("Examine: " + Examiner.describe(session, lookCursor) + "   (move to look, L or Esc to stop)");
        } else {
            promptLabel.setText(pending == ItemAction.NONE ? "" : pending.prompt + " (letter, Esc to cancel)");
        }
        List<MessageLog.Entry> recent = session.recentMessages(LOG_LINES);
        for (int i = 0; i < LOG_LINES; i++) {
            int index = i - (LOG_LINES - recent.size());
            logLines.get(i).setText(index >= 0 ? recent.get(index).display() : "");
            logLines.get(i).setOpacity(i == LOG_LINES - 1 ? 1.0 : 0.72 + 0.05 * i);
        }
        drawMap();
    }

    private void drawMap() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        TileRenderer.clear(g, width, height);
        FloorView floor = session.currentFloor();
        TileRenderer.usePalette(Zone.forDepth(floor.depth()));
        Position player = session.player().position();
        int tile = (int) Math.max(MIN_TILE, Math.min(MAX_TILE, Math.floor(height / TILES_HIGH)));
        int cols = (int) Math.ceil(width / tile);
        int rows = (int) Math.ceil(height / tile);
        int originX = Camera.origin(player.x(), cols, floor.width());
        int originY = Camera.origin(player.y(), rows, floor.height());
        for (int sy = 0; sy < rows; sy++) {
            for (int sx = 0; sx < cols; sx++) {
                Position p = new Position(originX + sx, originY + sy);
                if (!floor.isInBounds(p) || !floor.isExplored(p)) {
                    continue;
                }
                Terrain terrain = floor.terrainAt(p);
                Position below = p.step(Direction.SOUTH);
                boolean face = terrain.appearance() == Terrain.WALL && floor.isExplored(below)
                        && floor.terrainAt(below).appearance() != Terrain.WALL;
                TileRenderer.drawTerrain(g, terrain, sx * tile, sy * tile, tile, TileRenderer.hash(p.x(), p.y()), face);
                boolean visible = floor.isVisible(p);
                double light = 1.0 - LIGHT_FALLOFF * Math.min(1.0,
                        Math.sqrt(p.squaredDistanceTo(player)) / (session.sightRadius() + 1));
                TileRenderer.shade(g, sx * tile, sy * tile, tile, light, !visible);
            }
        }
        for (Map.Entry<Position, ItemCategory> entry : session.itemIcons().entrySet()) {
            Position p = entry.getKey();
            if (floor.isVisible(p)) {
                TileRenderer.drawItem(g, entry.getValue(), (p.x() - originX) * tile, (p.y() - originY) * tile, tile);
            }
        }
        for (Map.Entry<Position, Trap.Type> entry : session.discoveredTraps().entrySet()) {
            Position p = entry.getKey();
            if (floor.isExplored(p)) {
                TileRenderer.drawTrap(g, entry.getValue(), (p.x() - originX) * tile, (p.y() - originY) * tile, tile);
            }
        }
        for (Map.Entry<Position, Integer> fire : session.burningTiles().entrySet()) {
            Position p = fire.getKey();
            if (floor.isVisible(p)) {
                TileRenderer.drawFire(g, (p.x() - originX) * tile, (p.y() - originY) * tile, tile, fire.getValue());
            }
        }
        Companion doc = session.companion();
        if (doc != null && floor.isVisible(doc.position())) {
            TileRenderer.drawCompanion(g, doc, (doc.position().x() - originX) * tile,
                    (doc.position().y() - originY) * tile, tile);
        }
        for (Npc npc : session.npcs()) {
            if (floor.isVisible(npc.position())) {
                TileRenderer.drawNpc(g, npc, (npc.position().x() - originX) * tile,
                        (npc.position().y() - originY) * tile, tile);
            }
        }
        for (Monster m : session.monsters()) {
            if (floor.isVisible(m.position())) {
                TileRenderer.drawMonster(g, m, (m.position().x() - originX) * tile,
                        (m.position().y() - originY) * tile, tile);
            }
        }
        TileRenderer.drawPlayer(g, (player.x() - originX) * tile, (player.y() - originY) * tile, tile,
                session.player().name());
        if (lookCursor != null) {
            g.setStroke(Color.web("#f2c14e"));
            g.setLineWidth(2);
            g.strokeRect((lookCursor.x() - originX) * tile + 1, (lookCursor.y() - originY) * tile + 1, tile - 2, tile - 2);
        }
        if (session.phase() != GamePhase.PLAYING) {
            drawEndBanner(g, width, height);
        }
        if (session.isShopOpen()) {
            drawShop(g, width, height);
        } else if (!session.player().perkChoices().isEmpty() && session.phase() == GamePhase.PLAYING) {
            drawPerks(g, width, height);
        } else if (showJournal) {
            drawJournal(g, width, height);
        } else if (showHelp) {
            drawHelp(g, width, height);
        }
    }

    private void drawEndBanner(GraphicsContext g, double width, double height) {
        boolean won = session.phase() == GamePhase.VICTORY;
        RunStats stats = session.stats();
        String title = session.isTutorial() ? "ORIENTATION COMPLETE"
                : won ? "YOU HAVE EARNED THE DIPLOMA" : "YOU HAVE FALLEN";
        List<String> lines = new ArrayList<>();
        if (!session.isTutorial()) {
            lines.add("Score " + ScoreCalculator.score(session) + "   (" + session.difficulty().title() + ")");
            lines.add("Deepest depth " + session.deepestReached() + "   Level " + session.player().level()
                    + "   Turns " + session.turn());
            lines.add("Monsters defeated " + stats.totalKills() + "   Bosses " + stats.bossesDefeated()
                    + "   Gold found " + stats.goldCollected());
            lines.add("Damage dealt " + stats.damageDealt() + "   taken " + stats.damageTaken()
                    + "   Potions " + stats.potionsDrunk() + "   Scrolls " + stats.scrollsRead());
            if (stats.causeOfDeath() != null) {
                lines.add("Cause: " + stats.causeOfDeath());
            }
            lines.add("Seed " + session.seed());
        }
        lines.add("Press Enter to continue");
        double top = height / 2 - 50 - lines.size() * 10;
        g.setFill(Color.web("#000000", 0.75));
        g.fillRect(0, top, width, 70 + lines.size() * 20);
        g.setTextAlign(TextAlignment.CENTER);
        g.setFill(won ? Color.web("#f2c14e") : Color.web("#e05050"));
        g.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
        g.fillText(title, width / 2, top + 38);
        g.setFill(Color.web("#c8c6d4"));
        g.setFont(Font.font("Monospaced", 14));
        for (int i = 0; i < lines.size(); i++) {
            g.fillText(lines.get(i), width / 2, top + 66 + i * 20);
        }
        g.setTextAlign(TextAlignment.LEFT);
    }

    private String inventoryText() {
        List<GameSession.PackEntry> items = session.packEntries();
        StringBuilder text = new StringBuilder("Pack (").append(items.size()).append('/')
                .append(session.packCapacity()).append(')');
        for (int i = 0; i < items.size(); i++) {
            GameSession.PackEntry item = items.get(i);
            text.append('\n').append(Inventory.letterFor(i)).append(") ").append(item.description())
                    .append(item.equipmentSuffix());
        }
        return text.toString();
    }

    private void drawHelp(GraphicsContext g, double width, double height) {
        if (showSymbols) {
            SymbolLegend.draw(g, width, height);
        } else {
            drawPanel(g, width, height, "CONTROLS   (Tab: symbols, Esc: close)",
                    List.of(InputMapper.CONTROLS.split("\n")));
        }
    }

    private void drawJournal(GraphicsContext g, double width, double height) {
        List<String> lines = new ArrayList<>();
        for (Quest q : session.quests().started()) {
            String state = switch (q.state()) {
                case ACTIVE -> q.progress() + "/" + q.id().goal();
                case READY -> "ready to turn in";
                case COMPLETED -> "done";
                default -> "";
            };
            lines.add(q.id().title() + "  (" + q.id().giver() + ")  [" + state + "]");
            lines.add("    " + q.id().description());
            lines.add("");
        }
        if (lines.isEmpty()) {
            lines.add("No quests yet. Talk to the residents of the Atrium.");
        }
        drawPanel(g, width, height, "QUEST JOURNAL  (J or Esc to close)", lines);
    }

    private void drawPerks(GraphicsContext g, double width, double height) {
        List<String> lines = new ArrayList<>();
        List<Perk> offered = session.player().perkChoices();
        for (int i = 0; i < offered.size(); i++) {
            lines.add((i + 1) + ")  " + offered.get(i).title() + " - " + offered.get(i).description());
        }
        drawPanel(g, width, height, "CHOOSE A PERK  (press 1, 2 or 3)", lines);
    }

    private void drawShop(GraphicsContext g, double width, double height) {
        List<String> lines = new ArrayList<>();
        lines.add("Your gold: " + session.player().gold());
        lines.add("");
        if (shopSelling) {
            List<GameSession.PackEntry> pack = session.packEntries();
            for (int i = 0; i < pack.size(); i++) {
                GameSession.PackEntry item = pack.get(i);
                lines.add(Inventory.letterFor(i) + ")  " + item.description() + "   -> " + item.sellPrice() + " gold");
            }
        } else {
            List<GameSession.ShopOffer> stock = session.shopOffers();
            for (int i = 0; i < stock.size(); i++) {
                GameSession.ShopOffer item = stock.get(i);
                lines.add(Inventory.letterFor(i) + ")  " + item.description() + "   " + item.price() + " gold");
            }
        }
        String title = "CONOVER'S CURIO & CIRCUIT  -  " + (shopSelling ? "SELLING" : "BUYING")
                + "  (letter, Tab to switch, Esc to leave)";
        drawPanel(g, width, height, title, lines);
    }

    private void drawPanel(GraphicsContext g, double width, double height, String title, List<String> lines) {
        g.setFill(Color.web("#0b0b10", 0.93));
        g.fillRect(20, 20, width - 40, height - 40);
        g.setFill(Color.web("#f2c14e"));
        g.setFont(Font.font("Monospaced", FontWeight.BOLD, 15));
        g.fillText(title, 40, 50);
        g.setFill(Color.web("#c8c6d4"));
        g.setFont(Font.font("Monospaced", 13));
        for (int i = 0; i < lines.size(); i++) {
            g.fillText(lines.get(i), 40, 78 + i * 19);
        }
    }

    private static Label label(double size, boolean bold) {
        Label label = new Label();
        label.setFont(Font.font("Monospaced", bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        label.setStyle(TEXT_STYLE);
        return label;
    }
}
