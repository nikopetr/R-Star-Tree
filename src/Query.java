import java.util.ArrayList;

abstract class Query {
    abstract ArrayList<Long> getQueryRecordIds(Node node);
}
