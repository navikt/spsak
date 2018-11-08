DELETE FROM KODEVERK WHERE KODE='AKSJONSPUNKT_KATEGORI';
DELETE FROM KODEVERK WHERE KODE='ARKIV_TEMA';
DELETE FROM KODEVERK WHERE KODE='AVLOENNING_TYPE';
DELETE FROM KODEVERK WHERE KODE='BEHANDLING_MOTIV_TYPE';
DELETE FROM KODEVERK WHERE KODE='BRUKER_ROLLE_TYPE';
DELETE FROM KODEVERK WHERE KODE='BYDELER';
DELETE FROM KODEVERK WHERE KODE='FAGSAK_ARSAK';
DELETE FROM KODEVERK WHERE KODE='GEOGRAFI';
DELETE FROM KODEVERK WHERE KODE='GEOPOLITISKE_REGIONER';
DELETE FROM KODEVERK WHERE KODE='JOURNAL_STATUS';
DELETE FROM KODEVERK WHERE KODE='OPPHOLDSTILLATELSE';

DELETE FROM KODELISTE WHERE KODEVERK='ANDRE_YTELSER';
DELETE FROM KODEVERK WHERE KODE='ANDRE_YTELSER';
DELETE FROM KODELISTE WHERE KODEVERK='PERIODE_TYPE';
DELETE FROM KODEVERK WHERE KODE='PERIODE_TYPE';
DELETE FROM KODELISTE WHERE KODEVERK='OVERTA_KVOTE_FAR_MEDMOR_AARSAK';
DELETE FROM KODEVERK WHERE KODE='OVERTA_KVOTE_FAR_MEDMOR_AARSAK';
DELETE FROM KODELISTE WHERE KODEVERK='OVERTA_KVOTE_MOR_AARSAK';
DELETE FROM KODEVERK WHERE KODE='OVERTA_KVOTE_MOR_AARSAK';
DELETE FROM KODELISTE WHERE KODEVERK='UTSENDING_KANAL';
DELETE FROM KODEVERK WHERE KODE='UTSENDING_KANAL';
DELETE FROM KODELISTE WHERE KODEVERK='UTSETTELSE_AARSAK';
DELETE FROM KODEVERK WHERE KODE='UTSETTELSE_AARSAK';
DELETE FROM KODELISTE WHERE KODEVERK='UTSETTELSE_GRADERING_KVOTE';
DELETE FROM KODEVERK WHERE KODE='UTSETTELSE_GRADERING_KVOTE';
DELETE FROM KODELISTE WHERE KODEVERK='RELATERT_YTELSE_BEH_TEMA';
DELETE FROM KODEVERK WHERE KODE='RELATERT_YTELSE_BEH_TEMA';
-- Kommentert ut siden det fantes avhengigheter som ikke ble oppdaget i basen som er blitt reinstalert. Flyttet til 2.9.8
-- DELETE FROM KODELISTE WHERE KODEVERK='RELATERTE_YTELSER_STATUS';
-- DELETE FROM KODEVERK WHERE KODE='RELATERTE_YTELSER_STATUS';

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'BEREGNINGSGRUNNLAG_ANDELTYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'BEREGNINGSGRUNNLAG_TILSTAND', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'HISTORIKKINNSLAG_FELT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'HISTORIKK_AVKLART_SOEKNADSPERIODE_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'HISTORIKK_BEGRUNNELSE_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'HISTORIKK_ENDRET_FELT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'HISTORIKK_ENDRET_FELT_VERDI_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'HISTORIKK_OPPLYSNING_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'HISTORIKK_RESULTAT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'INNTEKTS_FILTER', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'INNTEKTS_FORMAAL', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'IVERKSETTING_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'NATURAL_YTELSE_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'SATS_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'SOKNAD_TYPE_TILLEGG', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'TYPE_FISKE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'VEDTAK_RESULTAT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'VILKAR_KATEGORI', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'UTTAK_DOKUMENTASJON_KLASSE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '1022', '1022', '1022', 'VILKAR_UTFALL_MERKNAD', to_date('2000-01-01', 'YYYY-MM-DD'));

UPDATE KODELISTE SET KODE='SAMTIDIGUTTAK' WHERE KODE='SAMMTIDIGUTTAK';


-- Det skal finnes migrering for disse, men hvis det trengs er dette verdiene til 'RELATERT_YTELSE_BEH_TEMA' og 'RELATERTE_YTELSER_STATUS'

-- insert into KODEVERK (kode, navn, beskrivelse, kodeverk_eier )
-- values ('RELATERT_YTELSE_BEH_TEMA', 'RelatertYtelseBehandlingTema', '', 'Arena');
-- INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'AAP', 'Arbeidsavklaringspenger', 'Arbeidsavklaringspenger', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'FISK', 'Dagp. v/perm fra fiskeindustri', 'Dagp. v/perm fra fiskeindustri', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'PERM', 'Dagpenger under permitteringer', 'Dagpenger under permitteringer', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'LONN', 'Lønnsgarantimidler - dagpenger', 'Lønnsgarantimidler - dagpenger', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'DAGO', 'Ordinære dagpenger', 'Ordinære dagpenger', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'BASI', 'Tiltakspenger (basisytelse før 2014)', 'Tiltakspenger (basisytelse før 2014)', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'OG', 'overgangsstønad', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'UT', 'skolepenger', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'ARAN', 'Andre behandlingstema Arena', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'FØ', 'Foreldrepenger fødsel', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'AP', 'Foreldrepenger adopsjon', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'SV', 'Svangerskapspenger', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'AE', 'Adopsjon engangsstønad', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'FE', 'Fødsel engangsstønad', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'FU', 'Foreldrepenger fødsel, utland', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'RS', 'forsikr.risiko sykefravær', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'RT', 'reisetilskudd', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'SP', 'sykepenger', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'SU', 'sykepenger utenlandsopphold', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'BT', 'stønad til barnetilsyn', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'FL', 'tilskudd til flytting', 'RELATERT_YTELSE_BEH_TEMA', to_date('2000-01-01', 'YYYY-MM-DD'));

-- insert into KODEVERK (kode, navn, beskrivelse, kodeverk_eier )
-- values ('RELATERTE_YTELSER_STATUS', 'RelaterteYtelserStatus', '', 'Arena');
-- INSERT INTO KODELISTE (id, kode, navn, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'IKKE_INNHENTET', 'Ikke innhentet', 'RELATERTE_YTELSER_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'UNDER_INNHENTING', 'Under innhenting', 'RELATERTE_YTELSER_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));
-- INSERT INTO KODELISTE (id, kode, navn, kodeverk, gyldig_fom)
-- VALUES (seq_kodeliste.nextval, 'INNHENTET', 'Innhentet', 'RELATERTE_YTELSER_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));
