package com.project.team6.ui;

import javax.swing.*;

/** Simple window to host the GamePanel. */
public final class GameFrame extends JFrame {

    public GameFrame(GamePanel panel) {
        super("CMPT 276 – Arcade Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panel);
        setResizable(false);
        pack();                       // size to panel's preferred size
        setLocationRelativeTo(null);  // center on screen
    }
}
