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

    private final Board board;
    private final int tickMillis;

    /** Random source for placement and timing. */
    private final Random rng;

    // Components (higher cohesion)
    private final Reachability reachability;
    private final BonusWaveSpawner bonusWaveSpawner;
    private final RegularRewardSpawner regularRewardSpawner;
    private final PunishmentSpawner punishmentSpawner;
    private final EnemySpawner enemySpawner;

    // ============================
    // Bonus config object (from v3)
    // ============================

    public static final class BonusConfig {
        private final int totalBonusesToAppear;
        private final int pointsPer;
        private final int spawnMinSec;
        private final int spawnMaxSec;
        private final int lifeMinSec;
        private final int lifeMaxSec;

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

    // ================================================================
    // Constructor
    // ================================================================

    public Spawner(Board board, int tickMillis) {
        this(board, tickMillis, new Random());
    }

    public Spawner(Board board, int tickMillis, Random rng) {
        this.board = Objects.requireNonNull(board);
        this.tickMillis = tickMillis;
        this.rng = Objects.requireNonNull(rng);

        this.reachability = new Reachability(this.board);
        this.bonusWaveSpawner = new BonusWaveSpawner(this.board, this.tickMillis, this.rng);
        this.regularRewardSpawner = new RegularRewardSpawner(this.board, this.rng);
        this.punishmentSpawner = new PunishmentSpawner(this.board, this.rng, this.reachability);
        this.enemySpawner = new EnemySpawner(this.board, this.rng, this.reachability);
    }

    public static Spawner withSeed(Board board, int tickMillis, long seed) {
        return new Spawner(board, tickMillis, new Random(seed));
    }

    // ================================================================
    // Public API (unchanged signatures)
    // ================================================================

    public void spawnBonusRewards(BonusConfig config) {
        bonusWaveSpawner.spawnBonusRewards(config);
    }

    public void spawnBonusRewards(int totalBonusesToAppear,
                                  int pointsPer,
                                  int spawnMinSec,
                                  int spawnMaxSec,
                                  int lifeMinSec,
                                  int lifeMaxSec) {
        bonusWaveSpawner.spawnBonusRewards(totalBonusesToAppear, pointsPer, spawnMinSec, spawnMaxSec, lifeMinSec, lifeMaxSec);
    }

    public void onTick() {
        bonusWaveSpawner.onTick();
    }

    public void notifyBonusCollected() {
        bonusWaveSpawner.notifyBonusCollected();
    }

    public void spawnRegularRewards(int count, int pointsPer) {
        regularRewardSpawner.spawnRegularRewards(count, pointsPer);
    }

    public void spawnPunishments(int count, int penaltyPer) {
        punishmentSpawner.spawnPunishments(count, penaltyPer);
    }

    public void spawnEnemies(int count, int movePeriod) {
        enemySpawner.spawnEnemies(count, movePeriod);
    }

    // ================================================================
    // Component: Reachability checks
    // ================================================================

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
    // Component: Bonus wave spawning
    // ================================================================

    private static final class BonusWaveSpawner {
        private final Board board;
        private final int tickMillis;
        private final Random rng;

        private boolean bonusEnabled = false;

        /**
         * Number of bonus items that still need to APPEAR.
         * Decreases when a wave is spawned.
         */
        private int bonusRemaining = 0;

        private int bonusPointsPer = 0;

        private int spawnMinTicks = 0;
        private int spawnMaxTicks = 0;

        private int lifeMinTicks = 0;
        private int lifeMaxTicks = 0;

        private int ticksUntilNextSpawn = -1;

        private BonusWaveSpawner(Board board, int tickMillis, Random rng) {
            this.board = board;
            this.tickMillis = tickMillis;
            this.rng = rng;
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
            ticksUntilNextSpawn = spawnMinTicks + (range == 0 ? 0 : rng.nextInt(range + 1));
        }

        private static void chooseFirstKRandomInPlace(List<Position> list, int count, Random rng) {
            int n = list.size();
            int k = Math.min(count, n);
            for (int i = 0; i < k; i++) {
                int j = i + rng.nextInt(n - i);
                if (i != j) Collections.swap(list, i, j);
            }
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
                bonusEnabled = false;
                bonusRemaining = 0;
                return;
            }

            int freeCells = freeFloorCells().size();
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

            List<Position> free = freeFloorCells();
            if (free.isEmpty()) {
                scheduleNextBonusSpawn();
                return;
            }

            int toSpawn = Math.min(bonusRemaining, free.size());
            chooseFirstKRandomInPlace(free, toSpawn, rng);

            int lifeRange = Math.max(1, lifeMaxTicks - lifeMinTicks + 1);
            for (int i = 0; i < toSpawn; i++) {
                Position pos = free.get(i);
                int lifeTicks = lifeMinTicks + rng.nextInt(lifeRange);
                BonusReward bonus = new BonusReward(pos, bonusPointsPer, lifeTicks);
                board.registerCollectible(bonus);
            }

            bonusRemaining -= toSpawn;
            if (bonusRemaining <= 0) {
                bonusEnabled = false;
                ticksUntilNextSpawn = -1;
                return;
            }

            scheduleNextBonusSpawn();
        }

        public void notifyBonusCollected() {
            // no-op by design (quota decreases on spawn)
        }
    }

    // ================================================================
    // Component: Regular rewards
    // ================================================================

    private static final class RegularRewardSpawner {
        private final Board board;
        private final Random rng;

        private RegularRewardSpawner(Board board, Random rng) {
            this.board = board;
            this.rng = rng;
        }

        private List<Position> freeFloorCells() {
            return SpawnerHelper.freeFloorCells(board);
        }

        private static void chooseFirstKRandomInPlace(List<Position> list, int count, Random rng) {
            int n = list.size();
            int k = Math.min(count, n);
            for (int i = 0; i < k; i++) {
                int j = i + rng.nextInt(n - i);
                if (i != j) Collections.swap(list, i, j);
            }
        }

        public void spawnRegularRewards(int count, int pointsPer) {
            if (count <= 0) return;

            List<Position> free = freeFloorCells();
            if (free.size() < count) {
                throw new IllegalStateException(
                        "Not enough free cells to place " + count + " regular rewards.");
            }

            chooseFirstKRandomInPlace(free, count, rng);
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

    private static final class PunishmentSpawner {
        private final Board board;
        private final Random rng;
        private final Reachability reachability;

        private PunishmentSpawner(Board board, Random rng, Reachability reachability) {
            this.board = board;
            this.rng = rng;
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

            free.remove(start);
            free.remove(exit);

            if (free.isEmpty()) return;

            Collections.shuffle(free, rng);

            List<Position> placed = new ArrayList<>();
            Set<Position> blocked = new HashSet<>();

            outer:
            for (Position candidate : free) {
                blocked.clear();
                blocked.addAll(placed);
                blocked.add(candidate);

                if (!reachability.canReachStartToExit(start, exit, blocked)) {
                    continue;
                }
                if (!reachability.canReachAllRegularRewards(start, blocked)) {
                    continue;
                }

                Punishment p = new Punishment(candidate, penaltyPer);
                board.registerCollectible(p);
                placed.add(candidate);

                if (placed.size() >= count) break outer;
            }
        }
    }

    // ================================================================
    // Component: Enemies
    // ================================================================

    private static final class EnemySpawner {
        private final Board board;
        private final Random rng;
        private final Reachability reachability;

        private EnemySpawner(Board board, Random rng, Reachability reachability) {
            this.board = board;
            this.rng = rng;
            this.reachability = reachability;
        }

        private List<Position> freeFloorCells() {
            return SpawnerHelper.freeFloorCells(board);
        }

        public void spawnEnemies(int count, int movePeriod) {
            if (count <= 0) return;

            List<Position> free = freeFloorCells();
            Position start = board.start();
            Position exit  = board.exit();

            Position blockStartFront = new Position(start.column() + 1, start.row());
            Position blockExitFront  = new Position(exit.column() - 1,  exit.row());
            free.remove(blockStartFront);
            free.remove(blockExitFront);

            if (free.isEmpty()) return;

            Collections.shuffle(free, rng);

            Set<Position> placedEnemies = new HashSet<>();
            Set<Position> blocked = new HashSet<>();

            int placed = 0;
            for (Position pos : free) {
                if (placed >= count) break;

                blocked.clear();
                blocked.addAll(placedEnemies);
                blocked.add(pos);

                if (!reachability.canReachStartToExit(start, exit, blocked)) {
                    continue;
                }

                MovingEnemy e = new MovingEnemy(pos, movePeriod);
                board.registerEnemy(e);
                placedEnemies.add(pos);
                placed++;
            }
        }
    }
}
