package com.project.team6;

import javax.swing.JFrame;

public class GameFrame extends JFrame {

    public GameFrame() {
        super("Maze Game"); // window title
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Add the main game panel
        setContentPane(new GamePanel());

        pack(); // automatically resize window to fit the panel size
        setLocationRelativeTo(null); // center on screen
    }
}