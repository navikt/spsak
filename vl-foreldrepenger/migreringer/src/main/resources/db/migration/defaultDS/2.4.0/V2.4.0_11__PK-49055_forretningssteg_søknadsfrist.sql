INSERT INTO BEHANDLING_STEG_TYPE (kode, navn, behandling_status_def, beskrivelse)
VALUES ('SØKNADSFRIST_FP', 'Vurder søknadsfrist foreldrepenger', 'UTRED', 'Vurder om søknaden er mottatt innenfor søknadsfristen for foreldrepenger.');

INSERT INTO BEHANDLING_TYPE_STEG_SEKV (ID, BEHANDLING_TYPE, BEHANDLING_STEG_TYPE, SEKVENS_NR, FAGSAK_YTELSE_TYPE)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-002', 'SØKNADSFRIST_FP', 115,  'FP');
