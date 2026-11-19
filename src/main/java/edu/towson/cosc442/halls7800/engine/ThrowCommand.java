package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.combat.Combat;
import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.entity.Player;
import edu.towson.cosc442.halls7800.item.Item;
import edu.towson.cosc442.halls7800.item.ThrownWeapon;
import edu.towson.cosc442.halls7800.world.LineOfSight;
import edu.towson.cosc442.halls7800.world.Position;
import java.util.List;

/**
 * Throws one missile from a pack slot at the nearest visible monster within range. The missile
 * lands under the target (or where its flight stopped) and can be picked up again.
 *
 * @param slot the pack slot index (0 = 'a')
 */
public record ThrowCommand(int slot) implements Command {

    @Override
    public String encode() {
        return "T:" + slot;
    }

    @Override
    public CommandResult execute(GameSession session) {
        Player player = session.player();
        Item item = player.inventory().get(slot);
        if (item == null) {
            return CommandResult.rejected("You don't have that item.");
        }
        if (!(item instanceof ThrownWeapon missiles)) {
            return CommandResult.rejected("That isn't made for throwing.");
        }
        Monster target = nearestTarget(session);
        if (target == null) {
            return CommandResult.rejected("There is nothing in range to throw at.");
        }
        ThrownWeapon one = (ThrownWeapon) player.inventory().removeOne(missiles);
        session.signal(GameEvent.Type.ITEM_USED, player.position());
        List<Position> flight = LineOfSight.trajectory(session.floor(), player.position(), target.position(),
                ThrownWeapon.RANGE);
        Position landing = flight.isEmpty() ? player.position() : flight.get(flight.size() - 1);
        if (landing.equals(target.position())) {
            Combat.attackWith(session, player, target, one.profile(player.accuracy()), true);
            session.placeItem(target.position(), one);
        } else {
            session.placeItem(landing, one);
            session.message("The " + one.name(session.identification()) + " falls short.");
        }
        return CommandResult.acted(player.actionCost());
    }

    /**
     * @param session the game
     * @return the closest visible monster within throwing range with a clear line, or {@code null}
     */
    static Monster nearestTarget(GameSession session) {
        Position from = session.player().position();
        Monster best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Monster m : session.monsters()) {
            int d = from.distanceTo(m.position());
            if (d <= ThrownWeapon.RANGE && d < bestDistance && session.currentFloor().isVisible(m.position())
                    && LineOfSight.isClear(session.floor(), from, m.position())) {
                best = m;
                bestDistance = d;
            }
        }
        return best;
    }
}
