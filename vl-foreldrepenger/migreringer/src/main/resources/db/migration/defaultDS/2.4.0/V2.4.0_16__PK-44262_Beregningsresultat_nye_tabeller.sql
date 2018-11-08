-- Tabell BEREGNINGSRESULTAT_FP
CREATE TABLE BEREGNINGSRESULTAT_FP (
    id                                    number(19, 0) not null,
    versjon                               number(19, 0) default 0 not null,
    opprettet_av                          varchar2(20 char) default 'VL' not null,
    opprettet_tid                         timestamp(3) default systimestamp not null,
    endret_av                             varchar2(20 char),
    endret_tid                            timestamp(3),
    regel_input                           CLOB NOT NULL,
    regel_sporing                         CLOB NOT NULL,
    CONSTRAINT PK_BEREGNINGSRESULTAT_FP primary key (id)
);

CREATE SEQUENCE SEQ_BEREGNINGSRESULTAT_FP MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

-- Tabell BEREGNINGSRESULTAT_PERIODE
CREATE TABLE BEREGNINGSRESULTAT_PERIODE (
    id                    number(19, 0) not null,
    BEREGNINGSRESULTAT_FP_ID number(19, 0) not null,
    br_periode_fom        DATE NOT NULL,
    br_periode_tom        DATE NOT NULL,
    versjon               number(19, 0) default 0 not null,
    opprettet_av          varchar2(20 char) default 'VL' not null,
    opprettet_tid         timestamp(3) default systimestamp not null,
    endret_av             varchar2(20 char),
    endret_tid            timestamp(3),
    CONSTRAINT PK_BR_PERIODE primary key (id),
    CONSTRAINT FK_BR_PERIODE_1 FOREIGN KEY (BEREGNINGSRESULTAT_FP_ID) REFERENCES BEREGNINGSRESULTAT_FP(id)
);

CREATE INDEX IDX_BR_PERIODE_01 ON BEREGNINGSRESULTAT_PERIODE (BEREGNINGSRESULTAT_FP_ID);
CREATE INDEX IDX_BR_PERIODE_02 ON BEREGNINGSRESULTAT_PERIODE (br_periode_fom);
CREATE INDEX IDX_BR_PERIODE_03 ON BEREGNINGSRESULTAT_PERIODE (br_periode_tom);

CREATE SEQUENCE SEQ_BR_PERIODE MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;


-- Tabell BEREGNINGSRESULTAT_ANDEL
CREATE TABLE BEREGNINGSRESULTAT_ANDEL (
    id                                    number(19, 0) not null,
    br_periode_id                         number(19, 0) not null,
    bruker_er_mottaker                    CHAR(1) not null check (bruker_er_mottaker IN ('J', 'N')),
    arbeidsforhold_id                     VARCHAR2(100),
    dagsats                               NUMBER(19, 0) NOT NULL,
    versjon                               number(19, 0) default 0 not null,
    opprettet_av                          varchar2(20 char) default 'VL' not null,
    opprettet_tid                         timestamp(3) default systimestamp not null,
    endret_av                             varchar2(20 char),
    endret_tid                            timestamp(3),
    CONSTRAINT PK_BEREGNINGSRESULTAT_ANDEL primary key (id),
    CONSTRAINT FK_BEREGNINGSRESULTAT_ANDEL_1 FOREIGN KEY (br_periode_id) REFERENCES BEREGNINGSRESULTAT_PERIODE(id)
);

CREATE INDEX IDX_BR_ANDEL_01 ON BEREGNINGSRESULTAT_ANDEL (br_periode_id);

CREATE SEQUENCE SEQ_BEREGNINGSRESULTAT_ANDEL MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

-- Tabell BeregningsresultatFPKobling
CREATE TABLE RES_BEREGNINGSRESULTAT_FP (
  id                            NUMBER(19)                        NOT NULL,
  behandling_id                 NUMBER(19)                        NOT NULL,
  beregningsresultat_fp_id      NUMBER(19)                        NOT NULL,
  versjon                       NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av                  VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid                 TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                     VARCHAR2(20 CHAR),
  endret_tid                    TIMESTAMP(3),
  CONSTRAINT PK_RES_BEREGNINGSRESULTAT_FP PRIMARY KEY (id),
  CONSTRAINT FK_RES_BEREGNINGSRESULTAT_FP_1 FOREIGN KEY (behandling_id) REFERENCES BEHANDLING,
  CONSTRAINT FK_RES_BEREGNINGSRESULTAT_FP_2 FOREIGN KEY (beregningsresultat_fp_id) REFERENCES BEREGNINGSRESULTAT_FP
);

CREATE SEQUENCE SEQ_RES_BEREGNINGSRESULTAT_FP MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE RES_BEREGNINGSRESULTAT_FP IS 'Tabell som kobler et beregningsresultat_fp til behandling';
COMMENT ON COLUMN RES_BEREGNINGSRESULTAT_FP.ID IS 'Primary Key';
COMMENT ON COLUMN RES_BEREGNINGSRESULTAT_FP.behandling_id IS 'FK: BEHANDLING';
COMMENT ON COLUMN RES_BEREGNINGSRESULTAT_FP.beregningsresultat_fp_id IS 'FK: BEREGNINGSRESULTAT_FP';

ALTER TABLE BG_PR_STATUS_OG_ANDEL ADD dagsats_bruker FLOAT;
ALTER TABLE BG_PR_STATUS_OG_ANDEL ADD dagsats_arbeidsgiver FLOAT;


-- Endrer navn i kodeliste for vilkår type FP_VK_41
UPDATE KODELISTE SET NAVN = 'Beregning' WHERE KODEVERK = 'VILKAR_TYPE' AND KODE = 'FP_VK_41';
