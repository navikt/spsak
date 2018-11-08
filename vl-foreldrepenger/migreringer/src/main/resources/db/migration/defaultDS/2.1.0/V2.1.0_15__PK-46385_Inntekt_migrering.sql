INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES
  (seq_kodeliste.nextval, 'INNTEKTSPOST_TYPE', 'YTELSE', 'YTELSE', 'Ytelse', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES
  (seq_kodeliste.nextval, 'INNTEKTSPOST_TYPE', 'LONN', 'LONN', 'Lønn', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES
  (seq_kodeliste.nextval, 'INNTEKTS_KILDE', 'INNTEKT', 'INNTEKT', 'Inntekt', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));

-- Etabler manglende kodeverk / kodeliste for pensjonTrygd og offentligytelse
INSERT INTO KODEVERK (kode, kodeverk_eier, kodeverk_eier_navn, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse, sammensatt)
VALUES ('PENSJON_TRYGD_BESKRIVELSE', 'Kodeverkforvaltning', 'PensjonEllerTrygdeBeskrivelse', 'http://nav.no/kodeverk/Kode/PensjonEllerTrygdeBeskrivelse', 6, 'J', 'J', 'Pensjon Eller Trygde Beskrivelse', 'Beskrivelse av pensjon eller trygde beskrivelse', 'J');
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', '-', NULL, 'Undefined', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'ALDERSPENSJON', 'alderspensjon', 'Alderspensjon', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'ANNET', 'annet', 'Annet', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'AFP', 'avtalefestetPensjon', 'Avtalefestet pensjon', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'BARNEPENSJON', 'barnepensjon', 'Barnepensjon', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'BARNEPENSJON_ANDRE', 'barnepensjonFraAndreEnnFolketrygden', 'Barnepensjon fra andre enn folketrygden', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'BIL', 'bil', 'Bil', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'BOLIG', 'bolig', 'Bolig', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'EKTEFELLE', 'ektefelletillegg', 'Ektefelletillegg', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'ELEKTRONISK_KOMMUNIKASJON', 'elektroniskKommunikasjon', 'Elektronisk kommunikasjon', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'INNSKUDDS_ENGANGS', 'engangsutbetalingInnskuddspensjon', 'Engangsutbetaling innskuddspensjon', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'ETTERLATTE_PENSJON', 'etterlattepensjon', 'Etterlatte pensjon', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'ETTERLØNN', 'etterloenn', 'Etterlønn', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'ETTERLØNN_OG_ETTERPENSJON', 'etterloennOgEtterpensjon', 'Etterlønn og etterpensjon', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'FØDERÅD', 'foederaad', 'Føderåd', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'INTRODUKSJONSSTØNAD', 'introduksjonsstoenad', 'Introduksjonsstønad', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'IPA_IPS_BARNEPENSJON', 'ipaEllerIpsBarnepensjon', 'Ipa eller ips barnepensjon', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'IPA_IPS_ENGANGSUTBETALING', 'ipaEllerIpsEngangsutbetaling', 'Ipa eller ips engangsutbetaling', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'IPA_IPS_PERIODISKE', 'ipaEllerIpsPeriodiskeYtelser', 'Ipa eller ips periodiske ytelser', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'IPA_IPS_UFØRE', 'ipaEllerIpsUfoerepensjon', 'Ipa eller ips uførepensjon', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'KRIGSPENSJON', 'krigspensjon', 'Krigspensjon', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'KVALIFISERINGSSTØNAD', 'kvalifiseringstoenad', 'Kvalifiseringsstønad', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'NY_AFP', 'nyAvtalefestetPensjonPrivatSektor', 'Ny avtalefestet pensjon privat sektor', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'NYE_LIVRENTER', 'nyeLivrenterIArbeidsforholdOgLivrenterFortsettelsesforsikringer', 'Nye livrenter i arbeidsforhold og livrenter fortsettelsesforsikringer', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'OVERGANGSSTØNAD_ENSLIG', 'overgangsstoenadTilEnsligMorEllerFarSomBegynteAaLoepe31Mars2014EllerTidligere', 'Overgangsstønad til enslig mor eller far', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'OVERGANGSSTØNAD_EKTEFELLE', 'overgangsstoenadTilGjenlevendeEktefelle', 'Overgangsstønad til gjenlevende ektefelle', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'PENSJON_DØDSMÅNED', 'pensjonIDoedsmaaneden', 'Pensjon i dødsmåned', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'LIVRENTER', 'pensjonOgLivrenterIArbeidsforhold', 'Pensjon og livrenter i arbeidsforhold', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'RENTEFORDEL_LÅN', 'rentefordelLaan', 'Rentefordel lån', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'SUPPLERENDE_STØNAD', 'supplerendeStoenadTilPersonKortBotidNorge', 'Supplerende stønad til person med kort botid i Norge', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'UFØREPENSJON', 'ufoerepensjon', 'Uførepensjon', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'UFØREPENSJON_ANDRE', 'ufoerepensjonFraAndreEnnFolketrygden', 'Uførepensjon fra andre enn folketrygden', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'UFØREPENSJON_ANDRE_ETTEROPPGJØR', 'ufoereytelseEtteroppgjoerFraAndreEnnFolketrygden', 'Uførepensjon etteroppgjør fra andre enn folketrygden', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PENSJON_TRYGD_BESKRIVELSE', 'UNDERHOLDNINGSBIDRAG', 'underholdsbidragTilTidligereEktefelle', 'Underholdningsbidrag tidligere ektefelle', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));

