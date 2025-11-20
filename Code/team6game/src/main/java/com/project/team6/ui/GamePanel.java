package com.project.team6.ui;

import com.project.team6.model.board.*;
import com.project.team6.model.collectibles.*;
import com.project.team6.model.collectibles.rewards.*;
import com.project.team6.model.runtime.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.net.URL;
import java.util.*;

/**
 * View class that draws the game.
 * Renders the board, HUD, and an optional banner.
 * Does not handle input or game logic.
 */
public final class GamePanel extends JPanel {

    /**
     * How the board is drawn.
     * Symbols uses ASCII. Images uses sprites.
     */
    public enum RenderMode {
        SYMBOLS, IMAGES
    }

    /**
     * Sets the render mode and repaints.
     *
     * @param mode new render mode
     */
    public void setRenderMode(RenderMode mode) {
        this.renderMode = Objects.requireNonNull(mode);
        repaint();
    }

    /**
     * Switches between images and symbols.
     * Triggers a repaint.
     */
    public void toggleRenderMode() {
        this.renderMode = (renderMode == RenderMode.IMAGES)
                ? RenderMode.SYMBOLS
                : RenderMode.IMAGES;
        repaint();
    }

    // default to images
    private RenderMode renderMode = RenderMode.IMAGES;

    private static final int TILE = 28;         // size of one board tile in pixels
    private static final int HUD_H = 54;        // height of HUD strip at bottom


    // --- colours for background / grid / floor ---
    private static final Color BACKGROUND_COLOR = Color.BLACK;

    private static final Color FLOOR_COLOR      = new Color(28, 28, 30);
    private static final Color FLOOR_COLOR_IMAGES = new Color(180,200,225);
    private static final Color GRID_COLOR       = new Color(20, 20, 22);

    // model references
    private final Board board;
    private final Scoreboard scoreboard;
    private final GameState state;

    // --- image assets ---
    private BufferedImage imgPlayer;
    private BufferedImage imgEnemy;
    private BufferedImage imgWall;
    private BufferedImage imgStart;
    private BufferedImage imgExit;
    private BufferedImage imgRegularReward;
    private BufferedImage imgBonusReward;
    private BufferedImage imgPunishment;
    private BufferedImage imgExplosion;

    private String bannerText = null;

    /**
     * Creates the panel and loads images.
     * Sets size based on board rows and columns.
     *
     * @param board      model of the world
     * @param scoreboard score and time model
     * @param state      game state
     */
    public GamePanel(Board board, Scoreboard scoreboard, GameState state) {
        this.board = board;
        this.scoreboard = scoreboard;
        this.state = state;

        imgPlayer        = loadImage("/assets/player.png");
        imgEnemy         = loadImage("/assets/enemy.png");
        imgWall          = loadImage("/assets/wall.jpg");
        imgStart         = loadImage("/assets/start.jpg");
        imgExit          = loadImage("/assets/exit.jpg");
        imgRegularReward = loadImage("/assets/RegularReward.png");
        imgBonusReward   = loadImage("/assets/BonusReward.jpg");
        imgPunishment    = loadImage("/assets/punishment.png");
        imgExplosion     = loadImage("/assets/explosion.jpg");

        int w = board.cols() * TILE;
        int h = board.rows() * TILE + HUD_H;
        setPreferredSize(new Dimension(w, h));
        setBackground(BACKGROUND_COLOR);
        setFocusable(true);
        requestFocusInWindow();
    }

    /**
     * Called when the player collects an item.
     * You can add small UI effects here.
     *
     * @param obj collected item
     */
    public void onCollected(CollectibleObject obj) {
        // optional UI feedback; keep minimal
    }

    /**
     * Shows a banner with a message.
     * Used on win or loss.
     *
     * @param message text to show
     */
    public void onGameOver(String message) {
        this.bannerText = message;
        repaint();
    }

