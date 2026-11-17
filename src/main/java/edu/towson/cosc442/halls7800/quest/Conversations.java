package edu.towson.cosc442.halls7800.quest;

import edu.towson.cosc442.halls7800.effect.EffectType;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.entity.MonsterType;
import edu.towson.cosc442.halls7800.entity.Npc;
import edu.towson.cosc442.halls7800.entity.Player;
import edu.towson.cosc442.halls7800.item.Identification;
import edu.towson.cosc442.halls7800.item.Inventory;
import edu.towson.cosc442.halls7800.item.Item;
import edu.towson.cosc442.halls7800.item.KeyItem;
import edu.towson.cosc442.halls7800.item.Potion;
import edu.towson.cosc442.halls7800.item.QuestItem;
import edu.towson.cosc442.halls7800.item.Scroll;
import edu.towson.cosc442.halls7800.util.RandomStream;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * What happens when the player talks to each NPC: dialogue, starting and finishing quests,
 * rewards, and services.
 */
public final class Conversations {

    /** Price of Scribe Irani's deciphering service. */
    public static final int DECIPHER_PRICE = 30;
    /** Gold given for the lost requirements. */
    public static final int REQUIREMENTS_GOLD = 100;
    /** Gold given for the survey. */
    public static final int SURVEY_GOLD = 60;
    /** Gold given for the vault quest. */
    public static final int VAULT_GOLD = 80;
    /** Gold given for the translation quest. */
    public static final int TRANSLATION_GOLD = 50;

    private static final String[] NOTICES = {
        "NOTICE BOARD, Suite YR-418: \"Adventurers seeking the Diploma: please see the Archivist first.\"",
        "A faded flyer: \"Lost something? Walls are not always walls. Search (F) where the map looks odd.\"",
        "A sticky note: \"Vault keys open one vault each. Check the floor the key came from.\"",
        "A handwritten sign: \"Guardians seal the stairs below them. Defeat them to go on.\"",
        "A poster: \"Hungry? The Curio & Circuit sells trail rations. Unattended snacks attract rats.\"",
        "Small print at the bottom: \"Office hours in the Sanctum are by appointment only.\"",
        "A course flyer: \"COSC 442, Software Quality Assurance and Testing. Final project: the Halls of 7800. "
                + "Find the bugs before they find you.\"",
    };

    private Conversations() {
    }

    /**
     * Talks to an NPC.
     *
     * @param session the game
     * @param npc who the player is talking to
     * @return what was said (one or more sentences)
     */
    public static String talk(GameSession session, Npc npc) {
        return switch (npc.role()) {
            case ARCHIVIST -> archivist(session);
            case TINKERER -> tinkerer(session);
            case LOREMASTER -> loremaster(session);
            case SCRIBE -> scribe(session);
            case LOCKSMITH -> locksmith(session);
            case WARDEN -> warden(session, npc);
            case TIGER_STATUE -> tigerStatue(session);
            case NOTICE_BOARD -> NOTICES[session.turn() % NOTICES.length];
            case SIGN -> npc.text();
            case ZOMBIE_KID -> "Zombie Kid: \"I like turtles.\"";
        };
    }

    // ----- Archivist Dehlinger: the main quest and the lost requirements -----

    private static String archivist(GameSession session) {
        QuestLog log = session.quests();
        Player player = session.player();
        if (log.state(Quest.Id.DIPLOMA) == Quest.State.READY) {
            return "Archivist Dehlinger: \"You found it! The Diploma of Ascendance. "
                    + "Take the stairs to the south and claim your commencement.\"";
        }
        Quest pages = log.get(Quest.Id.LOST_REQUIREMENTS);
        switch (pages.state()) {
            case NOT_STARTED -> {
                pages.start();
                log.update(session);
                return "Archivist Dehlinger: \"Welcome to the Halls of 7800. The Diploma waits at the very bottom, "
                        + "guarded by the Hollow Registrar. Before you go, a favor: the requirements for this whole "
                        + "place were torn into pages and lost in the Undercroft. Bring me all three and I'll make "
                        + "it worth your while.\"";
            }
            case ACTIVE -> {
                return "Archivist Dehlinger: \"Any luck with those requirements? You have " + pages.progress()
                        + " of 3 pages. Without them, nobody here agrees on what we're building.\"";
            }
            case READY -> {
                removeQuestItems(player.inventory(), QuestItem.Kind.REQUIREMENTS_PAGE);
                pages.complete();
                player.addGold(REQUIREMENTS_GOLD);
                Scroll reward = new Scroll(Scroll.Kind.REFACTORING, 1);
                session.identification().learn(Scroll.Kind.REFACTORING);
                String rewardText = giveOrDrop(session, reward);
                return "Archivist Dehlinger: \"All three pages! Clear, complete, and testable at last. Take "
                        + REQUIREMENTS_GOLD + " gold and this scroll of refactoring.\" " + rewardText;
            }
            default -> {
                return "Archivist Dehlinger: \"The requirements are safe in the archive. Now go and find that Diploma.\"";
            }
        }
    }

