CREATE TABLE BEHANDLING_AARSAK_TYPE (
  kode          VARCHAR2(20 CHAR)                 NOT NULL,
  navn          VARCHAR2(50 CHAR)                 NOT NULL,
  beskrivelse   VARCHAR2(4000 CHAR),
  opprettet_av  VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av     VARCHAR2(20 CHAR),
  endret_tid    TIMESTAMP(3),
  CONSTRAINT PK_BEHANDLING_AARSAK_TYPE PRIMARY KEY (kode)
);

INSERT INTO BEHANDLING_AARSAK_TYPE (kode, navn, beskrivelse)
VALUES ('RE-MF', 'Manglende fødsel', 'Revurdering pga. Manglende fødsel i TPS');
INSERT INTO BEHANDLING_AARSAK_TYPE (kode, navn, beskrivelse)
VALUES ('RE-MFIP', 'Manglende fødsel i terminperiode', 'Revurdering pga. Manglende fødsel i TPS mellom uke 26 og 29');
INSERT INTO BEHANDLING_AARSAK_TYPE (kode, navn, beskrivelse)
VALUES ('RE-AVAB', 'Ulikt antall barn', 'Revurdering pga. Avvik i antall barn');
INSERT INTO BEHANDLING_AARSAK_TYPE (kode, navn, beskrivelse)
VALUES ('RE-LOV', 'Feil lovanvendelse', 'Revurdering pga. Feil lovanvendelse');
INSERT INTO BEHANDLING_AARSAK_TYPE (kode, navn, beskrivelse)
VALUES ('RE-RGLF', 'Feil regelverksforståelse', 'Revurdering pga. Feil regelverksforståelse');
INSERT INTO BEHANDLING_AARSAK_TYPE (kode, navn, beskrivelse)
VALUES ('RE-FEFAKTA', 'Feil eller endret fakta', 'Revurdering pga. Feil eller endret fakta');
INSERT INTO BEHANDLING_AARSAK_TYPE (kode, navn, beskrivelse)
VALUES ('RE-PRSSL', 'Prosessuell feil', 'Revurdering pga. Prosessuell feil');
INSERT INTO BEHANDLING_AARSAK_TYPE (kode, navn, beskrivelse)
VALUES ('RE-ANNET', 'Annet', 'Revurdering pga. Annet');


CREATE TABLE BEHANDLING_AARSAK (
id                NUMBER(19, 0)                     NOT NULL,
behandling_id     NUMBER(19, 0)                     NOT NULL,
behandling_aarsak_type VARCHAR2(20 CHAR)                 NOT NULL,
versjon           NUMBER(19) DEFAULT 0              NOT NULL,
opprettet_av      VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
opprettet_tid     TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
endret_av         VARCHAR2(20 CHAR),
endret_tid        TIMESTAMP(3),
CONSTRAINT PK_BEHANDLING_AARSAK PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_BEHANDLING_AARSAK MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

ALTER TABLE BEHANDLING_AARSAK ADD CONSTRAINT FK_BEHANDLING_AARSAK_1 FOREIGN KEY (behandling_id) REFERENCES BEHANDLING;
CREATE INDEX IDX_BEHANDLING_AARSAK_1 ON BEHANDLING_AARSAK (behandling_id)

