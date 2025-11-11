package com.project.team6.model.runtime;

import java.time.Duration;
import java.time.Instant;

/**
 * Tracks score, required collectibles remaining, and elapsed time.
 * Controller starts/stops it; collectibles apply effects here.
 */
public final class Scoreboard {
    private int score;
    private int requiredRemaining;

    private Instant startedAt;
    private Instant stoppedAt;

    public Scoreboard(int initialScore, int requiredCount) {
        this.score = initialScore;
        this.requiredRemaining = Math.max(0, requiredCount);
    }

    // Lifecycle
    public void start() { startedAt = Instant.now(); stoppedAt = null; }
    public void stop()  { if (startedAt != null && stoppedAt == null) stoppedAt = Instant.now(); }

    // Score API
    public int score() { return score; }
    public int requiredRemaining() { return requiredRemaining; }

    public void add(int delta) { score += delta; }

    /** Called when a required (regular) reward is collected. */
    public void collectedRequired(int value) {
        score += value;
        if (requiredRemaining > 0) requiredRemaining--;
    }

    /** Called when an optional (bonus) reward is collected. */
    public void collectedOptional(int value) { score += value; }

    /** Called when a punishment is collected; value should be <= 0. */
    public void penalize(int negativeValue) { score += negativeValue; }

    // Time
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

