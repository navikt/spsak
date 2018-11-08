-- Nytt kodeverk for næringsvirksomhet
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse) values ('NARINGSVIRKSOMHET_TYPE', 'N', 'N', 'NæringsvirksomhetType', 'Kodeverk for type næringsvirksomhet');

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'FISKE', 'Fiske', 'Fiske', 'NARINGSVIRKSOMHET_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'FRILANSER', 'Frilanser', 'Frilanser', 'NARINGSVIRKSOMHET_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'REINDRIFT', 'Reindrift', 'Reindrift', 'NARINGSVIRKSOMHET_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'JORDBRUK_SKOGBRUK', 'Jordbruk/skogbruk', 'Jordbruk/skogbruk', 'NARINGSVIRKSOMHET_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'ANNEN', 'Annen næringsvirksomhet', 'Annen næringsvirksomhet', 'NARINGSVIRKSOMHET_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

-- Nytt kodeverk for type fiske
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse) values ('TYPE_FISKE', 'N', 'N', 'Type fiske', 'Kodeverk for type fiske');

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'BLAD_A', 'Blad A', 'Blad A', 'TYPE_FISKE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'LOTT', 'Lott', 'Lott', 'TYPE_FISKE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'BLAD_B', 'Blad B', 'Blad B', 'TYPE_FISKE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'HYRE', 'Hyre', 'Hyre', 'TYPE_FISKE', to_date('2000-01-01', 'YYYY-MM-DD'));

-- Nytt kodeverk for mors aktivitet
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse) values ('MORS_AKTIVITET', 'N', 'N', 'Mors aktivitet', 'Kodeverk for mors aktivitet');

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'UFORETRYGD', 'Mottar uføretrygd', 'Mottar uføretrygd', 'MORS_AKTIVITET', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'ARBEID', 'Er i arbeid', 'Er i arbeid', 'MORS_AKTIVITET', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'UTDANNING', 'Tar utdanning på heltid', 'Tar utdanning på heltid', 'MORS_AKTIVITET', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'UTDOGARB', 'Tar utdanning i kombinasjon med arbeid', 'Tar utdanning i kombinasjon med arbeid', 'MORS_AKTIVITET', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'KVALPROG', 'Deltar i kvalifiseringsprogrammet', 'Deltar i kvalifiseringsprogrammet', 'MORS_AKTIVITET', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'INTROPROG', 'Deltar i introduksjonsprogram for nykomne innvandrere', 'Deltar i introduksjonsprogram for nykomne innvandrere', 'MORS_AKTIVITET', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'TRENGER_HJELP', 'Er avhengig av hjelp til å ta seg av barnet', 'Er syk eller har en skade som gjør at hun er helt avhengig av hjelp til å ta seg av barnet', 'MORS_AKTIVITET', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'INNLAGT', 'Er innlagt på institusjon', 'Er innlagt på institusjon', 'MORS_AKTIVITET', to_date('2000-01-01', 'YYYY-MM-DD'));

-- Nytt kodeverk for overta kvote far/medmor
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse) values ('OVERTA_KVOTE_FAR_MEDMOR_AARSAK', 'N', 'N', 'Overta kvote far/medmor', 'Kodeverk for årsaker til å ta over kvote for far/medmor');

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'ALENEOMSORG', 'Aleneomsorg for barnet/barna', 'Aleneomsorg for barnet/barna', 'OVERTA_KVOTE_FAR_MEDMOR_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'INNLAGT', 'Mor er innlagt på helseinstitusjon', 'Mor er innlagt på helseinstitusjon', 'OVERTA_KVOTE_FAR_MEDMOR_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'IKKE_RETT', 'Mor har ikke rett på foreldrepenger', 'Mor har ikke rett på foreldrepenger', 'OVERTA_KVOTE_FAR_MEDMOR_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'TRENGER_HJELP', 'Mor er pga sykdom avhengig av hjelp for å ta seg av barnet/barna', 'Mor er pga sykdom avhengig av hjelp for å ta seg av barnet/barna', 'OVERTA_KVOTE_FAR_MEDMOR_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

-- Nytt kodeverk for overta kvote mor
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse) values ('OVERTA_KVOTE_MOR_AARSAK', 'N', 'N',  'Overta kvote mor', 'Kodeverk for årsaker til å ta over kvote for mor');

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'ALENEOMSORG', 'Aleneomsorg for barnet/barna', 'Aleneomsorg for barnet/barna', 'OVERTA_KVOTE_MOR_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'INNLAGT', 'Den andre foreldren er innlagt i helseinstitusjon', 'Den andre foreldren er innlagt i helseinstitusjon', 'OVERTA_KVOTE_MOR_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'IKKE_RETT', 'Den andre foreldren har ikke rett på foreldrepenger', 'Den andre foreldren har ikke rett på foreldrepenger', 'OVERTA_KVOTE_MOR_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'TRENGER_HJELP', 'Den andre foreldren er pga sykdom avhengig av hjelp for å ta seg av barnet/barna', 'Den andre foreldren er pga sykdom avhengig av hjelp for å ta seg av barnet/barna', 'OVERTA_KVOTE_MOR_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

-- Nytt kodeverk for søknadTypeTillegg
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse) values ('SOKNAD_TYPE_TILLEGG', 'N', 'N', 'Søknadtype-tillegg', 'Kodeverk for tillegg til soknadtype');

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'OVERFORING_AV_KVOTER', 'Overføring av kvoter', 'Overføring av kvoter', 'SOKNAD_TYPE_TILLEGG', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'UTSETTELSE', 'Utsettelse', 'Utsettelse', 'SOKNAD_TYPE_TILLEGG', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'GRADERING', 'Gradering', 'Gradering', 'SOKNAD_TYPE_TILLEGG', to_date('2000-01-01', 'YYYY-MM-DD'));


DELETE FROM KODELISTE WHERE KODEVERK = 'FORELDRE_TYPE' AND KODE = 'ANDRE';
DELETE FROM KODELISTE WHERE KODEVERK = 'FORELDRE_TYPE' AND KODE = 'MEDFAR';
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'ANDRE', 'Annen omsorgsperson', 'Annen omsorgsperson', 'FORELDRE_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));


