CREATE TABLE MEDLEMSKAP_VILKAR_PERIODE (
  id                NUMBER(19)                        NOT NULL,
  versjon           NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av      VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid     TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av         VARCHAR2(20 CHAR),
  endret_tid        TIMESTAMP(3),
  CONSTRAINT PK_MEDLEMSKAP_VILKAR_PERIODE PRIMARY KEY (id)
);
CREATE SEQUENCE SEQ_MEDLEMSKAP_VILKAR_PERIODE MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;


CREATE TABLE GR_MEDLEMSKAP_VILKAR_PERIODE (
  id                 NUMBER(19)                        NOT NULL,
  vilkar_resultat_id NUMBER(19)                        NOT NULL,
  medlemskap_vilkar_periode_id  NUMBER(19)             NOT NULL,
  aktiv              VARCHAR2(1 CHAR) DEFAULT 'N'      NOT NULL,
  versjon            NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av       VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid      TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av          VARCHAR2(20 CHAR),
  endret_tid         TIMESTAMP(3),
  CONSTRAINT PK_GR_MEDLEMSKAP_VILKAR_PER PRIMARY KEY (id),
  CONSTRAINT FK_GR_MEDLEMSKAP_VILKAR_PER_1 FOREIGN KEY (vilkar_resultat_id) REFERENCES VILKAR_RESULTAT,
  CONSTRAINT FK_GR_MEDLEMSKAP_VILKAR_PER_2 FOREIGN KEY (medlemskap_vilkar_periode_id) REFERENCES MEDLEMSKAP_VILKAR_PERIODE,
  CONSTRAINT CHK_GR_MEDLEMSKAP_VILKAR_PER CHECK (AKTIV IN ('J', 'N'))
);
CREATE SEQUENCE SEQ_GR_MEDLEMSKAP_VILKAR_PER MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE UNIQUE INDEX UIDX_GR_MEDLEMSKAP_VILKAR_P_01
  ON GR_MEDLEMSKAP_VILKAR_PERIODE (
    (CASE WHEN AKTIV = 'J'
      THEN VILKAR_RESULTAT_ID
     ELSE NULL END),
    (CASE WHEN AKTIV = 'J'
      THEN AKTIV
     ELSE NULL END)
  );

-- Tar oss ikke bryet med å migrere her. P.t. bruker ingenting denne strukturen.
DELETE FROM MEDLEMSKAP_VILKAR_PERIODER;
-- Knytt eksisiterende MEDLEMSKAP_VILKAR_PERIODER til denne redesignede aggregatstrukturen
alter table MEDLEMSKAP_VILKAR_PERIODER DROP CONSTRAINT FK_MEDLEMSKAP_VILKAR_PERIODER;
alter table MEDLEMSKAP_VILKAR_PERIODER DROP COLUMN vilkar_resultat_id;
alter table MEDLEMSKAP_VILKAR_PERIODER ADD medlemskap_vilkar_periode_id NUMBER(19) NOT NULL;
alter table MEDLEMSKAP_VILKAR_PERIODER ADD CONSTRAINT FK_MEDLEMSKAP_VILKAR_PERIODE FOREIGN KEY (medlemskap_vilkar_periode_id) REFERENCES MEDLEMSKAP_VILKAR_PERIODE;

alter table MEDLEMSKAP_VILKAR_PERIODER DROP COLUMN aktiv;
alter table MEDLEMSKAP_VILKAR_PERIODER DROP COLUMN VERSJON;
