INSERT INTO BEHANDLING_STEG_TYPE (kode, navn, behandling_status_def, beskrivelse)
VALUES ('VULOMED', 'Vurder løpende medlemskap', 'UTRED', 'Vurder løpende medlemskap');

Insert into BEHANDLING_TYPE_STEG_SEKV (ID, BEHANDLING_TYPE, BEHANDLING_STEG_TYPE, SEKVENS_NR, FAGSAK_YTELSE_TYPE)
values (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'VULOMED', '116', 'FP');
