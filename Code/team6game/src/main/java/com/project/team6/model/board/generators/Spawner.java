package com.project.team6.model.board.generators;

import com.project.team6.model.board.Board;
import com.project.team6.model.board.Position;
import com.project.team6.model.board.generators.helpers.SpawnerHelper;
import com.project.team6.model.characters.enemies.MovingEnemy;
import com.project.team6.model.collectibles.Punishment;
import com.project.team6.model.collectibles.rewards.BonusReward;
import com.project.team6.model.collectibles.rewards.RegularReward;

import java.util.*;

/**
 * Spawns collectibles and enemies onto a board.
 * One Spawner is used for one Board.
 * This class keeps the public API simple and delegates work to smaller components.
 */
public final class Spawner {

    /** The board that receives spawns. */
    private final Board board;

    /** Tick length in milliseconds. Must match the controller tick. */
    private final int tickMillis;

    /** Random source for placement and timing. */
    private final Random random;

    /** Shared reachability logic. */
    private final Reachability reachability;

    /** Spawning responsibilities split by feature. */
    private final BonusWaveSpawner bonusWaveSpawner;
    private final RegularRewardSpawner regularRewardSpawner;
    private final PunishmentSpawner punishmentSpawner;
    private final EnemySpawner enemySpawner;

    /**
     * Bonus configuration object.
     * This reduces long parameter lists and mistakes.
     */
    public static final class BonusConfig {
        private final int totalBonusesToAppear;
        private final int pointsPer;
        private final int spawnMinSec;
        private final int spawnMaxSec;
        private final int lifeMinSec;
        private final int lifeMaxSec;

        /**
         * Creates a bonus configuration.
         *
         * @param totalBonusesToAppear total number of bonuses that can appear
         * @param pointsPer points given per bonus
         * @param spawnMinSec minimum delay in seconds
         * @param spawnMaxSec maximum delay in seconds
         * @param lifeMinSec minimum lifetime in seconds
         * @param lifeMaxSec maximum lifetime in seconds
         */
        public BonusConfig(int totalBonusesToAppear,
                           int pointsPer,
                           int spawnMinSec,
                           int spawnMaxSec,
                           int lifeMinSec,
                           int lifeMaxSec) {
            this.totalBonusesToAppear = totalBonusesToAppear;
            this.pointsPer = pointsPer;
            this.spawnMinSec = spawnMinSec;
            this.spawnMaxSec = spawnMaxSec;
            this.lifeMinSec = lifeMinSec;
            this.lifeMaxSec = lifeMaxSec;
        }
    }

    /**
     * Creates a spawner for a board.
     * Uses a new Random for production.
     *
     * @param board the target board
     * @param tickMillis tick duration in milliseconds
     * @throws NullPointerException if board is null
     */
    public Spawner(Board board, int tickMillis) {
        this(board, tickMillis, new Random());
    }

    /**
     * Creates a spawner with an injected Random.
     * This helps deterministic tests.
     *
     * @param board the target board
     * @param tickMillis tick duration in milliseconds
     * @param random random source
     * @throws NullPointerException if board or random is null
     */
    public Spawner(Board board, int tickMillis, Random random) {
        this.board = Objects.requireNonNull(board);
        this.tickMillis = tickMillis;
        this.random = Objects.requireNonNull(random);

        this.reachability = new Reachability(this.board);

        this.bonusWaveSpawner = new BonusWaveSpawner(this.board, this.tickMillis, this.random);
        this.regularRewardSpawner = new RegularRewardSpawner(this.board, this.random);
        this.punishmentSpawner = new PunishmentSpawner(this.board, this.random, this.reachability);
        this.enemySpawner = new EnemySpawner(this.board, this.random, this.reachability);
    }

    /**
     * Creates a deterministic spawner for tests.
     *
     * @param board the target board
     * @param tickMillis tick duration in milliseconds
     * @param seed random seed
     * @return a spawner with a fixed Random
     */
    public static Spawner withSeed(Board board, int tickMillis, long seed) {
        return new Spawner(board, tickMillis, new Random(seed));
    }

