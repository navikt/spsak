-- Legger til boolean tidsbegrenset arbeidsforhold på andelen

alter table BG_PR_STATUS_OG_ANDEL add tidsbegrenset_arbeidsforhold VARCHAR2(1 CHAR);

alter table GR_BEREGNINGSGRUNNLAG add steg_opprettet VARCHAR(100 CHAR) default('-') NOT NULL;
