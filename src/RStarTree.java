import java.util.ArrayList;
import java.util.Collections;

class RStarTree {

    private int totalLevels; // The total levels of the tree, increasing the size starting of the root, the root (top level) will always have the highest level
    private boolean[] levelsInserted; // Used for information on  which levels have already called overFlowTreatment on a data insertion procedure
    private static final int ROOT_NODE_BLOCK_ID = 1; // Root node will always have 1 as it's ID, in order to identify which block has the root Node
    private static final int LEAF_LEVEL = 1; // Constant leaf level 1, since we are increasing the level from the root, the root (top level) will always have the highest level
    private static final int CHOOSE_SUBTREE_P_ENTRIES = 32;
    private static final int REINSERT_P_ENTRIES = (int) (0.30 * Node.getMaxEntries()); // Setting p to 30% of max entries

    // RStarTree constructor
    // If insertRecordsFromDataFile parameter is true then makes a new root node since we are resetting the tree and inserting the records from the datafile
    RStarTree(boolean insertRecordsFromDataFile) {
        this.totalLevels = FilesHelper.getTotalLevelsOfTreeIndex(); // Initialise the total levels from the FileHelper class, in case there is an already existing indexFile
        if (insertRecordsFromDataFile)
        {
            FilesHelper.writeNewIndexFileBlock(new Node(1)); // Initialising the root node

            // Adding the data of datafile in the RStarTree (to the indexFile)
            for (int i = 1; i< FilesHelper.getTotalBlocksInDatafile(); i++)
            {
                ArrayList<Record> records = FilesHelper.readDataFileBlock(i);
                if (records != null)
                {
                    for (Record record : records)
                        insertRecord(record);
                }
                else
                    throw new IllegalStateException("Could not read records properly from the datafile");
            }
        }
//        ArrayList<Double> recCoordinates = new ArrayList<>();
//        recCoordinates.add(-100.0);
//        recCoordinates.add(1.0);
//        insertRecord(new Record(1, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(-80.0);
//        recCoordinates.add(-1.0);
//        insertRecord(new Record(2, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(4.0);
//        recCoordinates.add(1.0);
//        insertRecord(new Record(3, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(5.0);
//        recCoordinates.add(0.0);
//        insertRecord(new Record(4, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(14.0);
//        recCoordinates.add(1.0);
//        insertRecord(new Record(5, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(2.0);
//        recCoordinates.add(1.0);
//        insertRecord(new Record(6, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(2.0);
//        recCoordinates.add(0.1);
//        insertRecord(new Record(7, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(-101.0);
//        recCoordinates.add(0.1);
//        insertRecord(new Record(8, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(-102.0);
//        recCoordinates.add(0.1);
//        insertRecord(new Record(9, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(-125.0);
//        recCoordinates.add(1.0);
//        insertRecord(new Record(10, recCoordinates));

//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(9.0);
//        recCoordinates.add(0.9);
//        insertRecord(new Record(11, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(-1.0);
//        recCoordinates.add(0.0);
//        insertRecord(new Record(12, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(23.0);
//        recCoordinates.add(1.7);
//        insertRecord(new Record(13, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(12.0);
//        recCoordinates.add(10.0);
//        insertRecord(new Record(14, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(20.0);
//        recCoordinates.add(-2.0);
//        insertRecord(new Record(15, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(2.0);
//        recCoordinates.add(-0.1);
//        insertRecord(new Record(16, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(-1.0);
//        recCoordinates.add(-2.0);
//        insertRecord(new Record(17, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(1.0);
//        recCoordinates.add(1.0);
//        insertRecord(new Record(18, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(15.0);
//        recCoordinates.add(-1.0);
//        insertRecord(new Record(19, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(-136.0);
//        recCoordinates.add(1.0);
//        insertRecord(new Record(20, recCoordinates));
    }

    Node getRoot() {
        return FilesHelper.readIndexFileBlock(ROOT_NODE_BLOCK_ID);
    }

    static int getRootNodeBlockId() {
        return ROOT_NODE_BLOCK_ID;
    }

    static int getLeafLevel() {
        return LEAF_LEVEL;
    }

    // Query which returns the ids of the K Records that are closer to the given point
    ArrayList<Long> getNearestNeighbours(ArrayList<Double> searchPoint, int k){
        Query query = new NearestNeighboursQuery(searchPoint,k);
        return query.getQueryRecordIds(FilesHelper.readIndexFileBlock(ROOT_NODE_BLOCK_ID));
    }

    // Query which returns the ids of the Records that are inside the given searchBoundingBox
    ArrayList<Long> getDataInBoundingBox(BoundingBox searchBoundingBox){
        Query query = new BoundingBoxRangeQuery(searchBoundingBox);
        return query.getQueryRecordIds(FilesHelper.readIndexFileBlock(ROOT_NODE_BLOCK_ID));
    }

