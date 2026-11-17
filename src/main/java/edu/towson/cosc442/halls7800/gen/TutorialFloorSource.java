package edu.towson.cosc442.halls7800.gen;

import edu.towson.cosc442.halls7800.engine.FloorContents;
import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.entity.MonsterType;
import edu.towson.cosc442.halls7800.entity.Npc;
import edu.towson.cosc442.halls7800.item.Food;
import edu.towson.cosc442.halls7800.item.Gold;
import edu.towson.cosc442.halls7800.item.Potion;
import edu.towson.cosc442.halls7800.item.Scroll;
import edu.towson.cosc442.halls7800.item.ThrownWeapon;
import edu.towson.cosc442.halls7800.item.Weapon;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.FloorParser;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.engine.FloorSource;
import edu.towson.cosc442.halls7800.world.Trap;

/**
 * The Orientation tutorial ("level 0"): a single hand-drawn floor of five rooms, each with signs
 * that explain one part of the game. Taking the stairs down ends the tutorial.
 */
public class TutorialFloorSource implements FloorSource {

    /** Fixed seed: the tutorial is the same every time. */
    public static final long SEED = 7800L;

    /** The signs, in the order the player meets them, with their positions. */
    static final Object[][] SIGNS = {
        {new Position(5, 2), "ORIENTATION, ROOM 1: Welcome to TU, new Tiger! Move with the arrow keys or W A S D. "
                + "Move diagonally with Q E Z C. Read a sign by walking into it, just as you did now."},
        {new Position(8, 6), "Doors look like brown panels (+). Walk into a door to open it, and press X to close it "
                + "again. Press Space to wait a turn. Press ? at any time for every control and symbol."},
        {new Position(12, 2), "ROOM 2, ITEMS: ! potion  ? scroll  ) weapon  [ armor  / thrown weapon  % food  $ gold. "
                + "Stand on an item and press G to pick it up. Gold is collected automatically."},
        {new Position(19, 6), "Your pack is listed on the right, one letter per item. Press U and then a letter to use "
                + "something: wield, wear, drink, read or eat. Unknown potions and scrolls reveal themselves when used."},
        {new Position(23, 2), "ROOM 3, MONSTERS: Monsters are colored letters in dark circles. A small 'z' means it is "
                + "asleep, and a bar above it shows its health. Walk into a monster to attack it."},
        {new Position(23, 6), "Press T and then the letter of your darts to throw one at the nearest monster in view. "
                + "Thrown darts land on the floor; pick them up again with G. Go ahead: the dummy can't hit back."},
        {new Position(34, 2), "ROOM 4, TRAPS: The colored ^ on the floor is a trap. Walk around it! Most traps are hidden "
                + "until you trigger them or find them by pressing F to search nearby."},
        {new Position(40, 6), "The > is a staircase down; stand on it and press > to finish Orientation. But first, the "
                + "east wall here sounds hollow... try searching (F) next to it a few times."},
        {new Position(48, 2), "You found a secret room! Searching pays off. In the real Halls, walk into the people of the "
                + "Atrium to talk, press J for your quest journal, and good luck finding the Diploma."},
    };

    @Override
    public Floor createFloor(int depth) {
        if (depth != 0) {
            throw new IllegalArgumentException("Orientation has only one floor");
        }
        return FloorParser.parse("Orientation Hall", 0, FixedLayouts.ORIENTATION);
    }

    @Override
    public int deepestDepth() {
        return 0;
    }

    @Override
    public FloorContents populate(Floor floor) {
        FloorContents contents = new FloorContents();
        for (Object[] sign : SIGNS) {
            contents.addNpc(new Npc(Npc.Role.SIGN, (Position) sign[0], (String) sign[1]));
        }
        contents.addItem(new Position(14, 4), new Potion(Potion.Kind.HEALING, 1));
        contents.addItem(new Position(16, 3), new ThrownWeapon(ThrownWeapon.Kind.DART, 5));
        contents.addItem(new Position(17, 5), new Weapon(Weapon.Kind.SHORT_SWORD, 0, false));
        contents.addItem(new Position(18, 4), new Gold(25));
        contents.addItem(new Position(15, 6), new Food(Food.Kind.ENERGY_BAR, 1));
        contents.addMonster(new Monster(MonsterType.TRAINING_DUMMY, new Position(28, 4), true));
        Trap trap = new Trap(Trap.Type.SPIKE, false);
        contents.addTrap(new Position(37, 4), trap);
        contents.addItem(new Position(48, 4), new Scroll(Scroll.Kind.DOCUMENTATION, 1));
        return contents;
    }

    @Override
    public String welcomeMessage() {
        return "Welcome to Towson University Orientation! Walk into the sign to the northeast to begin.";
    }

    @Override
    public boolean isTutorial() {
        return true;
    }
}
