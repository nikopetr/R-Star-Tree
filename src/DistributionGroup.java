import java.util.ArrayList;

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
