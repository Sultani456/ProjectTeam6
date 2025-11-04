package model.objects;

import model.boardUtilities.Position;

public class Barrier extends GameObject {
    private boolean isWall;

    public Barrier(Position position) {
        super(position);   // calls GameObject constructor to set position
        this.isWall = true;
    }

    public boolean isWall() {
        return isWall;
    }

    @Override
    public String toString() {
        return "Barrier at " + position;
    }
}


