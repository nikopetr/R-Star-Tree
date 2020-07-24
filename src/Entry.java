import java.io.Serializable;
import java.util.ArrayList;

class Entry implements Serializable {
    private BoundingBox boundingBox;

    public void setChildNodeBlockId(Long childNodeBlockId) {
        this.childNodeBlockId = childNodeBlockId;
    }

    //private Node childNode;
    private Long childNodeBlockId;

    Entry(Node childNode) {
        this.childNodeBlockId = childNode.getBlockId();
        adjustBBToFitEntries(childNode.getEntries());
    }

    Entry(BoundingBox boundingBox)
    {
        this.boundingBox = boundingBox;
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
        boundingBox = new BoundingBox(Bounds.findMinimumBounds(boundingBox,entry.getBoundingBox()));
    }
}
