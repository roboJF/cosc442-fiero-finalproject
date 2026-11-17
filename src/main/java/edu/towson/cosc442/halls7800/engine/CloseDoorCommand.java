package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.world.Direction;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Terrain;
import java.util.ArrayList;
import java.util.List;

/**
 * Closes an adjacent open door. With no direction, closes the only open door next to the player.
 */
public final class CloseDoorCommand implements Command {

    private final Direction direction;

    /**
     * @param direction where the door is, or {@code null} to pick the single adjacent open door
     */
    public CloseDoorCommand(Direction direction) {
        this.direction = direction;
    }

    /** @return a command that closes the only adjacent open door */
    public static CloseDoorCommand nearest() {
        return new CloseDoorCommand(null);
    }

    @Override
    public String encode() {
        return "X:" + (direction == null ? "*" : direction.name());
    }

    @Override
    public CommandResult execute(GameSession session) {
        Floor floor = session.floor();
        Position here = session.player().position();
        Position target;
        if (direction != null) {
            target = here.step(direction);
            if (floor.terrainAt(target) != Terrain.DOOR_OPEN) {
                return CommandResult.rejected("There is no open door there.");
            }
        } else {
            List<Position> candidates = new ArrayList<>();
            for (Position p : here.neighbors()) {
                if (floor.terrainAt(p) == Terrain.DOOR_OPEN) {
                    candidates.add(p);
                }
            }
            if (candidates.isEmpty()) {
                return CommandResult.rejected("There is no open door next to you.");
            }
            if (candidates.size() > 1) {
                return CommandResult.rejected("Several open doors are next to you; step closer to one.");
            }
            target = candidates.get(0);
        }
        if (session.monsterAt(target) != null) {
            return CommandResult.rejected("Something is standing in the doorway.");
        }
        floor.setTerrain(target, Terrain.DOOR_CLOSED);
        session.publish(new GameEvent(GameEvent.Type.DOOR_CHANGED, null, target, floor.depth()));
        return CommandResult.acted(session.player().actionCost(), "You close the door.");
    }
}
