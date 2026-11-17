package edu.towson.cosc442.halls7800.engine;

/**
 * How hard a run is. Difficulty changes the player's starting health and healing supplies, the
 * number and toughness of monsters, and the final score multiplier.
 */
public enum Difficulty {
    /** More health, fewer monsters, extra healing. Score is halved. */
    FRESHMAN("Freshman", "Easy - Syllabus week: extra health, fewer monsters, a spare healing potion. Half score.",
            10, -1, 100, 1, 50),
    /** The Halls as designed. */
    UNDERGRADUATE("Undergraduate", "Normal - Midterm season: the Halls exactly as designed. Full score.",
            0, 0, 100, 0, 100),
    /** Less health, more and tougher monsters. Score is doubled. */
    GRADUATE("Graduate", "Hard - Thesis defense: less health, more and tougher monsters. Double score.",
            -5, 2, 125, 0, 200);

    private final String title;
    private final String description;
    private final int bonusHp;
    private final int extraMonsterGroups;
    private final int monsterHpPercent;
    private final int extraHealingPotions;
    private final int scorePercent;

    Difficulty(String title, String description, int bonusHp, int extraMonsterGroups, int monsterHpPercent,
               int extraHealingPotions, int scorePercent) {
        this.title = title;
        this.description = description;
        this.bonusHp = bonusHp;
        this.extraMonsterGroups = extraMonsterGroups;
        this.monsterHpPercent = monsterHpPercent;
        this.extraHealingPotions = extraHealingPotions;
        this.scorePercent = scorePercent;
    }

    /** @return display title */
    public String title() {
        return title;
    }

    /** @return one-line description */
    public String description() {
        return description;
    }

    /** @return change to the player's starting maximum HP */
    public int bonusHp() {
        return bonusHp;
    }

    /** @return monster groups added to (or removed from) each generated floor */
    public int extraMonsterGroups() {
        return extraMonsterGroups;
    }

    /** @return monster HP as a percentage of normal */
    public int monsterHpPercent() {
        return monsterHpPercent;
    }

    /** @return extra potions of healing in the starting kit */
    public int extraHealingPotions() {
        return extraHealingPotions;
    }

    /** @return final score as a percentage of the base score */
    public int scorePercent() {
        return scorePercent;
    }

    /** @return the next easier difficulty, or this one if already the easiest */
    public Difficulty easier() {
        return this == GRADUATE ? UNDERGRADUATE : FRESHMAN;
    }

    /** @return the next harder difficulty, or this one if already the hardest */
    public Difficulty harder() {
        return this == FRESHMAN ? UNDERGRADUATE : GRADUATE;
    }
}
