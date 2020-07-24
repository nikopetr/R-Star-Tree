import java.util.ArrayList;

class PointRadiusQuery extends Query {
    private ArrayList<Long> qualifyingRecordIds; // Record ids used for queries
    private ArrayList<Double> searchPoint; // coordinates of point used for radius queries
    private double searchPointRadius; // The radius of the point which will be used for the query

    PointRadiusQuery(ArrayList<Double> searchPoint, Double searchPointRadius) {
        this.searchPoint = searchPoint;
        this.searchPointRadius = searchPointRadius;
    }

    @Override
    ArrayList<Long> getQueryRecordIds(Node node){
        qualifyingRecordIds = new ArrayList<>();
        search(node);
        return qualifyingRecordIds;
    }

    // Search for Records within searchPoint's radius
    private void search(Node node){
        // [Search subtrees]
        // If T is not a leaf check each entry E to determine whether E.R
        //overlaps with the searchPoint.
        if (node.getLevel() != RStarTree.LEAF_LEVEL)
            for (Entry entry: node.getEntries())
            {
                // For all overlapping entries, invoke Search on the tree whose root is
                // pointed to by E.childPTR.
                if (entry.getBoundingBox().checkOverLapWithPoint(searchPoint,searchPointRadius))
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
                if (entry.getBoundingBox().checkOverLapWithPoint(searchPoint,searchPointRadius))
                {
                    LeafEntry leafEntry = (LeafEntry) entry;
                    qualifyingRecordIds.add(leafEntry.getRecordId());
                }
            }
    }
}
