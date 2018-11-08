insert into BEHANDLING_STEG_TYPE(KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE, OPPRETTET_AV)
VALUES ('KOFAKBER', 'Kontroller fakta for beregning', 'UTRED', 'Kontroller fakta for beregning.', 'VL');

INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr, fagsak_ytelse_type)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-002', 'KOFAKBER', 98, 'FP');
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr, fagsak_ytelse_type)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'KOFAKBER', 98, 'FP');

INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom)
VALUES
  (seq_kodeliste.nextval, 'SKJERMLENKE_TYPE', 'FAKTA_OM_BEREGNING', 'Fakta om beregning', 'Fakta om beregning', to_date('2018-03-15', 'YYYY-MM-DD'));

INSERT INTO VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type)
VALUES ('FORS_BERGRUNN.INN', 'Foreslå beregningsgrunnlag - Inngang', 'FORS_BERGRUNN', 'INN');


INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, OPPRETTET_AV, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT, SKJERMLENKE_TYPE)
VALUES ('5046', 'Vurder tidsbegrenset arbeidsforhold', 'FORS_BERGRUNN.INN', 'Vurder om bruker har ett eller flere tidsbegrensede arbeidsforhold', 'VL', 'FP_VK_41', 'J', 'FAKTA_OM_BEREGNING');


INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, OPPRETTET_AV, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT, SKJERMLENKE_TYPE)
VALUES ('5047', 'Fastsett beregningsgrunnlag for tidsbegrenset arbeidsforhold', 'FAST_BERGRUNN.INN', 'Fastsett beregningsgrunnlag for tidsbegrenset arbeidsforhold', 'VL', 'FP_VK_41', 'J', 'BEREGNING_FORELDREPENGER');

ALTER TABLE BEREGNINGSGRUNNLAG_PERIODE ADD KL_PERIODE_AARSAK VARCHAR(100) generated always as ('PERIODE_AARSAK') virtual;
