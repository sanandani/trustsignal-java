package io.github.sanandani.trustsignal.example;

import io.github.sanandani.trustsignal.model.*;
import io.github.sanandani.trustsignal.score.AceUScorer;
import java.util.Map;

/** Synthetic composition-root example. */
public final class Main {
    private Main() { }
    public static void main(String[] args) {
        var scorer = new AceUScorer(Map.of(Dimension.AUTHENTICITY, 1d, Dimension.CREDIBILITY, 1d, Dimension.EMPATHY, 1d, Dimension.UNIQUENESS, 1d));
        var evidence = new AceUScorer.SignalEvidence("demo-claim", new SignalOutcome.Verified(), Map.of(Dimension.AUTHENTICITY, .9, Dimension.CREDIBILITY, .8, Dimension.EMPATHY, .7, Dimension.UNIQUENESS, .6), "synthetic example evidence");
        var result = scorer.score(java.util.List.of(evidence));
        result.profile().dimensions().forEach((dimension, score) -> System.out.println(dimension + "=" + score.value() + " confidence=" + score.confidence()));
    }
}
