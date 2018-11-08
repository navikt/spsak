-- Steg
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr, fagsak_ytelse_type)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'REGSØK', 25, 'FP');
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr, fagsak_ytelse_type)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'INSØK', 30, 'FP');

-- BEHANDLING_AARSAK
update kodeliste set kode = 'RE_END_FRA_BRUKER', navn = 'RE_END_FRA_BRUKER' where kode = 'RE_ENDRING_FRA_BRUKER' and KODEVERK = 'BEHANDLING_AARSAK';
