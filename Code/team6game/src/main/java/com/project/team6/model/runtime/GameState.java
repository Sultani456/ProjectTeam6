package com.project.team6.model.runtime;

import com.project.team6.model.board.Position;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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

    /** Scoreboard for time and score (kept for reference, not lifecycle). */
    private final Scoreboard scoreboard;

    /**
     * Creates a game state snapshot.
     *
     * @param playerStart starting player position
     * @param enemyStarts starting enemy positions, may be null
     * @param scoreboard  scoreboard reference
     * @throws NullPointerException if playerStart or scoreboard is null
     */
    public GameState(Position playerStart,
                     Collection<Position> enemyStarts,
                     Scoreboard scoreboard) {
        this.player = Objects.requireNonNull(playerStart, "playerStart");
        if (enemyStarts != null) {
            this.enemies.addAll(enemyStarts);
        }
        this.scoreboard = Objects.requireNonNull(scoreboard, "scoreboard");
    }

    // ---------------------------------------------------------------------
    // Status
    // ---------------------------------------------------------------------

    /**
     * Returns the current status.
     *
     * @return RUNNING, WON, or LOST
     */
    public Status status() {
        return status;
    }

    /**
     * Sets the game to running.
     */
    public void setRunning() {
        status = Status.RUNNING;
    }

    /**
     * Marks the game as won.
     * The controller is responsible for stopping timers.
     */
    public void setWon() {
        status = Status.WON;
    }

    /**
     * Marks the game as lost.
     * The controller is responsible for stopping timers.
     */
    public void setLost() {
        status = Status.LOST;
    }

    // ---------------------------------------------------------------------
    // Positions snapshot (optional, for views / HUD / saving)
    // ---------------------------------------------------------------------

    /**
     * Returns the last recorded player position.
     *
     * @return player position snapshot
     */
    public Position player() {
        return player;
    }

    /**
     * Updates the stored player position.
     *
     * @param newPos new player position
     */
    public void setPlayer(Position newPos) {
        this.player = Objects.requireNonNull(newPos, "newPos");
    }

    /**
     * Returns an unmodifiable view of enemy positions.
     *
     * @return enemy position snapshots
     */
    public Set<Position> enemies() {
        return Collections.unmodifiableSet(enemies);
    }

    /**
     * Replaces all enemy snapshots with the given collection.
     *
     * @param newEnemies new enemy positions, may be null
     */
    public void setEnemies(Collection<Position> newEnemies) {
        enemies.clear();
        if (newEnemies != null) {
            enemies.addAll(newEnemies);
        }
    }

    /**
     * Returns thse associated scoreboard.
     *d
     * @return scoreeboard reference
     */
    public Scoreboard scoreboard() {
        return scoreboard;
    }
}
