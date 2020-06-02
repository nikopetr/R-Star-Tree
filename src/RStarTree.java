import java.util.ArrayList;
import java.util.Collections;

class RStarTree {

    static final int LEAF_LEVEL = 1; // Constant leaf level 1, since we are increasing the level from the root, the root (top level) will always have the highest level
    private static final int CHOOSE_SUBTREE_P_ENTRIES = 32;
    private static final int REINSERT_P_ENTRIES = (int) (0.30 * Node.MAX_ENTRIES); // Setting p to 30% of max entries

    private Node root; // The root of the tree
    private int totalLevels; // The total levels of the tree
    private boolean[] levelsInserted; // Used to know which levels have already called overFlowTreatment on a data insertion procedure

    RStarTree(int dimensions) {
        MetaData.DIMENSIONS = dimensions;
        this.root = new Node(1); // We are increasing the size from the root, the root (top level) will always have the highest level
        this.totalLevels = 1;

        ArrayList<Bounds> initialBounds = new ArrayList<>();
        // For each dimension finds the max interval
        for (int d = 0; d < MetaData.DIMENSIONS; d++)
        {
            Bounds axisBound = new Bounds(-Double.MAX_VALUE,Double.MAX_VALUE);
            initialBounds.add(axisBound);
        }
        root.setOverallBoundingBox(new BoundingBox(initialBounds));

    }

    Node getRoot() { //TODO DELETE THIS AFTER TESTING
        return root;
    }

    // Query which returns the ids of the K Records that are closer to the given point
    ArrayList<Long> getNearestNeighbours(ArrayList<Double> searchPoint, int k){
        Query query = new NearestNeighboursQuery(searchPoint,k);
        return query.getQueryRecordIds(root);
    }

    // Query which returns the ids of the Records that are inside the given searchBoundingBox
    ArrayList<Long> getDataInBoundingBox(BoundingBox searchBoundingBox){
        Query query = new BoundingBoxRangeQuery(searchBoundingBox);
        return query.getQueryRecordIds(root);
    }

    // Query which returns the ids of the Records that are inside the radius of the given point
    ArrayList<Long> getDataInCircle(ArrayList<Double> searchPoint, double radius){
        Query query = new PointRadiusQuery(searchPoint,radius);
        return query.getQueryRecordIds(root);
    }

    void insertRecord(Record record) {
        ArrayList<Bounds> boundsForEachDimension = new ArrayList<>();
        // Since we have to do with points as records we set low and upper to be same
        for (int d = 0; d < MetaData.DIMENSIONS; d++)
            boundsForEachDimension.add(new Bounds(record.getCoordinate(d),record.getCoordinate(d)));

        levelsInserted = new boolean[totalLevels];
        insert(null, new LeafEntry(record.getId(), boundsForEachDimension), LEAF_LEVEL);
    }

