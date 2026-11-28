package io.github.sanandani.trustsignal.example;

import io.github.sanandani.trustsignal.model.*;
import io.github.sanandani.trustsignal.score.AceUScorer;
import java.util.List;
import java.util.Map;

/** Synthetic composition-root example. */
public final class Main {
    private Main() { }
    public static void main(String[] args) {
        var scorer = new AceUScorer(Map.of(Dimension.AUTHENTICITY, 1d, Dimension.CREDIBILITY, 1d, Dimension.EMPATHY, 1d, Dimension.UNIQUENESS, 1d));
        var contributions = Map.of(Dimension.AUTHENTICITY, .9, Dimension.CREDIBILITY, .8, Dimension.EMPATHY, .7, Dimension.UNIQUENESS, .6);

        show(scorer, "verified", new SignalOutcome.Verified(), contributions);
        show(scorer, "inferred", new SignalOutcome.Inferred("synthetic self-report", .5), contributions);
        show(scorer, "unverifiable", new SignalOutcome.Unverifiable(SignalOutcome.Reason.TIMEOUT), contributions);
        show(scorer, "invalid", new SignalOutcome.Invalid(SignalOutcome.InvalidReason.BAD_SIGNATURE), contributions);
    }

    private static void show(AceUScorer scorer, String label, SignalOutcome outcome,
                             Map<Dimension, Double> contributions) {
        var evidence = new AceUScorer.SignalEvidence("synthetic-claim", outcome, contributions, "synthetic example evidence");
        var result = scorer.score(List.of(evidence));
        System.out.println(label + ":");
        result.profile().dimensions().forEach((dimension, score) ->
                System.out.println("  " + dimension + "=" + score.value() + " confidence=" + score.confidence()
                        + " notes=" + score.contributions()));
        if (!result.excluded().isEmpty()) System.out.println("  excluded=" + result.excluded());
    }
}
