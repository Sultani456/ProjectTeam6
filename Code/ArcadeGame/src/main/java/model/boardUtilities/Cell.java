package model.boardUtilities;

import model.objects.Barrier;
import model.objects.GameObject;

public class Cell {
    GameObject gameObject;  // punishment or reward

    Barrier north;
    Barrier east;
    Barrier south;
    Barrier west;

    public Cell(GameObject gameObject) {
        this.gameObject = gameObject;
        Barrier north = new Barrier();
        Barrier east = new Barrier();
        Barrier south = new Barrier();
        Barrier west = new Barrier();

        north.makeWall();
    }
}
