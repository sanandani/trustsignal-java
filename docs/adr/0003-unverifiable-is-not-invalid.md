# ADR 0003: Preserve unknown evidence state

- Status: Accepted
- Decision: Model missing, expired, unknown issuer, and timeout as `Unverifiable`; reserve `Invalid` for bad signatures and revoked claims.
- Rationale: Inability to establish validity is distinct from evidence of invalidity.
- Consequence: Unverifiable evidence reduces confidence without changing dimension value.
