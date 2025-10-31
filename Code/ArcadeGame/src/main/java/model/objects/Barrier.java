package model.objects;

public class Barrier {
    private boolean isWall;

    public Barrier() {
        isWall = false;
    }

    public void makeWall() {
        isWall = true;
    }

}
