# Threat model

## Scope and assumptions

The library verifies caller-provided claims and computes caller-configured dimension summaries in-process. It assumes the application protects private keys, obtains public keys and revocation state from suitable sources, and presents results with context and consent. It provides no network transport, storage, identity proofing, or policy enforcement.

| STRIDE | Example threat | Mitigation / residual risk |
|---|---|---|
| Spoofing | Forged claim or unknown key | Ed25519 verification and explicit trusted issuer policy; caller owns key enrollment |
| Tampering | Signature or claim altered | Signed canonical representation; canonicalization format must remain stable |
| Repudiation | Issuer disputes a statement | Claim identifiers and signature; no audit log is provided |
| Information disclosure | Sensitive assertion printed or retained | Synthetic examples only; consumers must set privacy and retention controls |
| Denial of service | Large fan-out or slow verifier | Semaphore backpressure and shared deadline; caller must bound batch size |
| Elevation of privilege | Ambient trust capability used silently | Dependencies are injected; callers must protect their composition root |

## Security notes

Use synthetic data in examples and tests. Do not embed credentials or private keys. Validate issuer/key provenance and revocation freshness outside this library. A timeout means unknown, not invalid. Report vulnerabilities privately according to [SECURITY.md](../SECURITY.md).
