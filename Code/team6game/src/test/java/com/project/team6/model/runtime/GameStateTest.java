package com.project.team6.model.runtime;

import com.project.team6.model.board.Position;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Tests GameState status flags and scoreboard interaction. */
final class GameStateTest {

    @Test
    void startsInRunningStatus() {
        Scoreboard sb = new Scoreboard(0, 0);
        GameState gs = new GameState(new Position(0, 0), List.of(), sb);

        assertEquals(GameState.Status.RUNNING, gs.status());
    }

    @Test
    void setWonChangesStatusAndStopsTimer() throws InterruptedException {
        Scoreboard sb = new Scoreboard(0, 0);
        GameState gs = new GameState(new Position(0, 0), List.of(), sb);

        sb.start();
        Thread.sleep(30);                 // let some time pass

        gs.setWon();
        assertEquals(GameState.Status.WON, gs.status());

        long afterWon = sb.elapsed().toMillis();
        Thread.sleep(30);
        long afterWait = sb.elapsed().toMillis();

        // elapsed stays fixed once GameState calls scoreboard.stop()
        assertEquals(afterWon, afterWait);
    }

    @Test
    void setLostChangesStatusAndStopsTimer() throws InterruptedException {
        Scoreboard sb = new Scoreboard(0, 0);
        GameState gs = new GameState(new Position(0, 0), List.of(), sb);

        sb.start();
        Thread.sleep(30);

        gs.setLost();
        assertEquals(GameState.Status.LOST, gs.status());

        long afterLost = sb.elapsed().toMillis();
        Thread.sleep(30);
        long afterWait = sb.elapsed().toMillis();

        assertEquals(afterLost, afterWait);
    }
}
