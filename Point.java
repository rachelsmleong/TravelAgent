/**
 * Execution: Stores each point in the database
 */

public class Point {
    private final static double EARTH_RADIUS = 6371.0088; // average radius in KM.

    private double lat;
    private double lng;
    private String name;
    private double timeToSpend;
    private double budget;
    private double distanceToNext;
    private double durationToNext;

    public Point(double lat, double lng, String name, double timeToSpend, double budget) {

        this.lat = lat;
        this.lng = lng;
        this.name = name;
        this.timeToSpend = timeToSpend;
        this.budget = budget;

    }

    public double getDurationToNext() {
        return durationToNext;
    }

    public void setDurationToNext(double durationToNext) {
        this.durationToNext = durationToNext;
    }

    public double getDistanceToNext() {
        return distanceToNext;
    }

    public void setDistanceToNext(double distanceToNext) {
        this.distanceToNext = distanceToNext;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public double getTimeToSpend() {
        return timeToSpend;
    }

    public void setTimeToSpend(double timeToSpend) {
        this.timeToSpend = timeToSpend;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public double distanceTo(Point p) {

        // distance is calculated using the Haversine_formula , because earth is not flat
        // https://rosettacode.org/wiki/Haversine_formula#Java

        double lat1 = lat;
        double lon1 = lng;
        double lat2 = p.lat;
        double lon2 = p.lng;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2) + 
            Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return EARTH_RADIUS * c;
    }

    public void drawTo(Point p) {
        PennDraw.line(lat, lng, p.lat, p.lng);
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name + " (" + lat + ", " + lng + ")" + timeToSpend + " " + budget;
    }
}
