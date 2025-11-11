package com.project.team6.ui;

import com.project.team6.model.boardUtilities.*;
import com.project.team6.model.runtime.*;

import javax.swing.*;
import java.awt.*;

/**
 * Pure view. Renders the board, HUD, and an optional banner.
 * Knows nothing about input or ticking; only painting.
 */
public final class GamePanel extends JPanel {

    private static final int TILE = 32;
    private static final int HUD_H = 36;

    private final Board board;
    private final Scoreboard scoreboard;
    private final GameState state;

    private String bannerText = "";

    public GamePanel(Board board, Scoreboard scoreboard, GameState state) {
        this.board = board;
        this.scoreboard = scoreboard;
        this.state = state;

        int w = board.cols() * TILE;
        int h = board.rows() * TILE + HUD_H;
        setPreferredSize(new Dimension(w, h));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);
        requestFocusInWindow();
    }

    public void setBannerText(String text) {
        this.bannerText = text != null ? text : "";
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Board
        for (int y = 0; y < board.rows(); y++) {
            for (int x = 0; x < board.cols(); x++) {
                int px = x * TILE, py = y * TILE + HUD_H;
                Cell c = board.cellAt(new Position(x, y));
                // Terrain
                paintTerrain(g, c, px, py);
                // Item
                if (c.item() != null) {
                    paintGlyph(g, c.item().symbol(), px, py, Color.YELLOW);
                }
                // Occupant last (on top)
                if (c.occupant() != null) {
                    Color col = (c.occupant().symbol() == 'P') ? Color.CYAN : Color.RED;
                    paintGlyph(g, c.occupant().symbol(), px, py, col);
                }
                // Grid lines
                g.setColor(new Color(255,255,255,30));
                g.drawRect(px, py, TILE, TILE);
            }
        }

        // HUD
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), HUD_H);
        g.setColor(Color.WHITE);
        g.setFont(getFont().deriveFont(Font.BOLD, 16f));
        String left = "Score: " + scoreboard.score() + "   Required left: " + scoreboard.requiredRemaining();
        String mid  = "Time: " + scoreboard.elapsedPretty();
        String right= "State: " + state.status();
        g.drawString(left, 10, 24);
        g.drawString(mid, getWidth()/2 - g.getFontMetrics().stringWidth(mid)/2, 24);
        g.drawString(right, getWidth() - 10 - g.getFontMetrics().stringWidth(right), 24);

        // Banner (win/lose)
        if (!bannerText.isEmpty()) {
            g.setFont(getFont().deriveFont(Font.BOLD, 24f));
            int w = g.getFontMetrics().stringWidth(bannerText);
            g.setColor(new Color(0,0,0,180));
            g.fillRoundRect((getWidth()-w)/2 - 12, HUD_H + getHeight()/4 - 22, w + 24, 44, 12, 12);
            g.setColor(Color.WHITE);
            g.drawString(bannerText, (getWidth()-w)/2, HUD_H + getHeight()/4 + 8);
        }

        g.dispose();
    }

    private void paintTerrain(Graphics2D g, Cell c, int px, int py) {
        Color fill;
        switch (c.terrain()) {
            case WALL -> fill = new Color(60, 60, 60);
            case BARRIER -> fill = new Color(90, 90, 90);
            case START -> fill = new Color(0, 90, 0);
            case EXIT -> fill = new Color(90, 0, 0);
            default -> fill = new Color(20, 20, 20);
        }
        g.setColor(fill);
        g.fillRect(px, py, TILE, TILE);
    }

    private void paintGlyph(Graphics2D g, char ch, int px, int py, Color col) {
        g.setColor(col);
        g.setFont(getFont().deriveFont(Font.BOLD, 18f));
        String s = String.valueOf(ch);
        int w = g.getFontMetrics().stringWidth(s);
        int h = g.getFontMetrics().getAscent();
        g.drawString(s, px + (TILE - w) / 2, py + (TILE + h) / 2 - 3);
    }
}
