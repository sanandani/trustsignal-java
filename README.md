# TrustSignal Java

[![CI](https://github.com/sanandani/trustsignal-java/actions/workflows/ci.yml/badge.svg)](https://github.com/sanandani/trustsignal-java/actions/workflows/ci.yml) [![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)

A Java 21+ reference library for verifying professional identity claims and presenting trust signals across four separate dimensions: **Authenticity, Credibility, Empathy, and Uniqueness (ACE-U)**. The framework is a conceptual reference implementation, **not empirically validated** and not a measure of a person's worth or overall trustworthiness.

## Install

The project targets Java 21 and has no runtime dependencies. Clone the repository and build with `./gradlew build`. Maven Central publication is planned; the artifact coordinates are not yet available.

## Quick start (five minutes)

```java
var weights = Map.of(
    Dimension.AUTHENTICITY, 1d, Dimension.CREDIBILITY, 1d,
    Dimension.EMPATHY, 1d, Dimension.UNIQUENESS, 1d);
var scorer = new AceUScorer(weights);
var evidence = new AceUScorer.SignalEvidence(
    "demo-claim", new SignalOutcome.Verified(),
    Map.of(Dimension.AUTHENTICITY, .9, Dimension.CREDIBILITY, .8,
           Dimension.EMPATHY, .7, Dimension.UNIQUENESS, .6),
    "synthetic example evidence");
var result = scorer.score(List.of(evidence));
result.profile().dimensions().forEach((dimension, score) ->
    System.out.println(dimension + ": " + score.value() + " (confidence " + score.confidence() + ")"));
```

Run the composition-root example with `./gradlew run` (or `io.github.sanandani.trustsignal.example.Main` from an IDE). See [API Javadoc](https://sanandani.github.io/trustsignal-java/api/) and [docs](docs/ARCHITECTURE.md).

## Design principles

- Keep four dimensions distinct; there is no scalar, total, or overall profile score.
- Represent evidence state explicitly. Unverifiable evidence reduces confidence without lowering a dimension's value; invalid evidence is excluded and recorded.
- Inject time, issuer trust, revocation, and signature capabilities at construction/composition boundaries.
- Keep the core on `java.base`; use JDK Ed25519 and virtual threads.

## Research mapping

| Research concept | Code representation |
|---|---|
| Trust-aware professional identity | `Claim`, `SignedClaim`, and explicit `SignalOutcome` states |
| ACE-U dimensions | `Dimension`, `DimensionScore`, and `AceUProfile` |
| Capability-safe composition | `SignalVerifier.composed` receives clock and verification capabilities |
| Non-scalar representation | Four-axis profile API, with no aggregate method |

Conceptual sources: S. Anandani, “Trust-Aware Professional Identity Representation in AI-Mediated Systems,” IEEE SoutheastCon 2026, [doi:10.1109/SoutheastCon63549.2026.11476584](https://doi.org/10.1109/SoutheastCon63549.2026.11476584); Anandani & Aldrich, research manuscript on Wyvern capability-safe architecture (manuscript; no venue claimed). These sources motivate a conceptual mapping; they do not establish empirical validity for this software or its scores.

## Limitations and non-goals

This library does not determine identity, truth, hiring suitability, character, or social value. Scores depend on caller-supplied evidence and weights; they are not calibrated probabilities. No production trust registry, revocation-list transport, JSON codec, persistence, or network service is included. Consumers must define consent, provenance, retention, fairness, and review policies for their context.

## Links

- [Repository](https://github.com/sanandani/trustsignal-java)
- [Architecture](docs/ARCHITECTURE.md)
- [Design and research mapping](docs/DESIGN.md)
- [Threat model](docs/THREAT_MODEL.md)
- [Roadmap](docs/ROADMAP.md)
- [API Javadoc](https://sanandani.github.io/trustsignal-java/api/)

Licensed under Apache-2.0. See [CONTRIBUTING.md](CONTRIBUTING.md) and [SECURITY.md](SECURITY.md).
