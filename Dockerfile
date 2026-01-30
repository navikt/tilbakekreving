FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25

WORKDIR /tilbakekreving

# For swagger-genererte filer
RUN mkdir docs && chmod 777 docs

COPY build/install/tilbakekreving/lib/ lib/

ENTRYPOINT ["java"]

CMD ["-cp", "/tilbakekreving/lib/*", "no.nav.tilbakekreving.ApplicationKt"]
