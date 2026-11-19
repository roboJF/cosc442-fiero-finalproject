package edu.towson.cosc442.halls7800.entity;

import edu.towson.cosc442.halls7800.combat.AttackProfile;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.item.Armor;
import edu.towson.cosc442.halls7800.item.Inventory;
import edu.towson.cosc442.halls7800.item.Weapon;
import edu.towson.cosc442.halls7800.progression.Experience;
import edu.towson.cosc442.halls7800.progression.Perk;
import edu.towson.cosc442.halls7800.util.Rng;
import edu.towson.cosc442.halls7800.world.Position;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The adventurer controlled by the user. The player's actions come from commands submitted to
 * the {@link GameSession}, never from the scheduler.
 *
 * <p>Beyond the common creature statistics, the player has a pack, a wielded weapon and worn
 * armor (both of which stay in the pack), a purse of gold, and a hunger clock.
 */
public class Player extends Creature {

    /** Starting and base maximum HP. */
    public static final int STARTING_HP = 30;
    /** Base accuracy. */
    public static final int BASE_ACCURACY = 6;
    /** Base evasion. */
    public static final int BASE_EVASION = 3;
    /** Unarmed damage range. */
    public static final int UNARMED_MIN = 1;
    /** Unarmed damage range. */
    public static final int UNARMED_MAX = 4;
    /** Turns between natural regeneration of 1 HP. */
    public static final int REGEN_INTERVAL = 6;

    /** Most satiety the player can store. */
    public static final int MAX_SATIETY = 2000;
    /** Satiety at the start of a run. */
    public static final int STARTING_SATIETY = 1500;
    /** At or below this satiety the player is hungry. */
    public static final int HUNGRY_AT = 300;
    /** At or below this satiety the player is weak (accuracy penalty). */
    public static final int WEAK_AT = 100;
    /** Turns between 1 HP of starvation damage. */
    public static final int STARVE_INTERVAL = 5;

    /** How well fed the player is. */
    public enum Hunger {
        /** Plenty of food in the belly. */
        FED,
        /** Getting hungry; no penalty yet. */
        HUNGRY,
        /** Weak from hunger: accuracy suffers. */
        WEAK,
        /** Starving: losing HP. */
        STARVING
    }

    private final Inventory inventory = new Inventory();
    private final Consumer<Position> damageListener;
    private Weapon weapon;
    private Armor armor;
    private int gold;
    private int satiety = STARTING_SATIETY;
    private int starvingTurns;
    private int experience;
    private int level = 1;
    private final Set<Perk> perks = EnumSet.noneOf(Perk.class);
    private final List<Perk> perkChoices = new ArrayList<>();
    private int pendingPerks;
    private int kills;
    private int turnsSinceRegen;
    private int hungerTicks;

    /**
     * @param name the adventurer's name
     * @param position starting position
     */
    public Player(String name, Position position) {
        this(name, position, ignored -> { });
    }

