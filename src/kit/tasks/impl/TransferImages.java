package kit.tasks.impl;

import kit.Config;
import kit.models.Game;
import kit.tasks.GameTask;
import kit.utils.Logger;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.function.Consumer;

public class TransferImages extends GameTask {

    private File steamImageDir;

    public TransferImages(Logger logger, JSONObject settings) {
        super(logger, settings);
    }


    @Override
    public void start(Consumer<Double> tickCallback) {
        String filePath = settings.optString(Config.Keys.VDF_FILE.getKey(), null);
        File existingVdfFile = new File(filePath);
        File imageDir = new File(existingVdfFile.getParent(), "grid");
        if (!imageDir.exists() && !imageDir.mkdir()) {
            logger.log("ERROR: Cannot find or create directory: " + imageDir.getAbsolutePath());
            finishCallback.accept(false);
            return;
        }
        this.steamImageDir = imageDir;
        super.start(tickCallback);
    }

    @Override
    protected boolean processGame(Game game) {
        logger.log("Processing " + game.getDirectory());
        File imageDir = new File(Config.getImageDirectory(), game.getImageDirectoryName());
        if (!imageDir.exists()) {
            logger.log("Directory doesn't exist skipping " + imageDir.getAbsolutePath());
            return true;
        }
        ArrayList<File> files = new ArrayList<>();
        files.add(game.getHeaderImageFile());
        files.add(game.getCoverImageFile());
        files.add(game.getBackgroundImageFile());
        files.add(game.getLogoImageFile());

        String[] imageTypes = {"header", "p", "_hero", "_logo"};
        for (int i = 0; i < imageTypes.length; i++) {
            File currentFile = files.get(i);
            logger.log("image " + imageTypes[i]);
            if (currentFile == null) {
                logger.log("File not found, skipping ");
                continue;
            }

            String ext = currentFile.getAbsolutePath().contains(".png") ? ".png" : ".jpg";
            String newFileName = game.getId() + imageTypes[i] + ext;
            File toFile = new File(steamImageDir, newFileName);
            boolean deleted = false;
            try {
                if (toFile.exists()) {
                    deleted = toFile.delete();
                }
                Files.copy(currentFile.toPath(), toFile.toPath());
            } catch (IOException e) {
                logger.log("Can't copy " + currentFile.getAbsolutePath());
            }
        }
        return true;

    }

    @Override
    public String getStatusString() {
        return "Transferring Images";
    }
}
