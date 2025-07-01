# Tilbakekreving Project Guidelines

This document provides essential information for developers working on the Tilbakekreving project.

## Project Structure

The project follows a standard Kotlin/Ktor application structure:

- `src/main/kotlin/no/nav/tilbakekreving/` - Main application code
  - `Application.kt` - Entry point for the application
  - `infrastructure/` - Infrastructure components (routes, clients)
  - `domain/` - Domain models
  - `util/` - Utility classes
- `src/main/resources/` - Configuration files
  - `application.conf` - HOCON configuration for the application
- `src/test/kotlin/no/nav/tilbakekreving/` - Test code
  - `infrastructure/` - Tests for infrastructure components
  - `util/` - Tests for utility classes

## Build and Run

### Prerequisites

- JDK 21
- Gradle (wrapper included)

### Building and Testing

```bash
# Build the project
./gradlew build

# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests no.nav.tilbakekreving.infrastructure.route.HentKravdetaljerTest

# Run tests with continuous build
./gradlew test --continuous

# Create distribution
./gradlew installDist
```

### Environment Configuration

The application uses Hoplite to read configuration from `application.conf` files:

- **Local development**: Values are specified in `application-local.conf`
- **Development/Production**: The following environment variables must be set:
  - `NAIS_CLUSTER_NAME` - The NAIS cluster name (used to determine environment)
  - `NAIS_TOKEN_ENDPOINT` - The NAIS token endpoint for authentication

### Docker

The application is containerized using a distroless Java 21 image:

```bash
./gradlew installDist
docker build -t tilbakekreving .
```

## Testing Guidelines

### Test Framework

The project uses the following testing frameworks:

- **Kotest** - Main testing framework with WordSpec style
- **Mockk** - Mocking library
- **Ktor Test** - Utilities for testing Ktor applications
- **Arrow Test** - Assertions for Arrow functional types

### Writing Tests

Tests should follow the WordSpec style from Kotest. Here's an example:

```kotlin
class HentKravdetaljerTest : WordSpec({
    val hentKravdetaljer = mockk<HentKravdetaljer>()
    val client = specWideTestApplication {
        application {
            configureSerialization()
            routing {
                route("/kravdetaljer") {
                    hentKravdetaljerRoute(hentKravdetaljer)
                }
            }
        }
    }.client

    val kravidentifikator = Kravidentifikator.Nav("123456789")

    "hent kravdetaljer" should {
        "returnere 200 med kravdetaljer" {
            coEvery { hentKravdetaljer.hentKravdetaljer(kravidentifikator) } returns kravdetaljer.right()

            client.post("/kravdetaljer") {
                contentType(ContentType.Application.Json)
                setBody("""{"id": "${kravidentifikator.id}", "type": "NAV"}""")
            }.shouldBeOK()
        }
    }
})
```

For HTTP endpoint tests, use the `specWideTestApplication` utility as shown above.

## Development Guidelines

### Functional Programming

The project uses Arrow for functional programming patterns:

- Use `Either<L, R>` for operations that can fail
- Return `left()` for errors and `right()` for successful results
- Use Arrow operators for working with functional types

### Error Handling

- Use sealed classes for error types
- Return errors as `Either.Left` values
- Handle all possible error cases

### Code Quality

- **Testing**: Write tests for all public functions and mock external dependencies
- **Documentation**: Use KDoc comments for public APIs with parameters and return values
- **Kotlin Style**:
  - Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
  - Use expression bodies for simple functions
  - Prefer immutability (val over var)
  - Use data classes for models

### CI/CD

The project uses GitHub Actions for CI/CD pipelines:

- All workflows use `ubuntu-24.04` runner version for consistency
- Workflows:
  - **build.yaml**: Builds and tests the application
  - **deploy.yaml**: Deploys the application to NAIS
  - **tilbakekreving.yaml**: Main CI/CD pipeline
  - **dependabot-auto-merge.yml**: Auto-merges non-major Dependabot updates
