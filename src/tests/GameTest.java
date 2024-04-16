package tests;

import kit.models.Game;
import kit.models.SteamGame;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import tests.utils.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameTest {
    @Test
    void gameJsonSpecialSymbolsReversible(){
        Game nier = new Game("Nier Automata");
        JSONObject nierJson = new JSONObject();
        nierJson.put("name","NieR:Automata™");
        nierJson.put("appid",524220);
        SteamGame nierSteam = new SteamGame(nierJson);
        nier.getFoundSteamGames().add(nierSteam);
        nier.setSelectedSteamGame(nierSteam);

        Game nier2 = new Game(nier.toJson());

        String diff = TestUtils.getStringDiff(nier.toJson().toString(),nier2.toJson().toString());
        assertEquals("",diff,"The Json of games is not identical");
        assertEquals(nier.toJson().toString(), nier2.toJson().toString(),"The Json of games is not identical");

    }
}
