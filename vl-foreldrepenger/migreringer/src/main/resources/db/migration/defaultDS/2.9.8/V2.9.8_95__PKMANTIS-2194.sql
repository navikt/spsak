-- feil case i default verdi
alter table beregningsresultat_andel drop column kl_inntektskategori;
alter table beregningsresultat_ANDEL ADD KL_INNTEKTSKATEGORI VARCHAR2(100 CHAR) GENERATED ALWAYS AS ('INNTEKTSKATEGORI'); 

-- manglende FK til kodeverk
alter table AKSJONSPUNKT_DEF add constraint FK_AKSJONSPUNKT_DEF_83 foreign key (SKJERMLENKE_TYPE, KL_SKJERMLENKE_TYPE) references kodeliste (kode, kodeverk);
create index IDX_AKSJONSPUNKT_DEF_8 ON AKSJONSPUNKT_DEF(SKJERMLENKE_TYPE, KL_SKJERMLENKE_TYPE);

alter table behandling_resultat add constraint fk_behandling_resultat_81 foreign key (retten_til, kl_retten_til) references kodeliste(kode, kodeverk);
create index idx_behandling_resultat_8 on behandling_resultat(retten_til, kl_retten_til);

alter table behandling_resultat add constraint fk_behandling_resultat_82 foreign key (konsekvens_for_ytelsen, kl_konsekvens_for_ytelsen) references kodeliste(kode, kodeverk);
create index idx_behandling_resultat_9 on behandling_resultat(konsekvens_for_ytelsen, kl_konsekvens_for_ytelsen);

alter table behandling_resultat add constraint fk_behandling_resultat_83 foreign key (vedtaksbrev, kl_vedtaksbrev) references kodeliste(kode, kodeverk);
create index idx_behandling_resultat_10 on behandling_resultat(vedtaksbrev, kl_vedtaksbrev);

alter table beregningsresultat_andel add constraint fk_beregningsresultat_andel_81 foreign key (inntektskategori, KL_INNTEKTSKATEGORI) references kodeliste (kode, kodeverk);
create index idx_BEREGNINGSRESULTAT_ANDEL_4 on BEREGNINGSRESULTAT_ANDEL(inntektskategori, KL_INNTEKTSKATEGORI);

alter table beregningsresultat_andel add constraint fk_beregningsresultat_andel_82 foreign key (arbeidsforhold_type, KL_ARBEIDSFORHOLD_TYPE) references kodeliste (kode, kodeverk);
create index idx_beregningsresultat_andel_5 on beregningsresultat_andel(arbeidsforhold_type, KL_ARBEIDSFORHOLD_TYPE);

alter table BG_PR_STATUS_OG_ANDEL add constraint fk_BG_PR_STATUS_OG_ANDEL_81 foreign key (inntektskategori, KL_INNTEKTSKATEGORI) references kodeliste (kode, kodeverk);
create index idx_BG_PR_STATUS_OG_ANDEL_4 on BG_PR_STATUS_OG_ANDEL(inntektskategori, KL_INNTEKTSKATEGORI);

alter table BG_PR_STATUS_OG_ANDEL add constraint fk_BG_PR_STATUS_OG_ANDEL_82 foreign key (arbeidsforhold_type, KL_ARBEIDSFORHOLD_TYPE) references kodeliste (kode, kodeverk);
create index idx_BG_PR_STATUS_OG_ANDEL_5 on BG_PR_STATUS_OG_ANDEL(arbeidsforhold_type, KL_ARBEIDSFORHOLD_TYPE);

alter table bg_periode_aarsak add constraint fk_bg_periode_aarsak_83 foreign key (periode_aarsak, KL_PERIODE_AARSAK) references kodeliste (kode, kodeverk);
create index IDX_BG_PERIODE_AARSAK_2 ON BG_PERIODE_AARSAK(periode_aarsak, KL_PERIODE_AARSAK);

alter table IAY_YTELSE_GRUNNLAG add constraint fk_IAY_YTELSE_GRUNNLAG_81 foreign key (arbeidskategori, KL_ARBEIDSKATEGORI) references kodeliste (kode, kodeverk);
create index idx_IAY_YTELSE_GRUNNLAG_FELT_2 on IAY_YTELSE_GRUNNLAG(ARBEIDSKATEGORI, KL_ARBEIDSKATEGORI);
