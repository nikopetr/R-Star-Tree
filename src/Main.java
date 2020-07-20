import java.io.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        MetaData.DIMENSIONS = 2;
        MetaData.initializeDataFile();


        RStarTree rStarTree = new RStarTree();

//        ArrayList<Record> records;
//        for (int i = 0; i<MetaData.getTotalBlocksInDatafile(); i++)
//        {
//            records = MetaData.readDataFileBlock(i);
//            for (Record record : records)
//                rStarTree.insertRecord(record);
//        }

        ArrayList<Double> recCoordinates = new ArrayList<>();
        recCoordinates.add(-100.0);
        recCoordinates.add(1.0);
        rStarTree.insertRecord(new Record(1, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(-80.0);
        recCoordinates.add(-1.0);
        rStarTree.insertRecord(new Record(2, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(4.0);
        recCoordinates.add(1.0);
        rStarTree.insertRecord(new Record(3, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(5.0);
        recCoordinates.add(0.0);
        rStarTree.insertRecord(new Record(4, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(14.0);
        recCoordinates.add(1.0);
        rStarTree.insertRecord(new Record(5, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(2.0);
        recCoordinates.add(1.0);
        rStarTree.insertRecord(new Record(6, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(2.0);
        recCoordinates.add(0.1);
        rStarTree.insertRecord(new Record(7, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(-101.0);
        recCoordinates.add(0.1);
        rStarTree.insertRecord(new Record(8, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(-102.0);
        recCoordinates.add(0.1);
        rStarTree.insertRecord(new Record(9, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(-125.0);
        recCoordinates.add(1.0);
        rStarTree.insertRecord(new Record(10, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(9.0);
        recCoordinates.add(0.9);
        rStarTree.insertRecord(new Record(11, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(-1.0);
        recCoordinates.add(0.0);
        rStarTree.insertRecord(new Record(12, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(23.0);
        recCoordinates.add(1.7);
        rStarTree.insertRecord(new Record(13, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(12.0);
        recCoordinates.add(10.0);
        rStarTree.insertRecord(new Record(14, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(20.0);
        recCoordinates.add(-2.0);
        rStarTree.insertRecord(new Record(15, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(2.0);
        recCoordinates.add(-0.1);
        rStarTree.insertRecord(new Record(16, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(-1.0);
        recCoordinates.add(-2.0);
        rStarTree.insertRecord(new Record(17, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(1.0);
        recCoordinates.add(1.0);
        rStarTree.insertRecord(new Record(18, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(15.0);
        recCoordinates.add(-1.0);
        rStarTree.insertRecord(new Record(19, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(-136.0);
        recCoordinates.add(1.0);
        rStarTree.insertRecord(new Record(20, recCoordinates));

        Node node = rStarTree.getRoot();
        for (Entry rootNodeEntry: node.getEntries())
            printOverallNode(rootNodeEntry);

        // Range query testing
        System.out.print("Range Query: ");
        ArrayList<Bounds> queryBounds = new ArrayList<>();
        queryBounds.add(new Bounds(-154, -102.0));
        queryBounds.add(new Bounds(0.1, 254.0));
        ArrayList<Long> queryRecords = rStarTree.getDataInBoundingBox(new BoundingBox(queryBounds));

        for (Long id: queryRecords)
            System.out.print(id + ", ");
        System.out.println();

        // Point radius query testing
        System.out.print("Point Radius Query: ");
        ArrayList<Double> point = new ArrayList<>();
        // Circle' center
        point.add(0.0);
        point.add(0.0);
        queryRecords = rStarTree.getDataInCircle(point,125.004);

        for (Long id: queryRecords)
            System.out.print(id + ", ");
        System.out.println();

        // Point radius query testing
        System.out.print("KNN Query: ");
        point = new ArrayList<>();
        // Point's center 269064201,33.0127443,34.6633102
        point.add(33.0127443);
        point.add(34.6633102);
        queryRecords = rStarTree.getNearestNeighbours(point,2);

        for (Long id: queryRecords)
            System.out.print(id + ", ");
        System.out.println();

    }

    static private void printOverallNode(Entry parentEntry){
        // Prints overall node bb and entries
        // overall rectangle

        if (parentEntry.getBoundingBox()!= null)
        {
            System.out.print("Overall bounding box:  ");
            for (Bounds bounds : parentEntry.getBoundingBox().getBounds())
                System.out.print(bounds.getLower() + ", " + bounds.getUpper() + "      ");

            System.out.println();System.out.println();
            System.out.println("Entries: ");
            System.out.println();

            if(parentEntry.getChildNode() != null)
            {
                // root-sub rectangles
                for (Entry entry : parentEntry.getChildNode().getEntries())
                {
                    System.out.println("Current level: " + parentEntry.getChildNode().getLevel());
                    for (Bounds bounds : entry.getBoundingBox().getBounds())
                        System.out.print(bounds.getLower() + ", " + bounds.getUpper() + "      ");

                    System.out.println();

                    if (entry.getChildNode()!= null)
                    {
                        System.out.println("Going inside the node...");
                        printOverallNode(entry);
                    }

                    System.out.println();
                    System.out.println();
                }
                System.out.println("Leaving the node...");
            }
        }
    }
}
