package edu.towson.cosc442.halls7800.entity;

import edu.towson.cosc442.halls7800.combat.AttackProfile;
import edu.towson.cosc442.halls7800.combat.DamageType;
import edu.towson.cosc442.halls7800.effect.EffectManager;
import edu.towson.cosc442.halls7800.effect.EffectType;
import edu.towson.cosc442.halls7800.engine.Actor;
import edu.towson.cosc442.halls7800.engine.GameRules;
import edu.towson.cosc442.halls7800.world.Position;
import java.util.Objects;

/**
 * A living thing that occupies a tile on a floor: the player, monsters and NPCs.
 *
 * <p>Creatures have hit points, combat ratings (accuracy, evasion, armor), a base action cost
 * that sets their speed, and a set of status effects. Subclasses supply their attacks.
 */
public abstract class Creature extends Actor {

    private final String name;
    private Position position;
    private int maxHp;
    private int hp;
    private int baseAccuracy;
    private int baseEvasion;
    private int baseArmor;
    private int baseActionCost = GameRules.NORMAL_ACTION_COST;
    private final EffectManager effects = new EffectManager();

    /**
     * @param name display name (lower case for monsters, e.g. "giant rat")
     * @param position starting position
     * @param maxHp maximum (and starting) hit points
     */
    protected Creature(String name, Position position, int maxHp) {
        this.name = Objects.requireNonNull(name, "name");
        this.position = Objects.requireNonNull(position, "position");
        if (maxHp < 1) {
            throw new IllegalArgumentException("maxHp must be positive");
        }
        this.maxHp = maxHp;
        this.hp = maxHp;
    }

    /** @return display name */
    public String name() {
        return name;
    }

    /** @return the tile this creature occupies */
    public Position position() {
        return position;
    }

    /**
     * Moves the creature. Callers are responsible for checking that the move is legal; during play
     * use {@code GameSession.moveCreature} so that terrain effects apply.
     *
     * @param position the new position
     */
    public void setPosition(Position position) {
        this.position = Objects.requireNonNull(position, "position");
    }

    // ----- hit points -----

    /** @return current hit points */
    public int hp() {
        return hp;
    }

    /** @return maximum hit points */
    public int maxHp() {
        return maxHp;
    }

    /**
     * Changes maximum HP, keeping current HP within the new limit.
     *
     * @param maxHp new maximum (at least 1)
     */
    public void setMaxHp(int maxHp) {
        if (maxHp < 1) {
            throw new IllegalArgumentException("maxHp must be positive");
        }
        this.maxHp = maxHp;
        this.hp = Math.min(hp, maxHp);
    }

    /** @return {@code true} while HP is above zero */
    public boolean isAlive() {
        return hp > 0;
    }

    @Override
    public boolean isActive() {
        return isAlive();
    }

    /**
     * Removes hit points.
     *
     * @param amount damage to take (negative values are treated as 0)
     * @return HP actually lost
     */
    public int takeDamage(int amount) {
        int lost = Math.min(hp, Math.max(0, amount));
        hp -= lost;
        return lost;
    }

    /**
     * Restores hit points, up to the maximum.
     *
     * @param amount HP to restore (negative values are treated as 0)
     * @return HP actually restored
     */
    public int heal(int amount) {
        int gained = Math.min(maxHp - hp, Math.max(0, amount));
        hp += gained;
        return gained;
    }

    // ----- combat ratings -----

    /** @param value base accuracy before equipment and effects */
    protected void setBaseAccuracy(int value) {
        baseAccuracy = value;
    }

    /** @param value base evasion before equipment and effects */
    protected void setBaseEvasion(int value) {
        baseEvasion = value;
    }

    /** @param value base armor before equipment */
    protected void setBaseArmor(int value) {
        baseArmor = value;
    }

    /** @param ticks time a normal action takes for this creature */
    protected void setBaseActionCost(int ticks) {
        if (ticks < 1) {
            throw new IllegalArgumentException("action cost must be positive");
        }
        baseActionCost = ticks;
    }

    /** @return accuracy rating, before effect modifiers */
    public int accuracy() {
        return baseAccuracy;
    }

    /** @return evasion rating; helpless creatures cannot dodge */
    public int evasion() {
        return isHelpless() ? 0 : baseEvasion;
    }

    /** @return armor rating: physical damage is reduced by a roll from 0 to this value */
    public int armor() {
        return baseArmor;
    }

    /** @return ticks this creature's next ordinary action takes, including haste and slow */
    public int actionCost() {
        return effects.modifyActionCost(baseActionCost);
    }

    /** @return ticks an ordinary action takes for this creature, ignoring effects */
    public int baseActionCost() {
        return baseActionCost;
    }

    /** @return the creature's close-range attack */
    public abstract AttackProfile meleeAttack();

    /** @return the creature's ranged attack, or {@code null} if it has none */
    public AttackProfile rangedAttack() {
        return null;
    }

    /**
     * @param type a damage type
     * @return {@code true} if this creature takes no damage of that type
     */
    public boolean isImmuneTo(DamageType type) {
        return false;
    }

    /** @return {@code true} if the creature is asleep */
    public boolean isAsleep() {
        return false;
    }

    /** @return {@code true} if the creature cannot defend itself (asleep or stunned) */
    public boolean isHelpless() {
        return isAsleep() || effects.has(EffectType.STUNNED);
    }

    // ----- effects -----

    /** @return this creature's status effects */
    public EffectManager effects() {
        return effects;
    }

    /**
     * Applies a status effect, respecting immunities (fire-immune creatures do not burn,
     * poison-immune creatures are not poisoned).
     *
     * @param type the effect
     * @param duration turns
     * @param magnitude strength
     * @return what happened
     */
    public EffectManager.Outcome applyEffect(EffectType type, int duration, int magnitude) {
        if ((type == EffectType.BURNING && isImmuneTo(DamageType.FIRE))
                || (type == EffectType.POISONED && isImmuneTo(DamageType.POISON))) {
            return EffectManager.Outcome.RESISTED;
        }
        return effects.apply(type, duration, magnitude);
    }

    // ----- message grammar -----

    /** @return {@code true} for the player character */
    public boolean isPlayer() {
        return false;
    }

    /** @return the creature as a sentence subject: "You" or "The giant rat" */
    public String subject() {
        return isPlayer() ? "You" : "The " + name;
    }

    /** @return the creature as an object: "you" or "the giant rat" */
    public String object() {
        return isPlayer() ? "you" : "the " + name;
    }

    /**
     * Conjugates a verb for this creature as subject: "you hit" but "the rat hits".
     *
     * @param base the verb's base form, e.g. "hit", "miss", "are"
     * @return the agreeing form
     */
    public String verb(String base) {
        if (isPlayer()) {
            return base;
        }
        int space = base.indexOf(' ');
        if (space > 0) {
            return verb(base.substring(0, space)) + base.substring(space);
        }
        switch (base) {
            case "are":
                return "is";
            case "have":
                return "has";
            default:
                break;
        }
        if (base.endsWith("s") || base.endsWith("sh") || base.endsWith("ch") || base.endsWith("x")) {
            return base + "es";
        }
        return base + "s";
    }

    @Override
    public String toString() {
        return name + "@" + position + " " + hp + "/" + maxHp;
    }
}
