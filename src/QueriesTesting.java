import java.io.*;
import java.util.ArrayList;

// Testing the queries for a variety of two dimensional input
public class QueriesTesting {
    public static void main(String[] args){

        RStarTree rStarTree = new RStarTree(false);
        FilesHelper.initializeDataFile(2, false);

        ArrayList<Double> centerPoint = new ArrayList<>(); // ArrayList with the coordinates of an approximate center point
        centerPoint.add(22.2121); // Coordinate of second dimension
        centerPoint.add(37.4788); // Coordinate of first dimension

        double rangeIncrement = 0.000071; // How much the interval and radius increases each time

        // ------------------------------------------------------------------------
        // Range Query Data
        ArrayList<Double> rStarRangeQueryTimes = new ArrayList<>();
        ArrayList<Double> seqScanRangeQueryTimes = new ArrayList<>();
        ArrayList<Double> areaOfRectangles = new ArrayList<>();
        ArrayList<Integer> rangeQueryRecords = new ArrayList<>();

        // ------------------------------------------------------------------------
        // Point Radius Query Data
        ArrayList<Double> pointRadiusRStarTimes = new ArrayList<>();
        ArrayList<Double> pointRadiusSeqScanTimes = new ArrayList<>();
        ArrayList<Double> radiusOfCircles = new ArrayList<>();
        ArrayList<Integer> radiusQueryRecords = new ArrayList<>();

        // ------------------------------------------------------------------------
        // KNN Query Data
        ArrayList<Double> knnRStarTimes = new ArrayList<>();
        ArrayList<Double> knnSeqScanTimes = new ArrayList<>();

        int i = 1;
        while(i <= 10000){
            // Taking values for every 1000 samples
            if(i%100 == 0){

                //Range Query
                ArrayList<Bounds> queryBounds = new ArrayList<>();
                queryBounds.add(new Bounds(centerPoint.get(0) - i*rangeIncrement, centerPoint.get(0) + i*rangeIncrement));
                queryBounds.add(new Bounds(centerPoint.get(1) - i*rangeIncrement, centerPoint.get(1) + i*rangeIncrement));

                // R star Range Query
                long startRangeQueryTime = System.nanoTime();
                rangeQueryRecords.add(rStarTree.getDataInBoundingBox(new BoundingBox(queryBounds)).size());
                long stopRangeQueryTime = System.nanoTime();
                rStarRangeQueryTimes.add((double) (stopRangeQueryTime - startRangeQueryTime) / 1000000);

                // Sequential Scan - Range Query
                SequentialScanBoundingBoxRangeQuery sequentialScanBoundingBoxRangeQuery = new SequentialScanBoundingBoxRangeQuery(new BoundingBox(queryBounds));
                long startSequentialRangeQueryTime = System.nanoTime();
                sequentialScanBoundingBoxRangeQuery.getQueryRecordIds();
                long stopSequentialRangeQueryTime = System.nanoTime();
                seqScanRangeQueryTimes.add((double) (stopSequentialRangeQueryTime - startSequentialRangeQueryTime) / 1000000);
                areaOfRectangles.add(new BoundingBox(queryBounds).getArea());


                // R Star - Point Radius (circle) Query
                long startPointRadiusQueryTime = System.nanoTime();
                radiusQueryRecords.add((rStarTree.getDataInCircle(centerPoint, i*rangeIncrement).size()));
                long stopPointRadiusQueryTime = System.nanoTime();
                pointRadiusRStarTimes.add((double) (stopPointRadiusQueryTime - startPointRadiusQueryTime) / 1000000);

                // Sequential Scan Point Radius Query
                SequentialScanPointRadiusQuery sequentialScanPointRadiusQuery = new SequentialScanPointRadiusQuery(centerPoint, i*rangeIncrement);
                long startSeqScanPointRadius = System.nanoTime();
                sequentialScanPointRadiusQuery.getQueryRecordIds();
                long stopSeqScanPointRadius = System.nanoTime();
                pointRadiusSeqScanTimes.add((double) (stopSeqScanPointRadius - startSeqScanPointRadius) / 1000000);
                radiusOfCircles.add(i*rangeIncrement);

                // Knn R Star Query
                long startKNNTime = System.nanoTime();
                rStarTree.getNearestNeighbours(centerPoint, i);
                long stopKNNTime = System.nanoTime();
                knnRStarTimes.add((double) (stopKNNTime - startKNNTime) / 1000000 );

                // Knn Sequential Scan Query
                SequentialScanQuery sequentialNearestNeighboursQuery = new SequentialNearestNeighboursQuery(centerPoint, i);
                long startSequentialKNNTime = System.nanoTime();
                sequentialNearestNeighboursQuery.getQueryRecordIds();
                long stopSequentialKNNTime = System.nanoTime();
                knnSeqScanTimes.add((double) (stopSequentialKNNTime - startSequentialKNNTime) / 1000000);
            }
            i++;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("rangeQueryResults.csv"))) {
            String tagString = "Rectangle's Area" +
                    ',' +
                    "Returned Records" +
                    ',' +
                    "R* Time(ms)" +
                    ',' +
                    "Sequential Scan Time(ms)" +
                    '\n';
            writer.write(tagString);

            // Range Query File creation
            int j = 0;
            while(j < rStarRangeQueryTimes.size()){
                writer.write(String.format("%.4f", areaOfRectangles.get(j))+ "," + rangeQueryRecords.get(j) +"," +rStarRangeQueryTimes.get(j)+ "," + seqScanRangeQueryTimes.get(j) + "\n");
                j++;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("pointRadiusQueryResults.csv"))) {
            String tagString = "Circles's Radius" +
                    ',' +
                    "Returned Records" +
                    ',' +
                    "R* Time(ms)" +
                    ',' +
                    "Sequential Scan Time(ms)" +
                    '\n';
            writer.write(tagString);

            // Range Query File creation
            int j = 0;
            while(j < rStarRangeQueryTimes.size()){
                writer.write(String.format("%.4f", radiusOfCircles.get(j))+ "," + radiusQueryRecords.get(j) +"," +pointRadiusRStarTimes.get(j)+ "," + pointRadiusSeqScanTimes.get(j) + "\n");
                j++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("knnQueryResults.csv"))) {
            String tagString = "K" +
                    ',' +
                    "R* Time(ms)" +
                    ',' +
                    "Sequential Scan Time(ms)" +
                    '\n';
            writer.write(tagString);

            // Range Query File creation
            int j = 0;
            while(j < rStarRangeQueryTimes.size()){
                writer.write((j + 1)*100 + "," + knnRStarTimes.get(j)+ "," + knnSeqScanTimes.get(j) + "\n");
                j++;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
