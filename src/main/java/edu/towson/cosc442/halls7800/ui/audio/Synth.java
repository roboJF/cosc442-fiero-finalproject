package edu.towson.cosc442.halls7800.ui.audio;

import javax.sound.sampled.AudioFormat;

/**
 * A tiny chiptune synthesizer that renders the game's standard audio into mono 16-bit PCM using
 * square, pulse, triangle and noise oscillators with a simple envelope.
 */
final class Synth {

    /** Sample rate in Hz. */
    static final float RATE = 22050f;
    /** The format of every rendered buffer. */
    static final AudioFormat FORMAT = new AudioFormat(RATE, 16, 1, true, false);

    /** Oscillator shapes. */
    enum Wave {
        /** 50% duty square: bright and hollow. */
        SQUARE,
        /** 25% duty pulse: thinner, classic lead sound. */
        PULSE,
        /** Triangle: soft, used for bass and dark melodies. */
        TRIANGLE,
        /** Pitched noise: drums, hisses and crashes. */
        NOISE,
        /** Silence. */
        REST
    }

    private Synth() {
    }

    /**
     * @param midi a MIDI note number (60 = middle C)
     * @return its frequency in Hz
     */
    static double frequency(int midi) {
        return 440.0 * Math.pow(2, (midi - 69) / 12.0);
    }

    /**
     * Mixes one tone into a buffer, sweeping linearly from {@code f0} to {@code f1}.
     *
     * @param buf the mix buffer
     * @param start first sample
     * @param length number of samples
     * @param f0 starting frequency (Hz)
     * @param f1 ending frequency (Hz)
     * @param wave oscillator shape
     * @param volume peak amplitude (0..1)
     * @param release fraction of the tone spent fading out (0..1)
     */
    static void tone(float[] buf, int start, int length, double f0, double f1, Wave wave, double volume,
                     double release) {
        if (wave == Wave.REST || length <= 0) {
            return;
        }
        int attack = Math.min(length / 4, (int) (RATE * 0.004));
        int releaseStart = (int) (length * (1 - release));
        double phase = 0;
        int lfsr = 0x7ACE;
        double noiseValue = 1;
        for (int i = 0; i < length && start + i < buf.length; i++) {
            double t = (double) i / length;
            double f = f0 + (f1 - f0) * t;
            double previous = phase;
            phase = (phase + f / RATE) % 1.0;
            double sample = switch (wave) {
                case SQUARE -> phase < 0.5 ? 1 : -1;
                case PULSE -> phase < 0.25 ? 1 : -1;
                case TRIANGLE -> 4 * Math.abs(phase - 0.5) - 1;
                case NOISE -> {
                    if (phase < previous) {
                        int bit = (lfsr ^ (lfsr >> 1)) & 1;
                        lfsr = (lfsr >> 1) | (bit << 14);
                        noiseValue = (lfsr & 1) == 0 ? 1 : -1;
                    }
                    yield noiseValue;
                }
                default -> 0;
            };
            double env = 1;
            if (i < attack) {
                env = (double) i / Math.max(1, attack);
            } else if (i >= releaseStart) {
                env = 1 - (double) (i - releaseStart) / Math.max(1, length - releaseStart);
            }
            buf[start + i] += (float) (sample * env * volume);
        }
    }

    /**
     * Converts a mix buffer to 16-bit little-endian PCM, applying a master gain and clipping.
     *
     * @param buf the mix
     * @param gain master gain
     * @return PCM bytes in {@link #FORMAT}
     */
    static byte[] toPcm(float[] buf, double gain) {
        byte[] out = new byte[buf.length * 2];
        for (int i = 0; i < buf.length; i++) {
            double v = Math.max(-1, Math.min(1, buf[i] * gain));
            int s = (int) (v * 32000);
            out[2 * i] = (byte) s;
            out[2 * i + 1] = (byte) (s >> 8);
        }
        return out;
    }

    /**
     * @param seconds a duration
     * @return the number of samples it lasts
     */
    static int samples(double seconds) {
        return (int) (seconds * RATE);
    }
}
