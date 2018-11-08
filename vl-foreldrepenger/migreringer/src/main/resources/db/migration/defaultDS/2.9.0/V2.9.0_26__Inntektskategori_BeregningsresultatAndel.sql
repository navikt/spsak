ALTER TABLE BEREGNINGSRESULTAT_ANDEL ADD inntektskategori VARCHAR2(100 CHAR);
update BEREGNINGSRESULTAT_ANDEL set inntektskategori = '-';
ALTER TABLE BEREGNINGSRESULTAT_ANDEL MODIFY inntektskategori NOT NULL;
ALTER TABLE BEREGNINGSRESULTAT_ANDEL ADD kl_inntektskategori VARCHAR2(100 CHAR) AS ('inntektskategori');

comment on column BEREGNINGSRESULTAT_ANDEL.inntektskategori is 'Inntektskategori for andelen';
comment on column BEREGNINGSRESULTAT_ANDEL.kl_inntektskategori is 'Inntektskategori kodeverk';
