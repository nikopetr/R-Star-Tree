import java.util.ArrayList;

// Class used for executing a range query withing a specific bounding box without the use of an index
// Searches for records within that bounding box
class SequentialScanBoundingBoxRangeQuery extends SequentialScanQuery {

    private ArrayList<Long> qualifyingRecordIds; // Record ids used for queries
    private BoundingBox searchBoundingBox; // Bounding box used for range queries

    SequentialScanBoundingBoxRangeQuery(BoundingBox searchBoundingBox) {
        this.searchBoundingBox = searchBoundingBox;
    }

    // Returns the ids of the query's records
    @Override
    ArrayList<Long> getQueryRecordIds() {
        qualifyingRecordIds = new ArrayList<>();
        search();
        return qualifyingRecordIds;
    }

    private void search(){
        int blockId = 1;
        while(blockId < FilesHelper.getTotalBlocksInDatafile())
        {
            ArrayList<Record> recordsInBlock;
            recordsInBlock = FilesHelper.readDataFileBlock(blockId);
            ArrayList<LeafEntry> entries = new ArrayList<>();

            if (recordsInBlock != null)
            {
                for (Record record : recordsInBlock)
                {
                    ArrayList<Bounds> boundsForEachDimension = new ArrayList<>();
                    // Since we have to do with points as records we set low and upper to be same
                    for (int d = 0; d < FilesHelper.getDataDimensions(); d++)
                        boundsForEachDimension.add(new Bounds(record.getCoordinate(d), record.getCoordinate(d)));

                    entries.add(new LeafEntry(record.getId(), boundsForEachDimension));
                }

                for(Entry entry : entries)
                {
                    if(BoundingBox.checkOverlap(entry.getBoundingBox(), searchBoundingBox)){
                    LeafEntry leafEntry = (LeafEntry) entry;
                    qualifyingRecordIds.add(leafEntry.getRecordId());
                    }
                }
            }
            else
                throw new IllegalStateException("Could not read records properly from the datafile");
            blockId++;
            }
        }
}
