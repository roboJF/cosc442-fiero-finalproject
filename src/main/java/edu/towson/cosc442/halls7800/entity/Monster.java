package edu.towson.cosc442.halls7800.entity;

import edu.towson.cosc442.halls7800.combat.AttackProfile;
import edu.towson.cosc442.halls7800.combat.DamageType;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.world.Position;
import java.util.Objects;

/**
 * A hostile creature. Its statistics come from its {@link MonsterType}; its decisions come from
 * {@link MonsterAi}, which is driven by the monster's current {@link State}.
 */
public class Monster extends Creature {

    /** What the monster is currently doing. */
    public enum State {
        /** Dormant until it notices the player. */
        SLEEPING,
        /** Roaming the floor without a target. */
        WANDERING,
        /** Chasing (or shooting at) the player. */
        HUNTING,
        /** Running away to recover. */
        FLEEING
    }

    private final MonsterType type;
    private State state;
    private Position lastKnownPlayerPosition;
    private Position wanderTarget;
    private int turnsFleeing;
    private int fearTurns;
    private int bossPhase;
    private int generation;
    private int stolenGold;

    /**
     * @param type the kind of monster
     * @param position starting position
     * @param asleep whether it starts asleep
     */
    public Monster(MonsterType type, Position position, boolean asleep) {
        super(type.displayName(), position, type.maxHp());
        this.type = Objects.requireNonNull(type, "type");
        setBaseAccuracy(type.accuracy());
        setBaseEvasion(type.evasion());
        setBaseArmor(type.armor());
        setBaseActionCost(type.actionCost());
        this.state = asleep || type.has(MonsterTrait.AMBUSH) ? State.SLEEPING : State.WANDERING;
    }

    /** @return the kind of monster */
    public MonsterType type() {
        return type;
    }

    /** @return current behavior state */
    public State state() {
        return state;
    }

    /**
     * Changes the behavior state.
     *
     * @param state the new state
     */
    public void setState(State state) {
        if (state != State.FLEEING) {
            turnsFleeing = 0;
        }
        this.state = Objects.requireNonNull(state, "state");
    }

    /** Wakes the monster (if asleep) and makes it hunt the player. */
    public void alert() {
        if (state == State.SLEEPING || state == State.WANDERING) {
            setState(State.HUNTING);
        }
    }

    @Override
    public boolean isAsleep() {
        return state == State.SLEEPING;
    }

    @Override
    public AttackProfile meleeAttack() {
        return type.meleeProfile();
    }

    @Override
    public AttackProfile rangedAttack() {
        return type.rangedProfile();
    }

    @Override
    public boolean isImmuneTo(DamageType damageType) {
        return (damageType == DamageType.FIRE && type.has(MonsterTrait.FIRE_IMMUNE))
                || (damageType == DamageType.POISON && type.has(MonsterTrait.POISON_IMMUNE));
    }

    /** @return where the monster last saw the player, or {@code null} */
    public Position lastKnownPlayerPosition() {
        return lastKnownPlayerPosition;
    }

    /** @param position where the player was last seen */
    public void setLastKnownPlayerPosition(Position position) {
        this.lastKnownPlayerPosition = position;
    }

    /** @return where the monster is wandering to, or {@code null} */
    public Position wanderTarget() {
        return wanderTarget;
    }

    /** @param target a new wander destination */
    public void setWanderTarget(Position target) {
        this.wanderTarget = target;
    }

    /**
     * Makes the monster flee in terror for a number of turns, regardless of its health.
     *
     * @param turns how long the fear lasts
     */
    public void frighten(int turns) {
        setState(State.FLEEING);
        fearTurns = Math.max(fearTurns, turns);
    }

    /** @return turns of magical fear remaining */
    public int fearTurns() {
        return fearTurns;
    }

    /** Counts down magical fear by one turn. */
    public void countDownFear() {
        if (fearTurns > 0) {
            fearTurns--;
        }
    }

    /** @return how many times this monster's line has split */
    public int generation() {
        return generation;
    }

    /** @param generation split generation for a newly created copy */
    public void setGeneration(int generation) {
        this.generation = generation;
    }

    /** Records one more split. */
    public void incrementGeneration() {
        generation++;
    }

    /** @return gold this monster has stolen (dropped when it is defeated) */
    public int stolenGold() {
        return stolenGold;
    }

    /** @param amount gold the monster now carries */
    public void setStolenGold(int amount) {
        stolenGold = amount;
    }

    /** @return how many boss phase changes have happened (0 at full health) */
    public int bossPhase() {
        return bossPhase;
    }

    /** Advances the boss to its next phase. */
    public void advanceBossPhase() {
        bossPhase++;
    }

    /** @return {@code true} for unique bosses */
    public boolean isBoss() {
        return type.has(MonsterTrait.BOSS);
    }

    /** @return consecutive turns spent fleeing */
    public int turnsFleeing() {
        return turnsFleeing;
    }

    /** Counts one more turn spent fleeing. */
    public void countFleeingTurn() {
        turnsFleeing++;
    }

    @Override
    protected int act(GameSession session) {
        return MonsterAi.takeTurn(this, session);
    }
}