    // Inserts nodes recursively. As an optimization, the algorithm steps are
    // way out of order. :) If this returns a non null Entry, then that Entry should
    // be added to the caller's Node of the tree
    private Entry insert(Entry parentEntry, Entry dataEntry, int levelToAdd) {

        Node childNode;
        if(parentEntry == null)
            childNode = root;
        else
            childNode = parentEntry.getChildNode();

        // Adjusting bounding box of the Node to fit the dataEntry
        childNode.adjustBoundingBoxToIncludeEntry(dataEntry);

        // CS2: If we're at a leaf (or the level we wanted to insert the dataEntry), then use that level
        // I2: If N has less than M items, accommodate E in N
        if (childNode.getLevel() == levelToAdd)
        {
            childNode.insertEntry(dataEntry);
            // Updating-Adjusting the bounding box of the Entry that points to the Updated Node
            if(childNode != root && parentEntry != null)
                parentEntry.setBoundingBox(childNode.getOverallBoundingBox());
        }

        else {
            // I1: Invoke ChooseSubtree. with the level as a parameter,
            // to find an appropriate node N, m which to place the
            // new leaf E

            // Recurse to get the node that the new data entry will fit better
            Entry bestEntry = chooseSubTree(childNode, dataEntry.getBoundingBox(), levelToAdd);

            // Receiving a new Entry if the recursion caused the next level's Node to split
            Entry newEntry = insert(bestEntry, dataEntry, levelToAdd);

            // If split was called on children, the new entry that the split caused gets joined to the list of items at this level
            if (newEntry != null)
                childNode.insertEntry(newEntry);
            // Else no split was called on children, returning null upwards
            else
                return null;
        }


        // If N has M+1 items. invoke OverflowTreatment with the
        // level of N as a parameter [for reinsertion or split]
        if (childNode.getEntries().size() > Node.MAX_ENTRIES)
        {
            // I3: If OverflowTreatment was called and a split was
            // performed, propagate OverflowTreatment upwards
            // if necessary
            return overFlowTreatment(parentEntry);
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
            if (Node.MAX_ENTRIES > (CHOOSE_SUBTREE_P_ENTRIES *2)/3  && node.getEntries().size() > CHOOSE_SUBTREE_P_ENTRIES) //TODO check this condition
            {

                // Sort the rectangles in N in increasing order of
                // then area enlargement needed to include the new
                // data rectangle

                // Let A be the group of the first p entries
//                node.getEntries().subList(0, CHOOSE_SUBTREE_P_ENTRIES - 1).sort(new EntryComparator.EntryAreaEnlargementComparator(boundingBoxToAdd));
                node.getEntries().sort(new EntryComparator.EntryAreaEnlargementComparator(boundingBoxToAdd));

                // From the items in A, considering all items in
                // N, choose the entry whose rectangle needs least
                // overlap enlargement
                bestEntry = Collections.min(node.getEntries().subList(0, CHOOSE_SUBTREE_P_ENTRIES), new EntryComparator.EntryOverlapEnlargementComparator(boundingBoxToAdd,node.getEntries()));

                return bestEntry;
            }

            // Choose the entry in N whose rectangle needs least
            // overlap enlargement to include the new data
            // rectangle Resolve ties by choosing the entry
            // whose rectangle needs least area enlargement, then
            // the entry with the rectangle of smallest area
            bestEntry = Collections.min(node.getEntries(), new EntryComparator.EntryOverlapEnlargementComparator(boundingBoxToAdd,node.getEntries()));
            return bestEntry;
        }

        // if the child pointers in N do not point to leaves

        // [determine the minimum area cost],
        // choose the leaf in N whose rectangle needs least
        // area enlargement to include the new data
        // rectangle. Resolve ties by choosing the leaf
        // with the rectangle of smallest area
        bestEntry = Collections.min(node.getEntries(), new EntryComparator.EntryAreaEnlargementComparator(boundingBoxToAdd));
        return bestEntry;
    }

    // Algorithm OverflowTreatment
    private Entry overFlowTreatment(Entry parentEntry) {

        Node childNode;
        if(parentEntry == null)
            childNode = root;
        else
            childNode = parentEntry.getChildNode(); //TODO check if you can avoid getting child node

        // If the level is not the root level and this is the first
        // call of OverflowTreatment in the given level
        // during the insertion of one data rectangle, then reinsert
        if (childNode != root && !levelsInserted[childNode.getLevel()-1])
        {
            levelsInserted[childNode.getLevel()-1] = true; // Mark level as already reinserted
            reInsert(parentEntry);
            return null;
        }

        levelsInserted[childNode.getLevel()-1] = true; // Mark level as already reinserted

        // Else invoke Split
        // The two nodes occurring after the split
        ArrayList<Node> splitNodes = childNode.splitNode();
        if (splitNodes.size() != 2)
            throw new IllegalStateException("The resulting Nodes after a split cannot be more or less than two");

        // Adjusting the previous Node with the new entries and bounds
        childNode.setEntries(splitNodes.get(0).getEntries());

        // Updating-Adjusting the bounding box of the Entry that points to the Updated previous Node
        if (childNode != root && parentEntry != null)
            parentEntry.setBoundingBox(childNode.getOverallBoundingBox());

        // The new Node that occurred from the split
        Node splitNode = splitNodes.get(1);

        // If OverflowTreatment caused a split of the root, create a new root
        if (childNode == root)
        {
            Node oldRootNode = new Node(childNode.getLevel(),childNode.getEntries());

            ArrayList<Entry> newRootEntries = new ArrayList<>();
            newRootEntries.add(new Entry(oldRootNode));
            newRootEntries.add(new Entry(splitNode));

            root = new Node(++totalLevels,newRootEntries);
            return null;
         }

        // Propagate the overflow treatment upwards, to fit the entry on the caller's level Node
		return new Entry(splitNode);
    }

