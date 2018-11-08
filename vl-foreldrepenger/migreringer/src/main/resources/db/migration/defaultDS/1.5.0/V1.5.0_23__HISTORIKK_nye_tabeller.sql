-- Tabell HISTORIKKINNSLAG_TYPE
CREATE TABLE HISTORIKKINNSLAG_TYPE (
  kode            VARCHAR2(20 CHAR) NOT NULL,
  navn            VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse     VARCHAR2(2000 CHAR),
  opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 CHAR),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_HISTORIKKINNSLAG_TYPE PRIMARY KEY (kode)
);

COMMENT ON TABLE HISTORIKKINNSLAG_TYPE IS 'Angir definerte typer av historikkinnslag';
COMMENT ON COLUMN HISTORIKKINNSLAG_TYPE.kode IS 'PK - unik kode som identifiserer typen historikkinnslag';
COMMENT ON COLUMN HISTORIKKINNSLAG_TYPE.navn IS 'Lesbart navn for typen historikkinnslag';
COMMENT ON COLUMN HISTORIKKINNSLAG_TYPE.beskrivelse IS 'Utdypende forklaring av typen';

INSERT INTO HISTORIKKINNSLAG_TYPE(kode, navn) VALUES ('BEH_STARTET', 'Søknad mottatt');
INSERT INTO HISTORIKKINNSLAG_TYPE(kode, navn) VALUES ('FORSLAG_VEDTAK', 'Vedtak foreslått og sendt til beslutter');
INSERT INTO HISTORIKKINNSLAG_TYPE(kode, navn) VALUES ('SAK_RETUR','Vedtak returnert');
INSERT INTO HISTORIKKINNSLAG_TYPE(kode, navn) VALUES ('VEDTAK_FATTET','Vedtak fattet og iverksatt');
INSERT INTO HISTORIKKINNSLAG_TYPE(kode, navn) VALUES ('VEDLEGG_MOTTATT','Dokument mottatt');
INSERT INTO HISTORIKKINNSLAG_TYPE(kode, navn) VALUES ('FAKTA_ENDRET', 'Saksopplysning/faktagrunnlag er endret');
INSERT INTO HISTORIKKINNSLAG_TYPE(kode, navn) VALUES ('OVERSTYRT','Vedtak foreslått og sendt til beslutter');
INSERT INTO HISTORIKKINNSLAG_TYPE(kode, navn) VALUES ('AVBRUTT_BEH', 'Behandlingen er henlagt');
INSERT INTO HISTORIKKINNSLAG_TYPE(kode, navn) VALUES ('BEH_VENT','Behandlingen er satt på vent med frist');
INSERT INTO HISTORIKKINNSLAG_TYPE(kode, navn) VALUES ('BEH_GJEN','Behandlingen er gjenopptatt');
INSERT INTO HISTORIKKINNSLAG_TYPE(kode, navn) VALUES ('BREV_SENT','Behandlingen er gjenopptatt');


-- Tabell HISTORIKK_AKTOER
CREATE TABLE HISTORIKK_AKTOER (
  kode            VARCHAR2(20 CHAR) NOT NULL,
  navn            VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse     VARCHAR2(2000 CHAR),
  opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 CHAR),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_HISTORIKK_AKTOER PRIMARY KEY (kode)
);

COMMENT ON TABLE HISTORIKK_AKTOER IS 'Angir definerte typer av aktører som kan opprette historikkinnslag';
COMMENT ON COLUMN HISTORIKK_AKTOER.kode IS 'PK - unik kode som identifiserer aktøren';
COMMENT ON COLUMN HISTORIKK_AKTOER.navn IS 'Lesbart navn for aktøren';
COMMENT ON COLUMN HISTORIKK_AKTOER.beskrivelse IS 'Utdypende forklaring av aktøren';

INSERT INTO HISTORIKK_AKTOER(kode, navn) VALUES ('BESL', 'Beslutter');
INSERT INTO HISTORIKK_AKTOER(kode, navn) VALUES ('SBH', 'Saksbehandler');
INSERT INTO HISTORIKK_AKTOER(kode, navn) VALUES ('SOKER', 'Søker');
INSERT INTO HISTORIKK_AKTOER(kode, navn) VALUES ('VL', 'Vedtaksløsningen');

-- Tabell HISTORIKKINNSLAG
CREATE TABLE HISTORIKKINNSLAG (
  id                       NUMBER(19, 0) NOT NULL,
  tekst                    VARCHAR2(2000 CHAR),
  behandling_id            NUMBER(19, 0) NOT NULL,
  historikk_aktoer_id      VARCHAR2(20 CHAR) NOT NULL,
  historikkinnslag_type_id VARCHAR2(20 CHAR) NOT NULL,
  bruker_id                VARCHAR2(20 CHAR),
  opprettet_av             VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid            TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                VARCHAR2(20 CHAR),
  endret_tid               TIMESTAMP(3),
  CONSTRAINT PK_HISTORIKKINNSLAG PRIMARY KEY (id),
  CONSTRAINT FK_BEHANDLING_ID FOREIGN KEY (behandling_id) REFERENCES BEHANDLING(id),
  CONSTRAINT FK_HISTORIKK_AKTOER_ID FOREIGN KEY (historikk_aktoer_id) REFERENCES HISTORIKK_AKTOER(kode),
  CONSTRAINT FK_HISTORIKKINNSLAG_TYPE FOREIGN KEY (historikkinnslag_type_id) REFERENCES HISTORIKKINNSLAG_TYPE(kode)
);

CREATE SEQUENCE SEQ_HISTORIKKINNSLAG MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE INDEX IDX_HISTORIKKINNSLAG_01 ON HISTORIKKINNSLAG(behandling_id);

COMMENT ON TABLE HISTORIKKINNSLAG IS 'Historikk over hendelser i saken';
COMMENT ON COLUMN HISTORIKKINNSLAG.tekst IS 'Tekst som beskriver hendelsen (som skal vises i historikkfanen)';
COMMENT ON COLUMN HISTORIKKINNSLAG.bruker_id IS 'Referens til ekstern bruker ident som laget innslaget';


-- Tabell HISTORIKKINNSLAG_DOK_LINK
CREATE TABLE HISTORIKKINNSLAG_DOK_LINK (
  id                  NUMBER(19, 0) NOT NULL,
  tag                 VARCHAR2(100 CHAR) NOT NULL,
  historikkinnslag_id NUMBER(19, 0) NOT NULL,
  journal_post_id     VARCHAR2(100 CHAR),
  dokument_id         VARCHAR2(100 CHAR),
  opprettet_av        VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid       TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av           VARCHAR2(20 CHAR),
  endret_tid          TIMESTAMP(3),
  CONSTRAINT PK_HISTORIKKINNSLAG_DOK_LINK PRIMARY KEY (id),
  CONSTRAINT FK_HISTORIKKINNSLAG_ID FOREIGN KEY (historikkinnslag_id) REFERENCES HISTORIKKINNSLAG(id)
);

CREATE SEQUENCE SEQ_HISTORIKKINNSLAG_DOK_LINK MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE INDEX IDX_HISTINNSLAG_DOK_LINK_01 ON HISTORIKKINNSLAG_DOK_LINK(historikkinnslag_id);

COMMENT ON TABLE HISTORIKKINNSLAG_DOK_LINK IS 'Kobling fra historikkinnslag til aktuell dokumentasjon';
COMMENT ON COLUMN HISTORIKKINNSLAG_DOK_LINK.tag IS 'Tekst som vises for link til dokumentet';
