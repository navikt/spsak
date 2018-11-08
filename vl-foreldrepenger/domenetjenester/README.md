# Domenetjenester 

Modulene under denne er tjenester som har domene logikk (tilsv. DDD Domain Services) som ikke passer inn i domenemodellen.
(Domenemodellen i DDD består av Aggregates, Entitties og Value Objects).

Tjenestene her kan kalles fra Applikasjonstjenester (inklusiv web laget) og kan kalle på Repositories (for lagring etc.) eller infrastrukturtjenester for kall mot andre systemer.
De skal ikke håndtere lagring, eksterne kall eller transformasjon av eksterne formater selv, en delegere dette til henholdsvis Repositories eller infrastruktutjenester.



