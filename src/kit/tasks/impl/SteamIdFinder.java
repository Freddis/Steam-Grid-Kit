package kit.tasks.impl;

import javafx.util.Pair;
import kit.Config;
import kit.State;
import kit.interfaces.ILogger;
import kit.models.Game;
import kit.models.SteamGame;
import kit.tasks.GameTask;
import kit.utils.JsonHelper;
import kit.utils.StringHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SteamIdFinder extends GameTask {

    private final JsonHelper helper;
    private ArrayList<SteamGame> steamLibrary;

    public SteamIdFinder(ILogger logger, JSONObject settings) {
        super(logger, settings);
        helper = new JsonHelper(logger);
    }

    @Override
    public void start(State state, Consumer<Double> tickCallback) {
        logger.log("Preparing to find steam ids");
        logger.log("Reading steam json file");
        JSONObject json = helper.readJsonFromFile(Config.getSteamLibraryJsonFilePath());
        if(json.isEmpty())
        {
            logger.log("No stream shop library, exiting");
            finishCallback.accept(false);
            return;
        }

        logger.log("Creating object representation");
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
        logger.log("Starting processing");
        super.start(state,tickCallback);
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
        String compareToName = game.getAltName() != null ? game.getAltName() :  game.getDirectory();
        if(game.getAltSteamId() != 0){
            logger.log("Game has alt id: "+ game.getAltSteamId());
        }
        for(SteamGame steamGame : steamLibrary)
        {
            double similarity = StringHelper.strippedSimilarity(compareToName, steamGame.getName());
            if(game.getAltSteamId() != 0 && game.getAltSteamId() == steamGame.getAppId()){
                similarity = 2;
            }
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
        List<Pair<Double, SteamGame>> best = list.subList(0, 15);
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
