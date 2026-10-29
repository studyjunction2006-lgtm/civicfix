package com.civicfix.issue;

public enum IssueSeverity {
    LOW(10.0),
    MEDIUM(25.0),
    HIGH(45.0),
    CRITICAL(70.0);

    private final double basePoints;

    IssueSeverity(double basePoints) {
        this.basePoints = basePoints;
    }

    public double getBasePoints() {
        return basePoints;
    }
}
