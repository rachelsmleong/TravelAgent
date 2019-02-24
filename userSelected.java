/**
 * Execution: Record the data chosen by user to be used into calculations in
 * main class
 */

public class userSelected {
    private double duration;
    private double budget;
    private String name;
    private double distanceToNext;
    private double timeToNext;

    @Override
    public String toString() {
        return "userSelected{" +
                "duration=" + duration +
                ", budget=" + budget +
                ", name='" + name + '\'' +
                ", distanceToNext=" + distanceToNext +
                ", timeToNext=" + timeToNext +
                '}';
    }

    /**
     * Constructor
     */
    public userSelected(String name, double duration, double budget, double distanceToNext, 
                        double timeToNext) {

        this.duration = duration;
        this.budget = budget;
        this.name = name;
        this.distanceToNext = distanceToNext;
        this.timeToNext = timeToNext;
    }

    /**
     * Getters
     * @return
     */
    public double getTimeToNext() {
        return timeToNext;
    }

    public double getDuration() {
        return duration;
    }

    public double getBudget() {
        return budget;
    }

    public double getDistanceToNext() {
        return distanceToNext;
    }

    public String getName() {
        return name;
    }

}
