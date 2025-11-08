package com.project.team6.ui;

import com.project.team6.controller.GameControls;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        // Start the program here.

        GameControls controller = new GameControls();

        SwingUtilities.invokeLater(() -> {
            // Run UI code on the Swing thread so it is safe.
            new GameFrame(controller).setVisible(true); // Open the game window and show it.
        });
    }
}
