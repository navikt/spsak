-- Tabell PROSESS_TASK_FEILHAND
CREATE TABLE PROSESS_TASK_FEILHAND (
    kode            VARCHAR2(20 CHAR) NOT NULL,
    navn            VARCHAR2(50 CHAR) NOT NULL,
    beskrivelse     VARCHAR2(2000 CHAR),
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
    CONSTRAINT PK_PROSESS_TASK_FEILHAND PRIMARY KEY (kode)
);

INSERT INTO PROSESS_TASK_FEILHAND (kode, navn) VALUES ('DEFAULT', 'Eksponentiell back-off med tak');

-- Tabell PROSESS_TASK_TYPE
CREATE TABLE PROSESS_TASK_TYPE (
  kode            VARCHAR2(50 CHAR) NOT NULL,
  navn            VARCHAR2(50 CHAR),

  feil_maks_forsoek number(10, 0) default 1 not null,
  feil_sek_mellom_forsoek number(10, 0) default 30 not null,
  feilhandtering_algoritme varchar2(200 char) default 'DEFAULT',

  beskrivelse     VARCHAR2(2000 CHAR),
  opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 CHAR),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_PROSESS_TASK_TYPE PRIMARY KEY (kode)
);

CREATE INDEX IDX_PROSESS_TASK_TYPE_1 ON PROSESS_TASK_TYPE(feilhandtering_algoritme);

ALTER TABLE PROSESS_TASK_TYPE ADD CONSTRAINT FK_PROSESS_TASK_TYPE_1 FOREIGN KEY (feilhandtering_algoritme) REFERENCES PROSESS_TASK_FEILHAND;


-- Tabell PROSESS_TASK
create TABLE PROSESS_TASK (
	id number(19, 0) not null,
  task_type varchar2(200 char) not null,
	prioritet number(3, 0) default 0 not null,
	status varchar2(20 char) default 'KLAR' not null,
	task_parametere varchar2(4000 char),
	task_payload clob,
	task_gruppe varchar2(250 char),
	task_sekvens varchar2(100 char) default '1' NOT NULL,
  neste_kjoering_etter timestamp(0) default current_timestamp,

	feilede_forsoek number(5, 0) default 0,
	siste_kjoering_ts timestamp,
	siste_kjoering_feil_kode varchar2(50 char),
	siste_kjoering_feil_tekst clob,
	siste_kjoering_server varchar2(50 char),
	
	versjon number(19, 0) default 0 not null,
	CONSTRAINT CHK_PROSESS_TASK_STATUS CHECK (status IN ('KLAR', 'FEILET', 'VENTER_SVAR', 'SUSPENDERT', 'FERDIG')),
	CONSTRAINT PK_PROSESS_TASK PRIMARY KEY(id)
)
;

COMMENT ON TABLE PROSESS_TASK is 'Inneholder tasks som skal kjøres i bakgrunnen';
COMMENT ON COLUMN PROSESS_TASK.task_type is 'navn på task. Brukes til å matche riktig implementasjon';
COMMENT ON COLUMN PROSESS_TASK.prioritet is 'prioritet på task.  Høyere tall har høyere prioritet';
COMMENT ON COLUMN PROSESS_TASK.status is 'status på task: KLAR, NYTT_FORSOEK, FEILET, VENTER_SVAR, FERDIG';
COMMENT ON COLUMN PROSESS_TASK.neste_kjoering_etter is 'tasken skal ikke kjøeres før tidspunkt er passert';
COMMENT ON COLUMN PROSESS_TASK.feilede_forsoek is 'antall feilede forsøk';
COMMENT ON COLUMN PROSESS_TASK.siste_kjoering_ts is 'siste gang tasken ble forsøkt kjørt';
COMMENT ON COLUMN PROSESS_TASK.siste_kjoering_feil_kode is 'siste feilkode tasken fikk';
COMMENT ON COLUMN PROSESS_TASK.siste_kjoering_feil_tekst is 'siste feil tasken fikk';
COMMENT ON COLUMN PROSESS_TASK.siste_kjoering_server is 'navn på node som sist kjørte en task (server@pid)';
COMMENT ON COLUMN PROSESS_TASK.task_parametere is 'parametere angitt for en task';
comment on column PROSESS_TASK.task_payload is 'inputdata for en task';
COMMENT ON COLUMN PROSESS_TASK.task_sekvens is 'angir rekkefølge på task innenfor en gruppe ';
COMMENT ON COLUMN PROSESS_TASK.task_gruppe is 'angir en unik id som grupperer flere ';
COMMENT ON COLUMN PROSESS_TASK.versjon is 'angir versjon for optimistisk låsing';

