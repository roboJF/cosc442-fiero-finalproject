package edu.towson.cosc442.halls7800.ui.audio;

import edu.towson.cosc442.halls7800.engine.GameEvent;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.SysexMessage;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Plays the game's synthesized themes and sound effects (see {@link Synth}) and the bundled MIDI theme.
 *
 * <p>Music and sound effects can be switched on and off independently; the choices are
 * remembered between runs. If the computer has no usable audio device, the player quietly does
 * nothing, so the game always runs.
 */
public class AudioPlayer {

    /** Overall loudness of the music (kept low so it stays in the background). */
    static final double MUSIC_GAIN = 0.3;
    private static final double MIDI_GAIN = 0.6;
    /** Overall loudness of the sound effects. */
    static final double SFX_GAIN = 0.8;

    private static final String PREF_MUSIC = "music";
    private static final String PREF_SFX = "sfx";

    private final Preferences prefs = Preferences.userRoot().node("edu/towson/cosc442/halls7800");
    private final Map<Tune, Clip> tuneClips = new EnumMap<>(Tune.class);
    private final Map<Sound, Clip> soundClips = new EnumMap<>(Sound.class);
    private boolean available = true;
    private boolean musicOn;
    private boolean sfxOn;
    private Tune current;
    private Sequencer sequencer;
    private Synthesizer midiSynth;
    private boolean midiUnavailable;

    /** Creates the player, reading the saved music and sound settings. */
    public AudioPlayer() {
        musicOn = prefs.getBoolean(PREF_MUSIC, true);
        sfxOn = prefs.getBoolean(PREF_SFX, true);
        try {
            AudioSystem.getClip().close();
        } catch (Exception | LinkageError e) {
            available = false;
        }
    }

    /**
     * Starts a tune, unless it is already playing. Looping tunes repeat until another tune
     * starts; jingles play once.
     *
     * @param tune the tune, or {@code null} for silence
     */
    public void playMusic(Tune tune) {
        if (tune == current) {
            return;
        }
        stopMusic();
        current = tune;
        startCurrent();
    }

