package edu.towson.cosc442.halls7800.item;

/**
 * Something that can lie on the floor and be carried. Some items stack: a stack is a single
 * {@code Item} with a quantity greater than one.
 *
 * <p>How an item is named depends on what the player knows about it, so naming methods take the
 * run's {@link Identification}.
 */
public abstract class Item {

    private int quantity;

    /**
     * @param quantity number of items in the stack (at least 1)
     */
    protected Item(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be at least 1");
        }
        this.quantity = quantity;
    }

    /** @return the item's category */
    public abstract ItemCategory category();

    /**
     * @param id what the player has identified
     * @return the singular name as the player sees it, without an article
     */
    public abstract String name(Identification id);

    /**
     * @param id what the player has identified
     * @return the plural name as the player sees it
     */
    public String pluralName(Identification id) {
        return name(id) + "s";
    }

    /** @return value of one item in gold coins */
    public abstract int value();

    /**
     * Creates a new item of the same kind with the given quantity. Used to split stacks.
     *
     * @param newQuantity quantity of the copy
     * @return the copy
     */
    protected abstract Item copyWithQuantity(int newQuantity);

    /** @return an independent copy of this item with the same quantity and item state */
    public final Item copy() {
        Item copy = copyWithQuantity(quantity);
        copy.quantity = quantity;
        return copy;
    }

    /** @return {@code true} if several of this item can share one inventory slot */
    public boolean isStackable() {
        return false;
    }

    /**
     * @param other another item
     * @return {@code true} if {@code other} can be merged into this stack
     */
    public boolean canStackWith(Item other) {
        return false;
    }

    /**
     * Describes the item with an article or count, e.g. "a dagger", "an azure potion",
     * "3 darts".
     *
     * @param id what the player has identified
     * @return the description
     */
    public String describe(Identification id) {
        if (quantity > 1) {
            return quantity + " " + pluralName(id);
        }
        String name = name(id);
        return article(name) + " " + name;
    }

    /**
     * @param word the following word
     * @return "an" before a vowel sound, otherwise "a"
     */
    public static String article(String word) {
        if (word.isEmpty()) {
            return "a";
        }
        char c = Character.toLowerCase(word.charAt(0));
        return "aeiou".indexOf(c) >= 0 ? "an" : "a";
    }

    /** @return number of items in this stack */
    public int quantity() {
        return quantity;
    }

    /**
     * @param quantity new stack size (at least 1)
     */
    public void setQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be at least 1");
        }
        this.quantity = quantity;
    }

    /**
     * Removes {@code count} items from this stack and returns them as a new stack.
     *
     * @param count how many to take; must be less than the current quantity
     * @return the new stack
     */
    public Item split(int count) {
        if (count < 1 || count >= quantity) {
            throw new IllegalArgumentException("Can only split 1.." + (quantity - 1) + " from a stack of " + quantity);
        }
        quantity -= count;
        return copyWithQuantity(count);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "x" + quantity;
    }
}
