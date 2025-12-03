package com.project.team6.controller;

import com.project.team6.model.board.Position;

import java.awt.*;
import java.util.ArrayList;

public class GameConfig {
    // controller
    /** Tick duration in milliseconds. */
    public static final int DEFAULT_TICK_MS = 120;

    // rendering
    public static final int TILE = 36;         // size of one board tile in pixels
    public static final int HUD_H = 36;        // height of HUD strip at bottom
    // --- colours for background / grid / floor ---
    public static final Color BACKGROUND_COLOR = Color.BLACK;
    public static final Color FLOOR_COLOR      = new Color(28, 28, 30);
    public static final Color FLOOR_COLOR_IMAGES = new Color(180,200,225);
    public static final Color GRID_COLOR       = new Color(20, 20, 22);
    public static final Color SYMBOLBACKGROUND_WALL_COLOR = new Color(55, 55, 55);
    public static final Color SYMBOLBACKGROUND_START_COLOR = new Color(0, 128, 0);
    public static final Color SYMBOLBACKGROUND_EXIT_COLOR = new Color(128, 0, 0);
    public static final Color SYMBOL_PLAYER_COLOR = new Color(90, 210, 250);
    public static final Color SYMBOL_ENEMY_COLOR = new Color(210, 90, 120);
    public static final Color SYMBOL_REGULARREWARD_COLOR = new Color(255, 255, 200);
    public static final Color SYMBOL_BONUSREWARD_COLOR = new Color(255, 210, 120);
    public static final Color SYMBOL_PUNISHMENT_COLOR = new Color(255, 120, 120);
    public static final Color SYMBOL_COLLISION_COLOR = new Color(255, 255, 255);
    public static final Color SYMBOL_WALL_COLOR = new Color(160, 160, 160);
    public static final Color SYMBOL_BARRIER_COLOR = new Color(200, 200, 200);
    public static final Color SYMBOL_START_COLOR = new Color(120, 255, 120);
    public static final Color SYMBOL_EXIT_COLOR = new Color(255, 120, 120);
    public static final Color BANNER_BACKGROUND = new Color(0, 0, 0, 90);
    public static final Color HUD_BACKGROUND = new Color(24, 24, 24);


    // gameplay
    public static ArrayList<Position> barrierList = new ArrayList<>();
    public static String mapResource = null;

    public static int rows = 24;
    public static int cols = 24;

    public static int regularRewardCount = 15;
    public static int regularPoints = 10;

    public static int bonusRewardCount = 5;
    public static int bonusPoints = 20;

    public static int spawnMinSec = 3;
    public static int spawnMaxSec = 6;
    public static int lifeMinSec = 3;
    public static int lifeMaxSec = 6;

    public static int numPunishments = 8;
    public static int punishmentPenalty = -5;

    public static int numEnemies = 5;
    public static int enemyMovePeriod = 10;

    // Barrier density for RANDOM barrier mode (fraction of interior cells)
    public static double boardBarrierPercentage = 0.2;

    // helper functions
    public static void addToBarrierList() {
        barrierList.add(new Position(4, 2));
        barrierList.add(new Position(13,2));
        barrierList.add(new Position(4,4));
        barrierList.add(new Position(5,4));
        barrierList.add(new Position(12,4));
        barrierList.add(new Position(13,4));
        barrierList.add(new Position(4,6));
        barrierList.add(new Position(8,6));
        barrierList.add(new Position(9,6));
        barrierList.add(new Position(10,6));
        barrierList.add(new Position(7,7));
        barrierList.add(new Position(8,7));
        barrierList.add(new Position(3,8));
        barrierList.add(new Position(4,8));
        barrierList.add(new Position(5,8));
        barrierList.add(new Position(12,8));
        barrierList.add(new Position(13,8));
        barrierList.add(new Position(14,8));
    }

    public static void setBoardDimensions(int rows, int cols) {
        GameConfig.rows = rows;
        GameConfig.cols = cols;
    }

    public static void setMapResource(String newMapResource) {
        GameConfig.mapResource = newMapResource;
    }


}
