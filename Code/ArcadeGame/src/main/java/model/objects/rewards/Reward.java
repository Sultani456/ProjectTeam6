package model.objects.rewards;

import model.objects.GameObject;
import model.objects.characters.MainCharacter;

/**
 * Abstract reward collectible.
 */
public abstract class Reward extends GameObject {
    protected int value;

    public Reward(model.boardUtilities.Position position, int value) {
        super(position);
        this.value = value;
    }

    public abstract void applyTo(MainCharacter player);
}
