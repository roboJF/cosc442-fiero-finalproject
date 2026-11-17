package edu.towson.cosc442.halls7800.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.towson.cosc442.halls7800.engine.Difficulty;
import edu.towson.cosc442.halls7800.persist.RunRecord;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReplayToolTest {

    @Test
    void reportsAnInvalidActionInTheRunFile(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("run.txt");
        RunRecord record = new RunRecord(62L, Difficulty.FRESHMAN, "Ada", List.of("W", "SHOP:TYPO"));
        Files.writeString(file, record.toText());

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream out = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            assertEquals(1, ReplayTool.run(new String[] {file.toString()}, out));
        }

        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Invalid action 2 ('SHOP:TYPO')"));
    }
}
