package model.objects.characters;

import model.boardUtilities.Cell;
import model.boardUtilities.Direction;
import model.objects.rewards.Reward;
import model.objects.enemies.Punishment;

/**
 * Represents the player-controlled main character.
 */
public class MainCharacter extends Character {
    private int score;

    public MainCharacter(model.boardUtilities.Position position) {
        super(position);
        this.score = 0;
    }

    public int getScore() {
        return score;
    }

    @Override
    public void move(Direction direction) {
        this.direction = direction;
        position = position.next(direction); // assumes Position has next(Direction)
    }

    @Override
    public boolean canMoveTo(Cell cell) {
        return cell.isEmpty(); // basic example
    }

    public void collectReward(Reward reward) {
        reward.applyTo(this);
    }

    public void applyPunishment(Punishment punishment) {
        punishment.applyTo(this);
    }

    public void addScore(int amount) {
        this.score += amount;
    }

    public void subtractScore(int amount) {
        this.score -= amount;
    }
}
