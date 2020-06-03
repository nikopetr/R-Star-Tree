import java.io.Serializable;
import java.util.ArrayList;


class Node  implements Serializable {
    static final int MAX_ENTRIES = 4;
    private static final int MINIMUM_ENTRIES = (int)(0.4 * MAX_ENTRIES); // Setting m to 40%

    private int level;
    private ArrayList<Entry> entries;

    Node(int level) {
        this.level = level;
        this.entries = new ArrayList<>();
    }

    Node(int level, ArrayList<Entry> entries) {
        this.level = level;
        this.entries = entries;
    }

    int getLevel() {
        return level;
    }

    void setEntries(ArrayList<Entry> entries) {
        this.entries = entries;
    }

    ArrayList<Entry> getEntries() {
        return entries;
    }

    void insertEntry(Entry entry)
    {
        entries.add(entry);
    }

    ArrayList<Node> splitNode() {
        ArrayList<Distribution> splitAxisDistributions = chooseSplitAxis();
        return chooseSplitIndex(splitAxisDistributions);
    }

// Returns the distributions of the best Axis
private ArrayList<Distribution> chooseSplitAxis() {
//    For each axis
//    Sort the entries by the lower then by the upper
//    value of their rectangles and determine all
//    distributions as described above Compute S. the
//    sum of all margin-values of the different
//    distributions

        // int bestSplitAxis;
        ArrayList<Distribution> splitAxisDistributions = new ArrayList<>(); // Vector of pointers to nodes (for the different distributions)
        double splitAxisMarginsSum = Double.MAX_VALUE;
        for (int d = 0; d < MetaData.DIMENSIONS; d++)
        {
            ArrayList<Entry> entriesSortedByUpper = new ArrayList<>();
            ArrayList<Entry> entriesSortedByLower = new ArrayList<>();

            for (Entry entry : entries) {
                entriesSortedByLower.add(entry);
                entriesSortedByUpper.add(entry);
            }

            entriesSortedByLower.sort(new EntryComparator.EntryBoundComparator(d,false));
            entriesSortedByUpper.sort(new EntryComparator.EntryBoundComparator(d,true));

            ArrayList<ArrayList<Entry>> sortedEntries = new ArrayList<>();
            sortedEntries.add(entriesSortedByLower);
            sortedEntries.add(entriesSortedByUpper);

            double sumOfMargins = 0;
            ArrayList<Distribution>  distributions = new ArrayList<>();
            // Determining distributions
            // Total number of different distributions = M-2*m+2 for each sorted vector
            for (ArrayList<Entry> sortedEntryList: sortedEntries)
            {
                for (int k = 1; k <= MAX_ENTRIES - 2*MINIMUM_ENTRIES +2; k++) //TODO CHECK FOR "="
                {
                    ArrayList<Entry> firstGroup = new ArrayList<>();
                    ArrayList<Entry> secondGroup = new ArrayList<>();
                    // The first group contains the first (m-l)+k entries, the second group contains the remaining entries
                    for (int j = 0; j < (MINIMUM_ENTRIES -1)+k; j++)
                        firstGroup.add(sortedEntryList.get(j));
                    for (int j = (MINIMUM_ENTRIES -1)+k; j < entries.size(); j++)
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
