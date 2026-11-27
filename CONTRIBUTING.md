# Contributing

## Build and test

- Install a JDK 21 or newer.
- Run `./gradlew build` for compilation, tests, Javadoc, and coverage verification.
- Run `./gradlew buildSite` to generate the static site under `build/site` (Node.js and `npx` are used to render Markdown).

## Style

Use Java records and sealed outcomes where they fit the model. Keep core dependencies at `java.base`, inject capabilities, use deterministic clocks in tests, and keep all sample data synthetic. Preserve the no-scalar ACE-U profile API.

## First 30 minutes

1. Read [architecture](docs/ARCHITECTURE.md) and [design decisions](docs/DESIGN.md).
2. Run the build and inspect JaCoCo output.
3. Pick a scoped issue, add tests, then implement the change.
4. Update relevant docs and open a pull request with behavior and test notes.

Please follow the [Code of Conduct](CODE_OF_CONDUCT.md).
