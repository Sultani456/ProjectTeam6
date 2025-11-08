package com.project.team6.model.characters.enemies;

// This enemies always tries to move closer to the player.
public class MovingEnemy extends Enemy {

    // Start position of the enemies.
    public MovingEnemy(int x, int y) {
        super(x, y);
    }

    @Override
    public void tick(char[][] grid, int playerX, int playerY) {
        // dx and dy show the direction to the player: -1, 0, or 1.
        int dx = Integer.compare(playerX, this.x); // -1,0,1
        int dy = Integer.compare(playerY, this.y);

        // Distance on x and y. Used to decide which way to move first.
        int absDx = Math.abs(playerX - this.x);
        int absDy = Math.abs(playerY - this.y);

        // If we are farther (or equal) on x, try moving on x first.
        if (absDx >= absDy) {
            // Try step on x. If blocked, try step on y.
            if (!tryStep(grid, this.x + dx, this.y)) {
                tryStep(grid, this.x, this.y + dy);
            }
        } else {
            // If we are farther on y, try moving on y first.
            if (!tryStep(grid, this.x, this.y + dy)) {
                tryStep(grid, this.x + dx, this.y);
            }
        }
    }

    // Try to move to (nx, ny). Return true if the move works.
    private boolean tryStep(char[][] grid, int nx, int ny) {
        // Check if the next tile is walkable.
        if (canStep(grid, nx, ny)) {
            // Update the enemies position.
            this.x = nx;
            this.y = ny;
            return true;
        }
        // Could not move there.
        return false;
    }
}