INSERT INTO KODEVERK (kode, kodeverk_eier, kodeverk_eier_navn, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse, sammensatt)
VALUES ('YTELSE_FRA_OFFENTLIGE', 'Kodeverkforvaltning', 'YtelseFraOffentligeBeskrivelse', 'http://nav.no/kodeverk/Kode/YtelseFraOffentligeBeskrivelse', 4, 'J', 'J', 'Ytelser fra offentlige', 'Ytelse fra offentlige', 'J');
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'YTELSE_FRA_OFFENTLIGE', '-', null, 'UNDEFINED', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'YTELSE_FRA_OFFENTLIGE', 'AAP', 'arbeidsavklaringspenger', 'Arbeidsavklaringspenger', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'YTELSE_FRA_OFFENTLIGE', 'DAGPENGER_FISKER', 'dagpengerTilFiskerSomBareHarHyre', 'Dagpenger til fisker som bare har hyre', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'YTELSE_FRA_OFFENTLIGE', 'DAGPENGER_ARBEIDSLØS', 'dagpengerVedArbeidsloeshet', 'Dagpenger ved arbeidsløshet', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'YTELSE_FRA_OFFENTLIGE', 'FORELDREPENGER', 'foreldrepenger', 'Foreldrepenger', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'YTELSE_FRA_OFFENTLIGE', 'OVERGANGSSTØNAD_ENSLIG', 'overgangsstoenadTilEnsligMorEllerFarSomBegynteAaLoepe1April2014EllerSenere', 'Overgangsstønad til enslig mor eller far', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'YTELSE_FRA_OFFENTLIGE', 'SVANGERSKAPSPENGER', 'svangerskapspenger', 'Svangerskapspenger', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'YTELSE_FRA_OFFENTLIGE', 'SYKEPENGER', 'sykepenger', 'Sykepenger', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'YTELSE_FRA_OFFENTLIGE', 'SYKEPENGER_FISKER', 'sykepengerTilFiskerSomBareHarHyre', 'Sykepenger fisker', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'YTELSE_FRA_OFFENTLIGE', 'UFØRETRYGD', 'ufoeretrygd', 'Uføretrygd', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'YTELSE_FRA_OFFENTLIGE', 'UFØRETRYGD_ETTEROPPGJØR', 'ufoereytelseEtteroppgjoer', 'Uføretrygd etteroppgjør', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'YTELSE_FRA_OFFENTLIGE', 'UNDERHOLDNINGSBIDRAG_BARN', 'underholdsbidragTilBarn', 'Underholdningsbidrag til barn', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'YTELSE_FRA_OFFENTLIGE', 'VENTELØNN', 'venteloenn', 'Ventelønn', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));

-- Manglende kolonner
ALTER TABLE INNTEKTSPOST
  ADD (ytelse_type VARCHAR2(100), kl_ytelse_type VARCHAR2(100));
ALTER TABLE INNTEKTSPOST ADD
CONSTRAINT FK_INNTEKTSPOST_2 FOREIGN KEY (kl_ytelse_type, ytelse_type) REFERENCES KODELISTE (kodeverk, kode);

