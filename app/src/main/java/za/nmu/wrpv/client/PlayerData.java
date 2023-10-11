package za.nmu.wrpv.client;

import java.io.Serializable;

public class PlayerData implements Serializable {

    private static final long serialVersionUID = 12L;


    public String name;
   public int score;

    public PlayerData(String name, int score) {
        this.name = name;
        this.score = score;
    }
}
