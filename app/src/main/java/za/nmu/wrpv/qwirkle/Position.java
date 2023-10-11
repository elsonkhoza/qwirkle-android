package za.nmu.wrpv.qwirkle;

import java.io.Serializable;

public class Position implements Serializable {
    private static final long serialVersionUID = 3L;


    public int row,column;

    public Position(int row, int column) {
        this.row = row;
        this.column = column;
    }
}