ALTER TABLE AKTOER_INNTEKT add BG_ID VARCHAR2(100);
ALTER TABLE AKTOER_ARBEID add BG_ID VARCHAR2(100);
ALTER TABLE TMP_INNTEKT add bg_ID VARCHAR2(100);
ALTER TABLE INNTEKTSPOST add bg_id VARCHAR2(100);
ALTER TABLE INNTEKTSPOST add orgnr VARCHAR2(100);
ALTER TABLE INNTEKTSPOST MODIFY INNTEKT_ID NULL;

-- myker opp nødvendige constraints
ALTER TABLE AKTOER_INNTEKT
  MODIFY INNTEKT_ARBEID_YTELSER_ID NULL;
ALTER TABLE AKTOER_ARBEID
  MODIFY INNTEKT_ARBEID_YTELSER_ID NULL;
ALTER TABLE VIRKSOMHET
  MODIFY NAVN NULL;
ALTER TABLE YRKESAKTIVITET
  MODIFY ARBEIDSFORHOLD_ID DEFAULT NULL NULL;

-- SEQ
CREATE SEQUENCE SEQ_TMP_INNTEKT
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50
  NOCACHE
  NOCYCLE;

INSERT INTO VIRKSOMHET (ID, OPPLYSNINGER_OPPDATERT_TID, ORGNR) (
  SELECT
    SEQ_VIRKSOMHET.nextval AS ID,
    sysdate,
    orgnr
  FROM (SELECT DISTINCT i.UTBETALER AS orgnr
        FROM INNTEKT i
        WHERE i.YTELSE = 'N')
);

INSERT INTO AKTOER_INNTEKT (ID, AKTOER_ID, BG_ID) (
  SELECT
    SEQ_AKTOER_INNTEKT.nextval AS ID,
    aktoer_id,
    bgid
    FROM (SELECT DISTINCT i.AKTOER_ID AS aktoer_id, i.BEHANDLING_GRUNNLAG_ID as bgid
        FROM INNTEKT i)
);

INSERT INTO AKTOER_ARBEID (ID, AKTOER_ID, BG_ID) (
  SELECT
    SEQ_AKTOER_ARBEID.nextval AS ID,
    aktoer_id,
    bgid
  FROM (SELECT DISTINCT i.AKTOER_ID AS aktoer_id, i.BEHANDLING_GRUNNLAG_ID as bgid
        FROM INNTEKT i)
);

INSERT INTO YRKESAKTIVITET (ID, ARBEID_TYPE, AKTOER_ARBEID_ID, ARBEIDSGIVER_VIRKSOMHET_ID)
  SELECT
    SEQ_YRKESAKTIVITET.nextval AS ID,
    'ORDINAERT_ARBEIDSFORHOLD' AS ARBEID_TYPE,
    aaid,
    vid
  FROM (SELECT
          distinct
          aa.id AS aaid,
          v.id  AS vid
      FROM INNTEKT i INNER JOIN VIRKSOMHET v ON v.ORGNR = i.UTBETALER
          INNER JOIN AKTOER_ARBEID aa ON aa.AKTOER_ID = i.AKTOER_ID
          INNER JOIN BEHANDLING_GRUNNLAG bg ON bg.id = aa.bg_id
        WHERE i.YTELSE = 'N');

INSERT INTO TMP_INNTEKT (ID, KILDE, AKTOER_INNTEKT_ID, YRKESAKTIVITET_ID, bg_ID)
  SELECT
    SEQ_TMP_INNTEKT.nextval AS ID,
    'INNTEKT'               AS KILDE,
    ai,
    y,
    bgid
  FROM (SELECT distinct
          ai.id AS ai,
          y.id  AS y,
          bg.id as bgid
        FROM INNTEKT i INNER JOIN AKTOER_INNTEKT ai ON i.AKTOER_ID = ai.AKTOER_ID
          INNER JOIN AKTOER_ARBEID aa ON aa.AKTOER_ID = i.AKTOER_ID
          INNER JOIN YRKESAKTIVITET y ON y.AKTOER_ARBEID_ID = aa.id
          INNER JOIN BEHANDLING_GRUNNLAG bg ON bg.id = aa.BG_ID);

