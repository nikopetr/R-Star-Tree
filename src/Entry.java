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
      //  if (this instanceof LeafEntry)
            return boundingBox;
     //   else
          //  return childNode.getOverallBoundingBox();
    }

    Node getChildNode() {
        return childNode;
    }
}
