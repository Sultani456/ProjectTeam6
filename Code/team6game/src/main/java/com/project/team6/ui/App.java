package com.project.team6.ui;

import com.project.team6.controller.GameController;
import com.project.team6.model.boardUtilities.*;
import com.project.team6.model.generators.BoardGenerator;
import com.project.team6.model.runtime.*;

import javax.swing.*;
import java.util.List;

/**
 * Wires MVC together:
 * - Builds the Board (no external text files)
 * - Spawns items and enemies
 * - Creates Scoreboard, GameState
 * - Creates GamePanel and GameController
 * - Shows the GameFrame
 */
public final class App {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // --- Model setup ---
            var opts = new BoardGenerator.Options(
                    /*rows*/ 17, /*cols*/ 25,
                    new Position(0, 8),              // Start on edge
                    new Position(24, 8),             // Exit on opposite edge
                    BoardGenerator.InternalBarrierMode.RANDOM_MAZE,
                    List.of(),                       // not used in RANDOM_MAZE
                    42L                              // seed for reproducibility
            );
            Board board = new Board(opts);

            // Spawn items/enemies directly via Board (no factories)
            int regularCount = 25;
            board.spawnRegularRewards(regularCount, /*amount*/ 5);
            board.spawnBonusRewards(6, /*amount*/ 10);      // optional
            board.spawnPunishments(10, /*penalty*/ -7);
            board.spawnEnemies(4);

            // Score/time model
            Scoreboard scoreboard = new Scoreboard(/*initialScore*/ 0, /*requiredCount*/ regularCount);
            GameState state = new GameState(board.start(), /*enemyStarts*/ List.of(), scoreboard);

            // --- View ---
            GamePanel panel = new GamePanel(board, scoreboard, state);
            GameFrame frame = new GameFrame(panel);
            frame.setVisible(true);

            // --- Controller ---
            GameController controller = new GameController(board, scoreboard, state, panel);
            controller.start();
        });
    }
}
