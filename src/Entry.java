import java.io.Serializable;
import java.util.ArrayList;

class Entry implements Serializable {
    private BoundingBox boundingBox;
    private Node childNode;

    Entry(Node childNode) {
        this.childNode = childNode;
        adjustBBToFitEntries(childNode.getEntries());
    }

    Entry(BoundingBox boundingBox)
    {
        this.boundingBox = boundingBox;
    }

    BoundingBox getBoundingBox() {
        return boundingBox;
    }

    Node getChildNode() {
        return childNode;
    }

    void adjustBBToFitEntries(ArrayList<Entry> entries){
        boundingBox = new BoundingBox(Bounds.findMinimumBounds(entries));
    }

    void adjustBBToFitEntry(Entry entry){
        boundingBox = new BoundingBox(Bounds.findMinimumBounds(boundingBox,entry.getBoundingBox()));
    }
}
