FROM navikt/java:10

RUN mkdir /app/lib
RUN mkdir /app/webapp
RUN mkdir /app/conf

# AppDynamics config
RUN mkdir -p /app/appdynamics \
    && wget -O /app/appdynamics/appdynamicsagent.zip http://maven.adeo.no/nexus/service/local/repositories/m2internal/content/com/appdynamics/appdynamics-appagent/4.5.1/appdynamics-appagent-4.5.1.zip \
    && unzip /app/appdynamics/appdynamicsagent.zip -d /app/appdynamics

COPY appdynamics.sh /init-scripts/

# Config
COPY web/webapp/target/classes/logback.xml /app/conf/
COPY web/webapp/target/classes/jetty/jaspi-conf.xml /app/conf/

# Application Container (Jetty)
COPY web/webapp/target/app.jar /app/
COPY web/webapp/target/lib/*.jar /app/lib/

# Application
# TODO (u139158): PFP-1176 Fikse opp i klient-prosjektet. webpack.prod.js: path: path.resolve(__dirname, '../target/klient/public'),?
COPY web/klient/target/public/ /app/klient/public/
COPY web/klient/target/index.html /app/klient/

# Application Start Command
COPY run-java.sh /
RUN chmod +x /run-java.sh
