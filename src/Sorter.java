/**
 * Execution: Sorts data based on the Travelling Salesman Problem in homework 8
 */

public class Sorter {

    private LinkedList<Point> list;

    /**
     * Contructor
     */
    public Sorter() {
        list = new LinkedList<>();
    }

    /**
     * Returns list size
     *
     * @return list size
     */
    public int size() {
        return list.size();
    }

    /**
     * Inserts a new Point after location
     *
     * @param location point
     * @param point    index, point inserts after this index
     */
    private void insertAfter(int location, Point point) {
        list.add(location + 1, point);
    }

    /**
     * Returns list
     *
     * @return LinkedList
     */
    public LinkedList<Point> getList() {
        return list;
    }

    /**
     * Print all locations in string
     *
     * @return
     */
    public String toString() {
        String output = "";
        if (isEmpty()) return output;

        for (int i = 0; i < size(); i++) {
            output = output + "\n" + list.get(i).getName();

        }
        return output;
    }

    /**
     * Insert inOrder, points insert at the end of the list
     *
     * @param p point
     */
    public void insertInOrder(Point p) {
        list.add(p);
    }

    /**
     * Insert smallest, points insert at the point that creates 
     * the least change in the total distance
     *
     * @param p point
     */
    public void insertSmallest(Point p) {
        int indexOfClosest = 0;
        double smallestDistance;
        double currentDistance;
            smallestDistance = insertSmallestDistance(0, p);
            // find index with the closest distance
            for (int i = 0; i < size() - 1; i++) {
                currentDistance = insertSmallestDistance(i, p);
                if (currentDistance < smallestDistance) {
                    indexOfClosest = i;
                    smallestDistance = currentDistance;
                }
            }
            insertAfter(indexOfClosest, p);
    }

    /**
     * Returns the distance from current point to point p to next point after current.
     *
     * @param current
     * @param p
     * @return
     */
    private double insertSmallestDistance(int current, Point p) {
        return list.get(current).distanceTo(p) + p.distanceTo(list.get(current + 1)) 
            - list.get(current).distanceTo(list.get(current + 1));
    }

    /**
     * Return if list is empty
     *
     * @return
     */
    private boolean isEmpty() {
        return list.size() == 0;
    }

//    public static void main(String args[]) {
//        Sorter test = new Sorter();
//        test.insertSmallest(new Point(42.907665, -73.14254, "Vermont"));
//        test.insertSmallest(new Point(29.4964, -97.400358, "Texas"));
//        test.insertSmallest(new Point(38.596714, -121.3348,"California"));
//        test.insertSmallest(new Point(47.993195, -122.561462,"Washington"));
//        test.insertSmallest(new Point(41.881832,-87.623177, "Chicago"));
//        test.insertSmallest(new Point( 43.750546,-79.716408, "Canada"));
//        test.insertSmallest(new Point(46.923555, -106.642838,"Montana"));
//        test.insertSmallest(new Point(36.231387, -81.913440,"North Carolina"));
//        test.insertSmallest(new Point(39.921641, -107.508085,"Colarado"));
//        test.insertSmallest(new Point(6.081794, -65.185072,"Venezuela"));
//
//        System.out.println(test.toString());
//        System.out.print(test.distance());
//
//
//        Sorter test2 = new Sorter();
//        test2.insertInOrder(new Point(42.907665, -73.14254, "Vermont"));
//        test2.insertInOrder(new Point(29.4964, -97.400358, "Texas"));
//        test2.insertInOrder(new Point(39.921641, -107.508085,"Colarado"));
//        test2.insertInOrder(new Point(38.596714, -121.3348,"California"));
//        test2.insertInOrder(new Point(47.993195, -122.561462,"Washington"));
//        test2.insertInOrder(new Point(41.881832,-87.623177, "Chicago"));
//        test2.insertInOrder(new Point( 43.750546,-79.716408, "Canada"));
//        test2.insertInOrder(new Point(6.081794, -65.185072,"Venezuela"));
//        test2.insertInOrder(new Point(46.923555, -106.642838,"Montana"));
//        test2.insertInOrder(new Point(42.907665, -73.14254, "Vermont"));
//        test2.insertInOrder(new Point(36.231387, -81.913440,"North Carolina"));
//        test2.insertInOrder(new Point(39.921641, -107.508085,"Colarado"));
//        System.out.println(test2.toString());
//        System.out.print(test2.distance());
//    }

}