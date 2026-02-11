# Tilbakekreving

Kotlin/Ktor-tjeneste som proxyer NAV-forespørsler til Skatteetatens innkrevingsoppdrag-API med autentisering,
tilgangskontroll og auditlogging.

## Prosjektstruktur

- `infrastructure/` – HTTP-klienter, ruter, auth og auditlogging
- `app/` – Forretningslogikk-interfaces (`HentKravdetaljer`, `SøkEtterInnkrevingskrav`)
- `domain/` – Domenemodeller (`Krav`, `Kravdetaljer`, `Skyldner` m.m.)
- `config/` – Konfigurasjon (Hoplite)
- `setup/` – Bootstrapping (konfig, auth, HTTP-klienter)
- `plugin/` – Ktor-plugins (Maskinporten auth)

## API-endepunkter

- `POST /internal/kravdetaljer` – Hent detaljert informasjon om krav
- `POST /internal/kravoversikt` – Hent oversikt over krav
- `GET /internal/isAlive` – Liveness-probe
- `GET /internal/isReady` – Readiness-probe
- `GET /swagger` – OpenAPI/Swagger UI

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
```

## Konfigurasjon

Hoplite laster HOCON-filer (`application.conf` → `application-{env}.conf`). Miljø bestemmes av
`NAIS_CLUSTER_NAME` → `AppEnv` (LOCAL/DEV/PROD). For lokal utvikling, sett verdier direkte i
`application-local.conf`.

## Docker

```bash
./gradlew installDist && docker build -t tilbakekreving .
```

## Avhengighetshåndtering

[Dependabot](https://docs.github.com/en/code-security/dependabot) sjekker ukentlig for oppdateringer
til Gradle-avhengigheter, GitHub Actions og Docker. Ikke-hovedversjonsoppdateringer flettes automatisk.

## GitHub Actions

- **build.yaml** / **deploy.yaml**: Gjenbrukbare arbeidsflyter for bygg og deploy til NAIS
- **tilbakekreving.yaml**: Hoved-CI/CD-pipeline
- **dependabot-auto-merge.yml**: Automatisk fletting av Dependabot-oppdateringer
- **sync-labels.yml**: Synkroniserer GitHub-labels fra `.github/labels.yml`
