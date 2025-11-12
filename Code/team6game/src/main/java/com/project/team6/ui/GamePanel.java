package com.project.team6.ui;

import com.project.team6.model.boardUtilities.*;
import com.project.team6.model.collectibles.*;
import com.project.team6.model.runtime.*;

import javax.swing.*;
import java.awt.*;

/**
 * Pure view. Renders the board, HUD, and an optional banner.
 * Knows nothing about input or ticking; only painting.

 * View: paints the Board. It does not mutate the model.
 * GameController calls repaint(), onCollected(...), and onGameOver(...).
 */
public final class GamePanel extends JPanel {

    private static final int TILE = 36;
    private static final int HUD_H = 36;

    private final Board board;
    private final Scoreboard scoreboard;
    private final GameState state;

    private String bannerText = null;

    public GamePanel(Board board, Scoreboard scoreboard, GameState state) {
        this.board = board;
        this.scoreboard = scoreboard;
        this.state = state;

        int w = board.cols() * TILE;
        int h = board.rows() * TILE + HUD_H;
        setPreferredSize(new Dimension(w, h));
        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocusInWindow();
    }

    public void onCollected(CollectibleObject obj) {
        // optional UI feedback; keep minimal
    }

    public void onGameOver(String message) {
        this.bannerText = message;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // HUD strip
        paintHud(g);

        // Cells
        for (int y = 0; y < board.rows(); y++) {
            for (int x = 0; x < board.cols(); x++) {
                int px = x * TILE;
                int py = y * TILE + HUD_H;
                Cell c = board.cellAt(new Position(x, y));

                // Background by terrain
                switch (c.terrain()) {
                    case WALL -> g.setColor(new Color(35, 35, 35));
                    case BARRIER -> g.setColor(new Color(80, 80, 80));
                    case START -> g.setColor(new Color(22, 48, 22));
                    case EXIT -> g.setColor(new Color(48, 22, 22));
                    default -> g.setColor(new Color(18, 18, 18));
                }
                g.fillRect(px, py, TILE, TILE);

                // Grid lines
                g.setColor(new Color(55, 55, 55));
                g.drawRect(px, py, TILE, TILE);

                // Foreground glyph (from Cell.symbol, which now prioritizes P/B/C over S/E)
                char sym = c.symbol();
                if (sym != ' ') {
                    g.setFont(getFont().deriveFont(Font.BOLD, (float) (TILE * 0.6)));
                    Color fg = switch (sym) {
                        case 'P' -> new Color(90, 210, 255);
                        case 'B' -> new Color(255, 120, 120);
                        case 'C' -> new Color(255, 90, 0);
                        case '.', 'o' -> new Color(230, 230, 140);
                        case '*' -> new Color(255, 90, 160);
                        case 'S' -> new Color(120, 220, 120);
                        case 'E' -> new Color(220, 120, 120);
                        case 'X', '#' -> new Color(160, 160, 160);
                        default -> Color.WHITE;
                    };
                    g.setColor(fg);
                    var fm = g.getFontMetrics();
                    int tw = fm.charWidth(sym);
                    int th = fm.getAscent();
                    int cx = px + (TILE - tw) / 2;
                    int cy = py + (TILE + th) / 2 - 4;
                    g.drawString(String.valueOf(sym), cx, cy);
                }
            }
        }

        // Banner
        if (bannerText != null && !bannerText.isBlank()) {
            String text = bannerText;
            g.setFont(getFont().deriveFont(Font.BOLD, 18f));
            int w = g.getFontMetrics().stringWidth(text);
            int bx = (getWidth() - (w + 24)) / 2;
            int by = HUD_H + (getHeight() - HUD_H) / 3;
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRoundRect(bx, by, w + 24, 40, 12, 12);
            g.setColor(Color.WHITE);
            g.drawString(text, bx + 12, by + 26);
        }

        g.dispose();
    }

    private void paintHud(Graphics2D g) {
        g.setColor(new Color(24, 24, 24));
        g.fillRect(0, 0, getWidth(), HUD_H);

        g.setColor(Color.WHITE);
        g.setFont(getFont().deriveFont(Font.BOLD, 16f));

        String left  = "Score: " + scoreboard.score() + "   Required left: " + scoreboard.requiredRemaining();
        String mid   = "Time: " + scoreboard.elapsedPretty();
        String right = "State: " + state.status();

        g.drawString(left, 10, 24);
        int midX = getWidth() / 2 - g.getFontMetrics().stringWidth(mid) / 2;
        g.drawString(mid, midX, 24);
        int rightX = getWidth() - 10 - g.getFontMetrics().stringWidth(right);
        g.drawString(right, rightX, 24);
    }
}


