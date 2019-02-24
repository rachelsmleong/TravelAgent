/**
 * Execution: Fetches the driving distance between 2 points
 */

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.stream.Collectors;

public class fetchDistanceAPI {


    private int distance;
    private int duration;

    /**
     * Constructor, it also fetches the distance between origin and destination.
     * This class uses Google's JSON parsing library (GSON).
     * @param origin
     * @param destination
     * @param API_KEY Google API Key
     * @throws IOException
     */
    fetchDistanceAPI(String origin, String destination, String API_KEY) throws IOException {

        URL url = new URL(parseToURL(origin, destination, API_KEY));
        InputStream is = url.openConnection().getInputStream();
        BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
        String returnJSON = buffer.lines().collect(Collectors.joining("\n"));

        JsonObject data = new JsonParser().parse(returnJSON).getAsJsonObject().
                getAsJsonArray("rows").get(0).getAsJsonObject().
                get("elements").getAsJsonArray().get(0).getAsJsonObject();

        distance = data.get("distance").getAsJsonObject().get("value").getAsInt();
        duration = data.get("duration").getAsJsonObject().get("value").getAsInt();
    }

    public int getDistance() {
        return distance;
    }

    public int getDuration() {
        return duration;
    }

    /**
     * Parses origin, destination, API key into a query URL
     * @param origin
     * @param destination
     * @param API_KEY
     * @return
     * @throws UnsupportedEncodingException
     */
    private String parseToURL(String origin, String destination, String API_KEY) 
        throws UnsupportedEncodingException {
        String originEncoded = URLEncoder.encode(origin, "UTF-8");
        String destinationEncoded = URLEncoder.encode(destination, "UTF-8");

        return "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=" +
                originEncoded + "&destinations=" + destinationEncoded + "&key=" + API_KEY;

    }

}
