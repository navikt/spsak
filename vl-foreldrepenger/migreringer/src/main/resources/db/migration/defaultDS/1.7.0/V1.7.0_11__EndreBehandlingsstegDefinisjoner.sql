-- Endre stegnavn: Avklare fakta -> Kontroller fakta
insert into BEHANDLING_STEG_TYPE(KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE, OPPRETTET_AV)
VALUES ('KOFAK', 'Kontroller Fakta', 'UTRED', 'Kontroller fakta som skal benyttes videre i behandling av en sak. Utføres rett etter innhentsaksopplysninger.', 'VL');

INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN, BESKRIVELSE, OPPRETTET_AV)
VALUES ('KOFAK.INN', 'KOFAK', 'INN', 'Kontroller fakta - Inngang', null, 'VL');
INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN, BESKRIVELSE, OPPRETTET_AV)
VALUES ('KOFAK.UT', 'KOFAK', 'UT', 'Kontroller fakta - Utgang', null, 'VL');

UPDATE BEHANDLING_TYPE_STEG_SEKV SET
  BEHANDLING_STEG_TYPE = 'KOFAK'
where SEKVENS_NR = 2;

UPDATE AKSJONSPUNKT_DEF SET
  VURDERINGSPUNKT = 'KOFAK.INN'
where VURDERINGSPUNKT = 'AVFAK.INN';
UPDATE AKSJONSPUNKT_DEF SET
  VURDERINGSPUNKT = 'KOFAK.UT'
where VURDERINGSPUNKT = 'AVFAK.UT';

UPDATE AKSJONSPUNKT SET
  BEHANDLING_STEG_FUNNET = 'KOFAK'
where BEHANDLING_STEG_FUNNET = 'AVFAK';

delete from VURDERINGSPUNKT_DEF where KODE = 'AVFAK.INN';
delete from VURDERINGSPUNKT_DEF where KODE = 'AVFAK.UT';
delete from BEHANDLING_STEG_TYPE where KODE = 'AVFAK';

-- Endre stegnavn: Vurdere inngangsvilkår -> Vurdere valgt inngangsvilkår
INSERT INTO BEHANDLING_STEG_TYPE(kode, navn, behandling_status_def, beskrivelse)
VALUES ('VUBIN', 'Vurder betinget inngangsvilkår', 'UTRED', 'Vurdering av valgt inngangsvilkår for å oppnå rett til en ytelse, med evt. tilhørende kvoter');

INSERT INTO VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type)
VALUES ('VUBIN.INN', 'Vurder betinget inngangsvilkår - Inngang', 'VUBIN', 'INN');
INSERT INTO VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type)
VALUES ('VUBIN.UT', 'Vurder betinget inngangsvilkår - Utgang', 'VUBIN', 'UT');

UPDATE BEHANDLING_TYPE_STEG_SEKV SET
  BEHANDLING_STEG_TYPE = 'VUBIN'
where SEKVENS_NR = 3;

UPDATE AKSJONSPUNKT_DEF SET
  VURDERINGSPUNKT = 'VUBIN.INN'
where VURDERINGSPUNKT = 'VUIN.INN';
UPDATE AKSJONSPUNKT_DEF SET
  VURDERINGSPUNKT = 'VUBIN.UT'
where VURDERINGSPUNKT = 'VUIN.UT';

delete from VURDERINGSPUNKT_DEF where KODE = 'VUIN.INN';
delete from VURDERINGSPUNKT_DEF where KODE = 'VUIN.UT';
delete from BEHANDLING_STEG_TYPE where KODE = 'VUIN';


-- Introdusere nytt steg: Vurdere felles inngangsvilkår
INSERT INTO BEHANDLING_STEG_TYPE(kode, navn, behandling_status_def, beskrivelse)
VALUES ('VUFIN', 'Vurder felles inngangsvilkår', 'UTRED', 'Vurdering av felles inngangsvilkår for å oppnå rett til en ytelse, med evt. tilhørende kvoter');

INSERT INTO VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type)
VALUES ('VUFIN.INN', 'Vurder felles inngangsvilkår - Inngang', 'VUFIN', 'INN');
INSERT INTO VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type)
VALUES ('VUFIN.UT', 'Vurder felles inngangsvilkår - Utgang', 'VUFIN', 'UT');

UPDATE BEHANDLING_TYPE_STEG_SEKV SET sekvens_nr = sekvens_nr + 1 WHERE sekvens_nr >= 4;
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-002', 'VUFIN', 4);

UPDATE AKSJONSPUNKT_DEF
SET VURDERINGSPUNKT = 'VUBIN.UT'
WHERE NAVN IN (
  'Manuell vurdering av omsorgsvilkåret'
);
UPDATE AKSJONSPUNKT_DEF
SET VURDERINGSPUNKT = 'VUFIN.UT'
WHERE NAVN IN (
  'Manuell vurdering av søknadsfristvilkåret'
, 'Manuell vurdering av medlemskapsvilkåret'
);

UPDATE AKSJONSPUNKT_DEF SET VILKAR_TYPE = 'FP_VK_5' WHERE KODE = 5008;

INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, AKSJONSPUNKT_KATEGORI, VURDERINGSPUNKT, BESKRIVELSE, VILKAR_TYPE)
VALUES ('5011', 'Manuell vurdering av omsorgsvilkåret', 'APK-002', 'VUBIN.UT', 'Vurder om krav til omsorg §14-7, 3. ledd er oppfylt', 'FP_VK_5');

