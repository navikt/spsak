INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'GYLDIG_UTSETTELSE', 'Gyldig utsettelse', 'Gyldig utsettelse', 'PERIODE_RESULTAT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'UGYLDIG_UTSETTELSE', 'Ugyldig utsettelse', 'Ugyldig utsettelse', 'PERIODE_RESULTAT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));


ALTER TABLE UTTAK_POSTERING MODIFY (ARBEIDSFORHOLD_ID VARCHAR2(100));
