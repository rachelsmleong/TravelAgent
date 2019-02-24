/**
 * Execution: Class opens file, reads it and populates it into list
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class databaseManager {
    private final String API_KEY;
    private LinkedList<Point> fromFile;
    private LinkedList<Point> sorted;
    private String filename;
    
    
    public databaseManager(String filename, String API_KEY) throws IOException {
        this.filename = filename;
        this.API_KEY = API_KEY;
        populateFileToList(filename);
    }
    
    public static void main(String args[]) throws IOException {
        databaseManager test = new databaseManager("currentDatabase.txt", 
                                                   ""); // insert API_KEY here
        test.updateDatabase("./src/currentDatabase.txt");
    }
    
    public void updateDatabase(String filename) throws IOException {
        sortList();
        fetchDistance(sorted);
        writeToFile(toString(sorted), filename);
    }
    
    /**
     * Reads and populates file to list. Method also fetches GPS coordinates for 
     * specified location if it is not defined in the file.
     *
     * @param filename name of file/
     * @throws IOException if location name in file is not legal.
     */
    public void populateFileToList(String filename) throws IOException {
        fromFile = new LinkedList<>();
        int numberOfLocations;
        In read = new In(filename);
        numberOfLocations = read.readInt();
        read.readLine();
        for (int i = 0; i < numberOfLocations; i++) {
            String locationName = "";
            double lat;
            double lng;
            double time;
            double budget;
            double distanceToNext = 0;
            double durationToNext = 0;
            String currentLine = read.readLine();
            
            //read locations
            String[] readLocationName = currentLine.split("\"");
            if (readLocationName.length > 1) locationName = readLocationName[1];
            
            //read gps
            String[] readGPSCoordinates = currentLine.split("_");
            if (readGPSCoordinates.length > 1) {
                lat = Double.parseDouble(readGPSCoordinates[1]);
                lng = Double.parseDouble(readGPSCoordinates[2]);
            } else {
                System.out.println("Fetching GPS Coordinates for " + locationName);
                fetchGPS fetch = new fetchGPS(locationName, API_KEY);
                lat = fetch.getLat();
                lng = fetch.getLng();
            }
            
            //read time and budget;
            String[] readOther = currentLine.split("/");
            if (readOther.length > 1) {
                time = Double.parseDouble(readOther[1]);
                budget = Double.parseDouble(readOther[2]);
            } else {
                time = -1;
                budget = -1;
            }
            
            // read distance api ( time and duration)
            String[] readDistanceAPI = currentLine.split("#");
            if (readDistanceAPI.length > 1) {
                
                durationToNext = Double.parseDouble(readDistanceAPI[1]);
                distanceToNext = Double.parseDouble(readDistanceAPI[2]);
            }
            
            Point toAdd = new Point(lat, lng, locationName, time, budget);
            toAdd.setDistanceToNext(distanceToNext);
            toAdd.setDurationToNext(durationToNext);
            fromFile.add(fromFile.size(),toAdd);
        }
    }
    
    /**
     * Converts list into string for output/writing to file
     *
     * @param s LinkedList<Point>
     * @return output
     */
    public String toString(LinkedList<Point> s) {
        String output = s.size() + 
            "          \"Location\" _lat_lng_/time to spend/budget/^TimeToNext^DistanceToNext\n";
        for (int i = 0; i < s.size(); i++) {
            Point current = s.get(i);
            output = output + "\"" + current.getName() + "\""
                + "_" + current.getLat() + "_" + current.getLng() + "_" +
                "/" + current.getTimeToSpend() + "/" + current.getBudget() + "/"
                + "#" + current.getDurationToNext() + "#" 
                + current.getDistanceToNext() + "\n";
        }
        return output;
    }
    
    /**
     * Returns original list from file + any addLocations
     *
     * @return list
     */
    public LinkedList<Point> getFromFile() {
        return fromFile;
    }
    
    /**
     * Returns sorted list
     *
     * @return list
     */
    public LinkedList<Point> getSorted() {
        return sorted;
    }
    
    /**
     * Sort list sorts the list using insertSmallest algorithm. 
     * This implementation is implemented using the GPS coordinates
     * instead of using the google distance matrix API, 
     * this is because it will generate >2,500 request after a few runs 
     * which will incur cost.
     * 
     * This implementation is not the best, because it only uses physical 
     * distance difference which in a real world scenario,
     * point to point is not a straight line.
     * 
     * The pros of this implementation is to reduce the amount of API calls 
     * to reduce cost and increase speed.
     * 
     * It is recommended that once list is sorted, run fetchDistance method.
     */
    public void sortList() throws IOException {
        System.out.println("Please wait while the system sorts the list, it may take some time....\n");
        Sorter sort = new Sorter();
        sort.insertInOrder(fromFile.get(0));
        sort.insertInOrder(fromFile.get(fromFile.size() - 1));
        for (int i = 1; i < fromFile.size() - 1; i++) {
            sort.insertSmallest(fromFile.get(i));
        }
        System.out.println("Locations sorted!");
        sorted = sort.getList();
    }
    
    /**
     * This method fetches and sets DistanceToNext at first n-1 nodes. 
     * It runs the method that fetches distance data directly from Google's
     * Distance Matrix API
     *
     * @throws IOException if there is an error with the inputs
     */
    public void fetchDistance(LinkedList<Point> s) throws IOException {
        System.out.println("Fetching Distances.....");
        for (int i = 0; i < s.size() - 1; i++) {
            fetchDistanceAPI fetch = new fetchDistanceAPI(s.get(i).getName(), 
                                                          s.get(i + 1).getName(), 
                                                          API_KEY);
            System.out.println("Fetching " + s.get(i).getName() + " and " + 
                               s.get(i + 1).getName());
            s.get(i).setDistanceToNext(fetch.getDistance());
            s.get(i).setDurationToNext(fetch.getDuration());
        }
        System.out.println("\nDone!");
    }
    
    
    /**
     * Writes output string to file
     *
     * @param output string
     * @param file   filename
     * @throws IOException if
     */
    public void writeToFile(String output, String file) throws IOException {
        Files.write(Paths.get(file), output.getBytes());
    }
}


