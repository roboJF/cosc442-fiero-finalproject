package edu.towson.cosc442.halls7800.entity;

import edu.towson.cosc442.halls7800.combat.AttackProfile;
import edu.towson.cosc442.halls7800.engine.GameRules;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.world.Position;
import java.util.Objects;

/**
 * A friendly character or fixture the player can talk to by walking into it. NPCs never move,
 * cannot be attacked, and are not scheduled. What they say and do is in
 * {@code quest.Conversations}.
 *
 * <p>The Halls' residents are fictional characters. Their names are an affectionate nod to the
 * Towson University Computer &amp; Information Sciences faculty; their roles and dialogue are
 * invented for the game and say nothing about the real people.
 */
public final class Npc extends Creature {

    /** Who (or what) the NPC is. */
    public enum Role {
        /** Gives the main quest and the lost-requirements quest. */
        ARCHIVIST("Archivist Dehlinger", '@', "#f2c14e", true),
        /** Runs Conover's Curio &amp; Circuit, the Atrium shop. */
        TINKERER("Tinkerer Conover", '@', "#7fd0ff", true),
        /** Gives the cartography quest. */
        LOREMASTER("Loremaster Chakraborty", '@', "#c9a0ff", true),
        /** Deciphers unknown items for a fee. */
        SCRIBE("Scribe Irani", '@', "#80e0a0", true),
        /** Studies the locks of the Halls; gives the vault quest. */
        LOCKSMITH("Locksmith Taylor", '@', "#ff80c0", true),
        /** Runs the mutation trial in the Clockwork Stacks. */
        WARDEN("Warden Deng", '@', "#ff9f6a", true),
        /** A statue of a black-and-gold tiger that grants one blessing per run. */
        TIGER_STATUE("tiger statue", '&', "#f2c14e", false),
        /** The Atrium notice board, full of hints. */
        NOTICE_BOARD("notice board", '=', "#d0b080", false),
        /** A tutorial signpost with fixed text. */
        SIGN("signpost", '=', "#e8d8a0", false),
        /** An easter egg. He likes turtles. */
        ZOMBIE_KID("Zombie Kid", '@', "#8fd36a", true);

        private final String displayName;
        private final char glyph;
        private final String color;
        private final boolean person;

        Role(String displayName, char glyph, String color, boolean person) {
            this.displayName = displayName;
            this.glyph = glyph;
            this.color = color;
            this.person = person;
        }

        /** @return display name */
        public String displayName() {
            return displayName;
        }

        /** @return map symbol */
        public char glyph() {
            return glyph;
        }

        /** @return display color as a hex string */
        public String color() {
            return color;
        }

        /** @return {@code true} for characters (as opposed to objects) */
        public boolean isPerson() {
            return person;
        }
    }

    private final Role role;
    private final String text;

    /**
     * @param role who the NPC is
     * @param position where the NPC stands
     */
    public Npc(Role role, Position position) {
        this(role, position, null);
    }

    /**
     * @param role who the NPC is
     * @param position where the NPC stands
     * @param text what a sign says (ignored for characters)
     */
    public Npc(Role role, Position position, String text) {
        super(role.displayName(), position, 999);
        this.role = Objects.requireNonNull(role, "role");
        this.text = text;
    }

    /** @return the sign's text, or {@code null} */
    public String text() {
        return text;
    }

    /** @return who the NPC is */
    public Role role() {
        return role;
    }

    @Override
    public String subject() {
        return role.isPerson() ? name() : "The " + name();
    }

    @Override
    public String object() {
        return role.isPerson() ? name() : "the " + name();
    }

    @Override
    public AttackProfile meleeAttack() {
        return AttackProfile.basic(0, 0, 0, 0, "gesture at");
    }

    @Override
    public int takeDamage(int amount) {
        return 0;
    }

    @Override
    protected int act(GameSession session) {
        return GameRules.NORMAL_ACTION_COST;
    }
}
