package model.objects.enemies;

import model.boardUtilities.Position;
import model.objects.characters.Character;

/**
 * Base enemy class.
 */
public abstract class Enemy extends Character {
    protected int speed;

    public Enemy(Position position, int speed) {
        super(position);
        this.speed = speed;
    }

    public int getSpeed() {
        return speed;
    }

    public abstract void moveToward(Position target);
}