    // Algorithm reinsert
    private void reInsert(Entry parentEntry) {
        Node childNode = parentEntry.getChildNode(); //TODO check if you can avoid getting child node

        if(childNode.getEntries().size() != Node.MAX_ENTRIES + 1)
            throw new IllegalStateException("Cannot throw reinsert for node with total entries fewer than M+1");

        // RI1 For all M+l items of a node N, compute the distance
        // between the centers of their rectangles and the center
        // of the bounding rectangle of N

        // RI2: Sort the items in INCREASING order (since then we use close reinsert) of their distances
        // computed in RI1
        childNode.getEntries().sort(new EntryComparator.EntryDistanceFromCenterComparator(childNode.getOverallBoundingBox()));
        ArrayList<Entry> removedEntries = new ArrayList<>(childNode.getEntries().subList(childNode.getEntries().size()-REINSERT_P_ENTRIES,childNode.getEntries().size()));

        // RI3: Remove the last p items from N (since then we use close reinsert) and adjust the bounding rectangle of N
        for(int i = 0; i < REINSERT_P_ENTRIES; i++)
            childNode.getEntries().remove(childNode.getEntries().size()-1);

        // Updating bounding box of node and to the parent entry
        childNode.setOverallBoundingBox(new BoundingBox(Bounds.findMinimumBounds(childNode.getEntries())));
        parentEntry.setBoundingBox(childNode.getOverallBoundingBox());

        // RI4: In the sort, defined in RI2, starting with the
        // minimum distance (= close reinsert), invoke Insert
        // to reinsert the items
        if(removedEntries.size() != REINSERT_P_ENTRIES)
            throw new IllegalStateException("Entries queued for reinsert have different size than the ones that were removed");

        for (Entry entry : removedEntries)
            insert(null,entry,childNode.getLevel());
    }

//    void testSplitting() {
//        Node aNode = new Node(1);
//        int[][] rec1 = {{15, 15}, {15, 15}};
//        int[][] rec2 = {{1, 1}, {1, 2}};
//        int[][] rec3 = {{20500, 26000}, {1,10}};
//        int[][] rec4 = {{1002, 1006}, {1010, 1011}};
//        int[][] rec5 = {{1010, 1011}, {1010, 1011}};
//
//        ArrayList<int[][]> boundsOfRects = new ArrayList<>();
//        boundsOfRects.add(rec1);
//        boundsOfRects.add(rec2);
//        boundsOfRects.add(rec3);
//        boundsOfRects.add(rec4);
//        boundsOfRects.add(rec5);
//
//        int index = 0;
//        for (int[][] boundsOfRect : boundsOfRects)
//        {
//            ArrayList<Bounds> boundsRect = new ArrayList<>();
//            for (int d = 0; d < MetaData.DIMENSIONS; d++)
//                boundsRect.add(new Bounds(boundsOfRect[d][0],boundsOfRect[d][1]));
//
//            aNode.insertEntry(new LeafEntry(index++, boundsRect));
//        }
//
//
//        Node splitNode = split(aNode);
//        ArrayList<Node> splitNodes = new ArrayList<>();
//        splitNodes.add(aNode);
//        splitNodes.add(splitNode);
//
//        System.out.println("Testing split:");
//        for(Node node : splitNodes)
//        {
//            System.out.println("Node: ");
//
//            for (Entry entry : node.getEntries())
//            {
//                System.out.print(((LeafEntry)entry).getRecordId() + ":   ");
//                for (Bounds bounds : entry.getBoundingBox().getBounds())
//                {
//                    System.out.print(bounds.getLower() + ", " + bounds.getUpper() + "      ");
//                }
//                System.out.println();
//            }
//            System.out.println();
//        }
//
//        for (Bounds bounds : splitNodes.get(0).getOverallBoundingBox().getBounds())
//        {
//            System.out.print(bounds.getLower() + ", " + bounds.getUpper() + "      ");
//        }
//    }
}