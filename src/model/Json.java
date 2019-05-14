package model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.jar.JarException;
import org.json.JSONObject;


public class Json {

    public static JSONObject object;

    public static String getJson() throws JarException {
        try {
            String url;
            url = "http://weatherdash-api.app.maxemiliang.cloud/sensor?token=SecretTokenHello";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            object = new JSONObject(response.toString());

        } catch (Exception IOException) {

        }

        return object.toString();

    }
}
