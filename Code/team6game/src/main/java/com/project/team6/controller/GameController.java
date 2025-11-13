package com.project.team6.controller;

import com.project.team6.model.boardUtilities.*;
import com.project.team6.model.characters.*;
import com.project.team6.model.characters.enemies.*;
import com.project.team6.model.collectibles.*;
import com.project.team6.model.runtime.*;
import com.project.team6.ui.GamePanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

/**
 * Controller: Handles input and advances the simulation with a Swing Timer.
 * - Player movement happens immediately on keypress (validated by Board).
 * - World advancement (enemies, expiring bonus rewards) happens each tick via Board.tick().
 * - View is notified via repaint() and lightweight banner messages.
 */
public final class GameController {

    public static final int DEFAULT_TICK_MS = 120;

    private final Board board;
    private final Scoreboard scoreboard;
    private final GameState state;
    private final GamePanel view;

    private final Player player;
    private final Timer timer;

    private boolean running = false;
    private boolean gameOver = false;

    public GameController(Board board, Scoreboard scoreboard, GameState state, GamePanel view) {
        this.board = Objects.requireNonNull(board);
        this.scoreboard = Objects.requireNonNull(scoreboard);
        this.state = Objects.requireNonNull(state);
        this.view  = Objects.requireNonNull(view);

        // Place player at Start
        Position s = board.start();
        this.player = new Player(s);
        board.cellAt(s).addOccupant(player);

        installKeyBindings();

        this.timer = new Timer(DEFAULT_TICK_MS, this::onTick);
    }

    public void start() {
        if (state.status() != GameState.Status.RUNNING) {
            state.setRunning();
        }

        scoreboard.start();
        timer.start();
        view.repaint();
    }

    public void stop() {
        timer.stop();
        scoreboard.stop();
    }

    // -------- Input
    private void installKeyBindings() {
        bind("UP",    KeyStroke.getKeyStroke("UP"),    () -> tryPlayerMove(Direction.UP));
        bind("DOWN",  KeyStroke.getKeyStroke("DOWN"),  () -> tryPlayerMove(Direction.DOWN));
        bind("LEFT",  KeyStroke.getKeyStroke("LEFT"),  () -> tryPlayerMove(Direction.LEFT));
        bind("RIGHT", KeyStroke.getKeyStroke("RIGHT"), () -> tryPlayerMove(Direction.RIGHT));
        // wasd
        bind("W", KeyStroke.getKeyStroke('W'), () -> tryPlayerMove(Direction.UP));
        bind("A", KeyStroke.getKeyStroke('A'), () -> tryPlayerMove(Direction.LEFT));
        bind("S", KeyStroke.getKeyStroke('S'), () -> tryPlayerMove(Direction.DOWN));
        bind("D", KeyStroke.getKeyStroke('D'), () -> tryPlayerMove(Direction.RIGHT));
    }

    private void bind(String name, KeyStroke key, Runnable action) {
        var im = view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        var am = view.getActionMap();
        im.put(key, name);
        am.put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

    private void tryPlayerMove(Direction dir) {
        if (state.status() != GameState.Status.RUNNING) return;

        MoveResult r = board.step(player, dir);

        switch (r) {
            case MOVED -> {
                // Item collection
                board.collectAt(player.position()).ifPresent(this::applyCollectible);
                evaluateEndStates();
                view.repaint();
            }
            case COLLISION -> {
                // Enemy + Player share a cell → lose immediately
                lose("You were caught!");
                view.repaint();
            }
            case BLOCKED -> { /* no-op */ }
        }
    }

    private void applyCollectible(CollectibleObject obj) {
        obj.applyTo(scoreboard); // RegularReward should call collectedRequired(...)
        view.onCollected(obj);
    }

    private void evaluateEndStates() {
        // Win condition: all required collected AND at exit
        boolean allCollected = scoreboard.requiredRemaining() == 0;
        boolean atExit = player.position().equals(board.exit());

        if (allCollected && atExit) {
            win("You win! Time " + scoreboard.elapsedPretty() + "  Score " + scoreboard.score());
        }
        // Optional: lose if score < 0, if you want
        if (scoreboard.score() < 0) {
            lose("Score below zero!");
        }
    }

    private void win(String msg) {
        if (state.status() != GameState.Status.RUNNING) return;
        state.setWon();
        stop();
        view.onGameOver(msg);
    }

    private void lose(String msg) {
        if (state.status() != GameState.Status.RUNNING) return;
        state.setLost();
        stop();
        view.onGameOver(msg);
    }

    // ------------ Ticks
    private void onTick(ActionEvent e) {
        if (state.status() != GameState.Status.RUNNING) return;

        Board.TickSummary ts = board.tick(player.position());
        if (ts.playerCaught) {
            lose("You were caught!");
        }
        view.repaint();
    }
}

