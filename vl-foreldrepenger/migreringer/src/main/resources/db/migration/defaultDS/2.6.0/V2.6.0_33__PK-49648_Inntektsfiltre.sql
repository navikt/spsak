INSERT INTO KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
VALUES ('INNTEKTS_FILTER', 'N', 'N', 'A-inntektsfilter', 'Kodeverk for inntektsfilter');
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNTEKTS_FILTER', 'BEREGNINGSGRUNNLAG', '8-28', 'Beregningsgrunnlag', 'Beregningsgrunnlag.', to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNTEKTS_FILTER', 'SAMMENLIGNINGSGRUNNLAG', '8-30', 'Sammenligningsgrunnlag', 'Sammenligningsgrunnlag', to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNTEKTS_FILTER', 'OPPTJENINGSGRUNNLAG', 'PensjonsgivendeA-Inntekt', 'Pensjonsgivende inntekt', 'Benyttes som input til opptjeningsvilkåret.', to_date('2017-12-07', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNTEKTS_KILDE', 'INNTEKT_BEREGNING', NULL, 'INNTEKT_BEREGNING', 'Inntektskomponenten beregningsgrunnlag.', to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNTEKTS_KILDE', 'INNTEKT_SAMMENLIGNING', NULL, 'INNTEKT_SAMMENLIGNING', 'Inntektskomponenten sammenligningsgrunnlag.', to_date('2017-12-07', 'YYYY-MM-DD'));

UPDATE KODELISTE SET NAVN = 'INNTEKT_OPPTJENING', KODE = 'INNTEKT_OPPTJENING' WHERE KODEVERK = 'INNTEKTS_KILDE' AND KODE = 'INNTEKT';
UPDATE TMP_INNTEKT SET KILDE = 'INNTEKT_OPPTJENING' WHERE KILDE = 'INNTEKT';

INSERT INTO KODELISTE_RELASJON (id, kodeverk1, kode1, kodeverk2, kode2, GYLDIG_FOM, GYLDIG_TOM)
VALUES (seq_kodeliste_relasjon.nextval, 'INNTEKTS_KILDE', 'INNTEKT_BEREGNING', 'INNTEKTS_FILTER', 'BEREGNINGSGRUNNLAG', to_date('01.01.2000', 'DD.MM.YYYY'), to_date('31.12.9999', 'DD.MM.YYYY'));
INSERT INTO KODELISTE_RELASJON (id, kodeverk1, kode1, kodeverk2, kode2, GYLDIG_FOM, GYLDIG_TOM)
VALUES (seq_kodeliste_relasjon.nextval, 'INNTEKTS_KILDE', 'INNTEKT_OPPTJENING', 'INNTEKTS_FILTER', 'OPPTJENINGSGRUNNLAG', to_date('01.01.2000', 'DD.MM.YYYY'), to_date('31.12.9999', 'DD.MM.YYYY'));
INSERT INTO KODELISTE_RELASJON (id, kodeverk1, kode1, kodeverk2, kode2, GYLDIG_FOM, GYLDIG_TOM)
VALUES (seq_kodeliste_relasjon.nextval, 'INNTEKTS_KILDE', 'INNTEKT_SAMMENLIGNING', 'INNTEKTS_FILTER', 'SAMMENLIGNINGSGRUNNLAG', to_date('01.01.2000', 'DD.MM.YYYY'), to_date('31.12.9999', 'DD.MM.YYYY'));

INSERT INTO KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
VALUES ('INNTEKTS_FORMAAL', 'N', 'N', 'Formaal', 'Kodeverk for formål til inntekt.');
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNTEKTS_FORMAAL', 'FORMAAL_FORELDREPENGER', 'Foreldrepenger', 'Formålskode for foreldrepenger', 'Formålskode for 8-28 og 8-30.', to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNTEKTS_FORMAAL', 'FORMAAL_PGI', 'PensjonsgivendeA-inntekt', 'Formålskode for PGI', 'Formålskode for PGI.', to_date('2017-12-07', 'YYYY-MM-DD'));

INSERT INTO KODELISTE_RELASJON (id, kodeverk1, kode1, kodeverk2, kode2, GYLDIG_FOM, GYLDIG_TOM)
VALUES (seq_kodeliste_relasjon.nextval, 'INNTEKTS_FILTER', 'OPPTJENINGSGRUNNLAG', 'INNTEKTS_FORMAAL', 'FORMAAL_PGI', to_date('01.01.2000', 'DD.MM.YYYY'), to_date('31.12.9999', 'DD.MM.YYYY'));
INSERT INTO KODELISTE_RELASJON (id, kodeverk1, kode1, kodeverk2, kode2, GYLDIG_FOM, GYLDIG_TOM)
VALUES (seq_kodeliste_relasjon.nextval, 'INNTEKTS_FILTER', 'SAMMENLIGNINGSGRUNNLAG', 'INNTEKTS_FORMAAL', 'FORMAAL_FORELDREPENGER', to_date('01.01.2000', 'DD.MM.YYYY'), to_date('31.12.9999', 'DD.MM.YYYY'));
INSERT INTO KODELISTE_RELASJON (id, kodeverk1, kode1, kodeverk2, kode2, GYLDIG_FOM, GYLDIG_TOM)
VALUES (seq_kodeliste_relasjon.nextval, 'INNTEKTS_FILTER', 'BEREGNINGSGRUNNLAG', 'INNTEKTS_FORMAAL', 'FORMAAL_FORELDREPENGER', to_date('01.01.2000', 'DD.MM.YYYY'), to_date('31.12.9999', 'DD.MM.YYYY'));
