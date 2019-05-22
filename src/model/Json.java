package model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.jar.JarException;
import org.json.JSONArray;
import org.json.JSONObject;



public class Json {

    public static JSONObject object;

    public static Object[] data = new Object[2];


    public static Object[] getJson() throws JarException {
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
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            object = new JSONObject(response.toString());

                int diff = 0;
                int check = 2;
                int officialDataIndex = 0;
                int sensorDataIndex = 0;

                JSONArray jsonArray = object.getJSONArray("data");
                OfficialData[] officialDataArray = new OfficialData[(jsonArray.length() / 2)];
                SensorData[] sensorDataArray = new SensorData[(jsonArray.length() / 2) + 150];

               for(int i = 0; i < jsonArray.length(); i++) {

                   JSONObject jsonLine = jsonArray.getJSONObject(i);

                   if(jsonLine.get("source_name").toString().equals("nodemcu_1_turku") && (check == 1 || check == 2)){

                       SensorData x = new SensorData(jsonLine.get("timestamp").toString(), jsonLine.get("avg_temp").toString(), jsonLine.get("source_name").toString(), jsonLine.get("avg_humidity").toString());
                       sensorDataArray[sensorDataIndex] = x;
                       sensorDataIndex++;
                       check = 0;

                   } else if(jsonLine.get("source_name").toString().equals("external_api_turku") && (check == 0 || check == 2)){

                       jsonLine = jsonArray.getJSONObject(i - diff);

                       OfficialData y = new OfficialData(jsonLine.get("timestamp").toString(), jsonLine.get("avg_temp").toString(), jsonLine.get("source_name").toString(), jsonLine.get("avg_humidity").toString());
                       officialDataArray[officialDataIndex] = y;
                       officialDataIndex++;
                       check = 1;

                   } else {

                       diff = 2;
                       check = 2;

                   }

                }

                OfficialDataHandler x = new OfficialDataHandler(officialDataArray, officialDataIndex);
                SensorDataHandler y = new SensorDataHandler(sensorDataArray, sensorDataIndex);
                data[0] = x;
                data[1] = y;

                return data;

        } catch (Exception IOException) {
            System.out.println(IOException);
        }

        return data;

    }
}
