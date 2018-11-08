-- Arbeidskategori på IAY_YTELSE_GRUNNLAG
ALTER TABLE IAY_YTELSE_GRUNNLAG ADD ARBEIDSKATEGORI  VARCHAR2(100 CHAR);
UPDATE IAY_YTELSE_GRUNNLAG SET ARBEIDSKATEGORI = '-';
ALTER TABLE IAY_YTELSE_GRUNNLAG MODIFY ARBEIDSKATEGORI NOT NULL;
ALTER TABLE IAY_YTELSE_GRUNNLAG ADD KL_ARBEIDSKATEGORI   VARCHAR2(100 CHAR)  AS ('ARBEIDSKATEGORI');
COMMENT ON COLUMN IAY_YTELSE_GRUNNLAG.ARBEIDSKATEGORI IS 'FK: ARBEIDSKATEGORI';
COMMENT ON COLUMN IAY_YTELSE_GRUNNLAG.KL_ARBEIDSKATEGORI IS 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';


ALTER TABLE IAY_YTELSE_GRUNNLAG DROP COLUMN ARBEID_TYPE CASCADE CONSTRAINTS;
ALTER TABLE IAY_YTELSE_GRUNNLAG DROP COLUMN KL_ARBEID_TYPE;

-- Ny aksjonspunkt_def
INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, OPPRETTET_AV, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT, SKJERMLENKE_TYPE)
VALUES ('5050', 'Avklar beregningsgrunnlag og inntektskategori for tilstøtende ytelse', 'FORS_BERGRUNN.INN', 'Avklar beregningsgrunnlag og inntektskategori for bruker med tilstøtende ytelse kap 8, 9 eller 14', 'VL', 'FP_VK_41', 'J', 'FAKTA_OM_BEREGNING');

-- Nytt felt
alter table BG_PR_STATUS_OG_ANDEL add ny_i_arbeidslivet VARCHAR2(1 CHAR);
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.ny_i_arbeidslivet IS 'Oppgir om bruker er ny i arbeidslivet';

--Nye Aksjonspunkter
INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, OPPRETTET_AV, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT, SKJERMLENKE_TYPE)
VALUES ('5048', 'Vurder om bruker er SN som er ny i arbeidslivet', 'KOFAKBER.UT', 'Bruker har status SN eller kombinasjon hvor SN inngår, og har oppgitt i søknad at de er ny i arbeidslivet.', 'VL', '-', 'J', 'FAKTA_OM_BEREGNING');

INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, OPPRETTET_AV, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT, SKJERMLENKE_TYPE)
VALUES ('5049', 'Fastsett beregningsgrunnlag for SN som er ny i arbeidslivet', 'FAST_BERGRUNN.INN', 'Fastsett beregningsgrunnlag for SN som er ny i arbeidslivet', 'VL', '-', 'J', 'BEREGNING_FORELDREPENGER');

-- HISTORIKK_ENDRET_FELT_TYPE SELVSTENDIG NÆRINGSDRIVENDE
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'SELVSTENDIG_NAERINGSDRIVENDE', 'Selvstendig næringsdrivende', 'Selvstendig næringsdrivende', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');

-- HISTORIKK_ENDRET_FELT_VERDI_TYPE
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'NY_I_ARBEIDSLIVET', 'Endre til ny i arbeidslivet', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'IKKE_NY_I_ARBEIDSLIVET', 'Endre til ikke ny i arbeidslivet', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');

--Nye Aksjonspunkter
INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, OPPRETTET_AV, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT, SKJERMLENKE_TYPE)
VALUES ('5058', 'Vurder om bruker er nyoppstartet frilanser', 'KOFAKBER.UT', 'Bruker har status FL eller kombinasjon hvor FL inngår, og har oppgitt i søknad at de er nyoppstartet.', 'VL', '-', 'J', 'FAKTA_OM_BEREGNING');

INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, OPPRETTET_AV, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT, SKJERMLENKE_TYPE)
VALUES ('5059', 'Fastsett månedsinntekt for nyoppstartet frilanser', 'FORS_BERGRUNN.INN', 'Fastsett månedsinntekt for FL som er nyoppstartet', 'VL', '-', 'J', 'FAKTA_OM_BEREGNING');

