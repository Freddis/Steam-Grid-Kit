package kit.tasks.impl;

import kit.Config;
import kit.models.Game;
import kit.tasks.GameTask;
import kit.utils.Logger;
import kit.vdf.VdfKey;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

public class ExistingImagesFinder extends GameTask {

    private File[] imageFiles;

    public ExistingImagesFinder(Logger logger, JSONObject settings) {
        super(logger, settings);
    }

    @Override
    public void start(Consumer<Double> tickCallback) {
        File file = new File(Config.getImageDirectory());
        if(!file.exists() && !file.mkdir())
        {
            logger.log("Cannot create image dir "+file.getAbsolutePath());
            finishCallback.accept(false);
            return;
        }

        String filePath = settings.optString(Config.Keys.VDF_FILE.getKey(), null);
        File existingVdfFile = new File(filePath);
        File imageDir = new File(existingVdfFile.getParent(),"grid");
        if(!imageDir.exists() || !imageDir.isDirectory() && !imageDir.canRead())
        {
            logger.log("Cannot find or read directory: " + imageDir.getAbsolutePath());
            finishCallback.accept(false);
            return;
        }
        imageFiles = imageDir.listFiles();

        super.start(tickCallback);
    }

    @Override
    protected boolean processGame(Game game) {
        logger.log("Processing " + game.getIntendedTitle());
        if(!game.hasVdf())
        {
            logger.log("Game is not already added to steam, skipping since there's no possible images yet: " + game.getIntendedTitle());
            return true;
        }

        File imageDir = new File(Config.getImageDirectory(),game.getImageDirectoryName());
        if(!imageDir.exists() && !imageDir.mkdir())
        {
            logger.log("Cannot create image dir "+imageDir.getAbsolutePath());
            return false;
        }

        String id = game.getId();
        String path = game.getVdf().optString(VdfKey.ICON.getKey(), "");
        if(!path.isEmpty())
        {
            logger.log("Trying to get icon: "+ path);
            File icon = new File(path);
            if(icon.exists() && icon.canRead())
            {
                String extension = icon.getName().contains(".jpg") ? ".jpg" : ".png";
                File newFile = new File(imageDir, "setheader." + extension );
                if(newFile.exists())
                {
                    logger.log("File exists, skipping: "+newFile.getAbsolutePath());
                }
                else {
                    try {
                        Files.copy(icon.toPath(),newFile.toPath());
                    } catch (IOException e) {
                        logger.log("Can't copy "+icon.getAbsolutePath());
                    }
                }
            }
            else {
                logger.log("Can't read file "+icon.getAbsolutePath());
            }
        }

        String[] imageTypes = {"p","_hero","_logo","header"};
        String[] imageToTypes = {"setcover","setbackground","setlogo","setheader"};
        int i = 0;
        for(String type : imageTypes) {
            String filename = id + type;
            for (File file : imageFiles) {
                if(file.getName().indexOf(filename) != 0)
                {
                    continue;
                }

                String ext = file.getAbsolutePath().contains(".png") ? ".png" : ".jpg";
                String newName = imageToTypes[i] + ext;
                File newFile = new File(imageDir, newName);
                if(newFile.exists())
                {
                    logger.log("File exists, skipping: "+newFile.getAbsolutePath());
                    break;
                }
                try {
                    Files.copy(file.toPath(),newFile.toPath());
                } catch (IOException e) {
                    logger.log("Can't copy "+file.getAbsolutePath());
                }
                break;
            }
            i++;
        }

        return true;
    }

    @Override
    public String getStatusString() {
        return "Transferring existing images";
    }
}
