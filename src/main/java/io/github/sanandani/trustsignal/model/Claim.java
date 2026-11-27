package io.github.sanandani.trustsignal.model;

import java.time.Instant;
import java.util.Objects;

/** A synthetic or user-supplied assertion with a stable identifier and validity window. */
public record Claim(String id, String subject, String statement, Instant issuedAt, Instant expiresAt) {
    public Claim {
        Objects.requireNonNull(id); Objects.requireNonNull(subject); Objects.requireNonNull(statement);
        Objects.requireNonNull(issuedAt); Objects.requireNonNull(expiresAt);
        if (expiresAt.isBefore(issuedAt)) throw new IllegalArgumentException("expiry precedes issue time");
    }
}
