import java.io.Serializable;
import java.util.ArrayList;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

class BoundingBox implements Serializable {
    private ArrayList<Bounds> bounds;
    private Double area;
    private Double margin;
    private ArrayList<Double> center;

    BoundingBox(ArrayList<Bounds> bounds) {
        this.bounds = bounds;
        this.area = calculateArea();
        this.margin = calculateMargin();
        this.center = getCenter();
    }

    ArrayList<Bounds> getBounds() {
        return bounds;
    }

    //TODO check with big input if return margin works fine
    double getArea() {
        // If area is not yet initialized, find the area
        if (area == null)
            area = calculateArea();

        return area;
    }

    double getMargin() {
        // If margin is not yet initialized, find the margin
        if (margin == null)
            margin = calculateMargin();

        return margin;
    }

    // Returns true if the given's point radius overlaps with the bounding box
    boolean checkOverLapWithPoint(ArrayList<Double> point, double radius){
        // If the minimum distance from the point is less or equal the point's radius then the bounding box is in the range
        return findMinDistanceFromPoint(point) <= radius;
    }

    // Returns the minimum distance between the bounding box and the point
    double findMinDistanceFromPoint(ArrayList<Double> point){
        double minDistance = 0;
        // For every dimension find the minimum distance
        double rd;
        for (int d = 0; d < MetaData.DIMENSIONS; d++)
        {
            if(getBounds().get(d).getLower() > point.get(d))
                rd = getBounds().get(d).getLower();
            else if (getBounds().get(d).getUpper() < point.get(d))
                rd = getBounds().get(d).getUpper();
            else
                rd = point.get(d);

            minDistance += Math.pow(point.get(d) - rd,2);
        }
        return sqrt(minDistance);
    }

    private ArrayList<Double> getCenter() {
        // If center is not yet initialized, find the center and return it
        if (center == null)
        {
            center = new ArrayList<>();

            for (int d = 0; d < MetaData.DIMENSIONS; d++)
                center.add((bounds.get(d).getUpper()+bounds.get(d).getLower())/2);
        }
        return center;
    }
    // Calculates and returns the margin(perimeter) of this BoundingBox
    private double calculateMargin() {
        double sum = 0;
        for (int d = 0; d < MetaData.DIMENSIONS; d++)
            sum += abs(bounds.get(d).getUpper() - bounds.get(d).getLower());
        return sum;
    }

    // Calculates and returns the area of this BoundingBox
    private double calculateArea() {
        double productOfEdges = 1;
        for (int d = 0; d < MetaData.DIMENSIONS; d++)
            productOfEdges = productOfEdges * (bounds.get(d).getUpper() - bounds.get(d).getLower());
        return abs(productOfEdges);
    }

    // Returns true if the two bounding boxes overlap
    static boolean checkOverlap(BoundingBox boundingBoxA, BoundingBox boundingBoxB) {
        // For every dimension find the intersection point
        for (int d = 0; d < MetaData.DIMENSIONS; d++)
        {
            double overlapD = Math.min(boundingBoxA.getBounds().get(d).getUpper(), boundingBoxB.getBounds().get(d).getUpper())
                    - Math.max(boundingBoxA.getBounds().get(d).getLower(), boundingBoxB.getBounds().get(d).getLower());

            if (overlapD < 0) //TODO check if "=" is needed or not
                return false;
        }
        return true;
    }

    // Calculates and returns the overlap value between two bounding boxes
    static double calculateOverlapValue(BoundingBox boundingBoxA, BoundingBox boundingBoxB) {
        double overlapValue = 1;
        // For every dimension find the intersection point
        for (int d = 0; d < MetaData.DIMENSIONS; d++)
        {
            double overlapD = Math.min(boundingBoxA.getBounds().get(d).getUpper(), boundingBoxB.getBounds().get(d).getUpper())
                    - Math.max(boundingBoxA.getBounds().get(d).getLower(), boundingBoxB.getBounds().get(d).getLower());

            if (overlapD <= 0) //TODO check if "=" is needed or not
                return 0; // No overlap, return 0
            else
                overlapValue = overlapD*overlapValue;
        }
        return overlapValue;
    }

    // Calculates and returns the euclidean distance value between two bounding boxes's centers
    static double findDistanceBetweenBoundingBoxes(BoundingBox boundingBoxA, BoundingBox boundingBoxB) {
        double distance = 0;
        // For every dimension find the intersection point
        for (int d = 0; d < MetaData.DIMENSIONS; d++)
        {
            distance += Math.pow(boundingBoxA.getCenter().get(d) - boundingBoxB.getCenter().get(d),2);
        }
        return sqrt(distance);
    }
}
