package com.project.team6.model.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests time formatting and penalty rules in Scoreboard.
 */
final class ScoreboardTest {

    @Test
    void elapsedPrettyHasMmSsFormat() throws InterruptedException {
        Scoreboard scoreboard = new Scoreboard();
        scoreboard.start();
        Thread.sleep(50);
        scoreboard.stop();

        String pretty = scoreboard.elapsedPretty();
        assertTrue(pretty.matches("\\d+:\\d{2}"));
    }

    @Test
    void penalizeRejectsPositiveValues() {
        Scoreboard scoreboard = new Scoreboard();

        assertThrows(IllegalArgumentException.class, () -> scoreboard.penalize(5));
    }
}
