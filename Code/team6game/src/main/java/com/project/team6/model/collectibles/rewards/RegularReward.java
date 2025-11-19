package com.project.team6.model.collectibles.rewards;

import com.project.team6.model.board.Position;

/** Mandatory reward; needed to win. */
public final class RegularReward extends Reward {

    public RegularReward(Position position, int value) {
        super(position, value, true);
    }

    @Override public char symbol() { return '.'; }
}

