import java.util.ArrayList;

// Class used for executing a range query withing a specific bounding box with the use of the RStarTree
// Searches for records within that bounding box
class BoundingBoxRangeQuery extends Query {
    private ArrayList<Long> qualifyingRecordIds; // Record ids used for queries
    private BoundingBox searchBoundingBox; // BoundingBox used for range queries

    BoundingBoxRangeQuery(BoundingBox searchBoundingBox) {
        this.searchBoundingBox = searchBoundingBox;
    }

    // Returns the ids of the query's records
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
        if (node.getLevel() != RStarTree.getLeafLevel())
            for (Entry entry: node.getEntries())
            {
                // For all overlapping entries, invoke Search on the tree whose root is
                // pointed to by E.childPTR.
                if (BoundingBox.checkOverlap(entry.getBoundingBox(),searchBoundingBox))
                    search( FilesHelper.readIndexFileBlock(entry.getChildNodeBlockId()));
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
