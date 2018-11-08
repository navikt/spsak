CREATE TABLE RELATERT_YTELSE_TYPE (
  kode            VARCHAR2(20 CHAR) NOT NULL,
  navn            VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse     VARCHAR2(2000 CHAR),
  opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 CHAR),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_RELATERT_YTELSE_TYPE PRIMARY KEY (kode)
);

INSERT INTO RELATERT_YTELSE_TYPE(kode,navn) VALUES ('ENSLIG_FORSØRGER','Enslig forsørger');
INSERT INTO RELATERT_YTELSE_TYPE(kode,navn) VALUES ('SYKEPENGER','Sykepenger');
INSERT INTO RELATERT_YTELSE_TYPE(kode,navn) VALUES ('SVANGERSKAPSPENGER','Svangerskapspenger');
INSERT INTO RELATERT_YTELSE_TYPE(kode,navn) VALUES ('FORELDREPENGER','Foreldrepenger');
INSERT INTO RELATERT_YTELSE_TYPE(kode,navn) VALUES ('ENGANGSSTØNAD','Engangstønad');

CREATE TABLE RELATERT_YTELSE_TEMA (
  kode            VARCHAR2(20 CHAR) NOT NULL,
  navn            VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse     VARCHAR2(2000 CHAR),
  opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 CHAR),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_RELATERT_YTELSE_TEMA PRIMARY KEY (kode)
);

INSERT INTO RELATERT_YTELSE_TEMA(kode,navn) VALUES ('FA','Foreldrepenger');
INSERT INTO RELATERT_YTELSE_TEMA(kode,navn) VALUES ('SP','Sykepenger');
INSERT INTO RELATERT_YTELSE_TEMA(kode,navn) VALUES ('EF','Enslig forsørger');

CREATE TABLE RELATERT_YTELSE_BEHANDLTEMA (
  kode            VARCHAR2(20 CHAR) NOT NULL,
  navn            VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse     VARCHAR2(2000 CHAR),
  opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 CHAR),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_RELATERT_YTELSE_BEHANDLTEMA PRIMARY KEY (kode)
);

--Når tema er 'FA' Foreldrepenger
INSERT INTO RELATERT_YTELSE_BEHANDLTEMA(kode,navn) VALUES ('FØ','Foreldrepenger fødsel');
INSERT INTO RELATERT_YTELSE_BEHANDLTEMA(kode,navn) VALUES ('AP','Foreldrepenger adopsjon');
INSERT INTO RELATERT_YTELSE_BEHANDLTEMA(kode,navn) VALUES ('SV','Svangerskapspenger');
INSERT INTO RELATERT_YTELSE_BEHANDLTEMA(kode,navn) VALUES ('AE','Adopsjon engangsstønad');
INSERT INTO RELATERT_YTELSE_BEHANDLTEMA(kode,navn) VALUES ('FE','Fødsel engangsstønad');
INSERT INTO RELATERT_YTELSE_BEHANDLTEMA(kode,navn) VALUES ('FU','Foreldrepenger fødsel, utland');

--Når tema er 'SP' Sykepenger
INSERT INTO RELATERT_YTELSE_BEHANDLTEMA(kode,navn) VALUES ('RS','forsikr.risiko sykefravær');
INSERT INTO RELATERT_YTELSE_BEHANDLTEMA(kode,navn) VALUES ('RT','reisetilskudd');
INSERT INTO RELATERT_YTELSE_BEHANDLTEMA(kode,navn) VALUES ('SP','sykepenger');
INSERT INTO RELATERT_YTELSE_BEHANDLTEMA(kode,navn) VALUES ('SU','sykepenger utenlandsopphold');

--Når tema er 'EF' Enslig forsørger
INSERT INTO RELATERT_YTELSE_BEHANDLTEMA(kode,navn) VALUES ('BT','stønad til barnetilsyn');
INSERT INTO RELATERT_YTELSE_BEHANDLTEMA(kode,navn) VALUES ('FL','tilskudd til flytting');
INSERT INTO RELATERT_YTELSE_BEHANDLTEMA(kode,navn) VALUES ('OG','overgangsstønad');
INSERT INTO RELATERT_YTELSE_BEHANDLTEMA(kode,navn) VALUES ('UT','skolepenger');

CREATE TABLE RELATERT_YTELSE_SAKSTYPE (
  kode            VARCHAR2(20 CHAR) NOT NULL,
  navn            VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse     VARCHAR2(2000 CHAR),
  opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 CHAR),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_RELATERT_YTELSE_SAKSTYPE PRIMARY KEY (kode)
);

INSERT INTO RELATERT_YTELSE_SAKSTYPE(kode,navn) VALUES ('S','Søknad');
INSERT INTO RELATERT_YTELSE_SAKSTYPE(kode,navn) VALUES ('R','Revurdering');
INSERT INTO RELATERT_YTELSE_SAKSTYPE(kode,navn) VALUES ('K','Klage');
INSERT INTO RELATERT_YTELSE_SAKSTYPE(kode,navn) VALUES ('A','Anke');

