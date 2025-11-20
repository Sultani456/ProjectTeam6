package com.project.team6.model.runtime;

import com.project.team6.model.board.Position;

import java.util.*;

/**
 * Holds the current game status.
 * The controller updates it. Views read it.
 */
public final class GameState {

    /**
     * Overall state of the game.
     */
    public enum Status { RUNNING, WON, LOST }

    /** Current status flag. Starts in running state. */
    private Status status = Status.RUNNING;

    /** Player position snapshot. */
    private Position player;

    /** Enemy position snapshots. */
    private final Set<Position> enemies = new HashSet<>();

    /** Scoreboard for time and score. */
    private final Scoreboard scoreboard;

    /**
     * Creates a game state snapshot.
     *
     * @param playerStart starting player position
     * @param enemyStarts starting enemy positions, may be null
     * @param scoreboard  scoreboard reference
     * @throws NullPointerException if playerStart or scoreboard is null
     */
    public GameState(Position playerStart, Collection<Position> enemyStarts, Scoreboard scoreboard) {
        this.player = Objects.requireNonNull(playerStart);
        if (enemyStarts != null) this.enemies.addAll(enemyStarts);
        this.scoreboard = Objects.requireNonNull(scoreboard);
    }

    // Mutations

    /**
     * Returns the current status.
     *
     * @return RUNNING, WON, or LOST
     */
    public Status status() {return status;}

    /**
     * Sets the game to running.
     */
    public void setRunning() { status = Status.RUNNING; }

    /**
     * Marks the game as won and stops the scoreboard.
     */
    public void setWon()  { status = Status.WON;  scoreboard.stop(); }

    /**
     * Marks the game as lost and stops the scoreboard.
     */
    public void setLost() { status = Status.LOST; scoreboard.stop(); }
}
