import java.util.ArrayList;

public class SequentialScanBoundingBoxRangeQuery extends SequentialScanQuery {

    private ArrayList<Long> qualifyingRecordIds; // Record ids used for queries
    private BoundingBox searchBoundingBox; // bounding box used for range queries

    SequentialScanBoundingBoxRangeQuery(BoundingBox searchBoundingBox) {
        this.searchBoundingBox = searchBoundingBox;
    }
    @Override
    ArrayList<Long> getQueryRecordIds(int blockId) {
        qualifyingRecordIds = new ArrayList<>();
        search(blockId);
        return qualifyingRecordIds;
    }

    private void search(int blockId){
        if(blockId <= MetaData.calculateTotalBlocksInDataFile() - 1) {
            ArrayList<Record> recordsInBlock;
            recordsInBlock = MetaData.readDataFileBlock(blockId);
            ArrayList<LeafEntry> entries = new ArrayList<>();

            for (Record record : recordsInBlock) {
                ArrayList<Bounds> boundsForEachDimension = new ArrayList<>();
                // Since we have to do with points as records we set low and upper to be same
                for (int d = 0; d < MetaData.DIMENSIONS; d++)
                    boundsForEachDimension.add(new Bounds(record.getCoordinate(d), record.getCoordinate(d)));

                entries.add(new LeafEntry(record.getId(), boundsForEachDimension));

            }

            for(Entry entry : entries){
                if(BoundingBox.checkOverlap(entry.getBoundingBox(), searchBoundingBox)){
                    LeafEntry leafEntry = (LeafEntry) entry;
                    qualifyingRecordIds.add(leafEntry.getRecordId());
                }
            }

            blockId++;

           search(blockId);
        }else{
            return;
        }


    }
}
