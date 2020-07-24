import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

class MetaData {
    static int DIMENSIONS;
    private static final String DELIMITER = ","; /* delimiter for csv */
    private static final String PATH_TO_CSV = "data.csv";
    private static final String PATH_TO_DATAFILE = "datafile.dat";
    private static final String PATH_TO_INDEXFILE = "indexfile.dat";
    private static final int BLOCK_SIZE = 32 * 1024; // Each Block is 32KB
    private static int totalBlocksInDatafile;
    private static int totalBlocksInIndexFile;

    static String getDELIMITER() {
        return DELIMITER;
    }


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

    static int getTotalBlocksInDatafile() {
        return totalBlocksInDatafile;
    }

    // DATAFILE STUFF

    // Reads the data from the CSV files and adds it to the datafile
    static void initializeDataFile(){
        try{
            Files.deleteIfExists(Paths.get(PATH_TO_DATAFILE)); // Resetting data file
            ArrayList<Record> blockRecords = new ArrayList<>();
            BufferedReader csvReader = (new BufferedReader(new FileReader(PATH_TO_CSV))); // BufferedReader used to read the data from the csv file
            String stringRecord; // String used to read each line (row) of the csv file
            int maxRecordsInBlock = calculateMaxRecordsInBlock();
            while ((stringRecord = csvReader.readLine()) != null)
            {
                if (blockRecords.size() == maxRecordsInBlock)
                {
                    writeDataFileBlock(blockRecords);
                    blockRecords =  new ArrayList<>();
                }
                blockRecords.add(new Record(stringRecord));
            }
            csvReader.close();
            if (blockRecords.size() > 0)
                writeDataFileBlock(blockRecords);

        }catch(Exception e){e.printStackTrace();}
    }

