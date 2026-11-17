package edu.towson.cosc442.halls7800.progression;

/**
 * Permanent bonuses the player chooses at certain levels.
 */
public enum Perk {
    TOUGHNESS("Toughness", "+10 maximum HP."),
    PRECISION("Precision", "+2 accuracy and +5% critical hit chance."),
    FOOTWORK("Footwork", "+2 evasion."),
    QUICK_RECOVERY("Quick Recovery", "Natural healing is twice as fast."),
    IRON_STOMACH("Iron Stomach", "You get hungry half as quickly."),
    SHARPSHOOTER("Sharpshooter", "Thrown weapons deal +2 damage."),
    BERSERKER("Berserker", "+50% melee damage while below 40% HP."),
    SCAVENGER("Scavenger", "Monsters drop loot more often.");

    private final String title;
    private final String description;

    Perk(String title, String description) {
        this.title = title;
        this.description = description;
    }

    /** @return display title */
    public String title() {
        return title;
    }

    /** @return what the perk does */
    public String description() {
        return description;
    }
}
