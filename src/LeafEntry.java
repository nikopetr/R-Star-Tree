import java.util.ArrayList;

// Represents the entries on the bottom of the RStarTree
// Extends the Entry Class where it's BoundingBox
// is the bounding box of the spatial object (the record) indexed
// and also holds the recordId of the record
class LeafEntry extends Entry {
    private long recordId;

    LeafEntry(long recordId, ArrayList<Bounds> recordBounds) {
        super(new BoundingBox(recordBounds));
        this.recordId = recordId;
    }

    long getRecordId() {
        return recordId;
    }
}