CREATE TABLE RELATERT_YTELSE_STATUS (
  kode            VARCHAR2(20 CHAR) NOT NULL,
  navn            VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse     VARCHAR2(2000 CHAR),
  opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 CHAR),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_RELATERT_YTELSE_STATUS PRIMARY KEY (kode)
);

INSERT INTO RELATERT_YTELSE_STATUS(kode,navn,beskrivelse) VALUES ('IP','Ikke påbegynt','Saksbehandlingen kan starte med Statuskode IP (Ikke påbegynt). Da er det kun registrert en sakslinje uten at vedtaksbehandling er startet.');
INSERT INTO RELATERT_YTELSE_STATUS(kode,navn,beskrivelse) VALUES ('UB','Under Behandling','Saksbehandling startet - når sak med status UB - Under Behandling - lagres, rapporteres hendelsen BehandlingOpprettet');
INSERT INTO RELATERT_YTELSE_STATUS(kode,navn,beskrivelse) VALUES ('SG','Sendt til saksbehandler 2 for godkjenning','Saksbehandler 1 har fullført og sendt til saksbehandler 2 for godkjenning');
INSERT INTO RELATERT_YTELSE_STATUS(kode,navn,beskrivelse) VALUES ('UK','Underkjent av saksbehandler 2','Underkjent av saksbehandler 2 med retur til saksbehandler 1');
INSERT INTO RELATERT_YTELSE_STATUS(kode,navn) VALUES ('FB','FerdigBehandlet');
INSERT INTO RELATERT_YTELSE_STATUS(kode,navn) VALUES ('FI','ferdig iverksatt');

INSERT INTO RELATERT_YTELSE_STATUS(kode,navn) VALUES ('RF','returnert feilsendt');
INSERT INTO RELATERT_YTELSE_STATUS(kode,navn) VALUES ('RM','returnert midlertidig');
INSERT INTO RELATERT_YTELSE_STATUS(kode,navn) VALUES ('RT','returnert til');
INSERT INTO RELATERT_YTELSE_STATUS(kode,navn) VALUES ('ST','sendt til');
INSERT INTO RELATERT_YTELSE_STATUS(kode,navn) VALUES ('VD','videresendt Direktoratet');
INSERT INTO RELATERT_YTELSE_STATUS(kode,navn) VALUES ('VI','venter på iverksetting');
INSERT INTO RELATERT_YTELSE_STATUS(kode,navn) VALUES ('VT','videresendt Trygderetten');
INSERT INTO RELATERT_YTELSE_STATUS(kode,navn) VALUES ('HB','Henlagt/bortfalt');

CREATE TABLE RELATERT_YTELSE_RESULTAT (
  kode            VARCHAR2(20 CHAR) NOT NULL,
  navn            VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse     VARCHAR2(2000 CHAR),
  opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 CHAR),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_RELATERT_YTELSE_RESULTAT PRIMARY KEY (kode)
);

INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('?' , 'beslutningsstøtte Besl st');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('A' , ' Avslag');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('AK' , 'avvist klage');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('AV' , 'advarsel');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('DI' , 'delvis innvilget');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('DT' , 'delvis tilbakebetale');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('FB' , 'ferdigbehandlet');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('FI' , 'fortsatt innvilget');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('H' , 'henlagt / trukket tilbake');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('HB' , 'henlagt / bortfalt');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('I' , 'Innvilget');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('IN' , 'innvilget ny situasjon');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('IS' , 'ikke straffbart');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('IT' , ' ikke tilbakebetale');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('MO' , 'midlertidig opphørt');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('MT' , 'mottatt');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('O' , 'opphørt');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('PA' , 'politianmeldelse');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('R' , 'redusert');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('SB' , 'sak i bero');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('TB' , 'tilbakebetale');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('TH' , 'tips henlagt');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('TO' , 'tips oppfølging');
INSERT INTO RELATERT_YTELSE_RESULTAT(kode,navn) VALUES ('Ø' , 'økning');


CREATE TABLE RELATERT_YTELSE_TILSTAND (
  kode            VARCHAR2(20 CHAR) NOT NULL,
  navn            VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse     VARCHAR2(2000 CHAR),
  opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 CHAR),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_RELATERT_YTELSE_TILSTAND PRIMARY KEY (kode)
);

INSERT INTO RELATERT_YTELSE_TILSTAND(kode,navn) VALUES ('ÅPEN_SAK','Åpen sak');
INSERT INTO RELATERT_YTELSE_TILSTAND(kode,navn) VALUES ('LØPENDE_VEDTAK','Løpende vedtak');
INSERT INTO RELATERT_YTELSE_TILSTAND(kode,navn) VALUES ('LUKKET_SAK','Lukket sak');
