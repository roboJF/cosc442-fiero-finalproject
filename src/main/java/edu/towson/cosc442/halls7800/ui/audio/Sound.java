package edu.towson.cosc442.halls7800.ui.audio;

import edu.towson.cosc442.halls7800.ui.audio.Synth.Wave;
import java.util.List;

/**
 * The game's sound effects, synthesized from short sequences of tones or loaded from bundled WAVs.
 */
public enum Sound {
    HIT(seg(2000, 800, 40, Wave.NOISE, 0.5), seg(220, 110, 60, Wave.SQUARE, 0.4)),
    HURT(seg(320, 120, 120, Wave.PULSE, 0.5), seg(900, 300, 50, Wave.NOISE, 0.35)),
    MISS(seg(1200, 400, 70, Wave.NOISE, 0.22)),
    MONSTER_DIES(seg(440, 220, 50, Wave.SQUARE, 0.4), seg(330, 165, 50, Wave.SQUARE, 0.4),
            seg(220, 80, 110, Wave.SQUARE, 0.4)),
    BOSS_DIES(seg(392, 196, 120, Wave.SQUARE, 0.45), seg(330, 165, 120, Wave.SQUARE, 0.45),
            seg(262, 98, 200, Wave.SQUARE, 0.45), seg(400, 60, 400, Wave.NOISE, 0.4)),
    BOSS_ROAR(seg(400, 80, 380, Wave.NOISE, 0.5), seg(110, 55, 380, Wave.SQUARE, 0.4)),
    PICKUP(seg(660, 660, 50, Wave.PULSE, 0.35), seg(990, 990, 70, Wave.PULSE, 0.35)),
    GOLD(seg(1047, 1047, 60, Wave.PULSE, 0.3), seg(1568, 1568, 120, Wave.PULSE, 0.3)),
    DOOR(seg(300, 150, 80, Wave.NOISE, 0.35), seg(120, 90, 50, Wave.SQUARE, 0.2)),
    STAIRS(seg(523, 523, 60, Wave.TRIANGLE, 0.5), seg(440, 440, 60, Wave.TRIANGLE, 0.5),
            seg(349, 349, 60, Wave.TRIANGLE, 0.5), seg(262, 262, 120, Wave.TRIANGLE, 0.5)),
    POTION(seg(400, 800, 60, Wave.TRIANGLE, 0.5), seg(600, 1000, 60, Wave.TRIANGLE, 0.5),
            seg(500, 900, 80, Wave.TRIANGLE, 0.5)),
    SCROLL(seg(784, 784, 40, Wave.PULSE, 0.3), seg(988, 988, 40, Wave.PULSE, 0.3),
            seg(1175, 1175, 40, Wave.PULSE, 0.3), seg(1568, 1568, 90, Wave.PULSE, 0.3)),
    EQUIP(seg(880, 880, 30, Wave.SQUARE, 0.3), seg(660, 660, 60, Wave.SQUARE, 0.3)),
    FOOD(seg(700, 500, 40, Wave.NOISE, 0.3), seg(0, 0, 30, Wave.REST, 0), seg(700, 500, 40, Wave.NOISE, 0.3)),
    ZAP(seg(200, 1600, 150, Wave.SQUARE, 0.35), seg(1600, 600, 90, Wave.NOISE, 0.3)),
    TRAP(seg(3000, 200, 220, Wave.NOISE, 0.5)),
    LEVEL_UP(seg(523, 523, 80, Wave.PULSE, 0.35), seg(659, 659, 80, Wave.PULSE, 0.35),
            seg(784, 784, 80, Wave.PULSE, 0.35), seg(1047, 1047, 240, Wave.PULSE, 0.35)),
    TALK(seg(700, 700, 25, Wave.SQUARE, 0.2), seg(0, 0, 15, Wave.REST, 0), seg(760, 760, 25, Wave.SQUARE, 0.2)),
    TURTLES(seg(520, 520, 70, Wave.SQUARE, 0.3), seg(480, 480, 70, Wave.SQUARE, 0.3),
            seg(440, 440, 70, Wave.SQUARE, 0.3), seg(300, 280, 200, Wave.SQUARE, 0.3)),
    ACHIEVEMENT(seg(784, 784, 80, Wave.PULSE, 0.3), seg(1047, 1047, 80, Wave.PULSE, 0.3),
            seg(1319, 1319, 200, Wave.PULSE, 0.3)),
    KRIS_HIT("/audio/snd_hit.wav"),
    KRIS_HURT("/audio/snd_hurt1.wav"),
    KRIS_DAMAGE("/audio/snd_damage.wav"),
    KRIS_POWER("/audio/snd_power.wav");

    /** One piece of a sound effect. */
    private record Segment(double f0, double f1, int millis, Wave wave, double volume) {
    }

    private final List<Segment> segments;
    private final String wavResource;

    Sound(Segment... parts) {
        segments = List.of(parts);
        wavResource = null;
    }

    Sound(String wavResource) {
        segments = List.of();
        this.wavResource = wavResource;
    }

    String wavResource() {
        return wavResource;
    }

    private static Segment seg(double f0, double f1, int millis, Wave wave, double volume) {
        return new Segment(f0, f1, millis, wave, volume);
    }

    /**
     * Renders the effect.
     *
     * @param gain master gain
     * @return PCM bytes in {@link Synth#FORMAT}
     */
    byte[] render(double gain) {
        int total = 0;
        for (Segment s : segments) {
            total += Synth.samples(s.millis() / 1000.0);
        }
        float[] buf = new float[total + Synth.samples(0.02)];
        int pos = 0;
        for (Segment s : segments) {
            int len = Synth.samples(s.millis() / 1000.0);
            Synth.tone(buf, pos, len, s.f0(), s.f1(), s.wave(), s.volume(), 0.3);
            pos += len;
        }
        return Synth.toPcm(buf, gain);
    }
}
