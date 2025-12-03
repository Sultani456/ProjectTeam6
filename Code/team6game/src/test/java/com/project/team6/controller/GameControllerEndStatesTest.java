package com.project.team6.controller;

import com.project.team6.model.board.Board;
import com.project.team6.model.board.Position;
import com.project.team6.model.board.generators.BoardGenerator;
import com.project.team6.model.board.generators.BoardGenerator.Output;
import com.project.team6.model.board.generators.Spawner;
import com.project.team6.model.board.generators.barrierProperties.BarrierMode;
import com.project.team6.model.board.generators.barrierProperties.BarrierOptions;
import com.project.team6.model.runtime.GameState;
import com.project.team6.model.runtime.Scoreboard;
import com.project.team6.ui.GamePanel;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests win and lose rules in GameController.
 */
final class GameControllerEndStatesTest {

    /**
     * Holds the main objects used in these tests.
     */
    private record TestFixture(
            Board board,
            Scoreboard scoreboard,
            GameState state,
            GameController controller
    ) {}

    /**
     * Builds a small board and controller for tests.
     */
    private static TestFixture newFixture(int requiredCount, int initialScore) {
        BoardGenerator gen = new BoardGenerator();
        BarrierOptions opts = new BarrierOptions(
                5, 5,
                BarrierMode.NONE,
                null,
                null
        );
        Output out = gen.generate(opts, 0.0);
        Board board = new Board(out);

        Scoreboard scoreboard = new Scoreboard(initialScore, requiredCount);
        GameState state = new GameState(board.start(), List.of(), scoreboard);

        Spawner spawner = new Spawner(board, GameConfig.DEFAULT_TICK_MS);
        GamePanel view = new GamePanel(board, scoreboard, state);

        GameController controller = new GameController(board, spawner, scoreboard, state, view);
        return new TestFixture(board, scoreboard, state, controller);
    }

    /**
     * Calls GameController.evaluateEndStates using reflection.
     */
    private static void invokeEvaluateEndStates(GameController controller) throws Exception {
        Method m = GameController.class.getDeclaredMethod("evaluateEndStates");
        m.setAccessible(true);
        m.invoke(controller);
    }

    /**
     * The game is won when all required rewards are collected
     * and the player is standing on the exit.
     */
    @Test
    void winWhenAllRequiredCollectedAndAtExit() throws Exception {
        TestFixture fx = newFixture(1, 0);

        // Collect the single required reward
        fx.scoreboard.collectedRequired(10);

        // Move the player to the exit tile
        Position exit = fx.board.exit();
        fx.board.player().setPosition(exit);

        assertEquals(GameState.Status.RUNNING, fx.state.status());

        invokeEvaluateEndStates(fx.controller);

        assertEquals(GameState.Status.WON, fx.state.status());
        assertNull(fx.board.explosionPos());
    }

    /**
     * The game is lost when the score becomes negative.
     * An explosion is placed at the player position.
     */
    @Test
    void loseWhenScoreBelowZeroSetsExplosionAndStatusLost() throws Exception {
        TestFixture fx = newFixture(1, 0);

        // Still have a required reward and not at exit, so no accidental win
        assertTrue(fx.scoreboard.requiredRemaining() > 0);
        assertNotEquals(fx.board.exit(), fx.board.player().position());

        // Push score below zero
        fx.scoreboard.penalize(-5);
        assertTrue(fx.scoreboard.score() < 0);

        invokeEvaluateEndStates(fx.controller);

        assertEquals(GameState.Status.LOST, fx.state.status());
        assertNotNull(fx.board.explosionPos());
        assertEquals(fx.board.player().position(), fx.board.explosionPos());
    }
}
