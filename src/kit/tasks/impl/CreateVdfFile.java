package kit.tasks.impl;

import kit.Config;
import kit.State;
import kit.interfaces.ILogger;
import kit.interfaces.ITask;
import kit.models.Game;
import kit.utils.JsonHelper;
import kit.utils.Logger;
import kit.vdf.VdfWriter;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Consumer;

public class CreateVdfFile implements ITask {

    private final ILogger logger;
    private final JSONObject settings;
    private Consumer<Boolean> finishCallback;

    public CreateVdfFile(ILogger logger, JSONObject settings) {
        this.settings = settings;
        this.logger = logger;
        finishCallback = p -> {
        };
    }

    @Override
    public String getStatusString() {
        return "Creating new shortcuts file";
    }

    @Override
    public void onFinish(Consumer<Boolean> finishCallback) {
        this.finishCallback = finishCallback;
    }

    @Override
    public void start(State state, Consumer<Double> tickCallback) {
        try {
            logger.log("Creating new vdf file");
            if (!this.backUpExistingVdf()) {
                finishCallback.accept(false);
                return;
            }
            String content = this.createVdfContent();
            if (!writeContentToShortcutsFile(content)) {
                finishCallback.accept(false);
                return;
            }
            finishCallback.accept(true);
        } catch (Exception e) {
            logger.error("Error in creating vdf file",e);
            finishCallback.accept(false);
        }
    }

    private boolean writeContentToShortcutsFile(String content) {
        String filePath = settings.optString(Config.Keys.VDF_FILE.getKey(), null);
        File existingVdfFile = new File(filePath);
        if (existingVdfFile.exists() && !existingVdfFile.delete()) {
            logger.log("Cannot delete vdf file" + existingVdfFile.getAbsolutePath());
            return false;
        }

        try {
            existingVdfFile.createNewFile();
            FileOutputStream os = new FileOutputStream(existingVdfFile);
            byte[] bytes = content.getBytes();
            for (int i = 0; i < content.length(); i++) {
                os.write(content.charAt(i));
            }
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.log("Can't create  VDF file");
            return false;
        }
        return true;
    }

    private String createVdfContent() throws Exception {

        String filePath = settings.optString(Config.Keys.VDF_FILE.getKey(), null);
        File existingVdfFile = new File(filePath);

        JsonHelper helper = new JsonHelper(logger);
        ArrayList<Game> games = helper.toList(Game::new, settings.getJSONArray(Config.Keys.GAMES.getKey()));

        String gamesPath = settings.optString(Config.Keys.GAMES_DIRECTORY_PATH.getKey(),null);
        if(gamesPath == null){
            throw new Exception("Game path has to be set.");
        }

        VdfWriter writer = new VdfWriter();
        String imageDir = this.getSteamImageDirPath(existingVdfFile);
        for (Game game : games) {
            File headerFile = game.getHeaderImageFile();
            String ext = headerFile != null && headerFile.getAbsolutePath().contains(".png") ? ".png" : ".jpg";
            String imagePath = imageDir + "\\" + game.getId() + "header.jpg";
            if(!game.isReadyToExport()){
                imagePath = "";
            }
            String fullGameExePath = game.getSelectedExe();
            writer.addLine(game.getAppId(),game.getIntendedTitle(), fullGameExePath, imagePath, game.getVdf());

        }
        return writer.getVdfContent();
    }

    private String getSteamImageDirPath(File existingVdfFile) {

        String local = settings.optString(Config.Keys.LOCAL_VDF_PATH.getKey(), null);
        String existingFile = local != null ? local : existingVdfFile.getAbsolutePath();
        String[] parts = existingFile.split("\\\\");
        String dir = String.join("\\", Arrays.copyOf(parts, parts.length - 1)) + "\\grid";
        return dir;
    }

    private boolean backUpExistingVdf() {
        logger.log("Backing up");
        File dir = new File(Config.getVdfBackupFolder());
        if (!dir.exists()) {
            boolean created = dir.mkdir();
            if (!created) {
                logger.log("Can't create dir: " + dir.getAbsolutePath());
                return false;
            }
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        String date = format.format(now);
        String bakName = now.getTime() + "_" + date + ".vdf";
        File bakFile = new File(dir, bakName);

        File currentVdf = new File(Config.getVdfFilePath());

        String filePath = settings.optString(Config.Keys.VDF_FILE.getKey(), null);
        File existingVdfFile = new File(filePath);
        logger.log("Trying to backup vdf from " + filePath);

        if (!existingVdfFile.exists()) {
            logger.log("Steam Vdf doesn't exist");
            return true;
        }

        try {
            Files.copy(existingVdfFile.toPath(), bakFile.toPath());
        } catch (IOException e) {
            logger.log("Unable to copy file");
            return false;
        }
        return true;
    }

    @Override
    public void kill() {
        //no skip, it's quick
    }
}
