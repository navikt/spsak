
-- Tabell RelatertYtelseType
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'RELATERT_YTELSE_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

-- ny kodeverk AKTIVITET_STATUS for beregning, med kodeverdier
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse) values ('AKTIVITET_STATUS', 'N', 'N', 'Aktivitet status', 'Hvilken type aktivitet status som gjelder for beregning');

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'AT', 'Arbeidstaker', 'Arbeidstaker', 'AKTIVITET_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'FL', 'Frilanser', 'Frilanser', 'AKTIVITET_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'TY', 'Tilstøtende ytelse', 'Tilstøtende ytelse', 'AKTIVITET_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'SN', 'Selvstendig næringsdrivende', 'Selvstendig næringsdrivende', 'AKTIVITET_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'AT_FL', 'Kombinert arbeidstaker og frilanser', 'Kombinert arbeidstaker og frilanser', 'AKTIVITET_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'AT_SN', 'Kombinert arbeidstaker og selvstendig næringsdrivende', 'Kombinert arbeidstaker og selvstendig næringsdrivende', 'AKTIVITET_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'FL_SN', 'Kombinert frilanser og selvstendig næringsdrivende', 'Kombinert frilanser og selvstendig næringsdrivende', 'AKTIVITET_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'AT_FL_SN', 'Kombinert arbeidstaker, frilanser og selvstendig næringsdrivende', 'Kombinert arbeidstaker, frilanser og selvstendig næringsdrivende', 'AKTIVITET_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'AL', 'Arbeidsledig', 'Arbeidsledig (dagpenger)', 'AKTIVITET_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'AAP', 'Arbeidsavklaringspenger', 'Arbeidsavklaringspenger', 'AKTIVITET_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'MS', 'Militær eller sivil', 'Militær eller sivil', 'AKTIVITET_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'AKTIVITET_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));


-- Tabell BEREGNINGSGRUNNLAG
CREATE TABLE BEREGNINGSGRUNNLAG (
    id                                    number(19, 0) not null,
    skjaringstidspunkt                    date not null,
    versjon                               number(19, 0) default 0 not null,
    opprettet_av                          varchar2(20 char) default 'VL' not null,
    opprettet_tid                         timestamp(3) default systimestamp not null,
    endret_av                             varchar2(20 char),
    endret_tid                            timestamp(3),
    CONSTRAINT PK_BEREGNINGSGRUNNLAG primary key (id)
);

CREATE INDEX IDX_BEREGNINGSGRUNNLAG_02 ON BEREGNINGSGRUNNLAG (skjaringstidspunkt);

CREATE SEQUENCE SEQ_BEREGNINGSGRUNNLAG MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;


-- Tabell BEREGNINGSGRUNNLAG_PERIODE
CREATE TABLE BEREGNINGSGRUNNLAG_PERIODE (
    id                    number(19, 0) not null,
    beregningsgrunnlag_id number(19, 0) not null,
    bg_periode_fom        DATE NOT NULL,
    bg_periode_tom        DATE,
    beregnet_pr_aar       FLOAT,
    overstyrt_pr_aar      FLOAT,
    brutto_pr_aar         FLOAT,
    avkortet_pr_aar       FLOAT,
    redusert_pr_aar       FLOAT,
    tilkjent_pr_dag       FLOAT,
    versjon               number(19, 0) default 0 not null,
    opprettet_av          varchar2(20 char) default 'VL' not null,
    opprettet_tid         timestamp(3) default systimestamp not null,
    endret_av             varchar2(20 char),
    endret_tid            timestamp(3),
    CONSTRAINT PK_BG_PERIODE primary key (id),
    CONSTRAINT FK_BG_PERIODE_1 FOREIGN KEY (beregningsgrunnlag_id) REFERENCES BEREGNINGSGRUNNLAG(id)
);

CREATE INDEX IDX_BG_PERIODE_01 ON BEREGNINGSGRUNNLAG_PERIODE (beregningsgrunnlag_id);
CREATE INDEX IDX_BG_PERIODE_02 ON BEREGNINGSGRUNNLAG_PERIODE (bg_periode_fom);
CREATE INDEX IDX_BG_PERIODE_03 ON BEREGNINGSGRUNNLAG_PERIODE (bg_periode_tom);

CREATE SEQUENCE SEQ_BG_PERIODE MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;


-- Tabell BG_PR_STATUS_OG_ANDEL
CREATE TABLE BG_PR_STATUS_OG_ANDEL (
    id                                    number(19, 0) not null,
    bg_periode_id                         number(19, 0) not null,
    aktivitet_status                      VARCHAR2(100) NOT NULL,
    aktivitet_status_kl                   VARCHAR2(100) AS ('AKTIVITET_STATUS'),
    beregningsperiode_fom                 date not null,
    beregningsperiode_tom                 date not null,
    arbeidsforhold_id                     VARCHAR2(100),
    relatert_ytelse_type                  VARCHAR2(100) NOT NULL,
    relatert_ytelse_type_kl               VARCHAR2(100) AS ('RELATERT_YTELSE_TYPE'),
    brutto_pr_aar                         FLOAT,
    refusjonskrav_pr_aar                  FLOAT,
    naturalytelse_bortfalt_mnd            FLOAT,
    manuelt_fastsatt_pr_aar               FLOAT,
    avkortet_pr_aar                       FLOAT,
    redusert_pr_aar                       FLOAT,
    brukers_andel_pr_aar                  FLOAT,
    beregnet_pr_aar                       FLOAT,
    versjon                               number(19, 0) default 0 not null,
    opprettet_av                          varchar2(20 char) default 'VL' not null,
    opprettet_tid                         timestamp(3) default systimestamp not null,
    endret_av                             varchar2(20 char),
    endret_tid                            timestamp(3),
    CONSTRAINT PK_BG_PR_STATUS_OG_ANDEL primary key (id),
    CONSTRAINT FK_BG_PR_STATUS_OG_ANDEL_1 FOREIGN KEY (bg_periode_id) REFERENCES BEREGNINGSGRUNNLAG_PERIODE(id),
    CONSTRAINT FK_BG_PR_STATUS_OG_ANDEL_2 FOREIGN KEY (aktivitet_status_kl, aktivitet_status) REFERENCES KODELISTE(kodeverk, kode),
    CONSTRAINT FK_BG_PR_STATUS_OG_ANDEL_3 FOREIGN KEY (relatert_ytelse_type_kl, relatert_ytelse_type) REFERENCES KODELISTE(kodeverk, kode)
);

