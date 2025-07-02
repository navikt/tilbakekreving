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

## Build and Configuration Instructions

### Prerequisites

- JDK 21
- Gradle (wrapper included)

### Building the Project

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Create distribution
./gradlew installDist
```

### Environment Variables

The application requires the following environment variables:

- `NAIS_CLUSTER_NAME` - The NAIS cluster name (used to determine environment)
- `NAIS_TOKEN_ENDPOINT` - The NAIS token endpoint for authentication

Environment variables are read by Hoplite from the configuration files (application.conf). When running the application locally, you do not need to set these environment variables. Instead, you can add the values directly to the configuration files:

- For local development: Values are specified directly in `application-local.conf`
- For development environment: Environment variables must be set in the runtime environment.
- For production: Environment variables must be set in the runtime environment

### Docker

The application is containerized using a distroless Java 21 image. The Dockerfile is configured to use the output of the `installDist` Gradle task.

To build the Docker image locally:

```bash
./gradlew installDist
docker build -t tilbakekreving .
```

## Testing Information

### Test Framework

The project uses the following testing frameworks:

- **Kotest** - Main testing framework with WordSpec style
- **Mockk** - Mocking library
- **Ktor Test** - Utilities for testing Ktor applications
- **Arrow Test** - Assertions for Arrow functional types

### Running Tests

```bash
# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests no.nav.tilbakekreving.infrastructure.route.HentKravdetaljerTest

# Run tests with continuous build
./gradlew test --continuous
```

### Writing Tests

Tests should follow the WordSpec style from Kotest. Here's an example:

```
// Example test class
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

### Testing HTTP Endpoints

For testing HTTP endpoints, use the `specWideTestApplication` utility:

```
// Example HTTP endpoint test setup
val client = specWideTestApplication {
    application {
        configureSerialization()
        routing {
            route("/your-endpoint") {
                yourEndpointRoute(mockDependency)
            }
        }
    }
}.client

// Then use the client to make requests
client.post("/your-endpoint") {
    contentType(ContentType.Application.Json)
    setBody("""{"key": "value"}""")
}.shouldBeOK()
```

## Code Style and Development Guidelines

### Functional Programming

The project uses Arrow for functional programming patterns:

- Use `Either<L, R>` for operations that can fail
- Return `left()` for errors and `right()` for successful results
- Use the Arrow operators for working with functional types

### Error Handling

- Use sealed classes for error types
- Return errors as `Either.Left` values
- Handle all possible error cases

### Testing

- Write tests for all public functions
- Mock external dependencies
- Use descriptive test names that explain the behavior being tested

### Documentation

- Use KDoc comments for public APIs
- Document parameters and return values
- Provide examples for complex functions

### Kotlin Style

- Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use expression bodies for simple functions
- Prefer immutability (val over var)
- Use data classes for models

### GitHub Actions

The project uses GitHub Actions for CI/CD pipelines:

- All workflows use the specific runner version `ubuntu-24.04` instead of `latest` to ensure consistency
- The following workflows are defined:
  - **build.yaml**: Reusable workflow for building and testing the application
  - **deploy.yaml**: Reusable workflow for deploying the application to NAIS
  - **tilbakekreving.yaml**: Main CI/CD pipeline that uses the reusable workflows
  - **dependabot-auto-merge.yml**: Automatically merges non-major version updates from Dependabot
