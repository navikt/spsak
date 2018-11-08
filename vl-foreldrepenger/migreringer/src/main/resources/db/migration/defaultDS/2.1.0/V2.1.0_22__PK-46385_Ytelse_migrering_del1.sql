ALTER TABLE BEHANDLING_REL_YTELSER
  ADD RELATERTE_YTELSER_ID VARCHAR2(100);

ALTER TABLE BEHANDLING_REL_YTELSER
  MODIFY BEHANDLING_GRUNNLAG_ID NULL;


CREATE TABLE RELATERTE_YTELSER (
  ID                        NUMBER(19)                        NOT NULL,
  INNTEKT_ARBEID_YTELSER_ID NUMBER(19)                        NOT NULL,
  VERSJON                   NUMBER(19) DEFAULT 0              NOT NULL,
  OPPRETTET_AV              VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  OPPRETTET_TID             TIMESTAMP(3) DEFAULT SYSTIMESTAMP NOT NULL,
  ENDRET_AV                 VARCHAR2(20 CHAR),
  ENDRET_TID                TIMESTAMP(3)
);

CREATE SEQUENCE SEQ_RELATERTE_YTELSER MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;