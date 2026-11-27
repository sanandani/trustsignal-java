package io.github.sanandani.trustsignal;

import io.github.sanandani.trustsignal.model.*;
import io.github.sanandani.trustsignal.score.AceUScorer;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ScorerTest {
    private AceUScorer scorer() { return new AceUScorer(Map.of(Dimension.AUTHENTICITY, 1d, Dimension.CREDIBILITY, 1d, Dimension.EMPATHY, 1d, Dimension.UNIQUENESS, 1d)); }
    private AceUScorer.SignalEvidence item(String id, SignalOutcome outcome, double value) { return new AceUScorer.SignalEvidence(id, outcome, Map.of(Dimension.AUTHENTICITY, value), "synthetic"); }
    @Test void unverifiableEvidenceDoesNotChangeValueButReducesConfidence() {
        var verified = item("a", new SignalOutcome.Verified(), .8);
        var baseline = scorer().score(List.of(verified)).profile().dimensions().get(Dimension.AUTHENTICITY);
        var extended = scorer().score(List.of(verified, item("b", new SignalOutcome.Unverifiable(SignalOutcome.Reason.TIMEOUT), .1))).profile().dimensions().get(Dimension.AUTHENTICITY);
        assertEquals(baseline.value(), extended.value()); assertTrue(extended.confidence() <= baseline.confidence());
    }
    @Test void permutationDoesNotChangeProfile() {
        var a = item("a", new SignalOutcome.Verified(), .2); var b = item("b", new SignalOutcome.Verified(), .8);
        var left = scorer().score(List.of(a,b)).profile().dimensions().get(Dimension.AUTHENTICITY);
        var right = scorer().score(List.of(b,a)).profile().dimensions().get(Dimension.AUTHENTICITY);
        assertEquals(left.value(), right.value(), 1e-12); assertEquals(left.confidence(), right.confidence(), 1e-12);
        assertEquals(new java.util.TreeSet<>(left.contributions()), new java.util.TreeSet<>(right.contributions()));
    }
    @Test void invalidEvidenceIsExcluded() {
        var result = scorer().score(List.of(item("bad", new SignalOutcome.Invalid(SignalOutcome.InvalidReason.BAD_SIGNATURE), .9)));
        assertEquals(List.of("bad: BAD_SIGNATURE"), result.excluded());
        assertEquals(0, result.profile().dimensions().get(Dimension.AUTHENTICITY).value());
    }
    @Test void claimRejectsInvertedValidityWindow() {
        var now = java.time.Instant.parse("2026-01-01T00:00:00Z");
        assertThrows(IllegalArgumentException.class, () -> new Claim("a", "synthetic", "assertion", now, now.minusSeconds(1)));
    }
}
