-- SKJERMLENKE_TYPE
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FAKTA_OM_UTTAK', 'Fakta om uttak', 'Fakta om uttak', to_date('2000-01-01', 'YYYY-MM-DD'), 'SKJERMLENKE_TYPE');
UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'FAKTA_OM_UTTAK' WHERE KODE = '5070';

-- HISTORIKKINNSLAG_TYPE UTTAK
insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'UTTAK', 'Behandlet soknadsperiode', to_date('2017-01-01', 'YYYY-MM-DD'), '{"mal": "TYPE5"}');

-- HISTORIKKINNSLAG_FELT_TYPE AVKLART_SOEKNADSPERIODE
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'AVKLART_SOEKNADSPERIODE', 'Avklart soeknadsperiode', 'Avklart soeknadsperiode', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKKINNSLAG_FELT_TYPE');

-- HISTORIKK_AVKLART_SOEKNADSPERIODE_TYPE
INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier) VALUES ('HISTORIKK_AVKLART_SOEKNADSPERIODE_TYPE', 'Avklart soeknadsperiode type', 'Avklart soeknadsperiode type','VL');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'GRADERING', 'Gradering på grunn av arbeid', 'Gradering på grunn av arbeid', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_AVKLART_SOEKNADSPERIODE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'UTSETTELSE_ARBEID', 'Utsettelse på grunn av arbeid', 'Utsettelse på grunn av arbeid', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_AVKLART_SOEKNADSPERIODE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'UTSETTELSE_FERIE', 'Utsettelse på grunn av ferie', 'Utsettelse på grunn av ferie', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_AVKLART_SOEKNADSPERIODE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'UTSETTELSE_SKYDOM', 'Utsettelse på grunn av sykdom/skade', 'Utsettelse på grunn av sykdom/skade', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_AVKLART_SOEKNADSPERIODE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'UTSETTELSE_INSTITUSJON_SØKER', 'Utsettelse på grunn av innleggelse av forelder', 'Utsettelse på grunn av innleggelse av forelder', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_AVKLART_SOEKNADSPERIODE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'UTSETTELSE_INSTITUSJON_BARN', 'Utsettelse på grunn av innleggelse av barn', 'Utsettelse på grunn av innleggelse av barn', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_AVKLART_SOEKNADSPERIODE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'NY_SOEKNADSPERIODE', 'Ny periode er lagt til', 'Ny periode er lagt til', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_AVKLART_SOEKNADSPERIODE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'SLETTET_SOEKNASPERIODE', 'Periode slettet', 'Periode slettet', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_AVKLART_SOEKNADSPERIODE_TYPE');

-- HISTORIKK_ENDRET_FELT_TYPE FASTSETT_RESULTAT_PERIODEN
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FASTSETT_RESULTAT_PERIODEN', 'Fastsett resultat for perioden', 'Fastsett resultat for perioden', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');

-- HISTORIKK_ENDRET_FELT_VERDI_TYPE
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FASTSETT_RESULTAT_GRADERING_AVKLARES', 'Tilpass søknadsperiode og andel arbeid til inntektsmeldingen', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FASTSETT_RESULTAT_UTSETTELSE_AVKLARES', 'Tilpass søknadsperiode til inntektsmeldingen', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FASTSETT_RESULTAT_PERIODEN_AVKLARES_IKKE', 'Perioden kan ikke avklares', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FASTSETT_RESULTAT_PERIODEN_SYKDOM_DOKUMENTERT_IKKE', 'Sykdommen/skaden er ikke dokumentert', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FASTSETT_RESULTAT_PERIODEN_INNLEGGELSEN_DOKUMENTERT_IKKE', 'Innleggelsen er ikke dokumentert', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
