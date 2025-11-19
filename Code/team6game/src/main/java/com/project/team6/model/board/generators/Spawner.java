package com.project.team6.model.board.generators;

import com.project.team6.model.board.Board;
import com.project.team6.model.board.Cell;
import com.project.team6.model.board.Position;
import com.project.team6.model.board.generators.helpers.SpawnerHelper;
import com.project.team6.model.characters.enemies.MovingEnemy;
import com.project.team6.model.collectibles.rewards.*;
import com.project.team6.model.collectibles.Punishment;

import java.util.*;

/**
 * Centralised spawning logic for a Board:
 *  - Regular rewards
 *  - Punishments (with safe-path constraints)
 *  - Enemies
 *  - Timed bonus batches
 *
 * A Spawner instance is associated with a single Board.
 */
public final class Spawner {

    /** Tick duration in milliseconds (must match GameController’s tick). */
    private final Board board;
    private final int rows;
    private final int cols;
    private final Cell[][] grid;

    private final int tickMillis;
    private final Random rng;

    // -----------------------------------------------------------------
    // Bonus-reward configuration
    // -----------------------------------------------------------------

    /** Whether bonus spawning is enabled. */
    private boolean bonusEnabled = false;

    /**
     * Number of bonus rewards that still need to be COLLECTED.
     * Spawns always try to show exactly this many (subject to free cells).
     * This value is decremented only when a bonus is collected.
     */
    private int bonusRemaining = 0;

    /** Score value of each bonus. */
    private int bonusPointsPer = 0;

    /** Spawn delay in ticks (inclusive range). */
    private int spawnMinTicks = 0;
    private int spawnMaxTicks = 0;

    /** Lifetime in ticks (inclusive range). */
    private int lifeMinTicks = 0;
    private int lifeMaxTicks = 0;

    /** Countdown until next bonus batch is allowed to spawn. */
    private int ticksUntilNextSpawn = -1;

    // ================================================================
    // Constructor, getters, setters
    // ================================================================
    public Spawner(Board board, int tickMillis) {
        this.board = Objects.requireNonNull(board);
        this.rows = board.rows();
        this.cols = board.cols();
        this.grid = board.grid();

        this.tickMillis = tickMillis;
        this.rng = new Random();
    }

    // ================================================================
    // Bonus spawning (config + per-tick)
    // ================================================================

    private int secondsToTicks(int seconds) {
        if (seconds <= 0) return 0;
        double ticks = (seconds * 1000.0) / tickMillis;
        return Math.max(1, (int) Math.round(ticks));
    }

    /**
     * Configure bonus spawning with a quota:
     *
     * totalBonusesToAppear – total number that can ever be collected.
     * At each spawn wave we show exactly “bonusRemaining” bonuses,
     * subject to there being enough free floor cells.  When the player
     * collects one, we call {@link #notifyBonusCollected()}, which
     * reduces bonusRemaining for the next spawn wave.
     */
    public void spawnBonusRewards(int totalBonusesToAppear,
                                  int pointsPer,
                                  int spawnMinSec,
                                  int spawnMaxSec,
                                  int lifeMinSec,
                                  int lifeMaxSec) {
        if (totalBonusesToAppear <= 0) {
            bonusEnabled = false;
            bonusRemaining = 0;
            return;
        }

        // Check capacity once up-front: must be at least this many free cells.
        int freeCells = SpawnerHelper.freeFloorCells(board).size();
        if (totalBonusesToAppear > freeCells) {
            throw new IllegalArgumentException(
                    "totalBonusesToAppear (" + totalBonusesToAppear +
                            ") is larger than free floor cells (" + freeCells + ")");
        }

        this.bonusEnabled = true;
        this.bonusRemaining = totalBonusesToAppear;
        this.bonusPointsPer = pointsPer;

        this.spawnMinTicks = secondsToTicks(spawnMinSec);
        this.spawnMaxTicks = secondsToTicks(spawnMaxSec);
        this.lifeMinTicks = secondsToTicks(lifeMinSec);
        this.lifeMaxTicks = secondsToTicks(lifeMaxSec);

        if (spawnMaxTicks < spawnMinTicks) {
            spawnMaxTicks = spawnMinTicks;
        }
        if (lifeMaxTicks < lifeMinTicks) {
            lifeMaxTicks = lifeMinTicks;
        }

        scheduleNextBonusSpawn();
    }

