package kit;

import java.util.ArrayList;

public class Game {

    private String directory;
    private String name;
    private String steamId;
    private ArrayList<String> execs = new ArrayList<>();

    public Game(String directory)
    {
        this.directory = directory;
    }
    public String getDirectory() {
        return directory;
    }

    public ArrayList<String> getExecs()
    {
        return this.execs;
    }
}
