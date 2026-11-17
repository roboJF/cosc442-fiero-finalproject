package edu.towson.cosc442.halls7800.item;

/**
 * Broad groups of items, each with the symbol used to draw it on the floor.
 */
public enum ItemCategory {
    WEAPON(')', "#d0d0e0"),
    ARMOR('[', "#a0b8d0"),
    THROWN('/', "#c8c8a0"),
    WAND('|', "#b0e0ff"),
    POTION('!', "#ff70a0"),
    SCROLL('?', "#f0e8c0"),
    FOOD('%', "#d0a060"),
    GOLD('$', "#f2c14e"),
    KEY('-', "#e0c050"),
    QUEST('&', "#ffd700");

    private final char glyph;
    private final String color;

    ItemCategory(char glyph, String color) {
        this.glyph = glyph;
        this.color = color;
    }

    /** @return symbol for items of this category */
    public char glyph() {
        return glyph;
    }

    /** @return display color as a hex string */
    public String color() {
        return color;
    }
}
