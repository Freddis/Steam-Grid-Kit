package kit.tasks.impl;

import kit.Config;
import kit.models.Game;
import kit.tasks.GameTask;
import kit.utils.Logger;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SteamImageLoader extends GameTask {

    public SteamImageLoader(Logger logger, JSONObject settings) {
        super(logger, settings);
    }

    @Override
    protected boolean processGame(Game game) {
        if (game.getSelectedSteamGame() == null) {
            logger.log("Steam id not found, skipping");
            return true;
        }
        File imageDir = new File(Config.getImageDirectory());
        if (!imageDir.exists()) {
            logger.log("Attempting to create image dir: " + imageDir.getAbsolutePath());
            boolean result = imageDir.mkdir();
            if (!result) {
                logger.log("Could not create directory");
                return false;
            }
        }

        String gameImageDir = game.getImageDirectoryName();
        File gameDir = new File(imageDir, gameImageDir);
        if (!gameDir.exists()) {
            logger.log("Attempting to create game dir: " + gameDir.getAbsolutePath());
            boolean result = gameDir.mkdir();
            if (!result) {
                logger.log("Could not create directory" );
                return false;
            }
        }

        String[] imageNames = new String[]{"header.jpg", "library_600x900.jpg", "library_hero.jpg", "logo.png"};
        for (String imageName : imageNames) {
            String path = "https://steamcdn-a.akamaihd.net/steam/apps/" + game.getSelectedSteamGame().getAppId() + "/" + imageName;
            logger.log("Loading " + path);
            File file = new File(gameDir, imageName);
            if (file.exists() && !file.delete()) {
                logger.log("Can't remove old file " + file.getAbsolutePath());
                return false;
            }
            if (file.canWrite()) {
                logger.log("File " + file.getAbsolutePath() + " is not writable");
                return false;
            }
            try {
                logger.log("Writing to "+ file.getAbsolutePath());
                URL url = new URL(path);
                HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
                BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());
                FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
                int bufferSize = 1024;
                BufferedOutputStream bout = new BufferedOutputStream(fos, bufferSize);
                byte[] data = new byte[bufferSize];
                int x;
                while ((x = in.read(data, 0, bufferSize)) >= 0) {
                    bout.write(data, 0, x);
                }
                bout.close();
                in.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                logger.log("Cannot create url");
                return false;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                logger.log("File not found create url");
                continue;
            } catch (IOException e) {
                e.printStackTrace();
                logger.log("Can't write to file");
            }
        }
        return true;
    }

    @Override
    public String getStatusString() {
        return "Loading Steam Images";
    }
}
