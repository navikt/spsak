# web-webapp

web-webapp inneholder blant annet tjenester for hendelser, lese- eller aktivitetstjenester systemet leverer som kan aksesseres av andre systemer i NAV.

Disse leveres som  en av følgende:
* WebServices (WS-\*) med sikring via SAML (tilsv. NAV 3gen virksomhetstjenester)
* REST tjenester med JSON (evt. XML) output.  Sikret vha. OpenID Connect (OIDC) tokens.
* Json feeds.  Disse håndteres likt REST tjenester.  Brukes for å distribuere hendelser.

For tjenester som eksponeres for andre systemer vil de implementere en kontrakt og være dokumenteret i [Tjenestekatalogen](https://confluence.adeo.no/display/SDFS/Tjenestekatalog).

