package edu.towson.cosc442.halls7800.entity;

import edu.towson.cosc442.halls7800.combat.AttackProfile;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.world.Position;

/**
 * Doc, a black-and-gold tiger cub who joins the player after they rub the Atrium's tiger statue.
 * Doc follows the player between floors and attacks nearby monsters. Doc grows stronger as the
 * player levels up. Monsters may attack Doc; a knocked-out Doc retreats to the Atrium to rest
 * and rejoins the next time the player is there.
 */
public class Companion extends Creature {

    /** The cub's name. */
    public static final String NAME = "Doc";
    /** HP at player level 1. */
    public static final int BASE_HP = 20;
    /** Extra maximum HP per player level above 1. */
    public static final int HP_PER_LEVEL = 3;
    /** Turns between Doc's natural healing of 1 HP. */
    public static final int REGEN_INTERVAL = 5;

    private int level = 1;
    private int kills;
    private int turnsSinceRegen;

    /**
     * @param position where Doc appears
     */
    public Companion(Position position) {
        super(NAME, position, BASE_HP);
        setBaseAccuracy(6);
        setBaseEvasion(5);
        setBaseArmor(1);
    }

    /**
     * Grows Doc to match the player's level.
     *
     * @param playerLevel the player's current level
     */
    public void syncLevel(int playerLevel) {
        if (playerLevel > level) {
            int gained = playerLevel - level;
            level = playerLevel;
            setMaxHp(maxHp() + gained * HP_PER_LEVEL);
            heal(gained * HP_PER_LEVEL);
        }
    }

    /** @return the level Doc has grown to */
    public int level() {
        return level;
    }

    @Override
    public int accuracy() {
        return super.accuracy() + level / 2;
    }

    @Override
    public AttackProfile meleeAttack() {
        int bonus = level / 3;
        return AttackProfile.basic(accuracy(), 2 + bonus, 5 + bonus, 8, "bite");
    }

    @Override
    public String subject() {
        return NAME;
    }

    @Override
    public String object() {
        return NAME;
    }

    /** @return monsters Doc has defeated */
    public int kills() {
        return kills;
    }

    /** Records one monster defeated by Doc. */
    public void recordKill() {
        kills++;
    }

    /**
     * Heals 1 HP every {@link #REGEN_INTERVAL} of Doc's turns.
     */
    void regenerate() {
        turnsSinceRegen++;
        if (turnsSinceRegen >= REGEN_INTERVAL) {
            turnsSinceRegen = 0;
            heal(1);
        }
    }

    @Override
    protected int act(GameSession session) {
        return CompanionAi.takeTurn(this, session);
    }
}
