import java.util.ArrayList;
import java.util.Collections;

class Bounds {
    private double lower; // Representing the lower value of the interval
    private double upper; // Representing the upper value of the interval

    Bounds(double lower, double upper) {
        if (lower <= upper)
        {
            this.lower = lower;
            this.upper = upper;
        }
        else //TODO might just need to invert this instead of throwing exception if everything works fine
            throw new IllegalArgumentException( "Lower Bound cannot be bigger than upper");
//        else
//        {
//            this.lower = upper;
//            this.upper = lower;
//        }

    }

    double getLower() {
        return lower;
    }

    double getUpper() {
        return upper;
    }



    //TODO MIGHT BE OPTIMIZABLE

    // Returns an ArrayList with bounds for each dimension, including the the minimum bounds needed to fit the given entries
    static ArrayList<Bounds> findMinimumBounds(ArrayList<Entry> entries) {
        ArrayList<Bounds> minimumBounds = new ArrayList<>();
        // For each dimension finds the minimum interval needed for the entries to fit
        for (int d = 0; d < MetaData.DIMENSIONS; d++)
        {
            Entry lowerEntry = Collections.min(entries, new EntryComparator.EntryBoundComparator(d,false));
            Entry upperEntry = Collections.max(entries, new EntryComparator.EntryBoundComparator(d,true));
            minimumBounds.add(new Bounds(lowerEntry.getBoundingBox().getBounds().get(d).getLower(),upperEntry.getBoundingBox().getBounds().get(d).getUpper()));
        }
        return minimumBounds;
    }

    // Returns an ArrayList with bounds for each dimension, including the the minimum bounds needed to merge the given bounding boxes
    static ArrayList<Bounds> findMinimumBounds(BoundingBox boundingBoxA, BoundingBox boundingBoxB) {
        ArrayList<Bounds> minimumBounds = new ArrayList<>();
        // For each dimension finds the minimum interval needed for the entries to fit
        for (int d = 0; d < MetaData.DIMENSIONS; d++)
        {
            double lower = Math.min(boundingBoxA.getBounds().get(d).getLower(), boundingBoxB.getBounds().get(d).getLower());
            double upper = Math.max(boundingBoxA.getBounds().get(d).getUpper(), boundingBoxB.getBounds().get(d).getUpper());
            minimumBounds.add(new Bounds(lower,upper));
        }
        return minimumBounds;
    }
}
