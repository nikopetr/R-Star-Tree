import java.io.Serializable;
import java.util.ArrayList;

// An Entry refers to the address of a lower Node (child) in the RStarTree and to it's BoundingBox (it's covering rectangle),
// which covers all the bounding boxes in the lower Node's Entries
class Entry implements Serializable {
    private BoundingBox boundingBox; // The closed bounded intervals describing the extent of the object along each dimension
    private Long childNodeBlockId; // The address (block ID) of a lower Node (child) in the RStarTree

    // Constructor which takes parameters the lower Node which represents the child node of the entry
    Entry(Node childNode) {
        this.childNodeBlockId = childNode.getBlockId();
        adjustBBToFitEntries(childNode.getEntries()); // Adjusting the BoundingBox of the Entry to fit the objects of the childNode
    }

    // Constructor which takes parameters the lower Node which represents the child node of the entry
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

    // Adjusting the Bouncing Box of the entry by assigning a new bounding box to it with the new minimum bounds
    // based on the ArrayList parameter entries
    void adjustBBToFitEntries(ArrayList<Entry> entries){
        boundingBox = new BoundingBox(Bounds.findMinimumBounds(entries));
    }

    // Adjusting the Bouncing Box of the entry by assigning a new bounding box to it with the extended minimum bounds
    // that also enclose the given Entry parameter entryToInclude
    void adjustBBToFitEntry(Entry entryToInclude){
        boundingBox = new BoundingBox(Bounds.findMinimumBounds(boundingBox,entryToInclude.getBoundingBox()));
    }
}
