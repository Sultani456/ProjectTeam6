package com.project.team6.model.characters.enemies;

import com.project.team6.model.board.Board;
import com.project.team6.model.board.Cell;
import com.project.team6.model.board.Position;
import com.project.team6.model.board.utilities.Direction;
import com.project.team6.model.board.utilities.MoveResult;
import com.project.team6.model.characters.CharacterObject;

/**
 * Base enemy that moves at most one tile per tick.
 * Subclasses define the decision rule.
 */
public abstract class Enemy extends CharacterObject {

    /**
     * Provides a narrow view of the world for enemy AI.
     * Hides the full Board API.
     */
    public interface EnemyContext {
        /**
         * Current enemy position.
         */
        Position enemyPosition();

        /**
         * Current player position.
         */
        Position playerPosition();

        /**
         * Returns true if the enemy can attempt to step in the given direction.
         */
        boolean canStep(Direction direction);

        /**
         * Tries to move the enemy in the given direction.
         *
         * @param direction direction to move
         * @return move result from the board
         */
        MoveResult move(Direction direction);
    }

    /**
     * Adapter that wraps a Board and exposes only EnemyContext.
     */
    private static final class BoardEnemyContext implements EnemyContext {
        private final Board board;
        private final Enemy enemy;
        private final Position playerPos;

        BoardEnemyContext(Board board, Enemy enemy, Position playerPos) {
            this.board = board;
            this.enemy = enemy;
            this.playerPos = playerPos;
        }

        @Override
        public Position enemyPosition() {
            return enemy.position();
        }

        @Override
        public Position playerPosition() {
            return playerPos;
        }

        @Override
        public boolean canStep(Direction direction) {
            Position from = enemy.position();
            Position to = new Position(
                    from.column() + direction.d_column,
                    from.row() + direction.d_row
            );

            if (!board.isInBounds(to)) {
                return false;
            }

            Cell cell = board.cellAt(to);
            // Keep same logic as before: walkable terrain check used in MovingEnemy
            return cell.isWalkableTerrain();
        }

        @Override
        public MoveResult move(Direction direction) {
            return board.step(enemy, direction);
        }
    }

    /**
     * Creates an enemy at a starting position.
     *
     * @param position initial location
     */
    protected Enemy(Position position) {
        super(position);
    }

    /**
     * Runs once per tick from game code.
     * Wraps the board in an EnemyContext and delegates to context-based tick.
     *
     * @param board     current board
     * @param playerPos player position
     */
    public final void tick(Board board, Position playerPos) {
        EnemyContext ctx = new BoardEnemyContext(board, this, playerPos);
        tick(ctx);
    }

    /**
     * Context-based tick used by subclasses.
     *
     * @param ctx enemy context
     */
    protected void tick(EnemyContext ctx) {
        Direction d = decide(ctx);
        if (d == null) {
            return; // Stays still this tick
        }

        MoveResult result = ctx.move(d);
        onPostStep(ctx, result);
    }

    /**
     * Legacy helper for code/tests that still call decide(board, playerPos).
     * Wraps the Board into an EnemyContext and forwards to the new API.
     */
    public final Direction decide(Board board, Position playerPos) {
        EnemyContext ctx = new BoardEnemyContext(board, this, playerPos);
        return decide(ctx);
    }

    /**
     * Hook for reacting to a move attempt result with context.
     * Subclasses may override.
     *
     * @param ctx    enemy context
     * @param result move result
     */
    protected void onPostStep(EnemyContext ctx, MoveResult result) {
        // default: no-op
    }

    /**
     * Chooses a movement direction.
     * May return null to stay still.
     *
     * @param ctx enemy context
     * @return direction to move or null
     */
    protected abstract Direction decide(EnemyContext ctx);

    /**
     * Enemies can walk on normal terrain but not START/EXIT or occupied tiles.
     */
    @Override
    public boolean canEnter(Cell cell) {
        if (!cell.isWalkableTerrain()) return false;
        if (cell.terrain() == Cell.Terrain.START || cell.terrain() == Cell.Terrain.EXIT) return false;
        return cell.enemy() == null;
    }

    /** ASCII symbol for enemies. */
    @Override
    public char symbol() {
        return 'B';
    }
}
