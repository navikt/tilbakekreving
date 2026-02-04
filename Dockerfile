FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25

WORKDIR /tilbakekreving

COPY build/install/tilbakekreving/lib/ lib/

ENTRYPOINT ["java"]

CMD ["-cp", "/tilbakekreving/lib/*", "no.nav.tilbakekreving.ApplicationKt"]
