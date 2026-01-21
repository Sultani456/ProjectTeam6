package com.project.team6.model.runtime;

import com.project.team6.model.board.Position;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests simple GameState behavior.
 */
final class GameStateTest {

    @Test
    void startsInRunningStatus() {
        Scoreboard scoreboard = new Scoreboard();
        GameState state = new GameState(new Position(0, 0), List.of(), scoreboard);

        assertEquals(GameState.Status.RUNNING, state.status());
    }

    @Test
    void setWonUpdatesStatus() {
        Scoreboard scoreboard = new Scoreboard();
        GameState state = new GameState(new Position(0, 0), List.of(), scoreboard);

        state.setWon();

        assertEquals(GameState.Status.WON, state.status());
    }

    @Test
    void setLostUpdatesStatus() {
        Scoreboard scoreboard = new Scoreboard();
        GameState state = new GameState(new Position(0, 0), List.of(), scoreboard);

        state.setLost();

        assertEquals(GameState.Status.LOST, state.status());
    }

    @Test
    void canUpdatePlayerAndEnemiesSnapshots() {
        Scoreboard scoreboard = new Scoreboard();
        GameState state = new GameState(new Position(0, 0), List.of(), scoreboard);

        Position newPlayerPos = new Position(2, 3);
        state.setPlayer(newPlayerPos);
        assertEquals(newPlayerPos, state.player());

        state.setEnemies(List.of(new Position(1, 1)));
        assertEquals(1, state.enemies().size());
    }

    @Test
    void scoreboardReferenceIsStored() {
        Scoreboard scoreboard = new Scoreboard();
        GameState state = new GameState(new Position(0, 0), List.of(), scoreboard);

        assertSame(scoreboard, state.scoreboard());
    }
}