    private static int calculateMaxRecordsInBlock() {
        ArrayList<Record> blockRecords = new ArrayList<>();
        int i;
        for (i = 0; i < Integer.MAX_VALUE; i++) {
            ArrayList<Double> coordinateForEachDimension = new ArrayList<>();
            for (int d = 0; d < MetaData.DIMENSIONS; d++)
                coordinateForEachDimension.add(0.0);
            Record record = new Record(0, coordinateForEachDimension);
            blockRecords.add(record);
            byte[] recordInBytes = new byte[0];
            byte[] goodPutLengthInBytes = new byte[0];
            try {
                recordInBytes = serialize(blockRecords);
                goodPutLengthInBytes = serialize(recordInBytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (goodPutLengthInBytes.length + recordInBytes.length > BLOCK_SIZE)
                break;
        }
        return i;
    }

   // Works as append only
    private static void writeDataFileBlock(ArrayList<Record> records) {
        try {
            byte[] recordInBytes = serialize(records);
            byte[] goodPutLengthInBytes = serialize(recordInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
            System.arraycopy(recordInBytes, 0, block, goodPutLengthInBytes.length, recordInBytes.length);

            FileOutputStream fos = new FileOutputStream(PATH_TO_DATAFILE,true);
            BufferedOutputStream bout = new BufferedOutputStream(fos);
            bout.write(block);
            totalBlocksInDatafile++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static ArrayList<Record> readDataFileBlock(int blockId){
        try {
            RandomAccessFile raf = new RandomAccessFile(new File(PATH_TO_DATAFILE), "rw");
            FileInputStream fis = new FileInputStream(raf.getFD());
            BufferedInputStream bis = new BufferedInputStream(fis);
            //seek to a a different section of the file, so discard the previous buffer
            raf.seek(blockId*BLOCK_SIZE);
            //bis = new BufferedInputStream(fis);
            byte[] block = new byte[BLOCK_SIZE];
            if (bis.read(block,0,BLOCK_SIZE) != BLOCK_SIZE)
                throw new IllegalStateException("Block size read was not BLOCK_SIZE bytes");


            byte[] goodPutLengthInBytes = serialize(new Random().nextInt()); // Serializing an integer ir order to get the size of goodPutLength in bytes
            System.arraycopy(block, 0, goodPutLengthInBytes, 0, goodPutLengthInBytes.length);

            byte[] recordsInBlock = new byte[(Integer)deserialize(goodPutLengthInBytes)];
            System.arraycopy(block, goodPutLengthInBytes.length, recordsInBlock, 0, recordsInBlock.length);

            return (ArrayList<Record>)deserialize(recordsInBlock);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // INDEX FILE STUFF
    static int getTotalBlocksInIndexFile() {
        return totalBlocksInIndexFile;
    }

    static int calculateMaxEntriesInNode() {
        ArrayList<Entry> entries = new ArrayList<>();
        int i;
        for (i = 0; i < Integer.MAX_VALUE; i++) {
            ArrayList<Bounds> boundsForEachDimension = new ArrayList<>();
            for (int d = 0; d < MetaData.DIMENSIONS; d++)
                boundsForEachDimension.add(new Bounds(0.0, 0.0));
            Entry entry = new LeafEntry(new Random().nextLong(), boundsForEachDimension);
            entry.setChildNodeBlockId(new Random().nextLong());
            entries.add(entry);
            byte[] nodeInBytes = new byte[0];
            byte[] goodPutBytes = new byte[0];
            try {
                nodeInBytes = serialize(new Node(new Random().nextInt(), entries));
                goodPutBytes = serialize(nodeInBytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (goodPutBytes.length + nodeInBytes.length > BLOCK_SIZE)
                break;
        }
        return i;
    }

    static void resetIndexFile(){
        try {
            Files.deleteIfExists(Paths.get(PATH_TO_INDEXFILE)); // Resetting/Deleting index file data
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Works as append only
    static void writeNewIndexFileBlock(Node node) {
        try {
            byte[] nodeInBytes = serialize(node);
            byte[] goodPutLengthInBytes = serialize(nodeInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
            System.arraycopy(nodeInBytes, 0, block, goodPutLengthInBytes.length, nodeInBytes.length);

            FileOutputStream fos = new FileOutputStream(PATH_TO_INDEXFILE,true);
            BufferedOutputStream bout = new BufferedOutputStream(fos);
            bout.write(block);
            totalBlocksInIndexFile++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void updateIndexFileBlock(Node node, long blockId) {
        try {
            byte[] nodeInBytes = serialize(node);
            byte[] goodPutLengthInBytes = serialize(nodeInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
            System.arraycopy(nodeInBytes, 0, block, goodPutLengthInBytes.length, nodeInBytes.length);

            RandomAccessFile f = new RandomAccessFile(new File(PATH_TO_INDEXFILE), "rw");
            f.seek(blockId*BLOCK_SIZE); // this basically reads n bytes in the file
            f.write(block);
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Node readIndexFileBlock(long blockId){
        try {
            RandomAccessFile raf = new RandomAccessFile(new File(PATH_TO_INDEXFILE), "rw");
            FileInputStream fis = new FileInputStream(raf.getFD());
            BufferedInputStream bis = new BufferedInputStream(fis);
            //seek to a a different section of the file, so discard the previous buffer
            raf.seek(blockId*BLOCK_SIZE);
            //bis = new BufferedInputStream(fis);
            byte[] block = new byte[BLOCK_SIZE];
            if (bis.read(block,0,BLOCK_SIZE) != BLOCK_SIZE)
                throw new IllegalStateException("Block size read was not BLOCK_SIZE bytes");


            byte[] goodPutLengthInBytes = serialize(new Random().nextInt()); // Serializing an integer ir order to get the size of goodPutLength in bytes
            System.arraycopy(block, 0, goodPutLengthInBytes, 0, goodPutLengthInBytes.length);

            byte[] nodeInBytes = new byte[(Integer)deserialize(goodPutLengthInBytes)];
            System.arraycopy(block, goodPutLengthInBytes.length, nodeInBytes, 0, nodeInBytes.length);

            return (Node)deserialize(nodeInBytes);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}