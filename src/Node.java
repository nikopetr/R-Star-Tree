import java.io.Serializable;
import java.util.ArrayList;

// Class representing a Node of the RStarTree
class Node implements Serializable {
    private static final int MAX_ENTRIES = FilesHelper.calculateMaxEntriesInNode(); // The maximum entries that a Node can fit based on the file parameters
    private static final int MIN_ENTRIES = (int)(0.4 * MAX_ENTRIES); // Setting m to 40%
    private int level; // The level of the tree that this Node is located
    private long blockId; // The unique ID of the file block that this Node refers to
    private ArrayList<Entry> entries; // The ArrayList with the Entries of the Node

    // Root constructor with it's level as a parameter which makes a new empty ArrayList for the Node
    Node(int level) {
        this.level = level;
        this.entries = new ArrayList<>();
        this.blockId = RStarTree.getRootNodeBlockId();
    }

    // Node constructor with level and entries parameters
    Node(int level, ArrayList<Entry> entries) {
        this.level = level;
        this.entries = entries;
    }

    void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    void setEntries(ArrayList<Entry> entries) {
        this.entries = entries;
    }

    static int getMaxEntries() {
        return MAX_ENTRIES;
    }

    long getBlockId() {
        return blockId;
    }

    int getLevel() {
        return level;
    }

    ArrayList<Entry> getEntries() {
        return entries;
    }

    // Adds the given entry to the entries ArrayList of the Node
    void insertEntry(Entry entry)
    {
        entries.add(entry);
    }

    // Splits the entries of the Node and divides them to two new Nodes
    // Returns an ArrayList which
    ArrayList<Node> splitNode() {
        ArrayList<Distribution> splitAxisDistributions = chooseSplitAxis();
        return chooseSplitIndex(splitAxisDistributions);
    }

    // Returns the distributions of the best Axis
    private ArrayList<Distribution> chooseSplitAxis() {
        // For each axis sort the entries by the lower then by the upper
        // value of their rectangles and determine all distributions as described above Compute S which is the
        // sum of all margin-values of the different distributions

        ArrayList<Distribution> splitAxisDistributions = new ArrayList<>(); // for the different distributions
        double splitAxisMarginsSum = Double.MAX_VALUE;
        for (int d = 0; d < FilesHelper.getDataDimensions(); d++)
        {
            ArrayList<Entry> entriesSortedByUpper = new ArrayList<>();
            ArrayList<Entry> entriesSortedByLower = new ArrayList<>();

            for (Entry entry : entries)
            {
                entriesSortedByLower.add(entry);
                entriesSortedByUpper.add(entry);
            }

            entriesSortedByLower.sort(new EntryComparator.EntryBoundComparator(entriesSortedByLower,d,false));
            entriesSortedByUpper.sort(new EntryComparator.EntryBoundComparator(entriesSortedByUpper,d,true));

            ArrayList<ArrayList<Entry>> sortedEntries = new ArrayList<>();
            sortedEntries.add(entriesSortedByLower);
            sortedEntries.add(entriesSortedByUpper);

            double sumOfMargins = 0;
            ArrayList<Distribution>  distributions = new ArrayList<>();
            // Determining distributions
            // Total number of different distributions = M-2*m+2 for each sorted vector
            for (ArrayList<Entry> sortedEntryList: sortedEntries)
            {
                for (int k = 1; k <= MAX_ENTRIES - 2* MIN_ENTRIES +2; k++) //TODO CHECK FOR "="
                {
                    ArrayList<Entry> firstGroup = new ArrayList<>();
                    ArrayList<Entry> secondGroup = new ArrayList<>();
                    // The first group contains the first (m-l)+k entries, the second group contains the remaining entries
                    for (int j = 0; j < (MIN_ENTRIES -1)+k; j++)
                        firstGroup.add(sortedEntryList.get(j));
                    for (int j = (MIN_ENTRIES -1)+k; j < entries.size(); j++)
                        secondGroup.add(sortedEntryList.get(j));

                    BoundingBox bbFirstGroup = new BoundingBox(Bounds.findMinimumBounds(firstGroup));
                    BoundingBox bbSecondGroup = new BoundingBox(Bounds.findMinimumBounds(secondGroup));

                    Distribution distribution = new Distribution(new DistributionGroup(firstGroup,bbFirstGroup), new DistributionGroup(secondGroup,bbSecondGroup));
                    distributions.add(distribution);
                    sumOfMargins += bbFirstGroup.getMargin() + bbSecondGroup.getMargin();
                }

                // Choose the axis with the minimum sum as split axis
                if (splitAxisMarginsSum > sumOfMargins)
                {
                    // bestSplitAxis = d;
                    splitAxisMarginsSum = sumOfMargins;
                    splitAxisDistributions = distributions;
                }
            }
        }
        return splitAxisDistributions;
    }

    // Returns a vector of Nodes, containing the two nodes that occurred from the split
    private ArrayList<Node> chooseSplitIndex(ArrayList<Distribution> splitAxisDistributions) {

        if (splitAxisDistributions.size() == 0)
            throw new IllegalArgumentException("Wrong distributions group size. Given 0");

        double minOverlapValue = Double.MAX_VALUE;
        double minAreaValue = Double.MAX_VALUE;
        int bestDistributionIndex = 0;
        // Along the chosen split axis, choose the
        // distribution with the minimum overlap value
        for (int i = 0; i < splitAxisDistributions.size(); i++)
        {
            DistributionGroup distributionFirstGroup = splitAxisDistributions.get(i).getFirstGroup();
            DistributionGroup distributionSecondGroup = splitAxisDistributions.get(i).getSecondGroup();

            double overlap = BoundingBox.calculateOverlapValue(distributionFirstGroup.getBoundingBox(), distributionSecondGroup.getBoundingBox());
            if(minOverlapValue > overlap)
            {
                minOverlapValue = overlap;
                minAreaValue = distributionFirstGroup.getBoundingBox().getArea() + distributionSecondGroup.getBoundingBox().getArea();
                bestDistributionIndex = i;
            }
            // Resolve ties by choosing the distribution with minimum area-value
            else if (minOverlapValue == overlap)
            {
                double area = distributionFirstGroup.getBoundingBox().getArea() + distributionSecondGroup.getBoundingBox().getArea() ;
                if(minAreaValue > area)
                {
                    minAreaValue = area;
                    bestDistributionIndex = i;
                }
            }
        }
        ArrayList<Node> splitNodes = new ArrayList<>();
        DistributionGroup firstGroup = splitAxisDistributions.get(bestDistributionIndex).getFirstGroup();
        DistributionGroup secondGroup = splitAxisDistributions.get(bestDistributionIndex).getSecondGroup();
        splitNodes.add(new Node(level,firstGroup.getEntries()));
        splitNodes.add(new Node(level,secondGroup.getEntries()));
        return splitNodes;
    }
}
