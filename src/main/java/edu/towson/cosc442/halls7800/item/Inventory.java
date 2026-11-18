package edu.towson.cosc442.halls7800.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The player's pack: an ordered list of item stacks with a fixed number of slots. Stackable items
 * merge into an existing stack of the same kind and do not use a new slot. Each slot is addressed
 * by a letter, 'a' for the first.
 */
public final class Inventory {

    /** Number of slots in a standard pack. */
    public static final int DEFAULT_CAPACITY = 18;

    private final int capacity;
    private final List<Item> items = new ArrayList<>();

    /** Creates a pack with {@link #DEFAULT_CAPACITY} slots. */
    public Inventory() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * @param capacity number of slots (1 to 26)
     */
    public Inventory(int capacity) {
        if (capacity < 1 || capacity > 26) {
            throw new IllegalArgumentException("capacity must be 1..26");
        }
        this.capacity = capacity;
    }

    /**
     * @param item an item
     * @return {@code true} if it would fit, either in an existing stack or a free slot
     */
    public boolean canAdd(Item item) {
        return findStackFor(item) != null || items.size() < capacity;
    }

    /**
     * Adds an item, merging it into a matching stack if possible.
     *
     * @param item the item (gold is not allowed in the pack)
     * @return {@code true} if added; {@code false} if the pack is full
     */
    public boolean add(Item item) {
        Objects.requireNonNull(item, "item");
        if (item instanceof Gold) {
            throw new IllegalArgumentException("Gold goes in the purse, not the pack");
        }
        Item stack = findStackFor(item);
        if (stack != null) {
            stack.setQuantity(stack.quantity() + item.quantity());
            return true;
        }
        if (items.size() >= capacity) {
            return false;
        }
        items.add(item);
        return true;
    }

    private Item findStackFor(Item item) {
        if (!item.isStackable()) {
            return null;
        }
        for (Item existing : items) {
            if (existing.canStackWith(item)) {
                return existing;
            }
        }
        return null;
    }

    /**
     * Removes some or all of a stack.
     *
     * @param item a stack in this pack
     * @param count how many to remove (1 to its quantity)
     * @return the removed items as their own stack
     * @throws IllegalArgumentException if the item is not in the pack or the count is invalid
     */
    public Item remove(Item item, int count) {
        if (!items.contains(item)) {
            throw new IllegalArgumentException("Item is not in the pack");
        }
        if (count < 1 || count > item.quantity()) {
            throw new IllegalArgumentException("Cannot remove " + count + " of " + item.quantity());
        }
        if (count == item.quantity()) {
            items.remove(item);
            return item;
        }
        return item.split(count);
    }

    /**
     * Removes one item from a stack.
     *
     * @param item a stack in this pack
     * @return the single removed item
     */
    public Item removeOne(Item item) {
        return remove(item, 1);
    }

    /**
     * @param index slot index
     * @return the stack in that slot, or {@code null} if empty or out of range
     */
    public Item get(int index) {
        return index >= 0 && index < items.size() ? items.get(index) : null;
    }

    /**
     * @param item an item
     * @return its slot index, or -1
     */
    public int indexOf(Item item) {
        return items.indexOf(item);
    }

    /**
     * @param item an item
     * @return {@code true} if it is in the pack
     */
    public boolean contains(Item item) {
        return items.contains(item);
    }

    /**
     * @param type an item class
     * @param <T> the class
     * @return every stack of that class, in slot order
     */
    public <T extends Item> List<T> ofType(Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Item i : items) {
            if (type.isInstance(i)) {
                result.add(type.cast(i));
            }
        }
        return result;
    }

    /** @return the stacks, in slot order */
    public List<Item> items() {
        return Collections.unmodifiableList(items);
    }

    /** @return number of used slots */
    public int size() {
        return items.size();
    }

    /** @return number of slots */
    public int capacity() {
        return capacity;
    }

    /** @return {@code true} if every slot is used */
    public boolean isFull() {
        return items.size() >= capacity;
    }

    /**
     * @param index slot index (0-25)
     * @return the slot's letter, 'a' for 0
     */
    public static char letterFor(int index) {
        return (char) ('a' + index);
    }

    /**
     * @param letter a slot letter
     * @return the slot index, or -1 if not a letter a-z
     */
    public static int indexFor(char letter) {
        char c = Character.toLowerCase(letter);
        return c >= 'a' && c <= 'z' ? c - 'a' : -1;
    }
}
