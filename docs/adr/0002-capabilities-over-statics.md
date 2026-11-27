# ADR 0002: Inject capabilities instead of ambient statics

- Status: Accepted
- Decision: Inject clock, issuer policy, signature verification, and revocation checks at construction/composition boundaries.
- Rationale: Explicit dependencies make trust decisions reviewable and tests deterministic.
- Consequence: Applications own capability lifecycle and secure composition.
