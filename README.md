# Tilbakekreving

Kotlin/Ktor-tjeneste som proxyer NAV-forespørsler til Skatteetatens innkrevingsoppdrag-API med autentisering,
tilgangskontroll og auditlogging.


## API-endepunkter

- `POST /internal/kravdetaljer` – Hent detaljert informasjon om krav
- `POST /internal/kravoversikt` – Hent oversikt over krav for en skyldner (person/organisasjon)
- `GET /swagger` – OpenAPI/Swagger UI

## OpenAPI/Swagger UI

OpenAPI-dokumentasjon produseres automatisk ved oppstart av applikasjonen og er avhengig av code inference
i rutene. [Se dokumentasjon](https://ktor.io/docs/openapi-spec-generation.html#code-inference) for detaljer
rundt hvordan OpenAPI-specen genereres.

Swagger er tilgjengelig under `/swagger`.

## Kodegenerering

Hvilke kravtyper og hvilke kravtyper en enhet har tilgang til blir for øyeblikket vedlikeholdt i et regneark.
Dette regnearket brukes som kilde for å generer opp kravtypene som en enum, samt en mapping over hvilke
kravtyper en enhet har tilgang til.

Scriptene for kodegenereringen ligger under [/buildSrc](/buildSrc).

## CI/CD

Main-branchen deployes direkte til dev-miljøet når man pusher til GitHub. For å deploye til produksjon må
man godkjenne deployen i tilhørende Action for committen på GitHub.

## Bygg og test

Krever JDK 21+.

```bash
./gradlew build                # Bygg + test + lint
./gradlew test                 # Kjør alle tester
./gradlew test --tests '*.HentKravdetaljerTest'  # Enkelt testklasse
./gradlew test --continuous    # Watch mode
./gradlew ktlintCheck          # Lint
./gradlew ktlintFormat         # Auto-fiks lint
./gradlew installDist          # Lag distribusjon for Docker
./gradlew generateAll          # Kodegenerering for kravtyper og mapping av enheter til kravtyper
```

## Konfigurasjon

Hoplite laster HOCON-filer (`application.conf` → `application-{env}.conf`). Miljø bestemmes av
`NAIS_CLUSTER_NAME` → `AppEnv` (LOCAL/DEV/PROD). For lokal utvikling, sett verdier direkte i
`application-local.conf`.

## Docker

```bash
./gradlew installDist && docker build -t tilbakekreving .
```

## GitHub Actions

- **tilbakekreving.yaml**: Hoved-CI/CD-pipeline
- **build.yaml** / **deploy.yaml**: Gjenbrukbare arbeidsflyter for bygg og deploy til NAIS
- **dependabot-auto-merge.yml**: Automatisk fletting av Dependabot-oppdateringer
- **sync-labels.yml**: Synkroniserer GitHub-labels fra `.github/labels.yml`

## Prosjektstruktur

- `infrastructure/` – HTTP-klienter, ruter, auth og auditlogging
- `app/` – Forretningslogikk-interfaces
- `domain/` – Domenemodeller
- `config/` – Konfigurasjon
- `setup/` – Bootstrapping
- `plugin/` – Ktor-plugins
