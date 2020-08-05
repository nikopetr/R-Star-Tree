import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

// Represents the bounds of an interval in a single dimension
class Bounds implements Serializable {
    private double lower; // Representing the lower value of the interval
    private double upper; // Representing the upper value of the interval

    // constructor of the class
    // Since we have to do with bounds of an interval the lower Bound cannot be bigger than upper
    Bounds(double lower, double upper) {
        if (lower <= upper)
        {
            this.lower = lower;
            this.upper = upper;
        }
        else
            throw new IllegalArgumentException( "The lower value of the bounds cannot be bigger than the upper");
    }

    double getLower() {
        return lower;
    }

    double getUpper() {
        return upper;
    }

    // Returns an ArrayList with bounds for each dimension, including the the minimum bounds needed to fit the given entries
    static ArrayList<Bounds> findMinimumBounds(ArrayList<Entry> entries) {
        ArrayList<Bounds> minimumBounds = new ArrayList<>();
        // For each dimension finds the minimum interval needed for the entries to fit
        for (int d = 0; d < FilesHelper.getDataDimensions(); d++)
        {
            Entry lowerEntry = Collections.min(entries, new EntryComparator.EntryBoundComparator(entries,d,false));
            Entry upperEntry = Collections.max(entries, new EntryComparator.EntryBoundComparator(entries,d,true));
            minimumBounds.add(new Bounds(lowerEntry.getBoundingBox().getBounds().get(d).getLower(),upperEntry.getBoundingBox().getBounds().get(d).getUpper()));
        }
        return minimumBounds;
    }

    // Returns an ArrayList with bounds for each dimension, including the the minimum bounds needed to merge the given bounding boxes
    static ArrayList<Bounds> findMinimumBounds(BoundingBox boundingBoxA, BoundingBox boundingBoxB) {
        ArrayList<Bounds> minimumBounds = new ArrayList<>();
        // For each dimension finds the minimum interval needed for the entries to fit
        for (int d = 0; d < FilesHelper.getDataDimensions(); d++)
        {
            double lower = Math.min(boundingBoxA.getBounds().get(d).getLower(), boundingBoxB.getBounds().get(d).getLower());
            double upper = Math.max(boundingBoxA.getBounds().get(d).getUpper(), boundingBoxB.getBounds().get(d).getUpper());
            minimumBounds.add(new Bounds(lower,upper));
        }
        return minimumBounds;
    }
}
