package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.effect.EffectType;
import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.entity.Player;
import edu.towson.cosc442.halls7800.item.Armor;
import edu.towson.cosc442.halls7800.item.EquippableItem;
import edu.towson.cosc442.halls7800.item.Food;
import edu.towson.cosc442.halls7800.item.Identification;
import edu.towson.cosc442.halls7800.item.Item;
import edu.towson.cosc442.halls7800.item.KeyItem;
import edu.towson.cosc442.halls7800.item.QuestItem;
import edu.towson.cosc442.halls7800.item.Potion;
import edu.towson.cosc442.halls7800.item.Scroll;
import edu.towson.cosc442.halls7800.item.Wand;
import edu.towson.cosc442.halls7800.progression.Achievement;
import edu.towson.cosc442.halls7800.item.Weapon;
import edu.towson.cosc442.halls7800.util.RandomStream;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Uses the item in a pack slot in the natural way: wield or remove a weapon, put on or take off
 * armor, drink a potion, read a scroll, eat food, or zap a wand at the nearest monster.
 *
 * <p>Equipping reveals an item's upgrade level. Cursed equipment cannot be removed. Putting on or
 * taking off armor takes twice as long as other actions. Drinking or reading identifies the kind.
 *
 * @param slot the pack slot index (0 = 'a')
 */
public record UseItemCommand(int slot) implements Command {

    /** Turns that a scroll of code review terrifies monsters. */
    public static final int FEAR_TURNS = 10;

    @Override
    public String encode() {
        return "U:" + slot;
    }

    @Override
    public CommandResult execute(GameSession session) {
        Player player = session.player();
        Item item = player.inventory().get(slot);
        if (item == null) {
            return CommandResult.rejected("You don't have that item.");
        }
        if (item instanceof Weapon weapon) {
            return toggleWeapon(session, player, weapon);
        }
        if (item instanceof Armor armor) {
            return toggleArmor(session, player, armor);
        }
        if (item instanceof Potion potion) {
            return drink(session, player, potion);
        }
        if (item instanceof Scroll scroll) {
            return read(session, player, scroll);
        }
        if (item instanceof Food food) {
            return eat(session, player, food);
        }
        if (item instanceof QuestItem quest && quest.kind() == QuestItem.Kind.DIPLOMA) {
            if (session.depth() == GameRules.HUB_DEPTH) {
                return CommandResult.rejected("You are already in the Atrium. Take the south stairs out.");
            }
            session.changeFloor(GameRules.HUB_DEPTH);
            return CommandResult.acted(player.actionCost(), "You raise the Diploma. The Halls fold around you, "
                    + "and you stand once more in the Atrium.");
        }
        if (item instanceof Wand wand) {
            return WandZapper.zap(session, wand);
        }
        if (item instanceof KeyItem) {
            return CommandResult.rejected("Walk into the locked door to use the key.");
        }
        return CommandResult.rejected("You can't use that. Try throwing it.");
    }

    // ----- equipment -----

    private static CommandResult toggleWeapon(GameSession session, Player player, Weapon weapon) {
        Weapon current = player.weapon();
        if (current != null && current.isCursed()) {
            return CommandResult.rejected("Your " + current.name(session.identification())
                    + " is cursed and welded to your hand!");
        }
        if (current == weapon) {
            player.setWeapon(null);
            return CommandResult.acted(player.actionCost(), "You put away your " + name(session, weapon) + ".");
        }
        player.setWeapon(weapon);
        return CommandResult.acted(player.actionCost(), equipMessage(session, weapon, "wield"));
    }

    private static CommandResult toggleArmor(GameSession session, Player player, Armor armor) {
        Armor current = player.wornArmor();
        if (current != null && current.isCursed()) {
            return CommandResult.rejected("Your " + current.name(session.identification())
                    + " is cursed and won't come off!");
        }
        int cost = player.actionCost() * 2;
        if (current == armor) {
            player.setWornArmor(null);
            return CommandResult.acted(cost, "You take off your " + name(session, armor) + ".");
        }
        player.setWornArmor(armor);
        return CommandResult.acted(cost, equipMessage(session, armor, "put on"));
    }

    private static String equipMessage(GameSession session, EquippableItem item, String verb) {
        session.signal(GameEvent.Type.ITEM_EQUIPPED, session.player().position());
        boolean wasKnown = item.isKnown();
        item.reveal();
        String text = "You " + verb + " " + session.describe(item) + ".";
        if (item.isCursed()) {
            session.achievements().unlock(session, Achievement.WORKS_ON_MY_MACHINE);
            text += " It tightens around you with a sickly glow. It's cursed!";
        } else if (!wasKnown && item.upgrade() > 0) {
            text += " It feels unusually well made.";
        }
        return text;
    }

    // ----- potions -----

