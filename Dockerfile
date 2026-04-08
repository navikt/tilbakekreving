FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-26

WORKDIR /tilbakekreving

COPY build/install/tilbakekreving/lib/ lib/

ENTRYPOINT ["java", "-cp", "/tilbakekreving/lib/*", "no.nav.tilbakekreving.ApplicationKt"]

CMD ["-Dlogback.configurationFile=logback-app.xml"]
