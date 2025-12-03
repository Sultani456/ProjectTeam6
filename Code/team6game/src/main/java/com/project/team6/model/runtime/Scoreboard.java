package com.project.team6.model.runtime;

import java.time.Duration;
import java.time.Instant;

/**
 * Tracks score, required collectibles, and elapsed time.
 * The controller starts and stops it. Collectibles update the score here.
 */
public final class Scoreboard {

    /** Initial score for resets. */
    private final int initialScore;

    /** Initial required count for resets. */
    private final int initialRequiredCount;

    /** Current score. Can be positive or negative. */
    private int score;

    /** How many required rewards are still needed to win. */
    private int requiredRemaining;

    /** When the timer started. Null if not started yet. */
    private Instant startedAt;

    /** When the timer stopped. Null if still running. */
    private Instant stoppedAt;

    /**
     * Creates a scoreboard.
     *
     * @param initialScore   starting score
     * @param requiredCount  number of required rewards on the board
     */
    public Scoreboard(int initialScore, int requiredCount) {
        this.initialScore = initialScore;
        this.initialRequiredCount = Math.max(0, requiredCount);
        reset();
    }

    // ---------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------

    /**
     * Resets score, required count, and timer.
     * Use this before starting a new game with the same scoreboard.
     */
    public void reset() {
        this.score = initialScore;
        this.requiredRemaining = initialRequiredCount;
        this.startedAt = null;
        this.stoppedAt = null;
    }

    /**
     * Starts the timer. Resets any previous stop time.
     */
    public void start() {
        startedAt = Instant.now();
        stoppedAt = null;
    }

    /**
     * Stops the timer if it is running.
     * Safe to call multiple times.
     */
    public void stop() {
        if (startedAt != null && stoppedAt == null) {
            stoppedAt = Instant.now();
        }
    }

    // ---------------------------------------------------------------------
    // Score API
    // ---------------------------------------------------------------------

    /**
     * Returns the current score.
     *
     * @return score value
     */
    public int score() {
        return score;
    }

    /**
     * Returns how many required rewards remain.
     *
     * @return remaining required count
     */
    public int requiredRemaining() {
        return requiredRemaining;
    }

    /**
     * Adds a delta to the score.
     *
     * @param delta points to add, can be negative
     */
    public void add(int delta) {
        adjustScore(delta);
    }

    /**
     * Call when a required reward is collected.
     * Adds its value and decreases the remaining counter.
     *
     * @param value points to add
     */
    public void collectedRequired(int value) {
        adjustScore(value);
        if (requiredRemaining > 0) {
            requiredRemaining--;
        }
    }

    /**
     * Call when an optional reward is collected.
     * Adds its value to the score.
     *
     * @param value points to add
     */
    public void collectedOptional(int value) {
        adjustScore(value);
    }

    /**
     * Call when a punishment is collected.
     * The value must be zero or negative.
     *
     * @param negativeValue points to add, expected to be <= 0
     * @throws IllegalArgumentException if the value is positive
     */
    public void penalize(int negativeValue) {
        if (negativeValue > 0) {
            throw new IllegalArgumentException(
                    "Penalty value must be <= 0, got: " + negativeValue
            );
        }
        adjustScore(negativeValue);
    }

    /**
     * Internal helper to adjust the score.
     *
     * @param delta points to add (may be negative)
     */
    private void adjustScore(int delta) {
        score += delta;
    }

    // ---------------------------------------------------------------------
    // Time
    // ---------------------------------------------------------------------

    /**
     * Returns elapsed time since start.
     * If stopped, returns the duration between start and stop.
     * If never started, returns zero.
     *
     * @return elapsed duration
     */
    public Duration elapsed() {
        if (startedAt == null) {
            return Duration.ZERO;
        }
        Instant end = (stoppedAt != null) ? stoppedAt : Instant.now();
        return Duration.between(startedAt, end);
    }

    /**
     * Returns a short mm:ss style setring of elapsed time.
     *
     * @return formatted time string
     */
    public String elapsedPretty() {
        Duration d = elapsed();
        long seconds = d.getSeconds();
        long minutes = seconds / 60;
        long sec = seconds % 60;
        return String.format("%d:%02d", minutes, sec);
    }
}
