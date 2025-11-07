package com.project.team6.enemy;

public class ChaserEnemy extends Enemy {

    public ChaserEnemy(int x, int y) {
        super(x, y);
    }

    @Override
    public void tick(char[][] grid, int playerX, int playerY) {
        int dx = Integer.compare(playerX, this.x); // -1,0,1
        int dy = Integer.compare(playerY, this.y);

        int absDx = Math.abs(playerX - this.x);
        int absDy = Math.abs(playerY - this.y);

        if (absDx >= absDy) {
            if (!tryStep(grid, this.x + dx, this.y)) {
                tryStep(grid, this.x, this.y + dy);
            }
        } else {
            if (!tryStep(grid, this.x, this.y + dy)) {
                tryStep(grid, this.x + dx, this.y);
            }
        }
    }

    private boolean tryStep(char[][] grid, int nx, int ny) {
        if (canStep(grid, nx, ny)) {
            this.x = nx;
            this.y = ny;
            return true;
        }
        return false;
    }
}
