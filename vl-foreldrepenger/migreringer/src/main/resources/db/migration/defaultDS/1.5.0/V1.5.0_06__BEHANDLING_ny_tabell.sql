''-- Tabell BEHANDLING_STEG_TYPE
CREATE TABLE BEHANDLING_STEG_TYPE (
    kode            VARCHAR2(20 CHAR) NOT NULL,
    navn            VARCHAR2(50 CHAR) NOT NULL,
    behandling_status_def VARCHAR2(20 CHAR) NOT NULL,
    beskrivelse     VARCHAR2(2000 CHAR),
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
    CONSTRAINT PK_BEHANDLING_STEG_TYPE PRIMARY KEY (kode)
);

COMMENT ON TABLE BEHANDLING_STEG_TYPE IS 'Angir definerte behandlingsteg med hvilket status behandling skal stå i når steget kjøres';
COMMENT ON COLUMN BEHANDLING_STEG_TYPE.KODE IS 'PK - angir unik kode som identifiserer behandlingssteget';
COMMENT ON COLUMN BEHANDLING_STEG_TYPE.NAVN IS 'Et lesbart navn for behandlingssteget, ment for visning el.';
COMMENT ON COLUMN BEHANDLING_STEG_TYPE.BEHANDLING_STATUS_DEF IS 'Definert status behandling settes i når steget kjøres';
COMMENT ON COLUMN BEHANDLING_STEG_TYPE.BESKRIVELSE IS 'Beskrivelse/forklaring av hva steget gjør'; 

INSERT INTO BEHANDLING_STEG_TYPE(kode, navn, behandling_status_def, beskrivelse) VALUES ('INSAK', 'Innhent Saksopplysninger', 'UTRED', 'Innhenting av saksopplysninger som vil benyttes til avklaring av fakta og vurdering av saken');
INSERT INTO BEHANDLING_STEG_TYPE(kode, navn, behandling_status_def, beskrivelse) VALUES ('AVFAK', 'Avklar Fakta', 'UTRED', 'Avklar fakta som skal benyttes i vurdering av en behandling for å etablere hvile fakta som skal benyttes når det er flere opplysninger relatert til samme forhold, eller må manuelt registreres.  Utføres normalt før Vurdering av Inngangsvilkår.');
INSERT INTO BEHANDLING_STEG_TYPE(kode, navn, behandling_status_def, beskrivelse) VALUES ('VUIN', 'Vurder inngangsvilkår', 'UTRED', 'Vurdering av inngangsvilkår for å oppnå Rett Til en ytelse, med evt. tilhørende kvoter');
INSERT INTO BEHANDLING_STEG_TYPE(kode, navn, behandling_status_def, beskrivelse) VALUES ('BERYT', 'Beregn ytelse', 'UTRED', 'Beregning av Tilkjent ytelse.  Forutsetter at Rett Til ytelse er innvilget');
INSERT INTO BEHANDLING_STEG_TYPE(kode, navn, behandling_status_def, beskrivelse) VALUES ('FVEDSTEG', 'Fatte Vedtak', 'FVED', 'Fatte vedtak for en behandling.  Kan være et avslag eller enn innvilgelse');
INSERT INTO BEHANDLING_STEG_TYPE(kode, navn, behandling_status_def, beskrivelse) VALUES ('IVEDSTEG', 'Iverksett Vedtak', 'IVED', 'Iverksett vedtak fra en behandling.  Forutsetter at et vedtak er fattet');


-- Tabell VURDERINGSPUNKT_DEF
CREATE TABLE VURDERINGSPUNKT_DEF (
    kode            VARCHAR2(20 CHAR) NOT NULL,
    behandling_steg VARCHAR2(20 CHAR),
    vurderingspunkt_type VARCHAR2(20 CHAR) DEFAULT 'UT' NOT NULL CHECK (vurderingspunkt_type IN ('UT', 'INN')),
    navn            VARCHAR2(50 CHAR) NOT NULL,
    beskrivelse     VARCHAR2(2000 CHAR),
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
    CONSTRAINT PK_VURDERINGSPUNKT_DEF PRIMARY KEY (kode)
);

ALTER TABLE VURDERINGSPUNKT_DEF ADD CONSTRAINT FK_VURDERINGSPUNKT_DEF_1 FOREIGN KEY (behandling_steg) REFERENCES BEHANDLING_STEG_TYPE;
CREATE INDEX IDX_VURDERINGSPUNKT_DEF_1 ON VURDERINGSPUNKT_DEF(behandling_steg);
CREATE UNIQUE INDEX UIDX_VURDERINGSPUNKT_DEF_1 ON VURDERINGSPUNKT_DEF(behandling_steg, vurderingspunkt_type);

