-- Fjern ubrukt kodeverk fra databasen
DELETE FROM KODELISTE WHERE KODEVERK = 'NARINGSVIRKSOMHET_TYPE';
DELETE FROM KODEVERK WHERE KODE = 'NARINGSVIRKSOMHET_TYPE';

-- Legg til nye kodeliste verdier
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'FISKE', 'Fiske', 'Fiske', 'VIRKSOMHET_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'FRILANSER', 'Frilanser', 'Frilanser', 'VIRKSOMHET_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'REINDRIFT', 'Reindrift', 'Reindrift', 'VIRKSOMHET_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'JORDBRUK_SKOGBRUK', 'Jordbruk/skogbruk', 'Jordbruk/skogbruk', 'VIRKSOMHET_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'ANNEN', 'Annen næringsvirksomhet', 'Annen næringsvirksomhet', 'VIRKSOMHET_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
