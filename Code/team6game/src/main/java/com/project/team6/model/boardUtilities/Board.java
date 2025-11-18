package com.project.team6.model.boardUtilities;

import com.project.team6.model.boardUtilities.generators.*;
import com.project.team6.model.characters.*;
import com.project.team6.model.characters.enemies.*;
import com.project.team6.model.collectibles.rewards.*;
import com.project.team6.model.collectibles.*;

import java.util.*;

/**
 * Board is the authoritative model for the grid.
 * - Owns Cells, Player, Enemies, Rewards, Punishments.
 * - Knows nothing about Swing; pure game logic.
 */
public final class Board {

    private final int rows;
    private final int cols;
    private final Cell[][] grid;

    private final Position start;
    private final Position exit;

    private final Player player;
    private final List<MovingEnemy> enemies = new ArrayList<>();
    private final List<RegularReward> regularRewards = new ArrayList<>();
    private final List<Punishment> punishments = new ArrayList<>();
    private final List<BonusReward> bonusRewards = new ArrayList<>();

    /** Optional explosion position for game-over rendering. */
    private Position explosionPos;

    // -----------------------------------------------------------------
    // Construction
    // -----------------------------------------------------------------

    public Board(BoardGenerator.Output output) {
        Objects.requireNonNull(output);
        this.rows = output.rows();
        this.cols = output.cols();
        this.start = output.start();
        this.exit = output.exit();

        this.grid = new Cell[rows][cols];
        Cell.Terrain[][] terrain = output.terrain();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                grid[y][x] = new Cell(terrain[y][x]);
            }
        }

        // Create Player at start and register into the Cell.
        this.player = new Player(start);
        cellAt(start).addOccupant(player);
    }

    // -----------------------------------------------------------------
    // Basic accessors
    // -----------------------------------------------------------------

    public int rows() { return rows; }
    public int cols() { return cols; }
    public Cell[][] grid() {return grid.clone();}

    public Position start() { return start; }
    public Position exit()  { return exit;  }

    public Player player() { return player; }

    public Position explosionPos() {return explosionPos;}

    public Cell cellAt(Position p) {
        return grid[p.y()][p.x()];
    }

    public boolean isInBounds(Position p) {
        int x = p.x(), y = p.y();
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }

    public static int chebyshev(Position a, Position b) {
        return Math.max(Math.abs(a.x() - b.x()), Math.abs(a.y() - b.y()));
    }

    // -----------------------------------------------------------------
    // Registration helpers used by Spawner
    // -----------------------------------------------------------------

    public void registerRegularReward(RegularReward r) {
        regularRewards.add(r);
        cellAt(r.position()).setItem(r);
    }

    public void registerBonusReward(BonusReward b) {
        bonusRewards.add(b);
        cellAt(b.position()).setItem(b);
    }

    public void registerPunishment(Punishment p) {
        punishments.add(p);
        cellAt(p.position()).setItem(p);
    }

    public void registerEnemy(MovingEnemy e) {
        enemies.add(e);
        cellAt(e.position()).addOccupant(e);
    }

    public List<RegularReward> regularRewards() {
        return Collections.unmodifiableList(regularRewards);
    }

    // Used by Spawner / controller
    public boolean hasActiveBonusRewards() {
        return !bonusRewards.isEmpty();
    }

    // -----------------------------------------------------------------
    // Movement
    // -----------------------------------------------------------------

    public MoveResult step(CharacterObject who, Direction dir) {
        Objects.requireNonNull(who);
        Objects.requireNonNull(dir);

        Position from = who.position();
        Position to   = new Position(from.x() + dir.dx, from.y() + dir.dy);

        if (!isInBounds(to)) {
            return MoveResult.BLOCKED;
        }

        Cell target = cellAt(to);
        if (!target.isWalkableTerrain()) {
            return MoveResult.BLOCKED;
        }
        if (!target.isEnterableFor(who)) {
            return MoveResult.BLOCKED;
        }

        boolean collision = target.hasEnemy();

        // Move occupant between cells
        cellAt(from).removeOccupant(who);
        target.addOccupant(who);
        who.setPosition(to);

        return collision ? MoveResult.COLLISION : MoveResult.MOVED;
    }

    // -----------------------------------------------------------------
    // Tick: enemies + bonus lifetime
    // -----------------------------------------------------------------

    public static final class TickSummary {
        private final boolean playerCaught;

        public TickSummary(boolean playerCaught) {
            this.playerCaught = playerCaught;
        }

        public boolean playerCaught() {
            return playerCaught;
        }
    }

    /**
     * Advance the world by one tick:
     *  - Enemies move
     *  - Bonus rewards age and expire
     */
    public TickSummary tick(Position playerPos) {
        boolean caught = false;

        // --- enemies ---
        for (MovingEnemy enemy : enemies) {
            enemy.tick(this, playerPos);
            if (enemy.position().equals(playerPos)) {
                caught = true;
            }
        }

        // --- bonus lifetime / expiry ---
        Iterator<BonusReward> it = bonusRewards.iterator();
        while (it.hasNext()) {
            BonusReward b = it.next();
            if (!b.onTickAndAlive()) {
                // remove from board
                cellAt(b.position()).setItem(null);
                it.remove();
            }
        }

        return new TickSummary(caught);
    }

    // -----------------------------------------------------------------
    // Collecting / explosion
    // -----------------------------------------------------------------

    /**
     * Collect any item at the given position, returning it if present.
     * Caller is responsible for applying its effect to the Scoreboard
     * and telling Spawner that a bonus was collected (if applicable).
     */
    public Optional<CollectibleObject> collectAt(Position p) {
        Cell c = cellAt(p);
        CollectibleObject item = c.item();
        if (item == null) return Optional.empty();

        c.setItem(null);

        if (item instanceof RegularReward rr) {
            regularRewards.remove(rr);
        } else if (item instanceof BonusReward br) {
            bonusRewards.remove(br);
        } else if (item instanceof Punishment pu) {
            punishments.remove(pu);
        }

        return Optional.of(item);
    }

    public void setExplosion(Position p) {
        this.explosionPos = p;
    }
}

