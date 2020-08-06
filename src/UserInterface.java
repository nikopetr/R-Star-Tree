import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

class UserInterface {
    // Starts the application by initialising the files based on the user's options
    static boolean startApplication(){
        boolean filesExist = Files.exists(Paths.get(FilesHelper.PATH_TO_DATAFILE));
        boolean resetFiles = false;

        Scanner scan = new Scanner(System.in); // Scanner used to get input from user

        if (filesExist)
        {
            System.out.println("Existed data-file and index-file found.");
            System.out.print("Do you want to make new ones based on the data of the data.csv file? (y/n): ");
            String answer;
            while (true)
            {
                answer = scan.nextLine().trim().toLowerCase();
                System.out.println();
                // In case user wants to reset the files
                if (answer.equals("y")) {
                    resetFiles = true;
                    break;
                } else if (answer.equals("n")) {
                    break;
                } else {
                    System.out.println("Please answer with y/n: ");
                }
            }
        }

        boolean insertRecordsFromDataFile = false;
        int dataDimensions = 0;

        if (!filesExist || resetFiles)
        {
            insertRecordsFromDataFile = true;
            System.out.print("Give the dimensions of the spacial data (dimensions need to be the same as the data saved in data.csv): ");
            dataDimensions = scan.nextInt();
            System.out.println();
        }

        FilesHelper.initializeDataFile(dataDimensions,resetFiles);
        FilesHelper.initializeIndexFile(dataDimensions,resetFiles);

        return insertRecordsFromDataFile;
    }

