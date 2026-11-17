package edu.towson.cosc442.halls7800.ui.audio;

import edu.towson.cosc442.halls7800.engine.GamePhase;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.ui.audio.Synth.Wave;
import edu.towson.cosc442.halls7800.world.Zone;
import java.util.ArrayList;
import java.util.List;

/**
 * Original chiptune music. The mood follows the descent: an upbeat tutorial and heroic title,
 * a warm Atrium, then each region of the Halls darker and slower than the last.
 *
 * <p>Notes are written as {@code NAME OCTAVE : EIGHTHS}, e.g. {@code C#5:2} is a C-sharp in octave
 * 5 lasting two eighth notes, and {@code R:4} is a half-note rest. Bars are separated by
 * {@code |} for readability. Drum patterns use one character per eighth note: {@code k} kick,
 * {@code h} hi-hat, {@code .} nothing; the pattern repeats.
 */
public enum Tune {

    TITLE(116, true, Wave.PULSE, Wave.TRIANGLE,
            "C5:2 G4:1 C5:1 E5:2 G5:2 | F5:2 E5:1 D5:1 C5:2 D5:2 | E5:2 C5:1 E5:1 G5:3 E5:1 | D5:6 R:2 "
                    + "| C5:2 G4:1 C5:1 E5:2 G5:2 | A5:2 G5:1 F5:1 E5:2 D5:2 | C5:2 E5:2 D5:2 B4:2 | C5:6 R:2",
            "C3:2 G3:2 C3:2 G3:2 | F2:2 C3:2 F2:2 C3:2 | A2:2 E3:2 A2:2 E3:2 | G2:2 D3:2 G2:2 D3:2 "
                    + "| C3:2 G3:2 C3:2 G3:2 | F2:2 C3:2 F2:2 C3:2 | G2:2 D3:2 G2:2 D3:2 | C3:2 G2:2 C3:4",
            "k.h.k.h.", 0.85),

    TUTORIAL(150, true, Wave.PULSE, Wave.TRIANGLE,
            "G4:1 B4:1 D5:1 G5:1 R:1 D5:1 B4:1 D5:1 | C5:1 E5:1 G5:1 E5:1 C5:2 R:2 "
                    + "| D5:1 F#5:1 A5:1 F#5:1 D5:1 F#5:1 A5:2 | G5:3 D5:1 B4:2 R:2 "
                    + "| E5:1 G5:1 E5:1 C5:1 D5:2 B4:2 | C5:1 E5:1 C5:1 A4:1 B4:2 G4:2 "
                    + "| A4:1 B4:1 C5:1 D5:1 E5:1 F#5:1 G5:2 | G5:4 R:4",
            "G2:1 R:1 D3:1 R:1 G2:1 R:1 D3:1 R:1 | C3:1 R:1 G3:1 R:1 C3:1 R:1 G3:1 R:1 "
                    + "| D3:1 R:1 A3:1 R:1 D3:1 R:1 A3:1 R:1 | G2:1 R:1 D3:1 R:1 G2:1 R:1 D3:1 R:1 "
                    + "| C3:1 R:1 G3:1 R:1 G2:1 R:1 D3:1 R:1 | A2:1 R:1 E3:1 R:1 G2:1 R:1 D3:1 R:1 "
                    + "| D3:1 R:1 A3:1 R:1 D3:1 R:1 A3:1 R:1 | G2:1 R:1 D3:1 R:1 G2:2 R:2",
            "k.hhk.h.", 0.8),

    ATRIUM(92, true, Wave.PULSE, Wave.TRIANGLE,
            "A4:2 C5:2 F5:3 E5:1 | D5:2 C5:2 A4:4 | A#4:2 D5:2 F5:3 D5:1 | C5:6 R:2 "
                    + "| A4:2 C5:2 F5:3 G5:1 | A5:2 G5:2 F5:4 | D5:2 E5:2 G5:2 E5:2 | F5:6 R:2",
            "F2:4 C3:4 | A#2:4 F2:4 | A#2:4 D3:4 | C3:4 G2:4 | F2:4 C3:4 | D3:4 A2:4 | A#2:4 C3:4 | F2:8",
            "h...h...", 0.9),

