package edu.towson.cosc442.halls7800.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A rectangular room on a generated floor. The stored bounds are the room's <em>interior</em>
 * (walkable area); the walls form a one-tile ring around it.
 *
 * <p>Rooms are kept on the {@link Floor} after generation so that later steps (placing monsters,
 * loot and keys) know where the entrance room, exit room and vaults are.
 */
public class Room {

    /** The part a room plays in the floor's layout. */
    public enum Role {
        /** An ordinary room. */
        ORDINARY,
        /** Contains the up staircase where the player arrives. */
        ENTRANCE,
        /** Contains the down staircase. */
        EXIT,
        /** A dead end behind a locked door. */
        VAULT,
        /** A dead end behind a secret door. */
        HIDDEN
    }

    private final int id;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private Role role = Role.ORDINARY;
    private RoomType type = RoomType.PLAIN;
    private final List<Position> doorways = new ArrayList<>();
    private final Set<Integer> connections = new TreeSet<>();
    private int openings;

    /**
     * @param id index of the room on its floor
     * @param x left interior column
     * @param y top interior row
     * @param width interior width (at least 1)
     * @param height interior height (at least 1)
     */
    public Room(int id, int x, int y, int width, int height) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Room must be at least 1x1");
        }
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /** @return index of the room on its floor */
    public int id() {
        return id;
    }

    /** @return left interior column */
    public int left() {
        return x;
    }

    /** @return top interior row */
    public int top() {
        return y;
    }

    /** @return right interior column (inclusive) */
    public int right() {
        return x + width - 1;
    }

    /** @return bottom interior row (inclusive) */
    public int bottom() {
        return y + height - 1;
    }

    /** @return interior width */
    public int width() {
        return width;
    }

    /** @return interior height */
    public int height() {
        return height;
    }

    /** @return number of interior tiles */
    public int area() {
        return width * height;
    }

    /** @return the central interior tile (rounded toward the top-left) */
    public Position center() {
        return new Position(x + (width - 1) / 2, y + (height - 1) / 2);
    }

    /**
     * @param p a position
     * @return {@code true} if {@code p} is inside the room's interior
     */
    public boolean contains(Position p) {
        return p.x() >= x && p.x() <= right() && p.y() >= y && p.y() <= bottom();
    }

    /**
     * @param p a position
     * @return {@code true} if {@code p} is on the wall ring, excluding the four corners
     */
    public boolean isOnWall(Position p) {
        boolean onVerticalWall = (p.x() == x - 1 || p.x() == right() + 1) && p.y() >= y && p.y() <= bottom();
        boolean onHorizontalWall = (p.y() == y - 1 || p.y() == bottom() + 1) && p.x() >= x && p.x() <= right();
        return onVerticalWall || onHorizontalWall;
    }

    /**
     * Returns the direction pointing out of the room from a wall tile.
     *
     * @param wall a position for which {@link #isOnWall} is true
     * @return the outward direction
     */
    public Direction outwardFrom(Position wall) {
        if (wall.y() == y - 1) {
            return Direction.NORTH;
        }
        if (wall.y() == bottom() + 1) {
            return Direction.SOUTH;
        }
        if (wall.x() == x - 1) {
            return Direction.WEST;
        }
        if (wall.x() == right() + 1) {
            return Direction.EAST;
        }
        throw new IllegalArgumentException(wall + " is not on the wall of room " + id);
    }

    /**
     * Checks whether two rooms (including walls) come closer than {@code margin} tiles.
     *
     * @param other another room
     * @param margin required number of tiles between the walls
     * @return {@code true} if the rooms overlap or are too close
     */
    public boolean intersects(Room other, int margin) {
        return x - 1 - margin <= other.right() + 1
                && other.x - 1 <= right() + 1 + margin
                && y - 1 - margin <= other.bottom() + 1
                && other.y - 1 <= bottom() + 1 + margin;
    }

    /** @return every interior position, in row-major order */
    public List<Position> interior() {
        List<Position> result = new ArrayList<>(area());
        for (int py = y; py <= bottom(); py++) {
            for (int px = x; px <= right(); px++) {
                result.add(new Position(px, py));
            }
        }
        return result;
    }

    /** @return every wall position except corners, clockwise from the top-left */
    public List<Position> wallPositions() {
        List<Position> result = new ArrayList<>();
        for (int px = x; px <= right(); px++) {
            result.add(new Position(px, y - 1));
        }
        for (int py = y; py <= bottom(); py++) {
            result.add(new Position(right() + 1, py));
        }
        for (int px = right(); px >= x; px--) {
            result.add(new Position(px, bottom() + 1));
        }
        for (int py = bottom(); py >= y; py--) {
            result.add(new Position(x - 1, py));
        }
        return result;
    }

    /** @return the part this room plays in the layout */
    public Role role() {
        return role;
    }

    /** @param role the new role */
    public void setRole(Role role) {
        this.role = role;
    }

    /** @return the interior decoration */
    public RoomType type() {
        return type;
    }

    /** @param type the new decoration */
    public void setType(RoomType type) {
        this.type = type;
    }

    /** @return wall positions that hold a proper doorway (a door or a one-tile opening) */
    public List<Position> doorways() {
        return Collections.unmodifiableList(doorways);
    }

    /**
     * Records a proper doorway in this room's wall.
     *
     * @param p the doorway position
     */
    public void addDoorway(Position p) {
        if (!doorways.contains(p)) {
            doorways.add(p);
        }
    }

    /** @return how many wall tiles have been broken through, including irregular breaches */
    public int openings() {
        return openings;
    }

    /**
     * @param openings number of broken wall tiles
     */
    public void setOpenings(int openings) {
        this.openings = openings;
    }

    /** @return {@code true} if the room has exactly one way in, through a proper doorway */
    public boolean isDeadEnd() {
        return openings == 1 && doorways.size() == 1;
    }

    /** @return ids of rooms this room was connected to by a corridor */
    public Set<Integer> connections() {
        return Collections.unmodifiableSet(connections);
    }

    /**
     * Records a corridor connection to another room.
     *
     * @param otherId the other room's id
     */
    public void connectTo(int otherId) {
        connections.add(otherId);
    }

    @Override
    public String toString() {
        return "Room#" + id + "[" + x + "," + y + " " + width + "x" + height + " " + role + "/" + type + "]";
    }
}
