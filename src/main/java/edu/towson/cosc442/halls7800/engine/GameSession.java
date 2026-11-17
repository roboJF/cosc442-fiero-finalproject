package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.effect.EffectType;
import edu.towson.cosc442.halls7800.entity.Companion;
import edu.towson.cosc442.halls7800.entity.Creature;
import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.entity.MonsterType;
import edu.towson.cosc442.halls7800.entity.Npc;
import edu.towson.cosc442.halls7800.item.Potion;
import edu.towson.cosc442.halls7800.item.QuestItem;
import edu.towson.cosc442.halls7800.progression.AchievementTracker;
import edu.towson.cosc442.halls7800.progression.Perk;
import edu.towson.cosc442.halls7800.quest.Quest;
import edu.towson.cosc442.halls7800.quest.QuestLog;
import edu.towson.cosc442.halls7800.quest.Shop;
import edu.towson.cosc442.halls7800.world.Trap;
import edu.towson.cosc442.halls7800.entity.Player;
import edu.towson.cosc442.halls7800.item.Armor;
import edu.towson.cosc442.halls7800.item.Gold;
import edu.towson.cosc442.halls7800.item.Identification;
import edu.towson.cosc442.halls7800.item.Item;
import edu.towson.cosc442.halls7800.item.ItemGenerator;
import edu.towson.cosc442.halls7800.item.Wand;
import edu.towson.cosc442.halls7800.item.Weapon;
import edu.towson.cosc442.halls7800.util.RandomStream;
import edu.towson.cosc442.halls7800.util.Rng;
import edu.towson.cosc442.halls7800.util.SeedDeriver;
import edu.towson.cosc442.halls7800.world.FieldOfView;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.FloorView;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Terrain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * The central object of a run: owns the floors, the player, the turn scheduler and the message
 * log, and is the only entry point for player actions.
 *
 * <p>Typical use:
 * <pre>{@code
 * GameSession game = GameFactory.newGame(42L, "Ada", Difficulty.UNDERGRADUATE);
 * game.submit(new MoveCommand(Direction.EAST));
 * System.out.println(game.render());
 * }</pre>
 *
 * <p>The session has no dependency on any user-interface library. Everything it does is driven
 * by {@link #submit(Command)}, so the same seed and commands reproduce a run.
 */
public class GameSession {

    private final long seed;
    private final FloorSource floorSource;
    private final Map<Integer, Floor> floors = new TreeMap<>();
    private final Player player;
    private final TurnScheduler scheduler = new TurnScheduler();
    private final EventBus events = new EventBus();
    private final MessageLog log = new MessageLog(GameRules.MESSAGE_LOG_CAPACITY);
    private final Map<RandomStream, Rng> streams = new EnumMap<>(RandomStream.class);
    private final Map<Integer, FloorContents> contentsByDepth = new TreeMap<>();
    private final ItemGenerator itemGenerator = new ItemGenerator();
    private final Identification identification;
    private final QuestLog quests = new QuestLog();
    private final Difficulty difficulty;
    private final RunStats stats = new RunStats();
    private final List<String> recording = new ArrayList<>();
    private final AchievementTracker achievements = new AchievementTracker();
    private Shop shop;
    private boolean shopOpen;
    private Companion companion;
    private boolean companionResting;
    private Floor floor;
    private GamePhase phase = GamePhase.PLAYING;
    private int turn;
    private int deepestReached;
    private int sightRadius = GameRules.DEFAULT_SIGHT_RADIUS;

    /**
     * Starts a new run in the Atrium.
     *
     * @param seed the run seed; the same seed and commands reproduce the same run
     * @param floorSource supplies each floor on first visit
     * @param playerName the adventurer's name
     */
    public GameSession(long seed, FloorSource floorSource, String playerName) {
        this(seed, floorSource, playerName, Difficulty.UNDERGRADUATE);
    }

    /**
     * Starts a new run in the first floor of the source (normally the Atrium).
     *
     * @param seed the run seed; the same seed, difficulty and commands reproduce the same run
     * @param floorSource supplies each floor on first visit
     * @param playerName the adventurer's name
     * @param difficulty how hard the run is
     */
    public GameSession(long seed, FloorSource floorSource, String playerName, Difficulty difficulty) {
        this.seed = seed;
        this.difficulty = Objects.requireNonNull(difficulty, "difficulty");
        this.floorSource = Objects.requireNonNull(floorSource, "floorSource");
        this.identification = new Identification(rng(RandomStream.IDENTIFY));
        this.floor = obtainFloor(GameRules.HUB_DEPTH);
        this.player = new Player(playerName, floor.entrance());
        giveStartingKit();
        quests.get(Quest.Id.DIPLOMA).start();
        if (difficulty.bonusHp() != 0) {
            player.setMaxHp(player.maxHp() + difficulty.bonusHp());
            player.heal(player.maxHp());
        }
        for (int i = 0; i < difficulty.extraHealingPotions(); i++) {
            player.inventory().add(new Potion(Potion.Kind.HEALING, 1));
        }
        scheduler.add(player, 0);
        scheduleMonstersOnCurrentFloor();
        refreshVisibility();
        message(floorSource.welcomeMessage());
    }

    /**
     * Executes one player command. If the command used time, every other actor then takes its
     * turns until the player is next, and visibility is recalculated.
     *
     * @param command the action to attempt
     * @return the command's result; a rejected result means no time passed
     */
    public CommandResult submit(Command command) {
        Objects.requireNonNull(command, "command");
        if (phase != GamePhase.PLAYING) {
            return CommandResult.rejected("The run is over.");
        }
        CommandResult result;
        if (player.effects().has(EffectType.STUNNED)) {
            result = CommandResult.acted(player.actionCost(), "You are stunned and cannot act!");
        } else {
            recording.add(command.encode());
            result = command.execute(this);
        }
        if (result.message() != null) {
            message(result.message());
        }
        if (result.performed() && phase == GamePhase.PLAYING) {
            endPlayerTurn(result.timeCost());
        }
        return result;
    }

    /**
     * Finishes the player's turn: the player's effects tick, natural regeneration happens, then
     * every other actor acts until the player is next.
     */
    private void endPlayerTurn(int cost) {
        turn++;
        reportEffects(player, player.effects().tick(player, floor.terrainAt(player.position())));
        for (Wand wand : player.inventory().ofType(Wand.class)) {
            wand.tickRecharge();
        }
        String hungerMessage = player.tickHunger();
        if (hungerMessage != null) {
            message(hungerMessage);
        }
        boolean burning = player.effects().has(EffectType.BURNING);
        if (!player.effects().has(EffectType.POISONED) && !burning) {
            player.naturalRegeneration();
        }
        if (!player.isAlive()) {
            playerKilled(player.hunger() == Player.Hunger.STARVING ? "claimed by starvation"
                    : "overcome by " + (burning ? "flames" : "poison"));
            return;
        }
        scheduler.spend(player, cost);
        scheduler.runUntil(player, this);
        if (phase == GamePhase.PLAYING) {
            Hazards.tick(this);
        }
        refreshVisibility();
        quests.update(this);
        achievements.check(this);
        publish(new GameEvent(GameEvent.Type.TURN_ENDED, null, player.position(), floor.depth()));
    }

    // ----- creatures -----

    /** @return the living monsters on the current floor */
    public List<Monster> monsters() {
        return contents().monsters();
    }

    /** @return monsters and items on the current floor */
    public FloorContents contents() {
        return contentsOf(floor.depth());
    }

    private FloorContents contentsOf(int depth) {
        return contentsByDepth.computeIfAbsent(depth, d -> new FloorContents());
    }

    /**
     * @param p a position on the current floor
     * @return the monster standing there, or {@code null}
     */
    public Monster monsterAt(Position p) {
        return contents().monsterAt(p);
    }

    /**
     * @param p a position on the current floor
     * @return {@code true} if the player or a monster stands there
     */
    public boolean isOccupied(Position p) {
        return player.position().equals(p) || monsterAt(p) != null || contents().npcAt(p) != null
                || (companion != null && companion.position().equals(p));
    }

    /**
     * Adds a monster to the current floor and schedules it.
     *
     * @param monster the monster; its position must be free
     * @throws IllegalArgumentException if the position is occupied or impassable
     */
    public void addMonster(Monster monster) {
        if (isOccupied(monster.position()) || !floor.isPassable(monster.position())) {
            throw new IllegalArgumentException("Cannot place " + monster.name() + " at " + monster.position());
        }
        contents().addMonster(monster);
        scheduler.add(monster, rng(RandomStream.AI).nextInt(0, GameRules.NORMAL_ACTION_COST - 1));
    }

    /**
     * Removes a defeated monster. If the player defeated it, the player gains its experience.
     *
     * @param monster the monster
     * @param killer who defeated it, or {@code null} (e.g. it died of poison)
     */
    public void killMonster(Monster monster, Creature killer) {
        contents().removeMonster(monster);
        scheduler.remove(monster);
        signal(monster.isBoss() ? GameEvent.Type.BOSS_DEFEATED : GameEvent.Type.CREATURE_DIED, monster.position());
        if (monster.stolenGold() > 0) {
            contents().addItem(monster.position(), new Gold(monster.stolenGold()));
            messageIfVisible(monster.position(), "Your stolen gold spills onto the floor.");
        }
        if (monster.type() == MonsterType.HOLLOW_REGISTRAR) {
            Position altar = floor.positionsOf(Terrain.ALTAR).stream().findFirst().orElse(monster.position());
            contents().addItem(altar, new QuestItem(QuestItem.Kind.DIPLOMA, 1));
            message("The Hollow Registrar dissolves into a drift of blank forms. On the altar, the Diploma of "
                    + "Ascendance shines in black and gold.");
        } else if (monster.isBoss()) {
            message(monster.subject() + " falls! The seal on the stairs below is broken.");
        }
        if (monster.isBoss()) {
            stats.recordBoss();
            achievements.onBossDefeated(this);
        }
        boolean byCompanion = killer instanceof Companion;
        if (byCompanion) {
            companion.recordKill();
        }
        if (killer != null && (killer.isPlayer() || byCompanion)) {
            message((byCompanion ? Companion.NAME + " defeats " : "You defeat ") + monster.object() + ".");
            player.recordKill();
            stats.recordKill(monster.type());
            quests.onMonsterKilled(monster.type());
            for (String text : player.gainExperience(monster.type().experience(), rng(RandomStream.EFFECTS))) {
                message(text);
                if (text.startsWith("Welcome to level")) {
                    signal(GameEvent.Type.LEVEL_UP, player.position());
                }
            }
            Item drop = itemGenerator.monsterDrop(floor.depth(), rng(RandomStream.LOOT));
            if (drop == null && player.hasPerk(Perk.SCAVENGER)) {
                drop = itemGenerator.monsterDrop(floor.depth(), rng(RandomStream.LOOT));
            }
            if (drop != null) {
                contents().addItem(monster.position(), drop);
                messageIfVisible(monster.position(), monster.subject() + " " + monster.verb("drop") + " "
                        + drop.describe(identification) + ".");
            }
        } else {
            messageIfVisible(monster.position(), monster.subject() + " " + monster.verb("collapse") + ".");
        }
    }

    /**
     * Ends the run with the player's death.
     *
     * @param cause how the player died, e.g. "slain by the giant rat"
     */
    public void playerKilled(String cause) {
        stats.setCauseOfDeath(cause);
        endGame(GamePhase.DEAD, "You were " + cause + " on depth " + floor.depth() + ". Your quest ends here.");
    }

    /**
     * Moves a creature to an adjacent or distant tile and applies the terrain's effects (wading
     * into water soaks the creature and puts out flames).
     *
     * @param creature the creature
     * @param to destination; legality is the caller's responsibility
     */
    public void moveCreature(Creature creature, Position to) {
        creature.setPosition(to);
        Hazards.enter(this, creature, to);
        Terrain terrain = floor.terrainAt(to);
        if (terrain == Terrain.WATER) {
            boolean wasBurning = creature.effects().has(EffectType.BURNING);
            creature.applyEffect(EffectType.WET, 5, 0);
            if (wasBurning) {
                messageIfVisible(to, creature.subject() + " " + creature.verb("douse") + " the flames.");
            }
        }
        if (creature.isPlayer()) {
            collectGoldAndLook(to);
            publish(new GameEvent(GameEvent.Type.PLAYER_MOVED, null, to, floor.depth()));
            Trap trap = contents().trapAt(to);
            if (trap != null) {
                Traps.trigger(this, trap);
            } else {
                Traps.passiveSearch(this);
            }
        }
    }

    /** Scoops up any gold at the player's feet and mentions what else lies there. */
    private void collectGoldAndLook(Position at) {
        for (Item item : List.copyOf(contents().itemsAt(at))) {
            if (item instanceof Gold gold) {
                contents().removeItem(at, gold);
                player.addGold(gold.quantity());
                signal(GameEvent.Type.GOLD_PICKED_UP, at);
                stats.addGoldCollected(gold.quantity());
                message("You pick up " + gold.quantity() + " gold.");
            }
        }
        List<Item> rest = contents().itemsAt(at);
        if (rest.size() == 1) {
            message("You see " + rest.get(0).describe(identification) + " here.");
        } else if (rest.size() > 1) {
            message("Several items lie here.");
        }
    }

    // ----- items -----

    /**
     * @param p a position on the current floor
     * @return items lying there (empty if none)
     */
    public List<Item> itemsAt(Position p) {
        return contents().itemsAt(p);
    }

    /**
     * Puts an item on the current floor.
     *
     * @param p where
     * @param item the item
     */
    public void placeItem(Position p, Item item) {
        contents().addItem(p, item);
    }

    /**
     * Describes an item as the player currently perceives it, e.g. "an azure potion".
     *
     * @param item the item
     * @return the description
     */
    public String describe(Item item) {
        return item.describe(identification);
    }

    /** @return what the player has identified this run */
    public Identification identification() {
        return identification;
    }

    /** @return the generator used for loot */
    public ItemGenerator itemGenerator() {
        return itemGenerator;
    }

    // ----- the companion -----

    /** @return Doc the tiger cub, or {@code null} if Doc has not joined (or is resting) */
    public Companion companion() {
        return companion;
    }

    /** @return {@code true} if Doc was knocked out and is resting in the Atrium */
    public boolean isCompanionResting() {
        return companionResting;
    }

    /**
     * Brings Doc into the party beside the player. Does nothing if Doc is already here or resting.
     *
     * @return {@code true} if Doc joined
     */
    public boolean recruitCompanion() {
        if (companion != null || companionResting) {
            return false;
        }
        companion = new Companion(player.position());
        companion.syncLevel(player.level());
        placeCompanionNear(player.position());
        return true;
    }

    /** Doc has been knocked out: Doc leaves the floor and waits in the Atrium. */
    public void companionKnockedOut() {
        if (companion == null) {
            return;
        }
        scheduler.remove(companion);
        companion = null;
        companionResting = true;
        message(Companion.NAME + " yelps and scampers back up to the Atrium to rest.");
    }

    private void placeCompanionNear(Position center) {
        scheduler.remove(companion);
        Position spot = null;
        for (Position p : center.neighbors()) {
            if (floor.isPassable(p) && monsterAt(p) == null && contents().npcAt(p) == null && !p.equals(center)) {
                spot = p;
                break;
            }
        }
        if (spot == null) {
            companion = null;
            companionResting = true;
            message(Companion.NAME + " can't squeeze in beside you and heads back to the Atrium.");
            return;
        }
        companion.setPosition(spot);
        scheduler.add(companion, GameRules.NORMAL_ACTION_COST / 2);
    }

    // ----- quests, shop, perks, bosses -----

    /** @return achievements earned this run */
    public AchievementTracker achievements() {
        return achievements;
    }

    /** @return the run's difficulty */
    public Difficulty difficulty() {
        return difficulty;
    }

    /** @return statistics collected so far */
    public RunStats stats() {
        return stats;
    }

    /**
     * Every action the player has taken, as tokens (see {@link CommandCodec}). Replaying these
     * tokens on a new session with the same seed, floor source, name and difficulty reproduces
     * this run exactly; saved games work this way.
     *
     * @return the recorded tokens, oldest first
     */
    public List<String> recording() {
        return Collections.unmodifiableList(recording);
    }

    /** @return {@code true} if this is the Orientation tutorial rather than a real run */
    public boolean isTutorial() {
        return floorSource.isTutorial();
    }

    /** @return the run's quests */
    public QuestLog quests() {
        return quests;
    }

    /**
     * @param depth a visited depth
     * @return that floor, or {@code null} if it has not been visited
     */
    public Floor visitedFloor(int depth) {
        return floors.get(depth);
    }

    /**
     * @param p a position on the current floor
     * @return the friendly NPC standing there, or {@code null}
     */
    public Npc npcAt(Position p) {
        return contents().npcAt(p);
    }

    /** @return a living boss on the current floor, or {@code null} */
    public Monster livingBoss() {
        for (Monster m : contents().monsters()) {
            if (m.isBoss() && m.isAlive()) {
                return m;
            }
        }
        return null;
    }

    /** @return the Atrium shop, stocked on first use */
    public Shop shop() {
        if (shop == null) {
            shop = new Shop(itemGenerator, SeedDeriver.rngFor(seed, SeedDeriver.RUN_WIDE, RandomStream.SHOP));
        }
        return shop;
    }

    /** Opens the shop screen (after talking to the shopkeeper). */
    public void openShop() {
        shopOpen = true;
    }

    /** Closes the shop screen. */
    public void closeShop() {
        if (shopOpen) {
            recording.add("SHOP:CLOSE");
        }
        shopOpen = false;
    }

    /** @return {@code true} while the shop screen is open */
    public boolean isShopOpen() {
        return shopOpen;
    }

    /**
     * Buys an item from the shop. Shopping takes no game time.
     *
     * @param index position in the shop's stock
     * @return what happened
     */
    public String buy(int index) {
        if (!shopOpen) {
            return "The shop is closed.";
        }
        recording.add("BUY:" + index);
        String text = shop().buy(index, player, identification);
        message(text);
        return text;
    }

    /**
     * Sells a pack item to the shop. Shopping takes no game time.
     *
     * @param slot pack slot
     * @return what happened
     */
    public String sell(int slot) {
        if (!shopOpen) {
            return "The shop is closed.";
        }
        recording.add("SELL:" + slot);
        Item item = player.inventory().get(slot);
        String text = item == null ? "You don't have that item." : shop().sell(item, player, identification);
        message(text);
        return text;
    }

    /**
     * Takes one of the perks currently on offer. Choosing takes no game time.
     *
     * @param perk the perk
     * @return {@code true} if granted
     */
    public boolean choosePerk(Perk perk) {
        boolean granted = player.choosePerk(perk, rng(RandomStream.EFFECTS));
        if (granted) {
            recording.add("PERK:" + perk.name());
            message("You gain the " + perk.title() + " perk: " + perk.description());
        }
        return granted;
    }

    private void giveStartingKit() {
        for (Item item : itemGenerator.startingKit()) {
            player.inventory().add(item);
            identification.identify(item);
            if (item instanceof Weapon w && player.weapon() == null) {
                player.setWeapon(w);
            } else if (item instanceof Armor a && player.wornArmor() == null) {
                player.setWornArmor(a);
            }
        }
    }

    /**
     * Logs effect messages about a creature if the player is that creature or can see it.
     *
     * @param creature whose effects ticked
     * @param messages the messages
     */
    public void reportEffects(Creature creature, List<String> messages) {
        for (String text : messages) {
            if (creature.isPlayer()) {
                message(text);
            } else {
                messageIfVisible(creature.position(), text);
            }
        }
    }

    /**
     * Logs a message only if the player can see the given position.
     *
     * @param where where the event happened
     * @param text the message
     */
    public void messageIfVisible(Position where, String text) {
        if (floor.isVisible(where)) {
            message(text);
        }
    }

    private void scheduleMonstersOnCurrentFloor() {
        for (Monster m : contents().monsters()) {
            scheduler.add(m, rng(RandomStream.AI).nextInt(0, GameRules.NORMAL_ACTION_COST - 1));
        }
    }

    /**
     * Moves the player to another floor, creating it if it has not been visited. Going down
     * arrives at the floor's entrance; going up arrives at its down staircase.
     *
     * @param targetDepth the depth to travel to
     * @throws IllegalArgumentException if the depth is outside the dungeon
     */
    public void changeFloor(int targetDepth) {
        if (targetDepth < GameRules.HUB_DEPTH || targetDepth > floorSource.deepestDepth()) {
            throw new IllegalArgumentException("No floor at depth " + targetDepth);
        }
        boolean descending = targetDepth > floor.depth();
        Floor target = obtainFloor(targetDepth);
        scheduler.clearExcept(player);
        floor = target;
        Position arrival = descending || target.exit() == null ? target.entrance() : target.exit();
        player.setPosition(arrival);
        scheduleMonstersOnCurrentFloor();
        if (companionResting && targetDepth == GameRules.HUB_DEPTH) {
            companionResting = false;
            companion = new Companion(arrival);
            companion.syncLevel(player.level());
            message(Companion.NAME + " bounds over from the tiger statue, fully rested!");
        }
        if (companion != null) {
            placeCompanionNear(arrival);
        }
        deepestReached = Math.max(deepestReached, targetDepth);
        refreshVisibility();
        String verb = descending ? "descend to" : "climb back up to";
        message("You " + verb + " " + target.name() + ".");
        publish(new GameEvent(GameEvent.Type.FLOOR_CHANGED, null, arrival, targetDepth));
    }

    /**
     * Ends the run.
     *
     * @param outcome {@link GamePhase#DEAD} or {@link GamePhase#VICTORY}
     * @param text closing message
     */
    public void endGame(GamePhase outcome, String text) {
        if (outcome == GamePhase.PLAYING) {
            throw new IllegalArgumentException("endGame needs a final phase");
        }
        phase = outcome;
        message(text);
        achievements.check(this);
        publish(new GameEvent(GameEvent.Type.GAME_OVER, text, player.position(), floor.depth()));
    }

    /**
     * Recomputes what the player can see and updates explored tiles.
     */
    public void refreshVisibility() {
        int radius = player.effects().has(EffectType.BLINDED) ? 1 : sightRadius;
        Set<Position> visible = FieldOfView.compute(floor, player.position(), radius);
        floor.updateVisibility(visible);
    }

    /**
     * Adds a message to the log and publishes it as a {@link GameEvent.Type#MESSAGE} event.
     *
     * @param text the message
     */
    public void message(String text) {
        log.add(turn, text);
        publish(new GameEvent(GameEvent.Type.MESSAGE, text, null, floor == null ? 0 : floor.depth()));
    }

    /**
     * Publishes a signal-only event (no message) at a position on the current floor.
     *
     * @param type the kind of event
     * @param where where it happened, or {@code null}
     */
    public void signal(GameEvent.Type type, Position where) {
        publish(new GameEvent(type, null, where, floor.depth()));
    }

    /**
     * Publishes an event to all listeners.
     *
     * @param event the event
     */
    public void publish(GameEvent event) {
        events.publish(event);
    }

    /**
     * Returns the run-wide random generator for a purpose, creating it from the run seed on first
     * use. All game rules that need chance (combat, AI, loot, effects) must draw from here.
     *
     * @param stream the purpose
     * @return the generator for that purpose
     */
    public Rng rng(RandomStream stream) {
        return streams.computeIfAbsent(stream, s -> SeedDeriver.rngFor(seed, SeedDeriver.RUN_WIDE, s));
    }

    /**
     * Replaces the generator used for a purpose.
     *
     * @param stream the purpose
     * @param rng the generator to use from now on
     */
    public void setRng(RandomStream stream, Rng rng) {
        streams.put(stream, Objects.requireNonNull(rng, "rng"));
    }

    private Floor obtainFloor(int depth) {
        Floor existing = floors.get(depth);
        if (existing != null) {
            return existing;
        }
        Floor created = floorSource.createFloor(depth);
        floors.put(depth, created);
        contentsByDepth.put(depth, floorSource.populate(created));
        return created;
    }

    /** @return an ASCII picture of the explored part of the current floor */
    public String render() {
        return AsciiRenderer.render(floor, player.position(), true);
    }

    /** @return the run seed */
    public long seed() {
        return seed;
    }

    /** @return the player */
    public Player player() {
        return player;
    }

    /**
     * Returns the current floor for code that changes the game state (commands, monsters).
     * User-interface code should use {@link #currentFloor()} instead.
     *
     * @return the mutable current floor
     */
    public Floor floor() {
        return floor;
    }

    /** @return a read-only view of the current floor */
    public FloorView currentFloor() {
        return floor;
    }

    /** @return the current depth */
    public int depth() {
        return floor.depth();
    }

    /** @return the deepest depth the player has reached */
    public int deepestReached() {
        return deepestReached;
    }

    /** @return the deepest depth that exists in this dungeon */
    public int deepestDepth() {
        return floorSource.deepestDepth();
    }

    /** @return depths visited so far, in increasing order */
    public Set<Integer> visitedDepths() {
        return Collections.unmodifiableSet(floors.keySet());
    }

    /** @return whether the run is in progress, lost or won */
    public GamePhase phase() {
        return phase;
    }

    /** @return number of completed player turns */
    public int turn() {
        return turn;
    }

    /** @return the turn scheduler */
    public TurnScheduler scheduler() {
        return scheduler;
    }

    /** @return the event bus that listeners subscribe to */
    public EventBus events() {
        return events;
    }

    /** @return the message log */
    public MessageLog messages() {
        return log;
    }

    /** @return the player's sight radius in tiles */
    public int sightRadius() {
        return sightRadius;
    }

    /**
     * Changes the player's sight radius and refreshes visibility.
     *
     * @param radius new radius (0 or more)
     */
    public void setSightRadius(int radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("radius must be non-negative");
        }
        sightRadius = radius;
        refreshVisibility();
    }
}
