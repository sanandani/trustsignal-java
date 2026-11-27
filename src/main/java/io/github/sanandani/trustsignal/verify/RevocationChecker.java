package io.github.sanandani.trustsignal.verify;
@FunctionalInterface public interface RevocationChecker { boolean isRevoked(String keyId, String claimId); }
