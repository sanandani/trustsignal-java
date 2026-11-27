package io.github.sanandani.trustsignal.model;

import java.util.Objects;

public sealed interface SignalOutcome permits SignalOutcome.Verified, SignalOutcome.Inferred,
        SignalOutcome.Unverifiable, SignalOutcome.Invalid {
    record Verified() implements SignalOutcome { }
    record Inferred(String basis, double confidence) implements SignalOutcome {
        public Inferred { Objects.requireNonNull(basis); unit(confidence); }
    }
    record Unverifiable(Reason reason) implements SignalOutcome {
        public Unverifiable { Objects.requireNonNull(reason); }
    }
    record Invalid(InvalidReason reason) implements SignalOutcome {
        public Invalid { Objects.requireNonNull(reason); }
    }
    enum Reason { MISSING, EXPIRED, UNKNOWN_ISSUER, TIMEOUT }
    enum InvalidReason { BAD_SIGNATURE, REVOKED }
    private static void unit(double value) {
        if (!Double.isFinite(value) || value < 0 || value > 1) throw new IllegalArgumentException("expected value in [0,1]");
    }
}
