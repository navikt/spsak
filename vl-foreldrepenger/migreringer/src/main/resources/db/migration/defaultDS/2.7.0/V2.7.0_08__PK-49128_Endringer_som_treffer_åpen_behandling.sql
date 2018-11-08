INSERT INTO KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK)
VALUES (seq_kodeliste.nextval, 'INNTEKTSMELDING', 'Inntektsmelding', 'Dokumentkoder av type Søknad', 'DOKUMENT_GRUPPE');

INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr, fagsak_ytelse_type)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'VURDERKOMPLETT', 35, 'FP');

insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK) values (seq_kodeliste.nextval, 'RE-END-INNTEKTSMELD', 'RE-END-INNTEKTSMELD', 'Endret inntektsmelding"', 'BEHANDLING_AARSAK');
drop index IDX_BEHANDLING_ARSAK_1;

-- TODO(PK-49128): Skal gjøres i annen historie --> FJERN
DELETE FROM BEHANDLING_TYPE_STEG_SEKV WHERE BEHANDLING_TYPE='BT-004' and FAGSAK_YTELSE_TYPE='FP' and BEHANDLING_STEG_TYPE='VRSLREV';

-- 7008 - Satt på vent pga for tidlig søknad - flyttes til senere steg
UPDATE AKSJONSPUNKT_DEF
SET VURDERINGSPUNKT = 'VURDERKOMPLETT.UT'
WHERE KODE = 7008;
-- Autopunkter som kan kjøre steget sitt flere ganger
UPDATE AKSJONSPUNKT_DEF
SET TILBAKEHOPP_VED_GJENOPPTAKELSE = 'J'
WHERE KODE IN (7003, 7008);

INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT, AKSJONSPUNKT_TYPE, TILBAKEHOPP_VED_GJENOPPTAKELSE, FRIST_PERIODE, SKJERMLENKE_TYPE)
VALUES ('7009', 'Vent på oppdatering som passerer kompletthetssjekk', 'FVEDSTEG.INN',
        'Venter på oppdatering på åpen behandling som passerer kompletthetssjekk.',
        '-', 'N', 'AUTO', 'N', NULL, '-');

INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom)
VALUES
  (seq_kodeliste.nextval, 'VENT_AARSAK', 'OPPD_ÅPEN_BEH', 'Venter på oppdatering av åpen behandling', 'Venter på oppdatering av åpen behandling', to_date('2018-03-15', 'YYYY-MM-DD'));