    // ================================================================
    // Public API 
    // ================================================================

    /**
     * Configures timed bonus rewards by config object.
     *
     * @param config bonus configuration
     */
    public void spawnBonusRewards(BonusConfig config) {
        bonusWaveSpawner.spawnBonusRewards(config);
    }

    /**
     * Configures timed bonus rewards.
     * Quota is reduced when bonuses spawn, not when collected.
     * This prevents endless waves if the player ignores bonuses.
     */
    public void spawnBonusRewards(int totalBonusesToAppear,
                                  int pointsPer,
                                  int spawnMinSec,
                                  int spawnMaxSec,
                                  int lifeMinSec,
                                  int lifeMaxSec) {
        bonusWaveSpawner.spawnBonusRewards(totalBonusesToAppear, pointsPer, spawnMinSec, spawnMaxSec, lifeMinSec, lifeMaxSec);
    }

    /**
     * Runs once per controller tick.
     * Call this after the board tick.
     */
    public void onTick() {
        bonusWaveSpawner.onTick();
    }

    /**
     * Notifies the spawner that a bonus was collected.
     * This method is kept for compatibility.
     * Quota is tracked on spawn in this implementation.
     */
    public void notifyBonusCollected() {
        bonusWaveSpawner.notifyBonusCollected();
    }

    /**
     * Spawns regular rewards onto free floor cells.
     *
     * @param count number of rewards
     * @param pointsPer points per reward
     */
    public void spawnRegularRewards(int count, int pointsPer) {
        regularRewardSpawner.spawnRegularRewards(count, pointsPer);
    }

    /**
     * Spawns punishments while keeping paths safe.
     *
     * @param count number of punishments
     * @param penaltyPer penalty per punishment
     */
    public void spawnPunishments(int count, int penaltyPer) {
        punishmentSpawner.spawnPunishments(count, penaltyPer);
    }

    /**
     * Spawns moving enemies while keeping start-to-exit reachable.
     *
     * @param count number of enemies
     * @param movePeriod ticks between moves
     */
    public void spawnEnemies(int count, int movePeriod) {
        enemySpawner.spawnEnemies(count, movePeriod);
    }

    // ================================================================
    // Shared utilities
    // ================================================================

    /**
     * Partially shuffles a list so the first K elements are random.
     * This avoids shuffling the whole list when K is small.
     */
    private static void chooseFirstKRandomInPlace(List<Position> list, int count, Random random) {
        int n = list.size();
        int k = Math.min(count, n);
        for (int i = 0; i < k; i++) {
            int j = i + random.nextInt(n - i);
            if (i != j) Collections.swap(list, i, j);
        }
    }

    // ================================================================
    // Component: Reachability
    // ================================================================

    /**
     * Centralizes reachability checks.
     * This removes duplicated validation logic.
     */
    private static final class Reachability {
        private final Board board;

        private Reachability(Board board) {
            this.board = board;
        }

        private boolean canReach(Position from, Position to, Set<Position> blocked) {
            return SpawnerHelper.canReach(board, from, to, blocked);
        }

        private boolean canReachStartToExit(Position start, Position exit, Set<Position> blocked) {
            return canReach(start, exit, blocked);
        }

        private boolean canReachAllRegularRewards(Position start, Set<Position> blocked) {
            for (RegularReward rr : board.regularRewards()) {
                if (!canReach(start, rr.position(), blocked)) {
                    return false;
                }
            }
            return true;
        }
    }

    // ================================================================
    // Component: Bonus waves
    // ================================================================

    /**
     * Handles timed bonus waves.
     * It only runs onTick when bonus mode is enabled.
     */
    private static final class BonusWaveSpawner {
        private final Board board;
        private final int tickMillis;
        private final Random random;

        /** True when bonus waves are enabled. */
        private boolean bonusEnabled = false;

        /**
         * Number of bonus items that still need to APPEAR.
         * This number decreases when a wave spawns.
         */
        private int bonusRemaining = 0;

        /** Points given per bonus. */
        private int bonusPointsPer = 0;

        /** Min and max delay between waves in ticks. */
        private int spawnMinTicks = 0;
        private int spawnMaxTicks = 0;

