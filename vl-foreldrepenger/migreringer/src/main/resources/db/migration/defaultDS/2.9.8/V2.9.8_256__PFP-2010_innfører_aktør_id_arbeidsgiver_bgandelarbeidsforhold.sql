ALTER TABLE BG_ANDEL_ARBEIDSFORHOLD ADD arbeidsgiver_aktor_id VARCHAR2(100 CHAR);

COMMENT ON COLUMN BG_ANDEL_ARBEIDSFORHOLD.arbeidsgiver_aktor_id IS 'Arbeidsgivers aktør id.';
