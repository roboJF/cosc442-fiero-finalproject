package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.effect.StatusEffect;
import edu.towson.cosc442.halls7800.entity.Companion;
import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.entity.MonsterTrait;
import edu.towson.cosc442.halls7800.entity.Npc;
import edu.towson.cosc442.halls7800.entity.Player;
import edu.towson.cosc442.halls7800.item.Item;
import edu.towson.cosc442.halls7800.world.FloorView;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Trap;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes what the player knows about a tile, for the examine (look) mode. Examining takes no
 * game time and changes nothing, so it is not recorded in run recordings.
 */
public final class Examiner {

    private Examiner() {
    }

    /**
     * Describes a tile of the current floor.
     *
     * @param session the game
     * @param p the tile to examine
     * @return a description; never empty
     */
    public static String describe(GameSession session, Position p) {
        FloorView floor = session.currentFloor();
        if (!floor.isInBounds(p) || !floor.isExplored(p)) {
            return "You haven't seen that spot.";
        }
        if (!floor.isVisible(p)) {
            return "You remember " + withArticle(floor.terrainAt(p).appearance().description()) + " there.";
        }
        List<String> parts = new ArrayList<>();
        Player player = session.player();
        if (player.position().equals(p)) {
            parts.add("That's you, " + player.name() + ": level " + player.level() + ", " + player.hp() + " of "
                    + player.maxHp() + " HP" + effectsOf(player.effects().active()) + ".");
        }
        Companion doc = session.companion();
        if (doc != null && doc.position().equals(p)) {
            parts.add("Doc, your black-and-gold tiger cub (" + doc.hp() + "/" + doc.maxHp() + " HP, "
                    + doc.kills() + " monsters defeated).");
        }
        Monster monster = session.monsterAt(p);
        if (monster != null) {
            parts.add(describeMonster(monster));
        }
        Npc npc = session.npcAt(p);
        if (npc != null) {
            parts.add(describeNpc(npc));
        }
        if (session.contents().fireAt(p) > 0) {
            parts.add("Flames are burning here!");
        }
        Trap trap = session.contents().trapAt(p);
        if (trap != null && !trap.isHidden()) {
            parts.add("There is " + withArticle(trap.type().displayName()) + " here.");
        }
        List<Item> items = session.itemsAt(p);
        if (!items.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (Item item : items) {
                names.add(session.describe(item));
            }
            parts.add("On the floor: " + String.join(", ", names) + ".");
        }
        parts.add("Terrain: " + floor.terrainAt(p).appearance().description() + ".");
        return String.join(" ", parts);
    }

    /**
     * @param monster a monster
     * @return its name, condition, notable traits and flavor text
     */
    static String describeMonster(Monster monster) {
        StringBuilder sb = new StringBuilder(capitalize(withArticle(monster.name())));
        sb.append(" (").append(monster.hp()).append('/').append(monster.maxHp()).append(" HP, ");
        sb.append(switch (monster.state()) {
            case SLEEPING -> "asleep";
            case WANDERING -> "wandering";
            case HUNTING -> "hunting you";
            case FLEEING -> "fleeing";
        });
        sb.append(effectsOf(monster.effects().active())).append(").");
        List<String> notes = new ArrayList<>();
        if (monster.isBoss()) {
            notes.add("a guardian of the Halls");
        }
        if (monster.type().has(MonsterTrait.RANGED)) {
            notes.add("attacks from range");
        }
        if (monster.type().actionCost() < GameRules.NORMAL_ACTION_COST) {
            notes.add("fast");
        } else if (monster.type().actionCost() > GameRules.NORMAL_ACTION_COST) {
            notes.add("slow");
        }
        if (monster.type().has(MonsterTrait.FIRE_IMMUNE)) {
            notes.add("immune to fire");
        }
        if (monster.type().has(MonsterTrait.SPLITS)) {
            notes.add("splits when wounded");
        }
        if (monster.type().has(MonsterTrait.THIEF)) {
            notes.add("steals gold");
        }
        if (!notes.isEmpty()) {
            sb.append(" It is ").append(String.join(", ", notes)).append('.');
        }
        sb.append(' ').append(monster.type().description());
        return sb.toString();
    }

    private static String describeNpc(Npc npc) {
        return switch (npc.role()) {
            case ARCHIVIST -> "Archivist Dehlinger, keeper of the Atrium's records. Walk into them to talk.";
            case TINKERER -> "Tinkerer Conover, proprietor of the Curio & Circuit. Walk into them to shop.";
            case LOREMASTER -> "Loremaster Chakraborty, who studies how the Halls are laid out. Walk into them to talk.";
            case SCRIBE -> "Scribe Irani, reader of strange labels. Walk into them to talk.";
            case WARDEN -> "Warden Deng, who tests adventurers' resolve. Walk into them to talk.";
            case LOCKSMITH -> "Locksmith Taylor, who studies every lock in the Halls. Walk into them to talk.";
            case TIGER_STATUE -> "A statue of a tiger in black and gold. Its nose looks lucky.";
            case NOTICE_BOARD -> "A notice board covered in flyers. Walk into it to read one.";
            case SIGN -> "A signpost. Walk into it to read it.";
            case ZOMBIE_KID -> "A kid in very convincing zombie makeup. He seems friendly.";
        };
    }

    private static String effectsOf(List<StatusEffect> effects) {
        if (effects.isEmpty()) {
            return "";
        }
        List<String> names = new ArrayList<>();
        for (StatusEffect e : effects) {
            names.add(e.type().adjective());
        }
        return ", " + String.join(", ", names);
    }

    private static String withArticle(String noun) {
        return Item.article(noun) + " " + noun;
    }

    private static String capitalize(String text) {
        return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
