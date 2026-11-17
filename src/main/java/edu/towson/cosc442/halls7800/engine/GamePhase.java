package edu.towson.cosc442.halls7800.engine;

/**
 * The overall state of a run.
 */
public enum GamePhase {
    /** The run is in progress and accepts commands. */
    PLAYING,
    /** The player has died. No further commands are accepted. */
    DEAD,
    /** The player escaped with the Diploma. No further commands are accepted. */
    VICTORY
}
