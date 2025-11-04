package model.objects.enemies;

import model.objects.GameObject;
import model.objects.characters.MainCharacter;

/**
 * Static object that reduces player score when collided.
 */
public class Punishment extends GameObject {
    private int penaltyAmount;

    public Punishment(model.boardUtilities.Position position, int penaltyAmount) {
        super(position);
        this.penaltyAmount = penaltyAmount;
    }

    public void applyTo(MainCharacter player) {
        player.subtractScore(penaltyAmount);
    }
}