    /**
     * @param name the adventurer's name
     * @param position starting position
     * @param damageListener receives the player's position each time they lose HP to damage
     */
    public Player(String name, Position position, Consumer<Position> damageListener) {
        super(name, position, STARTING_HP);
        this.damageListener = Objects.requireNonNull(damageListener, "damageListener");
        setBaseAccuracy(BASE_ACCURACY);
        setBaseEvasion(BASE_EVASION);
        setBaseArmor(0);
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    @Override
    public int takeDamage(int amount) {
        int lost = super.takeDamage(amount);
        if (lost > 0) {
            damageListener.accept(position());
        }
        return lost;
    }

    // ----- combat -----

    @Override
    public int accuracy() {
        int total = super.accuracy();
        if (weapon != null) {
            total += weapon.accuracyBonus();
        }
        if (hunger() == Hunger.WEAK || hunger() == Hunger.STARVING) {
            total -= 2;
        }
        total += level - 1;
        if (perks.contains(Perk.PRECISION)) {
            total += 2;
        }
        return total;
    }

    @Override
    public int evasion() {
        if (isHelpless()) {
            return 0;
        }
        int penalty = armor == null ? 0 : armor.evasionPenalty();
        int bonus = level / 2 + (perks.contains(Perk.FOOTWORK) ? 2 : 0);
        return Math.max(0, super.evasion() + bonus - penalty);
    }

    @Override
    public int armor() {
        return super.armor() + (armor == null ? 0 : armor.armorValue());
    }

    @Override
    public AttackProfile meleeAttack() {
        AttackProfile base = weapon == null
                ? AttackProfile.basic(accuracy(), UNARMED_MIN, UNARMED_MAX, 5, "punch")
                : weapon.profile(accuracy());
        int crit = base.critPercent() + (perks.contains(Perk.PRECISION) ? 5 : 0);
        int min = base.minDamage();
        int max = base.maxDamage();
        if (perks.contains(Perk.BERSERKER) && hp() * 100 < maxHp() * 40) {
            min = min * 3 / 2;
            max = max * 3 / 2;
        }
        return new AttackProfile(base.accuracy(), min, max, base.damageType(), crit, base.onHitEffect(),
                base.onHitPercent(), base.effectDuration(), base.effectMagnitude(), base.verb());
    }

    /** @return extra damage added to thrown weapons */
    public int thrownDamageBonus() {
        return perks.contains(Perk.SHARPSHOOTER) ? 2 : 0;
    }

    /** @return ticks a melee attack takes with the current weapon, including haste and slow */
    public int attackCost() {
        int base = weapon == null ? baseActionCost() : weapon.attackCost();
        return effects().modifyActionCost(base);
    }

    // ----- equipment -----

    /** @return the pack */
    public Inventory inventory() {
        return inventory;
    }

    /** @return the wielded weapon, or {@code null} */
    public Weapon weapon() {
        return weapon;
    }

    /** @return the worn armor, or {@code null} */
    public Armor wornArmor() {
        return armor;
    }

    /**
     * Wields a weapon from the pack (or nothing). Curse checks are the caller's job.
     *
     * @param weapon a weapon in the pack, or {@code null} to fight unarmed
     */
    public void setWeapon(Weapon weapon) {
        if (weapon != null && !inventory.contains(weapon)) {
            throw new IllegalArgumentException("Weapon must be in the pack");
        }
        this.weapon = weapon;
    }

    /**
     * Wears armor from the pack (or nothing). Curse checks are the caller's job.
     *
     * @param armor armor in the pack, or {@code null}
     */
    public void setWornArmor(Armor armor) {
        if (armor != null && !inventory.contains(armor)) {
            throw new IllegalArgumentException("Armor must be in the pack");
        }
        this.armor = armor;
    }

    /**
     * @param item an item
     * @return {@code true} if it is the wielded weapon or worn armor
     */
    public boolean isEquipped(Object item) {
        return item != null && (item == weapon || item == armor);
    }

    // ----- gold -----

    /** @return coins in the purse */
    public int gold() {
        return gold;
    }

    /**
     * @param amount coins to add (0 or more)
     */
    public void addGold(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }
        gold += amount;
    }

    /**
     * Pays coins if the purse holds enough.
     *
     * @param amount price
     * @return {@code true} if paid
     */
    public boolean spendGold(int amount) {
        if (amount < 0 || amount > gold) {
            return false;
        }
        gold -= amount;
        return true;
    }

    // ----- hunger -----

    /** @return current satiety */
    public int satiety() {
        return satiety;
    }

    /** @return hunger level derived from satiety */
    public Hunger hunger() {
        if (satiety <= 0) {
            return Hunger.STARVING;
        }
        if (satiety <= WEAK_AT) {
            return Hunger.WEAK;
        }
        if (satiety <= HUNGRY_AT) {
            return Hunger.HUNGRY;
        }
        return Hunger.FED;
    }

    /**
     * Restores satiety, up to {@link #MAX_SATIETY}.
     *
     * @param amount satiety to add
     */
    public void eat(int amount) {
        satiety = Math.min(MAX_SATIETY, satiety + Math.max(0, amount));
        starvingTurns = 0;
    }

    /**
     * Burns one turn's worth of food. Called once per player turn.
     *
     * @return a message if the hunger level changed (or starvation hurt), otherwise {@code null}
     */
    public String tickHunger() {
        Hunger before = hunger();
        hungerTicks++;
        if (!perks.contains(Perk.IRON_STOMACH) || hungerTicks % 2 == 0) {
            satiety = Math.max(0, satiety - 1);
        }
        Hunger after = hunger();
        if (after == Hunger.STARVING) {
            starvingTurns++;
            if (starvingTurns % STARVE_INTERVAL == 0) {
                takeDamage(1);
                return before != after ? "You are starving!" : "You are starving to death!";
            }
        }
        if (after != before) {
            return switch (after) {
                case HUNGRY -> "You are getting hungry.";
                case WEAK -> "You feel weak from hunger.";
                case STARVING -> "You are starving!";
                default -> null;
            };
        }
        return null;
    }

