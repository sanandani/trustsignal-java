package io.github.sanandani.trustsignal;

import io.github.sanandani.trustsignal.model.*;
import io.github.sanandani.trustsignal.verify.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class VerificationTest {
    private SignedClaim signed(String key, Instant expiry) { return new SignedClaim(new Claim("synthetic-id", "synthetic-subject", "synthetic statement", Instant.parse("2026-01-01T00:00:00Z"), expiry), new byte[]{1,2}, key); }
    private SignalVerifier verifier(Clock clock, boolean trusted, boolean revoked, boolean signature) {
        return SignalVerifier.composed(clock, ignored -> signature, ignored -> trusted, (key, id) -> revoked);
    }
    @Test void verifiesGoodSignedClaim() {
        var now = Instant.parse("2026-01-02T00:00:00Z");
        assertInstanceOf(SignalOutcome.Verified.class, verifier(Clock.fixed(now, ZoneOffset.UTC), true, false, true).verify(signed("k", now.plusSeconds(20))));
    }
    @Test void unknownIssuerIsUnverifiable() {
        var now = Instant.parse("2026-01-02T00:00:00Z");
        assertEquals(new SignalOutcome.Unverifiable(SignalOutcome.Reason.UNKNOWN_ISSUER), verifier(Clock.fixed(now, ZoneOffset.UTC), false, false, true).verify(signed("k", now.plusSeconds(20))));
    }
    @Test void expiredClaimIsUnverifiable() {
        var now = Instant.parse("2026-01-02T00:00:00Z");
        assertEquals(new SignalOutcome.Unverifiable(SignalOutcome.Reason.EXPIRED), verifier(Clock.fixed(now, ZoneOffset.UTC), true, false, true).verify(signed("k", now.minusSeconds(1))));
    }
    @Test void revokedClaimIsInvalid() {
        var now = Instant.parse("2026-01-02T00:00:00Z");
        assertEquals(new SignalOutcome.Invalid(SignalOutcome.InvalidReason.REVOKED), verifier(Clock.fixed(now, ZoneOffset.UTC), true, true, true).verify(signed("k", now.plusSeconds(20))));
    }
    @Test void badSignatureIsInvalid() {
        var now = Instant.parse("2026-01-02T00:00:00Z");
        assertEquals(new SignalOutcome.Invalid(SignalOutcome.InvalidReason.BAD_SIGNATURE), verifier(Clock.fixed(now, ZoneOffset.UTC), true, false, false).verify(signed("k", now.plusSeconds(20))));
    }
    @Test void signatureBytesAreDefensivelyCopied() {
        byte[] source = {4}; var signed = new SignedClaim(new Claim("i","s","c",Instant.EPOCH,Instant.MAX), source, "k");
        source[0] = 8; assertEquals(4, signed.signature()[0]); byte[] returned = signed.signature(); returned[0] = 7; assertEquals(4, signed.signature()[0]);
    }
    @Test void ed25519SignatureRoundTrip() throws Exception {
        var pair = java.security.KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        var claim = new Claim("synthetic-id", "subject", "statement", Instant.EPOCH, Instant.MAX);
        var unsigned = new SignedClaim(claim, new byte[0], "k");
        var signer = java.security.Signature.getInstance("Ed25519"); signer.initSign(pair.getPrivate());
        signer.update(Ed25519SignatureVerifier.canonical(unsigned).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        var signed = new SignedClaim(claim, signer.sign(), "k");
        assertTrue(new Ed25519SignatureVerifier(Map.of("k", pair.getPublic())).verify(signed));
    }
    @Test void unknownEd25519KeyFailsClosed() { assertFalse(new Ed25519SignatureVerifier(Map.of()).verify(signed("absent", Instant.MAX))); }
    @Test void parallelVerifierPreservesOrderAndDeadline() {
        AtomicInteger calls = new AtomicInteger();
        ParallelVerifier parallel = new ParallelVerifier(c -> { calls.incrementAndGet(); return new SignalOutcome.Verified(); }, 2, Duration.ofSeconds(3));
        var result = parallel.verifyAll(List.of(signed("a", Instant.MAX), signed("b", Instant.MAX)));
        assertEquals(2, calls.get()); assertTrue(result.stream().allMatch(o -> o instanceof SignalOutcome.Verified));
    }
    @Test void parallelVerifierMapsSlowWorkToTimeout() {
        ParallelVerifier parallel = new ParallelVerifier(c -> { try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } return new SignalOutcome.Verified(); }, 1, Duration.ofMillis(10));
        var result = parallel.verifyAll(List.of(signed("a", Instant.MAX)));
        assertEquals(new SignalOutcome.Unverifiable(SignalOutcome.Reason.TIMEOUT), result.getFirst());
    }
}
