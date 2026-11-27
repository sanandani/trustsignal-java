package io.github.sanandani.trustsignal.score;

import io.github.sanandani.trustsignal.model.*;
import java.util.*;

/** Computes separate dimension values and confidence; never produces an aggregate. */
public final class AceUScorer {
    private final Map<Dimension, Double> weights;
    public AceUScorer(Map<Dimension, Double> weights) {
        EnumMap<Dimension, Double> copy = new EnumMap<>(Dimension.class); copy.putAll(weights);
        if (copy.size() != Dimension.values().length || copy.values().stream().anyMatch(w -> !Double.isFinite(w) || w < 0)) throw new IllegalArgumentException("nonnegative weight for every dimension required");
        this.weights = Map.copyOf(copy);
    }
    public Result score(Collection<SignalEvidence> evidence) {
        EnumMap<Dimension, Double> valueNumerator = new EnumMap<>(Dimension.class), valueDenominator = new EnumMap<>(Dimension.class), confidenceNumerator = new EnumMap<>(Dimension.class);
        EnumMap<Dimension, List<String>> explanations = new EnumMap<>(Dimension.class);
        for (Dimension d : Dimension.values()) { valueNumerator.put(d, 0d); valueDenominator.put(d, 0d); confidenceNumerator.put(d, 0d); explanations.put(d, new ArrayList<>()); }
        List<String> excluded = new ArrayList<>();
        for (SignalEvidence item : evidence) {
            switch (item.outcome()) {
                case SignalOutcome.Verified ignored -> add(item, 1, 1, valueNumerator, valueDenominator, confidenceNumerator, explanations);
                case SignalOutcome.Inferred inferred -> add(item, inferred.confidence(), inferred.confidence(), valueNumerator, valueDenominator, confidenceNumerator, explanations);
                case SignalOutcome.Unverifiable ignored -> { for (Dimension d : item.contributions().keySet()) explanations.get(d).add(item.id() + ": unverified; confidence reduced"); }
                case SignalOutcome.Invalid invalid -> excluded.add(item.id() + ": " + invalid.reason());
            }
        }
        EnumMap<Dimension, DimensionScore> scores = new EnumMap<>(Dimension.class);
        for (Dimension d : Dimension.values()) {
            double denominator = valueDenominator.get(d);
            double value = denominator == 0 ? 0 : valueNumerator.get(d) / denominator;
            if (Math.abs(value) < 1e-12) value = 0;
            if (Math.abs(value - 1) < 1e-12) value = 1;
            double weight = weights.get(d);
            double confidence = weight == 0 ? 0 : Math.min(1, confidenceNumerator.get(d) / weight);
            scores.put(d, new DimensionScore(d, value, confidence, explanations.get(d)));
        }
        return new Result(new AceUProfile(scores), excluded);
    }
    private void add(SignalEvidence item, double factor, double confidenceFactor, EnumMap<Dimension, Double> n, EnumMap<Dimension, Double> den, EnumMap<Dimension, Double> cn, EnumMap<Dimension, List<String>> exp) {
        item.contributions().forEach((d, v) -> { double w = weights.get(d); n.merge(d, w * factor * v, Double::sum); den.merge(d, w * factor, Double::sum); cn.merge(d, w * confidenceFactor, Double::sum); exp.get(d).add(item.id() + ": " + item.explanation()); });
    }
    public record SignalEvidence(String id, SignalOutcome outcome, Map<Dimension, Double> contributions, String explanation) {
        public SignalEvidence { Objects.requireNonNull(id); Objects.requireNonNull(outcome); Objects.requireNonNull(explanation); EnumMap<Dimension, Double> c = new EnumMap<>(Dimension.class); c.putAll(contributions); c.values().forEach(v -> { if (!Double.isFinite(v) || v < 0 || v > 1) throw new IllegalArgumentException("contribution must be in [0,1]"); }); contributions = Map.copyOf(c); }
    }
    public record Result(AceUProfile profile, List<String> excluded) { public Result { excluded = List.copyOf(excluded); } }
}