    private static CommandResult drink(GameSession session, Player player, Potion potion) {
        Potion.Kind kind = potion.kind();
        player.inventory().removeOne(potion);
        session.stats().recordPotion();
        session.signal(GameEvent.Type.POTION_DRUNK, player.position());
        boolean newlyKnown = session.identification().learn(kind);
        String effect = switch (kind) {
            case HEALING -> {
                player.heal(Math.max(10, player.maxHp() / 2));
                player.effects().remove(EffectType.POISONED);
                yield "You feel much better.";
            }
            case VITALITY -> {
                player.setMaxHp(player.maxHp() + 5);
                player.heal(player.maxHp());
                yield "You feel vigorous! Your maximum health rises.";
            }
            case HASTE -> {
                player.applyEffect(EffectType.HASTED, 12, 0);
                yield "Everything around you slows down.";
            }
            case SHIELDING -> {
                player.applyEffect(EffectType.SHIELDED, 25, 12);
                yield "A shimmering barrier surrounds you.";
            }
            case CLEANSING -> {
                int cured = player.effects().cureHarmful();
                yield cured > 0 ? "A cool wave washes your afflictions away." : "You feel refreshed.";
            }
            case REGENERATION -> {
                player.applyEffect(EffectType.REGENERATING, 15, 1);
                yield "Your wounds begin to knit.";
            }
            case POISON -> {
                player.applyEffect(EffectType.POISONED, 6, 2);
                yield "Ugh! This tastes like spoiled milk. You are poisoned.";
            }
            case CONFUSION -> {
                player.applyEffect(EffectType.CONFUSED, 8, 0);
                yield "The room spins around you.";
            }
            case BLINDNESS -> {
                player.applyEffect(EffectType.BLINDED, 10, 0);
                session.refreshVisibility();
                yield "Darkness swallows your sight!";
            }
            case LIQUID_FIRE -> {
                player.applyEffect(EffectType.BURNING, 4, 2);
                yield player.effects().has(EffectType.BURNING) ? "The potion bursts into flame in your mouth!" : "Steam hisses off your soaked clothes.";
            }
        };
        String prefix = newlyKnown ? "It was a potion of " + kind.displayName() + "! " : "";
        return CommandResult.acted(player.actionCost(), prefix + effect);
    }

    // ----- scrolls -----

    private static CommandResult read(GameSession session, Player player, Scroll scroll) {
        if (player.effects().has(EffectType.BLINDED)) {
            return CommandResult.rejected("You can't read while blind.");
        }
        Scroll.Kind kind = scroll.kind();
        player.inventory().removeOne(scroll);
        session.stats().recordScroll();
        session.signal(GameEvent.Type.SCROLL_READ, player.position());
        boolean newlyKnown = session.identification().learn(kind);
        String effect = switch (kind) {
            case INTROSPECTION -> introspect(session, player);
            case REFACTORING -> refactor(session, player);
            case DEBUGGING -> debug(player);
            case DOCUMENTATION -> {
                session.floor().revealAll();
                yield "A complete map of this floor unfolds in your mind.";
            }
            case CONTEXT_SWITCHING -> contextSwitch(session, player);
            case CODE_REVIEW -> codeReview(session);
        };
        String prefix = newlyKnown ? "It was a scroll of " + kind.displayName() + "! " : "";
        return CommandResult.acted(player.actionCost(), prefix + effect);
    }

    private static String introspect(GameSession session, Player player) {
        Identification id = session.identification();
        for (Item item : player.inventory().items()) {
            if (!id.isIdentified(item)) {
                id.identify(item);
                return "You study your belongings. That is " + session.describe(item) + ".";
            }
        }
        return "You study your belongings but learn nothing new.";
    }

    private static String refactor(GameSession session, Player player) {
        EquippableItem target = player.weapon();
        if (target == null) {
            target = player.wornArmor();
        }
        if (target == null) {
            return "The scroll glows, but finds nothing to improve.";
        }
        boolean wasCursed = target.isCursed();
        target.addUpgrade(1);
        if (wasCursed) {
            target.removeCurse();
        }
        target.reveal();
        return "Your " + target.name(session.identification()) + " glows as its structure improves."
                + (wasCursed ? " The curse is lifted." : "");
    }

    private static String debug(Player player) {
        int fixed = 0;
        for (EquippableItem item : player.inventory().ofType(EquippableItem.class)) {
            if (item.isCursed()) {
                item.removeCurse();
                item.reveal();
                fixed++;
            }
        }
        return fixed > 0 ? "You feel as if someone is watching over you. The curses are lifted."
                : "You feel strangely confident that nothing you carry is broken.";
    }

    private static String contextSwitch(GameSession session, Player player) {
        Floor floor = session.floor();
        List<Position> options = new ArrayList<>();
        for (Position p : floor.passablePositions()) {
            if (!session.isOccupied(p) && p.distanceTo(player.position()) > 5) {
                options.add(p);
            }
        }
        if (options.isEmpty()) {
            return "You feel briefly disoriented.";
        }
        Position destination = session.rng(RandomStream.EFFECTS).pick(options);
        session.moveCreature(player, destination);
        session.refreshVisibility();
        return "Your surroundings blur and reassemble around you.";
    }

    private static String codeReview(GameSession session) {
        int scared = 0;
        for (Monster m : List.copyOf(session.monsters())) {
            if (session.currentFloor().isVisible(m.position())) {
                m.frighten(FEAR_TURNS);
                scared++;
            }
        }
        return scared > 0 ? "Your merciless critique sends nearby monsters fleeing!"
                : "You critique the empty room. It has no comment.";
    }

    // ----- food -----

    private static CommandResult eat(GameSession session, Player player, Food food) {
        Food.Kind kind = food.kind();
        String name = food.name(session.identification());
        player.inventory().removeOne(food);
        player.eat(kind.satiety());
        session.signal(GameEvent.Type.FOOD_EATEN, player.position());
        player.heal(kind.healing());
        String taste = kind == Food.Kind.MYSTERY_MEAT ? "It's... probably chicken." : "Delicious.";
        return CommandResult.acted(player.actionCost(), "You eat the " + name + ". " + taste);
    }

    private static String name(GameSession session, Item item) {
        return item.name(session.identification());
    }
}
