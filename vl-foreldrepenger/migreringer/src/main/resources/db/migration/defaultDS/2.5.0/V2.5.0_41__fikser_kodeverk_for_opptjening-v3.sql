INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEID_TYPE', 'UTDANNINGSPERMISJON', null, 'Utdanningspermisjon', NULL, to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEID_TYPE', 'PLEIEPENGER', null, 'Mottar ytelse for Pleiepenger', NULL, to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE_RELASJON(ID, KODEVERK1, KODE1, KODEVERK2, KODE2, GYLDIG_FOM, GYLDIG_TOM)
values(seq_kodeliste_relasjon.nextval, 'OPPTJENING_AKTIVITET_TYPE', 'UTDANNINGSPERMISJON', 'ARBEID_TYPE', 'UTDANNINGSPERMISJON', to_date('01.01.2000', 'DD.MM.YYYY'), to_date('31.12.9999', 'DD.MM.YYYY'));
INSERT INTO KODELISTE_RELASJON(ID, KODEVERK1, KODE1, KODEVERK2, KODE2, GYLDIG_FOM, GYLDIG_TOM)
values(seq_kodeliste_relasjon.nextval, 'OPPTJENING_AKTIVITET_TYPE', 'PLEIEPENGER', 'ARBEID_TYPE', 'PLEIEPENGER', to_date('01.01.2000', 'DD.MM.YYYY'), to_date('31.12.9999', 'DD.MM.YYYY'));