    // Runs the application and executes queries that the user has selected
    static void runApplication(RStarTree rStarTree)
    {
        Scanner scan = new Scanner(System.in); // Scanner used to get input from user

        System.out.println("Do you want to print the bounds for each inner node of the RStarTree? (y/n): ");
        String answer; // Variable used to store the user's input
        while (true)
        {
            answer = scan.nextLine().trim().toLowerCase();
            System.out.println();
            // In case user wants to reset the files
            if (answer.equals("y")) {
                Node node = rStarTree.getRoot();
                for (Entry rootNodeEntry: node.getEntries())
                    printOverallNode(rootNodeEntry);
                break;
            } else if (answer.equals("n")) {
                break;
            } else {
                System.out.println("Please answer with y/n: ");
            }
        }

        String querySelection; // Variable used to store the user's input
        do {
            // Selecting type of query
            switchLabel:
            while (true) {
                System.out.println("Query Options: 1)Search for Records that overlap with a given bounding box, 2)Search for Records that overlap with a given circle, 3)K-NN or 0)To Exit");
                System.out.print("Select the type of query to execute: ");
                querySelection = scan.nextLine().trim().toLowerCase();
                System.out.println();
                // In case user wants to reset the files
                switch (querySelection) {
                    case "1":
                        // Range query withing a bounding box selected
                        System.out.println("Range Query within a bouncing box selected");
                        System.out.println("Give the lower and upper bounds of the bouncing box for each dimension (input example for each dimension: 33.4 38.13)");
                        ArrayList<Bounds> queryBounds = new ArrayList<>();
                        for (int i = 0; i < FilesHelper.getDataDimensions(); i++) {
                            while (true) {
                                int dim = i + 1;
                                System.out.print("Give the bounds for dimension " + dim + ": ");
                                double lowerBound = scan.nextDouble();
                                double upperBound = scan.nextDouble();
                                System.out.println();
                                if (lowerBound <= upperBound) {
                                    queryBounds.add(new Bounds(lowerBound, upperBound));
                                    break;
                                } else
                                    System.out.println("The lower value of the bounds cannot be bigger than the upper");
                            }

                        }
//                  queryBounds.add(new Bounds(32.7557378, 32.7589925));
//                  queryBounds.add(new Bounds(34, 35));

                        // R Star - Range query
                        System.out.print("R Star - Range Query: ");
                        long startRangeQueryTime = System.nanoTime();
                        ArrayList<Long> queryRecords = rStarTree.getDataInBoundingBox(new BoundingBox(queryBounds));
                        long stopRangeQueryTime = System.nanoTime();
                        for (Long id : queryRecords)
                            System.out.print(id + ", ");
                        System.out.println();
                        System.out.println("Time taken: " + (double) (stopRangeQueryTime - startRangeQueryTime) / 1000000 + " ms");

                        // Sequential Scan - Range Query
                        System.out.print("Sequential Scan - Range Query: ");
                        SequentialScanBoundingBoxRangeQuery sequentialScanBoundingBoxRangeQuery = new SequentialScanBoundingBoxRangeQuery(new BoundingBox(queryBounds));
                        long startSequentialRangeQueryTime = System.nanoTime();
                        queryRecords = sequentialScanBoundingBoxRangeQuery.getQueryRecordIds();
                        long stopSequentialRangeQueryTime = System.nanoTime();

                        for (Long id : queryRecords)
                            System.out.print(id + ", ");
                        System.out.println();
                        System.out.println("Time taken: " + (double) (stopSequentialRangeQueryTime - startSequentialRangeQueryTime) / 1000000 + " ms");
                        System.out.println();

                        break switchLabel;
                    case "2":
                        // Range query withing a given circle
                        System.out.println("Range Query within a given circle selected");
                        System.out.println("Give the coordinate of each axis that the circle's center is (input example for each dimension : 33.4)");
                        ArrayList<Double> point = new ArrayList<>(); // The circle's center
                        for (int i = 0; i < FilesHelper.getDataDimensions(); i++) {
                            int dim = i + 1;
                            System.out.print("Give the coordinate of circle's center in dimension " + dim + ": ");
                            double coordinate = scan.nextDouble();
                            System.out.println();
                            point.add(coordinate);
                        }

                        // Circle' center
//                    point.add(0.0);
//                    point.add(0.0);

                        // R Star - Point Radius (circle) Query
                        System.out.print("R Star - Point Radius Query: ");
                        long startPointRadiusQueryTime = System.nanoTime();
                        queryRecords = rStarTree.getDataInCircle(point, 47.69);
                        long stopPointRadiusQueryTime = System.nanoTime();

                        for (Long id : queryRecords)
                            System.out.print(id + ", ");
                        System.out.println();
                        System.out.println("Time taken: " + (double) (stopPointRadiusQueryTime - startPointRadiusQueryTime) / 1000000 + " ms");
                        System.out.println();

                        // Sequential Scan Point Radius Query
                        System.out.print("Sequential Scan Point Radious Query: ");
                        SequentialScanPointRadiusQuery sequentialScanPointRadiusQuery = new SequentialScanPointRadiusQuery(point, 47.69);
                        long startSeqScanPointRadius = System.nanoTime();
                        queryRecords = sequentialScanPointRadiusQuery.getQueryRecordIds();
                        long stopSeqScanPointRadius = System.nanoTime();
                        for (Long id: queryRecords)
                            System.out.print(id + ", ");
                        System.out.println();
                        System.out.println("Time taken: " + (double) (stopSeqScanPointRadius - startSeqScanPointRadius) / 1000000 + " ms");
                        System.out.println("");

                        break switchLabel;
                    case "3":
                        // KNN Query
                        System.out.println("K-NN query selected");
                        System.out.println("Give the coordinate of each axis that the reference point's center is (input example for each dimension : 33.4)");
                        point = new ArrayList<>(); // The point's center
                        for (int i = 0; i < FilesHelper.getDataDimensions(); i++) {
                            int dim = i + 1;
                            System.out.print("Give the coordinate of reference point's center in dimension " + dim + ": ");
                            double coordinate = scan.nextDouble();
                            System.out.println();
                            point.add(coordinate);
                        }
                        int k; // the number of nearest neighbours to get
                        while (true) {
                            System.out.print("Give the value of k (the number of nearest neighbours to get): ");
                            k = scan.nextInt();
                            System.out.println();
                            if (k > 0)
                                break;
                            else
                                System.out.println("The value of k must be a positive integer");
                        }
                        // Point's center
//                    point.add(32.7557378);
//                    point.add(34.6510560);

                        // R Star - KNN Query
                        System.out.print("R Star - KNN Query: ");
                        long startKNNTime = System.nanoTime();
                        queryRecords = rStarTree.getNearestNeighbours(point, k);
                        long stopKNNTime = System.nanoTime();
                        for (Long id : queryRecords)
                            System.out.print(id + ", ");
                        System.out.println();
                        System.out.println("Time taken for KNN using R star tree: " + (double) (stopKNNTime - startKNNTime) / 1000000 + " ms");
                        System.out.print("Sequential KNN Query: ");
                        SequentialScanQuery sequentialNearestNeighboursQuery = new SequentialNearestNeighboursQuery(point, k);
                        long startSequentialKNNTime = System.nanoTime();
                        queryRecords = sequentialNearestNeighboursQuery.getQueryRecordIds();
                        long stopSequentialKNNTime = System.nanoTime();
                        for (Long id : queryRecords)
                            System.out.print(id + ", ");
                        System.out.println();
                        System.out.println("Time taken for KNN using sequential scan: " + (double) (stopSequentialKNNTime - startSequentialKNNTime) / 1000000 + " ms");
                        System.out.println();

                        break switchLabel;
                    case "0":
                        System.out.println("Exiting application ...");
                        break switchLabel;
                    default:
                        System.out.println("Please answer with 1,2,3 or 0 ");
                        break;
                }
            }
        } while (!querySelection.equals("0"));
    }

    // TODO maybe remove or
    static private void printOverallNode(Entry parentEntry) {
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
                for (Entry entry : FilesHelper.readIndexFileBlock(parentEntry.getChildNodeBlockId()).getEntries())
                {
                    System.out.println("Current level: " + FilesHelper.readIndexFileBlock(parentEntry.getChildNodeBlockId()).getLevel());
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
