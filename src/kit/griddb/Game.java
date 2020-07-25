package kit.griddb;

import java.util.Date;

public class Game {

    private final int id;
    private final String name;
    private final boolean verified;
    private final String[] types;
    private final Date date;

    public Game(int id, String name, boolean verified, String[] types, Date date)
    {
        this.id = id;
        this.name = name;
        this.verified = verified;
        this.types = types;
        this.date = date;
    }


    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public int getId() {
        return id;
    }
}
