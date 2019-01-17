FROM navikt/java:11-appdynamics

RUN mkdir lib
RUN mkdir webapp
RUN mkdir conf

# Config
COPY webapp/target/classes/logback.xml conf/
COPY webapp/target/classes/jetty/jaspi-conf.xml conf/

# Application Container (Jetty)
COPY webapp/target/app.jar .
COPY webapp/target/lib/*.jar ./

ENV JAVA_OPTS="-Xmx1024m -Xms128m \
    -Djava.security.egd=file:/dev/urandom \
    -Dlogback.configurationFile=conf/logback.xml"
