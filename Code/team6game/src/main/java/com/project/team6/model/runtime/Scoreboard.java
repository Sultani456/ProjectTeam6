package com.project.team6.model.runtime;

import java.time.Duration;
import java.time.Instant;

/**
 * Tracks score, required collectibles, and elapsed time.
 * The controller starts and stops it. Collectibles update the score here.
 */
public final class Scoreboard {

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
        this.score = initialScore;
        this.requiredRemaining = Math.max(0, requiredCount);
    }

    // Lifecycle

    /**
     * Starts the timer. Resets any previous stop time.
     */
    public void start() { startedAt = Instant.now(); stoppedAt = null; }

    /**
     * Stops the timer if it is running.
     */
    public void stop()  { if (startedAt != null && stoppedAt == null) stoppedAt = Instant.now(); }

    // Score API

    /**
     * Returns the current score.
     *
     * @return score value
     */
    public int score() { return score; }

    /**
     * Returns how many required rewards remain.
     *
     * @return remaining required count
     */
    public int requiredRemaining() { return requiredRemaining; }

    /**
     * Adds a delta to the score.
     *
     * @param delta points to add, can be negative
     */
    public void add(int delta) { score += delta; }

    /**
     * Call when a required reward is collected.
     * Adds its value and decreases the remaining counter.
     *
     * @param value points to add
     */
    public void collectedRequired(int value) {
        score += value;
        if (requiredRemaining > 0) requiredRemaining--;
    }

    /**
     * Call when an optional reward is collected.
     * Adds its value to the score.
     *
     * @param value points to add
     */
    public void collectedOptional(int value) { score += value; }

    /**
     * Call when a punishment is collected.
     * The value should be zero or negative.
     *
     * @param negativeValue points to add, expected to be negative
     */
    public void penalize(int negativeValue) { score += negativeValue; }

    // Time

    /**
     * Returns elapsed time since start.
     * If stopped, returns the duration between start and stop.
     * If never started, returns zero.
     *
     * @return elapsed duration
     */
    public Duration elapsed() {
        if (startedAt == null) return Duration.ZERO;
        Instant end = (stoppedAt != null) ? stoppedAt : Instant.now();
        return Duration.between(startedAt, end);
    }

    /**
     * Returns a short mm:ss style string of elapsed time.
     *
     * @return formatted time string
     */
    public String elapsedPretty() {
        Duration d = elapsed();
        long s = d.getSeconds();
        long m = s / 60;
        long sec = s % 60;
        return String.format("%d:%02d", m, sec);
    }
}
