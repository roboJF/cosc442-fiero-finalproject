package edu.towson.cosc442.halls7800.gen;

import edu.towson.cosc442.halls7800.engine.Difficulty;
import edu.towson.cosc442.halls7800.engine.FloorContents;
import edu.towson.cosc442.halls7800.engine.FloorSource;
import edu.towson.cosc442.halls7800.engine.GameRules;
import edu.towson.cosc442.halls7800.util.RandomStream;
import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.util.SeedDeriver;
import edu.towson.cosc442.halls7800.util.SplitMixRng;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.FloorParser;
import edu.towson.cosc442.halls7800.world.Zone;

/**
 * The standard dungeon: the hand-drawn Atrium at depth 0, generated floors from depth 1 to 9,
 * and the hand-drawn Sanctum at depth 10.
 *
 * <p>Each generated floor's seed is derived from the run seed and the depth, so a floor is the
 * same whenever it is first visited, regardless of what happened on earlier floors.
 */
public class ProceduralFloorSource implements FloorSource {

    private final long runSeed;
    private final Difficulty difficulty;
    private final DungeonGenerator generator = new DungeonGenerator();
    private final MonsterPopulator populator = new MonsterPopulator();
    private final LootPlacer lootPlacer = new LootPlacer();
    private final TrapPlacer trapPlacer = new TrapPlacer();

    /**
     * @param runSeed the seed of the whole run
     */
    public ProceduralFloorSource(long runSeed) {
        this(runSeed, Difficulty.UNDERGRADUATE);
    }

    /**
     * @param runSeed the seed of the whole run
     * @param difficulty adjusts monster numbers and toughness
     */
    public ProceduralFloorSource(long runSeed, Difficulty difficulty) {
        this.runSeed = runSeed;
        this.difficulty = difficulty;
    }

    @Override
    public Floor createFloor(int depth) {
        if (depth == GameRules.HUB_DEPTH) {
            return FloorParser.parse(Zone.ATRIUM.title(), depth, FixedLayouts.ATRIUM);
        }
        if (depth == GameRules.FINAL_DEPTH) {
            return FloorParser.parse(Zone.SANCTUM.title(), depth, FixedLayouts.SANCTUM);
        }
        if (depth < 0 || depth > GameRules.FINAL_DEPTH) {
            throw new IllegalArgumentException("No floor at depth " + depth);
        }
        long seed = SeedDeriver.derive(runSeed, depth, RandomStream.LAYOUT);
        return generator.generate(depth, seed, true);
    }

    /**
     * Fills a new floor. The Atrium gets its residents and the Sanctum its guardian; generated
     * floors get monsters, a zone boss on the last floor of each zone, Warden Deng on the first
     * floor of the Clockwork Stacks, loot and traps, each from the floor's own random stream.
     *
     * @param floor a newly created floor
     * @return its starting monsters and items
     */
    @Override
    public FloorContents populate(Floor floor) {
        FloorContents contents = new FloorContents();
        int depth = floor.depth();
        if (depth == GameRules.HUB_DEPTH) {
            ResidentPlacer.placeAtriumResidents(contents);
            return contents;
        }
        if (depth == GameRules.FINAL_DEPTH) {
            ResidentPlacer.placeRegistrar(floor, contents);
            scaleMonsters(contents);
            return contents;
        }
        long monsterSeed = SeedDeriver.derive(runSeed, depth, RandomStream.POPULATION);
        for (Monster m : populator.populate(floor, new SplitMixRng(monsterSeed), difficulty.extraMonsterGroups())) {
            contents.addMonster(m);
        }
        ResidentPlacer.placeZoneBoss(floor, contents);
        scaleMonsters(contents);
        if (depth == ResidentPlacer.WARDEN_DEPTH) {
            ResidentPlacer.placeWarden(floor, contents);
        }
        long lootSeed = SeedDeriver.derive(runSeed, depth, RandomStream.LOOT);
        lootPlacer.place(floor, contents, new SplitMixRng(lootSeed));
        long trapSeed = SeedDeriver.derive(runSeed, depth, RandomStream.TRAPS);
        trapPlacer.place(floor, contents, new SplitMixRng(trapSeed));
        return contents;
    }

    private void scaleMonsters(FloorContents contents) {
        if (difficulty.monsterHpPercent() == 100) {
            return;
        }
        for (Monster m : contents.monsters()) {
            m.setMaxHp(Math.max(1, m.maxHp() * difficulty.monsterHpPercent() / 100));
            m.heal(m.maxHp());
        }
    }

    @Override
    public int deepestDepth() {
        return GameRules.FINAL_DEPTH;
    }

    /** @return the run seed */
    public long runSeed() {
        return runSeed;
    }
}
