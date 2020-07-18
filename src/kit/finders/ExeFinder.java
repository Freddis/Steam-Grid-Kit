package kit.finders;

import kit.Game;
import kit.ProgressedTask;
import kit.utils.Logger;

import java.io.File;
import java.util.List;

public class ExeFinder extends ProgressedTask<Game> {

    private final List<Game> games;
    private final File folder;

    public ExeFinder(Logger logger, List<Game> games, File gamesFolder)
    {
        super(logger);
        this.games = games;
        this.folder = gamesFolder;
    }

    @Override
    protected void process(Game o) {

    }

    @Override
    protected List<Game> getList() {
        return games;
    }
}
