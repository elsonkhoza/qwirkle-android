package za.nmu.wrpv.messages;

import java.io.Serializable;
import java.util.Map;

public class Package implements Serializable {

    private static final long serialVersionUID = 1L;

    public String command;
    public Map<String,Object> data;

    public Package(String command, Map<String, Object> data) {
        this.command = command;
        this.data = data;
    }
}
