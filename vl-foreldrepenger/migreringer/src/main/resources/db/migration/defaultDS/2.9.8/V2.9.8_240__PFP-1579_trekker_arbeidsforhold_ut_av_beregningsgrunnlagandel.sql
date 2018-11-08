-- Lag tabell BG_ANDEL_ARBEIDSFORHOLD
CREATE TABLE BG_ANDEL_ARBEIDSFORHOLD (
    id                                    number(19, 0) not null,
    bg_andel_id                           number(19, 0) not null,
    arbeidsgiver_virksomhet_id            NUMBER(19, 0),
    arbeidsforhold_id                     VARCHAR2(200 CHAR),
    refusjonskrav_pr_aar                  number(19, 2),
    naturalytelse_bortfalt_pr_aar         number(19, 2),
    naturalytelse_tilkommet_pr_aar        number(19, 2),
    tidsbegrenset_arbeidsforhold          VARCHAR2(1 CHAR),
    lønnsendring_i_perioden               VARCHAR2(1 CHAR),
    arbeidsperiode_fom                    DATE,
    arbeidsperiode_tom                    DATE,
    versjon                               number(19, 0) default 0 not null,
    opprettet_av                          VARCHAR2(20 char) default 'VL' not null,
    opprettet_tid                         timestamp(3) default systimestamp not null,
    endret_av                             VARCHAR2(20 char),
    endret_tid                            timestamp(3),
    CONSTRAINT PK_BG_ANDEL_ARBEIDSFORHOLD primary key (id),
    CONSTRAINT FK_BG_ANDEL_ARBEIDSFORHOLD_1 FOREIGN KEY (bg_andel_id) REFERENCES BG_PR_STATUS_OG_ANDEL(id),
    CONSTRAINT FK_BG_ANDEL_ARBEIDSFORHOLD_2 FOREIGN KEY (arbeidsgiver_virksomhet_id) REFERENCES VIRKSOMHET(id)
);


CREATE INDEX IDX_BG_ANDEL_ARBEIDSFORHOLD_01 ON BG_ANDEL_ARBEIDSFORHOLD (bg_andel_id);
CREATE INDEX IDX_BG_ANDEL_ARBEIDSFORHOLD_02 ON BG_ANDEL_ARBEIDSFORHOLD (arbeidsgiver_virksomhet_id);
CREATE SEQUENCE SEQ_BG_ANDEL_ARBEIDSFORHOLD MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE BG_ANDEL_ARBEIDSFORHOLD IS 'Informasjon om arbeidsforholdet knyttet til beregningsgrunnlagandelen';
COMMENT ON COLUMN BG_ANDEL_ARBEIDSFORHOLD.BG_ANDEL_ID IS 'Beregningsgrunnlagandelen arbeidsforholdet er knyttet til';
COMMENT ON COLUMN BG_ANDEL_ARBEIDSFORHOLD.ARBEIDSGIVER_VIRKSOMHET_ID IS 'Virksomhetene tilknyttet arbeidsgiver';
COMMENT ON COLUMN BG_ANDEL_ARBEIDSFORHOLD.ARBEIDSFORHOLD_ID IS 'Arbeidsforholdreferansen til arbeidsforholdet';
COMMENT ON COLUMN BG_ANDEL_ARBEIDSFORHOLD.REFUSJONSKRAV_PR_AAR IS 'Arbeidsgivers refusjonskrav';
COMMENT ON COLUMN BG_ANDEL_ARBEIDSFORHOLD.NATURALYTELSE_BORTFALT_PR_AAR IS 'Verdi av bortfalt naturalytelse';
COMMENT ON COLUMN BG_ANDEL_ARBEIDSFORHOLD.NATURALYTELSE_TILKOMMET_PR_AAR IS 'Verdi av tilkommet naturalytelse';
COMMENT ON COLUMN BG_ANDEL_ARBEIDSFORHOLD.TIDSBEGRENSET_ARBEIDSFORHOLD IS 'Er arbeidsforholdet tidsbegrenset';
COMMENT ON COLUMN BG_ANDEL_ARBEIDSFORHOLD.LØNNSENDRING_I_PERIODEN IS 'Er det lønnsendring i beregningsperioden';
COMMENT ON COLUMN BG_ANDEL_ARBEIDSFORHOLD.ARBEIDSPERIODE_FOM IS 'Fra og med dato arbeidsperiode';
COMMENT ON COLUMN BG_ANDEL_ARBEIDSFORHOLD.ARBEIDSPERIODE_TOM IS 'Til og med dato arbeidsperiode';

-- Migrer data
insert into BG_ANDEL_ARBEIDSFORHOLD (
  id,
  bg_andel_id,
  arbeidsgiver_virksomhet_id,
  arbeidsforhold_id,
  refusjonskrav_pr_aar,
  naturalytelse_bortfalt_pr_aar,
  naturalytelse_tilkommet_pr_aar,
  tidsbegrenset_arbeidsforhold,
  lønnsendring_i_perioden,
  arbeidsperiode_fom,
  arbeidsperiode_tom
)
select
  SEQ_BG_ANDEL_ARBEIDSFORHOLD.nextval,
  ID,
  VIRKSOMHET_ID,
  ARBEIDSFORHOLD_ID,
  REFUSJONSKRAV_PR_AAR,
  NATURALYTELSE_BORTFALT_PR_AAR,
  NATURALYTELSE_TILKOMMET_PR_AAR,
  TIDSBEGRENSET_ARBEIDSFORHOLD,
  LØNNSENDRING_I_PERIODEN,
  ARBEIDSPERIODE_FOM,
  ARBEIDSPERIODE_TOM

from BG_PR_STATUS_OG_ANDEL

where
BG_PR_STATUS_OG_ANDEL.VIRKSOMHET_ID is not null
or BG_PR_STATUS_OG_ANDEL.ARBEIDSFORHOLD_ID is not null
or BG_PR_STATUS_OG_ANDEL.REFUSJONSKRAV_PR_AAR is not null
or BG_PR_STATUS_OG_ANDEL.NATURALYTELSE_BORTFALT_PR_AAR is not null
or BG_PR_STATUS_OG_ANDEL.NATURALYTELSE_TILKOMMET_PR_AAR is not null
or BG_PR_STATUS_OG_ANDEL.TIDSBEGRENSET_ARBEIDSFORHOLD is not null
or BG_PR_STATUS_OG_ANDEL.LØNNSENDRING_I_PERIODEN is not null
or BG_PR_STATUS_OG_ANDEL.ARBEIDSPERIODE_FOM is not null
or BG_PR_STATUS_OG_ANDEL.ARBEIDSPERIODE_TOM is not null;
