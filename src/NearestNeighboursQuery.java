import java.util.*;

class NearestNeighboursQuery extends Query {
    private ArrayList<Double> searchPoint; // coordinates of point used for radius queries
    private double searchPointRadius; // The reference radius that is used as a bound
    private int k; // Number of nearest neighbours to be found
    private PriorityQueue<IdDistancePair> nearestNeighbours;

    NearestNeighboursQuery(ArrayList<Double> searchPoint, int k) {
        if (k < 0)
            throw new IllegalArgumentException("Parameter 'k' for the nearest neighbours must be a positive integer.");
        this.searchPoint = searchPoint;
        this.k = k;
        this.searchPointRadius = Double.MAX_VALUE;
        this.nearestNeighbours = new PriorityQueue<>(k, new Comparator<IdDistancePair>() {
            @Override
            public int compare(IdDistancePair recordDistancePairA, IdDistancePair recordDistancePairB) {
                return Double.compare(recordDistancePairB.getDistanceFromItem(),recordDistancePairA.getDistanceFromItem()); // In order to make a MAX heap
            }
        });
    }

    @Override
    // TODO make return to be in min instead of max
    ArrayList<Long> getQueryRecordIds(Node node) {
        ArrayList<Long> qualifyingRecordIds = new ArrayList<>();
        findNeighbours(node);
        while (nearestNeighbours.size() != 0)
        {
            IdDistancePair recordDistancePair = nearestNeighbours.poll();
            qualifyingRecordIds.add(recordDistancePair.getRecordId());
        }
        return qualifyingRecordIds;
    }

    //TODO fix it to calculate the distance only once
    private void findNeighbours(Node node) {
        node.getEntries().sort(new EntryComparator.EntryDistanceFromPointComparator(searchPoint));
        int i = 0;
        if (node.getLevel() != RStarTree.LEAF_LEVEL) {
            while (i < node.getEntries().size() && (nearestNeighbours.size() < k || node.getEntries().get(i).getBoundingBox().findMinDistanceFromPoint(searchPoint) <= searchPointRadius))
            {
                findNeighbours(node.getEntries().get(i).getChildNode());
                i++;
            }
        }
        else {
            while (i < node.getEntries().size() && (nearestNeighbours.size() < k || node.getEntries().get(i).getBoundingBox().findMinDistanceFromPoint(searchPoint) <= searchPointRadius))
            {
                if (nearestNeighbours.size() >= k)
                    nearestNeighbours.poll();
                LeafEntry leafEntry = (LeafEntry) node.getEntries().get(i);
                double minDistance = leafEntry.getBoundingBox().findMinDistanceFromPoint(searchPoint);
                nearestNeighbours.add(new IdDistancePair(leafEntry.getRecordId(), minDistance));
                searchPointRadius = nearestNeighbours.peek().getDistanceFromItem();
                i++;
            }

        }
    }
}
