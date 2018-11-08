-- Tabell INNTEKTSGRUNNLAG_MOCK
CREATE TABLE INNTEKTSGRUNNLAG_MOCK (
  id                   NUMBER(19, 0)                     NOT NULL,
  versjon              NUMBER(19, 0) DEFAULT 0           NOT NULL,
  fnr                  VARCHAR2(20 CHAR),
  aktor_id             NUMBER(19, 0),
  fp_arbeidsforhold_id NUMBER(19, 0),
  bruttoinntekt        DOUBLE PRECISION                  NOT NULL,
  kilde                VARCHAR2(100 CHAR)                NOT NULL,
  mnd                  DATE                              NOT NULL,
  frilanser            NUMBER(1) DEFAULT 0               NOT NULL CHECK (frilanser IN (0, 1)),
  opprettet_av         VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid        TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av            VARCHAR2(20 CHAR),
  endret_tid           TIMESTAMP(3),
  CONSTRAINT PK_INNTEKTSGRUNNLAG_MOCK PRIMARY KEY (id)
);
CREATE INDEX IDX_INNTEKTSGRUNNLAG_MOCK_01
  ON INNTEKTSGRUNNLAG_MOCK (fnr);
CREATE INDEX IDX_INNTEKTSGRUNNLAG_MOCK_02
  ON INNTEKTSGRUNNLAG_MOCK (aktor_id);
CREATE SEQUENCE SEQ_INNTEKTSGRUNNLAG_MOCK MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
