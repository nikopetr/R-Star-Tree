import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

class MetaData {
    static int DIMENSIONS;

    static int getMaxEntriesInNodes() {
        int maxSize = 0;
        ArrayList<Entry> entries = new ArrayList<>();
        int i;
        for (i = 0; i < Integer.MAX_VALUE; i++) {
            ArrayList<Bounds> boundsForEachDimension = new ArrayList<>();
            for (int d = 0; d < MetaData.DIMENSIONS; d++)
                boundsForEachDimension.add(new Bounds(0.0, 0.0));
            Entry entry = new LeafEntry(new Random().nextLong(), boundsForEachDimension);
            entries.add(entry);
            byte[] nodeInBytes = new byte[0];
            byte[] goodPutBytes = new byte[0];
            try {
                nodeInBytes = Main.serialize(new Node(new Random().nextInt(), entries));
                goodPutBytes = Main.serialize(nodeInBytes.length);
//                byte[] block = new byte[32768];
//                System.arraycopy(goodPutBytes,0,block,0,goodPutBytes.length);
//                System.arraycopy(nodeInBytes,0,block,goodPutBytes.length,nodeInBytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (goodPutBytes.length + nodeInBytes.length > 32768) {
                System.out.println(maxSize);
                break;
            } else
                maxSize = nodeInBytes.length;
        }
        return i;
    }

    static int getMaxRecordsInBlock() {
        int maxSize = 0;
        ArrayList<Record> records = new ArrayList<>();
        int i;
        for (i = 0; i < Integer.MAX_VALUE; i++) {
            ArrayList<Double> coordinateForEachDimension = new ArrayList<>();
            for (int d = 0; d < MetaData.DIMENSIONS; d++)
                coordinateForEachDimension.add(0.0);
            Record record = new Record(0, coordinateForEachDimension);
            records.add(record);
            byte[] recordInBytes = new byte[0];
            byte[] goodPutBytes = new byte[0];
            try {
                recordInBytes = Main.serialize(records);
                goodPutBytes = Main.serialize(recordInBytes.length);
//                byte[] block = new byte[32768];
//                System.arraycopy(goodPutBytes,0,block,0,goodPutBytes.length);
//                System.arraycopy(recordInBytes,0,block,goodPutBytes.length,recordInBytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (goodPutBytes.length + recordInBytes.length > 32768) {
                System.out.println(maxSize);
                break;
            } else
                maxSize = recordInBytes.length;
        }
        return i;
    }

    static void writeDataFileBlock(ArrayList<Record> records) {
        String fileName = "datafile.dat";

        try {
            byte[] recordInBytes = Main.serialize(records);
            byte[] goodPutBytes = Main.serialize(recordInBytes.length);
            byte[] block = new byte[32768];
            System.arraycopy(goodPutBytes, 0, block, 0, goodPutBytes.length);
            System.arraycopy(recordInBytes, 0, block, goodPutBytes.length, recordInBytes.length);

            try (FileOutputStream out = new FileOutputStream(fileName);
                 BufferedOutputStream bout = new BufferedOutputStream(out)) {
                bout.write(block);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

