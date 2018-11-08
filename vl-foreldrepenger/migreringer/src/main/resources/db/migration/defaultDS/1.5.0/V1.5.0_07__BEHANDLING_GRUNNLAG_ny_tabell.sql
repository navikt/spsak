-- Tabell SOEKNAD
CREATE TABLE SOEKNAD (
    id             NUMBER(19) NOT NULL,
    soeknadsdato  DATE not null,
    foedselsdato_fra_soeknad DATE,
    utstedt_dato_terminbekreftelse DATE,
    termindato_fra_soeknad DATE,
    antall_barn_fra_soeknad NUMBER(3),
    kilde_ref varchar2(50 char),
    tilleggsopplysninger varchar(4000 char),
    behandling_grunnlag_id NUMBER(19) not null,
    versjon              NUMBER(19) DEFAULT 0 NOT NULL,
    opprettet_av    VARCHAR2(20 char) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
    CONSTRAINT PK_SOEKNAD PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_SOEKNAD MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

-- Tabell FOEDSEL
CREATE TABLE FOEDSEL (
    id             NUMBER(19) NOT NULL,
    foedselsdato   date not null,
    antall_barn number(2) default 1 not null,
    antall_barn_tps_gjelder char(1) default 'N' not null check(antall_barn_tps_gjelder in ('J', 'N')),
    kilde_saksbehandler char(1) default 'N' not null check(kilde_saksbehandler in ('J', 'N')),
    kilde_ref varchar2(50 char),
    behandling_grunnlag_id number(19) not null,
    versjon              NUMBER(19) DEFAULT 0 NOT NULL,
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
    CONSTRAINT PK_FOEDSEL PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_FOEDSEL MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

-- Tabell TERMINBEKREFTELSE
CREATE TABLE TERMINBEKREFTELSE (
    id             NUMBER(19) NOT NULL,
    termindato   date not null,
    utstedt_dato date not null,
    antall_barn  NUMBER(3),
    kilde_ref varchar2(50 char),
    behandling_grunnlag_id number(19) not null,
    versjon              NUMBER(19) DEFAULT 0 NOT NULL,
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
    CONSTRAINT PK_TERMINBEKREFTELSE PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_TERMINBEKREFTELSE MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;


-- Tabell TERMINBEKREFTELSE
CREATE TABLE BEHANDLING_GRUNNLAG (
    id             NUMBER(19) NOT NULL,
    original_behandling_id number(19) NULL,
    versjon              NUMBER(19) DEFAULT 0 NOT NULL,
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
    CONSTRAINT PK_BEHANDLING_GRUNNLAG PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_BEHANDLING_GRUNNLAG MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

ALTER TABLE BEHANDLING_GRUNNLAG ADD CONSTRAINT FK_BEHANDLING_GRUNNLAG_1 FOREIGN KEY (original_behandling_id) REFERENCES BEHANDLING;
CREATE INDEX IDX_BEHANDLING_GRUNNLAG_1 ON BEHANDLING_GRUNNLAG (original_behandling_id);

ALTER TABLE SOEKNAD ADD CONSTRAINT FK_SOEKNAD_1 FOREIGN KEY (behandling_grunnlag_id) REFERENCES BEHANDLING_GRUNNLAG;
ALTER TABLE TERMINBEKREFTELSE ADD CONSTRAINT FK_TERMINBEKREFTELSE_1 FOREIGN KEY (behandling_grunnlag_id) REFERENCES BEHANDLING_GRUNNLAG;
ALTER TABLE FOEDSEL ADD CONSTRAINT FK_FOEDSEL_1 FOREIGN KEY (behandling_grunnlag_id) REFERENCES BEHANDLING_GRUNNLAG;

CREATE INDEX IDX_SOEKNAD_1 ON SOEKNAD(behandling_grunnlag_id);
CREATE INDEX IDX_TERMINBEKREFTELSE_1 ON TERMINBEKREFTELSE(behandling_grunnlag_id);
CREATE INDEX IDX_FOEDSEL_1 ON FOEDSEL(behandling_grunnlag_id);

ALTER TABLE BEHANDLING ADD behandling_grunnlag_id NUMBER(19, 0);
ALTER TABLE BEHANDLING ADD CONSTRAINT FK_BEHANDLING_6 FOREIGN KEY (behandling_grunnlag_id) REFERENCES BEHANDLING_GRUNNLAG;
CREATE INDEX IDX_BEHANDLING_5 ON BEHANDLING(behandling_grunnlag_id);

