package com.project.team6.ui;

import com.project.team6.controller.*;
import com.project.team6.model.boardUtilities.*;
import com.project.team6.model.boardUtilities.generators.*;
import com.project.team6.model.boardUtilities.generators.barrierProperties.*;
import com.project.team6.model.runtime.*;

import javax.swing.*;
import java.util.*;

/**
 * Wires MVC together:
 * - Builds the Board (no external text files) via BoardGenerator
 * - Spawns items and enemies
 * - Creates Scoreboard, GameState
 * - Creates GamePanel and GameController
 * - Creates the view + controller
 * - Shows the GameFrame
 */
public final class App {

    public static void main(String[] args) {
        /** –––––––––––––––––––– GAME CONTROLS –––––––––––––––––––– */
        int rows = 24;
        int cols = 24;

        int regularRewardCount = 15;
        int regularPoints = 10;

        int bonusRewardCount = 5;
        int bonusPoints = 20;

        int spawnAt_MinSec = 3;
        int spawnAt_MaxSec = 6;
        int lifeMinSec = 3;
        int lifeMaxSec = 6;

        int numPunishments = 8;
        int punishmentPenalty = -5;

        int numEnemies = 3;
        int enemyMovePeriod = 10;

        // Barrier density for RANDOM barrier mode (fraction of interior cells)
        double boardBarrierPercentage = 0.4;        // 0.1 to 0.4 are best



        SwingUtilities.invokeLater(() -> {
            /** –––––––––––––––––––– BOARD GENERATION –––––––––––––––––––– */
            BoardGenerator gen = new BoardGenerator();


            /** Option A: NONE – No barriers, with given rows/cols from client*/
//            BarrierOptions opts = new BarrierOptions(rows, cols, BarrierMode.NONE, null, null);

            /** Option B: PROVIDED – populate barriers from given barrierList, with given rows/cols from client */
//            List<Position> barrierList = BoardGenerator.barrierList();
//            BarrierOptions opts = new BarrierOptions(rows, cols, BarrierMode.PROVIDED, barrierList, null);

            /** Option C: TEXT – populate barriers from textfile, with given rows/cols from textfile */
//            BarrierOptions opts = new BarrierOptions(0,0, BarrierMode.TEXT, null, "maps/level1.txt");

            /** Option D: RANDOM – randomly put barriers, with given rows/cols from client */
            BarrierOptions opts = new BarrierOptions(rows, cols, BarrierMode.RANDOM, null, null);

            BoardGenerator.Output output = gen.generate(opts, boardBarrierPercentage);
            Board board = new Board(output);

            /** –––––––––––––––––––– SPAWNING –––––––––––––––––––– */
            Spawner spawner = new Spawner(board, GameController.DEFAULT_TICK_MS);

            spawner.spawnBonusRewards(bonusRewardCount, bonusPoints, spawnAt_MinSec, spawnAt_MaxSec, lifeMinSec, lifeMaxSec);
            spawner.spawnRegularRewards(regularRewardCount, regularPoints);
            spawner.spawnPunishments(numPunishments, punishmentPenalty);
            spawner.spawnEnemies(numEnemies, enemyMovePeriod); // 4 enemies, 1 tick period (fast). Increase to slow them down.

            /** –––––––––––––––––––– GAME PANEL RENDERING –––––––––––––––––––– */
            // Score/time model
            Scoreboard scoreboard = new Scoreboard(0, regularRewardCount);
            GameState state = new GameState(board.start(), List.of(), scoreboard);

            // --- View + Window
            GamePanel panel = new GamePanel(board, scoreboard, state);
//            panel.setRenderMode(GamePanel.RenderMode.SYMBOLS);      // comment out if want IMAGES

            GameFrame frame = new GameFrame(panel);
            frame.setVisible(true);

            /** –––––––––––––––––––– GAMEPLAY CONTROLLER –––––––––––––––––––– */
            // --- Controller
            GameController controller = new GameController(board, spawner, scoreboard, state, panel);
            controller.start();
        });
    }
}

