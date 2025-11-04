package model.objects.rewards;

import model.objects.characters.MainCharacter;

/**
 * Standard collectible reward.
 */
public class RegularReward extends Reward {

    public RegularReward(model.boardUtilities.Position position, int value) {
        super(position, value);
    }

    @Override
    public void applyTo(MainCharacter player) {
        player.addScore(value);
    }
}
