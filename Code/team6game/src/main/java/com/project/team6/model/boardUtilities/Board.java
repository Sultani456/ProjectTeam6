package com.project.team6.model.boardUtilities;

import com.project.team6.controller.GameController;
import com.project.team6.model.characters.*;
import com.project.team6.model.characters.enemies.*;
import com.project.team6.model.collectibles.*;
import com.project.team6.model.collectibles.rewards.*;
import com.project.team6.model.boardUtilities.generators.BoardGenerator;

import java.util.*;

/**
 * Authoritative model of the board: terrain, occupants, and items.
 * Owns spawning and movement legality. Now includes a tick() that
 * advances enemies, expires BonusRewards, and returns a summary.
 */
public final class Board {

    // ------------------------------------------------------------
    // Grid
    // ------------------------------------------------------------
    private final int rows, cols;
    private final Cell[][] grid;
    private final Position start, exit;

    public int rows() { return rows; }
    public int cols() { return cols; }
    public Position start() { return start; }
    public Position exit()  { return exit; }

    public boolean isInBounds(Position p) {
        return p.x() >= 0 && p.y() >= 0 && p.x() < cols && p.y() < rows;
    }

    public Cell cellAt(Position p) {
        return isInBounds(p) ? grid[p.y()][p.x()] : null;
    }

    // ------------------------------------------------------------
    // RNG
    // ------------------------------------------------------------
    private final Random rng = new Random();

    // ------------------------------------------------------------
    // Timed Bonus Spawner (quota-based: total appearances over the whole game)
    // ------------------------------------------------------------
    private boolean bonusSpawnerEnabled = false;
    private int tickMillis = 120;        // must match controller timer
    private long ticks = 0;              // world ticks since start

    // spawn & life windows (in ticks)
    private int spawnMinTicks, spawnMaxTicks;
    private int lifeMinTicks,  lifeMaxTicks;
    private long nextSpawnTick = -1;

    // total number of bonus appearances allowed (across the whole run)
    private int bonusTotalQuota = 0;     // total to appear
    private int bonusRemaining = 0;   // how many have been spawned already

    private int bonusPoints = 10;        // score amount per bonus

    private static final class TimedBonus {
        final Position pos;
        final long expiryTick;
        TimedBonus(Position pos, long expiryTick) { this.pos = pos; this.expiryTick = expiryTick; }
    }
    private final List<TimedBonus> activeBonuses = new ArrayList<>();

    // ------------------------------------------------------------
    // Tick Summary
    // ------------------------------------------------------------
    public static final class TickSummary {
        public final int enemiesAttempted, enemiesMoved, bonusExpired;
        public final boolean playerCaught;
        public TickSummary(int attempted, int moved, int expired, boolean caught) {
            this.enemiesAttempted = attempted;
            this.enemiesMoved     = moved;
            this.bonusExpired     = expired;
            this.playerCaught     = caught;
        }
    }

    // for when the player is caught
    private Position explosionPos = null;

    public void setExplosion(Position p) { explosionPos = p; }
    public Position explosionPos()       { return explosionPos; }

