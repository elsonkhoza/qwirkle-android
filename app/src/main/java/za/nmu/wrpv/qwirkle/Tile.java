package za.nmu.wrpv.qwirkle;

import java.io.Serializable;

public class Tile implements Serializable {
    private static final long serialVersionUID = 2L;

   public int color, shape, placement,row,column;

    public Tile(int color, int shape) {
        this.color = color;
        this.shape = shape;
        this.placement = 1;

    }

    public void setPlacement(int placement) {
        this.placement = placement;
    }

    public void setPosition(int row, int column)
    {
        this.row = row;
        this.column = column;
    }
}
