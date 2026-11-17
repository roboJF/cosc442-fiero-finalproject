package edu.towson.cosc442.halls7800.item;

/**
 * A pile of gold coins. Gold goes straight into the player's purse when picked up and never takes
 * an inventory slot. The pile's quantity is the number of coins.
 */
public class Gold extends Item {

    /**
     * @param amount number of coins (at least 1)
     */
    public Gold(int amount) {
        super(amount);
    }

    @Override
    public ItemCategory category() {
        return ItemCategory.GOLD;
    }

    @Override
    public String name(Identification id) {
        return "gold coin";
    }

    @Override
    public int value() {
        return 1;
    }

    @Override
    public boolean isStackable() {
        return true;
    }

    @Override
    public boolean canStackWith(Item other) {
        return other instanceof Gold;
    }

    @Override
    protected Item copyWithQuantity(int newQuantity) {
        return new Gold(newQuantity);
    }
}
