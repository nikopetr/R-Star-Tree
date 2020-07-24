import java.util.ArrayList;
import java.util.Collections;

class RStarTree {

    static final int LEAF_LEVEL = 1; // Constant leaf level 1, since we are increasing the level from the root, the root (top level) will always have the highest level
    private int totalLevels; // The total levels of the tree
    private boolean[] levelsInserted; // Used to know which levels have already called overFlowTreatment on a data insertion procedure
    static final int ROOT_NODE_BLOCK_ID = 0; // Root node will always have 0 as it's ID, in order to identify which block has the root Node
    private static final int CHOOSE_SUBTREE_P_ENTRIES = 32;
    private static final int REINSERT_P_ENTRIES = (int) (0.30 * Node.MAX_ENTRIES); // Setting p to 30% of max entries


    RStarTree() {
        MetaData.writeNewIndexFileBlock(new Node(1));
        this.totalLevels = 1; // We are increasing the size from the root, the root (top level) will always have the highest level
    }

    Node getRoot() {
        return MetaData.readIndexFileBlock(ROOT_NODE_BLOCK_ID);
    }

    // Query which returns the ids of the K Records that are closer to the given point
    ArrayList<Long> getNearestNeighbours(ArrayList<Double> searchPoint, int k){
        Query query = new NearestNeighboursQuery(searchPoint,k);
        return query.getQueryRecordIds(MetaData.readIndexFileBlock(ROOT_NODE_BLOCK_ID));
    }

    // Query which returns the ids of the Records that are inside the given searchBoundingBox
    ArrayList<Long> getDataInBoundingBox(BoundingBox searchBoundingBox){
        Query query = new BoundingBoxRangeQuery(searchBoundingBox);
        return query.getQueryRecordIds(MetaData.readIndexFileBlock(ROOT_NODE_BLOCK_ID));
    }

    // Query which returns the ids of the Records that are inside the radius of the given point
    ArrayList<Long> getDataInCircle(ArrayList<Double> searchPoint, double radius){
        Query query = new PointRadiusQuery(searchPoint,radius);
        return query.getQueryRecordIds(MetaData.readIndexFileBlock(ROOT_NODE_BLOCK_ID));
    }

    void insertRecord(Record record) {
        ArrayList<Bounds> boundsForEachDimension = new ArrayList<>();
        // Since we have to do with points as records we set low and upper to be same
        for (int d = 0; d < MetaData.DIMENSIONS; d++)
            boundsForEachDimension.add(new Bounds(record.getCoordinate(d),record.getCoordinate(d)));

        levelsInserted = new boolean[totalLevels];
        insert(null, null, new LeafEntry(record.getId(), boundsForEachDimension), LEAF_LEVEL);
    }

    // Inserts nodes recursively. As an optimization, the algorithm steps are
    // in a different order. If this returns a non null Entry, then
    // that Entry should be added to the caller's Node of the R-tree
    private Entry insert(Node parentNode, Entry parentEntry,  Entry dataEntry, int levelToAdd) {

        Node childNode;
        long idToRead;

        if(parentEntry == null)
            idToRead = ROOT_NODE_BLOCK_ID;

        else
        {
            // Updating-Adjusting the bounding box of the Entry that points to the Updated Node
            parentEntry.adjustBBToFitEntry(dataEntry);
            MetaData.updateIndexFileBlock(parentNode,parentNode.getBlockId());
            idToRead = parentEntry.getChildNodeBlockId();
        }

        childNode = MetaData.readIndexFileBlock(idToRead);
        if (childNode == null)
            throw new IllegalStateException("The Node-block read from file is null");

        // CS2: If we're at a leaf (or the level we wanted to insert the dataEntry), then use that level
        // I2: If N has less than M items, accommodate E in N
        if (childNode.getLevel() == levelToAdd)
        {
            childNode.insertEntry(dataEntry);
            MetaData.updateIndexFileBlock(childNode,childNode.getBlockId());
        }

        else {
            // I1: Invoke ChooseSubtree. with the level as a parameter,
            // to find an appropriate node N, m which to place the
            // new leaf E

            // Recurse to get the node that the new data entry will fit better
            Entry bestEntry = chooseSubTree(childNode, dataEntry.getBoundingBox(), levelToAdd);
            // Receiving a new Entry if the recursion caused the next level's Node to split
            Entry newEntry = insert(childNode, bestEntry, dataEntry, levelToAdd);

            childNode = MetaData.readIndexFileBlock(idToRead);
            if (childNode == null)
                throw new IllegalStateException("The Node-block read from file is null");

            // If split was called on children, the new entry that the split caused gets joined to the list of items at this level
            if (newEntry != null)
            {
                childNode.insertEntry(newEntry);
                MetaData.updateIndexFileBlock(childNode,childNode.getBlockId());
            }

            // Else no split was called on children, returning null upwards
            else
            {
                MetaData.updateIndexFileBlock(childNode,childNode.getBlockId());
                return null;
            }
        }

        // If N has M+1 items. invoke OverflowTreatment with the
        // level of N as a parameter [for reinsertion or split]
        if (childNode.getEntries().size() > Node.MAX_ENTRIES)
        {
            // I3: If OverflowTreatment was called and a split was
            // performed, propagate OverflowTreatment upwards
            // if necessary
            return overFlowTreatment(parentNode,parentEntry,childNode);
        }

        return null;
    }

