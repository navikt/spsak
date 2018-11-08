INSERT INTO BEHANDLING_STEG_TYPE(kode, navn, behandling_status_def, beskrivelse)
VALUES ('KOFAKUT', 'Kontroller fakta for uttak', 'UTRED', 'Kontroller fakta for uttak');

INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr, fagsak_ytelse_type)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-002', 'KOFAKUT', 117, 'FP');
