package edu.towson.cosc442.halls7800.progression;

/**
 * Feats the player can accomplish. Achievements are earned during a run and kept across runs.
 */
public enum Achievement {
    ORIENTATION("Orientation Complete", "Finish the TU Orientation tutorial."),
    HELLO_WORLD("Hello, World", "Defeat your first monster."),
    BUG_SQUASHER("Bug Squasher", "Defeat 20 monsters from the Undercroft in one run."),
    UNTANGLED("Untangled", "Defeat the Spaghetti Hydra."),
    DEADLOCK_BREAKER("Deadlock Breaker", "Defeat the Deadlock Colossus."),
    PATCH_TUESDAY("Patch Tuesday", "Defeat the Zero-Day Wyrm."),
    COMMENCEMENT("Commencement", "Escape the Halls with the Diploma."),
    DEANS_LIST("Dean's List", "Graduate on Graduate difficulty."),
    ACCELERATED("Accelerated Program", "Graduate in fewer than 4,000 turns."),
    CLEAN_CODE("Clean Code", "Defeat a boss without having drunk a single potion this run."),
    REQUIREMENTS_MET("Requirements Met", "Return all three requirements pages to the Archivist."),
    MUTATION_SCORE("100% Mutation Score", "Complete Warden Deng's mutation trial."),
    LEVEL_DESIGNER("Level Designer", "Complete the Loremaster's survey."),
    SECURE_BY_DESIGN("Secure by Design", "Complete Locksmith Taylor's vault quest."),
    POLYGLOT("Polyglot", "Complete Scribe Irani's translation quest."),
    FULL_COVERAGE("Full Coverage", "Identify every kind of potion in one run."),
    TUITION_PAID("Tuition Paid", "Carry 1,000 gold at once."),
    WORKS_ON_MY_MACHINE("Works on My Machine", "Equip a cursed item."),
    EDGE_CASES("Edge Cases", "Set off five traps in one run and live to tell of it."),
    TIGER_PRIDE("Tiger Pride", "Let Doc defeat ten monsters in one run.");

    private final String title;
    private final String description;

    Achievement(String title, String description) {
        this.title = title;
        this.description = description;
    }

    /** @return display title */
    public String title() {
        return title;
    }

    /** @return how to earn it */
    public String description() {
        return description;
    }
}
