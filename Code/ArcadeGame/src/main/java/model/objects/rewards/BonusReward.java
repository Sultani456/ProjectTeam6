package model.objects.rewards;

import model.objects.characters.MainCharacter;

/**
 * Temporary bonus collectible.
 */
public class BonusReward extends Reward {
    private int duration;
    private boolean isActive;

    public BonusReward(model.boardUtilities.Position position, int value, int duration) {
        super(position, value);
        this.duration = duration;
        this.isActive = true;
    }

    public void spawnRandomly() {
        isActive = true;
    }

    public void expire() {
        isActive = false;
    }

    @Override
    public void applyTo(MainCharacter player) {
        if (isActive) {
            player.addScore(value);
            expire();
        }
    }
}

