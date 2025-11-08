package com.project.team6.controller;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import com.project.team6.model.characters.player.MoveResult;
import com.project.team6.model.characters.player.Player;
import com.project.team6.model.characters.enemies.*;

public class GamePanel extends JPanel {

    // ---- Colors ----
    // Colors we use for background, frames, lines, and HUD.
    private static final Color GREEN_BG = new Color(180, 220, 80);
    private static final Color GREEN_FRAME = new Color(140, 120, 60);
    private static final Color GRID_LINES = new Color(20, 70, 20);
    private static final Color HUD_BG = new Color(240, 235, 200);
    private static final Color HUD_TEXT = new Color(30, 30, 30);

    // ---- Images (loaded from classpath) ----
    // Sprites for everything. They can be null if missing.
    private Image imgPlayer, imgEnemy, imgPunish, imgReq, imgOpt, imgEnd, imgWall;

    private final GameController controller;

    public GamePanel(GameController controller) {
        // Set the panel size and basic settings.
        this.controller = controller;
        controller.setOnUpdate(this::repaint);

        setPreferredSize(new Dimension(GameController.PANEL_W, GameController.PANEL_H));
        setBackground(Color.WHITE);
        setFocusable(true);

        // ---- Load map (classpath: src/main/resources/maps/level1.txt) ----
        // We read a level file and copy it into our grid.
        try {
            MapLoader.LoadedLevel L = MapLoader.load("maps/level1.txt", COLS, ROWS, true);
            for (int r = 0; r < ROWS; r++) {
                System.arraycopy(L.grid[r], 0, grid[r], 0, COLS);
            }
        } catch (IOException ex) {
            // If map fails to load, we stop because the game cannot run.
            throw new RuntimeException("Failed to load map file: " + ex.getMessage(), ex);
        }

        // Make sure start and end cells are clean and not blocked.
        sanitizeStartEnd();

        // ---- Top-up missing items (never on S/E) ----
        // We ensure the map has enough items and enemies.
        ensureMinimumCounts();
        requiredLeft = countChar('.');

        // ---- Initialize player & enemies ----
        // The player and enemies are created based on the grid.
        player = Player.fromGrid(grid);
        enemies = EnemyFactory.fromGridAndClear(grid);

        // ---- Load images from classpath (/assets/...) ----
        // We try to load all sprites. If any is missing, we draw a colored box.
        imgPlayer = safeLoad("/assets/player.png");
        imgEnemy  = safeLoad("/assets/enemy.png");
        imgPunish = safeLoad("/assets/punishment.png");
        imgReq    = safeLoad("/assets/reward_required.png");
        imgOpt    = safeLoad("/assets/reward_optional.png");
        imgEnd    = safeLoad("/assets/end.png");
        imgWall   = safeLoad("/assets/wall1.png");

        // Key bindings and timers start here.
        setupKeyBindings();
        clock.start();
        enemyTimer.start();
    }

    // ------------------------ Input ------------------------
    // We bind arrow keys and WASD to the same move actions.
    private void setupKeyBindings() {
        bind("LEFT", -1, 0);
        bind("RIGHT", 1, 0);
        bind("UP", 0, -1);
        bind("DOWN", 0, 1);
        bind("A", -1, 0);
        bind("D", 1, 0);
        bind("W", 0, -1);
        bind("S", 0, 1);
    }

