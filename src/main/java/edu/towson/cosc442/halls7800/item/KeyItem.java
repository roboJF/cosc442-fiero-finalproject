package edu.towson.cosc442.halls7800.item;

/**
 * A key that opens the locked vault on one particular floor, or (a master key) on any floor. An
 * ordinary key is used up when the door opens; a master key is kept.
 */
public class KeyItem extends Item {

    /** The depth value of a master key, which opens the vault on any floor. */
    public static final int MASTER = -1;

    private final int depth;

    /**
     * @param depth the floor whose vault this key opens
     */
    public KeyItem(int depth) {
        super(1);
        this.depth = depth;
    }

    /** @return the floor whose vault this key opens ({@link #MASTER} for a master key) */
    public int depth() {
        return depth;
    }

    /** @return {@code true} for a master key */
    public boolean isMaster() {
        return depth == MASTER;
    }

    /**
     * @param floorDepth a floor's depth
     * @return {@code true} if this key opens that floor's vault
     */
    public boolean opens(int floorDepth) {
        return isMaster() || depth == floorDepth;
    }

    @Override
    public ItemCategory category() {
        return ItemCategory.KEY;
    }

    @Override
    public String name(Identification id) {
        return isMaster() ? "master key" : "vault key (depth " + depth + ")";
    }

    @Override
    public String pluralName(Identification id) {
        return isMaster() ? "master keys" : "vault keys (depth " + depth + ")";
    }

    @Override
    public int value() {
        return 0;
    }

    @Override
    protected Item copyWithQuantity(int newQuantity) {
        return new KeyItem(depth);
    }
}