INSERT INTO VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type) VALUES ('VUIN.INN', 'Vurder inngangsvilkår - Inngang', 'VUIN', 'INN');
INSERT INTO VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type) VALUES ('VUIN.UT', 'Vurder inngangsvilkår - Utgang', 'VUIN', 'UT');
INSERT INTO VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type) VALUES ('BERYT.INN', 'Beregn ytelse - Inngang', 'BERYT', 'INN');
INSERT INTO VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type) VALUES ('BERYT.UT', 'Beregn ytelse - Utgang', 'BERYT', 'UT');
INSERT INTO VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type) VALUES ('INSAK.INN', 'Innhent saksopplysninger - Inngang', 'INSAK', 'INN');
INSERT INTO VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type) VALUES ('INSAK.UT', 'Innhent saksopplysninger - Utgang', 'INSAK', 'UT');

-- Tabell BEHANDLING_STATUS
CREATE TABLE BEHANDLING_STATUS (
    kode            VARCHAR2(20 CHAR) NOT NULL,
    navn            VARCHAR2(50 CHAR) NOT NULL,
    beskrivelse     VARCHAR2(2000 CHAR),
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
    CONSTRAINT PK_BEHANDLING_STATUS PRIMARY KEY (kode)
);

COMMENT ON TABLE BEHANDLING_STATUS IS 'Angir definerte statuser en behandling kan være i (faglig sett). Statusene er definert av Forretning og Fag';
COMMENT ON COLUMN BEHANDLING_STATUS.KODE IS 'PK - angir unik kode som identifiserer en status';
COMMENT ON COLUMN BEHANDLING_STATUS.NAVN IS 'Et lesbart navn for status, ment for visning el.';
COMMENT ON COLUMN BEHANDLING_STATUS.BESKRIVELSE IS 'Beskrivelse/forklaring av hva statusen innebærer for en behandling'; 

INSERT INTO BEHANDLING_STATUS (kode, navn, beskrivelse) VALUES ('OPPRE', 'Opprettet', 'Behandling er opprettet');
INSERT INTO BEHANDLING_STATUS (kode, navn, beskrivelse) VALUES ('UTRED', 'Behandling utredes', 'Behandling er under utredning (vurdering, registrering, kontroll)');
INSERT INTO BEHANDLING_STATUS (kode, navn, beskrivelse) VALUES ('FVED', 'Fatter vedtak', 'Behandling er satt klar til å fatte et vedtak om inngvilgelse eller avslag');
INSERT INTO BEHANDLING_STATUS (kode, navn, beskrivelse) VALUES ('IVED', 'Iverksetter vedtak', 'Vedtak er klar til å iverksettes');
INSERT INTO BEHANDLING_STATUS (kode, navn, beskrivelse) VALUES ('AVSLU', 'Avsluttet', 'Behandlingen er avsluttet.  Ingen flere endringer kan utføres på behandlingen');

-- Tabell BEHANDLING_TYPE
CREATE TABLE BEHANDLING_TYPE (
    kode            VARCHAR2(20 CHAR) NOT NULL,
    navn            VARCHAR2(50 CHAR) NOT NULL,
    beskrivelse     VARCHAR2(2000 CHAR),
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
    CONSTRAINT PK_BEHANDLING_TYPE PRIMARY KEY (kode)
);

INSERT INTO BEHANDLING_TYPE (kode, navn) VALUES ('BT-001', 'Endringssøknad');
INSERT INTO BEHANDLING_TYPE (kode, navn) VALUES ('BT-002', 'Førstegangssøknad');
INSERT INTO BEHANDLING_TYPE (kode, navn) VALUES ('BT-003', 'Klage');
INSERT INTO BEHANDLING_TYPE (kode, navn) VALUES ('BT-004', 'Omgjøring');

-- Tabell BEHANDLING_TYPE_STEG_SEKV
CREATE TABLE BEHANDLING_TYPE_STEG_SEKV (
	  id             NUMBER(19) NOT NULL,
    behandling_type            VARCHAR2(20 CHAR) NOT NULL,
    behandling_steg_type       VARCHAR2(20 CHAR) NOT NULL,
    sekvens_nr      NUMBER(5,0) NOT NULL CHECK(sekvens_nr > 0),
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
    CONSTRAINT PK_BEHANDLING_TYPE_STEG_SEKV PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_BEHANDLING_TYPE_STEG_SEKV MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE UNIQUE INDEX UIDX_BEH_STEG_TYPE_SEKV_1 ON BEHANDLING_TYPE_STEG_SEKV (behandling_type, behandling_steg_type);
CREATE INDEX IDX_BEH_STEG_TYPE_SEKV_1 ON BEHANDLING_TYPE_STEG_SEKV (behandling_steg_type);

ALTER TABLE BEHANDLING_TYPE_STEG_SEKV ADD CONSTRAINT FK_BEHANDLING_TYPE_STEG_SEK_01 FOREIGN KEY (behandling_steg_type) REFERENCES BEHANDLING_STEG_TYPE;
ALTER TABLE BEHANDLING_TYPE_STEG_SEKV ADD CONSTRAINT FK_BEHANDLING_TYPE_STEG_SEK_02 FOREIGN KEY (behandling_type) REFERENCES BEHANDLING_TYPE;

INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr) VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-002', 'INSAK', 1);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr) VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-002', 'AVFAK', 2);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr) VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-002', 'VUIN', 3);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr) VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-002', 'BERYT', 4);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr) VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-002', 'FVEDSTEG', 5);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr) VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-002', 'IVEDSTEG', 6);

