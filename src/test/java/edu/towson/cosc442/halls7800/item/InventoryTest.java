package edu.towson.cosc442.halls7800.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class InventoryTest {

    @Test
    void matchingPotionsShareOneSlot() {
        Inventory pack = new Inventory(5);

        pack.add(new Potion(Potion.Kind.HEALING, 1));
        pack.add(new Potion(Potion.Kind.HEALING, 2));

        assertEquals(1, pack.size());
        assertEquals(3, pack.get(0).quantity());
    }
}
