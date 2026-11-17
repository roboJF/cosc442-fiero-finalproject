package edu.towson.cosc442.halls7800.entity;

import edu.towson.cosc442.halls7800.combat.AttackProfile;
import edu.towson.cosc442.halls7800.combat.DamageType;
import edu.towson.cosc442.halls7800.effect.EffectType;
import java.util.EnumSet;
import java.util.Set;

/**
 * The monster roster. Each zone of the Halls has its own theme: the Undercroft is infested with
 * bugs, the Clockwork Stacks are haunted by data-structure and concurrency horrors, and the Ember
 * Vaults are patrolled by the creatures of the cyber-security wars.
 *
 * <p>Columns: name, glyph, color, HP, accuracy, evasion, damage range, armor, action cost
 * (100 = normal speed, lower is faster), experience, depth range, spawn weight, traits, damage
 * type, on-hit effect (type, percent, duration, magnitude), ranged reach, description.
 */
public enum MonsterType {

    // ----- The Undercroft (depths 1-3): bugs -----
    GIANT_RAT("giant rat", 'r', "#a08a6a", 6, 2, 3, 1, 3, 0, 100, 2, 1, 2, 10,
            traits(), DamageType.PHYSICAL, null, 0, 0, 0, 0, "bite",
            "A rat the size of a terrier, fat on dropped snacks."),
    NULL_POINTER_IMP("null-pointer imp", 'i', "#c9a0ff", 4, 3, 5, 1, 2, 0, 75, 3, 2, 3, 8,
            traits(MonsterTrait.ERRATIC, MonsterTrait.PACK), DamageType.PHYSICAL, null, 0, 0, 0, 0, "jab",
            "It points at things that are not there, very quickly."),
    OFF_BY_ONE_GOBLIN("off-by-one goblin", 'g', "#7fb35a", 9, 3, 2, 1, 4, 1, 100, 4, 1, 4, 12,
            traits(), DamageType.PHYSICAL, null, 0, 0, 0, 0, "club",
            "Always one step too far, or one step short."),
    SYNTAX_ERROR_SPRITE("syntax-error sprite", 's', "#ff7a7a", 7, 5, 5, 1, 3, 0, 100, 5, 2, 4, 8,
            traits(MonsterTrait.FLEES), DamageType.ARCANE, EffectType.CONFUSED, 25, 3, 0, 0, "zap",
            "A crackling spark of unmatched brackets. Its touch scrambles your thoughts."),
    MEMORY_LEAK_OOZE("memory-leak ooze", 'o', "#5fd0a0", 18, 3, 0, 2, 4, 0, 150, 6, 2, 5, 8,
            traits(MonsterTrait.REGENERATES, MonsterTrait.NO_DOORS, MonsterTrait.POISON_IMMUNE, MonsterTrait.SPLITS),
            DamageType.PHYSICAL, EffectType.POISONED, 30, 4, 1, 0, "engulf",
            "It never lets go of anything, and it keeps growing. Hit it and there are two of them."),

    // ----- The Clockwork Stacks (depths 4-6): data structures and concurrency -----
    LINKED_LIST_CENTIPEDE("linked-list centipede", 'c', "#d7b45a", 16, 6, 4, 2, 6, 2, 100, 8, 4, 6, 10,
            traits(), DamageType.PHYSICAL, EffectType.POISONED, 25, 4, 2, 0, "bite",
            "Each segment knows only the next. The venom is in the tail."),
    RACE_CONDITION_WISP("race-condition wisp", 'w', "#9be7ff", 10, 7, 9, 2, 4, 0, 50, 9, 4, 7, 8,
            traits(MonsterTrait.ERRATIC), DamageType.ARCANE, null, 0, 0, 0, 0, "flicker into",
            "Two wisps that each think they got there first."),
    SESSION_HIJACKER("session hijacker", 'j', "#60ffd0", 14, 8, 9, 1, 3, 0, 75, 9, 4, 8, 6,
            traits(MonsterTrait.THIEF), DamageType.PHYSICAL, null, 0, 0, 0, 0, "jostle",
            "It slips into your session while you are distracted and leaves with your gold."),
    STACK_OVERFLOW_CONSTRUCT("stack-overflow construct", 'K', "#b0b0c8", 30, 6, 1, 4, 8, 5, 150, 12, 5, 7, 6,
            traits(MonsterTrait.NO_DOORS, MonsterTrait.POISON_IMMUNE), DamageType.PHYSICAL, null, 0, 0, 0, 0, "pound",
            "Plates piled on plates, far past any sensible limit."),
    DEADLOCK_SENTRY("deadlock sentry", 'D', "#e0a040", 20, 6, 2, 3, 6, 3, 100, 11, 4, 7, 6,
            traits(MonsterTrait.POISON_IMMUNE), DamageType.PHYSICAL, EffectType.STUNNED, 12, 1, 0, 0, "clamp",
            "It holds what you need and waits for what it needs. Forever."),
    RECURSIVE_BOLTER("recursive bolter", 'b', "#f0e070", 14, 8, 4, 2, 5, 1, 100, 10, 5, 8, 7,
            traits(MonsterTrait.RANGED), DamageType.PHYSICAL, null, 0, 0, 0, 5, "fire a bolt at",
            "A crossbow that loads itself with smaller crossbows."),

