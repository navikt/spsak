-- Tabell VEDTAK_RESULTAT_TYPE
CREATE TABLE VEDTAK_RESULTAT_TYPE (
    kode            VARCHAR2(20 CHAR) NOT NULL,
    navn            VARCHAR2(40 CHAR) NOT NULL,
    beskrivelse     VARCHAR2(4000 CHAR),
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
    CONSTRAINT PK_VEDTAK_RESULTAT_TYPE PRIMARY KEY (kode)
);

INSERT INTO VEDTAK_RESULTAT_TYPE (kode, navn) VALUES ('INNVILGET', 'Innvilget');
INSERT INTO VEDTAK_RESULTAT_TYPE (kode, navn) VALUES ('AVSLAG', 'Avslag');

-- Tabell BEHANDLING_VEDTAK
CREATE TABLE BEHANDLING_VEDTAK (
    id                        NUMBER(19, 0) NOT NULL,
    vedtaksdato               DATE NOT NULL,
    ansvarlig_saksbehandler   VARCHAR2(40 CHAR) NOT NULL,
    behandling_resultat_id    NUMBER(19) NOT NULL,
    vedtak_resultat_type      VARCHAR2(20 char) NOT NULL,
    versjon                   NUMBER(19) DEFAULT 0 NOT NULL,
    opprettet_av              VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid             TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av                 VARCHAR2(20 CHAR),
    endret_tid                TIMESTAMP(3),
    CONSTRAINT PK_BEHANDLING_VEDTAK PRIMARY KEY (id),
    CONSTRAINT FK_BEHANDLING_VEDTAK_1 FOREIGN KEY (behandling_resultat_id) REFERENCES BEHANDLING_RESULTAT,
    CONSTRAINT FK_BEHANDLING_VEDTAK_2 FOREIGN KEY (vedtak_resultat_type) REFERENCES VEDTAK_RESULTAT_TYPE
);

CREATE SEQUENCE SEQ_BEHANDLING_VEDTAK MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE INDEX IDX_VEDTAK_1 ON BEHANDLING_VEDTAK(vedtak_resultat_type);
CREATE INDEX IDX_VEDTAK_2 ON BEHANDLING_VEDTAK(ansvarlig_saksbehandler);
CREATE INDEX IDX_VEDTAK_3 ON BEHANDLING_VEDTAK(vedtaksdato);
