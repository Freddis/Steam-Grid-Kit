package kit.tasks.impl;

import kit.Config;
import kit.models.Game;
import kit.tasks.GameTask;
import kit.utils.Logger;
import kit.utils.StringHelper;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

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
            logger.log("Can't read directory: " + gameDir.getAbsolutePath());
            return true;
        }

        ArrayList<File> execs = new ArrayList<>();
        try {
            this.searchForExecs(gameDir, execs);
        } catch (InterruptedException e) {
            logger.log("Interrupted really");
            return false;
        }

        String compareToName = game.getAltName() != null ? game.getAltName() : game.getDirectory();
        execs.sort((a, b) -> {
            double aResult = StringHelper.strippedSimilarity(compareToName, a.getName());
            double bResult = StringHelper.strippedSimilarity(compareToName, b.getName());
            if (aResult == bResult) {
                return 0;
            }
            return aResult > bResult ? -1 : 1;
        });

        String localPath = settings.optString(Config.Keys.LOCAL_GAMES_DIRECTORY_PATH.getKey());
        ArrayList<String> execPaths = new ArrayList<>();

        if (localPath != null) {
            execPaths.addAll(convertPathsToLocal(execs, localPath, folder.getAbsolutePath()));
        } else {
            for (File exec : execs) {
                execPaths.add(exec.getAbsolutePath());
            }
        }

        String currentExe = game.getExecs().size() > 0 ? game.getSelectedExe() : null;
        game.getExecs().clear();
        game.getExecs().addAll(execPaths);
        if (currentExe != null) {
            Optional<String> prevExe = Arrays.stream(game.getExecs().toArray(new String[0])).filter(path -> path.equals(currentExe)).findFirst();
            prevExe.ifPresent(game::setSelectedExe);
        }
        return true;
    }

    private ArrayList<String> convertPathsToLocal(ArrayList<File> execs, String localPath, String remotePath) {
        ArrayList<String> result = new ArrayList<>();
        for (File exe : execs) {
            String path = exe.getAbsolutePath().replace(remotePath, localPath).replace("/","\\");
            result.add(path);
        }
        return result;
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
            String extension = file.getAbsolutePath().substring(extensionPos).toLowerCase();
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
