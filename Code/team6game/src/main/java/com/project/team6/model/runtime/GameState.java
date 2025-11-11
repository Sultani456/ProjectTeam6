package com.project.team6.model.runtime;

import com.project.team6.model.boardUtilities.Position;

import java.util.*;

/**
 * Lightweight snapshot of game status and actor positions.
 * Controller mutates this; views read it.
 */
public final class GameState {

    public enum Status { RUNNING, WON, LOST }

    private Status status = Status.RUNNING;
    private long ticks = 0;

    private Position player;
    private final Set<Position> enemies = new HashSet<>();

    private final Scoreboard scoreboard;

    public GameState(Position playerStart, Collection<Position> enemyStarts, Scoreboard scoreboard) {
        this.player = Objects.requireNonNull(playerStart);
        if (enemyStarts != null) this.enemies.addAll(enemyStarts);
        this.scoreboard = Objects.requireNonNull(scoreboard);
    }

    // Read access
    public Status status() { return status; }
    public long ticks() { return ticks; }
    public Position player() { return player; }
    public Set<Position> enemies() { return Collections.unmodifiableSet(enemies); }
    public Scoreboard scoreboard() { return scoreboard; }

    // Mutations
    public void movePlayerTo(Position p) { this.player = Objects.requireNonNull(p); }

    public void setEnemyPositions(Collection<Position> ps) {
        enemies.clear();
        if (ps != null) enemies.addAll(ps);
    }

    public void tick() { ticks++; }

    public void win()  { status = Status.WON;  scoreboard.stop(); }
    public void lose() { status = Status.LOST; scoreboard.stop(); }
}

