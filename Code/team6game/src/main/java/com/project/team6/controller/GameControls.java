package com.project.team6.controller;

import javax.swing.*;
import java.util.List;
import java.util.Random;

import com.project.team6.model.characters.player.MoveResult;
import com.project.team6.model.characters.player.Player;
import com.project.team6.model.characters.enemies.*;

public class GameControls {

    // ---- Grid setup ----
    // Columns, rows, and tile size for the board.
    public static final int COLS = 18;
    public static final int ROWS = 11;
    public static final int TILE = 32;

    // HUD width and padding around things.
    public static final int HUD_W = 150;
    public static final int PAD = 12;

    // Calculated sizes for grid and whole panel.
    public static final int GRID_W = COLS * TILE;
    public static final int GRID_H = ROWS * TILE;
    public static final int PANEL_W = HUD_W + PAD + GRID_W + PAD;
    public static final int PANEL_H = PAD + GRID_H + PAD;

    // ---- Minimum counts ----
    // These are the minimum items we want on the map.
    private static final int MIN_ENEMIES = 4;   // 'B'
    private static final int MIN_PUNISH = 7;    // '*'
    private static final int MIN_REQUIRED = 5;  // '.'
    private static final int MIN_OPTIONAL = 10; // 'o'

    // Random for placing items.
    private final Random rng = new Random();

    // ---- Game data ----
    // The grid stores the map characters.
    private final char[][] grid = new char[ROWS][COLS];
    // Player and enemies list.
    private Player player;
    private List<Enemy> enemies;

    // ---- HUD state ----
    // Basic game state that we show on the left.
    private int score = 0;
    private int requiredLeft = 0;
    private boolean gameOver = false;
    private boolean gameWon = false;
    private int elapsedSeconds = 0;
    private String hudTime = "00:00";

    // ---- Timers ----
    // This timer updates time every second while the game is running.
    private final Timer clock = new Timer(1000, e -> {
        if (!gameOver && !gameWon) {
            elapsedSeconds++;
            hudTime = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60);
            notifyUpdate();
        }
    });

    // This timer makes enemies move every 2 seconds.
    private final Timer enemyTimer = new Timer(2000, e -> {
        if (!gameOver && !gameWon) {
            enemyTurn();
            notifyUpdate();
        }
    });

    private Runnable onUpdate = () -> {};

    // need to give this function a Runnable, in this case gamePanel::repaint
    public void setOnUpdate(Runnable onUpdate) {
        this.onUpdate = onUpdate;
    }

    private void notifyUpdate() {
        onUpdate.run();
    }

    // --------------------- Game Logic ---------------------
    // This applies one move and updates score/state based on what we stepped on.
    public void doMove(int dx, int dy) {
        MoveResult r = player.tryMove(dx, dy, grid);
        switch (r.type) {
            case COLLECTED_REQUIRED:
                requiredLeft = Math.max(0, requiredLeft - 1);
                score += 10;
                break;
            case COLLECTED_OPTIONAL:
                score += 5;
                break;
            case HIT_PUNISHMENT:
                score -= 10;
                if (score < 0) gameOver = true;
                break;
            case HIT_ENEMY:
                gameOver = true;
                break;
            case REACHED_EXIT:
                if (requiredLeft == 0) gameWon = true;
                break;
            default:
                break;
        }
        notifyUpdate();
    }

    // Enemies get their turn here. If one touches the player, it is game over.
    private void enemyTurn() {
        for (Enemy e : enemies) {
            e.tick(grid, player.getX(), player.getY());
            if (e.occupies(player.getX(), player.getY())) {
                gameOver = true;
                return;
            }
        }
    }

    // ---------------- Utility helpers ----------------

    /** Ensure S/E cells are not carrying '*' or 'B' from the map file. */
    // This keeps the start and end tiles safe.
    public void sanitizeStartEnd() {
        int[] s = findChar('S');
        int[] e = findChar('E');
        if (s[0] >= 0) {
            grid[s[1]][s[0]] = 'S';
        }
        if (e[0] >= 0) {
            grid[e[1]][e[0]] = 'E';
        }
    }

    // Count how many times a character appears on the grid.
    private int countChar(char ch) {
        int count = 0;
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (grid[r][c] == ch) count++;
        return count;
    }

    // Find the first location of a character. Returns -1,-1 if not found.
    private int[] findChar(char target) {
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (grid[r][c] == target) return new int[]{c, r};
        return new int[]{-1, -1};
    }

    // Make sure we have the minimum required items and enemies on empty spaces.
    public void ensureMinimumCounts() {
        int[] s = findChar('S');
        int[] e = findChar('E');
        int sx = s[0], sy = s[1];
        int ex = e[0], ey = e[1];

        topUp('.', MIN_REQUIRED, sx, sy, ex, ey);
        topUp('o', MIN_OPTIONAL, sx, sy, ex, ey);
        topUp('*', MIN_PUNISH, sx, sy, ex, ey);
        topUp('B', MIN_ENEMIES, sx, sy, ex, ey);
    }

    // Place more of a given character until we hit the minimum.
    private void topUp(char ch, int minCount, int sx, int sy, int ex, int ey) {
        int have = countChar(ch);
        int need = Math.max(0, minCount - have);
        int safety = COLS * ROWS * 20;

        while (need > 0 && safety-- > 0) {
            int c = rng.nextInt(COLS);
            int r = rng.nextInt(ROWS);
            if (grid[r][c] != ' ') continue;                           // only empty tiles
            if ((c == sx && r == sy) || (c == ex && r == ey)) continue; // never S/E
            grid[r][c] = ch;
            need--;
        }
    }

    public GameControls() {
//        requiredLeft = countChar('.');
//
//        // ---- Initialize player & enemies ----
//        // The player and enemies are created based on the grid.
//        player = Player.fromGrid(grid);
//        enemies = EnemyFactory.fromGridAndClear(grid);

    }

    public boolean getGameOver() {return gameOver;}

    public boolean getGameWon() {return gameWon;}

    public char getGridElement(int r, int c) {return grid[r][c];}
    public char[][] getGrid() {return grid;}

    public List<Enemy> getEnemies() {return enemies;}

    public int getScore() {return score;}
    public int getRequiredLeft() {return requiredLeft;}
    public String getHudTime() {return hudTime;}

    public void setRequiredLeft() {this.requiredLeft = countChar('.');}

    public void startClock() {clock.start();}

    public void startEnemyTimer() {enemyTimer.start();}

    public void setPlayer() {player = Player.fromGrid(grid);}
    public void setEnemies() {enemies = EnemyFactory.fromGridAndClear(grid);}
}














