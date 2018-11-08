-- oppdatering til kodeverk AKTIVITET_STATUS for beregning
update KODELISTE set kode = 'DP',navn='Dagpenger',beskrivelse='Dagpenger' where kode = 'AL' and kodeverk = 'AKTIVITET_STATUS';

-- tabellendringer
alter table BG_PR_STATUS_OG_ANDEL modify (beregningsperiode_fom null);
alter table BG_PR_STATUS_OG_ANDEL modify (beregningsperiode_tom null);
alter table BG_PR_STATUS_OG_ANDEL modify (relatert_ytelse_type null);

-- sporing av regelresultater
alter table BEREGNINGSGRUNNLAG_PERIODE add (regel_evaluering CLOB);

alter table BEREGNINGSGRUNNLAG add (regellogg_skjaringstidspunkt CLOB);
alter table BEREGNINGSGRUNNLAG add (regellogg_brukers_status CLOB);
