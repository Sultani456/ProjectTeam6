package com.project.team6.ui;

import com.project.team6.controller.GameControls;
import com.project.team6.controller.GamePanel;

import javax.swing.JFrame; // we use JFrame to make the app window

// This class creates the main window for the game.
// It puts the game panel inside the window and shows it.
public class GameFrame extends JFrame {

    public GameFrame(GameControls controller) {
        super("Maze Game"); // window title
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close the app when window is closed
        setResizable(false); // keep a fixed window size so tiles line up

        // Add the main game panel
        setContentPane(new GamePanel(controller)); // put the game screen inside this window

        pack(); // automatically resize window to fit the panel size
        setLocationRelativeTo(null); // center on screen
        // After this, when we show the frame, it should appear centered and ready.
    }
}


