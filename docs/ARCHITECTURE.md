# Architecture

The named module `io.github.sanandani.trustsignal` exports model, verification, scoring, and example packages. Core module metadata requires only `java.base`.

## Layers

- **model**: immutable claims, defensive signature bytes, sealed verification outcomes, and exactly four ACE-U scores. No dependency on other project packages.
- **verify**: ports for signature checks, issuer trust, revocation, and time; the JDK Ed25519 adapter; bounded virtual-thread fan-out.
- **score**: configurable dimension weights, evidence-aware aggregation per dimension, explanation strings, and a separate invalid-evidence record.
- **example**: synthetic composition root showing caller configuration.

## Composition root

The application supplies a `Clock`, key material, trusted issuer policy, revocation checker, and scorer weights. `SignalVerifier.composed` orders those checks. `ParallelVerifier` shares one monotonic deadline across the batch and limits active work with a semaphore. A timeout is represented as `Unverifiable(TIMEOUT)`.

Scoring verified evidence uses full weight. Inferred evidence uses its confidence as a discount. Unverifiable evidence cannot move a dimension's value; it is called out in the explanation and does not contribute confidence. Invalid evidence is excluded and listed in `Result.excluded`.
