package io.github.sanandani.trustsignal.verify;

import io.github.sanandani.trustsignal.model.SignedClaim;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Map;
import java.util.Objects;

/** JDK Ed25519 adapter using keys supplied by the caller. */
public final class Ed25519SignatureVerifier implements SignatureVerifier {
    private final Map<String, PublicKey> keys;
    public Ed25519SignatureVerifier(Map<String, PublicKey> keys) { this.keys = Map.copyOf(Objects.requireNonNull(keys)); }
    @Override public boolean verify(SignedClaim signed) {
        PublicKey key = keys.get(signed.keyId());
        if (key == null) return false;
        try {
            Signature verifier = Signature.getInstance("Ed25519");
            verifier.initVerify(key);
            verifier.update(canonical(signed).getBytes(StandardCharsets.UTF_8));
            return verifier.verify(signed.signature());
        } catch (java.security.GeneralSecurityException e) { return false; }
    }
    public static String canonical(SignedClaim signed) {
        var c = signed.claim();
        return String.join("\n", c.id(), c.subject(), c.statement(), c.issuedAt().toString(), c.expiresAt().toString(), signed.keyId());
    }
}