    // ----- The Ember Vaults (depths 7-9): the cyber-security wars -----
    BOTNET_SWARMLING("botnet swarmling", 'n', "#80ff80", 9, 8, 7, 2, 5, 1, 100, 6, 7, 9, 10,
            traits(MonsterTrait.PACK), DamageType.PHYSICAL, null, 0, 0, 0, 0, "nip",
            "Harmless alone. It is never alone."),
    PHISHING_PHANTOM("phishing phantom", 'P', "#7fa8ff", 18, 9, 8, 3, 6, 1, 100, 14, 7, 9, 8,
            traits(MonsterTrait.RANGED, MonsterTrait.FLEES), DamageType.ARCANE, EffectType.WEAKENED, 35, 5, 0, 4,
            "cast a glittering lure at",
            "It offers you exactly what you were hoping to find."),
    ROOTKIT_LURKER("rootkit lurker", 'L', "#8a7aa8", 24, 10, 10, 4, 9, 2, 100, 16, 7, 9, 6,
            traits(MonsterTrait.AMBUSH), DamageType.PHYSICAL, null, 0, 0, 0, 0, "strike",
            "You will not see it until it has already been here a long time."),
    FIREWALL_ELEMENTAL("firewall elemental", 'F', "#ff8040", 32, 9, 4, 5, 9, 3, 100, 18, 8, 9, 6,
            traits(MonsterTrait.FIRE_IMMUNE, MonsterTrait.POISON_IMMUNE), DamageType.FIRE, EffectType.BURNING,
            30, 3, 2, 0, "sear",
            "A living wall of flame that inspects everything trying to pass."),
    TROJAN_STEED("trojan steed", 'T', "#c08050", 40, 10, 5, 6, 12, 4, 75, 22, 8, 9, 4,
            traits(MonsterTrait.AMBUSH), DamageType.PHYSICAL, null, 0, 0, 0, 0, "trample",
            "A beautiful wooden horse, left here as a gift. Probably."),

    // ----- Summoned, quest and tutorial monsters (never placed at random: spawn weight 0) -----
    TRAINING_DUMMY("training dummy", 'd', "#c8a878", 10, 0, 0, 0, 0, 0, 100, 0, 0, 0, 0,
            traits(MonsterTrait.STATIONARY, MonsterTrait.POISON_IMMUNE), DamageType.PHYSICAL, null, 0, 0, 0, 0,
            "wobble at", "A sack of straw in a TU hoodie. It has seen many freshmen come and go."),
    MUTANT("mutant", 'm', "#e070e0", 14, 6, 4, 2, 6, 1, 100, 8, 4, 4, 0,
            traits(), DamageType.PHYSICAL, null, 0, 0, 0, 0, "swipe",
            "An ordinary goblin with exactly one small change. Warden Deng wants it dead."),
    REGISTRATION_HOLD("registration hold", 'h', "#9090ff", 16, 10, 6, 2, 5, 1, 100, 10, 10, 10, 0,
            traits(MonsterTrait.POISON_IMMUNE), DamageType.ARCANE, EffectType.SLOWED, 40, 4, 0, 0, "entangle",
            "A shade of unpaid fees and missing signatures. Everything it touches slows down."),

    // ----- Bosses: one at the end of each zone, and the keeper of the Diploma -----
    SPAGHETTI_HYDRA("Spaghetti Hydra", 'H', "#e8c070", 42, 6, 3, 2, 6, 1, 100, 40, 3, 3, 0,
            traits(MonsterTrait.BOSS, MonsterTrait.POISON_IMMUNE),
            DamageType.PHYSICAL, null, 0, 0, 0, 0, "lash",
            "A knot of tangled strands with no clear beginning or end. Cut one and new ones wriggle free."),
    DEADLOCK_COLOSSUS("Deadlock Colossus", 'C', "#e0a040", 70, 8, 2, 5, 9, 3, 125, 80, 6, 6, 0,
            traits(MonsterTrait.BOSS, MonsterTrait.POISON_IMMUNE, MonsterTrait.NO_DOORS),
            DamageType.PHYSICAL, EffectType.STUNNED, 10, 1, 0, 0, "crush",
            "Two giants welded together, each waiting for the other to let go."),
    ZERO_DAY_WYRM("Zero-Day Wyrm", 'W', "#ff5030", 120, 11, 6, 6, 11, 4, 100, 120, 9, 9, 0,
            traits(MonsterTrait.BOSS, MonsterTrait.FIRE_IMMUNE, MonsterTrait.RANGED),
            DamageType.FIRE, EffectType.BURNING, 30, 3, 3, 4, "breathe fire at",
            "Nobody saw it coming. Nobody ever does."),
    HOLLOW_REGISTRAR("Hollow Registrar", 'R', "#d0d0ff", 180, 13, 8, 7, 13, 5, 100, 0, 10, 10, 0,
            traits(MonsterTrait.BOSS, MonsterTrait.POISON_IMMUNE),
            DamageType.ARCANE, EffectType.WEAKENED, 25, 5, 0, 0, "stamp",
            "Keeper of the Diploma. It has never approved a form on the first attempt.");

