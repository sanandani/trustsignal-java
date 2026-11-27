# Design and research mapping

This project translates concepts from S. Anandani, “Trust-Aware Professional Identity Representation in AI-Mediated Systems,” IEEE SoutheastCon 2026, doi:10.1109/SoutheastCon63549.2026.11476584, and Anandani & Aldrich's research manuscript on Wyvern capability-safe architecture into a small Java reference design.

| Concept | Implementation choice | Boundary |
|---|---|---|
| Verifiable identity assertions | `Claim`, `SignedClaim`, Ed25519 adapter | The caller controls canonicalization and key lifecycle |
| ACE-U | Four independent `DimensionScore` values | Weights and evidence are domain-specific |
| Capability-safe construction | Injected `Clock`, issuer, signature, and revocation ports | No ambient trust store is assumed |
| Partial knowledge | `Unverifiable` distinct from `Invalid` | Unknown is not treated as false |
| Human-readable explanation | Per-dimension contribution notes and excluded reasons | Explanations do not prove evidence correctness |

The framework is conceptual and has not been empirically validated. It is not a validated psychometric instrument, identity standard, or decision system. No venue is asserted for the Wyvern research manuscript.
