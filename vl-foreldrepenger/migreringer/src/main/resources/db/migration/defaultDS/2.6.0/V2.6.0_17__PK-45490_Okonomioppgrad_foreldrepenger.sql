CREATE TABLE OKO_REFUSJONSINFO_156 (
  id                        NUMBER(19) NOT NULL,
  versjon                   NUMBER(19) DEFAULT 0 NOT NULL,
  maks_dato                 DATE NOT NULL,
  refunderes_id             VARCHAR2(20 CHAR) NOT NULL,
  dato_fom                  DATE NOT NULL,
  oppdrags_linje_150_id     NUMBER(19) NOT NULL,
  opprettet_av              VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid             TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                 VARCHAR2(20 CHAR),
  endret_tid                TIMESTAMP(3),
  CONSTRAINT PK_OKO_REFUSJONSINFO_156 PRIMARY KEY ( id )
);

ALTER TABLE OKO_REFUSJONSINFO_156 ADD CONSTRAINT FK_OKO_REFUSJONSINFO_156_1 FOREIGN KEY ( oppdrags_linje_150_id ) REFERENCES OKO_OPPDRAG_LINJE_150 ( id );

CREATE SEQUENCE SEQ_OKO_REFUSJONSINFO_156 MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE TABLE OKO_GRAD_170 (
  id                        NUMBER(19) NOT NULL,
  versjon                   NUMBER(19) DEFAULT 0 NOT NULL,
  type_grad                 VARCHAR2(10 CHAR) NOT NULL,
  grad                      NUMBER(5) NOT NULL,
  oppdrags_linje_150_id     NUMBER(19) NOT NULL,
  opprettet_av              VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid             TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                 VARCHAR2(20 CHAR),
  endret_tid                TIMESTAMP(3),
  CONSTRAINT PK_OKO_GRAD_170 PRIMARY KEY ( id )
);

ALTER TABLE OKO_GRAD_170 ADD CONSTRAINT FK_OKO_GRAD_170_1 FOREIGN KEY ( oppdrags_linje_150_id ) REFERENCES OKO_OPPDRAG_LINJE_150 ( id );

CREATE SEQUENCE SEQ_OKO_GRAD_170 MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;
