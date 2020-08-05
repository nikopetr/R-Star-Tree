import java.util.ArrayList;
import java.util.Objects;

// Class used for executing a range query withing a specific circle with the use of the RStarTree
// Searches for records within that circle
class PointRadiusQuery extends Query {
    private ArrayList<Long> qualifyingRecordIds; // Record ids used for queries
    private ArrayList<Double> searchPoint; // coordinates of point used for radius queries
    private double searchPointRadius; // The radius of the point which will be used for the query

    PointRadiusQuery(ArrayList<Double> searchPoint, Double searchPointRadius) {
        this.searchPoint = searchPoint;
        this.searchPointRadius = searchPointRadius;
    }

    // Returns the ids of the query's records
    @Override
    ArrayList<Long> getQueryRecordIds(Node node){
        qualifyingRecordIds = new ArrayList<>();
        search(node);
        return qualifyingRecordIds;
    }

    // Search for Records within searchPoint's radius
    private void search(Node node){
        // [Search subtrees]
        // If node does not point to leaves check each entry E to determine whether E.R
        // overlaps with the searchPoint.
        if (node.getLevel() != RStarTree.getLeafLevel())
            for (Entry entry: node.getEntries())
            {
                // For all overlapping entries, invoke Search on the tree whose root is
                // pointed to by E.childPTR.
                if (entry.getBoundingBox().checkOverLapWithPoint(searchPoint,searchPointRadius))
                    search(Objects.requireNonNull(FilesHelper.readIndexFileBlock(entry.getChildNodeBlockId())));
            }

            // [Search leaf node]
            // If point to leaves, check all entries E to determine whether E.r overlaps S.
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
