-- Typo kodeverk
ALTER TABLE YTELSE_STOERRELSE
  DROP CONSTRAINT FK_YTELSE_STOERRELSE_1;
ALTER TABLE YTELSE_STOERRELSE
  MODIFY (kl_hyppighet VARCHAR2(100) AS ('INNTEKT_PERIODE_TYPE'));
ALTER TABLE YTELSE_STOERRELSE
  ADD CONSTRAINT FK_YTELSE_STOERRELSE_1 FOREIGN KEY (hyppighet, kl_hyppighet) REFERENCES KODELISTE (kode, kodeverk);

INSERT INTO KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', '', 'Ikke definert', 'Andre verdier', 'INNTEKT_PERIODE_TYPE',
        to_date('2000-01-01', 'YYYY-MM-DD'));

ALTER TABLE YTELSE MODIFY (saksnummer NULL);