    private void startCurrent() {
        if (current == null || !available || !musicOn) {
            return;
        }
        if (current.midiResource() != null) {
            startMidi();
            return;
        }
        try {
            Clip clip = tuneClips.computeIfAbsent(current, t -> open(t.render(MUSIC_GAIN)));
            if (clip == null) {
                return;
            }
            clip.setFramePosition(0);
            if (current.loops()) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.start();
            }
        } catch (RuntimeException e) {
            available = false;
        }
    }

    private void stopMusic() {
        if (sequencer != null) {
            sequencer.stop();
        }
        if (current != null) {
            Clip clip = tuneClips.get(current);
            if (clip != null) {
                clip.stop();
            }
        }
    }

    private void startMidi() {
        if (midiUnavailable) {
            return;
        }
        try {
            if (sequencer == null) {
                openMidi();
            }
            sequencer.setTickPosition(0);
            sequencer.start();
        } catch (IOException | InvalidMidiDataException | MidiUnavailableException | RuntimeException | LinkageError e) {
            closeMidi();
            midiUnavailable = true;
        }
    }

    private void openMidi() throws IOException, InvalidMidiDataException, MidiUnavailableException {
        URL resource = AudioPlayer.class.getResource(current.midiResource());
        if (resource == null) {
            throw new IOException("Missing MIDI resource " + current.midiResource());
        }
        Sequence sequence = MidiSystem.getSequence(resource);
        sequencer = MidiSystem.getSequencer(false);
        sequencer.open();
        midiSynth = MidiSystem.getSynthesizer();
        midiSynth.open();
        Receiver output = midiSynth.getReceiver();
        sequencer.getTransmitter().setReceiver(output);
        // MIDI master volume is a 14-bit value, sent least significant byte first.
        int volume = (int) Math.round(MIDI_GAIN * 0x3fff);
        byte[] masterVolume = {0x7f, 0x7f, 0x04, 0x01, (byte) (volume & 0x7f), (byte) (volume >> 7), (byte) 0xf7};
        output.send(new SysexMessage(SysexMessage.SYSTEM_EXCLUSIVE, masterVolume, masterVolume.length), -1);
        sequencer.setSequence(sequence);
        sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
    }

    private void closeMidi() {
        if (sequencer != null) {
            sequencer.close();
            sequencer = null;
        }
        if (midiSynth != null) {
            midiSynth.close();
            midiSynth = null;
        }
    }

    /**
     * Plays a sound effect, restarting it if it is already playing.
     *
     * @param sound the effect
     */
    public void play(Sound sound) {
        if (!available || !sfxOn || sound == null) {
            return;
        }
        try {
            Clip clip = soundClips.computeIfAbsent(sound, s -> open(s.render(SFX_GAIN)));
            if (clip != null) {
                clip.stop();
                clip.setFramePosition(0);
                clip.start();
            }
        } catch (RuntimeException e) {
            available = false;
        }
    }

    /**
     * Plays the sound effect for a game event, if it has one.
     *
     * @param event the event
     */
    public void onEvent(GameEvent event) {
        play(soundFor(event));
    }

    /**
     * @param event a game event
     * @return the effect that goes with it, or {@code null} for none
     */
    static Sound soundFor(GameEvent event) {
        return switch (event.type()) {
            case ATTACK_HIT -> Sound.HIT;
            case ATTACK_MISSED -> Sound.MISS;
            case PLAYER_HURT -> Sound.HURT;
            case CREATURE_DIED -> Sound.MONSTER_DIES;
            case BOSS_DEFEATED -> Sound.BOSS_DIES;
            case BOSS_AWAKENED -> Sound.BOSS_ROAR;
            case ITEM_PICKED_UP -> Sound.PICKUP;
            case GOLD_PICKED_UP -> Sound.GOLD;
            case POTION_DRUNK -> Sound.POTION;
            case SCROLL_READ -> Sound.SCROLL;
            case FOOD_EATEN -> Sound.FOOD;
            case ITEM_EQUIPPED -> Sound.EQUIP;
            case WAND_ZAPPED -> Sound.ZAP;
            case TRAP_TRIGGERED -> Sound.TRAP;
            case LEVEL_UP -> Sound.LEVEL_UP;
            case ACHIEVEMENT -> Sound.ACHIEVEMENT;
            case DOOR_CHANGED -> Sound.DOOR;
            case FLOOR_CHANGED -> Sound.STAIRS;
            case TALKED -> "Zombie Kid".equals(event.message()) ? Sound.TURTLES : Sound.TALK;
            default -> null;
        };
    }

    /** Turns music on or off and remembers the choice. */
    public void toggleMusic() {
        musicOn = !musicOn;
        prefs.putBoolean(PREF_MUSIC, musicOn);
        if (musicOn) {
            startCurrent();
        } else {
            stopMusic();
        }
    }

    /** Turns sound effects on or off and remembers the choice. */
    public void toggleSoundEffects() {
        sfxOn = !sfxOn;
        prefs.putBoolean(PREF_SFX, sfxOn);
    }

    /** @return {@code true} if music is switched on */
    public boolean isMusicOn() {
        return musicOn;
    }

    /** @return {@code true} if sound effects are switched on */
    public boolean isSoundOn() {
        return sfxOn;
    }

    /** @return {@code true} if an audio device is available */
    public boolean isAvailable() {
        return available;
    }

    /** Stops everything and releases the audio device. */
    public void close() {
        closeMidi();
        for (Clip c : tuneClips.values()) {
            c.close();
        }
        for (Clip c : soundClips.values()) {
            c.close();
        }
        tuneClips.clear();
        soundClips.clear();
    }

    private Clip open(byte[] pcm) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(Synth.FORMAT, pcm, 0, pcm.length);
            return clip;
        } catch (Exception e) {
            available = false;
            return null;
        }
    }
}
