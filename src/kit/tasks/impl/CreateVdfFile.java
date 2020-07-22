package kit.tasks.impl;

import kit.Config;
import kit.interfaces.ITask;
import kit.utils.Logger;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

public class CreateVdfFile implements ITask {

    private final Logger logger;
    private final JSONObject settings;
    private Consumer<Boolean> finishCallback;

    public CreateVdfFile(Logger logger, JSONObject settings) {
        this.settings = settings;
        this.logger = logger;
        finishCallback = p -> {
        };
    }

    @Override
    public String getStatusString() {
        return "Creating new shortucts file";
    }

    @Override
    public void onFinish(Consumer<Boolean> finishCallback) {
        this.finishCallback = finishCallback;
    }

    @Override
    public void start(Consumer<Double> tickCallback) {
        logger.log("Creating new vdf file");
        if(!this.backUpExistingVdf())
        {
            finishCallback.accept(false);
            return;
        }

        String content = this.createVdfContent();
        if(!writeContentToShortcutsFile(content))
        {
            finishCallback.accept(false);
            return;
        }
        finishCallback.accept(true);
    }

    private boolean writeContentToShortcutsFile(String content) {
        String filePath = settings.optString(Config.Keys.VDF_FILE.getKey(), null);
        File existingVdfFile = new File(filePath);
        if(existingVdfFile.exists() && !existingVdfFile.delete())
        {
            logger.log("Cannot delete vdf file" + existingVdfFile.getAbsolutePath());
            return false;
        }


        try {
            existingVdfFile.createNewFile();
            PrintWriter out = new PrintWriter(existingVdfFile);
            out.write(content);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.log("Can't create  VDF file");
            return false;
        }
        return true;
    }

    private String createVdfContent() {
        return "dasdsadasd";
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
