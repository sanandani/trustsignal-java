# ADR 0001: Keep ACE-U multidimensional

- Status: Accepted
- Decision: Represent values and confidence independently for Authenticity, Credibility, Empathy, and Uniqueness. Do not provide a scalar/total/overall API.
- Rationale: A weighted scalar hides trade-offs and implies unsupported comparability. Consumers can inspect each dimension and its explanation.
- Consequence: Callers must make their own context-specific decisions without a library-level ranking.
