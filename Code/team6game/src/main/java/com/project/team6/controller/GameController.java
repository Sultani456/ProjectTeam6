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

    /**
     * Tick duration in milliseconds.
     * Must match the value used in {@link Spawner}.
     */
    public static final int DEFAULT_TICK_MS = 120;

    /** The game board model. */
    private final Board board;
    /** Spawns time based items and events. */
    private final Spawner spawner;
    /** Tracks score and timers. */
    private final Scoreboard scoreboard;
    /** Tracks running, won, or lost. */
    private final GameState state;
    /** The Swing panel used to draw the game. */
    private final GamePanel view;
    /** The player model object. */
    private final Player player;

    /** Swing timer that calls {@link #onTick(ActionEvent)}. */
    private final Timer timer;

    /**
     * Builds the controller.
     *
     * @param board the board model
     * @param spawner the spawner that creates time based items
     * @param scoreboard the score tracker
     * @param state the game state holder
     * @param view the panel used for input and drawing
     */
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

    /**
     * Starts the game loop and timers.
     * Sets the state to running if needed.
     */
    public void start() {
        if (state.status() != GameState.Status.RUNNING) {
            state.setRunning();
        }
        scoreboard.start();
        timer.start();
        view.repaint();
    }

    /**
     * Stops the game loop and timers.
     * Does not change the state flag.
     */
    public void stop() {
        timer.stop();
        scoreboard.stop();
    }

    // ---------------------------------------------------------------
    // Input
    // ---------------------------------------------------------------

    /**
     * Installs all key bindings on the view.
     * Supports arrow keys and WASD.
     */
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

    /**
     * Binds one key to a small action.
     *
     * @param name a unique action name
     * @param key the keystroke to listen for
     * @param action the code to run when pressed
     */
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

    /**
     * Tries to move the player one cell in the given direction.
     * Applies collection, checks win or loss, and repaints.
     *
     * @param direction the direction to move
     */
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

    /**
     * Applies the effect of a collected object to the scoreboard.
     * Notifies the spawner when a bonus is collected.
     * Sends a small UI message to the view.
     *
     * @param obj the collected object
     */
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

    /**
     * Called by the Swing timer every tick.
     * Advances the board, updates spawner, and checks end states.
     */
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

    /**
     * Checks win and loss conditions during normal play.
     * Player must collect all required items and stand on the exit.
     * Loss occurs if the score drops below zero.
     */
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

    /**
     * Ends the game with a win message.
     * Stops timers and notifies the view.
     *
     * @param msg the message to show
     */
    private void win(String msg) {
        if (state.status() != GameState.Status.RUNNING) return;
        state.setWon();
        stop();
        view.onGameOver(msg);
    }

    /**
     * Ends the game with a loss message.
     * Stops timers, sets an explosion, and notifies the view.
     *
     * @param msg the message to show
     */
    private void lose(String msg) {
        if (state.status() != GameState.Status.RUNNING) return;
        state.setLost();
        stop();
        board.setExplosion(player.position());
        view.onGameOver(msg);
    }
}
