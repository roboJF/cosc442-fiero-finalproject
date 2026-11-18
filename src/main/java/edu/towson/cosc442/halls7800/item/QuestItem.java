package edu.towson.cosc442.halls7800.item;

import java.util.Objects;

/**
 * Items that matter to quests rather than to combat.
 */
public final class QuestItem extends Item {

    /** Kinds of quest item. */
    public enum Kind {
        /** Pages of the lost requirements document, wanted by the Archivist. */
        REQUIREMENTS_PAGE("torn requirements page", "torn requirements pages", true),
        /** The object of the whole quest. */
        DIPLOMA("Diploma of Ascendance", "Diplomas of Ascendance", false);

        private final String singular;
        private final String plural;
        private final boolean stackable;

        Kind(String singular, String plural, boolean stackable) {
            this.singular = singular;
            this.plural = plural;
            this.stackable = stackable;
        }
    }

    private final Kind kind;

    /**
     * @param kind which quest item
     * @param quantity stack size
     */
    public QuestItem(Kind kind, int quantity) {
        super(quantity);
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    /** @return which quest item this is */
    public Kind kind() {
        return kind;
    }

    @Override
    public ItemCategory category() {
        return ItemCategory.QUEST;
    }

    @Override
    public String name(Identification id) {
        return kind.singular;
    }

    @Override
    public String pluralName(Identification id) {
        return kind.plural;
    }

    @Override
    public String describe(Identification id) {
        if (kind == Kind.DIPLOMA) {
            return "the " + kind.singular;
        }
        return super.describe(id);
    }

    @Override
    public int value() {
        return 0;
    }

    @Override
    public boolean isStackable() {
        return kind.stackable;
    }

    @Override
    public boolean canStackWith(Item other) {
        return kind.stackable && other instanceof QuestItem q && q.kind == kind;
    }

    @Override
    protected Item copyWithQuantity(int newQuantity) {
        return new QuestItem(kind, newQuantity);
    }
}
