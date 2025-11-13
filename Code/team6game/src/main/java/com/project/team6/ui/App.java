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

        int rows = 11;
        int cols = 18;

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
        int enemyMovePeriod = 20;

        SwingUtilities.invokeLater(() -> {
            /** –––––––––––––––––––– BOARD GENERATION –––––––––––––––––––– */
            BoardGenerator gen = new BoardGenerator();

            /** Option A: from textfile
             * Automatic wall/barrier population
             */
//            BoardGenerator.Options opts = new BoardGenerator.Options(0,0, BoardGenerator.InternalBarrierMode.TEXT, null, "maps/level1.txt", 42L);
//            BoardGenerator.Output output = gen.generate(opts);
//            Board board = new Board(output);

            /** Option B: from given list */
            BoardGenerator.Options opts2 = new BoardGenerator.Options(rows, cols, BoardGenerator.InternalBarrierMode.PROVIDED, barrierList, null);
            BoardGenerator.Output output2 = gen.generate(opts2);
            Board board2 = new Board(output2);

            /** –––––––––––––––––––– BOARD POPULATION –––––––––––––––––––– */
            board2.configureBonusSpawner(bonusRewardCount, bonusPoints, spawnAt_MinSec, spawnAt_MaxSec, lifeMinSec, lifeMaxSec);

            // --- Spawning (tweak counts as you like)
            board2.spawnRegularRewards(regularRewardCount, regularPoints);
            board2.spawnPunishments(numPunishments, punishmentPenalty);
            board2.spawnEnemies(numEnemies, enemyMovePeriod); // 4 enemies, 1 tick period (fast). Increase to slow them down.

            // Score/time model
            Scoreboard scoreboard = new Scoreboard(0, regularRewardCount);
            GameState state = new GameState(board2.start(), List.of(), scoreboard);

            // --- View + Window
            GamePanel panel = new GamePanel(board2, scoreboard, state);
            GameFrame frame = new GameFrame(panel);
            frame.setVisible(true);

            // --- Controller
            GameController controller = new GameController(board2, scoreboard, state, panel);
            controller.start();
        });
    }
}

