CREATE TABLE SO_RETTIGHET (
  id                     NUMBER(19)                        NOT NULL,
  annen_foreldre_rett    VARCHAR2(1 CHAR)                  NOT NULL,
  omsorg_i_hele_perioden VARCHAR2(1 CHAR)                  NOT NULL,
  versjon                NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av           VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid          TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av              VARCHAR2(20 CHAR),
  endret_tid             TIMESTAMP(3),
  CONSTRAINT PK_SO_RETTIGHET PRIMARY KEY (id),
  CONSTRAINT CHK_SO_RETTIGHET_01 CHECK (annen_foreldre_rett IN ('J', 'N')),
  CONSTRAINT CHK_SO_RETTIGHET_02 CHECK (omsorg_i_hele_perioden IN ('J', 'N'))
);
CREATE SEQUENCE SEQ_SO_RETTIGHET MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE TABLE SO_DEKNINGSGRAD (
  id            NUMBER(19)                        NOT NULL,
  dekningsgrad  NUMBER(3)                         NOT NULL,
  versjon       NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av  VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av     VARCHAR2(20 CHAR),
  endret_tid    TIMESTAMP(3),
  CONSTRAINT PK_SO_DEKNINGSGRAD PRIMARY KEY (id)
);
CREATE SEQUENCE SEQ_SO_DEKNINGSGRAD MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE TABLE SO_FORDELING (
  id            NUMBER(19)                        NOT NULL,
  versjon       NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av  VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av     VARCHAR2(20 CHAR),
  endret_tid    TIMESTAMP(3),
  CONSTRAINT PK_SO_FORDELING PRIMARY KEY (id)
);
CREATE SEQUENCE SEQ_SO_FORDELING MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE TABLE SO_FORDELING_PERIODE (
  id                NUMBER(19)                        NOT NULL,
  fordeling_id      NUMBER(19)                        NOT NULL,
  fom               DATE                              NOT NULL,
  tom               DATE                              NOT NULL,
  periode_type      VARCHAR2(100)                     NOT NULL,
  kl_periode_type   VARCHAR2(100) AS ('UTTAK_PERIODE_TYPE'),
  aarsak_type       VARCHAR2(100)                     NOT NULL,
  kl_aarsak_type    VARCHAR2(100)                     NOT NULL,
  gradering         NUMBER(3, 0),
  arbeidsforhold_id VARCHAR2(100),
  versjon           NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av      VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid     TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av         VARCHAR2(20 CHAR),
  endret_tid        TIMESTAMP(3),
  CONSTRAINT PK_SO_FORDELING_PERIODE PRIMARY KEY (id),
  CONSTRAINT FK_SO_FORDELING_PERIODE_1 FOREIGN KEY (kl_periode_type, periode_type) REFERENCES KODELISTE (kodeverk, kode),
  CONSTRAINT FK_SO_FORDELING_PERIODE_2 FOREIGN KEY (kl_aarsak_type, aarsak_type) REFERENCES KODELISTE (kodeverk, kode),
  CONSTRAINT FK_SO_FORDELING_PERIODE_4 FOREIGN KEY (fordeling_id) REFERENCES SO_FORDELING,
  CONSTRAINT CHK_SO_FORDELING_PERIODE_01 CHECK ((gradering IS NULL AND arbeidsforhold_id IS NULL) OR
                                                (gradering IS NOT NULL AND arbeidsforhold_id IS NOT NULL))
);
CREATE SEQUENCE SEQ_SO_FORDELING_PERIODE MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE TABLE GR_YTELSES_FORDELING (
  id                 NUMBER(19)                        NOT NULL,
  behandling_id      NUMBER(19)                        NOT NULL,
  so_rettighet_id    NUMBER(19),
  so_fordeling_id    NUMBER(19),
  so_dekningsgrad_id NUMBER(19),
  aktiv              VARCHAR2(1 CHAR) DEFAULT 'N'      NOT NULL,
  versjon            NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av       VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid      TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av          VARCHAR2(20 CHAR),
  endret_tid         TIMESTAMP(3),
  CONSTRAINT PK_GR_YTELSES_FORDELING PRIMARY KEY (id),
  CONSTRAINT FK_GR_YTELSES_FORDELING_1 FOREIGN KEY (behandling_id) REFERENCES BEHANDLING,
  CONSTRAINT FK_GR_YTELSES_FORDELING_2 FOREIGN KEY (so_rettighet_id) REFERENCES SO_RETTIGHET,
  CONSTRAINT FK_GR_YTELSES_FORDELING_3 FOREIGN KEY (so_dekningsgrad_id) REFERENCES SO_DEKNINGSGRAD,
  CONSTRAINT FK_GR_YTELSES_FORDELING_4 FOREIGN KEY (so_fordeling_id) REFERENCES SO_FORDELING,
  CONSTRAINT CHK_GR_YTELSES_FORDELING CHECK (AKTIV IN ('J', 'N'))
);
CREATE SEQUENCE SEQ_GR_YTELSES_FORDELING MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE UNIQUE INDEX UIDX_GR_YTELSES_FORDELING_01
  ON GR_YTELSES_FORDELING (
    (CASE WHEN AKTIV = 'J'
      THEN BEHANDLING_ID
     ELSE NULL END),
    (CASE WHEN AKTIV = 'J'
      THEN AKTIV
     ELSE NULL END)
  );

INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier)
VALUES ('UTTAK_PERIODE_TYPE', 'Kodeverk over periode søker kan spesifisere i søknaden.', '', 'VL');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, '-', 'Ikke satt eller valgt kode', 'Ikke satt eller valgt kode',
   to_date('2000-01-01', 'YYYY-MM-DD'), 'UTTAK_PERIODE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, 'FELLESPERIODE', 'Fellesperioden', 'Fellesperioden', to_date('2000-01-01', 'YYYY-MM-DD'),
   'UTTAK_PERIODE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, 'MØDREKVOTE', 'Mødrekvoten', 'Mødrekvoten', to_date('2000-01-01', 'YYYY-MM-DD'),
   'UTTAK_PERIODE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, 'FEDREKVOTE', 'Fedrekvoten', 'Fedrekvoten', to_date('2000-01-01', 'YYYY-MM-DD'),
   'UTTAK_PERIODE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, 'FORELDREPENGER', 'Foreldrepenger', 'Foreldrepenger', to_date('2000-01-01', 'YYYY-MM-DD'),
   'UTTAK_PERIODE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, 'ANNET', 'Andre typer som f.eks utsettelse', 'Andre typer som f.eks utsettelse',
   to_date('2000-01-01', 'YYYY-MM-DD'), 'UTTAK_PERIODE_TYPE');

INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier)
VALUES ('AARSAK_TYPE', 'Kodeverk over årsaker til avvik(Utsettelse, opphold eller overføring) i perioder.', '', 'VL');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, '-', 'Ikke satt eller valgt kode', 'Ikke satt eller valgt kode',
   to_date('2000-01-01', 'YYYY-MM-DD'), 'AARSAK_TYPE');

INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier)
VALUES ('OPPHOLD_AARSAK_TYPE', 'Kodeverk over opphold i uttak.', '', 'VL');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, '-', 'Ikke satt eller valgt kode', 'Ikke satt eller valgt kode',
   to_date('2000-01-01', 'YYYY-MM-DD'), 'OPPHOLD_AARSAK_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, 'INGEN', 'Ingen årsak.', 'Ingen årsak',
   to_date('2000-01-01', 'YYYY-MM-DD'), 'OPPHOLD_AARSAK_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, 'UTTAK_FELLESP_ANNEN_FORELDER', 'Annen foreldre har permisjon i fellesperioden.', 'Annen foreldre har permisjon i fellesperioden.',
   to_date('2000-01-01', 'YYYY-MM-DD'), 'OPPHOLD_AARSAK_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, 'UTTAK_KVOTE_ANNEN_FORELDER', 'Annen foreldre har uttak av kvote.', 'Annen foreldre har uttak av kvote.',
   to_date('2000-01-01', 'YYYY-MM-DD'), 'OPPHOLD_AARSAK_TYPE');

INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier)
VALUES ('OVERFOERING_AARSAK_TYPE', 'Kodeverk over årsaker til avvik(Utsettelse, opphold eller overføring) i perioder.', '', 'VL');
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'ALENEOMSORG', 'Aleneomsorg for barnet/barna', 'Aleneomsorg for barnet/barna', 'OVERFOERING_AARSAK_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'IKKE_RETT_ANNEN_FORELDER', 'Den andre foreldren har ikke rett på foreldrepenger', 'Den andre foreldren har ikke rett på foreldrepenger', 'OVERFOERING_AARSAK_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, '-', 'Ikke satt eller valgt kode', 'Ikke satt eller valgt kode',
   to_date('2000-01-01', 'YYYY-MM-DD'), 'OVERFOERING_AARSAK_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, 'INSTITUSJONSOPPHOLD_ANNEN_FORELDER', 'Den andre foreldren er innlagt i helseinstitusjon', 'Den andre foreldren er innlagt i helseinstitusjon',
   to_date('2000-01-01', 'YYYY-MM-DD'), 'OVERFOERING_AARSAK_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, 'SYKDOM_ANNEN_FORELDER', 'Den andre foreldren er pga sykdom avhengig av hjelp for å ta seg av barnet/barna', 'Den andre foreldren er pga sykdom avhengig av hjelp for å ta seg av barnet/barna',
   to_date('2000-01-01', 'YYYY-MM-DD'), 'OVERFOERING_AARSAK_TYPE');

INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier)
VALUES ('UTSETTELSE_AARSAK_TYPE', 'Kodeverk over årsaker til avvik(Utsettelse, opphold eller overføring) i perioder.', '', 'VL');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, '-', 'Ikke satt eller valgt kode', 'Ikke satt eller valgt kode',
   to_date('2000-01-01', 'YYYY-MM-DD'), 'UTSETTELSE_AARSAK_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, 'FERIE', 'Lovbestemt ferie', 'Lovbestemt ferie',
   to_date('2000-01-01', 'YYYY-MM-DD'), 'UTSETTELSE_AARSAK_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, 'SYKDOM', 'Avhengig av hjelp grunnet sykdom', 'Avhengig av hjelp grunnet sykdom',
   to_date('2000-01-01', 'YYYY-MM-DD'), 'UTSETTELSE_AARSAK_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, 'ARBEID', 'Arbeid', 'Arbeid',
   to_date('2000-01-01', 'YYYY-MM-DD'), 'UTSETTELSE_AARSAK_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, 'INSTITUSJONSOPPHOLD_SØKER', 'Søker er innlagt i helseinstitusjon', 'Søker er innlagt i helseinstitusjon',
   to_date('2000-01-01', 'YYYY-MM-DD'), 'UTSETTELSE_AARSAK_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, 'INSTITUSJONSOPPHOLD_BARNET', 'Barn er innlagt i helseinstitusjon', 'Barn er innlagt i helseinstitusjon',
   to_date('2000-01-01', 'YYYY-MM-DD'), 'UTSETTELSE_AARSAK_TYPE');


ALTER TABLE SOEKNAD
  ADD fordeling_id NUMBER(19);
ALTER TABLE SOEKNAD
  ADD rettighet_id NUMBER(19);
ALTER TABLE SOEKNAD
  ADD dekningsgrad_id NUMBER(19);

ALTER TABLE SOEKNAD
  ADD CONSTRAINT FK_SOEKNAD_SO_FORDELING FOREIGN KEY (fordeling_id) REFERENCES SO_FORDELING;
ALTER TABLE SOEKNAD
  ADD CONSTRAINT FK_SOEKNAD_SO_RETTIGHET FOREIGN KEY (rettighet_id) REFERENCES SO_RETTIGHET;
ALTER TABLE SOEKNAD
  ADD CONSTRAINT FK_SOEKNAD_SO_DEKNINGSGRAD FOREIGN KEY (dekningsgrad_id) REFERENCES SO_DEKNINGSGRAD;
