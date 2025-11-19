package com.project.team6.model.board;

import com.project.team6.model.board.generators.*;
import com.project.team6.model.board.utilities.Direction;
import com.project.team6.model.board.utilities.MoveResult;
import com.project.team6.model.board.utilities.TickSummary;
import com.project.team6.model.characters.*;
import com.project.team6.model.characters.enemies.*;
import com.project.team6.model.collectibles.rewards.*;
import com.project.team6.model.collectibles.*;

import java.util.*;

/**
 * Main model for the grid world.
 * Owns cells, player, enemies, rewards, and punishments.
 * Contains only game logic and no Swing code.
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

    /** Optional explosion position for game over rendering. */
    private Position explosionPos;

    // -----------------------------------------------------------------
    // Construction
    // -----------------------------------------------------------------

    /**
     * Builds a board from a generated output.
     * Fills the grid with terrain and places the player at start.
     *
     * @param output generated terrain, start, and exit
     * @throws NullPointerException if output is null
     */
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

    /** @return number of rows in the board */
    public int rows() { return rows; }

    /** @return number of columns in the board */
    public int cols() { return cols; }

    /**
     * Returns a shallow copy of the grid array.
     * Inner rows are shared.
     *
     * @return cloned top level grid array
     */
    public Cell[][] grid() {return grid.clone();}

    /** @return start position */
    public Position start() { return start; }

    /** @return exit position */
    public Position exit()  { return exit;  }

    /** @return the single player object */
    public Player player() { return player; }

    /** @return position of explosion if set, otherwise null */
    public Position explosionPos() {return explosionPos;}

    /**
     * Gets the cell at a position.
     *
     * @param p position inside the board
     * @return the cell reference
     */
    public Cell cellAt(Position p) {
        return grid[p.y()][p.x()];
    }

    /**
     * Checks if a position is inside the grid bounds.
     *
     * @param p position to check
     * @return true if inside, false otherwise
     */
    public boolean isInBounds(Position p) {
        int x = p.x(), y = p.y();
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }

    /**
     * Computes Chebyshev distance between two positions.
     *
     * @param a first position
     * @param b second position
     * @return max of horizontal and vertical differences
     */
    public static int chebyshev(Position a, Position b) {
        return Math.max(Math.abs(a.x() - b.x()), Math.abs(a.y() - b.y()));
    }

    // -----------------------------------------------------------------
    // Registration helpers used by Spawner
    // -----------------------------------------------------------------

    /**
     * Adds a regular reward to the board and places it into its cell.
     *
     * @param r reward to register
     */
    public void registerRegularReward(RegularReward r) {
        regularRewards.add(r);
        cellAt(r.position()).setItem(r);
    }

    /**
     * Adds a bonus reward to the board and places it into its cell.
     *
     * @param b bonus to register
     */
    public void registerBonusReward(BonusReward b) {
        bonusRewards.add(b);
        cellAt(b.position()).setItem(b);
    }

    /**
     * Adds a punishment to the board and places it into its cell.
     *
     * @param p punishment to register
     */
    public void registerPunishment(Punishment p) {
        punishments.add(p);
        cellAt(p.position()).setItem(p);
    }

    /**
     * Adds an enemy to the board and places it into its cell.
     *
     * @param e enemy to register
     */
    public void registerEnemy(MovingEnemy e) {
        enemies.add(e);
        cellAt(e.position()).addOccupant(e);
    }

    /**
     * Exposes the list of regular rewards as an unmodifiable view.
     *
     * @return list of regular rewards
     */
    public List<RegularReward> regularRewards() {
        return Collections.unmodifiableList(regularRewards);
    }

    /**
     * Checks if any bonus rewards are currently active.
     *
     * @return true if at least one bonus is on the board
     */
    public boolean hasActiveBonusRewards() {
        return !bonusRewards.isEmpty();
    }

    // -----------------------------------------------------------------
    // Movement
    // -----------------------------------------------------------------

    /**
     * Tries to move a character one step in a given direction.
     * Validates bounds, terrain, and occupancy rules.
     *
     * @param who character to move
     * @param dir direction to move
     * @return move result such as MOVED, BLOCKED, or COLLISION
     * @throws NullPointerException if who or dir is null
     */
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

    /**
     * Advances the world by one tick.
     * Enemies move and bonus rewards age and expire.
     *
     * @param playerPos current player position
     * @return summary of what happened this tick
     */
    public TickSummary tick(Position playerPos) {
        boolean caught = false;

        // enemies
        for (MovingEnemy enemy : enemies) {
            enemy.tick(this, playerPos);
            if (enemy.position().equals(playerPos)) {
                caught = true;
            }
        }

        // bonus lifetime and expiry
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
     * Collects any item at a position and returns it.
     * Also removes it from the cell and internal lists.
     * Caller applies score effects and informs the spawner if needed.
     *
     * @param p position to collect from
     * @return present item wrapped in Optional or empty if none
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

    /**
     * Sets the explosion location for rendering after a loss.
     *
     * @param p position to mark for explosion
     */
    public void setExplosion(Position p) {
        this.explosionPos = p;
    }
}
