import java.util.ArrayList;
// Class use for queries execution without any use of an index
abstract class SequentialScanQuery {
    // Returns the ids of the query's records
    abstract ArrayList<Long> getQueryRecordIds();
}
