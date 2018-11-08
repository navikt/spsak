ALTER TABLE YF_FORDELING_PERIODE
  ADD (
  arbeidsgiver_aktor_id VARCHAR2(100 CHAR),
  arbeidsgiver_virksomhet_id NUMBER(19)
  );

ALTER TABLE YF_FORDELING_PERIODE
  ADD CONSTRAINT FK_YF_FORDELING_PERIODE_4 FOREIGN KEY (arbeidsgiver_virksomhet_id) REFERENCES VIRKSOMHET (id);

CREATE INDEX IDX_YF_FORDELING_PERIODE_8
  ON YF_FORDELING_PERIODE (arbeidsgiver_virksomhet_id);

COMMENT ON COLUMN YF_FORDELING_PERIODE.arbeidsgiver_aktor_id IS 'Aktør til personlig foretak.';
COMMENT ON COLUMN YF_FORDELING_PERIODE.arbeidsgiver_virksomhet_id IS 'Fjernnøkkel til virksomhet tabellen.';
