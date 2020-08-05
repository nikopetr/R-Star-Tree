import java.util.ArrayList;

// Class use for queries execution with the use of the RStarTree
abstract class Query {
    // Returns the ids of the query's records
    abstract ArrayList<Long> getQueryRecordIds(Node node);
}
