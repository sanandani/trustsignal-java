package io.github.sanandani.trustsignal.verify;

import io.github.sanandani.trustsignal.model.*;
import java.time.Clock;

@FunctionalInterface
public interface SignalVerifier {
    SignalOutcome verify(SignedClaim claim);
    static SignalVerifier composed(Clock clock, SignatureVerifier signatures, TrustedIssuers issuers, RevocationChecker revocations) {
        return signed -> {
            if (!issuers.trusts(signed.keyId())) return new SignalOutcome.Unverifiable(SignalOutcome.Reason.UNKNOWN_ISSUER);
            if (clock.instant().isAfter(signed.claim().expiresAt())) return new SignalOutcome.Unverifiable(SignalOutcome.Reason.EXPIRED);
            if (revocations.isRevoked(signed.keyId(), signed.claim().id())) return new SignalOutcome.Invalid(SignalOutcome.InvalidReason.REVOKED);
            return signatures.verify(signed) ? new SignalOutcome.Verified() : new SignalOutcome.Invalid(SignalOutcome.InvalidReason.BAD_SIGNATURE);
        };
    }
}
