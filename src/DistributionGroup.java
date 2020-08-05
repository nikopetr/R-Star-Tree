import java.util.ArrayList;

// Class which represents a set of entries and their bounding box
// Mainly used to determine distribution groups on chooseSplitIndex during the split on a node in the RStarTree
class DistributionGroup {
    private ArrayList<Entry> entries;
    private BoundingBox boundingBox;

     DistributionGroup(ArrayList<Entry> entries, BoundingBox boundingBox) {
        this.entries = entries;
        this.boundingBox = boundingBox;
    }

    ArrayList<Entry> getEntries() {
        return entries;
    }

    BoundingBox getBoundingBox() {
        return boundingBox;
    }
}
