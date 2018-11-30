# Støttetjenester

TODO: 

Modulene under denne er i hovedsak tjenester som har domene logikk (tilsv. DDD Domain Services) og tjenester som mapper mellom eksterne tjenestekontrakter og interne informasjonsmodeller (DDD Anti-Corruption Layer).  Disse skal skilles.

## Anti-Corruption Layer
Eksterne tjenestekontrakter har ikke lov til å lekke ut i applikasjonen siden koden da blir mer sårbare for bytte av tjenester og endringer i kontrakter og gjør det vanskelig å bytte tjeneste eller refactorere informasjonsmodell. Et ACL (Anti-Corruption Layer) begrenser dette.

## Domain Services
Domain Services er tjenester som har domenelogikk for applikasjonen *som ikke naturlig hører hjemme i Entitites eller Value Objects*.  Typisk er dette logikk som spenner flere entiteter eller aggregater.

