package io.github.sanandani.trustsignal.model;

import java.util.List;
import java.util.Objects;

public record DimensionScore(Dimension dimension, double value, double confidence, List<String> contributions) {
    public DimensionScore {
        Objects.requireNonNull(dimension); Objects.requireNonNull(contributions);
        unit(value); unit(confidence); contributions = List.copyOf(contributions);
    }
    private static void unit(double v) { if (!Double.isFinite(v) || v < 0 || v > 1) throw new IllegalArgumentException("expected value in [0,1]"); }
}
