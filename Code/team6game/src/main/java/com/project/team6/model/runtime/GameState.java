package com.project.team6.model.runtime;

import com.project.team6.model.board.Position;

import java.util.*;

/**
 * Lightweight snapshot of game status and actor positions.
 * Controller mutates this; views read it.
 */
public final class GameState {

    public enum Status { RUNNING, WON, LOST }

    private Status status = Status.RUNNING;

    private Position player;
    private final Set<Position> enemies = new HashSet<>();

    private final Scoreboard scoreboard;

    public GameState(Position playerStart, Collection<Position> enemyStarts, Scoreboard scoreboard) {
        this.player = Objects.requireNonNull(playerStart);
        if (enemyStarts != null) this.enemies.addAll(enemyStarts);
        this.scoreboard = Objects.requireNonNull(scoreboard);
    }


    // Mutations

    public Status status() {return status;}
    public void setRunning() { status = Status.RUNNING; }
    public void setWon()  { status = Status.WON;  scoreboard.stop(); }
    public void setLost() { status = Status.LOST; scoreboard.stop(); }
}

