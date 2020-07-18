package kit.finders;

import kit.Game;
import kit.ProgressedTask;
import kit.utils.Logger;
import kit.utils.StringComparator;

import java.io.File;
import java.util.*;

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
    protected boolean process(Game game) {
        File gameDir = new File(folder,game.getDirectory());
        if(!gameDir.canRead())
        {
            this.error("Can't read directory");
            return false;
        }
        ArrayList<File> execs = new ArrayList<>();
        this.searchForExecs(gameDir,execs);
        ArrayList<File> clean = this.cleanupExecs(execs);

        clean.sort((a, b) -> {
            double aResult = StringComparator.similarity(game.getDirectory(),a.getName());
            double bResult = StringComparator.similarity(game.getDirectory(),b.getName());
            return aResult > bResult ? -1 : 1;
        });
        game.getExecs().clear();
        for(int i = 0; i < clean.size(); i++)
        {
            String path = clean.get(i).getAbsolutePath();
            game.getExecs().add(path);
        }

        return true;
    }

    private ArrayList<File> cleanupExecs(ArrayList<File> execs)
    {
        String[] excluded = new String[] {"launcher", "unins", "handler", "lngs"};
        ArrayList<File> filtered = new ArrayList<File>();
        for (int i =0; i < execs.size(); i++)
        {
            boolean skip = false;
            for(int j =0; j < excluded.length; j++)
            {
                int index = execs.get(i).getName().indexOf(excluded[j]);
                if(index != -1)
                {
                    this.logger.log("Excluded  " + execs.get(i).getName());
                    skip = true;
                    break;
                }
            }
            if(skip)
            {
                continue;
            }
            filtered.add(execs.get(i));
        }
        return filtered;
    }

    private void searchForExecs(File dir, ArrayList<File> execs) {
        File[] files = dir.listFiles();
        logger.log("Processing " + dir.getAbsolutePath());
        Queue<File> dirs = new LinkedList<>();
        for(int i = 0; i < files.length; i++)
        {
            if(files[i].isDirectory())
            {
                dirs.add(files[i]);
                continue;
            }
            int extensionPos = files[i].getAbsolutePath().lastIndexOf(".");
            if(extensionPos == -1)
            {
                continue;
            }
            String extension = files[i].getAbsolutePath().substring(extensionPos);
            if(extension.equals(".exe"))
            {
                execs.add(files[i]);
            }
        }
//        while(execs.size() == 0 && dirs.peek() != null)
        while(dirs.peek() != null)
        {
            searchForExecs(dirs.poll(),execs);
        }
    }

    @Override
    protected List<Game> getList() {
        return games;
    }
}