-- Nytt felt
alter table BG_PR_STATUS_OG_ANDEL add nyoppstartet_frilanser VARCHAR2(1 CHAR);
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.nyoppstartet_frilanser IS 'Oppgir om bruker er nyoppstartet frilanser';

-- HISTORIKK_ENDRET_FELT_TYPE SELVSTENDIG NÆRINGSDRIVENDE
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'FRILANSVIRKSOMHET', 'Frilansvirksomhet', 'Frilansvirksomhet', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');

-- HISTORIKK_ENDRET_FELT_VERDI_TYPE
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'NYOPPSTARTET', 'Endre til nyoppstartet', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'IKKE_NYOPPSTARTET', 'Endre til ikke nyoppstartet', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');


-- Nytt kodeverk ARBEIDSKATEGORI
insert into kodeverk (kode, navn, beskrivelse ) values ('ARBEIDSKATEGORI', 'Arbeidskategori', 'Arbeidskategori');

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEIDSKATEGORI', 'FISKER', 'Fisker', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEIDSKATEGORI', 'ARBEIDSTAKER', 'Arbeidstaker', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEIDSKATEGORI', 'SELVSTENDIG_NÆRINGSDRIVENDE', 'Selvstendig næringsdrivende', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEIDSKATEGORI', 'KOMBINASJON_ARBEIDSTAKER_OG_SELVSTENDIG_NÆRINGSDRIVENDE', 'Kombinasjon arbeidstaker og selvstendig næringsdrivende', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEIDSKATEGORI', 'SJØMANN', 'Sjømann', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEIDSKATEGORI', 'JORDBRUKER', 'Selvstendig næringsdrivende (jordbruker)', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEIDSKATEGORI', 'DAGPENGER', 'Dagpenger', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEIDSKATEGORI', 'INAKTIV', 'Inaktiv', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEIDSKATEGORI', 'KOMBINASJON_ARBEIDSTAKER_OG_JORDBRUKER', 'Kombinasjon arbeidstaker og jordbruker', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEIDSKATEGORI', 'KOMBINASJON_ARBEIDSTAKER_OG_FISKER', 'Kombinasjon arbeidstaker og fisker', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEIDSKATEGORI', 'FRILANSER', 'Frilanser', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEIDSKATEGORI', 'KOMBINASJON_ARBEIDSTAKER_OG_FRILANSER', 'Kombinasjon arbeidstaker og frilanser', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEIDSKATEGORI', 'KOMBINASJON_ARBEIDSTAKER_OG_DAGPENGER', 'Kombinasjon arbeidstaker og dagepenger', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEIDSKATEGORI', 'DAGMAMMA', 'Dagmamma', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEIDSKATEGORI', 'ANNET', 'Annet', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEIDSKATEGORI', '-', 'Ingen inntektskategori (default)', to_date('2000-01-01', 'YYYY-MM-DD'));

ALTER TABLE BEREGNINGSGRUNNLAG ADD DEKNINGSGRAD  NUMBER(3);
UPDATE BEREGNINGSGRUNNLAG SET DEKNINGSGRAD = 100;
ALTER TABLE BEREGNINGSGRUNNLAG MODIFY DEKNINGSGRAD NOT NULL;
COMMENT ON COLUMN BEREGNINGSGRUNNLAG.DEKNINGSGRAD IS 'Dekningsgrad (80 eller 100) for denne foreldrepengesaken';

ALTER TABLE BEREGNINGSGRUNNLAG ADD OPPRINNELIG_SKJARINGSTIDSPUNKT  DATE;
UPDATE BEREGNINGSGRUNNLAG SET OPPRINNELIG_SKJARINGSTIDSPUNKT = to_date('2000-01-01', 'YYYY-MM-DD');
ALTER TABLE BEREGNINGSGRUNNLAG MODIFY OPPRINNELIG_SKJARINGSTIDSPUNKT NOT NULL;
COMMENT ON COLUMN BEREGNINGSGRUNNLAG.OPPRINNELIG_SKJARINGSTIDSPUNKT IS 'Normalt samme som skjæringstidspunkt. Ved overgang mellom ytelser er det første uttaksdag for den første i rekken av tilstøtende ytelser.';

