import java.io.Serializable;
import java.util.ArrayList;

class Record implements Serializable {
    private long id;
    private ArrayList<Double> coordinates;

    Record(long id, ArrayList<Double> coordinates) {
        this.id = id;
        this.coordinates = coordinates;
    }

    Record(String recordInString)
    {
        String[] stringArray;
        /* given string will be split by the argument delimiter provided. */
        stringArray = recordInString.split(MetaData.getDELIMITER());

        if (stringArray.length < MetaData.DIMENSIONS + 1)
            throw new IllegalArgumentException("In order to convert a String to a Record, a Long and a total amount of coordinates for each dimension must be given");

        id = Long.parseLong(stringArray[0]);
        coordinates = new ArrayList<>();
        for (int i = 1; i < stringArray.length ; i++)
            coordinates.add(Double.parseDouble(stringArray[i]));
    }

    long getId() {
        return id;
    }

    ArrayList<Double> getCoordinates() {
        return coordinates;
    }

    // Returns the coordinate on the dimension(axis) given as parameter
    double getCoordinate(int dimension)
    {
        return coordinates.get(dimension);
    }

//    @Override
//    public String toString() {
//        return "Record{" +
//                "id=" + id +
//                ", lon=" + coordinates.get(0) +
//                ", lat=" + coordinates.get(1) +
//                '}';
//    }

    @Override
    public String toString() {
        return  id + "," + coordinates.get(0) + "," + coordinates.get(1);
    }
}
