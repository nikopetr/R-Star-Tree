import java.io.*;
import java.util.ArrayList;

public class QueriesTesting {
    public static void main(String[] args){


        RStarTree rStarTree = new RStarTree(false);
        FilesHelper.initializeDataFile(2, false);
        ArrayList<Double> rStarRangeQueryTime, seqScanRangeQueryTime, pointRadiusRStarTime, pointRadiusSeqScanTime,
                knnRStarTimes, knnSeqScanTimes, margin, point;
        ArrayList<Long> queryRecords;
        ArrayList<Bounds> queryBounds;

        // ------------------------------------------------------------------------
        // Range Query Data
        rStarRangeQueryTime = new ArrayList<>();
        seqScanRangeQueryTime = new ArrayList<>();
        queryBounds = new ArrayList<>();
        margin = new ArrayList<>();
        double upper1 = 2.0, upper2 = 4.0;

        // ------------------------------------------------------------------------
        // Point Radius Query Data
        pointRadiusRStarTime = new ArrayList<>();
        pointRadiusSeqScanTime = new ArrayList<>();
        point = new ArrayList<>();
        point.add(32.7557378);
        point.add(34.6510560);
        double radius = 10.0;


        // ------------------------------------------------------------------------
        // KNN Query Data
        point = new ArrayList<>();
        point.add(32.7557378);
        point.add(34.6510560);
        knnRStarTimes = new ArrayList<>();
        knnSeqScanTimes = new ArrayList<>();


        int timesTested = 0;

        while(timesTested < 20){

            // R star Range Query Test
            queryBounds.add(new Bounds(1.0, upper1));
            queryBounds.add(new Bounds(1.0, upper2));
            long startRangeQueryTime = System.nanoTime();
             queryRecords = rStarTree.getDataInBoundingBox(new BoundingBox(queryBounds));
            long stopRangeQueryTime = System.nanoTime();
            rStarRangeQueryTime.add((double) (stopRangeQueryTime - startRangeQueryTime) / 1000000);


            // Sequential Scan - Range Query Test

            SequentialScanBoundingBoxRangeQuery sequentialScanBoundingBoxRangeQuery = new SequentialScanBoundingBoxRangeQuery(new BoundingBox(queryBounds));
            long startSequentialRangeQueryTime = System.nanoTime();
            queryRecords = sequentialScanBoundingBoxRangeQuery.getQueryRecordIds();
            long stopSequentialRangeQueryTime = System.nanoTime();
            seqScanRangeQueryTime.add((double) (stopSequentialRangeQueryTime - startSequentialRangeQueryTime) / 1000000);
            margin.add(new BoundingBox(queryBounds).getArea());
            upper1 = upper1 + 5.0;
            upper2 = upper2 + 5.0;
            queryBounds = new ArrayList<>();




            // R Star - Point Radius (circle) Query
            long startPointRadiusQueryTime = System.nanoTime();
            queryRecords = rStarTree.getDataInCircle(point, radius);
            long stopPointRadiusQueryTime = System.nanoTime();
            pointRadiusRStarTime.add((double) (stopPointRadiusQueryTime - startPointRadiusQueryTime) / 1000000);

            // Sequential Scan Point Radius Query
            SequentialScanPointRadiusQuery sequentialScanPointRadiusQuery = new SequentialScanPointRadiusQuery(point, radius);
            long startSeqScanPointRadius = System.nanoTime();
            queryRecords = sequentialScanPointRadiusQuery.getQueryRecordIds();
            long stopSeqScanPointRadius = System.nanoTime();
            pointRadiusSeqScanTime.add((double) (stopSeqScanPointRadius - startSeqScanPointRadius) / 1000000);
            radius = radius + 5.0;



            // Knn R Star Query
            long startKNNTime = System.nanoTime();
            queryRecords = rStarTree.getNearestNeighbours(point, timesTested + 1);
            long stopKNNTime = System.nanoTime();
            queryRecords = new ArrayList<>();

            // Knn Sequential Scan Query
            SequentialScanQuery sequentialNearestNeighboursQuery = new SequentialNearestNeighboursQuery(point, timesTested + 1);
            long startSequentialKNNTime = System.nanoTime();
            queryRecords = sequentialNearestNeighboursQuery.getQueryRecordIds();
            long stopSequentialKNNTime = System.nanoTime();
            queryRecords = new ArrayList<>();

            knnRStarTimes.add((double) (stopKNNTime - startKNNTime) / 1000000 );
            knnSeqScanTimes.add((double) (stopSequentialKNNTime - startSequentialKNNTime) / 1000000);


            timesTested++;
        }





        try(

                BufferedWriter rangeQueryFile = new BufferedWriter(new FileWriter("rangeQueryTest.txt"));
                BufferedWriter pointRadiusQueryFile = new BufferedWriter(new FileWriter("pointRadiusTest.txt"));
                BufferedWriter knnQueryFile = new BufferedWriter(new FileWriter("knnQueryTest.txt"));
        ) {

            // Range Query File creation
            int j = 0;

            rangeQueryFile.write("area       R Star - Range Query" + " ------ " + "Sequential Scan Range Query\n");
            while(j < rStarRangeQueryTime.size()){

                rangeQueryFile.write(margin.get(j) + "  :                " + rStarRangeQueryTime.get(j)+ "                      " + seqScanRangeQueryTime.get(j) + "\n");
                j++;

            }


            // Point Radius Query File creation
            j = 0;
            double r = 10.0;
            pointRadiusQueryFile.write("radius       R Star - Point Radius Query" + " ------ " + "Sequential Scan Point Radius Query\n");
            while(j < pointRadiusRStarTime.size()){

                pointRadiusQueryFile.write(r + "  :                " + pointRadiusRStarTime.get(j)+ "                      " + pointRadiusSeqScanTime.get(j) + "\n");
                j++;
                r = r + 5.0;

            }


            // Knn Query File creation
            j = 0;
            knnQueryFile.write("        R star Knn query" + " ------ " + "Sequential Scan knn query\n");
            while(j < knnRStarTimes.size()){

                knnQueryFile.write(" k =" + (j + 1) + ":        " + knnRStarTimes.get(j)+ "                      " + knnSeqScanTimes.get(j) + "\n");
                j++;

            }



        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
