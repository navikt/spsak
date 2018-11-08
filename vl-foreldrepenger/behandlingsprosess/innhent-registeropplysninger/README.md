# Innhent registeropplysninger

Modulen inneholder implementasjonen for steget Innhent Saksopplysninger. Dette steget skal innhente, filtrere og lagre i behandlingslageret relevant informasjon fra følgende kilder. På grunn av at Infotrygd har begrenset oppetid, utføres steget asynkront i en prosesstask. Som følge av dette blir behandlingsprosessen delt i to ved dette steget. Når steget er fullført, vil behandlingsprosessen bli kjørt videre.

* Infotrygd (relaterte ytelser)
* ARENA (relaterte ytelser)
* TPS (søker, relasjoner)
* MEDL (medlemskap)
* Inntekt

## Hensikten

* Innhente relevant informasjon for behandlingen
* Håndtere begrenset oppetid i Infotrygd
* Fortsette behandlingsprosessen etter fullført innhenting

## Brukes av

* Behandlingsprosess

## Integrasjoner

* Behandlingslageret
