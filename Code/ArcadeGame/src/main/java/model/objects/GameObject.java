package model.objects;

import model.boardUtilities.Position;

public abstract class GameObject {
    protected Position position;

    public GameObject(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}

