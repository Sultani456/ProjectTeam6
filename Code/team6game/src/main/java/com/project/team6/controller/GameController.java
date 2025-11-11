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

    private static final int TICK_MS = 120;

    private final Board board;
    private final Scoreboard scoreboard;
    private final GameState state;
    private final GamePanel view;

    private final Player player;
    private final Timer timer;

    public GameController(Board board, Scoreboard scoreboard, GameState state, GamePanel view) {
        this.board = Objects.requireNonNull(board);
        this.scoreboard = Objects.requireNonNull(scoreboard);
        this.state = Objects.requireNonNull(state);
        this.view = Objects.requireNonNull(view);

        // Place player at Start
        Position start = board.start();
        this.player = new Player(start.x(), start.y());
        Cell startCell = board.cellAt(start);
        if (startCell != null) startCell.setOccupant(player);

        // Bind input actions to the view
        installKeyBindings();

        // Swing timer drives ticks
        this.timer = new Timer(TICK_MS, e -> onTick());
    }

    /** Start game loop and time tracking. */
    public void start() {
        scoreboard.start();
        timer.start();
        view.repaint();
    }

    /** Stop game loop and time tracking. */
    public void stop() {
        timer.stop();
        scoreboard.stop();
    }

    // ------------------------------------------------------------
    // Input
    // ------------------------------------------------------------

    private void installKeyBindings() {
        // Arrow keys
        bind("MOVE_UP",    KeyStroke.getKeyStroke("UP"),    () -> tryPlayerMove(Direction.UP));
        bind("MOVE_DOWN",  KeyStroke.getKeyStroke("DOWN"),  () -> tryPlayerMove(Direction.DOWN));
        bind("MOVE_LEFT",  KeyStroke.getKeyStroke("LEFT"),  () -> tryPlayerMove(Direction.LEFT));
        bind("MOVE_RIGHT", KeyStroke.getKeyStroke("RIGHT"), () -> tryPlayerMove(Direction.RIGHT));
        // WASD
        bind("W", KeyStroke.getKeyStroke('W'), () -> tryPlayerMove(Direction.UP));
        bind("S", KeyStroke.getKeyStroke('S'), () -> tryPlayerMove(Direction.DOWN));
        bind("A", KeyStroke.getKeyStroke('A'), () -> tryPlayerMove(Direction.LEFT));
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

        boolean moved = player.move(board, dir);
        if (!moved) return;

        // Collect item if present
        board.collectAt(player.position()).ifPresent(this::applyCollectible);

        // Immediate collision check (player stepped onto enemy)
        if (enemyOn(player.position())) {
            lose();
            view.repaint();
            return;
        }

        // Check win/lose (score may have changed from collectible)
        evaluateEndStates();
        view.repaint();
    }

    // ------------------------------------------------------------
    // Tick loop
    // ------------------------------------------------------------

    private void onTick() {
        if (state.status() != GameState.Status.RUNNING) { timer.stop(); return; }
        state.tick();

        // Let the Board advance the world: enemies + bonus expiry
        Board.TickSummary summary = board.tick(player.position());

        // Enemy collision after enemy movement
        if (summary.playerCaught()) {
            lose();
        } else {
            evaluateEndStates();
        }

        view.repaint();
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    private void applyCollectible(CollectibleObject obj) {
        obj.applyTo(scoreboard);
        if (scoreboard.score() < 0) {
            lose();
        }
    }

    private boolean enemyOn(Position p) {
        Cell c = board.cellAt(p);
        return c != null && c.occupant() instanceof Enemy;
    }

    private void evaluateEndStates() {
        if (state.status() != GameState.Status.RUNNING) return;

        if (scoreboard.score() < 0) {
            lose();
            return;
        }
        boolean allRequiredCollected = scoreboard.requiredRemaining() == 0;
        boolean atExit = player.position().equals(board.exit());
        if (allRequiredCollected && atExit) {
            win();
        }
    }

    private void win() {
        if (state.status() != GameState.Status.RUNNING) return;
        state.win();
        stop();
        view.setBannerText("You win!  Time " + scoreboard.elapsedPretty() + "   Score " + scoreboard.score());
    }

    private void lose() {
        if (state.status() != GameState.Status.RUNNING) return;
        state.lose();
        stop();
        view.setBannerText("Game over!  Time " + scoreboard.elapsedPretty() + "   Score " + scoreboard.score());
    }
}