INSERT INTO TMP_INNTEKT (ID, KILDE, AKTOER_INNTEKT_ID, bg_ID)
  SELECT
    SEQ_TMP_INNTEKT.nextval AS ID,
    'INNTEKT'               AS KILDE,
    ai,
    bgid
  FROM (SELECT distinct
          ai.id AS ai,
          bg.id as bgid
        FROM INNTEKT i INNER JOIN AKTOER_INNTEKT ai ON i.AKTOER_ID = ai.AKTOER_ID
          INNER JOIN AKTOER_ARBEID aa ON aa.AKTOER_ID = i.AKTOER_ID
          INNER JOIN BEHANDLING_GRUNNLAG bg ON bg.id = aa.BG_ID
        where BEHANDLING_GRUNNLAG_ID in (
          select BEHANDLING_GRUNNLAG_ID from INNTEKT where YTELSE = 'J'
          minus
          select BEHANDLING_GRUNNLAG_ID from INNTEKT where YTELSE = 'N'));

INSERT INTO TMP_INNTEKT (ID, KILDE, AKTOER_INNTEKT_ID, bg_ID)
  SELECT
    SEQ_TMP_INNTEKT.nextval AS ID,
    'INNTEKT'               AS KILDE,
    ai,
    bgid
  FROM (SELECT distinct
          ai.id AS ai,
          bg.id as bgid
        FROM INNTEKT i INNER JOIN AKTOER_INNTEKT ai ON i.AKTOER_ID = ai.AKTOER_ID
          INNER JOIN AKTOER_ARBEID aa ON aa.AKTOER_ID = i.AKTOER_ID
          INNER JOIN BEHANDLING_GRUNNLAG bg ON bg.id = aa.BG_ID
        where BEHANDLING_GRUNNLAG_ID in (select inn.BEHANDLING_GRUNNLAG_ID from INNTEKT inn
        WHERE inn.YTELSE = 'J' AND EXISTS
        (select * from INNTEKT inn2 where inn2.YTELSE = 'N'
                                          AND inn.BEHANDLING_GRUNNLAG_ID = inn2.BEHANDLING_GRUNNLAG_ID)));

INSERT INTO INNTEKTSPOST (ID, bg_id, INNTEKTSPOST_TYPE, FOM, TOM, BELOEP, orgnr)
  SELECT
    SEQ_INNTEKTSPOST.nextval AS ID,
    bgid,
    type,
    fom,
    tom,
    belop,
    orgnr
  FROM (SELECT
          ai.BG_ID   AS bgid,
          'LONN'  AS type,
          i.FOM   AS fom,
          i.TOM   AS tom,
          i.BELOP AS belop,
          i.UTBETALER as orgnr
        FROM INNTEKT i
          inner join AKTOER_INNTEKT ai
            on (ai.AKTOER_ID = i.AKTOER_ID and ai.BG_ID = i.BEHANDLING_GRUNNLAG_ID)
        where i.YTELSE = 'N');

DECLARE
  CURSOR C_TEST IS
    SELECT
      DISTINCT
      id as tmp_id,
      bg_id as bg_id,
      YRKESAKTIVITET_ID as y_id
    from TMP_INNTEKT;
BEGIN
  FOR LOOP_Y IN C_TEST LOOP
    update INNTEKTSPOST ip set ip.INNTEKT_ID = LOOP_Y.tmp_id
    where ip.BG_ID = LOOP_Y.bg_id and ip.ORGNR in(
      select v.orgnr from VIRKSOMHET v
        inner join YRKESAKTIVITET y
          on v.ID = y.ARBEIDSGIVER_VIRKSOMHET_ID
      where y.id = LOOP_Y.y_id);

  END LOOP;
END;
/


-- Sett kodeverkverdi for
INSERT INTO INNTEKTSPOST (ID, bg_id, INNTEKTSPOST_TYPE, FOM, TOM, ytelse_type, kl_ytelse_type, BELOEP)
  SELECT
    SEQ_INNTEKTSPOST.nextval AS ID,
    bgid,
    type,
    fom,
    tom,
    kode,
    kodeverk,
    belop
  FROM (SELECT
          ai.bg_id    AS bgid,
          'YTELSE' AS type,
          i.FOM    AS fom,
          i.TOM    AS tom,
          k.KODE AS kode,
          k.KODEVERK AS kodeverk,
          i.BELOP  AS belop
        FROM INNTEKT i
          inner join AKTOER_INNTEKT ai
            on (ai.AKTOER_ID = i.AKTOER_ID and ai.BG_ID = i.BEHANDLING_GRUNNLAG_ID)
          INNER JOIN KODELISTE k ON i.UTBETALER = k.OFFISIELL_KODE AND KODEVERK IN ('YTELSE_FRA_OFFENTLIGE', 'PENSJON_TRYGD_BESKRIVELSE')
        WHERE i.YTELSE = 'J'
  );