    // Choose subtree: only pass this items that do not have leaves
    // I took out the loop portion of this algorithm, so it only
    // picks a subtree at that particular level
    private Entry chooseSubTree(Node node, BoundingBox boundingBoxToAdd, int levelToAdd) {

        Entry bestEntry;

        // If the child pointers in N point to leaves
        if (node.getLevel() == levelToAdd+1)
        {
            // Alternative for large node sizes, determine the nearly minimum overlap cost
            if (Node.MAX_ENTRIES > (CHOOSE_SUBTREE_P_ENTRIES *2)/3  && node.getEntries().size() > CHOOSE_SUBTREE_P_ENTRIES)
            {
                // Sort the rectangles in N in increasing order of
                // then area enlargement needed to include the new data rectangle

                // Let A be the group of the first p entries
                ArrayList<EntryAreaEnlargementPair> entryAreaEnlargementPairs = new ArrayList<>();

                for (Entry entry: node.getEntries())
                {
                    BoundingBox newBoundingBoxA = new BoundingBox(Bounds.findMinimumBounds(entry.getBoundingBox(),boundingBoxToAdd));
                    double areaEnlargementA = newBoundingBoxA.getArea() - entry.getBoundingBox().getArea();
                    entryAreaEnlargementPairs.add(new EntryAreaEnlargementPair(entry,areaEnlargementA));
                }

                entryAreaEnlargementPairs.sort(EntryAreaEnlargementPair::compareTo);

                ArrayList<Entry> sortedByEnlargementEntries = new ArrayList<>();
                for (EntryAreaEnlargementPair pair: entryAreaEnlargementPairs)
                    sortedByEnlargementEntries.add(pair.getEntry());


                // From the items in A, considering all items in N, choose the entry
                // whose rectangle needs least overlap enlargement
                bestEntry = Collections.min(sortedByEnlargementEntries.subList(0, CHOOSE_SUBTREE_P_ENTRIES), new EntryComparator.EntryOverlapEnlargementComparator(boundingBoxToAdd,node.getEntries()));

                return bestEntry;
            }

            // Choose the entry in N whose rectangle needs least overlap enlargement to include the new data
            // rectangle Resolve ties by choosing the entry whose rectangle needs least area enlargement,
            // then the entry with the rectangle of smallest area
            bestEntry = Collections.min(node.getEntries(), new EntryComparator.EntryOverlapEnlargementComparator(boundingBoxToAdd,node.getEntries()));
            return bestEntry;
        }

        // if the child pointers in N do not point to leaves: determine the minimum area cost],
        // choose the leaf in N whose rectangle needs least area enlargement to include the new data
        // rectangle. Resolve ties by choosing the leaf with the rectangle of smallest area
        ArrayList<EntryAreaEnlargementPair> entryAreaEnlargementPairs = new ArrayList<>();
        for (Entry entry: node.getEntries())
        {
            BoundingBox newBoundingBoxA = new BoundingBox(Bounds.findMinimumBounds(entry.getBoundingBox(),boundingBoxToAdd));
            double areaEnlargementA = newBoundingBoxA.getArea() - entry.getBoundingBox().getArea();
            entryAreaEnlargementPairs.add(new EntryAreaEnlargementPair(entry,areaEnlargementA));
        }

        bestEntry = Collections.min(entryAreaEnlargementPairs,EntryAreaEnlargementPair::compareTo).getEntry();
        return bestEntry;
    }

