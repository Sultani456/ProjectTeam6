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
 * Controller: Handles input and advances the simulation with a Swing Timer.
 * - Player movement happens immediately on keypress (validated by Board).
 * - World advancement (enemies, expiring bonus rewards) happens each tick via Board.tick().
 * - View is notified via repaint() and lightweight banner messages.
 */

public final class GameController {

    /** Must match the tick duration used when constructing Spawner. */
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

    public void stop() {
        timer.stop();
        scoreboard.stop();
    }

    // ---------------------------------------------------------------
    // Input
    // ---------------------------------------------------------------

    private void installKeyBindings() {
        bind("UP",    KeyStroke.getKeyStroke("UP"),    () -> tryPlayerMove(Direction.UP));
        bind("DOWN",  KeyStroke.getKeyStroke("DOWN"),  () -> tryPlayerMove(Direction.DOWN));
        bind("LEFT",  KeyStroke.getKeyStroke("LEFT"),  () -> tryPlayerMove(Direction.LEFT));
        bind("RIGHT", KeyStroke.getKeyStroke("RIGHT"), () -> tryPlayerMove(Direction.RIGHT));

        // WASD
        bind("W", KeyStroke.getKeyStroke('w'), () -> tryPlayerMove(Direction.UP));
        bind("S", KeyStroke.getKeyStroke('s'), () -> tryPlayerMove(Direction.DOWN));
        bind("A", KeyStroke.getKeyStroke('a'), () -> tryPlayerMove(Direction.LEFT));
        bind("D", KeyStroke.getKeyStroke('d'), () -> tryPlayerMove(Direction.RIGHT));
    }

    private void bind(String name, KeyStroke key, Runnable action) {
        InputMap im = view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = view.getActionMap();

        im.put(key, name);
        am.put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

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
            case COLLISION -> {
                lose("You were caught!");
            }
            case BLOCKED -> {
                // no-op
            }
        }
    }

    /** Apply score effects; */
    private void applyCollectible(CollectibleObject obj) {
        int val = obj.value();

        // Required reward?
        if (obj.isRequiredToWin()) {
            scoreboard.collectedRequired(val);
        }
        // Optional reward? (e.g., BonusReward)
        else if (val > 0) {
            scoreboard.collectedOptional(val);
        }
        // Punishment (value < 0)
        else {
            scoreboard.penalize(val);
        }

        // Notify spawner only if it's a bonus
        if (obj instanceof BonusReward) {
            spawner.notifyBonusCollected();
        }

        // UI feedback
        view.onCollected(obj);
    }


    // ---------------------------------------------------------------
    // Tick loop
    // ---------------------------------------------------------------

    private void onTick(ActionEvent e) {
        if (state.status() != GameState.Status.RUNNING) return;

        Position playerPos = player.position();
        TickSummary summary = board.tick(playerPos);

        // bonus spawning
        spawner.onTick();

        if (summary.playerCaught()) {
            lose("You were caught!");
        } else {
            evaluateEndStates();
        }

        view.repaint();
    }

    // ---------------------------------------------------------------
    // Win / lose logic
    // ---------------------------------------------------------------

    private void evaluateEndStates() {
        if (state.status() != GameState.Status.RUNNING) return;

        boolean allRequiredCollected = scoreboard.requiredRemaining() == 0;
        boolean atExit = player.position().equals(board.exit());

        if (allRequiredCollected && atExit) {
            win("You win! Time " + scoreboard.elapsedPretty()
                    + "   Score " + scoreboard.score());
        }

        // Optional: lose when score < 0
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
        board.setExplosion(player.position());
        view.onGameOver(msg);
    }
}


