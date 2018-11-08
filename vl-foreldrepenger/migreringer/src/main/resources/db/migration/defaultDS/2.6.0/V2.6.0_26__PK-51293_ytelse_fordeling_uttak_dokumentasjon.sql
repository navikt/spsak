INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'UTTAK_DOKUMENTASJON_KLASSE', 'UTTAK_DOK', 'Uttak dokumentasjon',
        'Uttak dokumentasjon', to_date('2000-01-01', 'YYYY-MM-DD'));


ALTER TABLE GR_YTELSES_FORDELING
  ADD (
  uttak_dokumentasjon_id NUMBER
  );


ALTER TABLE GR_YTELSES_FORDELING
  ADD CONSTRAINT FK_YF_DOKUMENTASJON_PERIODE_11 FOREIGN KEY (uttak_dokumentasjon_id) REFERENCES YF_DOKUMENTASJON_PERIODER (ID);
