### Behandlingsprosess :: Prosesstask

Denne modulen implementerer rammeverk for å kjøre og fordele Prosess Tasks. 
Dette er asynkrone bakgrunnsjobber som kjøres fordet utover tilgjengelig maskiner. 
Prosess Tasks kan fordeles i grupper der de kjøres sekvensielt og/eller i parallell. 
Ytterligere dokumentasjon finnes her: [Automasjon](https://confluence.adeo.no/display/SVF/10.5+Tema+-+Automasjon)

Inneholder:
* API
* REST-api
* Tilhørende migreringer for db-ressurser eid av prosesstask-biten  

### Rutiner
##### Migreringer
Denne modulen eier alle db-ressurser den manipulerer. Ved endringer legges nye migreringsskript her, og deretter kopieres manuelt til applikasjonen som avhenger av denne modulen. Applikasjoner kan følge sin egen versjon, men som konvensjon, og for bedre sporbarhet, legg inn git-hash og navn på skriptet i denne modulen i tilhørende migreringsskript i din applikasjon. Grunnen til dette er at aura-pluginet (per 16.11.17) som brukes til migreringer og deploy ute i miljø ikke støtter flere migreringsstier.

###### Eksempel

Si at denne modulen får ny migrering på ``v1.2.3``.
Vi legger til nytt skript under ``migreringer/src/main/resources/db/migration/defaultDS/1.2.3/V1.2.3__0__skript.sql``, og comitter med git-hash `asd3423`.
````
# -- Skript V1.2.3__0__skript.sql
COMMENT ON COLUMN "PROSESS_TASK"."STATUS" IS 'status på task';
COMMENT ON COLUMN "PROSESS_TASK"."TASK_PARAMETERE" IS 'parametere angitt for en task';
COMMENT ON COLUMN "PROSESS_TASK"."TASK_PAYLOAD" IS 'inputdata for en task';
COMMENT ON COLUMN "PROSESS_TASK"."TASK_GRUPPE" IS 'angir en unik id som grupperer flere ';
````

Gitt, for eks. applikasjon fpsak, skal bruke ``v1.2.3`` av denne, må vi manuelt legge inn alle manglende migreringer fram til og med ``v1.2.3``. La oss anta at neste versjon av fpsak blir ``v4.5.6``.

Vi legger til nytt skript i fpsak: ``migreringer/src/main/resources/db/migration/defaultDS/4.5.6/V4.5.6__0__skript.sql``, som er en identisk kopi av ``V1.2.3__0__skript.sql``, med to kommentarer som referer til det originale skriptet ``V1.2.3__0__skript.sql``.  

````
# -- Skript V4.5.6__0__skript.sql
# -- originalt skript fra commit-hash asd3423 fra vl-felles-behandlingsprosess
# -- navn på skriptet: V1.2.3__0__skript.sql 
COMMENT ON COLUMN "PROSESS_TASK"."STATUS" IS 'status på task';
COMMENT ON COLUMN "PROSESS_TASK"."TASK_PARAMETERE" IS 'parametere angitt for en task';
COMMENT ON COLUMN "PROSESS_TASK"."TASK_PAYLOAD" IS 'inputdata for en task';
COMMENT ON COLUMN "PROSESS_TASK"."TASK_GRUPPE" IS 'angir en unik id som grupperer flere ';
````
  
##### Avhengigheten til vl-felles
Skal alltid være ha release-avhengighet til vl-felles, untatt ved feature eller lokalt.

