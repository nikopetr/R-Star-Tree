import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

// Class used for comparing Entries based on different
class EntryComparator {
    // Class used to compare entries by their lower or upper bounds
    static class EntryBoundComparator implements Comparator<Entry>
    {
        // Hash-map  used for mapping the comparison value of the Entries during the compare method
        // Key of the hash-map is the given Entry
        // Value of the hash-map is the given Entry's bound (either upper or lower)
        private HashMap<Entry,Double> entryComparisonMap;

        EntryBoundComparator(List<Entry> entriesToCompare, int dimension, boolean compareByUpper)
        {
            // Initialising hash-map
            this.entryComparisonMap = new HashMap<>();
            // If the comparison is based on the upper bound
            if (compareByUpper)
            {
                for (Entry entry : entriesToCompare)
                    entryComparisonMap.put(entry,entry.getBoundingBox().getBounds().get(dimension).getUpper());
            }
            // else if the comparison is based on the lower bound
            else
            {
                for (Entry entry : entriesToCompare)
                    entryComparisonMap.put(entry,entry.getBoundingBox().getBounds().get(dimension).getLower());
            }
        }

        public int compare(Entry entryA, Entry entryB)
        {
            return Double.compare(entryComparisonMap.get(entryA),entryComparisonMap.get(entryB));
        }
    }

    // Class used to compare entries by their area enlargement of including a new "rectangle" item
    static class EntryAreaEnlargementComparator implements Comparator<Entry>
    {
        // Hash-map used for mapping the comparison value of the Entries during the compare method
        // First value of the ArrayList is the area of the bounding box
        // Second value of the ArrayList is the area enlargement of the specific Entry
        private HashMap<Entry,ArrayList<Double>> entryComparisonMap;

        EntryAreaEnlargementComparator(List<Entry> entriesToCompare, BoundingBox boundingBoxToAdd)
        {
            // Initialising Hash-map
            this.entryComparisonMap = new HashMap<>();
            for (Entry entry : entriesToCompare)
            {
                BoundingBox entryNewBB = new BoundingBox(Bounds.findMinimumBounds(entry.getBoundingBox(),boundingBoxToAdd));
                ArrayList<Double> values = new ArrayList<>();
                values.add(entry.getBoundingBox().getArea()); // First value of the ArrayList is the area of the bounding box
                double areaEnlargement = entryNewBB.getArea() - entry.getBoundingBox().getArea();
                if (areaEnlargement < 0)
                    throw new IllegalStateException("The enlargement cannot be a negative number");
                values.add(areaEnlargement); // Second value of the ArrayList is the area enlargement of the specific Entry
                entryComparisonMap.put(entry,values);
            }

        }

        @Override
        public int compare(Entry entryA, Entry entryB) {
            double areaEnlargementA = entryComparisonMap.get(entryA).get(1);
            double areaEnlargementB = entryComparisonMap.get(entryB).get(1);
            // Resolve ties by choosing the entry with the rectangle of smallest area
            if (areaEnlargementA == areaEnlargementB)
                return Double.compare(entryComparisonMap.get(entryA).get(0),entryComparisonMap.get(entryB).get(0));
            else
                return Double.compare(areaEnlargementA,areaEnlargementB);
        }
    }

    // Class used to compare entries by their overlap enlargement of including a new "rectangle" item
    static class EntryOverlapEnlargementComparator implements Comparator<Entry>
    {
        private BoundingBox boundingBoxToAdd; // The bounding box to add
        private ArrayList<Entry> nodeEntries; // All the entries of the Node

