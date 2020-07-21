package kit.tasks.impl;

import javafx.util.Pair;
import kit.Config;
import kit.models.Game;
import kit.models.SteamGame;
import kit.tasks.GameTask;
import kit.utils.JsonHelper;
import kit.utils.Logger;
import kit.utils.StringHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SteamIdFinder extends GameTask {

    private final JsonHelper helper;
    private ArrayList<SteamGame> steamLibrary;

    public SteamIdFinder(Logger logger, JSONObject settings) {
        super(logger, settings);
        helper = new JsonHelper(logger);
    }

    @Override
    public void start(Consumer<Double> tickCallback) {
        JSONObject json = helper.readJsonFromFile(Config.getSteamLibraryJsonFilePath());
        JSONArray list = json.optJSONObject("applist").optJSONArray("apps");
        steamLibrary = new ArrayList<>(list.length());
        for(int i =0; i < list.length(); i++) {
            JSONObject row = list.getJSONObject(i);
            SteamGame game = new SteamGame(row);
            steamLibrary.add(game);
        }
        if(steamLibrary.size() == 0)
        {
            logger.log("Steam games not found or corrupted.");
            finishCallback.accept(false);
            return;
        }
        super.start(tickCallback);
    }

    @Override
    protected boolean processGame(Game game) {
        logger.log("Processing " + game.getDirectory());
        if(steamLibrary.size() <= 0)
        {
            logger.log("Not enough steam games");
            return false;
        }
        if(this.useCache && game.getFoundSteamGames().size() > 0)
        {
             logger.log("Using cached data");
             return true;
        }
        ArrayList<Pair<Double,SteamGame>> list = new ArrayList<>();
        for(SteamGame steamGame : steamLibrary)
        {
            double similarity = StringHelper.strippedSimilarity(game.getDirectory(), steamGame.getName());
            list.add(new Pair<>(similarity,steamGame));
        }
        list.sort((o1, o2) -> {
            if(o1.getKey().doubleValue() == o2.getKey().doubleValue())
            {
                return 0;
            }
            return o1.getKey() > o2.getKey() ? -1 : 1;
        });
        logger.log("Best matches:");
        List<Pair<Double, SteamGame>> best = list.subList(0, 10);
        game.getFoundSteamGames().clear();
        best.forEach(doubleSteamGamePair -> {
            game.getFoundSteamGames().add(doubleSteamGamePair.getValue());
            String rating = doubleSteamGamePair.getKey().toString();
            String msg = doubleSteamGamePair.getValue().getName() + "(" + doubleSteamGamePair.getValue().getAppId() + "): " + rating.substring(0,Math.min(4,rating.length()));
            logger.log(msg);
        });
        return true;
    }

    @Override
    public String getStatusString() {
        return "Getting Steam IDs";
    }
}
