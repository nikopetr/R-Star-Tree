class Distribution {
    private DistributionGroup firstGroup;
    private DistributionGroup secondGroup;

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