    UNDERCROFT(100, true, Wave.PULSE, Wave.TRIANGLE,
            "A4:2 C5:1 E5:1 A5:2 G5:1 E5:1 | F5:2 E5:1 D5:1 E5:4 | C5:2 D5:1 E5:1 G5:2 F5:1 E5:1 | D5:4 R:2 B4:2 "
                    + "| A4:2 C5:1 E5:1 A5:2 B5:1 C6:1 | B5:2 A5:1 G5:1 E5:4 | F5:2 E5:1 D5:1 C5:2 B4:2 | A4:6 R:2",
            "A2:2 E3:2 A2:2 E3:2 | F2:2 C3:2 E2:2 B2:2 | C3:2 G3:2 C3:2 G3:2 | G2:2 D3:2 E2:2 B2:2 "
                    + "| A2:2 E3:2 A2:2 E3:2 | E2:2 B2:2 E2:2 B2:2 | F2:2 C3:2 G2:2 D3:2 | A2:4 E2:4",
            "k...h.k.", 0.85),

    CLOCKWORK(92, true, Wave.SQUARE, Wave.TRIANGLE,
            "D5:1 R:1 F5:1 R:1 A5:1 R:1 F5:1 R:1 | E5:1 R:1 G5:1 R:1 A#5:1 R:1 G5:1 R:1 "
                    + "| D5:1 R:1 F5:1 R:1 A5:2 G5:2 | F5:2 E5:2 C#5:4 "
                    + "| D5:1 R:1 F5:1 R:1 A5:1 R:1 D6:1 R:1 | C6:1 R:1 A#5:1 R:1 A5:2 G5:2 "
                    + "| F5:2 E5:2 D5:2 C#5:2 | D5:6 R:2",
            "D3:1 A2:1 D3:1 A2:1 D3:1 A2:1 D3:1 A2:1 | C3:1 G2:1 C3:1 G2:1 C3:1 G2:1 C3:1 G2:1 "
                    + "| A#2:1 F2:1 A#2:1 F2:1 A#2:1 F2:1 A#2:1 F2:1 | A2:1 E2:1 A2:1 E2:1 A2:1 E2:1 A2:1 C#3:1 "
                    + "| D3:1 A2:1 D3:1 A2:1 D3:1 A2:1 D3:1 A2:1 | C3:1 G2:1 C3:1 G2:1 C3:1 G2:1 C3:1 G2:1 "
                    + "| A#2:1 F2:1 A#2:1 F2:1 A#2:1 F2:1 A#2:1 F2:1 | A2:1 E2:1 A2:1 E2:1 D3:4",
            "h.h.h.h.", 0.6),

    EMBER(72, true, Wave.TRIANGLE, Wave.TRIANGLE,
            "E4:4 F4:2 E4:2 | D4:4 E4:4 | G4:3 F4:1 E4:2 D4:2 | E4:8 "
                    + "| B4:4 C5:2 B4:2 | A4:4 G4:4 | F4:3 G4:1 F4:2 D4:2 | E4:8",
            "E2:8 | D2:8 | C2:8 | E2:8 | G2:8 | A2:8 | F2:8 | E2:8",
            "k.k.....", 0.95),

    SANCTUM(60, true, Wave.TRIANGLE, Wave.TRIANGLE,
            "C#4:6 R:2 | G4:6 R:2 | C4:4 B3:4 | C#4:8 | E4:6 R:2 | A#3:6 R:2 | C4:4 G3:4 | C#4:8",
            "C#2:8 | G2:8 | C2:8 | C#2:8 | E2:8 | A#1:8 | C2:8 | C#2:8",
            "k.......", 0.97),

    VICTORY(130, false, Wave.PULSE, Wave.TRIANGLE,
            "G4:1 C5:1 E5:1 G5:2 E5:1 G5:2 | C6:8",
            "C3:4 G2:4 | C3:8",
            "k.h.k.h.", 0.9),

    DEFEAT(84, false, Wave.TRIANGLE, Wave.TRIANGLE,
            "E5:2 D#5:2 D5:2 C#5:2 | C5:8",
            "A2:4 G#2:4 | A1:8",
            "........", 0.95);

