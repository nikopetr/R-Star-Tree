import java.util.ArrayList;
import java.util.Collections;

class RStarTree {
    private static final int LEAF_LEVEL = 1; // Constant leaf level 1, since we are increasing the level from the root, the root (top level) will always have the highest level
    private static final int CHOOSE_SUBTREE_P_ENTRIES = 32;
    private static final int REINSERT_P_ENTRIES = (int) (0.30 * Node.MAX_ENTRIES); // Setting p to 30% of max entries

    Node getRoot() { //TODO DELETE THIS AFTER TESTING
        return root;
    }

    private Node root;
    private int totalLevels;
    private boolean[] levelsInserted;

    RStarTree(int dimensions) {
        this.root = new Node(1); // We are increasing the size from the root, the root (top level) will always have the highest level
        this.totalLevels = 1;
        MetaData.DIMENSIONS = dimensions;
    }

    void insertRecord(Record record) {
        ArrayList<Bounds> boundsForEachDimension = new ArrayList<>();
        // Since we have to do with points as records we set low and upper to be same
        for (int d = 0; d < MetaData.DIMENSIONS; d++)
            boundsForEachDimension.add(new Bounds(record.getCoordinate(d),record.getCoordinate(d)));

        levelsInserted = new boolean[totalLevels];
        insert(root, new LeafEntry(record.getId(), boundsForEachDimension), LEAF_LEVEL);
    }

    // Inserts nodes recursively. As an optimization, the algorithm steps are
    // way out of order. :) If this returns something, then that item should
    // be added to the caller's level of the tree
    private Node insert(Node node, Entry entry, int levelToAdd) {

        // CS2: If we're at a leaf, then use that level
        if (node.getLevel() == levelToAdd)
        {
            // I2: If N has less than M items, accommodate E in N
            node.insertEntry(entry);
           // node.adjustBoundingBoxOnEntries(); //TODO FIND BETTER WAY
        }
        else {
            // I1: Invoke ChooseSubtree. with the level as a parameter,
            // to find an appropriate node N, m which to place the
            // new leaf E

            // of course, this already does all of that recursively. we just need to
            // determine whether we need to split the overflow or not
            Node childNode = insert(chooseSubTree(node, entry.getBoundingBox(), levelToAdd), entry, levelToAdd);

            // If childNode returned null means no OverflowTreatment was called on children , returning null upwards
            if (childNode == null)
                return null;

            // This gets joined to the list of items at this level
            node.insertEntry(new Entry(childNode));
           // node.adjustBoundingBoxOnEntries(); //TODO FIND BETTER WAY
        }

        // If N has M+1 items. invoke OverflowTreatment with the
        // level of N as a parameter [for reinsertion or split]
        if (node.getEntries().size() > Node.MAX_ENTRIES)
        {
            // I3: If OverflowTreatment was called and a split was
            // performed, propagate OverflowTreatment upwards
            // if necessary

            // This is implicit, the rest of the algorithm takes place in there
            return overFlowTreatment(node);
        }

        return null;
    }
//    }

    // choose subtree: only pass this items that do not have leaves
    // I took out the loop portion of this algorithm, so it only
    // picks a subtree at that particular level
    private Node chooseSubTree(Node node, BoundingBox boundingBoxToAdd, int levelToAdd) {

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

                return bestEntry.getChildNode();
            }

