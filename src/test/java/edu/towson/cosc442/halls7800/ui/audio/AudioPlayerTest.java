package edu.towson.cosc442.halls7800.ui.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.towson.cosc442.halls7800.engine.GameEvent;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AudioPlayerTest {

    @ParameterizedTest
    @CsvSource({
            "ATTACK_HIT, HIT, KRIS_HIT",
            "CREATURE_DIED, MONSTER_DIES, KRIS_DAMAGE",
            "BOSS_DEFEATED, BOSS_DIES, KRIS_DAMAGE",
            "POTION_DRUNK, POTION, KRIS_POWER",
            "SCROLL_READ, SCROLL, KRIS_POWER",
            "FOOD_EATEN, FOOD, KRIS_POWER",
            "ITEM_EQUIPPED, EQUIP, KRIS_POWER",
            "WAND_ZAPPED, ZAP, KRIS_POWER",
            "ITEM_USED, , KRIS_POWER",
            "ATTACK_MISSED, MISS, MISS",
            "PLAYER_HURT, HURT, ",
            "PLAYER_DAMAGED, , KRIS_HURT",
            "ITEM_PICKED_UP, PICKUP, PICKUP"
    })
    void selectsSoundForEachCharacterMode(GameEvent.Type type, Sound standard, Sound kris) {
        GameEvent event = new GameEvent(type, null, null, 0);

        assertEquals(standard, AudioPlayer.soundFor(event, false));
        assertEquals(kris, AudioPlayer.soundFor(event, true));
    }

    @ParameterizedTest
    @CsvSource({"KRIS_HIT, snd_hit.wav", "KRIS_HURT, snd_hurt1.wav", "KRIS_DAMAGE, snd_damage.wav",
            "KRIS_POWER, snd_power.wav"})
    void krisSoundLoadsFromTheClasspath(Sound sound, String fileName) throws Exception {
        assertEquals("/audio/" + fileName, sound.wavResource());
        URL resource = AudioPlayer.class.getResource(sound.wavResource());
        assertNotNull(resource);
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(resource)) {
            assertTrue(stream.readAllBytes().length > 0);
        }
    }
}