    /**
     * Paints the HUD and the board.
     * Uses images or symbols based on the render mode.
     *
     * @param g0 graphics context
     */
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // background
        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());

        // HUD at top
        paintHud(g);

        // board starts under HUD
        int originX = 0;
        int originY = HUD_H;

        if (renderMode == RenderMode.IMAGES) {
            // ===== IMAGE MODE =====
            for (int row = 0; row < board.rows(); row++) {
                for (int col = 0; col < board.cols(); col++) {
                    Position pos = new Position(col, row);
                    Cell cell = board.cellAt(pos);

                    int px = originX + col * TILE;
                    int py = originY + row * TILE;

                    // floor background
                    g.setColor(FLOOR_COLOR_IMAGES);
//                    g.setColor(FLOOR_COLOR);
                    g.fillRect(px, py, TILE, TILE);

                    // EXPLOSION EFFECT (if caught)
                    if (board.explosionPos() != null
                            && board.explosionPos().equals(pos)) {

                        // draw powder-blue or terrain first
                        g.setColor(FLOOR_COLOR_IMAGES);
                        g.fillRect(px, py, TILE, TILE);

                        // then explosion sprite
                        g.drawImage(imgExplosion, px, py, TILE, TILE, null);

                        // skip all other drawing for this cell
                        continue;
                    }

                    // --- 1) Draw terrain background ---
                    switch (cell.terrain()) {
                        case WALL, BARRIER -> g.drawImage(imgWall, px, py, TILE, TILE, null);
                        case START        -> g.drawImage(imgStart, px, py, TILE, TILE, null);
                        case EXIT         -> g.drawImage(imgExit,  px, py, TILE, TILE, null);
                        default -> {
                            g.setColor(FLOOR_COLOR_IMAGES);
                            g.fillRect(px, py, TILE, TILE);
                        }
                    }

                    // --- 2) Draw collectibles ---
                    var item = cell.item();
                    if (item instanceof RegularReward) {
                        g.drawImage(imgRegularReward, px, py, TILE, TILE, null);
                    } else if (item instanceof BonusReward) {
                        g.drawImage(imgBonusReward, px, py, TILE, TILE, null);
                    } else if (item instanceof Punishment) {
                        g.drawImage(imgPunishment, px, py, TILE, TILE, null);
                    }

                    // --- 3) Draw enemies ---
                    if (cell.hasEnemy()) {
                        g.drawImage(imgEnemy, px, py, TILE, TILE, null);
                    }

                    // --- 4) Draw player last (on top) ---
                    if (cell.hasPlayer()) {
                        g.drawImage(imgPlayer, px, py, TILE, TILE, null);
                    }

                    // --- 5) Draw grid outline ---
                    g.setColor(GRID_COLOR);
                    g.drawRect(px, py, TILE, TILE);
                }
            }
        } else {
            // ===== SYMBOL MODE (old ASCII-style rendering) =====
            for (int row = 0; row < board.rows(); row++) {
                for (int col = 0; col < board.cols(); col++) {
                    Position pos = new Position(col, row);
                    Cell cell    = board.cellAt(pos);

                    int px = originX + col * TILE;
                    int py = originY + row * TILE;

                    // background per terrain (optional – you can simplify if you like)
                    switch (cell.terrain()) {
                        case WALL, BARRIER -> g.setColor(new Color(55, 55, 55));
                        case START         -> g.setColor(new Color(0, 128, 0));
                        case EXIT          -> g.setColor(new Color(128, 0, 0));
                        default            -> g.setColor(FLOOR_COLOR);
                    }
                    g.fillRect(px, py, TILE, TILE);

                    // grid outline
                    g.setColor(GRID_COLOR);
                    g.drawRect(px, py, TILE, TILE);

                    // ASCII symbol from Cell.symbol()
                    char sym = cell.symbol();
                    if (sym != ' ') {
                        // choose colour based on symbol
                        Color fg = switch (sym) {
                            case 'P' -> new Color(90, 210, 250);   // player
                            case 'B' -> new Color(210, 90, 120);   // enemy / bad guy
                            case '.' -> new Color(255, 255, 200);  // regular reward
                            case 'o' -> new Color(255, 210, 120);  // bonus reward
                            case '*' -> new Color(255, 120, 120);  // punishment
                            case 'C' -> new Color(255, 255, 255);  // collision
                            case 'X' -> new Color(160, 160, 160);  // wall
                            case '#' -> new Color(200, 200, 200);  // barrier
                            case 'S' -> new Color(120, 255, 120);  // start
                            case 'E' -> new Color(255, 120, 120);  // exit
                            default  -> Color.WHITE;
                        };

                        g.setColor(fg);
                        g.setFont(getFont().deriveFont(Font.BOLD, (float) (TILE * 0.6)));

                        FontMetrics fm = g.getFontMetrics();
                        int cw = fm.charWidth(sym);
                        int ch = fm.getAscent();

                        int cx = px + (TILE - cw) / 2;
                        int cy = py + (TILE + ch) / 2 - 4;
                        g.drawString(String.valueOf(sym), cx, cy);
                    }
                }
            }
        }

        // banner text under the board
        if (bannerText != null && !bannerText.isBlank()) {
            g.setFont(getFont().deriveFont(Font.BOLD, 20f));
            String text = bannerText;

            FontMetrics fm = g.getFontMetrics();
            int textW = fm.stringWidth(text);
            int textH = fm.getHeight();

            // Board area (under the HUD)
            int boardW = board.cols() * TILE;
            int boardH = board.rows() * TILE;

            int centerX = boardW / 2;
            int centerY = HUD_H + boardH / 2;   // middle of the board area

            int padX = 24;
            int padY = 12;
            int rectW = textW + padX * 2;
            int rectH = textH + padY * 2;
            int rectX = centerX - rectW / 2;
            int rectY = centerY - rectH / 2;

            // Translucent black background
            g.setColor(new Color(0, 0, 0, 90));   // alpha 170 ~= 2/3 opaque
            g.fillRoundRect(rectX, rectY, rectW, rectH, 16, 16);

            // White text centered in the box
            g.setColor(Color.WHITE);
            int textX = centerX - textW / 2;
            int textY = rectY + padY + fm.getAscent();
            g.drawString(text, textX, textY);
        }

        g.dispose();
    }

    /**
     * Draws the top HUD strip.
     * Shows score, required left, time, and state.
     *
     * @param g graphics context
     */
    private void paintHud(Graphics2D g) {
        // HUD strip occupies the top HUD_H pixels of the panel
        g.setColor(new Color(24, 24, 24));
        g.fillRect(0, 0, getWidth(), HUD_H);

        g.setColor(Color.WHITE);
        g.setFont(getFont().deriveFont(Font.BOLD, 16f));

        String left   = "Score: " + scoreboard.score()
                + "   Required left: " + scoreboard.requiredRemaining();
        String middle = "Time: " + scoreboard.elapsedPretty();
        String right  = state.status().name();

        int baselineY = HUD_H - 10;  // a bit above the bottom of the bar

        // left text
        g.drawString(left, 10, baselineY);

        // centered middle text
        int midX = getWidth() / 2;
        int midW = g.getFontMetrics().stringWidth(middle);
        g.drawString(middle, midX - midW / 2, baselineY);

        // right-aligned status
        int rightW = g.getFontMetrics().stringWidth(right);
        g.drawString(right, getWidth() - rightW - 10, baselineY);
    }

    /**
     * Loads an image from the classpath.
     *
     * @param resourcePath path inside resources
     * @return loaded image
     * @throws RuntimeException if the image cannot be loaded
     */
    private BufferedImage loadImage(String resourcePath) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url == null) {
                throw new IllegalStateException("Image resource not found: " + resourcePath);
            }
            return ImageIO.read(url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load image: " + resourcePath, e);
        }
    }

    /**
     * Draws item and character sprites in a cell.
     * Items go under characters.
     *
     * @param g   graphics context
     * @param cell cell to draw
     * @param px  x in pixels
     * @param py  y in pixels
     */
    private void drawCellSprites(Graphics2D g, Cell cell, int px, int py) {
        // --- items first (under characters) ---
        var item = cell.item();
        if (item instanceof RegularReward) {
            g.drawImage(imgRegularReward, px, py, TILE, TILE, null);
        } else if (item instanceof BonusReward) {
            g.drawImage(imgBonusReward, px, py, TILE, TILE, null);
        } else if (item instanceof Punishment) {
            g.drawImage(imgPunishment, px, py, TILE, TILE, null);
        }

        // --- characters on top ---
        // Enemy under Player so Player appears “in front”
        if (cell.hasEnemy()) {
            g.drawImage(imgEnemy, px, py, TILE, TILE, null);
        }
        if (cell.hasPlayer()) {
            g.drawImage(imgPlayer, px, py, TILE, TILE, null);
        }
    }
}
