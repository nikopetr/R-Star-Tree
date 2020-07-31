import java.io.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        MetaData.DIMENSIONS = 2;

        //MetaData.resetIndexFile();
        //MetaData.initializeDataFile(); // initialises/resets the data of datafile

        RStarTree rStarTree = new RStarTree();
        // Adding the data of datafile in the RStarTree
        for (int i = 0; i<MetaData.getTotalBlocksInDatafile(); i++)
        {
            ArrayList<Record> records = MetaData.readDataFileBlock(i);
            for (Record record : records)
                rStarTree.insertRecord(record);
        }

//        ArrayList<Double> recCoordinates = new ArrayList<>();
//        recCoordinates.add(-100.0);
//        recCoordinates.add(1.0);
//        rStarTree.insertRecord(new Record(1, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(-80.0);
//        recCoordinates.add(-1.0);
//        rStarTree.insertRecord(new Record(2, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(4.0);
//        recCoordinates.add(1.0);
//        rStarTree.insertRecord(new Record(3, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(5.0);
//        recCoordinates.add(0.0);
//        rStarTree.insertRecord(new Record(4, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(14.0);
//        recCoordinates.add(1.0);
//        rStarTree.insertRecord(new Record(5, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(2.0);
//        recCoordinates.add(1.0);
//        rStarTree.insertRecord(new Record(6, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(2.0);
//        recCoordinates.add(0.1);
//        rStarTree.insertRecord(new Record(7, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(-101.0);
//        recCoordinates.add(0.1);
//        rStarTree.insertRecord(new Record(8, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(-102.0);
//        recCoordinates.add(0.1);
//        rStarTree.insertRecord(new Record(9, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(-125.0);
//        recCoordinates.add(1.0);
//        rStarTree.insertRecord(new Record(10, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(9.0);
//        recCoordinates.add(0.9);
//        rStarTree.insertRecord(new Record(11, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(-1.0);
//        recCoordinates.add(0.0);
//        rStarTree.insertRecord(new Record(12, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(23.0);
//        recCoordinates.add(1.7);
//        rStarTree.insertRecord(new Record(13, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(12.0);
//        recCoordinates.add(10.0);
//        rStarTree.insertRecord(new Record(14, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(20.0);
//        recCoordinates.add(-2.0);
//        rStarTree.insertRecord(new Record(15, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(2.0);
//        recCoordinates.add(-0.1);
//        rStarTree.insertRecord(new Record(16, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(-1.0);
//        recCoordinates.add(-2.0);
//        rStarTree.insertRecord(new Record(17, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(1.0);
//        recCoordinates.add(1.0);
//        rStarTree.insertRecord(new Record(18, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(15.0);
//        recCoordinates.add(-1.0);
//        rStarTree.insertRecord(new Record(19, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(-136.0);
//        recCoordinates.add(1.0);
//        rStarTree.insertRecord(new Record(20, recCoordinates));

//        Node node = rStarTree.getRoot();
//        for (Entry rootNodeEntry: node.getEntries())
//            printOverallNode(rootNodeEntry);

        // Range query testing
        System.out.print("Range Query: ");
        ArrayList<Bounds> queryBounds = new ArrayList<>();
        queryBounds.add(new Bounds(-154, -102.0));
        queryBounds.add(new Bounds(0.1, 254.0));
        long startRangeQueryTime = System.nanoTime();
        ArrayList<Long> queryRecords = rStarTree.getDataInBoundingBox(new BoundingBox(queryBounds));
        long stopRangeQueryTime = System.nanoTime();
        for (Long id: queryRecords)
            System.out.print(id + ", ");
        System.out.println();
        System.out.println("Time taken for range query using R star: ");
        System.out.println(stopRangeQueryTime - startRangeQueryTime);


        //Sequential Scan Range Query
        System.out.print("Sequential Scan Range Query: ");
        SequentialScanBoundingBoxRangeQuery sequentialScanBoundingBoxRangeQuery = new SequentialScanBoundingBoxRangeQuery(new BoundingBox(queryBounds));
        long startSequentialRangeQuryTime = System.nanoTime();
        queryRecords = sequentialScanBoundingBoxRangeQuery.getQueryRecordIds(0);
        long stopSequentialRangeQueryTime = System.nanoTime();

        for (Long id: queryRecords)
            System.out.print(id + ", ");
        System.out.println();
        System.out.println("Time taken for Range query using sequential scan: ");
        System.out.println(stopSequentialRangeQueryTime - startSequentialRangeQuryTime);

        // Point radius query testing
        System.out.print("Point Radius Query: ");
        ArrayList<Double> point = new ArrayList<>();
        // Circle' center
        point.add(0.0);
        point.add(0.0);
        queryRecords = rStarTree.getDataInCircle(point,125.004);

        for (Long id: queryRecords)
            System.out.println(id + ", ");
        System.out.println();







        // Point radius query testing
        System.out.print("KNN Query: ");
        point = new ArrayList<>();
        // Point's center 20896245,32.7557378,34.6510560
        point.add(32.7557378);
        point.add(34.6510560);
        long startKNNTime = System.nanoTime();
        queryRecords = rStarTree.getNearestNeighbours(point,3);
        long stopKNNTime = System.nanoTime();
        for (Long id: queryRecords)
            System.out.print(id + ", ");
        System.out.println();
        System.out.println("Time taken for KNN using R star tree: ");
        System.out.println(stopKNNTime - startKNNTime);

        System.out.print("Sequential KNN Query: ");
        SequentialScanQuery sequentialNearestNeighboursQuery = new SequentialNearestNeighboursQuery(point, 3);
        long startSequentialKNNTime = System.nanoTime();
        queryRecords = sequentialNearestNeighboursQuery.getQueryRecordIds(0);
        long stopSequentialKNNTime = System.nanoTime();
        for (Long id: queryRecords)
            System.out.print(id + ", ");
        System.out.println();

        System.out.println("Time taken for KNN using sequential scan: ");
        System.out.println(stopSequentialKNNTime - startSequentialKNNTime);
    }

    static private void printOverallNode(Entry parentEntry) throws IOException, ClassNotFoundException {
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

            if(parentEntry.getChildNodeBlockId() != null)
            {
                // root-sub rectangles
                for (Entry entry : MetaData.readIndexFileBlock(parentEntry.getChildNodeBlockId()).getEntries())
                {
                    System.out.println("Current level: " + MetaData.readIndexFileBlock(parentEntry.getChildNodeBlockId()).getLevel());
                    for (Bounds bounds : entry.getBoundingBox().getBounds())
                        System.out.print(bounds.getLower() + ", " + bounds.getUpper() + "      ");

                    System.out.println();

                    if (entry.getChildNodeBlockId()!= null)
                    {
                        System.out.println("Going inside the node...");
                        printOverallNode(entry);
                    }

                    System.out.println();
                    System.out.println();
                }
            }

            System.out.println("Leaving the node...");
        }
    }
}