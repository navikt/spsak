INSERT INTO BEHANDLING_STEG_TYPE (kode, navn, behandling_status_def, beskrivelse)
VALUES ('KOFAK_LOP_MEDL', 'Kontroller løpende medlemskap', 'UTRED', 'Kontroller løpende medlemskap i revuderingskontekst.');

Insert into BEHANDLING_TYPE_STEG_SEKV (ID, BEHANDLING_TYPE, BEHANDLING_STEG_TYPE, SEKVENS_NR, FAGSAK_YTELSE_TYPE)
values (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'KOFAK_LOP_MEDL', '114', 'FP');
