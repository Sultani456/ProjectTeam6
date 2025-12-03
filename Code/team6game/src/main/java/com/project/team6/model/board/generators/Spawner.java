package com.project.team6.model.board.generators;

import com.project.team6.model.board.Board;
import com.project.team6.model.board.Position;
import com.project.team6.model.board.generators.helpers.SpawnerHelper;
import com.project.team6.model.characters.enemies.MovingEnemy;
import com.project.team6.model.collectibles.rewards.*;
import com.project.team6.model.collectibles.Punishment;

import java.util.*;

public final class Spawner {

    private final Board board;
    private final int tickMillis;

    /** Random source for placement and timing. */
    private final Random rng;

    // Bonus-reward configuration
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

    public Spawner(Board board, int tickMillis) {
        this(board, tickMillis, new Random());
    }

    public Spawner(Board board, int tickMillis, Random rng) {
        this.board = Objects.requireNonNull(board);
        this.tickMillis = tickMillis;
        this.rng = Objects.requireNonNull(rng);
    }

    public static Spawner withSeed(Board board, int tickMillis, long seed) {
        return new Spawner(board, tickMillis, new Random(seed));
    }

    private int secondsToTicks(int seconds) {
        if (seconds <= 0) return 0;
        double ticks = (seconds * 1000.0) / tickMillis;
        return Math.max(1, (int) Math.round(ticks));
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

    public void spawnRegularRewards(int count, int pointsPer) {
        if (count <= 0) return;

        List<Position> free = SpawnerHelper.freeFloorCells(board);
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
