# Policy Enforcement Point (PEP)

Modulen er Vedtaksløsningens implementasjon av NAV ABAC tilgangskontroll. PEP samler relevante attributter og sender dem til en ekstern PDP (Policy Decision Point), og håndhever den avgjørelsen som PDP tar om å gi eller ikke gi tilgang.

Ressurser som skal beskyttes av PEP annoteres med @BeskyttetRessurs og hvilke attributter som gjelder.

## Hensikten

* Sikre at brukere av Vedtaksløsningen ikke får tilgang til informasjon de ikke skal se.

## Brukes av

Modulen brukes av alle REST og SOAP endepunkt i Vedtaksløsningen, samt andre tjenestepunkt som skal sikres
 
## Integrasjoner
* ABAC PDP via PDP-klient modul i Felles 
