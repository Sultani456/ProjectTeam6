package com.project.team6;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import com.project.team6.enemy.*; // Enemy, EnemyFactory

public class GamePanel extends JPanel {

    // ---- Grid setup ----
    private static final int COLS = 18;
    private static final int ROWS = 11;
    private static final int TILE = 32;

    private static final int HUD_W = 150;
    private static final int PAD = 12;

    private static final int GRID_W = COLS * TILE;
    private static final int GRID_H = ROWS * TILE;
    private static final int PANEL_W = HUD_W + PAD + GRID_W + PAD;
    private static final int PANEL_H = PAD + GRID_H + PAD;

    // ---- Colors ----
    private static final Color GREEN_BG = new Color(180, 220, 80);
    private static final Color GREEN_FRAME = new Color(140, 120, 60);
    private static final Color GRID_LINES = new Color(20, 70, 20);
    private static final Color HUD_BG = new Color(240, 235, 200);
    private static final Color HUD_TEXT = new Color(30, 30, 30);

    // ---- Minimum counts ----
    private static final int MIN_ENEMIES = 4;   // 'B'
    private static final int MIN_PUNISH = 7;    // '*'
    private static final int MIN_REQUIRED = 5;  // '.'
    private static final int MIN_OPTIONAL = 10; // 'o'

    private final Random rng = new Random();

    // ---- Game data ----
    private final char[][] grid = new char[ROWS][COLS];
    private Player player;
    private List<Enemy> enemies;

    // ---- HUD state ----
    private int score = 0;
    private int requiredLeft = 0;
    private boolean gameOver = false;
    private boolean gameWon = false;
    private int elapsedSeconds = 0;
    private String hudTime = "00:00";

    // ---- Images (loaded from classpath) ----
    private Image imgPlayer, imgEnemy, imgPunish, imgReq, imgOpt, imgEnd, imgWall;

    // ---- Timers ----
    private final Timer clock = new Timer(1000, e -> {
        if (!gameOver && !gameWon) {
            elapsedSeconds++;
            hudTime = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60);
            repaint();
        }
    });

    private final Timer enemyTimer = new Timer(2000, e -> {
        if (!gameOver && !gameWon) {
            enemyTurn();
            repaint();
        }
    });

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_W, PANEL_H));
        setBackground(Color.WHITE);
        setFocusable(true);

        // ---- Load map (classpath: src/main/resources/maps/level1.txt) ----
        try {
            // The MapLoader expects a path; keep your API but ensure the file is in resources/maps.
            MapLoader.LoadedLevel L = MapLoader.load("maps/level1.txt", COLS, ROWS, true);
            for (int r = 0; r < ROWS; r++) {
                System.arraycopy(L.grid[r], 0, grid[r], 0, COLS);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load map file: " + ex.getMessage(), ex);
        }

        // ---- Top-up missing items ----
        ensureMinimumCounts();
        requiredLeft = countChar('.');

        // ---- Initialize player & enemies ----
        player = Player.fromGrid(grid);
        enemies = EnemyFactory.fromGridAndClear(grid);

        // ---- Load images from classpath (/assets/...) ----
        imgPlayer = safeLoad("/assets/player.png");
        imgEnemy  = safeLoad("/assets/enemy.png");
        imgPunish = safeLoad("/assets/punishment.png");
        imgReq    = safeLoad("/assets/reward_required.png");
        imgOpt    = safeLoad("/assets/reward_optional.png");
        imgEnd    = safeLoad("/assets/end.png");
        imgWall   = safeLoad("/assets/wall1.png");

        setupKeyBindings();
        clock.start();
        enemyTimer.start();
    }

    // ------------------------ Input ------------------------
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

    private void bind(String key, int dx, int dy) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), "mv_" + key);
        getActionMap().put("mv_" + key, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (!gameOver && !gameWon) doMove(dx, dy);
            }
        });
    }

    // --------------------- Game Logic ---------------------
    private void doMove(int dx, int dy) {
        Player.MoveResult r = player.tryMove(dx, dy, grid);
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
        repaint();
    }

    private void enemyTurn() {
        for (Enemy e : enemies) {
            e.tick(grid, player.getX(), player.getY());
            if (e.occupies(player.getX(), player.getY())) {
                gameOver = true;
                return;
            }
        }
    }

    // --------------------- Rendering ---------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(HUD_BG);
        g.fillRect(PAD, PAD, HUD_W, GRID_H);

        int boardX = PAD + HUD_W + PAD;
        int boardY = PAD;

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
                        g.setColor(new Color(90, 70, 60));
                        g.fillRect(x, y, TILE, TILE);
                    }
                }
            }
        }

        // Static objects
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
            drawSprite(g, imgEnemy, e.getX(), e.getY(), boardX, boardY, Color.RED);
        }

        // Grid lines
        g.setColor(GRID_LINES);
        for (int c = 0; c <= COLS; c++)
            g.drawLine(boardX + c * TILE, boardY, boardX + c * TILE, boardY + GRID_H);
        for (int r = 0; r <= ROWS; r++)
            g.drawLine(boardX, boardY + r * TILE, boardX + GRID_W, boardY + r * TILE);

        drawHud(g, PAD, PAD, HUD_W);
    }

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

    private void drawHud(Graphics g, int x, int y, int w) {
        g.setColor(HUD_TEXT);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        int line = y + 30;
        g.drawString("Score: " + score, x + 10, line); line += 25;
        g.drawString("R. Left: " + requiredLeft, x + 10, line); line += 25;
        g.drawString("Time: " + hudTime, x + 10, line);

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

    // ---------------- Utility helpers ----------------
    private int countChar(char ch) {
        int count = 0;
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (grid[r][c] == ch) count++;
        return count;
    }

    private int[] findChar(char target) {
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (grid[r][c] == target) return new int[]{c, r};
        return new int[]{-1, -1};
    }

    private void ensureMinimumCounts() {
        int[] s = findChar('S');
        int[] e = findChar('E');
        int sx = s[0], sy = s[1];
        int ex = e[0], ey = e[1];

        topUp('.', MIN_REQUIRED, sx, sy, ex, ey);
        topUp('o', MIN_OPTIONAL, sx, sy, ex, ey);
        topUp('*', MIN_PUNISH, sx, sy, ex, ey);
        topUp('B', MIN_ENEMIES, sx, sy, ex, ey);
    }

    private void topUp(char ch, int minCount, int sx, int sy, int ex, int ey) {
        int have = countChar(ch);
        int need = Math.max(0, minCount - have);
        int safety = COLS * ROWS * 20;

        while (need > 0 && safety-- > 0) {
            int c = rng.nextInt(COLS);
            int r = rng.nextInt(ROWS);
            if (grid[r][c] != ' ') continue;
            if ((c == sx && r == sy) || (c == ex && r == ey)) continue;
            grid[r][c] = ch;
            need--;
        }
    }

    /**
     * Load an image strictly from the classpath (src/main/resources).
     * Pass either "/assets/xyz.png" or "assets/xyz.png".
     */
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