    // ----- Tinkerer Conover: the shop -----

    private static String tinkerer(GameSession session) {
        session.openShop();
        return "Tinkerer Conover looks up from a disassembled clockwork terminal: \"Welcome to the Curio & Circuit. "
                + "Everything here still works, mostly.\"";
    }

    // ----- Loremaster Chakraborty: the survey -----

    private static String loremaster(GameSession session) {
        Quest survey = session.quests().get(Quest.Id.CARTOGRAPHY);
        switch (survey.state()) {
            case NOT_STARTED -> {
                survey.start();
                session.quests().update(session);
                return "Loremaster Chakraborty: \"Every level tells you how it wants to be played, if you look closely. "
                        + "Explore most of three floors down there and tell me how they flow.\"";
            }
            case ACTIVE -> {
                return "Loremaster Chakraborty: \"So far you have surveyed " + survey.progress()
                        + " of 3 floors. Leave no corridor unwalked.\"";
            }
            case READY -> {
                survey.complete();
                session.player().addGold(SURVEY_GOLD);
                session.identification().learn(Potion.Kind.VITALITY);
                String rewardText = giveOrDrop(session, new Potion(Potion.Kind.VITALITY, 1));
                return "Loremaster Chakraborty: \"Wonderful notes! The pacing on those floors is better than I hoped. "
                        + "Here are " + SURVEY_GOLD + " gold and a potion of vitality.\" " + rewardText;
            }
            default -> {
                return "Loremaster Chakraborty: \"Your survey is already on my desk. Good luck below.\"";
            }
        }
    }

    // ----- Scribe Irani: the translation quest and deciphering service -----

    private static String scribe(GameSession session) {
        Quest quest = session.quests().get(Quest.Id.TRANSLATION);
        switch (quest.state()) {
            case NOT_STARTED -> {
                quest.start();
                session.quests().update(session);
                return "Scribe Irani: \"Every scroll in the Halls is written in a language of its own. Learn to read "
                        + "five kinds and you'll understand the place far better. Bring me anything you can't read; "
                        + "I'll decipher it for " + DECIPHER_PRICE + " gold.\"";
            }
            case READY -> {
                quest.complete();
                session.player().addGold(TRANSLATION_GOLD);
                session.identification().learn(Scroll.Kind.INTROSPECTION);
                String rewardText = giveOrDrop(session, new Scroll(Scroll.Kind.INTROSPECTION, 2));
                return "Scribe Irani: \"Five scripts! You read the Halls like a native now. Take these scrolls and "
                        + TRANSLATION_GOLD + " gold, and from now on my deciphering is free for you.\" " + rewardText;
            }
            default -> {
                return decipher(session, quest.state() == Quest.State.COMPLETED ? 0 : DECIPHER_PRICE, quest);
            }
        }
    }

    private static String decipher(GameSession session, int price, Quest quest) {
        Player player = session.player();
        Identification id = session.identification();
        Item unknown = null;
        for (Item item : player.inventory().items()) {
            if (!id.isIdentified(item)) {
                unknown = item;
                break;
            }
        }
        if (unknown == null) {
            String progress = quest.isOpen() ? " You can read " + quest.progress() + " of 5 kinds of scroll so far." : "";
            return "Scribe Irani: \"Everything you carry already reads plainly. Bring me something with a strange "
                    + "label.\"" + progress;
        }
        if (player.gold() < price) {
            return "Scribe Irani: \"I can decipher that " + unknown.name(id) + " for " + price
                    + " gold. You have " + player.gold() + ".\"";
        }
        String before = unknown.name(id);
        player.spendGold(price);
        id.identify(unknown);
        return "Scribe Irani studies the " + before + ", tracing each symbol: \"Ah. This is "
                + unknown.describe(id) + ".\"" + (price > 0 ? " (" + price + " gold)" : "");
    }

    // ----- Locksmith Taylor: the vault quest -----

