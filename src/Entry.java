import java.io.Serializable;
import java.util.ArrayList;

class Entry implements Serializable {
    private BoundingBox boundingBox;
    private Long childNodeBlockId;

    Entry(Node childNode) {
        this.childNodeBlockId = childNode.getBlockId();
        adjustBBToFitEntries(childNode.getEntries());
    }

    Entry(BoundingBox boundingBox)
    {
        this.boundingBox = boundingBox;
    }

    void setChildNodeBlockId(Long childNodeBlockId) {
        this.childNodeBlockId = childNodeBlockId;
    }

    BoundingBox getBoundingBox() {
        return boundingBox;
    }

    Long getChildNodeBlockId() {
        return childNodeBlockId;
    }

    void adjustBBToFitEntries(ArrayList<Entry> entries){
        boundingBox = new BoundingBox(Bounds.findMinimumBounds(entries));
    }

    void adjustBBToFitEntry(Entry entry){
        // Adjusting the Bouncing Box of the entry by assigning a new bounding box to it with the new minimum bounds
        boundingBox = new BoundingBox(Bounds.findMinimumBounds(boundingBox,entry.getBoundingBox()));
    }
}