    // Query which returns the ids of the Records that are inside the radius of the given point
    ArrayList<Long> getDataInCircle(ArrayList<Double> searchPoint, double radius){
        Query query = new PointRadiusQuery(searchPoint,radius);
        return query.getQueryRecordIds(FilesHelper.readIndexFileBlock(ROOT_NODE_BLOCK_ID));
    }

    private void insertRecord(Record record) {
        ArrayList<Bounds> boundsForEachDimension = new ArrayList<>();
        // Since we have to do with points as records we set low and upper to be same
        for (int d = 0; d < FilesHelper.getDataDimensions(); d++)
            boundsForEachDimension.add(new Bounds(record.getCoordinate(d),record.getCoordinate(d)));

        levelsInserted = new boolean[totalLevels];
        insert(null, null, new LeafEntry(record.getId(), boundsForEachDimension), LEAF_LEVEL); // Inserting on leaf level since it's a new record
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
            FilesHelper.updateIndexFileBlock(parentNode,totalLevels);
            idToRead = parentEntry.getChildNodeBlockId();
        }

        childNode = FilesHelper.readIndexFileBlock(idToRead);
        if (childNode == null)
            throw new IllegalStateException("The Node-block read from file is null");

        // CS2: If we're at a leaf (or the level we wanted to insert the dataEntry), then use that level
        // I2: If N has less than M items, accommodate E in N
        if (childNode.getLevel() == levelToAdd)
        {
            childNode.insertEntry(dataEntry);
            FilesHelper.updateIndexFileBlock(childNode,totalLevels);
        }

        else {
            // I1: Invoke ChooseSubtree. with the level as a parameter,
            // to find an appropriate node N, m which to place the
            // new leaf E

            // Recurse to get the node that the new data entry will fit better
            Entry bestEntry = chooseSubTree(childNode, dataEntry.getBoundingBox(), levelToAdd);
            // Receiving a new Entry if the recursion caused the next level's Node to split
            Entry newEntry = insert(childNode, bestEntry, dataEntry, levelToAdd);

            childNode = FilesHelper.readIndexFileBlock(idToRead);
            if (childNode == null)
                throw new IllegalStateException("The Node-block read from file is null");

            // If split was called on children, the new entry that the split caused gets joined to the list of items at this level
            if (newEntry != null)
            {
                childNode.insertEntry(newEntry);
                FilesHelper.updateIndexFileBlock(childNode,totalLevels);
            }

            // Else no split was called on children, returning null upwards
            else
            {
                FilesHelper.updateIndexFileBlock(childNode,totalLevels);
                return null;
            }
        }

        // If N has M+1 items. invoke OverflowTreatment with the
        // level of N as a parameter [for reinsertion or split]
        if (childNode.getEntries().size() > Node.getMaxEntries())
        {
            // I3: If OverflowTreatment was called and a split was
            // performed, propagate OverflowTreatment upwards
            // if necessary
            return overFlowTreatment(parentNode,parentEntry,childNode);
        }

