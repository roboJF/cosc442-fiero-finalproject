package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.entity.Player;
import edu.towson.cosc442.halls7800.item.Gold;
import edu.towson.cosc442.halls7800.item.Inventory;
import edu.towson.cosc442.halls7800.item.Item;
import edu.towson.cosc442.halls7800.world.Position;
import java.util.List;

/**
 * Picks up the top item of the pile under the player (the most recently dropped).
 */
public final class PickUpCommand implements Command {

    @Override
    public String encode() {
        return "G";
    }

    @Override
    public CommandResult execute(GameSession session) {
        Player player = session.player();
        Position here = player.position();
        List<Item> pile = session.itemsAt(here);
        if (pile.isEmpty()) {
            return CommandResult.rejected("There is nothing here to pick up.");
        }
        Item item = pile.get(pile.size() - 1);
        if (item instanceof Gold gold) {
            session.contents().removeItem(here, gold);
            player.addGold(gold.quantity());
            session.stats().addGoldCollected(gold.quantity());
            session.signal(GameEvent.Type.GOLD_PICKED_UP, here);
            return CommandResult.acted(player.actionCost(), "You pick up " + gold.quantity() + " gold.");
        }
        if (!player.inventory().canAdd(item)) {
            return CommandResult.rejected("Your pack is full.");
        }
        String description = session.describe(item);
        session.contents().removeItem(here, item);
        player.inventory().add(item);
        session.signal(GameEvent.Type.ITEM_PICKED_UP, here);
        int slot = player.inventory().indexOf(item);
        String letter = slot >= 0 ? String.valueOf(Inventory.letterFor(slot)) : "?";
        return CommandResult.acted(player.actionCost(), "You pick up " + description + " (" + letter + ").");
    }
}
