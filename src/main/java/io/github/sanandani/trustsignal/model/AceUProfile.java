package io.github.sanandani.trustsignal.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** Immutable four-axis profile. Deliberately exposes no aggregate score. */
public record AceUProfile(Map<Dimension, DimensionScore> dimensions) {
    public AceUProfile {
        Objects.requireNonNull(dimensions);
        EnumMap<Dimension, DimensionScore> copy = new EnumMap<>(Dimension.class);
        copy.putAll(dimensions);
        if (copy.size() != Dimension.values().length) throw new IllegalArgumentException("exactly four dimensions are required");
        for (Dimension d : Dimension.values()) if (!copy.containsKey(d) || copy.get(d).dimension() != d) throw new IllegalArgumentException("missing or mismatched " + d);
        dimensions = Map.copyOf(copy);
    }
}
