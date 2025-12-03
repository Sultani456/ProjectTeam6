package com.project.team6.model.board.generators;

import com.project.team6.model.board.Board;
import com.project.team6.model.board.Position;
import com.project.team6.model.board.generators.helpers.SpawnerHelper;
import com.project.team6.model.characters.enemies.MovingEnemy;
import com.project.team6.model.collectibles.rewards.*;
import com.project.team6.model.collectibles.Punishment;

import java.util.*;

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

    /**
     * Creates a spawner for a board.
     *
     * @param board      the target board
     * @param tickMillis tick duration in milliseconds
     * @throws NullPointerException if board is null
     */
    public Spawner(Board board, int tickMillis) {
        this(board, tickMillis, new Random());
    }

    /**
     * Creates a spawner for a board with an injected Random.
     * Useful for deterministic tests.
     *
     * @param board      the target board
     * @param tickMillis tick duration in milliseconds
     * @param rng        random source
     * @throws NullPointerException if board or rng is null
     */
    public Spawner(Board board, int tickMillis, Random rng) {
        this.board = Objects.requireNonNull(board);
        this.tickMillis = tickMillis;
        this.rng = Objects.requireNonNull(rng);
    }

    /**
     * Factory for tests that need deterministic spawning.
     *
     * @param board      the target board
     * @param tickMillis tick duration
     * @param seed       random seed
     * @return Spawner with fixed Random
     */
    public static Spawner withSeed(Board board, int tickMillis, long seed) {
        return new Spawner(board, tickMillis, new Random(seed));
    }

    // ================================================================
    // Bonus spawning (config + per-tick)
    // ================================================================

    private int secondsToTicks(int seconds) {
        if (seconds <= 0) return 0;
        double ticks = (seconds * 1000.0) / tickMillis;
        return Math.max(1, (int) Math.round(ticks));
    }

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

    public void onTick() {
        if (!bonusEnabled || bonusRemaining <= 0) return;
        if (board.hasActiveBonusRewards()) return;

        if (ticksUntilNextSpawn > 0) {
            ticksUntilNextSpawn--;
            return;
        }

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

        scheduleNextBonusSpawn();
    }

    public void notifyBonusCollected() {
        if (!bonusEnabled) return;
        if (bonusRemaining > 0) bonusRemaining--;
        if (bonusRemaining <= 0) {
            bonusEnabled = false;
        }
    }

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
            board.registerCollectible(r);
        }
    }

    // ================================================================
    // Punishments: keep safe paths
    // ================================================================

    public void spawnPunishments(int count, int penaltyPer) {
        if (count <= 0) return;

        List<Position> free = SpawnerHelper.freeFloorCells(board);
        Position start = board.start();
        Position exit = board.exit();

        free.remove(start);
        free.remove(exit);

        if (free.isEmpty()) return;

        Collections.shuffle(free, rng);

        List<Position> placed = new ArrayList<>();
        outer:
        for (Position candidate : free) {

            Set<Position> blocked = new HashSet<>(placed);
            blocked.add(candidate);

            if (!SpawnerHelper.canReach(board, start, exit, blocked)) {
                continue;
            }

            boolean ok = true;
            for (RegularReward rr : board.regularRewards()) {
                if (!SpawnerHelper.canReach(board, start, rr.position(), blocked)) {
                    ok = false;
                    break;
                }
            }
            if (!ok) continue;

            Punishment p = new Punishment(candidate, penaltyPer);
            board.registerCollectible(p);
            placed.add(candidate);

            if (placed.size() >= count) break outer;
        }
    }

    // ================================================================
    // Enemies
    // ================================================================

    public void spawnEnemies(int count, int movePeriod) {
        if (count <= 0) return;

        List<Position> free = SpawnerHelper.freeFloorCells(board);
        Position start = board.start();
        Position exit  = board.exit();

        Position blockStartFront = new Position(start.column() + 1, start.row());
        Position blockExitFront  = new Position(exit.column() - 1,  exit.row());
        free.remove(blockStartFront);
        free.remove(blockExitFront);

        if (free.isEmpty()) return;

        Collections.shuffle(free, rng);

        Set<Position> placedEnemies = new HashSet<>();

        int placed = 0;
        for (Position pos : free) {
            if (placed >= count) break;

            Set<Position> blocked = new HashSet<>(placedEnemies);
            blocked.add(pos);

            if (!SpawnerHelper.canReach(board, start, exit, blocked)) {
                continue;
            }

            MovingEnemy e = new MovingEnemy(pos, movePeriod);
            board.registerEnemy(e);
            placedEnemies.add(pos);
            placed++;
        }
    }
}
