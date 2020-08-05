// Class used to represent the distribution of a set of entries in to two groups (the two DistributionGroup objects) along a specific axis
class Distribution {
    private DistributionGroup firstGroup; // The first distribution group
    private DistributionGroup secondGroup; // The second distribution group

    Distribution(DistributionGroup firstGroup, DistributionGroup secondGroup) {
        this.firstGroup = firstGroup;
        this.secondGroup = secondGroup;
    }

    DistributionGroup getFirstGroup() {
        return firstGroup;
    }

    DistributionGroup getSecondGroup() {
        return secondGroup;
    }
}
