-- KONFIGURERER KODEVERK SOM SKAL SYNKES FRA Kodeverkklienten automatisk (eller ikke)

INSERT INTO kodeverk (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_eier_navn, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('ADRESSE_TYPE', 'Adressetyper', 'NAV Adressetyper', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/Adressetyper', '1', 'Adressetyper', 'J', 'J');

INSERT INTO kodeverk (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_eier_navn, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('ARKIV_FILTYPE', 'Arkivfiltyper', 'NAV Arkivfiltyper', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/Arkivfiltyper', '3', 'Arkivfiltyper', 'J', 'J');

INSERT INTO kodeverk (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_eier_navn, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('ARKIV_TEMA', 'Arkivtemaer', 'NAV Arkivtemaer', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/Arkivtemaer', '5', 'Arkivtemaer', 'J', 'J');

INSERT INTO kodeverk (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_eier_navn, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('AVLOENNING_TYPE', 'Avlønningstyper', 'NAV Avlønningstyper', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/Avl_c3_b8nningstyper', '1', 'Avlønningstyper', 'J', 'J');

-- tar ikke inn nye koder eller oppdaterer, preregistrer 
INSERT INTO kodeverk (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_eier_navn, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('BEHANDLING_TEMA', 'Behandlingstema', 'NAV Behandlingstema', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/Behandlingstema', '9', 'Behandlingstema', 'N', 'N');

UPDATE kodeverk SET kodeverk_eier='Kodeverkforvaltning', kodeverk_eier_ref='http://nav.no/kodeverk/Kodeverk/Behandlingstyper', kodeverk_eier_ver='5', kodeverk_eier_navn='Behandlingstyper', kodeverk_synk_nye='J', kodeverk_synk_eksisterende='J', navn='Behandlingstyper' WHERE kode='BEHANDLING_TYPE';
UPDATE kodeverk SET kodeverk_eier='Kodeverkforvaltning', kodeverk_eier_ref='http://nav.no/kodeverk/Kodeverk/Diskresjonskoder', kodeverk_eier_ver='1', kodeverk_eier_navn='Diskresjonskoder', kodeverk_synk_nye='J', kodeverk_synk_eksisterende='J', navn='Diskresjonskoder' WHERE kode='DISKRESJONSKODE';
UPDATE kodeverk SET kodeverk_eier='Kodeverkforvaltning', kodeverk_eier_ref='http://nav.no/kodeverk/Kodeverk/DokumentTypeId-er', kodeverk_eier_ver='2', kodeverk_eier_navn='DokumentTypeId-er', kodeverk_synk_nye='J', kodeverk_synk_eksisterende='J', navn='DokumentTypeId-er' WHERE kode='DOKUMENT_TYPE';
UPDATE kodeverk SET kodeverk_eier='Kodeverkforvaltning', kodeverk_eier_ref='http://nav.no/kodeverk/Kodeverk/Familierelasjoner', kodeverk_eier_ver='1', kodeverk_eier_navn='Familierelasjoner', kodeverk_synk_nye='J', kodeverk_synk_eksisterende='J', navn='Familierelasjoner' WHERE kode='RELASJONSROLLE_TYPE';

/*
-- tas ikke inn nå, brukes ikke i Engangsstønad?
INSERT INTO kodeverk (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_eier_navn, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('FORELDREANSVAR_TYPE', 'Foreldreansvarstyper', 'NAV Foreldreansvarstyper', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/Foreldreansvarstyper', '1', 'Foreldreansvarstyper', 'J', 'J');

INSERT INTO kodeverk (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_eier_navn, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('IDENT_STATUS', 'Identstatuser', 'NAV Identstatuser', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/Identstatuser', '1', 'Identstatuser', 'J', 'J');

INSERT INTO kodeverk (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_eier_navn, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('INNTEKT_STATUS', 'Inntektstatuser', 'NAV Inntektstatuser', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/Inntektstatuser', '1', 'Inntektstatuser', 'J', 'J');
*/

INSERT INTO kodeverk (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_eier_navn, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('JOURNAL_STATUS', 'Journalstatuser', 'NAV Journalstatuser', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/Journalstatuser', '1', 'Journalstatuser', 'J', 'J');

INSERT INTO kodeverk (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_eier_navn, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('KOMMUNER', 'Kommuner', 'NAV Kommuner', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/Kommuner', '1', 'Kommuner', 'J', 'J');

INSERT INTO kodeverk (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_eier_navn, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('OPPHOLDSTILLATELSE', 'Oppholdstillatelser', 'NAV Oppholdstillatelser', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/Oppholdstillatelser', '1', 'Oppholdstillatelser', 'J', 'J');

UPDATE kodeverk SET kodeverk_eier='Kodeverkforvaltning', kodeverk_eier_ref='http://nav.no/kodeverk/Kodeverk/Postadressetyper', kodeverk_eier_ver='1', kodeverk_eier_navn='Postadressetyper', kodeverk_synk_nye='J', kodeverk_synk_eksisterende='J', navn='Postadressetyper' WHERE kode='OPPLYSNING_ADRESSE_TYPE';

UPDATE kodeverk SET kodeverk_eier='Kodeverkforvaltning', kodeverk_eier_ref='http://nav.no/kodeverk/Kodeverk/Sivilstander', kodeverk_eier_ver='1', kodeverk_eier_navn='Sivilstander', kodeverk_synk_nye='J', kodeverk_synk_eksisterende='J', navn='Sivilstander' WHERE kode='SIVILSTAND_TYPE';
