FROM gcr.io/distroless/java21-debian12:nonroot

WORKDIR /tilbakekreving

COPY build/install/tilbakekreving/lib/ lib/

ENTRYPOINT ["java"]

CMD ["-cp", "/tilbakekreving/lib/*", "no.nav.tilbakekreving.ApplicationKt"]
