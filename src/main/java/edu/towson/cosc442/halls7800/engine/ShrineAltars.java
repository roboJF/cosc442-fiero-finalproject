package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.item.EquippableItem;
import edu.towson.cosc442.halls7800.item.Identification;
import edu.towson.cosc442.halls7800.item.Item;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Terrain;

/** The identification and curse removal offered by dungeon shrines. */
final class ShrineAltars {

    private static final int IDENTIFY_COST = 1;
    private static final int CLEANSE_COST = 2;

    private ShrineAltars() {
    }

    private static boolean isShrine(GameSession session, Position position) {
        return session.actionRules().usesShrineAltars()
                && session.depth() > GameRules.HUB_DEPTH
                && session.depth() < GameRules.FINAL_DEPTH
                && session.currentFloor().terrainAt(position) == Terrain.ALTAR;
    }

    /** Applies any shrine effect before the dropped item is copied into the floor pile. */
    static String onDrop(GameSession session, Position position, Item item) {
        if (!isShrine(session, position)) {
            return "";
        }
        FloorContents contents = session.contents();
        int charges = contents.shrineChargesAt(position);
        if (charges == 0) {
            return "";
        }

        Identification identification = session.identification();
        if (!identification.isIdentified(item)) {
            identification.identify(item);
            contents.spendShrineCharges(position, IDENTIFY_COST);
            String message = " The shrine reveals " + session.describe(item) + ".";
            if (item instanceof EquippableItem equipment) {
                message += equipment.isCursed() ? " It is cursed." : " It is uncursed.";
            }
            return message;
        }
        if (charges >= CLEANSE_COST && item instanceof EquippableItem equipment && equipment.isCursed()) {
            equipment.removeCurse();
            contents.spendShrineCharges(position, CLEANSE_COST);
            return " The shrine lifts the curse, leaving " + session.describe(item) + ".";
        }
        return "";
    }

    static String examine(GameSession session, Position position) {
        if (!isShrine(session, position)) {
            return "";
        }
        int charges = session.contents().shrineChargesAt(position);
        if (charges == 0) {
            return " The shrine altar is dormant.";
        }
        return " The shrine altar has " + charges + (charges == 1 ? " charge" : " charges") + " remaining.";
    }
}
