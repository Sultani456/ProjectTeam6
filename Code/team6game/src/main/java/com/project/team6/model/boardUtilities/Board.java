package com.project.team6.model.boardUtilities;

import com.project.team6.model.characters.*;
import com.project.team6.model.characters.enemies.*;
import com.project.team6.model.collectibles.*;
import com.project.team6.model.collectibles.rewards.*;
import com.project.team6.model.generators.BoardGenerator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Authoritative model of the board: terrain, occupants, and items.
 * Owns spawning and movement legality. Now includes a tick() that
 * advances enemies, expires BonusRewards, and returns a summary.
 */

public final class Board {

    private final int rows, cols;
    private final Cell[][] grid;
    private final Position start, exit;

    public static final class TickSummary {
        public final int enemiesAttempted, enemiesMoved, bonusExpired;
        public final boolean playerCaught;
        public TickSummary(int attempted, int moved, int expired, boolean caught) {
            this.enemiesAttempted = attempted; this.enemiesMoved = moved;
            this.bonusExpired = expired; this.playerCaught = caught;
        }
    }

    /** Build from generator output. */
    public Board(BoardGenerator.Output gen) {
        this.rows = gen.rows();
        this.cols = gen.cols();
        this.grid = new Cell[rows][cols];
        // Create cells
        for (int y=0; y<rows; y++)
            for (int x=0; x<cols; x++)
                grid[y][x] = new Cell(gen.terrainAt(x,y));
        // remember S/E
        this.start = gen.start();
        this.exit  = gen.exit();
    }

    // --- Basic queries
    public int rows() { return rows; }
    public int cols() { return cols; }
    public boolean isInBounds(Position p) { return p.x()>=0 && p.y()>=0 && p.x()<cols && p.y()<rows; }
    public Position start() { return start; }
    public Position exit()  { return exit; }

    public Cell cellAt(Position p) { return isInBounds(p) ? grid[p.y()][p.x()] : null; }

    // --- Spawns (simple, random examples you can replace with your own)
    public void spawnEnemies(int count, int movePeriod) {
        List<Position> spots = freeFloorCells(count);
        for (Position p : spots) {
            Enemy e = new MovingEnemy(p, movePeriod);
            cellAt(p).addOccupant(e);
        }
    }
    public void spawnEnemies(int count) { spawnEnemies(count, 1); }

    public void spawnRegularRewards(int count, int amountEach) {
        for (Position p : freeFloorCells(count)) {
            // your RegularReward(int x,int y,int amount) type
            var r = new com.project.team6.model.collectibles.rewards.RegularReward(p, amountEach);
            cellAt(p).setItem(r);
        }
    }
    public void spawnBonusRewards(int count, int amountEach) {
        for (Position p : freeFloorCells(count)) {
            var r = new com.project.team6.model.collectibles.rewards.BonusReward(p, amountEach);
            cellAt(p).setItem(r);
        }
    }
    public void spawnPunishments(int count, int penaltyEach) {
        for (Position p : freeFloorCells(count)) {
            var r = new com.project.team6.model.collectibles.Punishment(p, penaltyEach);
            cellAt(p).setItem(r);
        }
    }

    private List<Position> freeFloorCells(int n) {
        ArrayList<Position> all = new ArrayList<>();
        for (int y=0; y<rows; y++)
            for (int x=0; x<cols; x++) {
                Position p = new Position(x,y);
                Cell c = cellAt(p);
                if (c.terrain()==Cell.Terrain.FLOOR && !c.hasPlayer() && !c.hasEnemy() && c.item()==null) all.add(p);
            }
        Collections.shuffle(all, new Random());
        return all.subList(0, Math.min(n, all.size()));
    }

    // --- Item collection
    public Optional<CollectibleObject> collectAt(Position p) {
        Cell c = cellAt(p);
        if (c == null) return Optional.empty();
        var it = c.item();
        if (it != null) { c.setItem(null); return Optional.of(it); }
        return Optional.empty();
    }

    // --- STEP: unified move primitive (multi-occupancy with per-type cap)
    public MoveResult step(CharacterObject who, Direction dir) {
        if (who == null || dir == null) return MoveResult.BLOCKED;

        Position from = who.position();
        Position to   = new Position(from.x() + dir.dx, from.y() + dir.dy);

        if (!isInBounds(to)) return MoveResult.BLOCKED;
        Cell dest = cellAt(to);
        if (dest == null || !dest.isWalkableTerrain()) return MoveResult.BLOCKED;

        // NEW: enemies cannot enter Start/Exit tiles
        if (who instanceof Enemy && (dest.terrain() == Cell.Terrain.START || dest.terrain() == Cell.Terrain.EXIT)) {
            return MoveResult.BLOCKED;
        }

        if (!dest.isEnterableFor(who)) return MoveResult.BLOCKED;

        Cell src = cellAt(from);
        if (src != null) src.removeOccupant(who);
        dest.addOccupant(who);
        who.setPosition(to);

        return dest.hasCollision() ? MoveResult.COLLISION : MoveResult.MOVED;
    }

    // --- Tick: expire bonus + move enemies + report collision
    public TickSummary tick(Position playerPos) {
        int expired = 0;
        for (int y=0; y<rows; y++)
            for (int x=0; x<cols; x++) {
                var c = grid[y][x];
                if (c.item() instanceof BonusReward br) {
                    if (!br.onTickAndAlive()) { c.setItem(null); expired++; }
                }
            }

        List<Enemy> enemies = snapshotEnemies();
        int attempted = enemies.size(), moved = 0;
        boolean caught = false;

        for (Enemy e : enemies) {
            Position before = e.position();
            e.tick(this, playerPos);           // internally calls board.step(...)
            Position after  = e.position();
            if (!after.equals(before)) moved++;
            if (after.equals(playerPos)) caught = true; // shares the player's cell
        }
        // defensive: if player stands on a cell that already has enemy
        Cell pc = cellAt(playerPos);
        if (pc != null && pc.hasCollision()) caught = true;

        return new TickSummary(attempted, moved, expired, caught);
    }

    private List<Enemy> snapshotEnemies() {
        ArrayList<Enemy> list = new ArrayList<>();
        for (int y=0; y<rows; y++)
            for (int x=0; x<cols; x++) {
                Enemy e = grid[y][x].enemy();
                if (e != null) list.add(e);
            }
        return list;
    }
}

