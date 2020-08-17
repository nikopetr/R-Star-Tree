import java.util.ArrayList;

// Represents the entries on the bottom of the RStarTree
// Extends the Entry Class where it's BoundingBox
// is the bounding box of the spatial object (the record) indexed
// also holds the recordId of the record and a pointer of the block which the record is saved in the datafile
class LeafEntry extends Entry {
    private long recordId;
    private long dataFileBlockId; // The id of the block which the record is saved in the datafile

    LeafEntry(long recordId, long dataFileBlockId, ArrayList<Bounds> recordBounds) {
        super(new BoundingBox(recordBounds));
        this.recordId = recordId;
        this.dataFileBlockId = dataFileBlockId;
    }

    long getRecordId() {
        return recordId;
    }

    long getDataFileBlockId() {
        return dataFileBlockId;
    }
}
