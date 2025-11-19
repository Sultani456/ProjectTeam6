package com.project.team6.model.board.utilities;

public final class TickSummary {
    private final boolean playerCaught;

    public TickSummary(boolean playerCaught) {
        this.playerCaught = playerCaught;
    }

    public boolean playerCaught() {
        return playerCaught;
    }
}
