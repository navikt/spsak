CREATE TABLE VURDER_PAA_NYTT_AARSAK_TYPE (
  kode           VARCHAR2(20 CHAR)                 NOT NULL,
  navn           VARCHAR2(50 CHAR)                 NOT NULL,
  sorteringsvekt NUMBER(19)                        NOT NULL,
  beskrivelse    VARCHAR2(2000 CHAR),
  opprettet_av   VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av      VARCHAR2(20 CHAR),
  endret_tid     TIMESTAMP(3),
  CONSTRAINT PK_VURDER_PAA_NYTT_AARSAK_TYPE PRIMARY KEY (kode)
);

INSERT INTO VURDER_PAA_NYTT_AARSAK_TYPE(kode, navn, sorteringsvekt) VALUES ('FEIL_FAKTA', 'Feil fakta',           0);
INSERT INTO VURDER_PAA_NYTT_AARSAK_TYPE(kode, navn, sorteringsvekt) VALUES ('FEIL_LOV',   'Feil lovanvendelse',   1);
INSERT INTO VURDER_PAA_NYTT_AARSAK_TYPE(kode, navn, sorteringsvekt) VALUES ('FEIL_REGEL', 'Feil regelforståelse', 2);
INSERT INTO VURDER_PAA_NYTT_AARSAK_TYPE(kode, navn, sorteringsvekt) VALUES ('ANNET',      'Annet',                3);

COMMENT ON TABLE VURDER_PAA_NYTT_AARSAK_TYPE IS 'Kodeverk over årsaker for revurdering';
COMMENT ON COLUMN VURDER_PAA_NYTT_AARSAK_TYPE.sorteringsvekt IS 'Index som angir hvilken rekkefølgene årsakene skal vises i GUI.';


CREATE TABLE VURDER_PAA_NYTT_AARSAK (
  id              NUMBER(19)                        NOT NULL,
  aarsak_type_id  VARCHAR2(20 CHAR)                 NOT NULL,
  aksjonspunkt_id NUMBER(19)                        NOT NULL,
  opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 CHAR),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_VURDER_PAA_NYTT_AARSAK PRIMARY KEY (id),
  CONSTRAINT FK_VURDER_PAA_NYTT_AARSAK_1 FOREIGN KEY (aksjonspunkt_id) REFERENCES AKSJONSPUNKT (id),
  CONSTRAINT FK_VURDER_PAA_NYTT_AARSAK_2 FOREIGN KEY (aarsak_type_id) REFERENCES VURDER_PAA_NYTT_AARSAK_TYPE (kode)
);

CREATE SEQUENCE SEQ_VURDER_PAA_NYTT_AARSAK MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;
COMMENT ON TABLE VURDER_PAA_NYTT_AARSAK IS 'Årsaken til at aksjonspunkt må vurderes på nytt';

ALTER TABLE BEHANDLING ADD totrinnsbehandling VARCHAR2(1 CHAR);
UPDATE BEHANDLING SET totrinnsbehandling = '0';
ALTER TABLE BEHANDLING MODIFY (totrinnsbehandling NOT NULL);
COMMENT ON COLUMN BEHANDLING.totrinnsbehandling IS 'Indikerer at behandlingen skal totrinnsbehandles';

ALTER TABLE BEHANDLING ADD ansvarlig_saksbehandler VARCHAR2(100 CHAR);
COMMENT ON COLUMN BEHANDLING.ansvarlig_saksbehandler IS 'Id til saksbehandler som oppretter forslag til vedtak ved totrinnsbehandling.';

ALTER TABLE AKSJONSPUNKT ADD totrinnsbehandling VARCHAR2(1 CHAR);
UPDATE AKSJONSPUNKT SET totrinnsbehandling = '0';
ALTER TABLE AKSJONSPUNKT MODIFY (totrinnsbehandling NOT NULL);
COMMENT ON COLUMN AKSJONSPUNKT.totrinnsbehandling IS 'Indikerer at aksjonspunkter krever en totrinnsbehandling';

