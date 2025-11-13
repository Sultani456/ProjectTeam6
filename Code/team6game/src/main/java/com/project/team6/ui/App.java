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

        int regularRewardCount = 8;
        int regularPoints = 5;

        int bonusRewardCount = 5;
        int bonusPoints = 10;
        int spawnAt_MinSec = 3;
        int spawnAt_MaxSec = 6;
        int lifeMinSec = 3;
        int lifeMaxSec = 6;

        int numPunishments = 10;
        int punishmentPenalty = -5;

        int numEnemies = 4;
        int enemyMovePeriod = 10;

        SwingUtilities.invokeLater(() -> {
            // --- Board generation options
            var opts = new BoardGenerator.Options(
                    /*rows*/ 11,
                    /*cols*/ 18,
                    /*start*/ new Position(0, 9),
                    /*exit*/  new Position(17, 8),
                    BoardGenerator.InternalBarrierMode.PROVIDED,
                    // Provide your own internal barrier list, or use RANDOM mode above
                    barrierList,
                    /*seed*/ 42L
            );

            var out   = BoardGenerator.generate(opts);
            Board board = new Board(out);

            board.configureBonusSpawner(bonusRewardCount, bonusPoints, spawnAt_MinSec, spawnAt_MaxSec, lifeMinSec, lifeMaxSec);

            // --- Spawning (tweak counts as you like)
            board.spawnRegularRewards(regularRewardCount, regularPoints);
            board.spawnPunishments(numPunishments, punishmentPenalty);
            board.spawnEnemies(numEnemies, enemyMovePeriod); // 4 enemies, 1 tick period (fast). Increase to slow them down.

            // Score/time model
            Scoreboard scoreboard = new Scoreboard(0, regularRewardCount);
            GameState state = new GameState(board.start(), List.of(), scoreboard);

            // --- View + Window
            GamePanel panel = new GamePanel(board, scoreboard, state);
            GameFrame frame = new GameFrame(panel);
            frame.setVisible(true);

            // --- Controller
            GameController controller = new GameController(board, scoreboard, state, panel);
            controller.start();
        });
    }
}

