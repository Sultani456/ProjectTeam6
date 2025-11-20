package com.project.team6.ui;

import javax.swing.*;

/**
 * Window that shows the game panel.
 */
public final class GameFrame extends JFrame {

    /**
     * Builds the main game window.
     * Sets the content, size, and position.
     *
     * @param panel game panel to display
     */
    public GameFrame(GamePanel panel) {
        super("CMPT 276 – Arcade Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panel);
        setResizable(false);
        pack();                       // size to panel's preferred size
        setLocationRelativeTo(null);  // center on screen
    }
}
