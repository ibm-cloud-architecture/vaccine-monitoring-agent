package ibm.gse.eda.vaccine.coldchainagent.test;

import java.io.*;
import java.net.MalformedURLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ValidateWMLCall {
    public static void main(String[] args) throws IOException {
        // NOTE: you must construct iam_token based on provided documentation
        String iam_token = "Bearer " + System.getenv("WML_TOKEN");
        HttpURLConnection scoringConnection = null;
        BufferedReader scoringBuffer = null;
        try {
            // Scoring request
            URL scoringUrl = new URL(System.getenv("ANOMALY_DETECTION_URL"));
            System.out.println(("send to " + scoringUrl.toString()));
            System.out.println(("token  " + iam_token));
            scoringConnection = (HttpURLConnection) scoringUrl.openConnection();
            scoringConnection.setDoInput(true);
            scoringConnection.setDoOutput(true);
            scoringConnection.setRequestMethod("POST");
            scoringConnection.setRequestProperty("Accept", "application/json");
            scoringConnection.setRequestProperty("Authorization", iam_token);
            scoringConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            OutputStreamWriter writer = new OutputStreamWriter(scoringConnection.getOutputStream(), "UTF-8");
            // NOTE: manually define and pass the array(s) of values to be scored in the
            // next line
            String payload = "{\"input_data\": [{\"fields\": [\"temperature\",\"ambiant_temperature\",\"kilowatts\",\"oxygen_level\",\"nitrogen_level\",\"humidity_level\",\"fan_1\",\"vent_2\"], \"values\": [[4.73,19.048,4.505,1,3,76.98,40.153,4.181]}]}";


System.out.println(("payload  " + payload));
            writer.write(payload);
            writer.close();
            scoringBuffer = new BufferedReader(new InputStreamReader(scoringConnection.getInputStream()));
            StringBuffer jsonStringScoring = new StringBuffer();
            String lineScoring;
            while ((lineScoring = scoringBuffer.readLine()) != null) {
                jsonStringScoring.append(lineScoring);
            }
            System.out.println(jsonStringScoring);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            if (scoringConnection != null) {
                scoringConnection.disconnect();
            }
            if (scoringBuffer != null) {
                scoringBuffer.close();
            }
        }
    }
}
