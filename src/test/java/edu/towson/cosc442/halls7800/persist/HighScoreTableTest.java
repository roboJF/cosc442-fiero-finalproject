package edu.towson.cosc442.halls7800.persist;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.towson.cosc442.halls7800.engine.Difficulty;
import edu.towson.cosc442.halls7800.engine.GamePhase;
import org.junit.jupiter.api.Test;

class HighScoreTableTest {

    private static HighScoreTable.Entry entry(String name, int score) {
        return new HighScoreTable.Entry(score, name, 3, GamePhase.DEAD, Difficulty.UNDERGRADUATE, 500, 1L, "2026-11-09");
    }

    @Test
    void entriesAreKeptHighestFirst() {
        HighScoreTable table = new HighScoreTable();
        table.add(entry("Low", 100));
        int rank = table.add(entry("High", 900));

        assertEquals(1, rank);
        assertEquals("High", table.entries().get(0).name());
    }
}
