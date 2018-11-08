INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier) VALUES ('FAKTA_OM_BEREGNING_TILFELLE', 'Kodeverk for mer spesifik kategorisering av kilde', 'Kodeverk for mer spesifik kategorisering av kilde','VL');

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'FAKTA_OM_BEREGNING_TILFELLE', 'VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD', 'Vurder tidsbegrenset arbeidsforhold', 'Vurder tidsbegrenset arbeidsforhold', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'FAKTA_OM_BEREGNING_TILFELLE', 'VURDER_SN_NY_I_ARBEIDSLIVET', 'Vurder om søker er SN og ny i arbeidslivet', 'Vurder om søker er SN og ny i arbeidslivet', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'FAKTA_OM_BEREGNING_TILFELLE', 'VURDER_NYOPPSTARTET_FL', 'Vurder nyoppstartet frilans', 'Vurder nyoppstartet frilans', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'FAKTA_OM_BEREGNING_TILFELLE', 'FASTSETT_MAANEDSINNTEKT_FL', 'Fastsett månedsinntekt frilans', 'Fastsett månedsinntekt frilans', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'FAKTA_OM_BEREGNING_TILFELLE', 'FASTSETT_ENDRET_BEREGNINGSGRUNNLAG', 'Fastsette endring i beregningsgrunnlag', 'Revurdering: Fastsette endring i beregningsgrunnlag', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'FAKTA_OM_BEREGNING_TILFELLE', 'FASTSETT_BG_ARBEIDSTAKER_UTEN_INNTEKTSMELDING', 'Fastsette beregningsgrunnlag for arbeidstaker uten inntektsmelding', 'Fastsette beregningsgrunnlag for arbeidstaker uten inntektsmelding', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'FAKTA_OM_BEREGNING_TILFELLE', '-', 'Ikke definert', 'Ikke definert', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

DELETE FROM AKSJONSPUNKT_DEF
WHERE KODE = '5046';
DELETE FROM AKSJONSPUNKT_DEF
WHERE KODE = '5048';
DELETE FROM AKSJONSPUNKT_DEF
WHERE KODE = '5059';

UPDATE AKSJONSPUNKT_DEF
set NAVN = 'Vurder fakta for arbeidstaker, frilans og selvstendig næringsdrivende', BESKRIVELSE = 'Faktaavklaring for arbeidstaker, frilanser og selvstendig næringsdrivende'
WHERE KODE = '5058';
