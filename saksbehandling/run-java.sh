#!/usr/bin/env sh
set -e

exec java -cp app.jar:lib/* \
    ${DEFAULT_JAVA_OPTS} \
    ${JAVA_OPTS} \
    -Xmx1024m -Xms128m \
    -Djava.security.egd=file:/dev/urandom \
    -Dlogback.configurationFile=conf/logback.xml \
    -Dconf="conf" \
    -Dwebapp="webapp" \
    -Dklient="klient" \
    -Di18n="i18n" \
    -Dapplication.name=spsak no.nav.foreldrepenger.web.server.jetty.JettyServer $@
