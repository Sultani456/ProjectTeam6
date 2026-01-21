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
 * Tests win and lose rules inside GameController.
 */
final class GameControllerEndStatesTest {

    /**
     * Small helper object that holds the main game parts.
     */
    private record TestFixture(
            Board board,
            Scoreboard scoreboard,
            GameState state,
            GameController controller
    ) {}

    /**
     * Builds a small 5 by 5 board and controller for tests.
     */
    private static TestFixture newFixture(int requiredCount) {
        GameConfig.setBoardDimensions(5, 5);

        BoardGenerator gen = new BoardGenerator();
        BarrierOptions opts = new BarrierOptions(BarrierMode.NONE);
        Output out = gen.generate(opts);
        Board board = new Board(out);

        GameConfig.regularRewardCount = requiredCount;
        Scoreboard scoreboard = new Scoreboard();
        GameState state = new GameState(board.start(), List.of(), scoreboard);

        Spawner spawner = new Spawner(board);
        GamePanel view = new GamePanel(board, scoreboard, state);

        GameController controller = new GameController(board, spawner, scoreboard, state, view);
        return new TestFixture(board, scoreboard, state, controller);
    }

    /**
     * Calls the private evaluateEndStates method using reflection.
     */
    private static void invokeEvaluateEndStates(GameController controller) throws Exception {
        Method m = GameController.class.getDeclaredMethod("evaluateEndStates");
        m.setAccessible(true);
        m.invoke(controller);
    }

    /**
     * Game is won when all required rewards are collected and
     * the player stands on the exit tile.
     */
    @Test
    void winWhenAllRequiredCollectedAndAtExit() throws Exception {
        TestFixture fx = newFixture(1);

        fx.scoreboard.collectedRequired(10);

        Position exit = fx.board.exit();
        fx.board.player().setPosition(exit);

        assertEquals(GameState.Status.RUNNING, fx.state.status());

        invokeEvaluateEndStates(fx.controller);

        assertEquals(GameState.Status.WON, fx.state.status());
        assertNull(fx.board.explosionPos());
    }

    /**
     * Game is lost when the score becomes negative and
     * the explosion is placed at the player position.
     */
    @Test
    void loseWhenScoreBelowZeroSetsExplosionAndStatusLost() throws Exception {
        TestFixture fx = newFixture(1);

        assertTrue(fx.scoreboard.requiredRemaining() > 0);
        assertNotEquals(fx.board.exit(), fx.board.player().position());

        fx.scoreboard.penalize(-5);
        assertTrue(fx.scoreboard.score() < 0);

        invokeEvaluateEndStates(fx.controller);

        assertEquals(GameState.Status.LOST, fx.state.status());
        assertNotNull(fx.board.explosionPos());
        assertEquals(fx.board.player().position(), fx.board.explosionPos());
    }
}