            // Choose the entry in N whose rectangle needs least
            // overlap enlargement to include the new data
            // rectangle Resolve ties by choosing the entry
            // whose rectangle needs least area enlargement, then
            // the entry with the rectangle of smallest area
            bestEntry = Collections.min(node.getEntries(), new EntryComparator.EntryOverlapEnlargementComparator(boundingBoxToAdd,node.getEntries()));
            return bestEntry.getChildNode();

        }

        // if the child pointers in N do not point to leaves

        // [determine the minimum area cost],
        // choose the leaf in N whose rectangle needs least
        // area enlargement to include the new data
        // rectangle. Resolve ties by choosing the leaf
        // with the rectangle of smallest area
        bestEntry = Collections.min(node.getEntries(), new EntryComparator.EntryAreaEnlargementComparator(boundingBoxToAdd));
        return bestEntry.getChildNode();
    }

    // Algorithm OverflowTreatment
    private Node overFlowTreatment(Node node) {

        // If the level is not the root level and this is the first
        // call of OverflowTreatment in the given level
        // during the insertion of one data rectangle, then reinsert
        if (node.getLevel() != totalLevels && !levelsInserted[node.getLevel()-1])
        {
            reInsert(node);
            return null;
        }

        // Else invoke Split
        Node splitNode = split(node);
        levelsInserted[node.getLevel()-1] = true;

        // If OverflowTreatment caused a split of the root, create a new root
        if (node == root)
        {
            Node oldRootNode = new Node(node.getLevel(),node.getEntries()); //TODO might need to change how we copy stuff

            ArrayList<Entry> newRootEntries = new ArrayList<>();
            newRootEntries.add(new Entry(oldRootNode));
            newRootEntries.add(new Entry(splitNode));

            root = new Node(++totalLevels,newRootEntries,new BoundingBox(Bounds.findMinimumBounds(newRootEntries)));
            return null;
         }
         //TODO maybe changes needed here as well?

        // Propagate upwards
		return splitNode;
    }

    // Algorithm reinsert
    private void reInsert(Node node) {
        levelsInserted[node.getLevel()-1] = true; // Mark level as already reinserted

        if(node.getEntries().size() != Node.MAX_ENTRIES + 1)
            throw new IllegalStateException("Cannot throw reinsert for node with total entries fewer than M+1");

        // RI1 For all M+l items of a node N, compute the distance
        // between the centers of their rectangles and the center
        // of the bounding rectangle of N

        // RI2: Sort the items in INCREASING order (since then we use close reinsert) of their distances
        // computed in RI1
        node.getEntries().sort(new EntryComparator.EntryDistanceFromCenterComparator(node.getOverallBoundingBox()));
        ArrayList<Entry> removedEntries = new ArrayList<>(node.getEntries().subList(node.getEntries().size()-REINSERT_P_ENTRIES,node.getEntries().size()));

        // RI3: Remove the last p items from N (since then we use close reinsert) and adjust the bounding rectangle of N
        for(int i = 0; i < REINSERT_P_ENTRIES; i++)
            node.getEntries().remove(node.getEntries().size()-1);

        node.setOverallBoundingBox(new BoundingBox(Bounds.findMinimumBounds(node.getEntries()))); //TODO S.O.S check if works /otherwise FIND A WAY TO ADJUST PARENT ENTRY AS WELL

        // RI4: In the sort, defined in RI2, starting with the
        // minimum distance (= close reinsert), invoke Insert
        // to reinsert the items
        if(removedEntries.size() != REINSERT_P_ENTRIES)
            throw new IllegalStateException("Entries queued for reinsert have different size than the ones that were removed");

        for (Entry entry : removedEntries)
            insert(root,entry,node.getLevel());
    }

    private Node split(Node node)
    {
        ArrayList<Node> splitNodes = node.splitNode();
        //node.setLevel(splitNodes.get(0).getLevel()); //TODO MIGHT BE USELESS, REMOVE THIS
        //node.setOverallBoundingBox(splitNodes.get(0).getOverallBoundingBox());
        node.setEntries(splitNodes.get(0).getEntries());
        node.adjustBoundingBoxOnEntries();
        return splitNodes.get(1);
    }

    void testSplitting() {
        Node aNode = new Node(1);
        int[][] rec1 = {{15, 15}, {15, 15}};
        int[][] rec2 = {{1, 1}, {1, 2}};
        int[][] rec3 = {{20500, 26000}, {1,10}};
        int[][] rec4 = {{1002, 1006}, {1010, 1011}};
        int[][] rec5 = {{1010, 1011}, {1010, 1011}};

        ArrayList<int[][]> boundsOfRects = new ArrayList<>();
        boundsOfRects.add(rec1);
        boundsOfRects.add(rec2);
        boundsOfRects.add(rec3);
        boundsOfRects.add(rec4);
        boundsOfRects.add(rec5);

        int index = 0;
        for (int[][] boundsOfRect : boundsOfRects)
        {
            ArrayList<Bounds> boundsRect = new ArrayList<>();
            for (int d = 0; d < MetaData.DIMENSIONS; d++)
                boundsRect.add(new Bounds(boundsOfRect[d][0],boundsOfRect[d][1]));

            aNode.insertEntry(new LeafEntry(index++, boundsRect));
        }


        Node splitNode = split(aNode);
        ArrayList<Node> splitNodes = new ArrayList<>();
        splitNodes.add(aNode);
        splitNodes.add(splitNode);

        System.out.println("Testing split:");
        for(Node node : splitNodes)
        {
            System.out.println("Node: ");

            for (Entry entry : node.getEntries())
            {
                System.out.print(((LeafEntry)entry).getRecordId() + ":   ");
                for (Bounds bounds : entry.getBoundingBox().getBounds())
                {
                    System.out.print(bounds.getLower() + ", " + bounds.getUpper() + "      ");
                }
                System.out.println();
            }
            System.out.println();
        }

        for (Bounds bounds : splitNodes.get(0).getOverallBoundingBox().getBounds())
        {
            System.out.print(bounds.getLower() + ", " + bounds.getUpper() + "      ");
        }
    }
}