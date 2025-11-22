package com.project.team6.model.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests time formatting and lifecycle. */
final class ScoreboardTest {

    @Test
    void elapsedPretty_hasMmSsFormat() throws InterruptedException {
        Scoreboard s = new Scoreboard(0, 0);
        s.start();
        Thread.sleep(110);
        s.stop();
        String pretty = s.elapsedPretty();
        assertTrue(pretty.matches("\\d+:\\d{2}"));
    }
}
