-- Tabell LAGRET_VEDTAK_TYPE
CREATE TABLE LAGRET_VEDTAK_TYPE (
    kode            VARCHAR2(20 CHAR) NOT NULL,
    navn            VARCHAR2(40 CHAR) NOT NULL,
    beskrivelse     VARCHAR2(4000 CHAR),
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
    CONSTRAINT PK_LAGRET_VEDTAK_TYPE PRIMARY KEY (kode)
);

INSERT INTO LAGRET_VEDTAK_TYPE (kode, navn) VALUES ('FODSEL', 'Fodsel');
INSERT INTO LAGRET_VEDTAK_TYPE (kode, navn) VALUES ('ADOPSJON', 'Adopsjon');


-- Tabell LAGRET_VEDTAK
CREATE TABLE LAGRET_VEDTAK (
    id                  NUMBER(19, 0) NOT NULL,
    fagsak_id           NUMBER(19) NOT NULL,
    behandling_id       NUMBER(19) NOT NULL,
    xml_clob            CLOB NOT NULL,
    LAGRET_VEDTAK_TYPE  VARCHAR2(20 char) NOT NULL,
    versjon             NUMBER(19) DEFAULT 0 NOT NULL,
    opprettet_av        VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid       TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av           VARCHAR2(20 CHAR),
    endret_tid          TIMESTAMP(3),
    CONSTRAINT PK_LAGRET_VEDTAK PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_LAGRET_VEDTAK MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE INDEX IDX_LAGRET_VEDTAK_1 ON LAGRET_VEDTAK(lagret_vedtak_type);
