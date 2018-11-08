
CREATE TABLE SAKSOPPLYSNING (
    id                         NUMBER(19) NOT NULL ,
    fagsak_id                  NUMBER(19) NOT NULL ,  -- ikke FK med constraint
    behandling_id              NUMBER(19) NOT NULL ,  -- ikke FK med constraint
    ekstern_referanse          VARCHAR2(100 CHAR) ,
    saksopplysning_type        VARCHAR2(7 CHAR) NOT NULL ,
    saksopplysning_kilde       VARCHAR2(7 CHAR) NOT NULL ,
    saksopplysning_dokument_id NUMBER(19) ,
    opprettet_av               VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid              TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av                  VARCHAR2(20 CHAR),
    endret_tid                 TIMESTAMP(3)
  ) ;
ALTER TABLE SAKSOPPLYSNING ADD CONSTRAINT PK_SAKSOPPLYSNING PRIMARY KEY ( id ) ;
CREATE INDEX IDX_SAKSOPPLYSNING_1 ON SAKSOPPLYSNING(fagsak_id);
CREATE INDEX IDX_SAKSOPPLYSNING_2 ON SAKSOPPLYSNING(behandling_id);
CREATE INDEX IDX_SAKSOPPLYSNING_3 ON SAKSOPPLYSNING(ekstern_referanse);
CREATE INDEX IDX_SAKSOPPLYSNING_4 ON SAKSOPPLYSNING(saksopplysning_type);
CREATE INDEX IDX_SAKSOPPLYSNING_5 ON SAKSOPPLYSNING(saksopplysning_kilde);
CREATE INDEX IDX_SAKSOPPLYSNING_6 ON SAKSOPPLYSNING(saksopplysning_dokument_id);

CREATE SEQUENCE SEQ_SAKSOPPLYSNING MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;
  -- 50 er viktig: må være lik Hibernate sin (default) verdi


CREATE TABLE SAKSOPPLYSNING_DOKUMENT (
    id                      NUMBER (19) NOT NULL ,
    dokument                CLOB NOT NULL ,
    md5_hash_hex            VARCHAR2(40 CHAR) NOT NULL ,  -- 128 bits = 16 bytes = 32 hex digits
    saksopplysning_dok_type VARCHAR2(7 CHAR) NOT NULL,
    opprettet_av            VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid           TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av               VARCHAR2(20 CHAR),
    endret_tid              TIMESTAMP(3)
  ) ;
ALTER TABLE SAKSOPPLYSNING_DOKUMENT ADD CONSTRAINT PK_SAKSOPPLYSNING_DOKUMENT PRIMARY KEY ( id ) ;
CREATE INDEX IDX_SAKSOPPLYSNING_DOK_1 ON SAKSOPPLYSNING_DOKUMENT(md5_hash_hex);

CREATE SEQUENCE SEQ_SAKSOPPLYSNING_DOKUMENT MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;
  -- 50 er viktig: må være lik Hibernate sin (default) verdi


CREATE TABLE SAKSOPPLYSNING_DOKUMENT_TYPE (
    kode            VARCHAR2(7 CHAR) NOT NULL ,
    navn            VARCHAR2(50 CHAR) NOT NULL,
    beskrivelse     VARCHAR2(2000 CHAR),
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3)
  ) ;
ALTER TABLE SAKSOPPLYSNING_DOKUMENT_TYPE ADD CONSTRAINT PK_SAKSOPPLYSNING_DOK_TYPE PRIMARY KEY ( kode ) ;

-- TODO (rune) midlertidig, ufullstendig:
INSERT INTO SAKSOPPLYSNING_DOKUMENT_TYPE (kode, navn) VALUES ('XML', 'XML-dokument');
INSERT INTO SAKSOPPLYSNING_DOKUMENT_TYPE (kode, navn) VALUES ('JSON', 'JSON-dokument');
INSERT INTO SAKSOPPLYSNING_DOKUMENT_TYPE (kode, navn) VALUES ('BINARY', 'Binært dokument');


CREATE TABLE SAKSOPPLYSNING_KILDE (
    kode            VARCHAR2(7 CHAR) NOT NULL ,
    navn            VARCHAR2(50 CHAR) NOT NULL,
    beskrivelse     VARCHAR2(2000 CHAR),
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3)
  ) ;
ALTER TABLE SAKSOPPLYSNING_KILDE ADD CONSTRAINT PK_SAKSOPPLYSNING_KILDE PRIMARY KEY ( kode ) ;

-- TODO (rune) midlertidig, ufullstendig:
INSERT INTO SAKSOPPLYSNING_KILDE (kode, navn) VALUES ('TPS', 'TPS');
INSERT INTO SAKSOPPLYSNING_KILDE (kode, navn) VALUES ('JOARK', 'JOARK');


CREATE TABLE SAKSOPPLYSNING_TYPE (
    kode            VARCHAR2(7 CHAR) NOT NULL ,
    navn            VARCHAR2(50 CHAR) NOT NULL,
    beskrivelse     VARCHAR2(2000 CHAR),
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3)
  ) ;
ALTER TABLE SAKSOPPLYSNING_TYPE ADD CONSTRAINT PK_SAKSOPPLYSNING_TYPE PRIMARY KEY ( kode ) ;

-- TODO (rune) midlertidig, ufullstendig:
INSERT INTO SAKSOPPLYSNING_TYPE (kode, navn) VALUES ('POSOKER', 'Personopplysninger søker');
INSERT INTO SAKSOPPLYSNING_TYPE (kode, navn) VALUES ('POEKTEF', 'Personopplysninger ektefelle');
INSERT INTO SAKSOPPLYSNING_TYPE (kode, navn) VALUES ('POBARN', 'Personopplysninger barn');
INSERT INTO SAKSOPPLYSNING_TYPE (kode, navn) VALUES ('SOKNAD', 'Søknad');


ALTER TABLE SAKSOPPLYSNING_DOKUMENT ADD CONSTRAINT FK_SAKSOPPLYSNING_DOKUMENT_1 FOREIGN KEY ( saksopplysning_dok_type ) REFERENCES SAKSOPPLYSNING_DOKUMENT_TYPE ( kode ) ;

ALTER TABLE SAKSOPPLYSNING ADD CONSTRAINT FK_SAKSOPPLYSNING_1 FOREIGN KEY ( saksopplysning_dokument_id ) REFERENCES SAKSOPPLYSNING_DOKUMENT ( id ) ;

ALTER TABLE SAKSOPPLYSNING ADD CONSTRAINT FK_SAKSOPPLYSNING_2 FOREIGN KEY ( saksopplysning_kilde ) REFERENCES SAKSOPPLYSNING_KILDE ( kode ) ;

ALTER TABLE SAKSOPPLYSNING ADD CONSTRAINT FK_SAKSOPPLYSNING_3 FOREIGN KEY ( saksopplysning_type ) REFERENCES SAKSOPPLYSNING_TYPE ( kode ) ;