    // Algorithm OverflowTreatment
    private Entry overFlowTreatment(Node parentNode, Entry parentEntry, Node childNode) {

        // If the level is not the root level and this is the first call of OverflowTreatment
        // in the given level during the insertion of one data rectangle, then reinsert
        if (childNode.getBlockId() != ROOT_NODE_BLOCK_ID && !levelsInserted[childNode.getLevel()-1])
        {
            levelsInserted[childNode.getLevel()-1] = true; // Mark level as already inserted
            reInsert(parentNode,parentEntry,childNode);
            return null;
        }

        // TODO check this line might not be needed
        levelsInserted[childNode.getLevel()-1] = true; // Mark level as already inserted

        // Else invoke Split
        ArrayList<Node> splitNodes = childNode.splitNode(); // The two nodes occurring after the split
        if (splitNodes.size() != 2)
            throw new IllegalStateException("The resulting Nodes after a split cannot be more or less than two");
        childNode.setEntries(splitNodes.get(0).getEntries()); // Adjusting the previous Node with the new entries
        Node splitNode = splitNodes.get(1); // The new Node that occurred from the split

        // Updating the file with the new changes of the split nodes
        if (childNode.getBlockId() != ROOT_NODE_BLOCK_ID)
        {
            MetaData.updateIndexFileBlock(childNode,childNode.getBlockId());
            splitNode.setBlockId(MetaData.getTotalBlocksInIndexFile());
            MetaData.writeNewIndexFileBlock(splitNode);

            // Propagate the overflow treatment upwards, to fit the entry on the caller's level Node
            parentEntry.adjustBBToFitEntries(childNode.getEntries()); // Adjusting the bounding box of the Entry that points to the updated Node
            MetaData.updateIndexFileBlock(parentNode,parentNode.getBlockId()); // Write changes to file
            return new Entry(splitNode);
        }

        // Else if OverflowTreatment caused a split of the root, create a new root

        // Creating two Node-blocks for the split
        childNode.setBlockId(MetaData.getTotalBlocksInIndexFile());
        MetaData.writeNewIndexFileBlock(childNode);
        splitNode.setBlockId(MetaData.getTotalBlocksInIndexFile());
        MetaData.writeNewIndexFileBlock(splitNode);

        // Updating the root Node-block with the new root Node
        ArrayList<Entry> newRootEntries = new ArrayList<>();
        newRootEntries.add(new Entry(childNode));
        newRootEntries.add(new Entry(splitNode));
        Node newRoot = new Node(++totalLevels,newRootEntries);
        newRoot.setBlockId(ROOT_NODE_BLOCK_ID);
        MetaData.updateIndexFileBlock(newRoot,newRoot.getBlockId());
        return null;
    }

    // Algorithm reinsert
    private void reInsert(Node parentNode, Entry parentEntry, Node childNode) {

        if(childNode.getEntries().size() != Node.MAX_ENTRIES + 1)
            throw new IllegalStateException("Cannot throw reinsert for node with total entries fewer than M+1");

        // RI1 For all M+l items of a node N, compute the distance between the centers of their rectangles
        // and the center of the bounding rectangle of N

        // RI2: Sort the items in INCREASING order (since then we use close reinsert)
        // of their distances computed in RI1
        childNode.getEntries().sort(new EntryComparator.EntryDistanceFromCenterComparator(parentEntry.getBoundingBox()));
        ArrayList<Entry> removedEntries = new ArrayList<>(childNode.getEntries().subList(childNode.getEntries().size()-REINSERT_P_ENTRIES,childNode.getEntries().size()));

        // RI3: Remove the last p items from N (since then we use close reinsert) and adjust the bounding rectangle of N
        for(int i = 0; i < REINSERT_P_ENTRIES; i++)
            childNode.getEntries().remove(childNode.getEntries().size()-1);

        // Updating bounding box of node and to the parent entry
        parentEntry.adjustBBToFitEntries(childNode.getEntries());
        MetaData.updateIndexFileBlock(parentNode,parentNode.getBlockId());
        MetaData.updateIndexFileBlock(childNode,childNode.getBlockId());

        // RI4: In the sort, defined in RI2, starting with the minimum distance (= close reinsert),
        // invoke Insert to reinsert the items
        if(removedEntries.size() != REINSERT_P_ENTRIES)
            throw new IllegalStateException("Entries queued for reinsert have different size than the ones that were removed");

        for (Entry entry : removedEntries)
            insert(null,null,entry,childNode.getLevel());
    }
}