ALTER TABLE AKSJONSPUNKT ADD beslutters_begrunnelse VARCHAR2(2000 CHAR);
COMMENT ON COLUMN AKSJONSPUNKT.beslutters_begrunnelse IS 'Beslutters begrunnelse for hvorfor et aksjonspunkt må vurderes på nytt';

ALTER TABLE AKSJONSPUNKT_DEF ADD alltid_totrinnsbehandling VARCHAR2(1 CHAR);
UPDATE AKSJONSPUNKT_DEF SET alltid_totrinnsbehandling = 'N';
ALTER TABLE AKSJONSPUNKT_DEF MODIFY (alltid_totrinnsbehandling NOT NULL);
COMMENT ON COLUMN AKSJONSPUNKT_DEF.alltid_totrinnsbehandling IS 'Indikerer om dette aksjonspunktet alltid skal kreve totrinnsbehandling';

INSERT INTO BEHANDLING_STATUS (kode, navn, beskrivelse) VALUES('FORVED', 'Foreslå vedtak', 'Totrinnskontroll av behandling.');
INSERT INTO BEHANDLING_STEG_TYPE(kode, navn, behandling_status_def, beskrivelse) VALUES('FORVEDSTEG', 'Foreslå vedtak', 'FORVED', 'Totrinnskontroll av behandling. Går rett til fatte vedtak dersom behandlingen ikke krever totrinnskontroll.');
UPDATE BEHANDLING_TYPE_STEG_SEKV SET sekvens_nr = 7 WHERE behandling_steg_type = 'FVEDSTEG';
UPDATE BEHANDLING_TYPE_STEG_SEKV SET sekvens_nr = 8 WHERE behandling_steg_type = 'IVEDSTEG';
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr) VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-002', 'FORVEDSTEG', 6);

INSERT INTO VURDERINGSPUNKT_DEF (kode, behandling_steg, vurderingspunkt_type, navn) values('FORVEDSTEG.INN', 'FORVEDSTEG', 'INN', 'Foreslå vedtak - Inngang');
INSERT INTO VURDERINGSPUNKT_DEF (kode, behandling_steg, vurderingspunkt_type, navn) values('FORVEDSTEG.UT', 'FORVEDSTEG', 'UT', 'Foreslå vedtak - Utgang');
INSERT INTO AKSJONSPUNKT_DEF (kode, navn, aksjonspunkt_kategori, vurderingspunkt, beskrivelse, alltid_totrinnsbehandling) VALUES('5015', 'Foreslå vedtak', 'APK-003', 'FORVEDSTEG.UT', 'Totrinnsbehandling. Saksbehandler kontroller opplysninger og sender forslag til vedtak for godkjenning', 'N');

INSERT INTO VURDERINGSPUNKT_DEF (kode, behandling_steg, vurderingspunkt_type, navn) values('FVEDSTEG.INN', 'FVEDSTEG', 'INN', 'Fatter vedtak - Inngang');
INSERT INTO VURDERINGSPUNKT_DEF (kode, behandling_steg, vurderingspunkt_type, navn) values('FVEDSTEG.UT', 'FVEDSTEG', 'UT', 'Fatter vedtak - Utgang');
INSERT INTO AKSJONSPUNKT_DEF (kode, navn, aksjonspunkt_kategori, vurderingspunkt, beskrivelse, alltid_totrinnsbehandling) VALUES('5016', 'Fatter vedtak', 'APK-003', 'FVEDSTEG.INN', 'Totrinnsbehandling. Beslutter kontroller opplysninger og godkjenner vedtak', 'N');

INSERT INTO PROSESS_TASK_TYPE (kode, navn, feil_maks_forsoek, feil_sek_mellom_forsoek, feilhandtering_algoritme, beskrivelse)
VALUES ('oppgavebehandling.opprettOppgaveGodkjennVedtak', 'Oppretter oppgave for godkjenning av vedtak i GSAK',  3, 60, 'DEFAULT', 'Task som oppretter oppgave for godkjenning av vedtak i GSAK');
