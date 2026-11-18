package edu.towson.cosc442.halls7800.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.towson.cosc442.halls7800.item.Item;
import edu.towson.cosc442.halls7800.item.Potion;
import edu.towson.cosc442.halls7800.item.Weapon;
import edu.towson.cosc442.halls7800.world.Position;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FloorContentsTest {

    @Test
    void pileSnapshotsRejectCollectionWritesAndDetachTheirItems() {
        FloorContents contents = new FloorContents();
        Position tile = new Position(2, 1);
        Potion potion = new Potion(Potion.Kind.HEALING, 2);
        Weapon weapon = new Weapon(Weapon.Kind.DAGGER, 2, true);
        contents.addItem(tile, potion);
        contents.addItem(tile, weapon);
        contents.addItem(tile, new Potion(Potion.Kind.HEALING, 3));

        Map<Position, List<Item>> snapshot = contents.piles();
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertThrows(UnsupportedOperationException.class, snapshot.get(tile)::clear);
        assertThrows(UnsupportedOperationException.class, () -> contents.itemsAt(tile).clear());

        potion.setQuantity(9);
        weapon.addUpgrade(3);
        snapshot.get(tile).get(0).setQuantity(20);
        Weapon snapshotWeapon = (Weapon) contents.itemsAt(tile).get(1);
        assertEquals(2, snapshotWeapon.upgrade());
        snapshotWeapon.addUpgrade(-10);

        List<Item> stored = contents.itemsAt(tile);
        assertEquals(2, stored.size());
        assertEquals(5, stored.get(0).quantity());
        assertEquals(2, ((Weapon) stored.get(1)).upgrade());
    }
}
