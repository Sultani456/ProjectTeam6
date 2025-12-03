package com.project.team6.model.board.generators;

import com.project.team6.controller.GameConfig;
import com.project.team6.model.board.Board;
import com.project.team6.model.board.Position;
import com.project.team6.model.board.generators.helpers.SpawnerHelper;
import com.project.team6.model.characters.enemies.MovingEnemy;
import com.project.team6.model.collectibles.rewards.*;
import com.project.team6.model.collectibles.Punishment;

import java.util.*;

import static com.project.team6.controller.GameConfig.regularRewardCount;

/**
 * Controls how items and enemies appear on the board.
 * Handles regular rewards, punishments, enemies, and timed bonus rewards.
 * One spawner is used for one board.
 */
public final class Spawner {

    /**
     * The board that receives spawns.
     */
    private final Board board;


    /**
     * Tick length in milliseconds. Must match the controller tick.
     */
    private final int tickMillis;

    /** Random source for placement and timing. */
    private final Random rng;

    // -----------------------------------------------------------------
    // Bonus-reward configuration
    // -----------------------------------------------------------------

    /** True when bonus waves are enabled. */
    private boolean bonusEnabled = false;

    /**
     * Number of bonus items that still need to be collected.
     * Each wave tries to show this many, if enough free cells exist.
     * This value goes down only when the player collects a bonus.
     */
    private int bonusRemaining = 0;

    /** Points awarded for each bonus. */
    private int bonusPointsPer = 0;

    /** Minimum and maximum delay in ticks before a bonus wave spawns. */
    private int spawnMinTicks = 0;
    private int spawnMaxTicks = 0;

    /** Minimum and maximum lifetime in ticks for a bonus. */
    private int lifeMinTicks = 0;
    private int lifeMaxTicks = 0;

    /** Countdown in ticks until the next bonus wave is allowed. */
    private int ticksUntilNextSpawn = -1;

    // ================================================================
    // Constructor, getters, setters
    // ================================================================

    /**
     * Creates a spawner for a board.
     *
     * @param board      the target board
     * @throws NullPointerException if board is null
     */
    public Spawner(Board board) {
        this.board = Objects.requireNonNull(board);

        this.tickMillis = GameConfig.DEFAULT_TICK_MS;
        this.rng = new Random();
    }

    // ================================================================
    // Bonus spawning (config + per-tick)
    // ================================================================

    /**
     * Converts seconds to ticks using the configured tick length.
     *
     * @param seconds whole or fractional seconds
     * @return number of ticks, at least 1 when seconds is positive
     */
    private int secondsToTicks(int seconds) {
        if (seconds <= 0) return 0;
        double ticks = (seconds * 1000.0) / tickMillis;
        return Math.max(1, (int) Math.round(ticks));
    }

    /**
     * Sets up timed bonus rewards with a fixed total quota.
     * The spawner shows a wave with {@code bonusRemaining} items each time.
     * When a bonus is collected, call {@link #notifyBonusCollected()}.
     *
     * @throws IllegalArgumentException if {@code GameConfig.bonusRewardCount} is larger than free cells
     */
    public void spawnBonusRewards() {
        if (GameConfig.bonusRewardCount <= 0) {
            bonusEnabled = false;
            bonusRemaining = 0;
            return;
        }

        // Check capacity once up front
        int freeCells = SpawnerHelper.freeFloorCells(board).size();
        if (GameConfig.bonusRewardCount > freeCells) {
            throw new IllegalArgumentException(
                    "GameConfig.bonusRewardCount (" + GameConfig.bonusRewardCount +
                            ") is larger than free floor cells (" + freeCells + ")");
        }

        this.bonusEnabled = true;
        this.bonusRemaining = GameConfig.bonusRewardCount;
        this.bonusPointsPer = GameConfig.bonusPoints;

        this.spawnMinTicks = secondsToTicks(GameConfig.spawnMinSec);
        this.spawnMaxTicks = secondsToTicks(GameConfig.spawnMaxSec);
        this.lifeMinTicks = secondsToTicks(GameConfig.lifeMinSec);
        this.lifeMaxTicks = secondsToTicks(GameConfig.lifeMaxSec);

        if (spawnMaxTicks < spawnMinTicks) {
            spawnMaxTicks = spawnMinTicks;
        }
        if (lifeMaxTicks < lifeMinTicks) {
            lifeMaxTicks = lifeMinTicks;
        }

        scheduleNextBonusSpawn();
    }

