package edu.towson.cosc442.halls7800.ui;

import javafx.scene.Parent;
import javafx.scene.input.KeyCode;

/**
 * One full-window screen of the application (title screen or game). The application forwards
 * keyboard input to whichever screen is showing.
 */
public interface Screen {

    /** @return the screen's root node */
    Parent root();

    /**
     * Handles a key press.
     *
     * @param code the key
     */
    void keyPressed(KeyCode code);

    /**
     * Handles typed text (for characters such as '?' or '&gt;' whose key differs by layout).
     *
     * @param character the typed character
     */
    void keyTyped(String character);
}
