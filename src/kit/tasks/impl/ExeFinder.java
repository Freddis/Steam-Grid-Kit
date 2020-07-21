package kit.tasks.impl;

import kit.Config;
import kit.models.Game;
import kit.tasks.GameTask;
import kit.utils.Logger;
import kit.utils.StringHelper;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class ExeFinder extends GameTask {

    private final File folder;

    public ExeFinder(Logger logger, JSONObject settings) {
        super(logger, settings);
        folder = new File(settings.optString(Config.Keys.GAMES_DIRECTORY_PATH.getKey()));
    }

    @Override
    protected boolean processGame(Game game) {

        if (game.getExecs().size() > 0 && this.useCache) {
            logger.log("Using cached data for " + game.getDirectory());
            return true;
        }

        File gameDir = new File(folder, game.getDirectory());
        if (!gameDir.canRead()) {
            logger.log("Can't read directory");
            return false;
        }
        ArrayList<File> execs = new ArrayList<>();

        try {
            this.searchForExecs(gameDir, execs);
        } catch (InterruptedException e) {
            logger.log("Interrupted really");
            return false;
        }

        ArrayList<File> clean = this.cleanupExecs(execs);

        String compareToName = game.getAltName() != null ? game.getAltName() : game.getDirectory();
        clean.sort((a, b) -> {
            double aResult = StringHelper.strippedSimilarity(compareToName, a.getName());
            double bResult = StringHelper.strippedSimilarity(compareToName, b.getName());
            if (aResult == bResult) {
                return 0;
            }
            return aResult > bResult ? -1 : 1;
        });
        game.getExecs().clear();
        for (File file : clean) {
            String path = file.getAbsolutePath();
            game.getExecs().add(path);
        }
        return true;
    }

    private ArrayList<File> cleanupExecs(ArrayList<File> execs) {
        String[] excluded = new String[]{"launcher", "unins", "handler", "lngs"};
        ArrayList<File> filtered = new ArrayList<>();
        for (File exec : execs) {
            boolean skip = false;
            for (String s : excluded) {
                int index = exec.getName().indexOf(s);
                if (index != -1) {
                    this.logger.log("Excluded  " + exec.getName());
                    skip = true;
                    break;
                }
            }
            if (skip) {
                continue;
            }
            filtered.add(exec);
        }
        return filtered;
    }

    private void searchForExecs(File dir, ArrayList<File> execs) throws InterruptedException {
        //Passing control
        Thread.sleep(0);
        File[] files = dir.listFiles();
        logger.log("Processing " + dir.getAbsolutePath());
        Queue<File> dirs = new LinkedList<>();
        for (File file : Objects.requireNonNull(files)) {
            if (file.isDirectory()) {
                dirs.add(file);
                continue;
            }
            int extensionPos = file.getAbsolutePath().lastIndexOf(".");
            if (extensionPos == -1) {
                continue;
            }
            String extension = file.getAbsolutePath().substring(extensionPos);
            if (extension.equals(".exe")) {
                execs.add(file);
            }
        }
        while (dirs.peek() != null) {
            searchForExecs(dirs.poll(), execs);
        }
    }

    @Override
    public String getStatusString() {
        return "Searching for executables";
    }
}
