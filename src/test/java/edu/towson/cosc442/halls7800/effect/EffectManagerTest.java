package edu.towson.cosc442.halls7800.effect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EffectManagerTest {

    @Test
    void repeatedPoisonIntensifies() {
        EffectManager effects = new EffectManager();
        effects.apply(EffectType.POISONED, 4, 1);
        effects.apply(EffectType.POISONED, 2, 1);

        assertEquals(2, effects.magnitude(EffectType.POISONED));
        assertEquals(4, effects.remaining(EffectType.POISONED));
    }
}
