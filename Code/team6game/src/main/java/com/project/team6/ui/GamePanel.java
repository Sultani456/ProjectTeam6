package com.project.team6.ui;

import com.project.team6.controller.GameConfig;
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

    // default to images
    private RenderMode renderMode = RenderMode.IMAGES;

    // model references
    private final Board board;
    private final Scoreboard scoreboard;
    private final GameState state;

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

        int w = board.cols() * GameConfig.TILE;
        int h = board.rows() * GameConfig.TILE + GameConfig.HUD_H;
        setPreferredSize(new Dimension(w, h));
        setBackground(GameConfig.BACKGROUND_COLOR);
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
        g.setColor(GameConfig.BACKGROUND_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());

        // HUD at top
        paintHud(g);

        // board starts under HUD
        int originX = 0;
        int originY = GameConfig.HUD_H;

        // draw sprites
        for (int row = 0; row < board.rows(); row++) {
            for (int col = 0; col < board.cols(); col++) {
                Position pos = new Position(col, row);
                Cell cell = board.cellAt(pos);

                int px = originX + col * GameConfig.TILE;
                int py = originY + row * GameConfig.TILE;

                if (renderMode == RenderMode.IMAGES) {
                    drawCellSpritesForImages(g, cell, px, py, pos);
                } else {        // RenderMode.SYMBOLS
                    drawCellSpritesForSymbols(g, cell, px, py, pos);
                }
            }
        }

        // banner text under the board
        if (bannerText != null && !bannerText.isBlank()) {
            drawBanner(g);
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
        g.setColor(GameConfig.HUD_BACKGROUND);
        g.fillRect(0, 0, getWidth(), GameConfig.HUD_H);

        g.setColor(Color.WHITE);
        g.setFont(getFont().deriveFont(Font.BOLD, 16f));

        String left   = "Score: " + scoreboard.score()
                + "   Required left: " + scoreboard.requiredRemaining();
        String middle = "Time: " + scoreboard.elapsedPretty();
        String right  = state.status().name();

        int baselineY = GameConfig.HUD_H - 10;  // a bit above the bottom of the bar

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
    private void drawCellSpritesForImages(Graphics2D g, Cell cell, int px, int py, Position pos) {
        // floor background
        g.setColor(GameConfig.FLOOR_COLOR_IMAGES);
        g.fillRect(px, py, GameConfig.TILE, GameConfig.TILE);

        // EXPLOSION EFFECT (if caught)
        if (board.explosionPos() != null
                && board.explosionPos().equals(pos)) {

            // then explosion sprite
            g.drawImage(GameConfig.imgExplosion, px, py, GameConfig.TILE, GameConfig.TILE, null);

            // skip all other drawing for this cell
            return;
        }

        // --- 1) Draw terrain background ---
        switch (cell.terrain()) {
            case WALL, BARRIER -> g.drawImage(GameConfig.imgWall, px, py, GameConfig.TILE, GameConfig.TILE, null);
            case START        -> g.drawImage(GameConfig.imgStart, px, py, GameConfig.TILE, GameConfig.TILE, null);
            case EXIT         -> g.drawImage(GameConfig.imgExit,  px, py, GameConfig.TILE, GameConfig.TILE, null);
//            default -> {
//                g.setColor(FLOOR_COLOR_IMAGES);
//                g.fillRect(px, py, TILE, TILE);
//            }
        }

        // --- 2) Draw collectibles ---
        // --- items first (under characters) ---
        var item = cell.item();
        if (item instanceof RegularReward) {
            g.drawImage(GameConfig.imgRegularReward, px, py, GameConfig.TILE, GameConfig.TILE, null);
        } else if (item instanceof BonusReward) {
            g.drawImage(GameConfig.imgBonusReward, px, py, GameConfig.TILE, GameConfig.TILE, null);
        } else if (item instanceof Punishment) {
            g.drawImage(GameConfig.imgPunishment, px, py, GameConfig.TILE, GameConfig.TILE, null);
        }

        // --- 3) Draw enemies ---
        // Enemy under Player so Player appears “in front”
        if (cell.hasEnemy()) {
            g.drawImage(GameConfig.imgEnemy, px, py, GameConfig.TILE, GameConfig.TILE, null);
        }

        // --- 4) Draw player last (on top) ---
        if (cell.hasPlayer()) {
            g.drawImage(GameConfig.imgPlayer, px, py, GameConfig.TILE, GameConfig.TILE, null);
        }

        // --- 5) Draw grid outline ---
        g.setColor(GameConfig.GRID_COLOR);
        g.drawRect(px, py, GameConfig.TILE, GameConfig.TILE);
    }

    private void drawCellSpritesForSymbols(Graphics2D g, Cell cell, int px, int py, Position pos) {
        // background per terrain
        switch (cell.terrain()) {
            case WALL, BARRIER -> g.setColor(GameConfig.SYMBOLBACKGROUND_WALL_COLOR);
            case START         -> g.setColor(GameConfig.SYMBOLBACKGROUND_START_COLOR);
            case EXIT          -> g.setColor(GameConfig.SYMBOLBACKGROUND_EXIT_COLOR);
            default            -> g.setColor(GameConfig.FLOOR_COLOR);
        }
        g.fillRect(px, py, GameConfig.TILE, GameConfig.TILE);

        // grid outline
        g.setColor(GameConfig.GRID_COLOR);
        g.drawRect(px, py, GameConfig.TILE, GameConfig.TILE);

        // ASCII symbol from Cell.symbol()
        char sym = cell.symbol();
        if (sym != ' ') {
            // choose colour based on symbol
            Color fg = switch (sym) {
                case 'P' ->   GameConfig.SYMBOL_PLAYER_COLOR;           // player
                case 'B' ->   GameConfig.SYMBOL_ENEMY_COLOR;            // enemy / bad guy
                case '.' ->   GameConfig.SYMBOL_REGULARREWARD_COLOR;    // regular reward
                case 'o' ->   GameConfig.SYMBOL_BONUSREWARD_COLOR;      // bonus reward
                case '*' ->   GameConfig.SYMBOL_PUNISHMENT_COLOR;       // punishment
                case 'C' ->   GameConfig.SYMBOL_COLLISION_COLOR;        // collision
                case 'X' ->   GameConfig.SYMBOL_WALL_COLOR;             // wall
                case '#' ->   GameConfig.SYMBOL_BARRIER_COLOR;          // barrier
                case 'S' ->   GameConfig.SYMBOL_START_COLOR;            // start
                case 'E' ->   GameConfig.SYMBOL_EXIT_COLOR;             // exit
                default  -> Color.WHITE;
            };

            g.setColor(fg);
            g.setFont(getFont().deriveFont(Font.BOLD, (float) (GameConfig.TILE * 0.6)));

            FontMetrics fm = g.getFontMetrics();
            int cw = fm.charWidth(sym);
            int ch = fm.getAscent();

            int cx = px + (GameConfig.TILE - cw) / 2;
            int cy = py + (GameConfig.TILE + ch) / 2 - 4;
            g.drawString(String.valueOf(sym), cx, cy);
        }
    }

    private void drawBanner(Graphics2D g) {
        g.setFont(getFont().deriveFont(Font.BOLD, 20f));
        String text = bannerText;

        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(text);
        int textH = fm.getHeight();

        // Board area (under the HUD)
        int boardW = board.cols() * GameConfig.TILE;
        int boardH = board.rows() * GameConfig.TILE;

        int centerX = boardW / 2;
        int centerY = GameConfig.HUD_H + boardH / 2;   // middle of the board area

        int padX = 24;
        int padY = 12;
        int rectW = textW + padX * 2;
        int rectH = textH + padY * 2;
        int rectX = centerX - rectW / 2;
        int rectY = centerY - rectH / 2;

        // Translucent black background
        g.setColor(GameConfig.BANNER_BACKGROUND);   // alpha 170 ~= 2/3 opaque
        g.fillRoundRect(rectX, rectY, rectW, rectH, 16, 16);

        // White text centered in the box
        g.setColor(Color.WHITE);
        int textX = centerX - textW / 2;
        int textY = rectY + padY + fm.getAscent();
        g.drawString(text, textX, textY);
    }
}
