package edu.towson.cosc442.halls7800.item;

import edu.towson.cosc442.halls7800.util.Rng;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What the player knows about item kinds in this run. Each run shuffles which color belongs to
 * which potion and which label belongs to which scroll. Drinking or reading an item of an unknown
 * kind identifies the whole kind.
 */
public class Identification {

    /** Potion colors, assigned to potion kinds at random each run. */
    public static final List<String> POTION_COLORS = List.of(
            "crimson", "azure", "emerald", "amber", "violet", "ivory",
            "obsidian", "golden", "teal", "rose", "murky", "silver");

    /** Scroll labels, assigned to scroll kinds at random each run. */
    public static final List<String> SCROLL_LABELS = List.of(
            "CAFEBABE", "DEADBEEF", "FOO BAR", "LOREM IPSUM", "HELLO WORLD",
            "XYZZY", "SUDO MAKE", "RTFM", "QUUX", "NOP SLED");

    private final Map<Potion.Kind, String> colors = new EnumMap<>(Potion.Kind.class);
    private final Map<Scroll.Kind, String> labels = new EnumMap<>(Scroll.Kind.class);
    private final Set<Potion.Kind> knownPotions = EnumSet.noneOf(Potion.Kind.class);
    private final Set<Scroll.Kind> knownScrolls = EnumSet.noneOf(Scroll.Kind.class);

    /**
     * Shuffles appearances for a new run.
     *
     * @param rng randomness source (the run's identification stream)
     */
    public Identification(Rng rng) {
        List<String> c = new ArrayList<>(POTION_COLORS);
        rng.shuffle(c);
        Potion.Kind[] potions = Potion.Kind.values();
        for (int i = 0; i < potions.length; i++) {
            colors.put(potions[i], c.get(i));
        }
        List<String> l = new ArrayList<>(SCROLL_LABELS);
        rng.shuffle(l);
        Scroll.Kind[] scrolls = Scroll.Kind.values();
        for (int i = 0; i < scrolls.length; i++) {
            labels.put(scrolls[i], l.get(i));
        }
    }

    /**
     * @param kind a potion kind
     * @return its color this run
     */
    public String colorOf(Potion.Kind kind) {
        return colors.get(kind);
    }

    /**
     * @param kind a scroll kind
     * @return its label this run
     */
    public String labelOf(Scroll.Kind kind) {
        return labels.get(kind);
    }

    /**
     * @param kind a potion kind
     * @return {@code true} if the player has identified it
     */
    public boolean isKnown(Potion.Kind kind) {
        return knownPotions.contains(kind);
    }

    /**
     * @param kind a scroll kind
     * @return {@code true} if the player has identified it
     */
    public boolean isKnown(Scroll.Kind kind) {
        return knownScrolls.contains(kind);
    }

    /**
     * Identifies a potion kind.
     *
     * @param kind the kind
     * @return {@code true} if it was not already known
     */
    public boolean learn(Potion.Kind kind) {
        return knownPotions.add(kind);
    }

    /**
     * Identifies a scroll kind.
     *
     * @param kind the kind
     * @return {@code true} if it was not already known
     */
    public boolean learn(Scroll.Kind kind) {
        return knownScrolls.add(kind);
    }

    /**
     * @param item any item
     * @return {@code true} if the player knows everything about it
     */
    public boolean isIdentified(Item item) {
        if (item instanceof Potion p) {
            return isKnown(p.kind());
        }
        if (item instanceof Scroll s) {
            return isKnown(s.kind());
        }
        if (item instanceof EquippableItem e) {
            return e.isKnown();
        }
        return true;
    }

    /**
     * Fully identifies an item (and, for potions and scrolls, its whole kind).
     *
     * @param item the item
     */
    public void identify(Item item) {
        if (item instanceof Potion p) {
            learn(p.kind());
        } else if (item instanceof Scroll s) {
            learn(s.kind());
        } else if (item instanceof EquippableItem e) {
            e.reveal();
        }
    }

    /** @return identified potion kinds */
    public Set<Potion.Kind> knownPotions() {
        return EnumSet.copyOf(knownPotions);
    }

    /** @return identified scroll kinds */
    public Set<Scroll.Kind> knownScrolls() {
        return EnumSet.copyOf(knownScrolls);
    }
}