        /** Min and max lifetime in ticks. */
        private int lifeMinTicks = 0;
        private int lifeMaxTicks = 0;

        /** Countdown until the next wave can spawn. */
        private int ticksUntilNextSpawn = -1;

        private BonusWaveSpawner(Board board, int tickMillis, Random random) {
            this.board = board;
            this.tickMillis = tickMillis;
            this.random = random;
        }

        private int secondsToTicks(int seconds) {
            if (seconds <= 0) return 0;
            double ticks = (seconds * 1000.0) / tickMillis;
            return Math.max(1, (int) Math.round(ticks));
        }

        private List<Position> freeFloorCells() {
            return SpawnerHelper.freeFloorCells(board);
        }

        private void scheduleNextBonusSpawn() {
            if (!bonusEnabled || bonusRemaining <= 0) {
                ticksUntilNextSpawn = -1;
                return;
            }
            int range = Math.max(0, spawnMaxTicks - spawnMinTicks);
            ticksUntilNextSpawn = spawnMinTicks + (range == 0 ? 0 : random.nextInt(range + 1));
        }

        private void disableBonuses() {
            bonusEnabled = false;
            ticksUntilNextSpawn = -1;
        }

        public void spawnBonusRewards(BonusConfig config) {
            Objects.requireNonNull(config);
            spawnBonusRewards(config.totalBonusesToAppear,
                    config.pointsPer,
                    config.spawnMinSec,
                    config.spawnMaxSec,
                    config.lifeMinSec,
                    config.lifeMaxSec);
        }

        public void spawnBonusRewards(int totalBonusesToAppear,
                                      int pointsPer,
                                      int spawnMinSec,
                                      int spawnMaxSec,
                                      int lifeMinSec,
                                      int lifeMaxSec) {
            if (totalBonusesToAppear <= 0) {
                disableBonuses();
                bonusRemaining = 0;
                return;
            }

            // Capacity check at setup time.
            int freeCells = freeFloorCells().size();
            if (totalBonusesToAppear > freeCells) {
                throw new IllegalArgumentException(
                        "totalBonusesToAppear (" + totalBonusesToAppear +
                                ") is larger than free floor cells (" + freeCells + ")");
            }

            bonusEnabled = true;
            bonusRemaining = totalBonusesToAppear;
            bonusPointsPer = pointsPer;

            spawnMinTicks = secondsToTicks(spawnMinSec);
            spawnMaxTicks = secondsToTicks(spawnMaxSec);
            lifeMinTicks = secondsToTicks(lifeMinSec);
            lifeMaxTicks = secondsToTicks(lifeMaxSec);

            if (spawnMaxTicks < spawnMinTicks) spawnMaxTicks = spawnMinTicks;
            if (lifeMaxTicks < lifeMinTicks) lifeMaxTicks = lifeMinTicks;

            scheduleNextBonusSpawn();
        }

        public void onTick() {
            if (!bonusEnabled || bonusRemaining <= 0) return;

            // Do not spawn while bonuses are still active.
            if (board.hasActiveBonusRewards()) return;

            if (ticksUntilNextSpawn > 0) {
                ticksUntilNextSpawn--;
                return;
            }

            List<Position> free = freeFloorCells();
            if (free.isEmpty()) {
                scheduleNextBonusSpawn();
                return;
            }

            int toSpawn = Math.min(bonusRemaining, free.size());
            chooseFirstKRandomInPlace(free, toSpawn, random);

            int lifeRange = Math.max(1, lifeMaxTicks - lifeMinTicks + 1);
            for (int i = 0; i < toSpawn; i++) {
                Position pos = free.get(i);
                int lifeTicks = lifeMinTicks + random.nextInt(lifeRange);
                BonusReward bonus = new BonusReward(pos, bonusPointsPer, lifeTicks);
                board.registerCollectible(bonus);
            }

            // Quota decreases when bonuses appear.
            bonusRemaining -= toSpawn;
            if (bonusRemaining <= 0) {
                disableBonuses();
                return;
            }

            scheduleNextBonusSpawn();
        }