    // ------------------------------------------------------------
    // Construction from generator
    // ------------------------------------------------------------
    public Board(BoardGenerator.Output output) {
        Objects.requireNonNull(output, "BoardGenerator output must not be null.");

        this.rows = output.rows();
        this.cols = output.cols();
        this.start = output.start();
        this.exit = output.exit();

        // --- Create grid with correct terrain ---
        this.grid = new Cell[rows][cols];
        Cell.Terrain[][] terrainMap = output.terrain();

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Position p = new Position(x, y);
                Cell.Terrain terrain = terrainMap[y][x];

                // Override terrain for start/exit tiles
                if (p.equals(start))      terrain = Cell.Terrain.START;
                else if (p.equals(exit))  terrain = Cell.Terrain.EXIT;

                grid[y][x] = new Cell(terrain);
            }
        }

        // --- Optional: Validation ---
        if (start == null || exit == null) {
            throw new IllegalStateException("Board requires both start and exit positions.");
        }
        if (!isInBounds(start) || !isInBounds(exit)) {
            throw new IllegalArgumentException("Start or Exit lies outside grid bounds.");
        }
    }

    // ------------------------------------------------------------
    // Timed Bonus Spawner configuration (quota-based)
    // ------------------------------------------------------------
    /**
     * Configure randomized, delayed BonusReward spawns with a batch quota.
     * Example: totalBonusesToAppear = 5
     *  - First spawn tries to place 5 bonuses.
     *  - If player collects 1 (and others expire), next spawn places 4.
     *  - If then player collects 2, next spawn places 2. Etc.
     *
     * @param totalBonusesToAppear total number of bonuses that should EVER be collectible across the run
     * @param spawnMinSec          min seconds until next batch spawn
     * @param spawnMaxSec          max seconds until next batch spawn
     * @param lifeMinSec           min seconds a spawned bonus remains before auto-expire
     * @param lifeMaxSec           max seconds a spawned bonus remains before auto-expire
     * @param bonusPoints          points awarded per bonus
     */
    public void configureBonusSpawner(int totalBonusesToAppear, int bonusPoints,
                                               int spawnMinSec, int spawnMaxSec,
                                               int lifeMinSec,  int lifeMaxSec) {
        // Count strictly empty floor cells (no Start/End, no occupants, no items)
        int emptyFloors = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Position p = new Position(x, y);
                if (p.equals(start) || p.equals(exit)) continue;
                Cell c = grid[y][x];
                if (c.terrain() != Cell.Terrain.FLOOR) continue;
                if (c.hasPlayer() || c.hasEnemy() || c.item() != null) continue;
                emptyFloors++;
            }
        }

        if (totalBonusesToAppear > emptyFloors) {
            IllegalArgumentException ex = new IllegalArgumentException(
                    "totalBonusesToAppear (" + totalBonusesToAppear + ") exceeds available empty floor cells (" + emptyFloors + ")."
            );
            ex.printStackTrace();
            System.exit(1); // end program as requested
        }

        this.bonusTotalQuota = Math.max(0, totalBonusesToAppear);
        this.bonusRemaining  = this.bonusTotalQuota;   // decremented ONLY when player collects
        this.tickMillis = Math.max(10, GameController.DEFAULT_TICK_MS);
        this.spawnMinTicks = secToTicks(spawnMinSec);
        this.spawnMaxTicks = secToTicks(spawnMaxSec);
        this.lifeMinTicks  = secToTicks(lifeMinSec);
        this.lifeMaxTicks  = secToTicks(lifeMaxSec);
        this.bonusPoints   = bonusPoints;
        this.bonusSpawnerEnabled = (bonusRemaining > 0);
        scheduleNextBonusSpawn();
    }


    private int secToTicks(int seconds) {
        return Math.max(1, (int) Math.round((seconds * 1000.0) / this.tickMillis));
    }

    private void scheduleNextBonusSpawn() {
        if (!bonusSpawnerEnabled || bonusRemaining <= 0) {
            nextSpawnTick = -1; // no further spawns
            return;
        }
        int delay = randBetween(spawnMinTicks, spawnMaxTicks);
        nextSpawnTick = ticks + delay;
    }

    private int randBetween(int aInclusive, int bInclusive) {
        if (bInclusive < aInclusive) { int t = aInclusive; aInclusive = bInclusive; bInclusive = t; }
        return aInclusive + rng.nextInt(bInclusive - aInclusive + 1);
    }

    // ------------------------------------------------------------
    // Manual spawners (optional use)
    // ------------------------------------------------------------
    public void spawnEnemies(int count, int movePeriod) {
        List<Position> spots = freeFloorCells(count, true, true, 3,3);
        for (Position p : spots) {
            Enemy e = new MovingEnemy(p, movePeriod);
            cellAt(p).addOccupant(e);
        }
    }

    public void spawnRegularRewards(int count, int amountEach) {
        for (Position p : freeFloorCells(count, true, true, 0,0)) {
            cellAt(p).setItem(new RegularReward(p, amountEach));
        }
    }

    public void spawnPunishments(int count, int penaltyEach) {
        for (Position p : freeFloorCells(count, true, true, 2, 2)) {
            cellAt(p).setItem(new Punishment(p, penaltyEach));
        }
    }

    /** Avoid calling at start if you want no initial bonuses. */
    public void spawnBonusRewards(int count, int amountEach) {
        for (Position p : freeFloorCells(count, true, true,0,0)) {
            cellAt(p).setItem(new BonusReward(p, amountEach));
        }
    }

    public static int chebyshev(Position a, Position b) {
        return Math.max(Math.abs(a.x() - b.x()), Math.abs(a.y() - b.y()));
    }

    // NEW overload used by enemy/punishment spawners
    private List<Position> freeFloorCells(int n,
                                          boolean excludeStartExit,
                                          boolean excludeOccupied,
                                          int minChebFromStart,
                                          int minChebFromExit) {
        ArrayList<Position> all = new ArrayList<>();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Position p = new Position(x, y);
                Cell c = cellAt(p);
                if (c.terrain() != Cell.Terrain.FLOOR) continue;
                if (excludeStartExit && (p.equals(start) || p.equals(exit))) continue;
                if (excludeOccupied && (c.hasPlayer() || c.hasEnemy() || c.item() != null)) continue;
                if (minChebFromStart > 0 && chebyshev(p, start) < minChebFromStart) continue;
                if (minChebFromExit  > 0 && chebyshev(p, exit)  < minChebFromExit ) continue;
                all.add(p);
            }
        }
        Collections.shuffle(all, rng);
        return all.subList(0, Math.min(n, all.size()));
    }


    // ------------------------------------------------------------
    // Items
    // ------------------------------------------------------------
    public Optional<CollectibleObject> collectAt(Position p) {
        Cell c = cellAt(p);
        if (c == null) return Optional.empty();
        CollectibleObject it = c.item();
        if (it != null) {
            c.setItem(null);
            // If it was a timed bonus, remove its timer entry and decrement remaining quota
            if (it instanceof BonusReward) {
                activeBonuses.removeIf(tb -> tb.pos.equals(p));
                if (bonusRemaining > 0) bonusRemaining--;
                if (bonusRemaining <= 0) {
                    bonusSpawnerEnabled = false;
                    nextSpawnTick = -1;
                }
            }
            return Optional.of(it);
        }
        return Optional.empty();
    }


    // ------------------------------------------------------------
    // Movement
    // ------------------------------------------------------------
    public MoveResult step(CharacterObject who, Direction dir) {
        if (who == null || dir == null) return MoveResult.BLOCKED;

        Position from = who.position();
        Position to   = new Position(from.x() + dir.dx, from.y() + dir.dy);

        if (!isInBounds(to)) return MoveResult.BLOCKED;
        Cell dest = cellAt(to);
        if (dest == null || !dest.isWalkableTerrain()) return MoveResult.BLOCKED;

        // Enemies cannot enter Start/Exit
        if (who instanceof Enemy
                && (dest.terrain() == Cell.Terrain.START || dest.terrain() == Cell.Terrain.EXIT)) {
            return MoveResult.BLOCKED;
        }

        if (!dest.isEnterableFor(who)) return MoveResult.BLOCKED;

        // Commit move
        Cell src = cellAt(from);
        if (src != null) src.removeOccupant(who);
        dest.addOccupant(who);
        who.setPosition(to);

        return dest.hasCollision() ? MoveResult.COLLISION : MoveResult.MOVED;
    }

    // ------------------------------------------------------------
    // Tick
    // ------------------------------------------------------------
    public TickSummary tick(Position playerPos) {
        ticks++; // advance world time

        // A) Expire timed bonuses that reached their expiry (quota unaffected)
        int expiredNow = 0;
        if (!activeBonuses.isEmpty()) {
            Iterator<TimedBonus> it = activeBonuses.iterator();
            while (it.hasNext()) {
                TimedBonus tb = it.next();
                if (tb.expiryTick <= ticks) {
                    Cell c = cellAt(tb.pos);
                    if (c != null && c.item() instanceof BonusReward) {
                        c.setItem(null);
                        expiredNow++;
                    }
                    it.remove();
                }
            }
        }

        // B) Spawn a NEW BATCH if time reached and quota remains
        if (bonusSpawnerEnabled && nextSpawnTick >= 0 && bonusRemaining > 0 && ticks >= nextSpawnTick) {
            int currentlyActive = activeBonuses.size();
            int needed = Math.max(0, bonusRemaining - currentlyActive);

            if (needed > 0) {
                // Strict free floor cells only (no Start/End, no occupants, no any item)
                List<Position> targets = freeFloorCells(needed, /*excludeStartExit*/ true, /*excludeOccupied*/ true,0,0);
                // Place exactly 'needed' bonuses
                for (Position p : targets) {
                    Cell c = cellAt(p);
                    c.setItem(new BonusReward(p, bonusPoints));
                    long life = randBetween(lifeMinTicks, lifeMaxTicks);
                    activeBonuses.add(new TimedBonus(p, ticks + life));
                }
            }

            // Schedule the next spawn window
            scheduleNextBonusSpawn();
        }

        // C) Move enemies
        List<Enemy> enemies = snapshotEnemies();
        int attempted = enemies.size();
        int moved = 0;
        boolean playerCaught = false;

        for (Enemy e : enemies) {
            Position before = e.position();
            e.tick(this, playerPos);          // internally calls board.step(...)
            Position after = e.position();
            if (!after.equals(before)) moved++;
            if (after.equals(playerPos)) playerCaught = true;
        }

        // D) Defensive collision check for player cell
        Cell pc = cellAt(playerPos);
        if (pc != null && pc.hasCollision()) playerCaught = true;

        return new TickSummary(attempted, moved, expiredNow, playerCaught);
    }


    private List<Enemy> snapshotEnemies() {
        ArrayList<Enemy> list = new ArrayList<>();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Enemy e = grid[y][x].enemy();
                if (e != null) list.add(e);
            }
        }
        return list;
    }

}



