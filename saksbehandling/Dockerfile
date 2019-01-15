FROM navikt/java:11-appdynamics

RUN mkdir lib
RUN mkdir webapp
RUN mkdir conf

# Config
COPY webapp/target/classes/logback.xml conf/
COPY webapp/target/classes/jetty/jaspi-conf.xml conf/

# Application Container (Jetty)
COPY webapp/target/app.jar .
COPY webapp/target/lib/*.jar lib/

# Application Start Command
COPY run-java.sh /
RUN chmod +x /run-java.sh
