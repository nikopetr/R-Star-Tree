class Entry {
    private BoundingBox boundingBox;
    private Node childNode;

    Entry(Node childNode) {
        this.childNode = childNode;
        this.boundingBox = childNode.getOverallBoundingBox();
    }

    Entry(BoundingBox boundingBox)
    {
        this.boundingBox = boundingBox;
    }

    void setChildNode(Node childNode) {
        this.childNode = childNode;
    }

    void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    BoundingBox getBoundingBox() {
        return boundingBox;
    }

    Node getChildNode() {
        return childNode;
    }
}
