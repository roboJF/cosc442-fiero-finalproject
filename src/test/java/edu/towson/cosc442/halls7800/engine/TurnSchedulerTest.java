package edu.towson.cosc442.halls7800.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.towson.cosc442.halls7800.gen.FixedFloorSource;
import org.junit.jupiter.api.Test;

class TurnSchedulerTest {

    /** An actor that counts its actions and always spends the same amount of time. */
    private static final class CountingActor extends Actor {
        private final int cost;
        private int actions;

        CountingActor(int cost) {
            this.cost = cost;
        }

        @Override
        protected int act(GameSession session) {
            actions++;
            return cost;
        }
    }

    @Test
    void fasterActorActsMoreOften() {
        GameSession game = new GameSession(1L, new FixedFloorSource().add("Room", "#####", "#@..#", "#####"), "Tester");
        CountingActor fast = new CountingActor(50);
        CountingActor normal = new CountingActor(100);
        game.scheduler().add(fast, 1);
        game.scheduler().add(normal, 1);

        for (int i = 0; i < 4; i++) {
            game.submit(new WaitCommand());
        }

        assertEquals(8, fast.actions);
        assertEquals(4, normal.actions);
    }
}
