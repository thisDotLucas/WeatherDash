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

            //if (true) {

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

                       SensorData x = new SensorData(jsonLine.get("timestamp").toString(), jsonLine.get("avg_temp").toString(), jsonLine.get("source_name").toString());
                       sensorDataArray[sensorDataIndex] = x;
                       sensorDataIndex++;
                       check = 0;

                   } else if(jsonLine.get("source_name").toString().equals("external_api_turku") && (check == 0 || check == 2)){

                       jsonLine = jsonArray.getJSONObject(i - diff);

                       OfficialData y = new OfficialData(jsonLine.get("timestamp").toString(), jsonLine.get("avg_temp").toString(), jsonLine.get("source_name").toString());
                       officialDataArray[officialDataIndex] = y;
                       officialDataIndex++;
                       check = 1;

                   } else { //yoink yoink

                       diff = 2;
                       check = 2;

                       //int diff = 1;

                       //while(true) {
                         //  jsonLine = jsonArray.getJSONObject(i - diff);
                           //if(jsonLine.get("source_name").equals("external_api_turku"))
                             //  break;
                           //diff++;
                       //}

                       //SensorData x = new SensorData(jsonLine.get("timestamp").toString(), jsonLine.get("avg_temp").toString(), jsonLine.get("source_name").toString());
                       //x.setSourceName("XXXnodemcu_1_turkuXXX");
                       //sensorDataArray[sensorDataIndex] = x;
                       //sensorDataIndex++;

                   //} else {

                     //  int diff = 1;

                       //while(true) {
                         //  jsonLine = jsonArray.getJSONObject(i - diff);
                           ////  break;
                           //diff++;
                       //}

                       //OfficialData y = new OfficialData(jsonLine.get("timestamp").toString(), jsonLine.get("avg_temp").toString(), jsonLine.get("source_name").toString());
                       //y.setSourceName("XXXexternal_api_turkuXXX");
                       //officialDataArray[officialDataIndex] = y;
                       //officialDataIndex++;



                   }

                }

                OfficialDataHandler x = new OfficialDataHandler(officialDataArray);
                SensorDataHandler y = new SensorDataHandler(sensorDataArray);
                data[0] = x;
                data[1] = y;

                int counter = 0;
                while (officialDataArray[counter + 1] != null){

                    System.out.println(officialDataArray[counter].getSourceName());
                    System.out.println(officialDataArray[counter].getTimeStamp());
                    System.out.println(officialDataArray[counter].getTemperature());
                    System.out.println();
                    System.out.println(sensorDataArray[counter].getSourceName());
                    System.out.println(sensorDataArray[counter].getTimeStamp());
                    System.out.println(sensorDataArray[counter].getTemperature());
                    System.out.println();
                    counter++;

                }

                return data;

           // } else {
             //   throw new JSONException("AlphaVantage API Limit reached");
            //}

        } catch (Exception IOException) {
            System.out.println(IOException);
        }

        return data;

    }
}