    private static String locksmith(GameSession session) {
        Quest quest = session.quests().get(Quest.Id.SECURE_VAULTS);
        switch (quest.state()) {
            case NOT_STARTED -> {
                quest.start();
                session.quests().update(session);
                return "Locksmith Taylor: \"Every lock in the Halls was built by someone who never expected it to be "
                        + "tested. Each floor's vault key is hidden on that floor. Open three vaults and tell me how "
                        + "they held up.\"";
            }
            case ACTIVE -> {
                return "Locksmith Taylor: \"" + quest.progress() + " of 3 vaults opened. Remember: a key only opens "
                        + "the vault on the floor where you found it.\"";
            }
            case READY -> {
                quest.complete();
                session.player().addGold(VAULT_GOLD);
                String rewardText = giveOrDrop(session, new KeyItem(KeyItem.MASTER));
                return "Locksmith Taylor: \"Three vaults, three lessons in secure design. Here: " + VAULT_GOLD
                        + " gold and a master key. It opens any vault, and it never wears out.\" " + rewardText;
            }
            default -> {
                return "Locksmith Taylor: \"Validate your inputs, and never trust a lock you haven't tested.\"";
            }
        }
    }

    // ----- Warden Deng: the mutation trial -----

    private static String warden(GameSession session, Npc warden) {
        QuestLog log = session.quests();
        Quest trial = log.get(Quest.Id.MUTATION_TRIAL);
        switch (trial.state()) {
            case NOT_STARTED -> {
                trial.start();
                int released = releaseMutants(session, warden);
                log.markMutantsReleased();
                return "Warden Deng: \"A strong test suite kills every mutant. Here are " + released
                        + " of them, each one small change away from correct. Show me yours.\"";
            }
            case ACTIVE -> {
                return "Warden Deng: \"" + (3 - trial.progress()) + " mutants still survive. A surviving mutant "
                        + "means a gap in your tests.\"";
            }
            case READY -> {
                trial.complete();
                Player player = session.player();
                String reward;
                if (player.weapon() != null) {
                    player.weapon().addUpgrade(1);
                    player.weapon().removeCurse();
                    player.weapon().reveal();
                    reward = "The Warden tunes your weapon: it is now "
                            + player.weapon().describe(session.identification()) + ".";
                } else {
                    reward = giveOrDrop(session, new Scroll(Scroll.Kind.REFACTORING, 1));
                }
                return "Warden Deng: \"Every mutant killed. That is a mutation score I can trust.\" " + reward;
            }
            default -> {
                return "Warden Deng: \"Your suite is strong. Keep it that way as you go deeper.\"";
            }
        }
    }

    private static int releaseMutants(GameSession session, Npc warden) {
        Floor floor = session.floor();
        List<Position> spots = new ArrayList<>();
        for (int dy = -4; dy <= 4; dy++) {
            for (int dx = -4; dx <= 4; dx++) {
                Position p = warden.position().offset(dx, dy);
                if (p.distanceTo(warden.position()) >= 2 && p.distanceTo(session.player().position()) >= 2
                        && floor.isPassable(p) && !session.isOccupied(p)) {
                    spots.add(p);
                }
            }
        }
        int released = 0;
        for (int i = 0; i < 3 && !spots.isEmpty(); i++) {
            Position p = spots.remove(session.rng(RandomStream.AI)
                    .nextInt(spots.size()));
            Monster mutant = new Monster(MonsterType.MUTANT, p, false);
            mutant.alert();
            mutant.setLastKnownPlayerPosition(session.player().position());
            session.addMonster(mutant);
            released++;
        }
        return released;
    }

    // ----- The tiger statue: one blessing per run -----

    private static String tigerStatue(GameSession session) {
        QuestLog log = session.quests();
        if (log.tigerBlessingUsed()) {
            return "The tiger statue's nose is polished bright from your last visit. Its gold-and-black stripes "
                    + "gleam encouragingly.";
        }
        log.markTigerBlessingUsed();
        Player player = session.player();
        player.heal(player.maxHp());
        player.applyEffect(EffectType.SHIELDED, 30, 10);
        String cub = session.recruitCompanion()
                ? " A black-and-gold tiger cub tumbles out from behind the statue and falls in at your heels. "
                        + "Doc has joined you!"
                : "";
        return "You rub the black-and-gold tiger statue's nose for luck. A warm roar echoes through the Atrium. "
                + "Go Tigers! You feel ready for anything." + cub;
    }

    // ----- helpers -----

    private static void removeQuestItems(Inventory pack, QuestItem.Kind kind) {
        for (Item item : new ArrayList<>(pack.items())) {
            if (item instanceof QuestItem q && q.kind() == kind) {
                pack.remove(q, q.quantity());
            }
        }
    }

    private static String giveOrDrop(GameSession session, Item item) {
        Player player = session.player();
        if (player.inventory().add(item)) {
            return "";
        }
        session.placeItem(player.position(), item);
        return "Your pack is full, so it is set at your feet.";
    }
}
