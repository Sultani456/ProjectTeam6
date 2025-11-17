package com.project.team6.ui;

import com.project.team6.controller.GameController;
import com.project.team6.model.boardUtilities.*;
import com.project.team6.model.generators.BoardGenerator;
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
        List<Position> barrierList = BoardGenerator.barrierList();

        int rows = 24;
        int cols = 24;

        int regularRewardCount = 8;
        int regularPoints = 5;

        int bonusRewardCount = 5;
        int bonusPoints = 10;
        int spawnAt_MinSec = 3;
        int spawnAt_MaxSec = 6;
        int lifeMinSec = 3;
        int lifeMaxSec = 6;

        int numPunishments = 5;
        int punishmentPenalty = -5;

        int numEnemies = 4;
        int enemyMovePeriod = 10;

        double boardBarrierPercentage = 0.1;        // 0.1 to 0.4 are best

        SwingUtilities.invokeLater(() -> {
            /** –––––––––––––––––––– BOARD GENERATION –––––––––––––––––––– */
            BoardGenerator gen = new BoardGenerator();


            /** Option A: NONE – No barriers, with given rows/cols from client*/
            BoardGenerator.Options opts = new BoardGenerator.Options(rows, cols, BoardGenerator.InternalBarrierMode.NONE, null, null);

            /** Option B: PROVIDED – populate barriers from given barrierList, with given rows/cols from client */
//            BoardGenerator.Options opts = new BoardGenerator.Options(rows, cols, BoardGenerator.InternalBarrierMode.PROVIDED, barrierList, null);

            /** Option C: RANDOM – randomly put barriers, with given rows/cols from client */
//            BoardGenerator.Options opts = new BoardGenerator.Options(rows, cols, BoardGenerator.InternalBarrierMode.RANDOM, null, null);

            /** Option D: TEXT – populate barriers from textfile, with given rows/cols from textfile */
//            BoardGenerator.Options opts = new BoardGenerator.Options(0,0, BoardGenerator.InternalBarrierMode.TEXT, null, "maps/level1.txt");


            BoardGenerator.Output output = gen.generate(opts, boardBarrierPercentage);
            Board board = new Board(output);

            /** –––––––––––––––––––– BOARD POPULATION –––––––––––––––––––– */
            board.configureBonusSpawner(bonusRewardCount, bonusPoints, spawnAt_MinSec, spawnAt_MaxSec, lifeMinSec, lifeMaxSec);

            // --- Spawning (tweak counts as you like)
            board.spawnRegularRewards(regularRewardCount, regularPoints);
            board.spawnPunishments(numPunishments, punishmentPenalty);
            board.spawnEnemies(numEnemies, enemyMovePeriod); // 4 enemies, 1 tick period (fast). Increase to slow them down.

            /** –––––––––––––––––––– GAME PANEL RENDERING –––––––––––––––––––– */
            // Score/time model
            Scoreboard scoreboard = new Scoreboard(0, regularRewardCount);
            GameState state = new GameState(board.start(), List.of(), scoreboard);

            // --- View + Window
            GamePanel panel = new GamePanel(board, scoreboard, state);
//            panel.setRenderMode(GamePanel.RenderMode.SYMBOLS);      // comment out if want IMAGES

            GameFrame frame = new GameFrame(panel);
            frame.setVisible(true);

            // --- Controller
            GameController controller = new GameController(board, scoreboard, state, panel);
            controller.start();
        });
    }
}