ALTER TABLE BEREGNINGSGRUNNLAG ADD GRUNNBELOEP  NUMBER(12,2);
UPDATE BEREGNINGSGRUNNLAG SET GRUNNBELOEP = 0;
ALTER TABLE BEREGNINGSGRUNNLAG MODIFY GRUNNBELOEP NOT NULL;
COMMENT ON COLUMN BEREGNINGSGRUNNLAG.GRUNNBELOEP IS 'Grunnbeløp (G) ved opprinnelig_skjæringstidspunkt';

ALTER TABLE BEREGNINGSGRUNNLAG ADD REDUSERT_GRUNNBELOEP  NUMBER(12,2);
UPDATE BEREGNINGSGRUNNLAG SET REDUSERT_GRUNNBELOEP = 0;
ALTER TABLE BEREGNINGSGRUNNLAG MODIFY REDUSERT_GRUNNBELOEP NOT NULL;
COMMENT ON COLUMN BEREGNINGSGRUNNLAG.REDUSERT_GRUNNBELOEP IS 'Normalt samme som grunnbeløp. Ved overgang mellom ytelser blir grunnbeløpet redusert i henhold til dekningsgrad for alle tidligere tilstøtende ytelser';

ALTER TABLE BG_PR_STATUS_OG_ANDEL ADD ARBEIDSPERIODE_FOM  DATE;
UPDATE BG_PR_STATUS_OG_ANDEL SET ARBEIDSPERIODE_FOM = to_date('2000-01-01', 'YYYY-MM-DD');
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.ARBEIDSPERIODE_FOM IS 'Datoen da arbeidsforholdet (eller aktiviteten) startet';

ALTER TABLE BG_PR_STATUS_OG_ANDEL ADD ARBEIDSPERIODE_TOM  DATE;
UPDATE BG_PR_STATUS_OG_ANDEL SET ARBEIDSPERIODE_TOM = to_date('2000-01-01', 'YYYY-MM-DD');
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.ARBEIDSPERIODE_TOM IS 'Datoen da arbeidsforholdet (eller aktiviteten) avsluttet';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.TIDSBEGRENSET_ARBEIDSFORHOLD IS '''J'' dersom andelen angår et tidsbegrenset arbeidsforhold, ellers ''N''.';


insert into kodeverk (kode, navn, beskrivelse ) values ('BEREGNINGSGRUNNLAG_ANDELTYPE', 'Beregningsgrunnlag andeltype', 'Internt kodeverk for andelstyper ved fastsetting av beregningsgrunnlag ved tilstøtende ytelser.');

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'BEREGNINGSGRUNNLAG_ANDELTYPE', 'BRUKERS_ANDEL', 'Brukers andel', to_date('2000-01-01', 'YYYY-MM-DD'));


INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'BEREGNINGSGRUNNLAG_ANDELTYPE', 'EGEN_NÆRING', 'Egen næring', to_date('2000-01-01', 'YYYY-MM-DD'));


INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'BEREGNINGSGRUNNLAG_ANDELTYPE', 'FRILANS', 'Frilans', to_date('2000-01-01', 'YYYY-MM-DD'));

-- Sporing av regelinput og regellogg for tilstøtende ytelse
alter table BEREGNINGSGRUNNLAG add (regelinput_tilstøtende_ytelse CLOB);
alter table BEREGNINGSGRUNNLAG add (regellogg_tilstøtende_ytelse CLOB);
COMMENT ON COLUMN BEREGNINGSGRUNNLAG.regelinput_tilstøtende_ytelse IS 'Input til regelen som oppretter andeler for aktivitetstatus tilstøtende ytelse.';
COMMENT ON COLUMN BEREGNINGSGRUNNLAG.regellogg_tilstøtende_ytelse IS 'Sporing av regelen som oppretter andeler for aktivitetstatus tilstøtende ytelse.';
