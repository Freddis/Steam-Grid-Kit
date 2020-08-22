package kit.griddb;

import javafx.util.Pair;
import kit.http.HttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class SteamGridDbClient {

    private final String apiKey;
    private final HttpClient http;
    private final String baseUrl = "https://www.steamgriddb.com/api/v2";

    public enum ImageType {
        GRID("grids"),
        HERO("heroes"),
        LOGO("logos"),
        d660x930("660x930");
        private final String value;

        ImageType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum Dimentions {
        d460x215("460x215"),
        d920x430("920x430"),
        d600x900("600x900"),
        d660x930("660x930"),
        d1920x620("1920x620"),
        d3840x1240("3840x1240"),
        d1600x650("1600x650");
        private final String value;

        Dimentions(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }


    public SteamGridDbClient(String apiKey, String userAgent) {
        this.apiKey = apiKey;
        List<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<>("Content-Type", "text/html"));
        headers.add(new Pair<>("Content-Language", "en-us"));
        headers.add(new Pair<>("User-Agent", userAgent));
        headers.add(new Pair<>("Authorization", "Bearer " + apiKey));
        this.http = new HttpClient();
        http.setHeaders(headers);
    }

    public void testApi(Consumer<Boolean> callback) {
        try {
            http.get(this.baseUrl + "/search/autocomplete/half", null, response -> {
                boolean status = response.getStatus() == 200;
                callback.accept(status);
            });
        } catch (Exception e) {
            callback.accept(false);
        }
    }

    public void findGameBySteamId(int steamId, Consumer<Game> callback) {

        http.get(this.baseUrl + "/games/steam/" + steamId, null, response -> {
            Game game = null;
            if (response.getStatus() == 200) {
                String json = response.getBody();
                JSONObject data;
                try {
                    data = new JSONObject(json);
                    JSONObject row = data.optJSONObject("data");
                    game = this.parseGame(row);
                } catch (Exception e) {
                    callback.accept(null);
                    return;
                }
            }
            callback.accept(game);
        });
    }

    public void findGames(String name, Consumer<List<Game>> callback) {
        http.get(this.baseUrl + "/search/autocomplete/" + name, null, response -> {
            ArrayList<Game> result = new ArrayList<>();
            if (response.getStatus() == 200) {
                String json = response.getBody();
                JSONObject data;
                try {
                    data = new JSONObject(json);
                    JSONArray rows = data.optJSONArray("data");
                    if (rows != null) {
                        for (int i = 0; i < rows.length(); i++) {
                            JSONObject row = rows.getJSONObject(i);
                            Game game = this.parseGame(row);
                            result.add(game);
                        }
                    }
                } catch (Exception e) {
                    callback.accept(result);
                    return;
                }
            }
            callback.accept(result);
        });
    }

    public void findHeroImages(int id, Dimentions dimentions, Consumer<List<GridImage>> callback) {
        List<Dimentions> list = new ArrayList<>();
        list.add(dimentions);
        findImages(id, ImageType.HERO, list, false, callback);
    }

    public void findLogoImages(int id, Consumer<List<GridImage>> callback) {
        List<Dimentions> list = new ArrayList<>();
        findImages(id, ImageType.LOGO, list, false, callback);
    }

    public void findGridImages(int id, Dimentions dimentions, Consumer<List<GridImage>> callback) {
        List<Dimentions> list = new ArrayList<>();
        list.add(dimentions);
        findImages(id, ImageType.GRID, list, false, callback);
    }

    public void findImages(int id, ImageType type, List<Dimentions> dimensions, boolean animatied, Consumer<List<GridImage>> callback) {
        ArrayList<Pair<String, String>> params = new ArrayList<>();
        String[] dimensionStrings = new String[dimensions.size()];
        Arrays.setAll(dimensionStrings, value -> dimensions.get(value).getValue());
        if (dimensions.size() > 0) {
            params.add(new Pair<>("dimensions", String.join(",", dimensionStrings)));
        }
        String url = this.baseUrl + "/" + type.getValue() + "/game/" + id;
        http.get(url, params, response -> {
            ArrayList<GridImage> result = new ArrayList<>();
            if (response.getStatus() == 200) {
                String json = response.getBody();
                JSONObject data;
                try {
                    data = new JSONObject(json);
                    JSONArray rows = data.optJSONArray("data");
                    if (rows != null) {
                        for (int i = 0; i < rows.length(); i++) {
                            JSONObject row = rows.getJSONObject(i);
                            GridImage image = this.parseGridImage(row);
                            result.add(image);
                        }
                    }
                } catch (Exception e) {
                    callback.accept(result);
                    return;
                }
            }
            callback.accept(result);
        });
    }

    private GridImage parseGridImage(JSONObject row) {
        int id = row.getInt("id");
        String url = row.getString("url");
        String author = null;
        JSONObject objAuthor = row.optJSONObject("author");
        if (objAuthor != null) {
            author = objAuthor.getString("name");
        }
        String style = row.getString("style");
        GridImage game = new GridImage(id, style, url, author);
        return game;
    }

    private Game parseGame(JSONObject row) {

        int id = row.getInt("id");
        String name = row.getString("name");
        boolean verified = row.getBoolean("verified");
        JSONArray typesArr = row.getJSONArray("types");
        String[] types = new String[typesArr.length()];
        for (int i = 0; i < typesArr.length(); i++) {
            types[i] = typesArr.getString(i);
        }
        int stamp = row.optInt("release_date", 0);
        Date date = stamp != 0 ? new Date((long) stamp * 1000) : null;
        Game game = new Game(id, name, verified, types, date);
        return game;
    }
}
