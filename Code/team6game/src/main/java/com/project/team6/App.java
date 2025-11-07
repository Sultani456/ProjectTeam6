package com.project.team6;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        // Start the program here.
        SwingUtilities.invokeLater(() -> {
            // Run UI code on the Swing thread so it is safe.
            new GameFrame().setVisible(true); // Open the game window and show it.
        });
    }
}
