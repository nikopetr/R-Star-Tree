// Class used in order to compare and sort Entry objects based on their area enlargement
class EntryAreaEnlargementPair implements Comparable {
    private Entry entry; // The Entry object
    private double areaEnlargement; // It's area enlargement assigned

    EntryAreaEnlargementPair(Entry entry, double areaEnlargement){
        this.entry = entry;
        this.areaEnlargement = areaEnlargement;
    }

    Entry getEntry() {
        return entry;
    }

    private double getAreaEnlargement() {
        return areaEnlargement;
    }

    // Comparing the pairs by area enlargement
    @Override
    public int compareTo(Object obj) {
        EntryAreaEnlargementPair pairB = (EntryAreaEnlargementPair)obj;
        // Resolve ties by choosing the entry with the rectangle of smallest area
        if (this.getAreaEnlargement() == pairB.getAreaEnlargement())
            return Double.compare(this.getEntry().getBoundingBox().getArea(),pairB.getEntry().getBoundingBox().getArea());
        else
            return Double.compare(this.getAreaEnlargement(),pairB.getAreaEnlargement());
        }
    }
