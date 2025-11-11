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
    private final Position start, exit;
    private final Map<Position, Cell> grid;
    private final Random rng;

    // --- Summary returned by tick() ---
    public static final class TickSummary {
        private final int enemiesAttempted;
        private final int enemiesMoved;
        private final int bonusExpired;
        private final boolean playerCaught;

        public TickSummary(int enemiesAttempted, int enemiesMoved, int bonusExpired, boolean playerCaught) {
            this.enemiesAttempted = enemiesAttempted;
            this.enemiesMoved     = enemiesMoved;
            this.bonusExpired     = bonusExpired;
            this.playerCaught     = playerCaught;
        }
        public int enemiesAttempted() { return enemiesAttempted; }
        public int enemiesMoved()     { return enemiesMoved; }
        public int bonusExpired()     { return bonusExpired; }
        public boolean playerCaught() { return playerCaught; }

        @Override public String toString() {
            return "TickSummary{attempted=" + enemiesAttempted +
                    ", moved=" + enemiesMoved +
                    ", bonusExpired=" + bonusExpired +
                    ", playerCaught=" + playerCaught + "}";
        }
    }

    public Board(BoardGenerator.Options genOpts) {
        this.rows = genOpts.rows();
        this.cols = genOpts.cols();
        this.start = genOpts.start();
        this.exit  = genOpts.exit();
        this.rng   = new Random(genOpts.randomSeed());
        this.grid  = BoardGenerator.generate(genOpts);
    }

    // ---- Queries ----

    public int rows() { return rows; }
    public int cols() { return cols; }
    public Position start() { return start; }
    public Position exit()  { return exit; }

    public Cell cellAt(Position p) { return grid.get(p); }

    public boolean isInBounds(Position p) {
        return p.x() >= 0 && p.y() >= 0 && p.x() < cols && p.y() < rows;
    }

    public boolean isWalkable(Position p) {
        Cell c = grid.get(p);
        return c != null && c.isWalkableTerrain();
    }

    /** Returns a String view (ASCII) of the current board. */
    public String displayBoard() {
        return BoardGenerator.toAscii(grid, rows, cols);
    }

    // ---- Spawning (no factories) ----

    /** Place N regular rewards of a fixed amount on free FLOOR cells. */
    public List<Position> spawnRegularRewards(int count, int amount) {
        return placeItems(count, pos -> new RegularReward(pos.x(), pos.y(), amount));
    }

    /** Place N bonus rewards of a fixed amount on free FLOOR cells. */
    public List<Position> spawnBonusRewards(int count, int amount) {
        return placeItems(count, pos -> new BonusReward(pos.x(), pos.y(), amount));
    }

    /** Place N punishments with a fixed penalty (negative score). */
    public List<Position> spawnPunishments(int count, int penalty) {
        return placeItems(count, pos -> new Punishment(pos.x(), pos.y(), penalty));
    }

    /** Place N moving enemies on free FLOOR cells (not Start/Exit). */
    public List<Position> spawnEnemies(int count) {
        List<Position> spots = pickFreeFloorCells(count, /*excludeStartExit*/true);
        for (Position p : spots) {
            MovingEnemy enemy = new MovingEnemy(p.x(), p.y());
            grid.get(p).setOccupant(enemy);
        }
        return spots;
    }

    // ---- Movement and item interaction ----

    public boolean tryEnter(Position to) {
        Cell c = grid.get(to);
        return c != null && c.isEnterableNow();
    }

    /** Remove and return collectible at p, if any. */
    public Optional<CollectibleObject> collectAt(Position p) {
        Cell c = grid.get(p);
        if (c == null) return Optional.empty();
        CollectibleObject item = c.item();
        if (item == null) return Optional.empty();
        c.setItem(null);
        return Optional.of(item);
    }

    /** Move a character from 'from' to 'to' if enterable. Returns success. */
    public boolean moveCharacter(Position from, Position to) {
        if (!tryEnter(to)) return false;
        Cell a = grid.get(from);
        Cell b = grid.get(to);
        if (a == null || b == null) return false;
        CharacterObject who = a.occupant();
        if (who == null) return false;
        a.setOccupant(null);
        b.setOccupant(who);
        who.setPosition(to);
        return true;
    }

    // ---- Ticking ----

    /**
     * Advance world one tick:
     * - Decrement lifetimes of BonusRewards; remove expired.
     * - Move each Enemy by at most one cell (using Enemy.tick()).
     * - Report whether an enemy ended up on the player's position.
     *
     * @param playerPos the current player position.
     * @return TickSummary for controller decisions (e.g., lose condition).
     */
    public TickSummary tick(Position playerPos) {
        // 1) Expire bonus rewards
        int bonusExpired = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Cell c = grid.get(new Position(x, y));
                if (c == null) continue;
                if (c.item() instanceof BonusReward br) {
                    if (!br.onTickAndAlive()) {
                        c.setItem(null);
                        bonusExpired++;
                    }
                }
            }
        }

        // 2) Move enemies one step. Snapshot first to avoid alias issues during movement.
        List<Enemy> enemies = snapshotEnemies();

        int attempted = enemies.size();
        int moved = 0;

        for (Enemy e : enemies) {
            Position before = e.position();
            e.tick(this, playerPos);           // enemy decides its step; uses Board.tryEnter/move via CharacterObject.tryMove(...)
            Position after = e.position();
            if (!after.equals(before)) moved++;
        }

        // 3) Collision: did any enemy land on the player tile?
        boolean playerCaught = enemyOn(playerPos);

        return new TickSummary(attempted, moved, bonusExpired, playerCaught);
    }

    // ---- Helpers ----

    private boolean enemyOn(Position p) {
        Cell c = grid.get(p);
        return c != null && c.occupant() instanceof Enemy;
    }

    /** Collect a snapshot list of current enemies by scanning cells. */
    private List<Enemy> snapshotEnemies() {
        List<Enemy> list = new ArrayList<>();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Cell c = grid.get(new Position(x, y));
                if (c != null && c.occupant() instanceof Enemy e) {
                    list.add(e);
                }
            }
        }
        return list;
    }

    private interface ItemMaker { CollectibleObject make(Position p); }

    private List<Position> placeItems(int count, ItemMaker maker) {
        List<Position> spots = pickFreeFloorCells(count, /*excludeStartExit*/false);
        for (Position p : spots) {
            Cell c = grid.get(p);
            if (c.item() == null) c.setItem(maker.make(p));
        }
        return spots;
    }

    /** Pick up to 'count' random FLOOR cells with no occupant/item. */
    private List<Position> pickFreeFloorCells(int count, boolean excludeStartExit) {
        List<Position> candidates = grid.entrySet().stream()
                .filter(e -> e.getValue().terrain() == Cell.Terrain.FLOOR)
                .filter(e -> e.getValue().item() == null && e.getValue().occupant() == null)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(ArrayList::new));

        if (excludeStartExit) {
            candidates.remove(start);
            candidates.remove(exit);
        }

        Collections.shuffle(candidates, rng);
        return candidates.subList(0, Math.min(count, candidates.size()));
    }
}
