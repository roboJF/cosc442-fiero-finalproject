package edu.towson.cosc442.halls7800.app;

/**
 * Program entry point.
 *
 * <p>This class deliberately does not extend {@link javafx.application.Application}. Launching
 * through a plain class lets the game start from an IDE "Run main" without extra JavaFX module
 * configuration.
 */
public final class Launcher {

    private Launcher() {
    }

    /**
     * Starts the JavaFX application.
     *
     * @param args command-line arguments, passed through to JavaFX
     */
    public static void main(String[] args) {
        HallsApp.launch(HallsApp.class, args);
    }
}
