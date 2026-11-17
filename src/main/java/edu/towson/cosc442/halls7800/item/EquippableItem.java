package edu.towson.cosc442.halls7800.item;

/**
 * A weapon or armor. Equipment has an upgrade level that is hidden until the item is equipped or
 * identified. A negative level means the item is cursed: once equipped, it cannot be removed
 * until the curse is lifted.
 */
public abstract class EquippableItem extends Item {

    private int upgrade;
    private boolean known;

    /**
     * @param upgrade upgrade level; negative means cursed
     * @param known whether the player already knows the upgrade level
     */
    protected EquippableItem(int upgrade, boolean known) {
        super(1);
        this.upgrade = upgrade;
        this.known = known;
    }

    /** @return the upgrade level (negative when cursed) */
    public int upgrade() {
        return upgrade;
    }

    /**
     * Changes the upgrade level, e.g. from a scroll of refactoring.
     *
     * @param delta amount to add
     */
    public void addUpgrade(int delta) {
        upgrade += delta;
    }

    /** @return {@code true} if the item is cursed */
    public boolean isCursed() {
        return upgrade < 0;
    }

    /** Lifts a curse by raising a negative upgrade level to zero. */
    public void removeCurse() {
        if (upgrade < 0) {
            upgrade = 0;
        }
    }

    /** @return {@code true} if the player knows the upgrade level */
    public boolean isKnown() {
        return known;
    }

    /** Reveals the upgrade level to the player. */
    public void reveal() {
        known = true;
    }

    /**
     * @return the base name of the kind, e.g. "dagger"
     */
    protected abstract String kindName();

    @Override
    public String name(Identification id) {
        if (!known) {
            return kindName();
        }
        return (upgrade >= 0 ? "+" + upgrade : String.valueOf(upgrade)) + " " + kindName();
    }

    @Override
    public String describe(Identification id) {
        String name = name(id);
        return article(known ? "x" : name) + " " + name;
    }
}
