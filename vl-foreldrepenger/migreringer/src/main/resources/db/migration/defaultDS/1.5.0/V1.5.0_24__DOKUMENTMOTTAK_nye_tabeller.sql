CREATE TABLE DOKUMENT_TYPE (
  kode                           VARCHAR2(20 CHAR) NOT NULL,
  navn                           VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse                    VARCHAR2(2000 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_DOKUMENT_TYPE PRIMARY KEY (kode)
);

CREATE TABLE MOTTATT_STATUS (
  kode                           VARCHAR2(20 CHAR) NOT NULL,
  navn                           VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse                    VARCHAR2(2000 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_MOTTATT_STATUS PRIMARY KEY (kode)
);

CREATE TABLE MOTTATTE_DOKUMENT (
  id                             NUMBER(19) NOT NULL,
  journal_post_id                VARCHAR2(20 CHAR),
  dokument_id                    VARCHAR2(20 CHAR),
  varientformat                  VARCHAR2(20 CHAR),
  type                           VARCHAR2(20 CHAR) NOT NULL,
  status                         VARCHAR2(20 CHAR) NOT NULL,
  behandling_grunnlag_id         NUMBER(19) NOT NULL,
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_MOTTATTE_DOKUMENT PRIMARY KEY (id),
  CONSTRAINT FK_MOTTATTE_DOKUMENT_1 FOREIGN KEY (type) REFERENCES DOKUMENT_TYPE,
  CONSTRAINT FK_MOTTATTE_DOKUMENT_2 FOREIGN KEY (status) REFERENCES MOTTATT_STATUS,
  CONSTRAINT FK_MOTTATTE_DOKUMENT_3 FOREIGN KEY (behandling_grunnlag_id) REFERENCES BEHANDLING_GRUNNLAG
);

CREATE INDEX IDX_MOTTATTE_DOKUMENT_1 ON MOTTATTE_DOKUMENT(type);
CREATE INDEX IDX_MOTTATTE_DOKUMENT_2 ON MOTTATTE_DOKUMENT(status);
CREATE INDEX IDX_MOTTATTE_DOKUMENT_3 ON MOTTATTE_DOKUMENT(behandling_grunnlag_id);

CREATE SEQUENCE SEQ_MOTTATTE_DOKUMENT MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE TABLE INNSENDINGSVALG (
  kode                           VARCHAR2(20 CHAR) NOT NULL,
  navn                           VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse                    VARCHAR2(2000 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_INNSENDINGSVALG PRIMARY KEY (kode)
);

CREATE TABLE VEDLEGG (
  id                             NUMBER(19) NOT NULL,
  skjemanummer                   VARCHAR2(20 CHAR),
  tilleggsinfo                   VARCHAR2(2000 CHAR),
  er_paakrevd_i_soeknadsdialog   VARCHAR2(1 CHAR) DEFAULT 0 NOT NULL,
  innsendingsvalg                VARCHAR2(20 CHAR) NOT NULL,
  soeknad_id                     NUMBER(19) NOT NULL,
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_VEDLEGG PRIMARY KEY (id),
  CONSTRAINT FK_VEDLEGG_1 FOREIGN KEY (innsendingsvalg) REFERENCES INNSENDINGSVALG,
  CONSTRAINT FK_VEDLEGG_2 FOREIGN KEY (soeknad_id) REFERENCES SOEKNAD
);

CREATE INDEX IDX_VEDLEGG_1 ON VEDLEGG(innsendingsvalg);
CREATE INDEX IDX_VEDLEGG_2 ON VEDLEGG(soeknad_id);

CREATE SEQUENCE SEQ_VEDLEGG MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE TABLE SOEKNAD_BARN (
  id                             NUMBER(19) NOT NULL,
  foedselsdato                   DATE NOT NULL,
  soeknad_id                     NUMBER(19) NOT NULL,
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_SOEKNAD_BARN PRIMARY KEY (id),
  CONSTRAINT FK_SOEKNAD_BARN FOREIGN KEY (soeknad_id) REFERENCES SOEKNAD
);

CREATE INDEX IDX_SOEKNAD_BARN ON SOEKNAD_BARN(soeknad_id);

CREATE SEQUENCE SEQ_SOEKNAD_BARN MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE TABLE LANDKODER (
  kode                           VARCHAR2(20 CHAR) NOT NULL,
  navn                           VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse                    VARCHAR2(2000 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_LANDKODER PRIMARY KEY (kode)
);

CREATE TABLE SOEKNAD_ANNEN_PART_TYPE (
  kode                           VARCHAR2(20 CHAR) NOT NULL,
  navn                           VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse                    VARCHAR2(2000 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_SOEKNAD_ANNEN_PART_TYPE PRIMARY KEY (kode)
);

CREATE TABLE SOEKNAD_ANNEN_PART (
  id                             NUMBER(19) NOT NULL,
  aktoer_id                      NUMBER(19) NOT NULL,
  utenlandsk_fnr                 VARCHAR2(20 CHAR),
  utenlandsk_fnr_land            VARCHAR2(20 CHAR),
  aarsak                         VARCHAR2(20 CHAR),
  begrunnelse                    VARCHAR2(2000 CHAR),
  type                           VARCHAR2(20 CHAR) NOT NULL,
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_SOEKNAD_ANNEN_PART PRIMARY KEY (id),
  CONSTRAINT FK_SOEKNAD_ANNEN_PART_1 FOREIGN KEY (utenlandsk_fnr_land) REFERENCES LANDKODER,
  CONSTRAINT FK_SOEKNAD_ANNEN_PART_2 FOREIGN KEY (type) REFERENCES SOEKNAD_ANNEN_PART_TYPE
);

CREATE INDEX IDX_SOEKNAD_ANNEN_PART_1 ON SOEKNAD_ANNEN_PART(utenlandsk_fnr_land);
CREATE INDEX IDX_SOEKNAD_ANNEN_PART_2 ON SOEKNAD_ANNEN_PART(type);

CREATE SEQUENCE SEQ_SOEKNAD_ANNEN_PART MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

ALTER TABLE SOEKNAD ADD annen_part_id NUMBER(19);
ALTER TABLE SOEKNAD ADD CONSTRAINT FK_SOEKNAD_SOEKNAD_ANNEN_PART FOREIGN KEY (annen_part_id) REFERENCES SOEKNAD_ANNEN_PART;
CREATE INDEX IDX_SOEKNAD_2 ON SOEKNAD(annen_part_id);

CREATE TABLE TILKNYTNING_NORGE (
  id                             NUMBER(19) NOT NULL,
  opphold_norge_naa              VARCHAR2(1 CHAR),
  tidligere_opphold_norge        VARCHAR2(1 CHAR),
  fremtidig_opphold_norge        VARCHAR2(1 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_TILKNYTNING_NORGE PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_TILKNYTNING_NORGE MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

ALTER TABLE SOEKNAD ADD tilknytning_norge_id NUMBER(19);
ALTER TABLE SOEKNAD ADD CONSTRAINT FK_SOEKNAD_TILKNYTNING_NORGE FOREIGN KEY (tilknytning_norge_id) REFERENCES TILKNYTNING_NORGE;
CREATE INDEX IDX_SOEKNAD_3 ON SOEKNAD(tilknytning_norge_id);

CREATE TABLE UTLANDSOPPHOLD (
  id                             NUMBER(19) NOT NULL,
  tidligere_opphold_norge        NUMBER(19),
  fremtidig_opphold_norge        NUMBER(19),
  land                           VARCHAR2(20 CHAR) NOT NULL,
  periode_startdato              DATE NOT NULL,
  periode_sluttdato              DATE NOT NULL,
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_UTLANDSOPPHOLD PRIMARY KEY (id),
  CONSTRAINT FK_UTLANDSOPPHOLD_1 FOREIGN KEY (tidligere_opphold_norge) REFERENCES TILKNYTNING_NORGE,
  CONSTRAINT FK_UTLANDSOPPHOLD_2 FOREIGN KEY (fremtidig_opphold_norge) REFERENCES TILKNYTNING_NORGE,
  CONSTRAINT FK_UTLANDSOPPHOLD_3 FOREIGN KEY (land) REFERENCES LANDKODER
);

CREATE INDEX IDX_UTLANDSOPPHOLD_1 ON UTLANDSOPPHOLD(tidligere_opphold_norge);
CREATE INDEX IDX_UTLANDSOPPHOLD_2 ON UTLANDSOPPHOLD(fremtidig_opphold_norge);
CREATE INDEX IDX_UTLANDSOPPHOLD_3 ON UTLANDSOPPHOLD(land);

CREATE SEQUENCE SEQ_UTLANDSOPPHOLD MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;