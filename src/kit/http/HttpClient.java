package kit.http;

import javafx.util.Pair;
import kit.Config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HttpClient {

    private List<Pair<String, String>> headers = new ArrayList<>();

    public void  setHeaders(List<Pair<String, String>> headers)
    {
        this.headers = headers;
    }

    public void get(String url, List<Pair<String, String>> params, Consumer<Response> callback) {
        String urlWithParams = this.createUrl(url, params);
        Runnable getRequest = () -> {
            HttpURLConnection connection = null;
            int responseCode = 0;
            try {
                URL urlObj = new URL(urlWithParams);
                connection = (HttpURLConnection) urlObj.openConnection();
                connection.setRequestMethod("GET");
                for(Pair<String,String> header : headers)
                {
                    connection.setRequestProperty(header.getKey(),header.getValue());
                }

                //Get Response
                responseCode = connection.getResponseCode();
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();

                Response responseObject = new Response(responseCode, response.toString());
                callback.accept(responseObject);
            } catch (Exception e) {
                e.printStackTrace();
                callback.accept(new Response(responseCode,null));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        };

        Thread thread = new Thread(getRequest);
        thread.start();
    }

    private String createUrl(String url, List<Pair<String, String>> params) {
        if(params == null)
        {
            return url;
        }
        String[] lines = new String[params.size()];
        for(Pair<String,String> param : params)
        {
            try {
                String key = URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8.toString());
                String val = URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8.toString());
                lines[params.indexOf(param)] = key + "=" + val;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return url + "?" + String.join("&", lines);
    }
}
