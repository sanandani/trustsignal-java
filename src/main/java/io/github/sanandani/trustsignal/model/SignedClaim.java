package io.github.sanandani.trustsignal.model;

import java.util.Objects;

/** Signed claim. Signature bytes are defensively copied at both boundaries. */
public record SignedClaim(Claim claim, byte[] signature, String keyId) {
    public SignedClaim {
        Objects.requireNonNull(claim); Objects.requireNonNull(signature); Objects.requireNonNull(keyId);
        signature = signature.clone();
    }
    @Override public byte[] signature() { return signature.clone(); }
}
