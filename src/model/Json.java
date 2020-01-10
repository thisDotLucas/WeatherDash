package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class gets the jason data and makes them into TemperatureData objects and stores them
 * in an ArrayList. Api data is stored on uneven indexes and sensor data on even indexes.
 */
public class Json {

    private final static String START_DATE = "2019-05-31";

    private static ArrayList<TemperatureData> data = new ArrayList<>();


    public static ArrayList<TemperatureData> getTemperatureDataArrayList() {

        try {
            String url;
            url = "http://weatherdash-api.app.maxemiliang.cloud/sensor/all?token=SecretTokenHello&interval=1";
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
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject object = new JSONObject(response.toString());

            JSONArray jsonArray = object.getJSONArray("data");

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonLine = (JSONObject) jsonArray.get(i);

                data.add(new TemperatureData(jsonLine.get("timestamp").toString().replaceAll("T", ",").replaceAll("Z", ""), jsonLine.get("avg_temp").toString(), jsonLine.get("source_name").toString(), jsonLine.get("avg_humidity").toString()));

                if (jsonLine.get("timestamp").toString().substring(0, 10).equals(START_DATE))
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();

        }
        return data;
    }
}
