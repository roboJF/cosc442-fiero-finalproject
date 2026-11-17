package edu.towson.cosc442.halls7800.ui.audio;

import edu.towson.cosc442.halls7800.engine.GameEvent;
import java.util.EnumMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Plays the game's music and sound effects. Every sound is synthesized in code (see
 * {@link Synth}); there are no audio files.
 *
 * <p>Music and sound effects can be switched on and off independently; the choices are
 * remembered between runs. If the computer has no usable audio device, the player quietly does
 * nothing, so the game always runs.
 */
public class AudioPlayer {

    /** Overall loudness of the music (kept low so it stays in the background). */
    static final double MUSIC_GAIN = 0.3;
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
        if (current != null) {
            Clip clip = tuneClips.get(current);
            if (clip != null) {
                clip.stop();
            }
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
