package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.entity.Player;
import edu.towson.cosc442.halls7800.item.EquippableItem;
import edu.towson.cosc442.halls7800.item.Item;

/**
 * Drops a whole stack from the pack onto the floor. Equipped items are unequipped first, unless
 * they are cursed.
 *
 * @param slot the pack slot index (0 = 'a')
 */
public record DropCommand(int slot) implements Command {

    @Override
    public String encode() {
        return "R:" + slot;
    }

    @Override
    public CommandResult execute(GameSession session) {
        Player player = session.player();
        Item item = player.inventory().get(slot);
        if (item == null) {
            return CommandResult.rejected("You don't have that item.");
        }
        if (player.isEquipped(item)) {
            if (item instanceof EquippableItem e && e.isCursed()) {
                return CommandResult.rejected("The cursed " + session.describe(item).replaceFirst("^an? ", "")
                        + " won't come off!");
            }
            if (item == player.weapon()) {
                player.setWeapon(null);
            } else {
                player.setWornArmor(null);
            }
        }
        String description = session.describe(item);
        player.inventory().remove(item, item.quantity());
        session.placeItem(player.position(), item);
        return CommandResult.acted(player.actionCost(), "You drop " + description + ".");
    }
}