    /**
     * Runs once per tick from the controller after the board tick.
     * Spawns a new bonus wave when allowed and when no bonuses are active.
     */
    public void onTick() {
        if (!bonusEnabled || bonusRemaining <= 0) return;

        // Do not spawn while a wave is still visible
        if (board.hasActiveBonusRewards()) return;

        if (ticksUntilNextSpawn > 0) {
            ticksUntilNextSpawn--;
            return;
        }

        // Time to spawn
        List<Position> free = SpawnerHelper.freeFloorCells(board);
        if (free.isEmpty()) {
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
            board.registerCollectible(bonus);
        }

        // Quota decreases only on collection
        scheduleNextBonusSpawn();
    }

    /**
     * Tell the spawner that a bonus was collected.
     * Disables further waves when the quota reaches zero.
     */
    public void notifyBonusCollected() {
        if (!bonusEnabled) return;
        if (bonusRemaining > 0) bonusRemaining--;
        if (bonusRemaining <= 0) {
            bonusEnabled = false;
        }
    }

    /**
     * Chooses the next delay before a wave can appear.
     * Uses the min and max spawn ticks.
     */
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

    /**
     * Places regular rewards on free floor cells.
     *
     * @throws IllegalStateException if there are not enough free cells
     */
    public void spawnRegularRewards() {

        List<Position> free = SpawnerHelper.freeFloorCells(board);
        if (free.size() < GameConfig.regularRewardCount) {
            throw new IllegalStateException(
                    "Not enough free cells to place " + GameConfig.regularRewardCount + " regular rewards.");
        }

        Collections.shuffle(free, rng);
        for (int i = 0; i < GameConfig.regularRewardCount; i++) {
            Position p = free.get(i);
            RegularReward r = new RegularReward(p);
            board.registerCollectible(r);
        }
    }

    // ================================================================
    // Punishments: keep safe paths
    // ================================================================

    /**
     * Places punishments while keeping key paths reachable.
     * Start to exit stays reachable.
     * Each regular reward stays reachable from start.
     * Start and exit cells are never used.
     *
     */
    public void spawnPunishments() {
        List<Position> free = SpawnerHelper.freeFloorCells(board);
        Position start = board.start();
        Position exit = board.exit();

        // Do not allow punishments directly on start or exit
        free.remove(start);
        free.remove(exit);

        if (free.isEmpty()) return;

        Collections.shuffle(free, rng);

        List<Position> placed = new ArrayList<>();
        outer:
        for (Position candidate : free) {

            // Assume we place a punishment here
            Set<Position> blocked = new HashSet<>(placed);
            blocked.add(candidate);

            // 1) Start to exit must remain reachable
            if (!SpawnerHelper.canReach(board, start, exit, blocked)) {
                continue;
            }

            // 2) Each regular reward must remain reachable from start
            boolean ok = true;
            for (RegularReward rr : board.regularRewards()) {
                if (!SpawnerHelper.canReach(board, start, rr.position(), blocked)) {
                    ok = false;
                    break;
                }
            }
            if (!ok) continue;

            // Passed checks, place punishment
            Punishment p = new Punishment(candidate, GameConfig.punishmentPenalty);
            board.registerCollectible(p);
            placed.add(candidate);

            if (placed.size() >= GameConfig.numPunishments) break outer;
        }
    }

    // ================================================================
    // Enemies
    // ================================================================

    /**
     * Places moving enemies on free floor cells.
     * Allows enemies near start and exit but not on the first interior tiles.
     * Keeps a valid path from start to exit after each placement.
     *
     */
    public void spawnEnemies() {
        List<Position> free = SpawnerHelper.freeFloorCells(board);
        Position start = board.start();
        Position exit  = board.exit();

        // Never allow the tile directly inside Start or Exit
        Position blockStartFront = new Position(start.column() + 1, start.row());
        Position blockExitFront  = new Position(exit.column() - 1,  exit.row());
        free.remove(blockStartFront);
        free.remove(blockExitFront);

        if (free.isEmpty()) return;

        Collections.shuffle(free, rng);

        // Track placed enemies and validate reachability incrementally
        Set<Position> placedEnemies = new HashSet<>();

        int placed = 0;
        for (Position pos : free) {
            if (placed >= GameConfig.numEnemies) break;

            // Treat enemy cells as blocked for path checks
            Set<Position> blocked = new HashSet<>(placedEnemies);
            blocked.add(pos);

            if (!SpawnerHelper.canReach(board, start, exit, blocked)) {
                continue; // would block Start -> Exit path
            }

            // Safe to place
            MovingEnemy e = new MovingEnemy(pos, GameConfig.enemyMovePeriod);
            board.registerEnemy(e);
            placedEnemies.add(pos);
            placed++;
        }
    }
}
