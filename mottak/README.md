# FPFORDEL
---


### Kjøre applikasjonen på Docker
Tar utgangspunkt i denne guiden: 
https://confluence.adeo.no/pages/viewpage.action?pageId=280272665

#### Truststore
Jeg henter bare denne rett fra Fasit i t10 og lagrer den på hjemmeområdet mitt

```bash
wget -O ~/truststore/truststore.jts https://fasit.adeo.no/api/v2/resources/3816117/file/keystore
```

#### Håndtere Secrets
Kan legges i egen env-fil feks ved å kopiere `fpfordel-env.list` til en lokal fil som
legges i `.gitignore`, feks `fordel-env.local`.

```
NAV_TRUSTSTORE_PASSWORD=****
OPENIDCONNECT_PASSWORD=****
SYSTEMBRUKER_PASSWORD=****
```

Eller settes direkte i run configuration:

```bash
docker run -d \
    -v ~/truststore/:/truststore/ \
    --env-file ./fpfordel-env.list \
    -e NAV_TRUSTSTORE_PASSWORD=***** \
    -e OPENIDCONNECT_PASSWORD=***** \
    -e SYSTEMBRUKER_PASSWORD=***** \
    --net host \
    --name fpfordel \
    -p 8090:8090 \
     docker.adeo.no:5000/fpfordel:2.0_20180821151221_f00277c
```

#### Øvrige environment Variabler
I repo ligger det en `fpfordel-env.list`, denne er hentet fra fasit via nais-cli og tilpasset med lokale
overrides. Komplett liste over overrides. Disse er hentet fra `es-dev.properties` fila.

```
APP_NAME=fpfordel
DEFAULTDS_PASSWORD=fpfordel
DEFAULTDS_URL=jdbc:oracle:thin:@localhost:1521:XE
DEFAULTDS_USERNAME=fpfordel
ENVIRONMENT_NAME=devimg
FASIT_ENVIRONMENT_NAME=local
FORELDREPENGER_STARTDATO=2017-01-01
FP_STATUSINFORMASJON_URL=http://localhost:8080/fpsak/api/dokumentforsendelse/status
FPSAK_JOURNALPOSTKNYTTNING_URL=http://localhost:8080/fpsak/api/fordel/fagsak/knyttJournalpost
FPSAK_MOTTAJOURNALPOST_URL=HTTP://localhost:8080/fpsak/api/fordel/journalpost
FPSAK_OPPRETT_SAK_URL=http://localhost:8080/fpsak/api/fordel/fagsak/opprett
FPSAK_SAKSINFORMASJON_URL=http://localhost:8080/fpsak/api/fordel/fagsak/informasjon
FPSAK_VURDERFAGSYSTEM_URL=http://localhost:8080/fpsak/api/fordel/vurderFagsystem
LOADBALANCER_FQDN=localhost:8090
LOGBACK_CONFIG=./conf/logback-dev.xml
MOTTAK_QUEUE_QUEUEMANAGER=mq://e26apvl121.test.local:1411/MUXLSC01
MOTTAK_QUEUE_QUEUENAME=QA.U87_FPSAK_SSL.MOTTAK_QUEUE
MQGATEWAY02_CHANNEL=U87_FPSAK_SSL
MQGATEWAY02_HOSTNAME=e26apvl121.test.local
MQGATEWAY02_NAME=MUXLSC01
MQGATEWAY02_PORT=1411
MQGATEWAY02_USESSLONJETTY=true
NAV_TRUSTSTORE_PATH=/truststore/truststore.jts
OPENIDCONNECT_USERNAME=fpfordel-localhost
PROXY_URL=http://webproxy-utvikler.nav.no:8088
SERVER_PORT=8090
```