        return null;
    }

    // Returns a the best Entry of the sub tree to place the new index entry
    // The loop portion of this algorithm is taken out, so it only picks a subtree at that particular level
    private Entry chooseSubTree(Node node, BoundingBox boundingBoxToAdd, int levelToAdd) {

        Entry bestEntry;

        // If the child pointers in N point to leaves
        if (node.getLevel() == levelToAdd+1)
        {
            // Alternative for large node sizes, determine the nearly minimum overlap cost
            if (Node.getMaxEntries() > (CHOOSE_SUBTREE_P_ENTRIES *2)/3  && node.getEntries().size() > CHOOSE_SUBTREE_P_ENTRIES)
            {
                // Sorting the entries in the node in increasing order of
                // their area enlargement needed to include the new data rectangle
                ArrayList<EntryAreaEnlargementPair> entryAreaEnlargementPairs = new ArrayList<>();
                for (Entry entry: node.getEntries())
                {
                    BoundingBox newBoundingBoxA = new BoundingBox(Bounds.findMinimumBounds(entry.getBoundingBox(),boundingBoxToAdd));
                    double areaEnlargementA = newBoundingBoxA.getArea() - entry.getBoundingBox().getArea();
                    entryAreaEnlargementPairs.add(new EntryAreaEnlargementPair(entry,areaEnlargementA));
                }
                entryAreaEnlargementPairs.sort(EntryAreaEnlargementPair::compareTo);
                // Let sortedByEnlargementEntries be the group of the sorted entries
                ArrayList<Entry> sortedByEnlargementEntries = new ArrayList<>();
                for (EntryAreaEnlargementPair pair: entryAreaEnlargementPairs)
                    sortedByEnlargementEntries.add(pair.getEntry());

                // From the items in sortedByEnlargementEntries, let A be the group of the first p entries,
                // considering all items in the node, choosing the entry whose rectangle needs least overlap enlargement
                //ArrayList<Entry> pFirstEntries = (ArrayList<Entry>)sortedByEnlargementEntries.subList(0, CHOOSE_SUBTREE_P_ENTRIES);
                bestEntry = Collections.min(sortedByEnlargementEntries.subList(0, CHOOSE_SUBTREE_P_ENTRIES), new EntryComparator.EntryOverlapEnlargementComparator(sortedByEnlargementEntries.subList(0, CHOOSE_SUBTREE_P_ENTRIES),boundingBoxToAdd,node.getEntries()));

                return bestEntry;
            }

            // Choose the entry in the node whose rectangle needs least overlap enlargement to include the new data rectangle
            // Resolve ties by choosing the entry whose rectangle needs least area enlargement,
            // then the entry with the rectangle of smallest area
            bestEntry = Collections.min(node.getEntries(), new EntryComparator.EntryOverlapEnlargementComparator(node.getEntries(),boundingBoxToAdd,node.getEntries()));
            return bestEntry;
        }

        // If the child pointers in N do not point to leaves: determine the minimum area cost],
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
//        levelsInserted[childNode.getLevel()-1] = true; // Mark level as already inserted

        // Else invoke Split
        ArrayList<Node> splitNodes = childNode.splitNode(); // The two nodes occurring after the split
        if (splitNodes.size() != 2)
            throw new IllegalStateException("The resulting Nodes after a split cannot be more or less than two");
        childNode.setEntries(splitNodes.get(0).getEntries()); // Adjusting the previous Node with the new entries
        Node splitNode = splitNodes.get(1); // The new Node that occurred from the split

        // Updating the file with the new changes of the split nodes
        if (childNode.getBlockId() != ROOT_NODE_BLOCK_ID)
        {
            FilesHelper.updateIndexFileBlock(childNode,totalLevels);
            splitNode.setBlockId(FilesHelper.getTotalBlocksInIndexFile());
            FilesHelper.writeNewIndexFileBlock(splitNode);

            // Propagate the overflow treatment upwards, to fit the entry on the caller's level Node
            parentEntry.adjustBBToFitEntries(childNode.getEntries()); // Adjusting the bounding box of the Entry that points to the updated Node
            FilesHelper.updateIndexFileBlock(parentNode,totalLevels); // Write changes to file
            return new Entry(splitNode);
        }

        // Else if OverflowTreatment caused a split of the root, create a new root

        // Creating two Node-blocks for the split
        childNode.setBlockId(FilesHelper.getTotalBlocksInIndexFile());
        FilesHelper.writeNewIndexFileBlock(childNode);
        splitNode.setBlockId(FilesHelper.getTotalBlocksInIndexFile());
        FilesHelper.writeNewIndexFileBlock(splitNode);

        // Updating the root Node-block with the new root Node
        ArrayList<Entry> newRootEntries = new ArrayList<>();
        newRootEntries.add(new Entry(childNode));
        newRootEntries.add(new Entry(splitNode));
        Node newRoot = new Node(++totalLevels,newRootEntries);
        newRoot.setBlockId(ROOT_NODE_BLOCK_ID);
        FilesHelper.updateIndexFileBlock(newRoot,totalLevels);
        return null;
    }

    // Algorithm reinsert
    private void reInsert(Node parentNode, Entry parentEntry, Node childNode) {

        if(childNode.getEntries().size() != Node.getMaxEntries() + 1)
            throw new IllegalStateException("Cannot throw reinsert for node with total entries fewer than M+1");

        // RI1 For all M+l items of a node N, compute the distance between the centers of their rectangles
        // and the center of the bounding rectangle of N

        // RI2: Sort the items in INCREASING order (since then we use close reinsert)
        // of their distances computed in RI1
        childNode.getEntries().sort(new EntryComparator.EntryDistanceFromCenterComparator(childNode.getEntries(),parentEntry.getBoundingBox()));
        ArrayList<Entry> removedEntries = new ArrayList<>(childNode.getEntries().subList(childNode.getEntries().size()-REINSERT_P_ENTRIES,childNode.getEntries().size()));

        // RI3: Remove the last p items from N (since then we use close reinsert) and adjust the bounding rectangle of N
        for(int i = 0; i < REINSERT_P_ENTRIES; i++)
            childNode.getEntries().remove(childNode.getEntries().size()-1);

        // Updating bounding box of node and to the parent entry
        parentEntry.adjustBBToFitEntries(childNode.getEntries());
        FilesHelper.updateIndexFileBlock(parentNode,totalLevels);
        FilesHelper.updateIndexFileBlock(childNode,totalLevels);

        // RI4: In the sort, defined in RI2, starting with the minimum distance (= close reinsert),
        // invoke Insert to reinsert the items
        if(removedEntries.size() != REINSERT_P_ENTRIES)
            throw new IllegalStateException("Entries queued for reinsert have different size than the ones that were removed");

        for (Entry entry : removedEntries)
            insert(null,null,entry,childNode.getLevel());
    }
}