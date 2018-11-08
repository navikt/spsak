# Dokumentbestiller

Modulen er Vedtaksløsningens proxy mot DokumentProduksjon. Dokumenter forhåndsvises og produseres basert på data som hentes fra Vedtaksløsningens behandlingslager. Visse felter (typisk fritekstfelter) kan også angis av saksbehandler.

Det skilles mellom dokumenter som opprettes av automatisert prosess og dokumenter som opprettes "manuelt" av saksbehandler.

Dokumentbestiller kjenner til et antall dokumentmaler. Hver mal består av en felles-del (kalt Mastermal for Vedtaksløsningen) og en dokumenttype-spesifikk del.

## Hensikten

* Forhåndsvise dokumenter i Vedtaksløsningens arbeidsflate.
* Produsere ikke-redigerbare dokumenter, som DokumentProduksjon sender til Bruker og arkiverer i Arkiv (JOARK) på vegne av Vedtaksløsningen. 

## Brukes av

Modulen brukes av Vedtaksløsningen (automatisert behandling og saksbehandlers arbeidsflate) 

## Integrasjoner

* DokumentProduksjon (operasjon TDOK001 og TDOK003).
* Vedtaksløsningen (applikasjonstjenester og behandlingslager)
