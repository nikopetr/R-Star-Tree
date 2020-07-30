import java.util.ArrayList;

abstract class SequentialScanQuery {
    abstract ArrayList<Long> getQueryRecordIds(int blockId);
}
