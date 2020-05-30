import java.io.*;
import java.util.ArrayList;


public class Main {

    private static final String PATH_TO_CSV = "test.csv";
    private static final String PATH_TO_DATAFILE = "datafile.dat";
    private static final int BLOCK_SIZE = 32 * 1024; // Each Block is 32KB

    public static void main(String[] args) throws IOException, ClassNotFoundException {


        RStarTree rStarTree = new RStarTree(2);

        ArrayList<Double> recCoordinates = new ArrayList<>();
        recCoordinates.add(-100.0);
        recCoordinates.add(1.0);
        rStarTree.insertRecord(new Record(1, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(-80.0);
        recCoordinates.add(-1.0);
        rStarTree.insertRecord(new Record(1, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(4.0);
        recCoordinates.add(1.0);
        rStarTree.insertRecord(new Record(1, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(5.0);
        recCoordinates.add(0.0);
        rStarTree.insertRecord(new Record(1, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(14.0);
        recCoordinates.add(1.0);
        rStarTree.insertRecord(new Record(1, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(2.0);
        recCoordinates.add(1.0);
        rStarTree.insertRecord(new Record(1, recCoordinates));

        recCoordinates = new ArrayList<>();
        recCoordinates.add(2.0);
        recCoordinates.add(0.1);
        rStarTree.insertRecord(new Record(1, recCoordinates));

//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(-1.0);
//        recCoordinates.add(0.0);
//        rStarTree.insertRecord(new Record(1, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(1.0);
//        recCoordinates.add(1.0);
//        rStarTree.insertRecord(new Record(1, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(2.0);
//        recCoordinates.add(1.0);
//        rStarTree.insertRecord(new Record(1, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(2.0);
//        recCoordinates.add(2.0);
//        rStarTree.insertRecord(new Record(1, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(2.0);
//        recCoordinates.add(0.1);
//        rStarTree.insertRecord(new Record(1, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(-1.0);
//        recCoordinates.add(2.0);
//        rStarTree.insertRecord(new Record(1, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(1.0);
//        recCoordinates.add(1.0);
//        rStarTree.insertRecord(new Record(1, recCoordinates));
//
//        recCoordinates = new ArrayList<>();
//        recCoordinates.add(2.0);
//        recCoordinates.add(1.0);
//        rStarTree.insertRecord(new Record(1, recCoordinates));

        //rStarTree.testSplitting();

        Node node = rStarTree.getRoot();
        printOverallNode(node);

    }

    static private void printOverallNode(Node node){
        // Prints overall node bb and entries
        // overall rectangle
        if (node.getOverallBoundingBox() != null)
        {
            System.out.print("Overall bounding box:  ");
            for (Bounds bounds : node.getOverallBoundingBox().getBounds())
                System.out.print(bounds.getLower() + ", " + bounds.getUpper() + "      ");

            System.out.println();System.out.println();
            System.out.println("Entries: ");
            System.out.println();

            // root-sub rectangles
            for (Entry entry : node.getEntries())
            {
                System.out.println("Current level: " + node.getLevel());
                for (Bounds bounds : entry.getBoundingBox().getBounds())
                    System.out.print(bounds.getLower() + ", " + bounds.getUpper() + "      ");

                System.out.println();

                if (entry.getChildNode()!=null)
                {
                    System.out.println("Going inside the node...");
                    printOverallNode(entry.getChildNode());
                }

                System.out.println();
                System.out.println();
            }
            System.out.println("Leaving the node...");
        }
    }



//        // In order to find the size of record in bytes
//        byte[] recordInBytes = serialize(new Record(1231231,123.2,123.1));
//        int sizeOfRecord = recordInBytes.length;
//        System.out.println(sizeOfRecord);
//
//        Record rec = new Record(1231231,123.2,123.1);
//        ArrayList<Integer> asd= new ArrayList<>();
//        byte[] recordInBytes2= serialize(rec);
//        sizeOfRecord = recordInBytes2.length;
//        System.out.println(sizeOfRecord);
//
//        // In order how many records fit a block
//        int maxRecordsInBlock = BLOCK_SIZE/sizeOfRecord;
//
//        byte[][] records = new byte[maxRecordsInBlock][sizeOfRecord];
//        for(int i = 0; i < maxRecordsInBlock; i++){
//            records[i] = serialize(new Record(i,123.2,123.1));
//        }


//        for(int i = 0; i < maxRecordsInBlock; i++){
//            Record record1 = (Record)deserialize(records[i]);
//            System.out.println(record1);
//        }


//        // Copying from records to block
//        byte[] block = new byte[BLOCK_SIZE];
//        for(int i = 0; i < maxRecordsInBlock; i++)
//            System.arraycopy(records[i], 0, block, i * sizeOfRecord, sizeOfRecord);
//
//        System.out.print(block.length);
//
//        // Copying from block to records
//        records = new byte[maxRecordsInBlock][sizeOfRecord];
//        for(int i = 0; i < maxRecordsInBlock; i++)
//            System.arraycopy(block, i * sizeOfRecord, records[i], 0, sizeOfRecord);
//
//        // Printing
//        for(int i = 0; i < maxRecordsInBlock; i++){
//            Record record1 = (Record)deserialize(records[i]);
//            System.out.println(record1);
//        }


























//        BufferedReader csvReader = (new BufferedReader(new FileReader(PATH_TO_CSV))); // BufferedReader used to read the data from the csv file
//        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(PATH_TO_DATAFILE)); // BufferedOutputStream used to save the data in blocks to the datafile
//        byte[] bFile = new byte[BLOCK_SIZE]; // Buffer of bytes used to temporally save each block
//        int totalBytesRead = 0;
//
//        int count = 0;
//        int temp=0;
//        ArrayList<Integer> arrayList = new ArrayList<>();
//
//        String row; // String used to read each line of the csv file
//        while ((row = csvReader.readLine()) != null)
//        {
//            count++;
//            System.arraycopy(row.getBytes(), 0, bFile, totalBytesRead, row.getBytes().length); // Copying the data from the row read to the buffer
//            totalBytesRead += row.getBytes().length;
//            if (totalBytesRead + row.getBytes().length >= BLOCK_SIZE)
//            {
//                bufferedOutputStream.write(bFile);
//                bFile = new byte[BLOCK_SIZE]; //Block is 32KB
//                totalBytesRead = 0;
//
//                arrayList.add(count-temp);
//                temp=count;
//            }
//        }
//        arrayList.add(count-temp);
//        // Writing the rest of the records
//        bufferedOutputStream.write(bFile);
//
//        bufferedOutputStream.close();
//        csvReader.close();
//
//        System.out.println("Total records : " + count);
//
//        for(int i=0; i < arrayList.size(); i++){
//            System.out.println("Block " + (i+1) + " has " + arrayList.get(i) + " records");
//        }
//
//        // For reading the blocks
//        InputStream inStream = null;
//        BufferedInputStream bis;
//        bis = null;
//        try {
//            // open input stream test.txt for reading purpose.
//            inStream = new FileInputStream(PATH_TO_DATAFILE);
//
//            // input stream is converted to buffered input stream
//            bis = new BufferedInputStream(inStream);
//
//            // read number of bytes available
//            int numByte = bis.available();
//
//            // byte array declared
//            byte[] buf = new byte[numByte];
//
//            int bytesRead = 0;
//            while ((bytesRead = bis.read(buf,0,BLOCK_SIZE)) != -1)
//            {
//                // for each byte in buf
//                for (byte b : buf) {
//                    //System.out.print((char)b);
//                }
//
//                System.out.println(bytesRead);
//            }
//
//
//        } catch(Exception e) {
//            e.printStackTrace();
//        } finally {
//            // releases any system resources associated with the stream
//            if(inStream!=null)
//                inStream.close();
//            if(bis!=null)
//                bis.close();
//        }

    private static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

     private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

}
