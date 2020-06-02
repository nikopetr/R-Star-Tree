import java.util.ArrayList;
import java.util.Comparator;

class EntryComparator {
    // Class used to compare entries by their lower or upper bounds
    static class EntryBoundComparator implements Comparator<Entry>
    {
        private int dimension;
        private boolean sortInUpper;

        EntryBoundComparator(int dimension, boolean sortInUpper)
        {
            this.dimension = dimension;
            this.sortInUpper = sortInUpper;
        }

        public int compare(Entry entryA, Entry entryB)
        {
            if (sortInUpper)
                return Double.compare(entryA.getBoundingBox().getBounds().get(dimension).getUpper(), entryB.getBoundingBox().getBounds().get(dimension).getUpper());
            else
                return Double.compare(entryA.getBoundingBox().getBounds().get(dimension).getLower(), entryB.getBoundingBox().getBounds().get(dimension).getLower());
        }
    }

    // Class used to compare entries by their area enlargement of including a new "rectangle" item
    static class EntryAreaEnlargementComparator implements Comparator<Entry>
    {
        private BoundingBox boundingBoxToAdd;

        EntryAreaEnlargementComparator(BoundingBox boundingBoxToAdd)
        {
            this.boundingBoxToAdd = boundingBoxToAdd;
        }

        @Override
        public int compare(Entry entryA, Entry entryB) {
            BoundingBox newBoundingBoxA = new BoundingBox(Bounds.findMinimumBounds(entryA.getBoundingBox(),boundingBoxToAdd));
            BoundingBox newBoundingBoxB = new BoundingBox(Bounds.findMinimumBounds(entryB.getBoundingBox(),boundingBoxToAdd));

            double areaEnlargementA = newBoundingBoxA.getArea() - entryA.getBoundingBox().getArea();
            double areaEnlargementB = newBoundingBoxB.getArea() - entryB.getBoundingBox().getArea();
            ;
            if (areaEnlargementA < 0 || areaEnlargementB < 0)
                throw new IllegalStateException("The enlargement cannot be a negative number");

            // Resolve ties by choosing the entry with the rectangle of smallest area
            if (areaEnlargementA == areaEnlargementB)
                return Double.compare(entryA.getBoundingBox().getArea(),entryB.getBoundingBox().getArea());
            else
                return Double.compare(areaEnlargementA,areaEnlargementB);
        }
    }

    // Class used to compare entries by their overlap enlargement of including a new "rectangle" item
    static class EntryOverlapEnlargementComparator implements Comparator<Entry>
    {
        private BoundingBox boundingBoxToAdd;
        private ArrayList<Entry> nodeEntries;

        EntryOverlapEnlargementComparator(BoundingBox boundingBoxToAdd, ArrayList<Entry> nodeEntries)
        {
            this.boundingBoxToAdd = boundingBoxToAdd;
            this.nodeEntries = nodeEntries;
        }

        //TODO maybe make this run a bit faster
        @Override
        public int compare(Entry entryA, Entry entryB) {
            double overlapEntryA = calculateEntryOverlapValue(entryA, entryA.getBoundingBox());
            Entry newEntryA = new Entry(new BoundingBox(Bounds.findMinimumBounds(entryA.getBoundingBox(),boundingBoxToAdd))); // The entry's bounding box after it includes the new bounding box
            double overlapNewEntryA = calculateEntryOverlapValue(entryA, newEntryA.getBoundingBox()); // Using the previous entry signature in order to check for equality
            double overlapEnlargementEntryA = overlapNewEntryA - overlapEntryA ;

            double overlapEntryB = calculateEntryOverlapValue(entryB, entryB.getBoundingBox());
            Entry newEntryB = new Entry(new BoundingBox(Bounds.findMinimumBounds(entryB.getBoundingBox(),boundingBoxToAdd))); // The entry's bounding box after it includes the new bounding box
            double overlapNewEntryB = calculateEntryOverlapValue(entryB, newEntryB.getBoundingBox()); // Using the previous entry signature in order to check for equality
            double overlapEnlargementEntryB =  overlapNewEntryB - overlapEntryB ;

            if (overlapEnlargementEntryA < 0 || overlapEnlargementEntryB < 0)
                throw new IllegalStateException("The enlargement cannot be a negative number");

            // Resolve ties by choosing the entry
            // whose rectangle needs least area enlargement, then
            // the entry with the rectangle of smallest area
            if (overlapEnlargementEntryA == overlapEnlargementEntryB)
                return new EntryAreaEnlargementComparator(boundingBoxToAdd).compare(entryA,entryB);

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
        private BoundingBox boundingBox;

        EntryDistanceFromCenterComparator(BoundingBox boundingBox) {
            this.boundingBox = boundingBox;
        }

        public int compare(Entry entryA, Entry entryB)
        {
            return Double.compare(BoundingBox.findDistanceBetweenBoundingBoxes(entryA.getBoundingBox(),boundingBox),BoundingBox.findDistanceBetweenBoundingBoxes(entryB.getBoundingBox(),boundingBox));
        }
    }

    // Class used to compare entries by their distance from a point
    static class EntryDistanceFromPointComparator implements Comparator<Entry>
    {
        private ArrayList<Double> point;

        EntryDistanceFromPointComparator(ArrayList<Double> point) {
            this.point = point;
        }

        public int compare(Entry entryA, Entry entryB)
        {
            return Double.compare(entryA.getBoundingBox().findMinDistanceFromPoint(point),entryB.getBoundingBox().findMinDistanceFromPoint(point));
        }
    }
}
