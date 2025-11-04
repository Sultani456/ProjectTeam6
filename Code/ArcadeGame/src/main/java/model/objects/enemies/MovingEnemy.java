package model.objects.enemies;

import model.boardUtilities.Position;
import model.boardUtilities.Direction;

/**
 * Enemy that moves toward the player.
 */
public class MovingEnemy extends Enemy {

    public MovingEnemy(Position position, int speed) {
        super(position, speed);
    }

    @Override
    public void moveToward(Position target) {
        int dx = target.getX() - position.getX();
        int dy = target.getY() - position.getY();

        if (Math.abs(dx) > Math.abs(dy)) {
            position = position.next(dx > 0 ? Direction.RIGHT : Direction.LEFT);
        } else {
            position = position.next(dy > 0 ? Direction.DOWN : Direction.UP);
        }
    }

    @Override
    public void move(Direction direction) {
        position = position.next(direction);
    }

    @Override
    public boolean canMoveTo(model.boardUtilities.Cell cell) {
        return cell.isEmpty();
    }
}

