package com.project.team6.ui;

import com.project.team6.controller.*;
import com.project.team6.model.board.*;
import com.project.team6.model.board.generators.*;
import com.project.team6.model.board.generators.barrierProperties.*;
import com.project.team6.model.runtime.*;

import javax.swing.*;
import java.util.*;

/**
 * Sets up and runs the game.
 * Builds the board, spawns items and enemies, and opens the window.
 */
public final class App {

    /**
     * Entry point.
     * Creates models, view, and controller, then starts the game.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            /** –––––––––––––––––––– BOARD GENERATION –––––––––––––––––––– */
            BoardGenerator gen = new BoardGenerator();

            /** Option A: NONE – No barriers, with given rows/cols from client*/
//            BarrierOptions opts = new BarrierOptions(BarrierMode.NONE);

            /** Option B: PROVIDED – populate barriers from given barrierList, with given rows/cols from client */
//            GameConfig.addToBarrierList();
//            BarrierOptions opts = new BarrierOptions(BarrierMode.PROVIDED);

            /** Option C: TEXT – populate barriers from textfile, with given rows/cols from textfile */
//            GameConfig.setBoardDimensions(0,0);
//            GameConfig.setMapResource("maps/level1.txt");
//            BarrierOptions opts = new BarrierOptions(BarrierMode.TEXT);

            /** Option D: RANDOM – randomly put barriers, with given rows/cols from client */
            BarrierOptions opts = new BarrierOptions(BarrierMode.RANDOM);

            BoardGenerator.Output output = gen.generate(opts);
            Board board = new Board(output);

            /** –––––––––––––––––––– SPAWNING –––––––––––––––––––– */
            Spawner spawner = new Spawner(board);

            spawner.spawnRegularRewards();
            spawner.spawnPunishments();
            spawner.spawnEnemies();
            spawner.spawnBonusRewards();

            /** –––––––––––––––––––– GAME PANEL RENDERING –––––––––––––––––––– */
            // Score/time model
            Scoreboard scoreboard = new Scoreboard();
            GameState state = new GameState(board.start(), List.of(), scoreboard);

            // --- View + Window
            GamePanel panel = new GamePanel(board, scoreboard, state);
//            panel.setRenderMode(GamePanel.RenderMode.SYMBOLS);      // comment out if want IMAGES
//
            GameFrame frame = new GameFrame(panel);
            frame.setVisible(true);

            /** –––––––––––––––––––– GAMEPLAY CONTROLLER –––––––––––––––––––– */
            // --- Controller
            GameController controller = new GameController(board, spawner, scoreboard, state, panel);
            controller.start();
        });
    }
}
