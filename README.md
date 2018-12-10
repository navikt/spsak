# Saksbehandling av sykepenger
Dette er en fork av fpsak for behandling av sykepenger søknader.
For å kunne behandle en sykepengesøknad trengs en søknad fra bruker (sendt inn på bakgrunn av en sykemelding) og inntektsmelding fra arbeidsgiver.

[Om saksbehandling sykepenger](docs/sykepenger.md)

# Oppsett utvikler miljø
## Database
Postgresql benyttes som database. Denne kan kjøres lokalt vha. docker-compose.

`(cd docker/localdev && docker-compose up -d)`
