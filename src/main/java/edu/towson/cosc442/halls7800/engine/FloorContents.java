package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.entity.Npc;
import edu.towson.cosc442.halls7800.item.Item;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Trap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Everything on one floor besides its terrain: monsters, friendly NPCs, traps and piles of items. The session keeps one
 * of these per visited depth, so a floor's contents are preserved when the player leaves it.
 */
public class FloorContents {

    private final List<Monster> monsters = new ArrayList<>();
    private final Map<Position, List<Item>> piles = new LinkedHashMap<>();
    private final Map<Position, Trap> traps = new LinkedHashMap<>();
    private final List<Npc> npcs = new ArrayList<>();
    private final Map<Position, Integer> fires = new LinkedHashMap<>();

    /** @return the monsters, in the order they were added */
    public List<Monster> monsters() {
        return Collections.unmodifiableList(monsters);
    }

    /**
     * @param monster a monster to add
     */
    public void addMonster(Monster monster) {
        monsters.add(Objects.requireNonNull(monster, "monster"));
    }

    /**
     * @param monster a monster to remove
     * @return {@code true} if it was present
     */
    public boolean removeMonster(Monster monster) {
        return monsters.remove(monster);
    }

    /**
     * @param p a position
     * @return the monster standing there, or {@code null}
     */
    public Monster monsterAt(Position p) {
        for (Monster m : monsters) {
            if (m.position().equals(p)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Puts an item on the floor. Stackable items merge with a matching stack already there.
     *
     * @param p where to put it
     * @param item the item
     */
    public void addItem(Position p, Item item) {
        Objects.requireNonNull(item, "item");
        List<Item> pile = piles.computeIfAbsent(p, k -> new ArrayList<>());
        if (item.isStackable()) {
            for (Item existing : pile) {
                if (existing.canStackWith(item)) {
                    existing.setQuantity(existing.quantity() + item.quantity());
                    return;
                }
            }
        }
        pile.add(item);
    }

    /**
     * @param p a position
     * @return the items lying there, top of the pile last (empty if none)
     */
    public List<Item> itemsAt(Position p) {
        List<Item> pile = piles.get(p);
        return pile == null ? List.of() : Collections.unmodifiableList(pile);
    }

    /**
     * Removes an item from a pile.
     *
     * @param p the pile's position
     * @param item the item
     * @return {@code true} if it was there
     */
    public boolean removeItem(Position p, Item item) {
        List<Item> pile = piles.get(p);
        if (pile == null || !pile.remove(item)) {
            return false;
        }
        if (pile.isEmpty()) {
            piles.remove(p);
        }
        return true;
    }

    /** @return every non-empty pile, keyed by position */
    public Map<Position, List<Item>> piles() {
        return Collections.unmodifiableMap(piles);
    }

    /**
     * Sets a trap.
     *
     * @param p where
     * @param trap the trap
     */
    public void addTrap(Position p, Trap trap) {
        traps.put(p, Objects.requireNonNull(trap, "trap"));
    }

    /**
     * @param p a position
     * @return the trap there, or {@code null}
     */
    public Trap trapAt(Position p) {
        return traps.get(p);
    }

    /** @return every trap, keyed by position */
    public Map<Position, Trap> traps() {
        return Collections.unmodifiableMap(traps);
    }

    /**
     * @param npc a friendly character to place on this floor
     */
    public void addNpc(Npc npc) {
        npcs.add(Objects.requireNonNull(npc, "npc"));
    }

    /** @return the friendly characters on this floor */
    public List<Npc> npcs() {
        return Collections.unmodifiableList(npcs);
    }

    /**
     * @param p a position
     * @return the NPC standing there, or {@code null}
     */
    public Npc npcAt(Position p) {
        for (Npc n : npcs) {
            if (n.position().equals(p)) {
                return n;
            }
        }
        return null;
    }

    /**
     * Starts (or refreshes) a fire.
     *
     * @param p where
     * @param turns turns left to burn (at least 1)
     */
    public void addFire(Position p, int turns) {
        if (turns < 1) {
            throw new IllegalArgumentException("turns must be positive");
        }
        fires.put(p, turns);
    }

    /**
     * @param p a position
     * @return turns of fire left there, or 0 if not burning
     */
    public int fireAt(Position p) {
        return fires.getOrDefault(p, 0);
    }

    /**
     * @param p a position whose fire has gone out
     */
    public void removeFire(Position p) {
        fires.remove(p);
    }

    /** @return every burning tile with its remaining turns */
    public Map<Position, Integer> fires() {
        return Collections.unmodifiableMap(fires);
    }

    /** @return total number of item stacks on the floor */
    public int itemStackCount() {
        int count = 0;
        for (List<Item> pile : piles.values()) {
            count += pile.size();
        }
        return count;
    }
}