DECLARE
  CURSOR C_TEST IS
    SELECT
      DISTINCT
      id as tmp_id,
      bg_id as bg_id,
      YRKESAKTIVITET_ID as y_id
    from TMP_INNTEKT;
BEGIN
  FOR LOOP_Y IN C_TEST LOOP
    update INNTEKTSPOST ip set ip.INNTEKT_ID = LOOP_Y.tmp_id
    where ip.BG_ID = LOOP_Y.bg_id and LOOP_Y.y_id is null
          and ip.ORGNR is null;
  END LOOP;
END;
/


INSERT INTO INNTEKT_ARBEID_YTELSER (ID, INNTEKT_OPPLYSNINGER_ID)
  SELECT
    SEQ_INNTEKT_ARBEID_YTELSER.nextval AS ID,
    aiid
  FROM (select distinct bg.id as aiid from INNTEKT i
    inner join BEHANDLING_GRUNNLAG bg on bg.id = i.BEHANDLING_GRUNNLAG_ID);
DECLARE
  CURSOR C_TEST IS
    SELECT
      DISTINCT
      i.id as id,
      i.INNTEKT_OPPLYSNINGER_ID as bg_id
    from INNTEKT_ARBEID_YTELSER i;

BEGIN
  FOR LOOP_Y IN C_TEST LOOP
    update AKTOER_INNTEKT ai set ai.INNTEKT_ARBEID_YTELSER_ID = LOOP_Y.id
    where LOOP_Y.bg_id = ai.BG_ID;
  END LOOP;
END;
/
DECLARE
  CURSOR C_TEST IS
    SELECT
      DISTINCT
      i.id as id,
      i.INNTEKT_OPPLYSNINGER_ID as bg_id
    from INNTEKT_ARBEID_YTELSER i;

BEGIN
  FOR LOOP_Y IN C_TEST LOOP
    update AKTOER_ARBEID ai set ai.INNTEKT_ARBEID_YTELSER_ID = LOOP_Y.id
    where LOOP_Y.bg_id = ai.BG_ID;
  END LOOP;
END;
/

DECLARE
  CURSOR C_TEST IS
    SELECT
      DISTINCT
      b.id as bid,
      iay.id as iayid
    from INNTEKT_ARBEID_YTELSER iay
      INNER JOIN BEHANDLING_GRUNNLAG bg on iay.INNTEKT_OPPLYSNINGER_ID = bg.ID
      INNER JOIN BEHANDLING b on bg.ORIGINAL_BEHANDLING_ID = b.ID;
BEGIN
  FOR LOOP_Y IN C_TEST LOOP
    INSERT INTO GR_ARBEID_INNTEKT (ID, AKTIV, BEHANDLING_ID, INNTEKT_ARBEID_YTELSER_ID)
    VALUES (SEQ_GR_ARBEID_INNTEKT.nextval, 'J', LOOP_Y.bid, LOOP_Y.iayid);
  END LOOP;
END;
/

ALTER TABLE AKTOER_INNTEKT
  MODIFY INNTEKT_ARBEID_YTELSER_ID NOT NULL;
ALTER TABLE AKTOER_ARBEID
  MODIFY INNTEKT_ARBEID_YTELSER_ID NOT NULL;

--rydder opp
ALTER TABLE AKTOER_INNTEKT drop COLUMN BG_ID;
ALTER TABLE AKTOER_ARBEID drop COLUMN BG_ID;
ALTER TABLE TMP_INNTEKT drop COLUMN BG_ID;
ALTER TABLE INNTEKTSPOST drop COLUMN BG_ID;
ALTER TABLE INNTEKTSPOST drop COLUMN orgnr;

ALTER TABLE INNTEKTSPOST MODIFY INNTEKT_ID NOT NULL;
