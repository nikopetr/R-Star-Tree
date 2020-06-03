class IdDistancePair {
    private long recordId;
    private double distanceFromItem;

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