        // Hash-map used for mapping the comparison value of the Entries during the compare method
        // Key of the hash-map is the given Entry
        // Value of the hash-map is the given Entry's overlap Enlargement
        private HashMap<Entry,Double> entryComparisonMap;
        EntryOverlapEnlargementComparator(List<Entry> entriesToCompare, BoundingBox boundingBoxToAdd, ArrayList<Entry> nodeEntries)
        {
            this.boundingBoxToAdd = boundingBoxToAdd;
            this.nodeEntries = nodeEntries;

            // Initialising Hash-map
            this.entryComparisonMap = new HashMap<>();
            for (Entry entry : entriesToCompare)
            {
                double overlapEntry = calculateEntryOverlapValue(entry, entry.getBoundingBox());
                Entry newEntry = new Entry(new BoundingBox(Bounds.findMinimumBounds(entry.getBoundingBox(),boundingBoxToAdd))); // The entry's bounding box after it includes the new bounding box
                double overlapNewEntry = calculateEntryOverlapValue(entry, newEntry.getBoundingBox()); // Using the previous entry signature in order to check for equality
                double overlapEnlargementEntry = overlapNewEntry - overlapEntry ;

                if (overlapEnlargementEntry < 0)
                    throw new IllegalStateException("The enlargement cannot be a negative number");

                entryComparisonMap.put(entry,overlapEnlargementEntry);
            }
        }

        @Override
        public int compare(Entry entryA, Entry entryB) {
            double overlapEnlargementEntryA = entryComparisonMap.get(entryA);
            double overlapEnlargementEntryB = entryComparisonMap.get(entryB);
            // Resolve ties by choosing the entry whose rectangle needs least area enlargement, then
            // the entry with the rectangle of smallest area (which is included in the EntryAreaEnlargementComparator)
            if (overlapEnlargementEntryA == overlapEnlargementEntryB)
            {   ArrayList<Entry> entriesToCompare = new ArrayList<>();
                entriesToCompare.add(entryA);
                entriesToCompare.add(entryB);
                return new EntryAreaEnlargementComparator(entriesToCompare,boundingBoxToAdd).compare(entryA,entryB);
            }
            else
                return Double.compare(overlapEnlargementEntryA,overlapEnlargementEntryB);
        }

        // Calculates and returns the overlap value of the given entry with the other node entries
        double calculateEntryOverlapValue(Entry entry, BoundingBox boundingBox){
            double sum = 0;
            for (Entry nodeEntry : nodeEntries)
            {
                if (nodeEntry != entry)
                    sum += BoundingBox.calculateOverlapValue(boundingBox,nodeEntry.getBoundingBox());
            }
            return sum;
        }
    }

    // Class used to compare entries by their distance from their overall's bouncing box's center
    static class EntryDistanceFromCenterComparator implements Comparator<Entry>
    {
        // Hash-map  used for mapping the comparison value of the Entries during the compare method
        // Key of the hash-map is the given Entry
        // Value of the hash-map is the given Entry's BoundingBox distance from the given BoundingBox
        private HashMap<Entry,Double> entryComparisonMap;

        EntryDistanceFromCenterComparator(List<Entry>entriesToCompare, BoundingBox boundingBox) {
            // Initialising Hash-map
            this.entryComparisonMap = new HashMap<>();

            for (Entry entry : entriesToCompare)
                entryComparisonMap.put(entry,BoundingBox.findDistanceBetweenBoundingBoxes(entry.getBoundingBox(),boundingBox));
        }

        public int compare(Entry entryA, Entry entryB)
        {
            return Double.compare(entryComparisonMap.get(entryA),entryComparisonMap.get(entryB));
        }
    }

    // Class used to compare entries by their distance from a point
    static class EntryDistanceFromPointComparator implements Comparator<Entry>
    {
        // Hash-map  used for mapping the comparison value of the Entries during the compare method
        // Key of the hash-map is the given Entry
        // Value of the hash-map is the given Entry's BoundingBox distance from the given point
        private HashMap<Entry,Double> entryComparisonMap;

        EntryDistanceFromPointComparator(List<Entry> entriesToCompare, ArrayList<Double> point) {
            // Initialising Hash-map
            this.entryComparisonMap = new HashMap<>();

            for (Entry entry : entriesToCompare)
                entryComparisonMap.put(entry,entry.getBoundingBox().findMinDistanceFromPoint(point));
        }

        public int compare(Entry entryA, Entry entryB)
        {
            return Double.compare(entryComparisonMap.get(entryA),entryComparisonMap.get(entryB));
        }
    }
}