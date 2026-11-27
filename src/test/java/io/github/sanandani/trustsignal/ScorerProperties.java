package io.github.sanandani.trustsignal;

import io.github.sanandani.trustsignal.model.*;
import io.github.sanandani.trustsignal.score.AceUScorer;
import net.jqwik.api.*;
import java.util.*;

class ScorerProperties {
    private final AceUScorer scorer = new AceUScorer(Map.of(Dimension.AUTHENTICITY, 1d, Dimension.CREDIBILITY, 1d, Dimension.EMPATHY, 1d, Dimension.UNIQUENESS, 1d));
    @Property(tries = 80) void addingUnverifiableNeverLowersValue(@ForAll @net.jqwik.api.constraints.DoubleRange(min = 0, max = 1) double value) {
        var verified = new AceUScorer.SignalEvidence("v", new SignalOutcome.Verified(), Map.of(Dimension.AUTHENTICITY, value), "synthetic");
        var absent = new AceUScorer.SignalEvidence("u", new SignalOutcome.Unverifiable(SignalOutcome.Reason.MISSING), Map.of(Dimension.AUTHENTICITY, 0d), "synthetic");
        double before = scorer.score(List.of(verified)).profile().dimensions().get(Dimension.AUTHENTICITY).value();
        double after = scorer.score(List.of(verified, absent)).profile().dimensions().get(Dimension.AUTHENTICITY).value();
        if (before > after) throw new AssertionError("unverifiable evidence changed value");
    }
    @Property(tries = 60) void scoringIsPermutationInvariant(@ForAll @net.jqwik.api.constraints.DoubleRange(min = 0, max = 1) double a, @ForAll @net.jqwik.api.constraints.DoubleRange(min = 0, max = 1) double b) {
        var one = new AceUScorer.SignalEvidence("a", new SignalOutcome.Verified(), Map.of(Dimension.AUTHENTICITY, a), "x");
        var two = new AceUScorer.SignalEvidence("b", new SignalOutcome.Inferred("synthetic", .5), Map.of(Dimension.AUTHENTICITY, b), "y");
        var x = scorer.score(List.of(one,two)).profile().dimensions().get(Dimension.AUTHENTICITY);
        var y = scorer.score(List.of(two,one)).profile().dimensions().get(Dimension.AUTHENTICITY);
        if (Math.abs(x.value()-y.value()) > 1e-12) throw new AssertionError("permutation changed value");
    }
}
