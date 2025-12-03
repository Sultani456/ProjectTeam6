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

    // ---------------------------------------------------------------------
    // Reset (new)
    // ---------------------------------------------------------------------

    /**
     * Resets score, required count, and timer fields so the scoreboard
     * can be reused for a new playthrough.
     */
    public void reset(int initialScore, int requiredCount) {
        this.score = initialScore;
        this.requiredRemaining = Math.max(0, requiredCount);
        this.startedAt = null;
        this.stoppedAt = null;
    }

    // ---------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------

    /** Starts the timer. Resets any previous stop time. */
    public void start() {
        startedAt = Instant.now();
        stoppedAt = null;
    }

    /** Stops the timer if it is running. */
    public void stop() {
        if (startedAt != null && stoppedAt == null) {
            stoppedAt = Instant.now();
        }
    }

    // ---------------------------------------------------------------------
    // Score API
    // ---------------------------------------------------------------------

    public int score() { return score; }

    public int requiredRemaining() { return requiredRemaining; }

    /** Adds a delta to the score. */
    public void add(int delta) { score += delta; }

    /** Required reward collected. */
    public void collectedRequired(int value) {
        score += value;
        if (requiredRemaining > 0) requiredRemaining--;
    }

    /** Optional reward collected. */
    public void collectedOptional(int value) {
        score += value;
    }

    /** Punishment applied. */
    public void penalize(int negativeValue) {
        score += negativeValue;
    }

    // ---------------------------------------------------------------------
    // Time
    // ---------------------------------------------------------------------

    public Duration elapsed() {
        if (startedAt == null) return Duration.ZERO;
        Instant end = (stoppedAt != null) ? stoppedAt : Instant.now();
        return Duration.between(startedAt, end);
    }

    public String elapsedPretty() {
        Duration d = elapsed();
        long s = d.getSeconds();
        long m = s / 60;
        long sec = s % 60;
        return String.format("%d:%02d", m, sec);
    }
}
