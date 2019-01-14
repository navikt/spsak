FROM navikt/java:11-appdynamics

RUN mkdir lib
RUN mkdir conf

COPY appdynamics.sh /init-scripts/10-appdynamics.sh

# Config
COPY web/webapp/target/classes/logback.xml conf/
COPY web/webapp/target/classes/jetty/jaspi-conf.xml conf/

# Application Container (Jetty)
COPY web/webapp/target/app.jar .
COPY web/webapp/target/lib/*.jar lib/

# Application Start Command
COPY run-java.sh /
RUN chmod +x /run-java.sh