        public void notifyBonusCollected() {
            // Kept for compatibility.
            // Quota tracking is based on spawn in this version.
        }
    }

    // ================================================================
    // Component: Regular rewards
    // ================================================================

    /**
     * Handles spawning regular rewards.
     */
    private static final class RegularRewardSpawner {
        private final Board board;
        private final Random random;

        private RegularRewardSpawner(Board board, Random random) {
            this.board = board;
            this.random = random;
        }

        private List<Position> freeFloorCells() {
            return SpawnerHelper.freeFloorCells(board);
        }

        public void spawnRegularRewards(int count, int pointsPer) {
            if (count <= 0) return;

            List<Position> free = freeFloorCells();
            if (free.size() < count) {
                throw new IllegalStateException(
                        "Not enough free cells to place " + count + " regular rewards.");
            }

            chooseFirstKRandomInPlace(free, count, random);
            for (int i = 0; i < count; i++) {
                Position p = free.get(i);
                RegularReward r = new RegularReward(p, pointsPer);
                board.registerCollectible(r);
            }
        }
    }

    // ================================================================
    // Component: Punishments
    // ================================================================

    /**
     * Handles spawning punishments while preserving paths.
     */
    private static final class PunishmentSpawner {
        private final Board board;
        private final Random random;
        private final Reachability reachability;

        private PunishmentSpawner(Board board, Random random, Reachability reachability) {
            this.board = board;
            this.random = random;
            this.reachability = reachability;
        }

        private List<Position> freeFloorCells() {
            return SpawnerHelper.freeFloorCells(board);
        }

        public void spawnPunishments(int count, int penaltyPer) {
            if (count <= 0) return;

            List<Position> free = freeFloorCells();
            Position start = board.start();
            Position exit = board.exit();

            // Do not place on start or exit.
            free.remove(start);
            free.remove(exit);

            if (free.isEmpty()) return;

            Collections.shuffle(free, random);

            List<Position> placed = new ArrayList<>();
            Set<Position> blocked = new HashSet<>();

            for (Position candidate : free) {
                blocked.clear();
                blocked.addAll(placed);
                blocked.add(candidate);

                if (!reachability.canReachStartToExit(start, exit, blocked)) continue;
                if (!reachability.canReachAllRegularRewards(start, blocked)) continue;

                Punishment p = new Punishment(candidate, penaltyPer);
                board.registerCollectible(p);
                placed.add(candidate);

                if (placed.size() >= count) break;
            }
        }
    }

    // ================================================================
    // Component: Enemies
    // ================================================================

    /**
     * Handles spawning enemies while preserving the start-to-exit path.
     */
    private static final class EnemySpawner {
        private final Board board;
        private final Random random;
        private final Reachability reachability;

        private EnemySpawner(Board board, Random random, Reachability reachability) {
            this.board = board;
            this.random = random;
            this.reachability = reachability;
        }

        private List<Position> freeFloorCells() {
            return SpawnerHelper.freeFloorCells(board);
        }

        public void spawnEnemies(int count, int movePeriod) {
            if (count <= 0) return;

            List<Position> free = freeFloorCells();
            Position start = board.start();
            Position exit = board.exit();

            // Never allow the tile directly inside Start or Exit.
            Position blockStartFront = new Position(start.column() + 1, start.row());
            Position blockExitFront = new Position(exit.column() - 1, exit.row());
            free.remove(blockStartFront);
            free.remove(blockExitFront);

            if (free.isEmpty()) return;

            Collections.shuffle(free, random);

            Set<Position> placedEnemies = new HashSet<>();
            Set<Position> blocked = new HashSet<>();

            int placed = 0;
            for (Position pos : free) {
                if (placed >= count) break;

                blocked.clear();
                blocked.addAll(placedEnemies);
                blocked.add(pos);

                if (!reachability.canReachStartToExit(start, exit, blocked)) continue;

                MovingEnemy e = new MovingEnemy(pos, movePeriod);
                board.registerEnemy(e);
                placedEnemies.add(pos);
                placed++;
            }
        }
    }
}
