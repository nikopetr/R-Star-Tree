import java.io.Serializable;
import java.util.ArrayList;

class Record implements Serializable {
    private long id;
    private ArrayList<Double> coordinates;

    Record(long id, ArrayList<Double> coordinates) {
        this.id = id;
        this.coordinates = coordinates;
    }

    long getId() {
        return id;
    }

    public ArrayList<Double> getCoordinates() {
        return coordinates;
    }

    // Returns the coordinate on the dimension(axis) given as parameter
    double getCoordinate(int dimension)
    {
        return coordinates.get(dimension);
    }

    @Override
    public String toString() {
        return "Record{" +
                "id=" + id +
                ", lon=" + coordinates.get(0) +
                ", lat=" + coordinates.get(1) +
                '}';
    }
}
