package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.combat.Combat;
import edu.towson.cosc442.halls7800.effect.EffectType;
import edu.towson.cosc442.halls7800.entity.Companion;
import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.entity.MonsterAi;
import edu.towson.cosc442.halls7800.entity.Npc;
import edu.towson.cosc442.halls7800.entity.Player;
import edu.towson.cosc442.halls7800.item.KeyItem;
import edu.towson.cosc442.halls7800.quest.Conversations;
import edu.towson.cosc442.halls7800.util.RandomStream;
import edu.towson.cosc442.halls7800.world.Direction;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Terrain;
import java.util.Objects;

/**
 * Moves the player one tile. Walking into a monster attacks it, walking into a friendly character
 * talks to it, walking into a closed door opens it, and walking into a locked vault door with the
 * floor's key unlocks it. A confused player sometimes stumbles in a random direction instead.
 *
 * @param direction the direction the player intends to step
 */
public record MoveCommand(Direction direction) implements Command {

    /**
     * @param direction the direction to step; must not be {@code null}
     */
    public MoveCommand {
        Objects.requireNonNull(direction, "direction");
    }

    @Override
    public String encode() {
        return "M:" + direction.name();
    }

    @Override
    public CommandResult execute(GameSession session) {
        Player player = session.player();
        Floor floor = session.floor();
        int cost = player.actionCost();
        Direction actual = direction;
        boolean stumbled = false;
        if (player.effects().has(EffectType.CONFUSED)
                && session.rng(RandomStream.EFFECTS).percent(MonsterAi.CONFUSED_PERCENT)) {
            actual = Direction.values()[session.rng(RandomStream.EFFECTS).nextInt(Direction.values().length)];
            stumbled = actual != direction;
        }

        Position from = player.position();
        Position target = from.step(actual);
        Companion doc = session.companion();
        if (doc != null && doc.position().equals(target) && floor.canStep(from, actual, false)) {
            doc.setPosition(from);
            session.moveCreature(player, target);
            return CommandResult.acted(cost, "You swap places with " + Companion.NAME + ".");
        }
        Npc npc = session.npcAt(target);
        if (npc != null && !stumbled) {
            session.publish(new GameEvent(GameEvent.Type.TALKED, npc.name(), target, floor.depth()));
            return CommandResult.acted(cost, Conversations.talk(session, npc));
        }
        Monster monster = session.monsterAt(target);
        if (monster != null) {
            if (!floor.canStep(from, actual, false)) {
                return stumbled ? CommandResult.acted(cost, "You stumble about in confusion.")
                        : CommandResult.rejected("You can't reach " + monster.object() + " from here.");
            }
            Combat.attack(session, player, monster, false);
            return CommandResult.acted(player.attackCost());
        }

        if (!floor.isInBounds(target)) {
            return CommandResult.rejected("You can't go that way.");
        }
        Terrain terrain = floor.terrainAt(target);
        if (terrain == Terrain.DOOR_CLOSED) {
            return openDoor(session, floor, target, actual, cost);
        }
        if (terrain == Terrain.DOOR_LOCKED) {
            if (stumbled) {
                return CommandResult.acted(cost, "You stumble into a locked door.");
            }
            return unlock(session, player, floor, target, cost);
        }
        if (!floor.canStep(from, actual, false)) {
            if (stumbled) {
                return CommandResult.acted(cost, "You stumble into the " + terrain.description() + ".");
            }
            if (terrain.isPassable()) {
                return CommandResult.rejected("You can't squeeze through diagonally there.");
            }
            return CommandResult.rejected("There is a " + terrain.description() + " in the way.");
        }

        session.moveCreature(player, target);
        if (terrain == Terrain.WATER) {
            cost += GameRules.WATER_EXTRA_COST;
        }
        String message = stumbled ? "You stumble in confusion." : arrivalMessage(terrain);
        return CommandResult.acted(cost, message);
    }

    private static CommandResult unlock(GameSession session, Player player, Floor floor, Position door, int cost) {
        for (KeyItem key : player.inventory().ofType(KeyItem.class)) {
            if (key.opens(floor.depth()) && !key.isMaster()) {
                player.inventory().removeOne(key);
                return openVault(session, floor, door, cost, "You unlock the vault door. The key stays stuck in the lock.");
            }
        }
        for (KeyItem key : player.inventory().ofType(KeyItem.class)) {
            if (key.isMaster()) {
                return openVault(session, floor, door, cost, "Your master key turns smoothly in the lock.");
            }
        }
        if (!player.inventory().ofType(KeyItem.class).isEmpty()) {
            return CommandResult.rejected("The door is locked, and none of your keys fit.");
        }
        return CommandResult.rejected("The door is locked. Somewhere on this floor there must be a key.");
    }

    private static CommandResult openVault(GameSession session, Floor floor, Position door, int cost, String text) {
        floor.setTerrain(door, Terrain.DOOR_OPEN);
        session.quests().onVaultUnlocked();
        session.signal(GameEvent.Type.ITEM_USED, door);
        session.publish(new GameEvent(GameEvent.Type.DOOR_CHANGED, null, door, floor.depth()));
        return CommandResult.acted(cost, text);
    }

    private static CommandResult openDoor(GameSession session, Floor floor, Position door, Direction dir, int cost) {
        if (dir.isDiagonal()) {
            return CommandResult.rejected("You can't reach the door from that angle.");
        }
        floor.setTerrain(door, Terrain.DOOR_OPEN);
        session.publish(new GameEvent(GameEvent.Type.DOOR_CHANGED, null, door, floor.depth()));
        return CommandResult.acted(cost, "You open the door.");
    }

    private static String arrivalMessage(Terrain terrain) {
        switch (terrain) {
            case STAIRS_DOWN:
                return "A staircase leads down from here.";
            case STAIRS_UP:
                return "A staircase leads up from here.";
            case ALTAR:
                return "You stand before an old stone altar.";
            default:
                return null;
        }
    }
}
