import java.util.ArrayList;

class BoundingBoxRangeQuery extends Query {
    private ArrayList<Long> qualifyingRecordIds; // Record ids used for queries
    private BoundingBox searchBoundingBox; // bounding box used for range queries

    BoundingBoxRangeQuery(BoundingBox searchBoundingBox) {
        this.searchBoundingBox = searchBoundingBox;
    }

    @Override
    ArrayList<Long> getQueryRecordIds(Node node){
        qualifyingRecordIds = new ArrayList<>();
        search(node);
        return qualifyingRecordIds;
    }

    // Search for records within searchBoundingBox
    private void search(Node node){
        // [Search subtrees]
        // If T is not a leaf check each entry E to determine whether E.R
        //overlaps searchBoundingBox.
        if (node.getLevel() != RStarTree.LEAF_LEVEL)
            for (Entry entry: node.getEntries())
            {
                // For all overlapping entries, invoke Search on the tree whose root is
                // pointed to by E.childPTR.
                if (BoundingBox.checkOverlap(entry.getBoundingBox(),searchBoundingBox))
                    search( MetaData.readIndexFileBlock(entry.getChildNodeBlockId()));
            }

            // [Search leaf node]
            // If T is a leaf, check all entries E to determine whether E.r overlaps S.
            // If so, E is a qualifying record
        else
            for (Entry entry: node.getEntries())
            {
                // For all overlapping entries, invoke Search on the tree whose root is
                // pointed to by E.childPTR.
                if (BoundingBox.checkOverlap(entry.getBoundingBox(),searchBoundingBox))
                {
                    LeafEntry leafEntry = (LeafEntry) entry;
                    qualifyingRecordIds.add(leafEntry.getRecordId());
                }
            }
    }
}
