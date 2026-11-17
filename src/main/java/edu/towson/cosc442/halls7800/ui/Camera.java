package edu.towson.cosc442.halls7800.ui;

/**
 * Chooses which part of a floor is visible when the floor is larger than the window.
 */
final class Camera {

    private Camera() {
    }

    /**
     * Computes the first floor column (or row) shown on screen.
     *
     * @param center the player's column (or row)
     * @param viewSize how many tiles fit on screen
     * @param floorSize floor width (or height) in tiles
     * @return the floor coordinate drawn at the left (or top) edge; negative values center a small
     *         floor in a large window
     */
    static int origin(int center, int viewSize, int floorSize) {
        if (floorSize <= viewSize) {
            return -(viewSize - floorSize) / 2;
        }
        int start = center - viewSize / 2;
        return Math.max(0, Math.min(start, floorSize - viewSize));
    }
}
