import java.util.ArrayList;

class LeafEntry extends Entry {
    private long recordId;
    //long blockId;

    LeafEntry(long recordId, ArrayList<Bounds> recordBounds) {
        super(new BoundingBox(recordBounds));
        this.setChildNode(null);
        this.recordId = recordId;
    }

    long getRecordId() {
        return recordId;
    }
}
