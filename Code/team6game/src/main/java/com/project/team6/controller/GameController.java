package com.project.team6.controller;

import com.project.team6.model.board.*;
import com.project.team6.model.board.generators.*;
import com.project.team6.model.board.utilities.Direction;
import com.project.team6.model.board.utilities.MoveResult;
import com.project.team6.model.board.utilities.TickSummary;
import com.project.team6.model.characters.*;
import com.project.team6.model.collectibles.*;
import com.project.team6.model.collectibles.rewards.*;
import com.project.team6.model.runtime.*;
import com.project.team6.ui.GamePanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

/**
 * Runs input and the game loop.
 * Reads keys, moves the player, and advances the world on each tick.
 * Repaints the view after actions.
 */
public final class GameController {

    public static final int DEFAULT_TICK_MS = 120;

    private final Board board;
    private final Spawner spawner;
    private final Scoreboard scoreboard;
    private final GameState state;
    private final GamePanel view;
    private final Player player;
    private final Timer timer;

    public GameController(Board board,
                          Spawner spawner,
                          Scoreboard scoreboard,
                          GameState state,
                          GamePanel view) {
        this.board = Objects.requireNonNull(board);
        this.spawner = Objects.requireNonNull(spawner);
        this.scoreboard = Objects.requireNonNull(scoreboard);
        this.state = Objects.requireNonNull(state);
        this.view = Objects.requireNonNull(view);

        this.player = board.player();

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

    /**
     * Stops the game loop timer.
     * Scoreboard is stopped by GameState on win/lose.
     */
    public void stop() {
        timer.stop();
        // scoreboard.stop(); // removed to avoid double-stop on win/lose
    }

    // ---------------------------------------------------------------
    // Input
    // ---------------------------------------------------------------

    private void installKeyBindings() {
        bind("UP",    KeyStroke.getKeyStroke("UP"),    () -> tryPlayerMove(Direction.UP));
        bind("DOWN",  KeyStroke.getKeyStroke("DOWN"),  () -> tryPlayerMove(Direction.DOWN));
        bind("LEFT",  KeyStroke.getKeyStroke("LEFT"),  () -> tryPlayerMove(Direction.LEFT));
        bind("RIGHT", KeyStroke.getKeyStroke("RIGHT"), () -> tryPlayerMove(Direction.RIGHT));

        bind("W", KeyStroke.getKeyStroke('w'), () -> tryPlayerMove(Direction.UP));
        bind("S", KeyStroke.getKeyStroke('s'), () -> tryPlayerMove(Direction.DOWN));
        bind("A", KeyStroke.getKeyStroke('a'), () -> tryPlayerMove(Direction.LEFT));
        bind("D", KeyStroke.getKeyStroke('d'), () -> tryPlayerMove(Direction.RIGHT));
    }

    private void bind(String name, KeyStroke key, Runnable action) {
        InputMap inputMap = view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = view.getActionMap();

        inputMap.put(key, name);
        actionMap.put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

    // ---------------------------------------------------------------
    // Movement
    // ---------------------------------------------------------------

    private void tryPlayerMove(Direction direction) {
        if (state.status() != GameState.Status.RUNNING) return;

        MoveResult result = board.step(player, direction);

        switch (result) {
            case MOVED -> {
                board.collectAt(player.position())
                        .ifPresent(this::applyCollectible);
                evaluateEndStates();
                view.repaint();
            }
            case COLLISION -> lose("You were caught!");
            case BLOCKED -> { /* no-op */ }
        }
    }

    // ---------------------------------------------------------------
    // Collectibles
    // ---------------------------------------------------------------

    private void applyCollectible(CollectibleObject obj) {
        int val = obj.value();

        if (obj.isRequiredToWin()) {
            scoreboard.collectedRequired(val);
        } else if (val > 0) {
            scoreboard.collectedOptional(val);
        } else {
            scoreboard.penalize(val);
        }

        notifyBonusIfNeeded(obj);

        view.onColle