    private final int bpm;
    private final boolean loops;
    private final Wave leadWave;
    private final Wave bassWave;
    private final String melody;
    private final String bass;
    private final String drums;
    private final double legato;

    Tune(int bpm, boolean loops, Wave leadWave, Wave bassWave, String melody, String bass, String drums,
         double legato) {
        this.bpm = bpm;
        this.loops = loops;
        this.leadWave = leadWave;
        this.bassWave = bassWave;
        this.melody = melody;
        this.bass = bass;
        this.drums = drums;
        this.legato = legato;
    }

    /** @return {@code true} if the tune repeats until changed; jingles play once */
    public boolean loops() {
        return loops;
    }

    /**
     * Chooses the music for the current state of a run.
     *
     * @param session the game
     * @return the tune to play
     */
    public static Tune forSession(GameSession session) {
        if (session.phase() == GamePhase.VICTORY) {
            return VICTORY;
        }
        if (session.phase() == GamePhase.DEAD) {
            return DEFEAT;
        }
        if (session.isTutorial()) {
            return TUTORIAL;
        }
        return switch (Zone.forDepth(session.depth())) {
            case ATRIUM -> ATRIUM;
            case UNDERCROFT -> UNDERCROFT;
            case CLOCKWORK_STACKS -> CLOCKWORK;
            case EMBER_VAULTS -> EMBER;
            case SANCTUM -> SANCTUM;
        };
    }

    /** A parsed note: MIDI number (or -1 for a rest) and length in eighths. */
    record Note(int midi, int eighths) {
    }

    /**
     * Parses a note line.
     *
     * @param line notes such as "C#5:2 R:1 | G4:3"
     * @return the notes in order
     */
    static List<Note> parse(String line) {
        List<Note> notes = new ArrayList<>();
        for (String token : line.trim().split("\\s+")) {
            if (token.equals("|") || token.isEmpty()) {
                continue;
            }
            String[] parts = token.split(":");
            int eighths = Integer.parseInt(parts[1]);
            notes.add(new Note(parts[0].equals("R") ? -1 : midiOf(parts[0]), eighths));
        }
        return notes;
    }

    /**
     * @param name a note name such as "C#5" or "A1"
     * @return its MIDI number
     */
    static int midiOf(String name) {
        String letters = "C D EF G A B";
        int semitone = letters.indexOf(name.charAt(0));
        int i = 1;
        if (name.charAt(1) == '#') {
            semitone++;
            i++;
        }
        int octave = Integer.parseInt(name.substring(i));
        return 12 * (octave + 1) + semitone;
    }

    /**
     * @param line a note line
     * @return its total length in eighths
     */
    static int lengthOf(String line) {
        int total = 0;
        for (Note n : parse(line)) {
            total += n.eighths();
        }
        return total;
    }

    /**
     * Renders the whole tune.
     *
     * @param gain master gain
     * @return PCM bytes in {@link Synth#FORMAT}
     */
    byte[] render(double gain) {
        int eighths = Math.max(lengthOf(melody), lengthOf(bass));
        int perEighth = Synth.samples(60.0 / bpm / 2);
        float[] buf = new float[eighths * perEighth + Synth.samples(loops ? 0 : 0.4)];
        voice(buf, parse(melody), perEighth, leadWave, 0.32);
        voice(buf, parse(bass), perEighth, bassWave, 0.42);
        for (int e = 0; e < eighths; e++) {
            char hit = drums.charAt(e % drums.length());
            if (hit == 'k') {
                Synth.tone(buf, e * perEighth, Synth.samples(0.09), 180, 50, Wave.NOISE, 0.32, 0.8);
            } else if (hit == 'h') {
                Synth.tone(buf, e * perEighth, Synth.samples(0.03), 7000, 6000, Wave.NOISE, 0.12, 0.8);
            }
        }
        return Synth.toPcm(buf, gain);
    }

    private void voice(float[] buf, List<Note> notes, int perEighth, Wave wave, double volume) {
        int pos = 0;
        for (Note n : notes) {
            int len = n.eighths() * perEighth;
            if (n.midi() >= 0) {
                double f = Synth.frequency(n.midi());
                Synth.tone(buf, pos, (int) (len * legato), f, f, wave, volume, 0.35);
            }
            pos += len;
        }
    }
}
