package edu.towson.cosc442.halls7800.effect;

import edu.towson.cosc442.halls7800.entity.Creature;
import edu.towson.cosc442.halls7800.world.Terrain;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The set of status effects currently on one creature.
 *
 * <p>Effects tick once per action of their owner (see {@link #tick}). Because ticks follow the
 * owner's actions, a hasted creature's effects also run out faster.
 */
public class EffectManager {

    /** What happened when an effect was applied. */
    public enum Outcome {
        /** The effect was newly added. */
        APPLIED,
        /** An existing effect of the same type was strengthened or lengthened. */
        STACKED,
        /** The effect could not take hold (e.g. burning while wet, or already stunned). */
        RESISTED,
        /** The effect canceled its opposite (haste vs. slow) and was not itself added. */
        CANCELED
    }

    private final Map<EffectType, StatusEffect> active = new EnumMap<>(EffectType.class);

    /**
     * Applies an effect, combining it with any existing effect of the same type according to the
     * type's {@link EffectType.Stacking} rule, and resolving interactions between effects.
     * Immunities are not checked here; see {@link Creature#applyEffect}.
     *
     * @param type the effect
     * @param duration turns (at least 1)
     * @param magnitude strength
     * @return what happened
     */
    public Outcome apply(EffectType type, int duration, int magnitude) {
        if (duration < 1) {
            throw new IllegalArgumentException("duration must be at least 1");
        }
        switch (type) {
            case BURNING:
                if (active.containsKey(EffectType.WET)) {
                    return Outcome.RESISTED;
                }
                break;
            case WET:
                active.remove(EffectType.BURNING);
                break;
            case HASTED:
                active.remove(EffectType.SLOWED);
                break;
            case SLOWED:
                active.remove(EffectType.HASTED);
                break;
            default:
                break;
        }

        StatusEffect existing = active.get(type);
        if (existing == null) {
            active.put(type, new StatusEffect(type, duration, magnitude));
            return Outcome.APPLIED;
        }
        switch (type.stacking()) {
            case REFRESH:
                existing.setRemaining(Math.max(existing.remaining(), duration));
                existing.setMagnitude(Math.max(existing.magnitude(), magnitude));
                break;
            case EXTEND:
                existing.setRemaining(existing.remaining() + duration);
                existing.setMagnitude(Math.max(existing.magnitude(), magnitude));
                break;
            case INTENSIFY:
                existing.setRemaining(Math.max(existing.remaining(), duration));
                existing.setMagnitude(existing.magnitude() + magnitude);
                break;
            case NONE:
            default:
                return Outcome.RESISTED;
        }
        return Outcome.STACKED;
    }

    /**
     * Runs one turn of every active effect: damage over time, healing, environmental
     * interactions and expiry.
     *
     * @param owner the creature the effects are on
     * @param standingOn terrain under the owner
     * @return messages describing what happened, phrased with the owner as subject
     */
    public List<String> tick(Creature owner, Terrain standingOn) {
        List<String> messages = new ArrayList<>();
        if (standingOn == Terrain.WATER && active.remove(EffectType.BURNING) != null) {
            messages.add(owner.subject() + " " + owner.verb("douse") + " the flames in the water.");
        }
        for (StatusEffect effect : new ArrayList<>(active.values())) {
            if (!owner.isAlive()) {
                break;
            }
            switch (effect.type()) {
                case POISONED -> {
                    int dealt = owner.takeDamage(effect.magnitude());
                    messages.add(owner.subject() + " " + owner.verb("suffer") + " " + dealt + " poison damage.");
                }
                case BURNING -> {
                    int dealt = owner.takeDamage(effect.magnitude());
                    messages.add(owner.subject() + " " + owner.verb("burn") + " for " + dealt + ".");
                }
                case REGENERATING -> owner.heal(effect.magnitude());
                default -> {
                    // other effects only modify stats while active
                }
            }
            if (effect.countDown()) {
                active.remove(effect.type());
                messages.add(owner.subject() + " " + owner.verb("are") + " " + effect.type().endPhrase() + ".");
            }
        }
        return messages;
    }

    /**
     * Adjusts the time an action takes for haste and slow.
     *
     * @param baseCost ticks the action would normally take
     * @return adjusted ticks (at least 1)
     */
    public int modifyActionCost(int baseCost) {
        int cost = baseCost;
        if (active.containsKey(EffectType.HASTED)) {
            cost = cost / 2;
        }
        if (active.containsKey(EffectType.SLOWED)) {
            cost = cost * 2;
        }
        return Math.max(1, cost);
    }

    /** @return change to accuracy from active effects (negative when blinded) */
    public int accuracyModifier() {
        return active.containsKey(EffectType.BLINDED) ? -4 : 0;
    }

    /** @return percentage of normal damage dealt (67 when weakened, otherwise 100) */
    public int damagePercent() {
        return active.containsKey(EffectType.WEAKENED) ? 67 : 100;
    }

    /**
     * Lets a shield soak up incoming damage. The shield breaks when its points run out.
     *
     * @param damage incoming damage
     * @return damage remaining after the shield
     */
    public int absorb(int damage) {
        StatusEffect shield = active.get(EffectType.SHIELDED);
        if (shield == null || damage <= 0) {
            return damage;
        }
        int soaked = Math.min(damage, shield.magnitude());
        shield.setMagnitude(shield.magnitude() - soaked);
        if (shield.magnitude() <= 0) {
            active.remove(EffectType.SHIELDED);
        }
        return damage - soaked;
    }

    /**
     * @param type an effect
     * @return {@code true} if it is active
     */
    public boolean has(EffectType type) {
        return active.containsKey(type);
    }

    /**
     * @param type an effect
     * @return its magnitude, or 0 if inactive
     */
    public int magnitude(EffectType type) {
        StatusEffect e = active.get(type);
        return e == null ? 0 : e.magnitude();
    }

    /**
     * @param type an effect
     * @return turns left, or 0 if inactive
     */
    public int remaining(EffectType type) {
        StatusEffect e = active.get(type);
        return e == null ? 0 : e.remaining();
    }

    /**
     * @param type the effect to end immediately
     * @return {@code true} if it was active
     */
    public boolean remove(EffectType type) {
        return active.remove(type) != null;
    }

    /**
     * Removes every harmful effect.
     *
     * @return how many effects were removed
     */
    public int cureHarmful() {
        int before = active.size();
        active.keySet().removeIf(EffectType::isHarmful);
        return before - active.size();
    }

    /** Removes all effects. */
    public void clear() {
        active.clear();
    }

    /** @return snapshots of active effects, in {@link EffectType} order */
    public List<StatusEffect> active() {
        List<StatusEffect> copy = new ArrayList<>();
        for (StatusEffect e : active.values()) {
            copy.add(new StatusEffect(e.type(), Math.max(1, e.remaining()), e.magnitude()));
        }
        return copy;
    }

    /** @return {@code true} if no effects are active */
    public boolean isEmpty() {
        return active.isEmpty();
    }
}
