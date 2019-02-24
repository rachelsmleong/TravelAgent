/**
 * Execution: Fetches the GPS coordinates using the Google API 
 * Note: Not the best way to calculate because the driving distance is not 
 * displacement between 2 points
 * 
 */

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.stream.Collectors;

public class fetchGPS {
    private double lat;
    private double lng;

    fetchGPS(String location, String API_KEY) throws IOException {

        URL url = new URL(parseToURL(location, API_KEY));
        InputStream is = url.openConnection().getInputStream();
        BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
        String returnJSON = buffer.lines().collect(Collectors.joining("\n"));

        JsonObject loc = new JsonParser().parse(returnJSON).
                getAsJsonObject().
                getAsJsonArray("results").get(0).
                getAsJsonObject().getAsJsonObject("geometry").
                getAsJsonObject("location");

        lat = loc.get("lat").getAsDouble();
        lng = loc.get("lng").getAsDouble();

        // Sample query URL
        // https://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&key=YOUR_API_KEY/


    }

    /**
     * Parses location, API key into a query URL
     *
     * @param location
     * @param API_KEY
     * @return
     * @throws UnsupportedEncodingException
     */
    private String parseToURL(String location, String API_KEY) throws UnsupportedEncodingException {
        String originEncoded = URLEncoder.encode(location, "UTF-8");

        return "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                originEncoded + "&key=" + API_KEY;

    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
