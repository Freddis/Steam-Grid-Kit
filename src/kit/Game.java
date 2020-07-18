package kit;

public class Game {

    private String directory;
    private String name;
    private String steamId;

    public Game(String directory)
    {
        this.directory = directory;
    }

    public String getDirectory() {
        return directory;
    }
}
