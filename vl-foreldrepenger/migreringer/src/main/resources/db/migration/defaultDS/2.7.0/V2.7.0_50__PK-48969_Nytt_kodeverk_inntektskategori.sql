insert into kodeverk (kode, navn, beskrivelse ) values ('INNTEKTSKATEGORI', 'Inntektskategori', 'Inntektskategori');

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNTEKTSKATEGORI', 'ARBEIDSTAKER', 'Arbeidstaker', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNTEKTSKATEGORI', 'FRILANSER', 'Frilanser', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNTEKTSKATEGORI', 'SELVSTENDIG_NÆRINGSDRIVENDE', '	Selvstendig næringsdrivende (ordinær næring)', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNTEKTSKATEGORI', 'DAGPENGER', 'Dagpenger', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNTEKTSKATEGORI', 'ARBEIDSAVKLARINGSPENGER', 'Arbeidsavklaringspenger', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNTEKTSKATEGORI', 'DAGMAMMA', 'Selvstendig næringsdrivende (dagmamma)', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNTEKTSKATEGORI', 'JORDBRUKER', 'Selvstendig næringsdrivende (jordbruker)', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNTEKTSKATEGORI', 'FISKER', 'Selvstendig næringsdrivende (fisker)', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNTEKTSKATEGORI', 'ARBEIDSTAKER_UTEN_FERIEPENGER', 'Arbeidstaker uten feriepenger', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNTEKTSKATEGORI', '-', 'Ingen inntektskategori (default)', to_date('2000-01-01', 'YYYY-MM-DD'));

ALTER TABLE BG_PR_STATUS_OG_ANDEL ADD INNTEKTSKATEGORI  VARCHAR2(100);
UPDATE BG_PR_STATUS_OG_ANDEL SET INNTEKTSKATEGORI = '-';
ALTER TABLE BG_PR_STATUS_OG_ANDEL MODIFY INNTEKTSKATEGORI NOT NULL;
ALTER TABLE BG_PR_STATUS_OG_ANDEL ADD KL_INNTEKTSKATEGORI   VARCHAR2(100)  AS ('INNTEKTSKATEGORI');
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.INNTEKTSKATEGORI IS 'FK: INNTEKTSKATEGORI';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.KL_INNTEKTSKATEGORI IS 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
