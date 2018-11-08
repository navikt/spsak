
-- lagre dagsatser som heltall

alter table BG_PR_STATUS_OG_ANDEL rename column dagsats_bruker to dagsats_bruker_old;
alter table BG_PR_STATUS_OG_ANDEL add dagsats_bruker NUMBER(19,0);
update BG_PR_STATUS_OG_ANDEL set dagsats_bruker = dagsats_bruker_old;
alter table BG_PR_STATUS_OG_ANDEL drop column dagsats_bruker_old;

alter table BG_PR_STATUS_OG_ANDEL rename column dagsats_arbeidsgiver to dagsats_arbeidsgiver_old;
alter table BG_PR_STATUS_OG_ANDEL add dagsats_arbeidsgiver NUMBER(19,0);
update BG_PR_STATUS_OG_ANDEL set dagsats_arbeidsgiver = dagsats_arbeidsgiver_old;
alter table BG_PR_STATUS_OG_ANDEL drop column dagsats_arbeidsgiver_old;

alter table BEREGNINGSGRUNNLAG_PERIODE rename column dagsats to dagsats_old;
alter table BEREGNINGSGRUNNLAG_PERIODE add dagsats NUMBER(19,0);
update BEREGNINGSGRUNNLAG_PERIODE set dagsats = dagsats_old;
alter table BEREGNINGSGRUNNLAG_PERIODE drop column dagsats_old;