    private final String displayName;
    private final char glyph;
    private final String color;
    private final int maxHp;
    private final int accuracy;
    private final int evasion;
    private final int minDamage;
    private final int maxDamage;
    private final int armor;
    private final int actionCost;
    private final int experience;
    private final int minDepth;
    private final int maxDepth;
    private final int spawnWeight;
    private final Set<MonsterTrait> traits;
    private final DamageType damageType;
    private final EffectType onHitEffect;
    private final int onHitPercent;
    private final int effectDuration;
    private final int effectMagnitude;
    private final int range;
    private final String attackVerb;
    private final String description;

    MonsterType(String displayName, char glyph, String color, int maxHp, int accuracy, int evasion,
                int minDamage, int maxDamage, int armor, int actionCost, int experience,
                int minDepth, int maxDepth, int spawnWeight, Set<MonsterTrait> traits,
                DamageType damageType, EffectType onHitEffect, int onHitPercent, int effectDuration,
                int effectMagnitude, int range, String attackVerb, String description) {
        this.displayName = displayName;
        this.glyph = glyph;
        this.color = color;
        this.maxHp = maxHp;
        this.accuracy = accuracy;
        this.evasion = evasion;
        this.minDamage = minDamage;
        this.maxDamage = maxDamage;
        this.armor = armor;
        this.actionCost = actionCost;
        this.experience = experience;
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
        this.spawnWeight = spawnWeight;
        this.traits = traits;
        this.damageType = damageType;
        this.onHitEffect = onHitEffect;
        this.onHitPercent = onHitPercent;
        this.effectDuration = effectDuration;
        this.effectMagnitude = effectMagnitude;
        this.range = range;
        this.attackVerb = attackVerb;
        this.description = description;
    }

    private static Set<MonsterTrait> traits(MonsterTrait... list) {
        Set<MonsterTrait> set = EnumSet.noneOf(MonsterTrait.class);
        set.addAll(java.util.Arrays.asList(list));
        return set;
    }

    /**
     * @param trait a trait
     * @return {@code true} if this type has it
     */
    public boolean has(MonsterTrait trait) {
        return traits.contains(trait);
    }

    /**
     * @param depth a dungeon depth
     * @return {@code true} if this type can appear there
     */
    public boolean appearsAt(int depth) {
        return depth >= minDepth && depth <= maxDepth;
    }

    /** @return the close-range attack for this type */
    public AttackProfile meleeProfile() {
        String verb = has(MonsterTrait.RANGED) ? "strike" : attackVerb;
        return new AttackProfile(accuracy, minDamage, maxDamage, damageType, 5,
                onHitEffect, onHitPercent, effectDuration, effectMagnitude, verb);
    }

    /** @return the ranged attack for this type, or {@code null} if it has none */
    public AttackProfile rangedProfile() {
        if (!has(MonsterTrait.RANGED)) {
            return null;
        }
        return new AttackProfile(accuracy, minDamage, maxDamage, damageType, 5,
                onHitEffect, onHitPercent, effectDuration, effectMagnitude, attackVerb);
    }

    /** @return lower-case display name */
    public String displayName() {
        return displayName;
    }

    /** @return single-character symbol */
    public char glyph() {
        return glyph;
    }

    /** @return display color as a hex string such as "#a08a6a" */
    public String color() {
        return color;
    }

    /** @return maximum HP */
    public int maxHp() {
        return maxHp;
    }

    /** @return accuracy rating */
    public int accuracy() {
        return accuracy;
    }

    /** @return evasion rating */
    public int evasion() {
        return evasion;
    }

    /** @return armor rating */
    public int armor() {
        return armor;
    }

    /** @return ticks per action */
    public int actionCost() {
        return actionCost;
    }

    /** @return experience awarded for defeating it */
    public int experience() {
        return experience;
    }

    /** @return shallowest depth it appears at */
    public int minDepth() {
        return minDepth;
    }

    /** @return deepest depth it appears at */
    public int maxDepth() {
        return maxDepth;
    }

    /** @return relative chance of being chosen when a floor is populated */
    public int spawnWeight() {
        return spawnWeight;
    }

    /** @return ranged reach in tiles (0 for melee-only) */
    public int range() {
        return range;
    }

    /** @return flavor text */
    public String description() {
        return description;
    }
}
