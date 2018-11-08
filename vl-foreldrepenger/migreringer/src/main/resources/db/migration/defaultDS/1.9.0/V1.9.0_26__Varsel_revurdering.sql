UPDATE behandling_type SET navn='Revurdering' where kode='BT-004';
UPDATE behandling_type SET navn='Førstegangsbehandling' where kode='BT-002';
DELETE FROM behandling_type WHERE kode='BT-001';

INSERT INTO Behandling_steg_type(kode, navn, behandling_status_def, beskrivelse)
VALUES ('VRSLREV', 'Varsel om revurdering', 'UTRED', 'Vurder om det skal sendes varsel om revurdering');

INSERT INTO VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type)
VALUES ('VRSLREV.UT', 'Varsel om revurdering - Utgang', 'VRSLREV', 'UT');

INSERT INTO VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type)
VALUES ('VRSLREV.INN', 'Varsel om revurdering - Inngang', 'VRSLREV', 'INN');


INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, AKSJONSPUNKT_KATEGORI, VURDERINGSPUNKT, ALLTID_TOTRINNSBEHANDLING, BESKRIVELSE)
VALUES ('5025', 'Varsel om revurdering ved automatisk etterkontroll', 'APK-002', 'VRSLREV.UT', 'N', 'Ta stilling til om varsel skal sendes etter automatisk etterkontroll.');

INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, AKSJONSPUNKT_KATEGORI, VURDERINGSPUNKT, ALLTID_TOTRINNSBEHANDLING, BESKRIVELSE)
VALUES ('5026', 'Varsel om revurdering', 'APK-002', 'VRSLREV.UT', 'N', 'Ta stilling til om varsel skal sendes etter manuelt opprettet revurdering.');

INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, AKSJONSPUNKT_KATEGORI, VURDERINGSPUNKT, FRIST_PERIODE, ALLTID_TOTRINNSBEHANDLING, BESKRIVELSE, AKSJONSPUNKT_TYPE)
VALUES ('7005', 'Satt på vent etter varsel om revurdering', 'APK-002', 'VRSLREV.UT', 'P4W', 'N', 'Satt på vent etter at det har blitt sendt ut varsel om revurdering.', 'AUTO');


INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'VRSLREV', 1);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'INSAK', 2);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'KOFAK', 3);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'VURDEROP', 4);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'VURDERBV', 5);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'VURDERMV', 6);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'VURDERSFV', 7);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'BERYT', 8);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'FORVEDSTEG', 9);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'FVEDSTEG', 10);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'IVEDSTEG', 11);
