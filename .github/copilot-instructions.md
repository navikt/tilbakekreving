# Copilot Instructions – Tilbakekreving

## Build & Test

```bash
./gradlew build                # Build + test + lint
./gradlew test                 # Run all tests
./gradlew test --tests '*.HentKravdetaljerTest'  # Single test class
./gradlew test --continuous    # Watch mode
./gradlew ktlintCheck          # Lint all Kotlin sources
./gradlew ktlintFormat         # Auto-fix lint issues
./gradlew installDist          # Create distribution for Docker
```

Requires JDK 21+. Linting uses ktlint via the `org.jlleitschuh.gradle.ktlint` Gradle plugin (pinned to ktlint 1.8.0 for
context parameters support). `ktlintCheck` runs automatically as part of `build`.

## Architecture

Kotlin/Ktor service that proxies NAV requests to Skatteetaten's innkrevingsoppdrag API with authentication, access
control, and audit logging.

### Layers

- **`infrastructure/`** – HTTP clients, routes, auth, and audit logging (external boundary)
- **`app/`** – Business logic interfaces (`HentKravdetaljer`, `SøkEtterInnkrevingskrav`) that define operations and
  their error types
- **`domain/`** – Pure data classes (`Krav`, `Kravdetaljer`, `Skyldner`, etc.)
- **`config/`** – Configuration data classes loaded by Hoplite
- **`setup/`** – App bootstrapping (config loading, auth setup, HTTP client creation)
- **`plugin/`** – Ktor plugins (Maskinporten auth header injection)

### Authentication (two-part)

1. **Inbound**: Bearer tokens from Entra ID verified via NAIS Texas sidecar (`TexasClient` →
   `NAIS_TOKEN_INTROSPECTION_ENDPOINT`). Extracts `navIdent` + `groupIds` into `NavUserPrincipal`.
2. **Outbound**: `MaskinportenAuthHeaderPlugin` intercepts all Skatteetaten requests, fetches Maskinporten token via
   `TexasMaskinportenClient`, and injects the `Authorization` header.

### Configuration

Hoplite loads HOCON files (`application.conf` → `application-{env}.conf`) into `TilbakekrevingConfig`. Environment is
determined by `NAIS_CLUSTER_NAME` → `AppEnv` (LOCAL/DEV/PROD). For local development, set values directly in
`application-local.conf`.

### Skatteetaten Integration

`SkatteetatenInnkrevingsoppdragHttpClient` implements both `app/` interfaces. It POSTs to Skatteetaten endpoints, sends
`Klientid: NAV/2.0` header, and maps HTTP errors to sealed error types. OpenAPI schemas for request/response are stored
in `skatteetaten/`.

## Key Conventions

### Error Handling with Arrow Either

All fallible operations return `Either<ErrorType, SuccessType>`. Use Arrow's `either { }` builder with `raise()` for
short-circuiting errors. Each operation defines its own sealed error hierarchy (e.g.,
`HentKravdetaljer.HentKravdetaljerFeil`). In routes, use `.getOrElse { error -> when(error) { ... } }` for exhaustive
error handling.

### Kotlin Context Parameters

The codebase uses Kotlin context parameters (`context(...)`) for dependency injection rather than constructor injection
or DI frameworks.

### Type Safety with Inline Value Classes

Wrapper types like `GroupId` and `AuthenticationConfigName` are inline value classes to prevent mixing up string/ID
parameters.

### Testing Patterns

- **Framework**: Kotest WordSpec style with `shouldBe`, `shouldEqualJson` matchers
- **HTTP tests**: Use `specWideTestApplication` to set up Ktor test server with mocked dependencies
- **Client tests**: Use Ktor `MockEngine` to simulate external HTTP responses
- **Auth in tests**: Inject bearer tokens via test client; mock `AccessTokenVerifier`
- **Arrow assertions**: Use `kotest-assertions-arrow` for `Either` assertions

### Access Control (ABAC DSL)

Access control uses a custom attribute-based access control (ABAC) DSL defined in `infrastructure/auth/abac/`. Policies
are built with `accessPolicy<Subject, Resource> { }` using composable rules:

- `require { }` — all must pass for access to be granted
- `deny { }` — if any matches, access is denied (checked first)

The krav access policy (`kravAccessPolicy()`) gates access to krav resources based on user group membership. It is
injected via context parameters and used in routes to filter krav lists. The enhet-based kravtype filtering rule is
designed and tested but currently disabled pending kravtype-to-enhet mapping.

### Routes

All business routes are under `/internal/` and require authentication. The kravoversikt route applies access control via
`AccessPolicy.filter()`. Health checks at `/internal/isAlive` and `/internal/isReady`.

### Serialization

Kotlinx Serialization with `ignoreUnknownKeys = true`. Domain models use `@Serializable` annotation.
