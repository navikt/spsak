-- Introdusere nytt steg: Registrer søknad
INSERT INTO BEHANDLING_STEG_TYPE(kode, navn, behandling_status_def, beskrivelse)
VALUES ('VURDERSAMLET', 'Vurder vilkår samlet', 'UTRED', 'Vurder alle vilkårne samlet som under et inngangsvilkår');
INSERT INTO VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type)
VALUES ('VURDERSAMLET.INN', 'Vurder vilkår samlet - Inngang', 'VURDERSAMLET', 'INN');
INSERT INTO VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type)
VALUES ('VURDERSAMLET.UT', 'Vurder vilkår samlet - Utgang', 'VURDERSAMLET', 'UT');

-- Sett inn steg i eksisterende rekkefølge
UPDATE BEHANDLING_TYPE_STEG_SEKV
SET sekvens_nr = sekvens_nr + 1
  where SEKVENS_NR > (SELECT SEKVENS_NR
                      FROM BEHANDLING_TYPE_STEG_SEKV
                      WHERE BEHANDLING_STEG_TYPE = 'VURDERSFV' AND BEHANDLING_TYPE = 'BT-002');
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-002', 'VURDERSAMLET',
        (SELECT SEKVENS_NR
               FROM BEHANDLING_TYPE_STEG_SEKV
               WHERE BEHANDLING_STEG_TYPE = 'VURDERSFV' AND BEHANDLING_TYPE = 'BT-002'));
