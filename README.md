# Tilbakekreving

En Kotlin/Ktor-applikasjon for håndtering av tilbakekrevingsoperasjoner gjennom integrasjon med
Skatteetatens API.

## Prosjektoversikt

Tilbakekreving er en tjeneste som tilrettelegger for kommunikasjon med Skatteetatens API for tilbakekrevingsoperasjoner.
Den
tilbyr endepunkter for å hente kravdetaljer og kravoversikter.

## Prosjektstruktur

Prosjektet følger en standard Kotlin/Ktor-applikasjonsstruktur:

- `src/main/kotlin/no/nav/tilbakekreving/` - Hovedapplikasjonskode
  - `Application.kt` - Inngangspunkt for applikasjonen
  - `infrastructure/` - Infrastrukturkomponenter (ruter, klienter)
  - `domain/` - Domenemodeller
  - `util/` - Hjelpeklasser
- `src/main/resources/` - Konfigurasjonsfiler
  - `application.conf` - HOCON-konfigurasjon for applikasjonen
- `src/test/kotlin/no/nav/tilbakekreving/` - Testkode
  - `infrastructure/` - Tester for infrastrukturkomponenter
  - `util/` - Tester for hjelpeklasser

## API-endepunkter

Applikasjonen eksponerer følgende endepunkter:

- `/internal/kravdetaljer` - For å hente detaljert informasjon om krav
- `/internal/kravoversikt` - For å hente en oversikt over krav

## Bygge- og konfigurasjonsanvisninger

### Forutsetninger

- JDK 21
- Gradle (wrapper inkludert)

### Bygge prosjektet

```bash
# Bygg prosjektet
./gradlew build

# Kjør tester
./gradlew test

# Lag distribusjon
./gradlew installDist
```

### Miljøvariabler

Applikasjonen krever følgende miljøvariabler:

- `NAIS_CLUSTER_NAME` - NAIS-klusternavnet (brukes for å bestemme miljø)
- `NAIS_TOKEN_ENDPOINT` - NAIS-token-endepunktet for autentisering

Miljøvariabler leses av Hoplite fra konfigurasjonsfilene (application.conf). Når du kjører applikasjonen
lokalt, trenger du ikke å sette disse miljøvariablene. I stedet kan du legge til verdiene direkte i
konfigurasjonsfilene:

- For lokal utvikling: Verdier spesifiseres direkte i `application-local.conf`
- For utviklingsmiljø: Miljøvariabler må settes i kjøretidsmiljøet
- For produksjon: Miljøvariabler må settes i kjøretidsmiljøet

### Docker

Applikasjonen er containerisert ved hjelp av et distroless Java 21-image. Dockerfile er konfigurert til å bruke utdataen
fra
`installDist` Gradle-oppgaven.

For å bygge Docker-image lokalt:

```bash
./gradlew installDist
docker build -t tilbakekreving .
```

## Testinformasjon

### Testrammeverk

Prosjektet bruker følgende testrammeverk:

- **Kotest** - Hovedtestrammeverk med WordSpec-stil
- **Mockk** - Bibliotek for mocking
- **Ktor Test** - Verktøy for testing av Ktor-applikasjoner
- **Arrow Test** - Påstander for Arrow funksjonelle typer

### Kjøre tester

```bash
# Kjør alle tester
./gradlew test

# Kjør en spesifikk testklasse
./gradlew test --tests no.nav.tilbakekreving.infrastructure.route.HentKravdetaljerTest

# Kjør tester med kontinuerlig bygging
./gradlew test --continuous
```

### Skrive tester

Kotest støtter flere teststiler. Her er et eksempel med FunSpec:

```kotlin
// Eksempel på testklasse med Kotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ExampleTest : FunSpec({
  test("skal returnere forventet verdi") {
    val result = someFunction()
    result shouldBe expectedValue
  }

  test("skal håndtere feiltilfeller") {
    val result = someFunctionWithError()
    result shouldBe expectedErrorValue
  }
})
```

### Testing av HTTP-endepunkter

For testing av HTTP-endepunkter, bruk `specWideTestApplication`-verktøyet:

```kotlin
// Eksempel på oppsett for testing av HTTP-endepunkt
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

// Deretter bruk klienten til å sende forespørsler
client.post("/your-endpoint") {
  contentType(ContentType.Application.Json)
  setBody("""{"key": "value"}""")
}.shouldBeOK()
```

## Kodestil og utviklingsretningslinjer

### Funksjonell programmering

Prosjektet bruker Arrow for funksjonelle programmeringsmønstre:

- Bruk `Either<L, R>` for operasjoner som kan feile
- Returner `left()` for feil og `right()` for vellykkede resultater
- Bruk Arrow-operatorer for å jobbe med funksjonelle typer

### Feilhåndtering

- Bruk forseglede klasser (sealed classes) for feiltyper
- Returner feil som `Either.Left`-verdier
- Håndter alle mulige feiltilfeller

### Testing

- Skriv tester for alle offentlige funksjoner
- Mock eksterne avhengigheter
- Bruk beskrivende testnavn som forklarer oppførselen som testes

### Dokumentasjon

- Bruk KDoc-kommentarer for offentlige API-er
- Dokumenter parametere og returverdier
- Gi eksempler for komplekse funksjoner

### Kotlin-stil

- Følg [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Bruk uttrykkskropper for enkle funksjoner
- Foretrekk uforanderlighet (val over var)
- Bruk dataklasser for modeller

## Avhengighetshåndtering

Dette prosjektet bruker [Dependabot](https://docs.github.com/en/code-security/dependabot) for å holde avhengigheter
oppdatert.
Dependabot er konfigurert til å:

- Sjekke ukentlig for oppdateringer til Gradle-avhengigheter, GitHub Actions og Docker
- Automatisk opprette pull-forespørsler for oppdateringer
- Automatisk flette ikke-hovedversjonsoppdateringer for direkte avhengigheter etter at alle kontroller er bestått

## GitHub Actions

Prosjektet bruker GitHub Actions for CI/CD-pipelines:

- Alle arbeidsflyter bruker den spesifikke runner-versjonen `ubuntu-24.04` i stedet for `latest` for å sikre konsistens
- Følgende arbeidsflyter er definert:
  - **build.yaml**: Gjenbrukbar arbeidsflyt for bygging og testing av applikasjonen
  - **deploy.yaml**: Gjenbrukbar arbeidsflyt for distribusjon av applikasjonen til NAIS
  - **tilbakekreving.yaml**: Hoved-CI/CD-pipeline som bruker de gjenbrukbare arbeidsflyterne
  - **dependabot-auto-merge.yml**: Fletter automatisk ikke-hovedversjonsoppdateringer fra Dependabot

## Teknologier

- **Kotlin** 2.1.10 - Programmeringsspråk
- **Ktor** 3.1.2 - Webrammeverk
- **Arrow** 2.0.1 - Bibliotek for funksjonell programmering
- **Hoplite** 2.9.0 - Konfigurasjonsbibliotek
- **Kotest** 6.0.0.M2 - Testrammeverk
- **Mockk** 1.13.17 - Mockingbibliotek
