import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

 class SequentialNearestNeighboursQuery extends SequentialScanQuery {
    private ArrayList<Double> searchPoint;
    private int k;
    private PriorityQueue<IdDistancePair> nearestNeighbours;


    SequentialNearestNeighboursQuery(ArrayList<Double> searchPoint, int k) {
        if (k < 0)
            throw new IllegalArgumentException("Parameter 'k' for the nearest neighbours must be a positive integer.");
        this.searchPoint = searchPoint;
        this.k = k;
        this.nearestNeighbours = new PriorityQueue<>(k, new Comparator<IdDistancePair>() {
            @Override
            public int compare(IdDistancePair recordDistancePairA, IdDistancePair recordDistancePairB) {
                return Double.compare(recordDistancePairB.getDistanceFromItem(),recordDistancePairA.getDistanceFromItem()); // In order to make a MAX heap
            }
        });
    }
    @Override
    ArrayList<Long> getQueryRecordIds(int blockId) {
        ArrayList<Long> qualifyingRecordIds = new ArrayList<>();
        findNeighbours(blockId);
        while (nearestNeighbours.size() != 0)
        {
            IdDistancePair recordDistancePair = nearestNeighbours.poll();
            qualifyingRecordIds.add(recordDistancePair.getRecordId());
        }
        return qualifyingRecordIds;
    }

    private void findNeighbours(int blockId){

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

                int i = 0;
                while(i < entries.size()){
                    double distanceFromPoint = entries.get(i).getBoundingBox().findMinDistanceFromPoint(searchPoint);
                    if(nearestNeighbours.size() == k){
                        if(distanceFromPoint < nearestNeighbours.peek().getDistanceFromItem()){
                            nearestNeighbours.poll();
                            nearestNeighbours.add(new IdDistancePair(entries.get(i).getRecordId(), distanceFromPoint));

                        }
                    }else{
                        nearestNeighbours.add(new IdDistancePair(entries.get(i).getRecordId(), distanceFromPoint));
                    }

                    i++;
                }

                blockId++;

                findNeighbours(blockId);
            }else{

                return;
            }
        }
}

