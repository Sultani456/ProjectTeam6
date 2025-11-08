package com.project.team6.ui;

import com.project.team6.controller.GameController;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        // Start the program here.

        GameController controller = new GameController();

        SwingUtilities.invokeLater(() -> {
            // Run UI code on the Swing thread so it is safe.
            new GameFrame(controller).setVisible(true); // Open the game window and show it.
        });
    }
}