CREATE INDEX IDX_BG_PR_STATUS_OG_ANDEL_01 ON BG_PR_STATUS_OG_ANDEL (bg_periode_id);
CREATE INDEX IDX_BG_PR_STATUS_OG_ANDEL_02 ON BG_PR_STATUS_OG_ANDEL (aktivitet_status);
CREATE INDEX IDX_BG_PR_STATUS_OG_ANDEL_04 ON BG_PR_STATUS_OG_ANDEL (relatert_ytelse_type);

CREATE SEQUENCE SEQ_BG_PR_STATUS_OG_ANDEL MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

-- Tabell SAMMENLIGNINGSGRUNNLAG
CREATE TABLE SAMMENLIGNINGSGRUNNLAG (
    id                                    number(19, 0) not null,
    beregningsgrunnlag_id                 number(19, 0) not null,
    sammenligningsperiode_fom             DATE NOT NULL,
    sammenligningsperiode_tom             DATE NOT NULL,
    rapportert_pr_aar                     FLOAT NOT NULL,
    versjon                               number(19, 0) default 0 not null,
    opprettet_av                          varchar2(20 char) default 'VL' not null,
    opprettet_tid                         timestamp(3) default systimestamp not null,
    endret_av                             varchar2(20 char),
    endret_tid                            timestamp(3),
    CONSTRAINT PK_SAMMENLIGNINGSGRUNNLAG primary key (id),
    CONSTRAINT FK_SAMMENLIGNINGSGRUNNLAG_1 FOREIGN KEY (beregningsgrunnlag_id) REFERENCES BEREGNINGSGRUNNLAG(id)
);

CREATE INDEX IDX_SAMMENLIGNINGSGRUNNLAG_01 ON SAMMENLIGNINGSGRUNNLAG (beregningsgrunnlag_id);
CREATE INDEX IDX_SAMMENLIGNINGSGRUNNLAG_02 ON SAMMENLIGNINGSGRUNNLAG (sammenligningsperiode_fom);
CREATE INDEX IDX_SAMMENLIGNINGSGRUNNLAG_03 ON SAMMENLIGNINGSGRUNNLAG (sammenligningsperiode_tom);
CREATE INDEX IDX_SAMMENLIGNINGSGRUNNLAG_04 ON SAMMENLIGNINGSGRUNNLAG (rapportert_pr_aar);

CREATE SEQUENCE SEQ_SAMMENLIGNINGSGRUNNLAG MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

-- Tabell BG_AKTIVITET_STATUS
CREATE TABLE BG_AKTIVITET_STATUS (
    id                                    number(19, 0) not null,
    beregningsgrunnlag_id                 number(19, 0) not null,
    aktivitet_status                      VARCHAR2(100) NOT NULL,
    aktivitet_status_kl                   VARCHAR2(100) AS ('AKTIVITET_STATUS'),
    versjon                               number(19, 0) default 0 not null,
    opprettet_av                          varchar2(20 char) default 'VL' not null,
    opprettet_tid                         timestamp(3) default systimestamp not null,
    endret_av                             varchar2(20 char),
    endret_tid                            timestamp(3),
    CONSTRAINT PK_BG_AKTIVITET_STATUS primary key (id),
    CONSTRAINT FK_BG_AKTIVITET_STATUS_1 FOREIGN KEY (beregningsgrunnlag_id) REFERENCES BEREGNINGSGRUNNLAG(id),
    CONSTRAINT FK_BG_AKTIVITET_STATUS_2 FOREIGN KEY (aktivitet_status_kl, aktivitet_status) REFERENCES KODELISTE(kodeverk, kode)
);

CREATE INDEX IDX_BG_AKTIVITET_STATUS_01 ON BG_AKTIVITET_STATUS (beregningsgrunnlag_id);
CREATE INDEX IDX_BG_AKTIVITET_STATUS_02 ON BG_AKTIVITET_STATUS (aktivitet_status);

CREATE SEQUENCE SEQ_BG_AKTIVITET_STATUS MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

-- Tabell BEHANDLING_RESULTAT, ny kobling mot BEREGNINGSGRUNNLAG
ALTER TABLE BEHANDLING_RESULTAT ADD beregningsgrunnlag_id number(19, 0);
ALTER TABLE BEHANDLING_RESULTAT Add CONSTRAINT FK_BEHANDLING_RESULTAT_7 FOREIGN KEY (beregningsgrunnlag_id) References BEREGNINGSGRUNNLAG(id);
