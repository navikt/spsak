ALTER TABLE TMP_INNTEKT
  ADD (
  arbeidsgiver_aktor_id VARCHAR2(100),
  arbeidsgiver_virksomhet_id NUMBER(19)
  );

COMMENT ON COLUMN TMP_INNTEKT.arbeidsgiver_aktor_id IS 'AktørID i de tilfeller hvor arbeidsgiver er en person.';
COMMENT ON COLUMN TMP_INNTEKT.arbeidsgiver_virksomhet_id IS 'Fjernnøkkel til virksomhets tabellen.';

ALTER TABLE TMP_INNTEKT
  ADD CONSTRAINT FK_TMP_INNTEKT_1 FOREIGN KEY (arbeidsgiver_virksomhet_id) REFERENCES VIRKSOMHET;

MERGE INTO TMP_INNTEKT I
USING (SELECT * FROM YRKESAKTIVITET) Y
ON (I.YRKESAKTIVITET_ID = Y.ID)
WHEN MATCHED THEN UPDATE SET I.arbeidsgiver_aktor_id = Y.arbeidsgiver_aktor_id, I.arbeidsgiver_virksomhet_id = Y.arbeidsgiver_virksomhet_id;

ALTER TABLE TMP_INNTEKT DROP COLUMN YRKESAKTIVITET_ID;
