package io.github.sanandani.trustsignal.verify;
import io.github.sanandani.trustsignal.model.SignedClaim;
@FunctionalInterface public interface SignatureVerifier { boolean verify(SignedClaim claim); }