    // Helper to connect a key to a movement delta.
    private void bind(String key, int dx, int dy) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), "mv_" + key);
        getActionMap().put("mv_" + key, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (!gameOver && !gameWon) doMove(dx, dy);
            }
        });
    }

    // --------------------- Rendering ---------------------
    // We draw the HUD, the board, walls, items, enemies, and grid lines.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // HUD background box on the left.
        g.setColor(HUD_BG);
        g.fillRect(PAD, PAD, HUD_W, GRID_H);

        // Board top-left corner.
        int boardX = PAD + HUD_W + PAD;
        int boardY = PAD;

        // A frame and the green board background.
        g.setColor(GREEN_FRAME);
        g.fillRect(boardX - 6, boardY - 6, GRID_W + 12, GRID_H + 12);
        g.setColor(GREEN_BG);
        g.fillRect(boardX, boardY, GRID_W, GRID_H);

        // Walls
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] == 'X') {
                    int x = boardX + c * TILE, y = boardY + r * TILE;
                    if (imgWall != null) g.drawImage(imgWall, x, y, TILE, TILE, null);
                    else {
                        // If wall image is missing, we draw a brown block.
                        g.setColor(new Color(90, 70, 60));
                        g.fillRect(x, y, TILE, TILE);
                    }
                }
            }
        }

        // Static collectibles
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                char ch = grid[r][c];
                switch (ch) {
                    case 'S': drawSprite(g, imgPlayer, c, r, boardX, boardY, Color.BLUE); break;
                    case 'E': drawSprite(g, imgEnd, c, r, boardX, boardY, Color.CYAN); break;
                    case '*': drawSprite(g, imgPunish, c, r, boardX, boardY, Color.BLACK); break;
                    case '.': drawSprite(g, imgReq, c, r, boardX, boardY, Color.YELLOW); break;
                    case 'o': drawSprite(g, imgOpt, c, r, boardX, boardY, Color.ORANGE); break;
                    default:  break;
                }
            }
        }

        // Enemies
        for (Enemy e : enemies) {
            // Each enemy is drawn as a sprite or a red box if image is missing.
            drawSprite(g, imgEnemy, e.getX(), e.getY(), boardX, boardY, Color.RED);
        }

        // Grid lines to make the tiles visible.
        g.setColor(GRID_LINES);
        for (int c = 0; c <= COLS; c++)
            g.drawLine(boardX + c * TILE, boardY, boardX + c * TILE, boardY + GRID_H);
        for (int r = 0; r <= ROWS; r++)
            g.drawLine(boardX, boardY + r * TILE, boardX + GRID_W, boardY + r * TILE);

        // Finally draw the HUD text.
        drawHud(g, PAD, PAD, HUD_W);
    }

    // Draw one tile sprite. If image is null, we draw a simple colored box.
    private void drawSprite(Graphics g, Image img, int c, int r, int boardX, int boardY, Color fallback) {
        int x = boardX + c * TILE;
        int y = boardY + r * TILE;
        if (img == null) {
            g.setColor(fallback);
            g.fillRect(x + 4, y + 4, TILE - 8, TILE - 8);
        } else {
            g.drawImage(img, x + 2, y + 2, TILE - 4, TILE - 4, null);
        }
    }

    // Draws the text on the left side: score, required left, and time.
    private void drawHud(Graphics g, int x, int y, int w) {
        g.setColor(HUD_TEXT);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        int line = y + 30;
        g.drawString("Score: " + score, x + 10, line); line += 25;
        g.drawString("R. Left: " + requiredLeft, x + 10, line); line += 25;
        g.drawString("Time: " + hudTime, x + 10, line);

        // Show end messages.
        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            g.drawString("GAME OVER!", x + 10, line + 40);
        } else if (gameWon) {
            g.setColor(Color.GREEN.darker());
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            g.drawString("You won!", x + 10, line + 40);
        }
    }

    /**
     * Load an image strictly from the classpath (src/main/resources).
     * Pass either "/assets/xyz.png" or "assets/xyz.png".
     */
    // If it cannot find the image, it returns null and prints a warning.
    private Image safeLoad(String resourcePath) {
        String cp = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
        try (InputStream in = getClass().getResourceAsStream(cp)) {
            if (in != null) {
                BufferedImage img = ImageIO.read(in);
                if (img != null) return img;
            }
        } catch (IOException ignored) {
        }
        System.err.println("Warning: missing image on classpath: " + cp);
        return null;
    }
}
