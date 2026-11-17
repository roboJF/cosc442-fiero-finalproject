package edu.towson.cosc442.halls7800.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A single dungeon floor: a rectangular grid of {@link Terrain} plus what the player has seen.
 *
 * <p>Floors are mutable (doors open, secret doors are discovered) and are owned by the game
 * session. Later stages attach creatures, items and traps to a floor.
 */
public class Floor implements FloorView {

    private final int width;
    private final int height;
    private final int depth;
    private final String name;
    private final Terrain[][] tiles;
    private final boolean[][] visible;
    private final boolean[][] explored;
    private final List<Room> rooms = new ArrayList<>();
    private Position entrance;
    private Position exit;

    /**
     * Creates a floor filled entirely with walls.
     *
     * @param width number of columns (at least 3)
     * @param height number of rows (at least 3)
     * @param depth dungeon depth; 0 is the Atrium hub
     * @param name display name
     */
    public Floor(int width, int height, int depth, String name) {
        if (width < 3 || height < 3) {
            throw new IllegalArgumentException("Floor must be at least 3x3, was " + width + "x" + height);
        }
        if (depth < 0) {
            throw new IllegalArgumentException("Depth cannot be negative: " + depth);
        }
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.name = Objects.requireNonNull(name, "name");
        this.tiles = new Terrain[height][width];
        this.visible = new boolean[height][width];
        this.explored = new boolean[height][width];
        fill(Terrain.WALL);
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public int depth() {
        return depth;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isInBounds(Position p) {
        return p.x() >= 0 && p.y() >= 0 && p.x() < width && p.y() < height;
    }

    /**
     * @param p a position
     * @return {@code true} if {@code p} is inside the floor but not on its outer edge
     */
    public boolean isInterior(Position p) {
        return p.x() > 0 && p.y() > 0 && p.x() < width - 1 && p.y() < height - 1;
    }

    @Override
    public Terrain terrainAt(Position p) {
        if (!isInBounds(p)) {
            return Terrain.WALL;
        }
        return tiles[p.y()][p.x()];
    }

    /**
     * Changes the terrain of one tile.
     *
     * @param p position inside the floor
     * @param terrain new terrain
     * @throws IndexOutOfBoundsException if {@code p} is outside the floor
     */
    public void setTerrain(Position p, Terrain terrain) {
        checkBounds(p);
        tiles[p.y()][p.x()] = Objects.requireNonNull(terrain, "terrain");
    }

    /**
     * Sets every tile to the given terrain.
     *
     * @param terrain terrain to fill with
     */
    public final void fill(Terrain terrain) {
        for (Terrain[] row : tiles) {
            java.util.Arrays.fill(row, terrain);
        }
    }

    /**
     * Fills a rectangle (inclusive of both corners) with a terrain.
     *
     * @param x1 left column
     * @param y1 top row
     * @param x2 right column
     * @param y2 bottom row
     * @param terrain terrain to place
     */
    public void fillRect(int x1, int y1, int x2, int y2, Terrain terrain) {
        for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
            for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
                setTerrain(new Position(x, y), terrain);
            }
        }
    }

    /**
     * @param p a position
     * @return {@code true} if a creature may stand at {@code p} (terrain only)
     */
    public boolean isPassable(Position p) {
        return isInBounds(p) && terrainAt(p).isPassable();
    }

    /**
     * @param p a position
     * @return {@code true} if {@code p} blocks line of sight; out-of-bounds tiles always do
     */
    public boolean blocksSight(Position p) {
        return !isInBounds(p) || terrainAt(p).blocksSight();
    }

    @Override
    public boolean canStep(Position from, Direction direction, boolean allowClosedDoors) {
        Position to = from.step(direction);
        if (!isInBounds(to) || !terrainAt(to).isTraversable(allowClosedDoors)) {
            return false;
        }
        if (!direction.isDiagonal()) {
            return true;
        }
        // Doorways can only be entered or left orthogonally.
        if (terrainAt(from).isDoor() || terrainAt(to).isDoor()) {
            return false;
        }
        // No cutting the corner of a wall or pillar.
        Position sideA = from.offset(direction.dx(), 0);
        Position sideB = from.offset(0, direction.dy());
        return !terrainAt(sideA).isSolid() && !terrainAt(sideB).isSolid();
    }

