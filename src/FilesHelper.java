import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

// Class used for file operations (saving, updating etc) in the datafile and indexfile
// also holds metadata information (file paths, block sizes, delimiter for csv file etc)
class FilesHelper {
    private static final String DELIMITER = ","; // The delimiter for csv file
    private static final String PATH_TO_CSV = "data.csv";
    static final String PATH_TO_DATAFILE = "datafile.dat";
    private static final String PATH_TO_INDEXFILE = "indexfile.dat";
    private static final int BLOCK_SIZE = 32 * 1024; // Each Block is 32KB
    private static int dataDimensions; // The data's used dimensions
    private static int totalBlocksInDatafile;  // The total blocks written in the datafile
    private static int totalBlocksInIndexFile; // The total blocks written in the indexfile
    private static int totalLevelsOfTreeIndex; // The total levels of the rStar tree

    static String getPathToCsv() {
        return PATH_TO_CSV;
    }

    static String getDELIMITER() {
        return DELIMITER;
    }

    static int getDataDimensions() {
        return dataDimensions;
    }

    // Used to serializable a serializable Object to byte array
    private static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    // Used to deserializable a byte array to a serializable Object
    private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    // Reads the data from the given file path
    // Reads the Block 0, which is used for the metadata
    // Returns an ArrayList<Integer> which includes the values for the metadata in the given file
    private static ArrayList<Integer> readMetaDataBlock(String pathToFile){
        try {
            RandomAccessFile raf = new RandomAccessFile(new File(pathToFile), "rw");
            FileInputStream fis = new FileInputStream(raf.getFD());
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] block = new byte[BLOCK_SIZE];
            if (bis.read(block,0,BLOCK_SIZE) != BLOCK_SIZE)
                throw new IllegalStateException("Block size read was not of " + BLOCK_SIZE + " bytes");

            byte[] goodPutLengthInBytes = serialize(new Random().nextInt()); // Serializing an integer ir order to get the size of goodPutLength in bytes
            System.arraycopy(block, 0, goodPutLengthInBytes, 0, goodPutLengthInBytes.length);

            byte[] dataInBlock = new byte[(Integer)deserialize(goodPutLengthInBytes)];
            System.arraycopy(block, goodPutLengthInBytes.length, dataInBlock, 0, dataInBlock.length);

            return (ArrayList<Integer>)deserialize(dataInBlock);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Updates the metadata on the given file path
    // Saves the current dataDimensions, the block size and the new total blocks on the block 0 of the file
    private static void updateMetaDataBlock(String pathToFile) {
        try {
            ArrayList<Integer> dataFileMetaData = new ArrayList<>();
            dataFileMetaData.add(dataDimensions);
            dataFileMetaData.add(BLOCK_SIZE);
            if (pathToFile.equals(PATH_TO_DATAFILE))
                dataFileMetaData.add(++totalBlocksInDatafile);
            else if (pathToFile.equals(PATH_TO_INDEXFILE))
            {
                dataFileMetaData.add(++totalBlocksInIndexFile);
                dataFileMetaData.add(totalLevelsOfTreeIndex);
            }
            byte[] metaDataInBytes = serialize(dataFileMetaData);
            byte[] goodPutLengthInBytes = serialize(metaDataInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
            System.arraycopy(metaDataInBytes, 0, block, goodPutLengthInBytes.length, metaDataInBytes.length);

            RandomAccessFile f = new RandomAccessFile(new File(pathToFile), "rw");
            f.write(block);
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // dataFile Methods
    static int getTotalBlocksInDatafile() {
        return totalBlocksInDatafile;
    }

    // Calculates the total blocks in the datafile
    // Reads the data from the CSV files and adds it to the datafile
    static void initializeDataFile(int dataDimensions, boolean makeNewDataFile){
        try{
            // Checks if a datafile already exists, initialise the metaData from the metadata block (block 0 of the file)
            // If already exists, initialise the variables with the values of the dimensions, block size and total blocks of the data file
            if (!makeNewDataFile && Files.exists(Paths.get(PATH_TO_DATAFILE)))
            {
                ArrayList<Integer> dataFileMetaData = readMetaDataBlock(PATH_TO_DATAFILE);
                if (dataFileMetaData == null)
                    throw new IllegalStateException("Could not read datafile's Meta Data Block properly");
                FilesHelper.dataDimensions = dataFileMetaData.get(0);
                if (FilesHelper.dataDimensions  <= 0)
                    throw new IllegalStateException("The number of data dimensions must be a positive integer");
                if (dataFileMetaData.get(1) != BLOCK_SIZE)
                    throw new IllegalStateException("Block size read was not of " + BLOCK_SIZE + " bytes");
                totalBlocksInDatafile = dataFileMetaData.get(2);
                if (totalBlocksInDatafile  < 0)
                    throw new IllegalStateException("The total blocks of the datafile cannot be a negative number");
            }
            // Else initialize a new datafile
            else
            {
                Files.deleteIfExists(Paths.get(PATH_TO_DATAFILE)); // Resetting/Deleting dataFile data
                FilesHelper.dataDimensions = dataDimensions;
                if (FilesHelper.dataDimensions  <= 0)
                    throw new IllegalStateException("The number of data dimensions must be a positive integer");
                updateMetaDataBlock(PATH_TO_DATAFILE);
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
            }
        }catch(Exception e){e.printStackTrace();}
    }

    // Calculates and return an integer which represents the maximum number of records a block of BLOCK_SIZE can have
    private static int calculateMaxRecordsInBlock() {
        ArrayList<Record> blockRecords = new ArrayList<>();
        int i;
        for (i = 0; i < Integer.MAX_VALUE; i++) {
            ArrayList<Double> coordinateForEachDimension = new ArrayList<>();
            for (int d = 0; d < FilesHelper.dataDimensions; d++)
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

   // Writes/saves the given array of records as a new block of bytes in the dataFile (works as append only)
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
            updateMetaDataBlock(PATH_TO_DATAFILE);
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
                throw new IllegalStateException("Block size read was not of " + BLOCK_SIZE + " bytes");
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

    // Index File Methods
    static int getTotalBlocksInIndexFile() {
        return totalBlocksInIndexFile;
    }

    static int getTotalLevelsOfTreeIndex() {
        return totalLevelsOfTreeIndex;
    }

    // Calculates and return an integer which represents the maximum number of records a block of BLOCK_SIZE can have
    static int calculateMaxEntriesInNode() {
        ArrayList<Entry> entries = new ArrayList<>();
        int i;
        for (i = 0; i < Integer.MAX_VALUE; i++) {
            ArrayList<Bounds> boundsForEachDimension = new ArrayList<>();
            for (int d = 0; d < FilesHelper.dataDimensions; d++)
                boundsForEachDimension.add(new Bounds(0.0, 0.0));
            Entry entry = new LeafEntry(new Random().nextLong(),new Random().nextLong(), boundsForEachDimension);
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

    // Updates the metadata block with an increased level of tree index in the indexFile
    // Saves the current dataDimensions, the block size, the total blocks on the block 0 and the new increased totalLevelsOfTreeIndex
    private static void updateLevelsOfTreeInIndexFile()
    {
        try {
            ArrayList<Integer> dataFileMetaData = new ArrayList<>();
            dataFileMetaData.add(dataDimensions);
            dataFileMetaData.add(BLOCK_SIZE);
            dataFileMetaData.add(totalBlocksInIndexFile);
            dataFileMetaData.add(++totalLevelsOfTreeIndex);
            byte[] metaDataInBytes = serialize(dataFileMetaData);
            byte[] goodPutLengthInBytes = serialize(metaDataInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
            System.arraycopy(metaDataInBytes, 0, block, goodPutLengthInBytes.length, metaDataInBytes.length);

            RandomAccessFile f = new RandomAccessFile(new File(PATH_TO_INDEXFILE), "rw");
            f.write(block);
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Calculates the total blocks in the indexFile
    // Reads the data from the CSV files and adds it to the indexFile
    static void initializeIndexFile(int dataDimensions, boolean makeNewDataFile){
        try{
            // Checks if an indexFile already exists, initialise the metaData from the metadata block (block 0 of the file)
            // Initialise the variables with the values of the dimensions, block size, total blocks, and the levels (height) of the RStar tree index
            if (!makeNewDataFile && Files.exists(Paths.get(PATH_TO_INDEXFILE)))
            {
                ArrayList<Integer> indexFileMetaData = readMetaDataBlock(PATH_TO_INDEXFILE);
                if (indexFileMetaData == null)
                    throw new IllegalStateException("Could not read datafile's Meta Data Block properly");
                FilesHelper.dataDimensions = indexFileMetaData.get(0);
                if (FilesHelper.dataDimensions  <= 0)
                    throw new IllegalStateException("The number of data dimensions must be a positive integer");
                if (indexFileMetaData.get(1) != BLOCK_SIZE)
                    throw new IllegalStateException("Block size read was not of " + BLOCK_SIZE + " bytes");
                totalBlocksInIndexFile = indexFileMetaData.get(2);
                if (totalBlocksInIndexFile  < 0)
                    throw new IllegalStateException("The total blocks of the index file cannot be a negative number");
                totalLevelsOfTreeIndex = indexFileMetaData.get(3);
                if (totalLevelsOfTreeIndex  < 0)
                    throw new IllegalStateException("The total index's tree levels cannot be a negative number");
            }
            // Else if not exists or a new one is to be made, initialize a new indexFile
            else
            {
                Files.deleteIfExists(Paths.get(PATH_TO_INDEXFILE)); // Resetting/Deleting index file data
                FilesHelper.dataDimensions = dataDimensions;
                totalLevelsOfTreeIndex = 1; // increasing the size from the root, the root (top level) will always have the highest level
                if (FilesHelper.dataDimensions  <= 0)
                    throw new IllegalStateException("The number of data dimensions must be a positive integer");
                updateMetaDataBlock(PATH_TO_INDEXFILE);
            }
        }catch(Exception e){e.printStackTrace();}
    }

    // Writes/saves the given array of records as a new block of bytes in the indexFile (works as append only)
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
            updateMetaDataBlock(PATH_TO_INDEXFILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Updates the indexFile block with the corresponding given already saved Node
    // In case node's block id is the root's and the given parameter totalLevelsOfTreeIndex is changed during the tree's changes then
    // the totalLevelsOfTreeIndex variable's value is increased by one
    static void updateIndexFileBlock(Node node, int totalLevelsOfTreeIndex) {
        try {
            byte[] nodeInBytes = serialize(node);
            byte[] goodPutLengthInBytes = serialize(nodeInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
            System.arraycopy(nodeInBytes, 0, block, goodPutLengthInBytes.length, nodeInBytes.length);

            RandomAccessFile f = new RandomAccessFile(new File(PATH_TO_INDEXFILE), "rw");
            f.seek(node.getBlockId()*BLOCK_SIZE); // this basically reads n bytes in the file
            f.write(block);
            f.close();

            if (node.getBlockId() == RStarTree.getRootNodeBlockId() && FilesHelper.totalLevelsOfTreeIndex != totalLevelsOfTreeIndex)
                updateLevelsOfTreeInIndexFile();

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
                throw new IllegalStateException("Block size read was not of " + BLOCK_SIZE + "bytes");


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