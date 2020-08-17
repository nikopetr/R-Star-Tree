import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

// Class used for executing a k-nearest neighbours query of a specific search point without any use of an index
// Finds the k closest records of that search point
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

    // Returns the ids of the query's records
   @Override
   ArrayList<Long> getQueryRecordIds() {
       ArrayList<Long> qualifyingRecordIds = new ArrayList<>();
       findNeighbours();
       while (nearestNeighbours.size() != 0)
       {
           IdDistancePair recordDistancePair = nearestNeighbours.poll();
           qualifyingRecordIds.add(recordDistancePair.getRecordId());
       }
       Collections.reverse(qualifyingRecordIds); // In order to return closest neighbours first instead of farthest
       return qualifyingRecordIds;
   }

   private void findNeighbours(){
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

                       entries.add(new LeafEntry(record.getId(), blockId, boundsForEachDimension));
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
               }
               else
                   throw new IllegalStateException("Could not read records properly from the datafile");
               blockId++;
           }
       }
}

