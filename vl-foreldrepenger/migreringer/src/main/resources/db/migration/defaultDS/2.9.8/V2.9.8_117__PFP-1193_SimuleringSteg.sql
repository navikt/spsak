insert into BEHANDLING_STEG_TYPE(KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE, OPPRETTET_AV)
VALUES ('SIMOPP', 'Simuler oppdrag', 'UTRED', 'Send vedtak til simulering i oppdrag.', 'VL');

INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr, fagsak_ytelse_type)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-002', 'SIMOPP', 165, 'FP');

INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr, fagsak_ytelse_type)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'SIMOPP', 165, 'FP');