-- Tabell BEHANDLING
CREATE TABLE BEHANDLING (
    id             NUMBER(19) NOT NULL,
    fagsak_id      NUMBER(19) NOT NULL,
    behandling_status    VARCHAR2(20 CHAR) NOT NULL,
    behandling_type    VARCHAR2(20 CHAR) NOT NULL,
    behandling_steg    VARCHAR2(20 CHAR),
    opprettet_dato  date DEFAULT sysdate NOT NULL,
    avsluttet_dato  date,
    versjon              NUMBER(19) DEFAULT 0 NOT NULL,
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
    CONSTRAINT PK_BEHANDLING PRIMARY KEY (id)
);

ALTER TABLE BEHANDLING ADD CONSTRAINT FK_BEHANDLING_1 FOREIGN KEY (fagsak_id) REFERENCES FAGSAK;
ALTER TABLE BEHANDLING ADD CONSTRAINT FK_BEHANDLING_2 FOREIGN KEY (BEHANDLING_STATUS) REFERENCES BEHANDLING_STATUS;
ALTER TABLE BEHANDLING ADD CONSTRAINT FK_BEHANDLING_3 FOREIGN KEY (behandling_type) REFERENCES BEHANDLING_TYPE;
ALTER TABLE BEHANDLING ADD CONSTRAINT FK_BEHANDLING_4 FOREIGN KEY (behandling_steg) REFERENCES BEHANDLING_STEG_TYPE;

CREATE INDEX IDX_BEHANDLING_1 ON BEHANDLING(fagsak_id);
CREATE INDEX IDX_BEHANDLING_2 ON BEHANDLING(behandling_status);
CREATE INDEX IDX_BEHANDLING_3 ON BEHANDLING(behandling_type);
CREATE INDEX IDX_BEHANDLING_4 ON BEHANDLING(behandling_steg);

CREATE SEQUENCE SEQ_BEHANDLING MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

-- Tabell OPPGAVE_AARSAK
CREATE TABLE OPPGAVE_AARSAK (
    kode            VARCHAR2(20 CHAR) NOT NULL,
    navn            VARCHAR2(50 CHAR) NOT NULL,
    beskrivelse     VARCHAR2(2000 CHAR),
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
    CONSTRAINT PK_OPPGAVE_AARSAK PRIMARY KEY ( kode )
);
INSERT INTO OPPGAVE_AARSAK (kode, navn) VALUES ('BEH_SAK_VL', 'Behandle sak i VL');
INSERT INTO OPPGAVE_AARSAK (kode, navn) VALUES ('REG_SOK_VL', 'Registrere søknad i VL');
INSERT INTO OPPGAVE_AARSAK (kode, navn) VALUES ('GOD_VED_VL', 'Godkjenne vedtak i VL');
INSERT INTO OPPGAVE_AARSAK (kode, navn) VALUES ('RV_VL', 'Revurdere i VL');
INSERT INTO OPPGAVE_AARSAK (kode, navn) VALUES ('VUR_VL', 'Vurder dokument i VL');


CREATE TABLE OPPGAVE_BEHANDLING_KOBLING (
    id                NUMBER(19) NOT NULL,
    oppgave_aarsak    VARCHAR2(20 CHAR) NOT NULL,
    oppgave_id        VARCHAR2(50 CHAR),
    behandling_id     NUMBER(19) NOT NULL,
    saks_id           VARCHAR2(50 CHAR),
    versjon           NUMBER(19) DEFAULT 0 NOT NULL,
    opprettet_av      VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid     TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av         VARCHAR2(20 CHAR),
    endret_tid        TIMESTAMP(3),
    ferdigstilt       VARCHAR2(1 CHAR),
    ferdigstilt_av    VARCHAR2(20 CHAR),
    ferdigstilt_tid   TIMESTAMP(3),
    CONSTRAINT PK_OPPGAVE_BEHANDLING_KOBLING PRIMARY KEY ( id )
);
CREATE INDEX IDX_OPPGAVE_BEH_KOB_BEH_ID ON OPPGAVE_BEHANDLING_KOBLING ( behandling_id ASC );
ALTER TABLE OPPGAVE_BEHANDLING_KOBLING ADD CONSTRAINT FK_OPPGAVE_BEH_KOBLING_1 FOREIGN KEY ( oppgave_aarsak ) REFERENCES oppgave_aarsak ( kode );
ALTER TABLE OPPGAVE_BEHANDLING_KOBLING ADD CONSTRAINT FK_OPPGAVE_BEH_KOBLING_2 FOREIGN KEY ( behandling_id ) REFERENCES behandling ( id );
CREATE SEQUENCE SEQ_OPPGAVE_BEHANDLING_KOBLING MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;
