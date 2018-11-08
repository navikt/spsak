ALTER TABLE UTTAK_AKTIVITET
  ADD (
  arbeidsgiver_aktor_id VARCHAR2(100 CHAR),
  arbeidsgiver_virksomhet_id NUMBER(19)
  );

ALTER TABLE UTTAK_AKTIVITET
  ADD CONSTRAINT FK_UTTAK_AKTIVITET_3 FOREIGN KEY (arbeidsgiver_virksomhet_id) REFERENCES VIRKSOMHET (id);

CREATE INDEX IDX_UTTAK_AKTIVITET_3
  ON UTTAK_AKTIVITET (arbeidsgiver_virksomhet_id);

COMMENT ON COLUMN UTTAK_AKTIVITET.arbeidsgiver_aktor_id IS 'Aktør til personlig foretak.';
COMMENT ON COLUMN UTTAK_AKTIVITET.arbeidsgiver_virksomhet_id IS 'Fjernnøkkel til virksomhet tabellen.';