CREATE SEQUENCE SEQ_PROSESS_TASK  MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE SEQUENCE SEQ_PROSESS_TASK_GRUPPE MINVALUE 10000000 START WITH 10000000 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE INDEX IDX_PROSESS_TASK_1 ON PROSESS_TASK(status);

CREATE INDEX IDX_PROSESS_TASK_2 ON PROSESS_TASK(task_type);
CREATE INDEX IDX_PROSESS_TASK_3 ON PROSESS_TASK(neste_kjoering_etter);

--CREATE INDEX IDX_PROSESS_TASK_4 ON PROSESS_TASK(task_parametere);
CREATE INDEX IDX_PROSESS_TASK_5 ON PROSESS_TASK(task_gruppe);

ALTER TABLE PROSESS_TASK ADD CONSTRAINT FK_PROSESS_TASK_1 FOREIGN KEY (task_type) REFERENCES PROSESS_TASK_TYPE;


INSERT INTO PROSESS_TASK_TYPE (kode, navn) values ('integrasjon.gsak.opprettOppgave', 'Oppretter Oppgave i GSak');
INSERT INTO PROSESS_TASK_TYPE (kode, navn) values ('fordeling.hentFraJoark', 'Henter metadata og xml fra Joark');
INSERT INTO PROSESS_TASK_TYPE (kode, navn) values ('fordeling.hentOgVurderVLSak', 'Forsøker å finne matchende sak I repo');
INSERT INTO PROSESS_TASK_TYPE (kode, navn) values ('fordeling.hentOgVurderInfotrygdSak', 'Forsøker å finne matchende sak i GSAK/Infotrygd');
INSERT INTO PROSESS_TASK_TYPE (kode, navn) values ('fordeling.opprettSak', 'Oppretter ny sak internt Vedtaksløsningen');
INSERT INTO PROSESS_TASK_TYPE (kode, navn) values ('fordeling.tilJournalforing', 'Setter oppgaven klar til journal');
INSERT INTO PROSESS_TASK_TYPE (kode, navn) values ('fordeling.oppdaterSakOgBehandling', 'Melder om ny sak til Sak Og Behandling');
INSERT INTO PROSESS_TASK_TYPE (kode, navn, feil_maks_forsoek, feil_sek_mellom_forsoek, feilhandtering_algoritme, beskrivelse)
     VALUES ('oppgavebehandling.opprettOppgave', 'Oppretter oppgave i GSAK',  3, 60, 'DEFAULT', 'Task som oppretter oppgave i GSAK');
INSERT INTO PROSESS_TASK_TYPE (kode, navn, feil_maks_forsoek, feil_sek_mellom_forsoek, feilhandtering_algoritme, beskrivelse)
     VALUES ('oppgavebehandling.avsluttOppgave', 'Avslutter oppgave i GSAK',  3, 60, 'DEFAULT', 'Task som avslutter oppgave i GSAK');
INSERT INTO PROSESS_TASK_TYPE (kode, navn) values ('IverksetteVedtak.VarsleOmVedtak', 'Varsler andre stønadsområder i Iverksette vedtak');
INSERT INTO PROSESS_TASK_TYPE (kode, navn) values ('IverksetteVedtak.SendVedtaksbrev', 'Kall til "Sende vedtaksbrev" i Iverksette vedtak');
INSERT INTO PROSESS_TASK_TYPE (kode, navn) values ('IverksetteVedtak.Utbetale', 'Kall til "Utbetale" i Iverksette vedtak');
INSERT INTO PROSESS_TASK_TYPE (kode, navn) values ('IverksetteVedtak.AvsluttBehandling', 'Avslutte behandling i modul Iverksette vedtak');