    // ----- progress -----

    /**
     * Regains 1 HP every {@link #REGEN_INTERVAL} turns unless starving. Called once per turn.
     *
     * @return HP regained this turn (0 or 1)
     */
    public int naturalRegeneration() {
        if (hunger() == Hunger.STARVING) {
            return 0;
        }
        turnsSinceRegen++;
        int interval = perks.contains(Perk.QUICK_RECOVERY) ? REGEN_INTERVAL / 2 : REGEN_INTERVAL;
        if (turnsSinceRegen >= interval) {
            turnsSinceRegen = 0;
            return heal(1);
        }
        return 0;
    }

    /** @return total experience earned */
    public int experience() {
        return experience;
    }

    /**
     * Adds experience without applying level-ups. Prefer {@link #gainExperience}.
     *
     * @param amount experience to add (0 or more)
     */
    public void addExperience(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("experience cannot be negative");
        }
        experience += amount;
    }

    /**
     * Adds experience and applies any level-ups it earns: more HP, accuracy and evasion, and a
     * perk choice at every third level.
     *
     * @param amount experience to add (0 or more)
     * @param rng randomness used to pick which perks are offered
     * @return messages describing level-ups (empty if none)
     */
    public List<String> gainExperience(int amount, Rng rng) {
        addExperience(amount);
        List<String> messages = new ArrayList<>();
        int target = Experience.levelFor(experience);
        while (level < target) {
            level++;
            setMaxHp(maxHp() + Experience.HP_PER_LEVEL);
            heal(Experience.HP_PER_LEVEL);
            messages.add("Welcome to level " + level + "! You feel more capable.");
            if (Experience.grantsPerk(level)) {
                pendingPerks++;
                if (perkChoices.isEmpty()) {
                    offerPerks(rng);
                }
                messages.add("You may choose a new perk.");
            }
        }
        return messages;
    }

    private void offerPerks(Rng rng) {
        List<Perk> available = new ArrayList<>();
        for (Perk p : Perk.values()) {
            if (!perks.contains(p)) {
                available.add(p);
            }
        }
        rng.shuffle(available);
        perkChoices.clear();
        perkChoices.addAll(available.subList(0, Math.min(Experience.PERK_CHOICES, available.size())));
        if (perkChoices.isEmpty()) {
            pendingPerks = 0;
        }
    }

    /**
     * Takes one of the offered perks.
     *
     * @param perk the chosen perk; must be one of {@link #perkChoices()}
     * @param rng randomness used if another perk choice is still owed
     * @return {@code true} if the perk was granted
     */
    public boolean choosePerk(Perk perk, Rng rng) {
        if (!perkChoices.contains(perk)) {
            return false;
        }
        perks.add(perk);
        if (perk == Perk.TOUGHNESS) {
            setMaxHp(maxHp() + 10);
            heal(10);
        }
        pendingPerks--;
        perkChoices.clear();
        if (pendingPerks > 0) {
            offerPerks(rng);
        }
        return true;
    }

    /** @return the perks on offer (empty when no choice is pending) */
    public List<Perk> perkChoices() {
        return Collections.unmodifiableList(perkChoices);
    }

    /**
     * @param perk a perk
     * @return {@code true} if the player has it
     */
    public boolean hasPerk(Perk perk) {
        return perks.contains(perk);
    }

    /** @return the perks the player has */
    public Set<Perk> perks() {
        return Collections.unmodifiableSet(perks);
    }

    /** @return current experience level */
    public int level() {
        return level;
    }

    /** @return number of monsters defeated */
    public int kills() {
        return kills;
    }

    /** Records one defeated monster. */
    public void recordKill() {
        kills++;
    }

    /**
     * Always throws: the scheduler must stop before the player's turn and wait for a command.
     *
     * @param session unused
     * @return never returns normally
     */
    @Override
    protected int act(GameSession session) {
        throw new IllegalStateException("Player turns are driven by commands, not the scheduler");
    }
}
