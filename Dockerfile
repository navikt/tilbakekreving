FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25-dev AS build

USER root

# Mappe for genererte OpenAPI- og Swagger-filer
RUN mkdir /prep/docs

FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25

WORKDIR /tilbakekreving

COPY --from=build --chown=65532:65532 /prep/docs /tilbakekreving/docs

COPY build/install/tilbakekreving/lib/ lib/

ENTRYPOINT ["java"]

CMD ["-cp", "/tilbakekreving/lib/*", "no.nav.tilbakekreving.ApplicationKt"]