    @Override
    public boolean isVisible(Position p) {
        return isInBounds(p) && visible[p.y()][p.x()];
    }

    @Override
    public boolean isExplored(Position p) {
        return isInBounds(p) && explored[p.y()][p.x()];
    }

    /**
     * Replaces the set of currently visible tiles. Every newly visible tile also becomes explored.
     *
     * @param nowVisible positions the player can see this turn
     */
    public void updateVisibility(Collection<Position> nowVisible) {
        for (boolean[] row : visible) {
            java.util.Arrays.fill(row, false);
        }
        for (Position p : nowVisible) {
            if (isInBounds(p)) {
                visible[p.y()][p.x()] = true;
                explored[p.y()][p.x()] = true;
            }
        }
    }

    /**
     * Marks every tile as explored (a magic-mapping effect).
     */
    public void revealAll() {
        for (boolean[] row : explored) {
            java.util.Arrays.fill(row, true);
        }
    }

    /** @return the number of tiles that are not solid rock (the denominator for exploration) */
    public int openTileCount() {
        int count = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (tiles[y][x] != Terrain.WALL) {
                    count++;
                }
            }
        }
        return count;
    }

    /** @return the number of explored tiles that are not solid rock */
    public int exploredOpenTileCount() {
        int count = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (explored[y][x] && tiles[y][x] != Terrain.WALL) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public Position entrance() {
        return entrance;
    }

    /**
     * @param entrance arrival point when coming down from above; must be passable
     */
    public void setEntrance(Position entrance) {
        checkBounds(entrance);
        this.entrance = entrance;
    }

    @Override
    public Position exit() {
        return exit;
    }

    /**
     * @param exit position of the down staircase, or {@code null} if the floor has none
     */
    public void setExit(Position exit) {
        if (exit != null) {
            checkBounds(exit);
        }
        this.exit = exit;
    }

    /**
     * Lists every position whose terrain matches, in row-major order.
     *
     * @param terrain terrain to look for
     * @return matching positions (possibly empty)
     */
    public List<Position> positionsOf(Terrain terrain) {
        List<Position> result = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (tiles[y][x] == terrain) {
                    result.add(new Position(x, y));
                }
            }
        }
        return result;
    }

    /**
     * @param terrain terrain to count
     * @return how many tiles have that terrain
     */
    public int count(Terrain terrain) {
        return positionsOf(terrain).size();
    }

    /** @return the rooms recorded during generation (empty for hand-drawn floors) */
    public List<Room> rooms() {
        return java.util.Collections.unmodifiableList(rooms);
    }

    /**
     * Records a room created during generation.
     *
     * @param room the room
     */
    public void addRoom(Room room) {
        rooms.add(Objects.requireNonNull(room, "room"));
    }

    /**
     * @param p a position
     * @return the room whose interior contains {@code p}, or {@code null} (e.g. in a corridor)
     */
    public Room roomAt(Position p) {
        for (Room r : rooms) {
            if (r.contains(p)) {
                return r;
            }
        }
        return null;
    }

    /**
     * @param role a room role
     * @return the first room with that role, or {@code null}
     */
    public Room roomWithRole(Room.Role role) {
        for (Room r : rooms) {
            if (r.role() == role) {
                return r;
            }
        }
        return null;
    }

    /** @return the zone this floor belongs to, based on its depth */
    public Zone zone() {
        return Zone.forDepth(depth);
    }

    /** @return every passable position in row-major order */
    public List<Position> passablePositions() {
        List<Position> result = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (tiles[y][x].isPassable()) {
                    result.add(new Position(x, y));
                }
            }
        }
        return result;
    }

    private void checkBounds(Position p) {
        if (p == null || !isInBounds(p)) {
            throw new IndexOutOfBoundsException("Position " + p + " is outside " + width + "x" + height + " floor");
        }
    }
}