    /** Called each tick by the controller AFTER Board.tick(...). */
    public void onTick() {
        if (!bonusEnabled || bonusRemaining <= 0) return;

        // Don’t spawn a new wave while old bonuses are still visible.
        if (board.hasActiveBonusRewards()) return;

        if (ticksUntilNextSpawn > 0) {
            ticksUntilNextSpawn--;
            return;
        }

        // Time to spawn a new wave: we want exactly “bonusRemaining” bonuses.
        List<Position> free = SpawnerHelper.freeFloorCells(board);
        if (free.isEmpty()) {
            // Nothing to do (should not really happen due to upfront check).
            scheduleNextBonusSpawn();
            return;
        }

        Collections.shuffle(free, rng);

        int toSpawn = Math.min(bonusRemaining, free.size());
        int lifeRange = Math.max(1, lifeMaxTicks - lifeMinTicks + 1);

        for (int i = 0; i < toSpawn; i++) {
            Position pos = free.get(i);
            int lifeTicks = lifeMinTicks + rng.nextInt(lifeRange);
            BonusReward bonus = new BonusReward(pos, bonusPointsPer, lifeTicks);
            board.registerBonusReward(bonus);
        }

        // Wave spawned. The quota "bonusRemaining" is *not* modified here;
        // it only decreases when a bonus is actually collected.
        scheduleNextBonusSpawn();
    }

    /** Inform spawner that one bonus has been collected. */
    public void notifyBonusCollected() {
        if (!bonusEnabled) return;
        if (bonusRemaining > 0) bonusRemaining--;
        if (bonusRemaining <= 0) {
            bonusEnabled = false;
        }
    }

    // helper
    private void scheduleNextBonusSpawn() {
        if (!bonusEnabled || bonusRemaining <= 0) {
            ticksUntilNextSpawn = -1;
            return;
        }
        int range = Math.max(0, spawnMaxTicks - spawnMinTicks);
        ticksUntilNextSpawn = spawnMinTicks + (range == 0 ? 0 : rng.nextInt(range + 1));
    }

    // ================================================================
    // Regular rewards
    // ================================================================

    public void spawnRegularRewards(int count, int pointsPer) {
        if (count <= 0) return;

        List<Position> free = SpawnerHelper.freeFloorCells(board);
        if (free.size() < count) {
            throw new IllegalStateException(
                    "Not enough free cells to place " + count + " regular rewards.");
        }

        Collections.shuffle(free, rng);
        for (int i = 0; i < count; i++) {
            Position p = free.get(i);
            RegularReward r = new RegularReward(p, pointsPer);
            board.registerRegularReward(r);
        }
    }

    // ================================================================
    // Punishments: keep safe paths
    // ================================================================

    /**
     * Spawn punishments such that:
     *  - Start to Exit remains reachable.
     *  - Every regular reward remains reachable from Start.
     *  - Punishments do not sit directly on Start/Exit cells.
     *
     * Cave rule (“single gateway” cave not closed by a punishment) is
     * approximated by the reachability checks: if a punishment blocks the
     * only path to a reward pocket, canReach(...) will fail.
     */
    public void spawnPunishments(int count, int penaltyPer) {
        if (count <= 0) return;

        List<Position> free = SpawnerHelper.freeFloorCells(board);
        Position start = board.start();
        Position exit = board.exit();

        // Don’t allow punishments directly on start/exit
        free.remove(start);
        free.remove(exit);

        if (free.isEmpty()) return;

        Collections.shuffle(free, rng);

        List<Position> placed = new ArrayList<>();
        outer:
        for (Position candidate : free) {

            // Temporarily assume we place a punishment here
            Set<Position> blocked = new HashSet<>(placed);
            blocked.add(candidate);

            // 1) Start–Exit must remain reachable
            if (!SpawnerHelper.canReach(board, start, exit, blocked)) {
                continue;
            }

            // 2) Every regular reward must remain reachable from Start
            boolean ok = true;
            for (RegularReward rr : board.regularRewards()) {
                if (!SpawnerHelper.canReach(board, start, rr.position(), blocked)) {
                    ok = false;
                    break;
                }
            }
            if (!ok) continue;

            // Passed all checks => place punishment
            Punishment p = new Punishment(candidate, penaltyPer);
            board.registerPunishment(p);
            placed.add(candidate);

            if (placed.size() >= count) break outer;
        }
    }

    // ================================================================
    // Enemies
    // ================================================================

    /**
     * Spawn moving enemies on free floor cells, staying at least Chebyshev
     * distance 3 away from Start and Exit.
     */
    public void spawnEnemies(int count, int movePeriod) {
        if (count <= 0) return;

        List<Position> free = SpawnerHelper.freeFloorCells(board);
        Position start = board.start();
        Position exit = board.exit();

        free.removeIf(p ->
                Board.chebyshev(p, start) < 3 || Board.chebyshev(p, exit) < 3);

        if (free.isEmpty()) return;

        Collections.shuffle(free, rng);

        int placed = 0;
        for (Position pos : free) {
            if (placed >= count) break;
            MovingEnemy e = new MovingEnemy(pos, movePeriod);
            board.registerEnemy(e);
            placed++;
        }
    }
}


