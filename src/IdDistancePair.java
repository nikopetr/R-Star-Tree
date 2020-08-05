// Class which is used to hold an id of a record it's distance from a specific item
class IdDistancePair {
    private long recordId; // The id of the record
    private double distanceFromItem; // The distance from an item

    IdDistancePair(long recordId, double distanceFromItem) {
        this.recordId = recordId;
        this.distanceFromItem = distanceFromItem;
    }

    long getRecordId() {
        return recordId;
    }


    double getDistanceFromItem() {
        return distanceFromItem;
    }
}
