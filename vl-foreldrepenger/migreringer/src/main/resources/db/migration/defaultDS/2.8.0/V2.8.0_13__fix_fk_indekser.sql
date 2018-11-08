create index IDX_KONFIG_VERDI_6 on KONFIG_VERDI(KONFIG_KODE,KONFIG_GRUPPE);
create index IDX_AKSJONSPUNKT_6 on AKSJONSPUNKT(BEHANDLING_STEG_FUNNET);
create index IDX_AKSJONSPUNKT_7 on AKSJONSPUNKT(AKSJONSPUNKT_DEF);
create index IDX_AKSJONSPUNKT_8 on AKSJONSPUNKT(VENT_AARSAK);
create index IDX_AKSJONSPUNKT_9 on AKSJONSPUNKT(AKSJONSPUNKT_STATUS);
create index IDX_AKSJONSPUNKT_DEF_6 on AKSJONSPUNKT_DEF(AKSJONSPUNKT_TYPE);
create index IDX_AKSJONSPUNKT_DEF_7 on AKSJONSPUNKT_DEF(VILKAR_TYPE);
create index IDX_BEHANDLING_6 on BEHANDLING(STARTPUNKT_TYPE);
create index IDX_BEHANDLING_ARSAK_6 on BEHANDLING_ARSAK(BEHANDLING_ID);
create index IDX_BEHANDLING_ARSAK_7 on BEHANDLING_ARSAK(ORIGINAL_BEHANDLING_ID);
create index IDX_BEHANDLING_GRUNNLAG_6 on BEHANDLING_GRUNNLAG(YTELSER_INFOTRYGD_STATUS);
create index IDX_BEHANDLING_RESULTAT_6 on BEHANDLING_RESULTAT(AVSLAG_ARSAK);
create index IDX_BEHANDLING_RESULTAT_7 on BEHANDLING_RESULTAT(BEREGNINGSGRUNNLAG_ID);
create index IDX_BEHANDLING_STEG_TILSTAND_6 on BEHANDLING_STEG_TILSTAND(BEHANDLING_ID);
create index IDX_BEHANDLING_STEG_TILSTAND_7 on BEHANDLING_STEG_TILSTAND(BEHANDLING_STEG_STATUS);
create index IDX_BEHANDLING_TYPE_STEG_SEK_6 on BEHANDLING_TYPE_STEG_SEKV(FAGSAK_YTELSE_TYPE);
create index IDX_BEHANDLING_VEDTAK_6 on BEHANDLING_VEDTAK(IVERKSETTING_STATUS);
create index IDX_BRUKER_6 on BRUKER(SPRAK_KODE);
create index IDX_DOKUMENT_DATA_6 on DOKUMENT_DATA(DOKUMENT_MAL_NAVN);
create index IDX_DOKUMENT_FELLES_6 on DOKUMENT_FELLES(MOTTAKER_ADRESSE_ID);
create index IDX_DOKUMENT_FELLES_7 on DOKUMENT_FELLES(RETUR_ADRESSE_ID);
create index IDX_DOKUMENT_FELLES_8 on DOKUMENT_FELLES(POST_ADRESSE_ID);
create index IDX_DOKUMENT_FELLES_9 on DOKUMENT_FELLES(DOKUMENT_DATA_ID);
create index IDX_DOKUMENT_FELLES_10 on DOKUMENT_FELLES(SPRAK_KODE);
create index IDX_DOKUMENT_MAL_TYPE_6 on DOKUMENT_MAL_TYPE(DOKUMENT_MAL_RESTRIKSJON);
create index IDX_DOKUMENT_TYPE_DATA_6 on DOKUMENT_TYPE_DATA(DOKUMENT_FELLES_ID);
create index IDX_FAGSAK_6 on FAGSAK(ARSAK_TYPE);
create index IDX_FAGSAK_7 on FAGSAK(YTELSE_TYPE);
create index IDX_FH_ADOPSJON_6 on FH_ADOPSJON(OMSORG_VILKAAR_TYPE);
create index IDX_FH_FAMILIE_HENDELSE_6 on FH_FAMILIE_HENDELSE(FAMILIE_HENDELSE_TYPE);
create index IDX_FORRETNINGSHENDELSE_DEF_6 on FORRETNINGSHENDELSE_DEF(FORRETNINGSHENDELSE_TYPE);
create index IDX_FORRETNINGSHENDELSE_DEF_7 on FORRETNINGSHENDELSE_DEF(REVURDERINGSÅRSAK_TYPE);
create index IDX_FORRETNINGSHENDELSE_DEF_8 on FORRETNINGSHENDELSE_DEF(STARTPUNKT_TYPE);
create index IDX_GR_BEREGNINGSGRUNNLAG_6 on GR_BEREGNINGSGRUNNLAG(BEHANDLING_ID);
create index IDX_GR_BEREGNINGSGRUNNLAG_7 on GR_BEREGNINGSGRUNNLAG(BEREGNINGSGRUNNLAG_ID);
create index IDX_GR_FAMILIE_HENDELSE_6 on GR_FAMILIE_HENDELSE(BEHANDLING_ID);
create index IDX_GR_MEDLEMSKAP_VILKAR_PER_6 on GR_MEDLEMSKAP_VILKAR_PERIODE(VILKAR_RESULTAT_ID);
create index IDX_GR_MEDLEMSKAP_VILKAR_PER_7 on GR_MEDLEMSKAP_VILKAR_PERIODE(MEDLEMSKAP_VILKAR_PERIODE_ID);
create index IDX_GR_PERSONOPPLYSNING_6 on GR_PERSONOPPLYSNING(SO_ANNEN_PART_ID);
create index IDX_HISTORIKKINNSLAG_6 on HISTORIKKINNSLAG(FAGSAK_ID);
create index IDX_HISTORIKKINNSLAG_7 on HISTORIKKINNSLAG(HISTORIKK_AKTOER_ID);
create index IDX_IAY_ANNEN_AKTIVITET_6 on IAY_ANNEN_AKTIVITET(ARBEID_TYPE);
create index IDX_IAY_EGEN_NAERING_6 on IAY_EGEN_NAERING(LAND);
create index IDX_IAY_INNTEKTSMELDING_6 on IAY_INNTEKTSMELDING(MOTTATT_DOKUMENT_ID);
create index IDX_IAY_INNTEKTSPOST_6 on IAY_INNTEKTSPOST(YTELSE_TYPE);
create index IDX_IAY_NATURAL_YTELSE_6 on IAY_NATURAL_YTELSE(NATURAL_YTELSE_TYPE);
create index IDX_IAY_OPPGITT_ARBEIDSFORHO_6 on IAY_OPPGITT_ARBEIDSFORHOLD(ARBEID_TYPE);
create index IDX_IAY_PERMISJON_6 on IAY_PERMISJON(BESKRIVELSE_TYPE);
create index IDX_IAY_RELATERT_YTELSE_6 on IAY_RELATERT_YTELSE(YTELSE_TYPE);
create index IDX_IAY_RELATERT_YTELSE_7 on IAY_RELATERT_YTELSE(STATUS);
create index IDX_IAY_RELATERT_YTELSE_8 on IAY_RELATERT_YTELSE(KILDE);
create index IDX_IAY_RELATERT_YTELSE_9 on IAY_RELATERT_YTELSE(TEMAUNDERKATEGORI);
create index IDX_IAY_UTSETTELSE_PERIODE_6 on IAY_UTSETTELSE_PERIODE(UTSETTELSE_AARSAK_TYPE);
create index IDX_IAY_YTELSE_GRUNNLAG_6 on IAY_YTELSE_GRUNNLAG(ARBEID_TYPE);
create index IDX_INNSYN_6 on INNSYN(INNSYN_RESULTAT_TYPE);
create index IDX_INNSYN_DOKUMENT_6 on INNSYN_DOKUMENT(INNSYN_ID);
create index IDX_KLAGE_VURDERING_RESULTAT_6 on KLAGE_VURDERING_RESULTAT(BEHANDLING_ID);
create index IDX_KODELISTE_6 on KODELISTE(KODEVERK);
create index IDX_KODELISTE_RELASJON_6 on KODELISTE_RELASJON(KODE1,KODEVERK1);
create index IDX_KODELISTE_RELASJON_7 on KODELISTE_RELASJON(KODE2,KODEVERK2);

create index IDX_KONFIG_VERDI_KODE_6 on KONFIG_VERDI_KODE(KONFIG_TYPE);
create index IDX_KONFIG_VERDI_KODE_7 on KONFIG_VERDI_KODE(KONFIG_GRUPPE);
create index IDX_LAND_REGION_6 on LAND_REGION(REGION);
create index IDX_MEDLEMSKAP_OPPG_LAND_6 on MEDLEMSKAP_OPPG_LAND(MEDLEMSKAP_OPPG_TILKNYT_ID);
create index IDX_MEDLEMSKAP_PERIODER_6 on MEDLEMSKAP_PERIODER(LOVVALG_LAND);
create index IDX_MEDLEMSKAP_PERIODER_7 on MEDLEMSKAP_PERIODER(STUDIE_LAND);
create index IDX_MEDLEMSKAP_VILKAR_PERIOD_6 on MEDLEMSKAP_VILKAR_PERIODER(MEDLEMSKAP_VILKAR_PERIODE_ID);
create index IDX_MEDLEMSKAP_VURDERING_6 on MEDLEMSKAP_VURDERING(MEDLEMSPERIODE_MANUELL_VURD);
create index IDX_MOTTATT_DOKUMENT_6 on MOTTATT_DOKUMENT(BEHANDLING_ID);
create index IDX_OKO_ATTESTANT_180_6 on OKO_ATTESTANT_180(OPPDRAGS_LINJE_150_ID);
create index IDX_OKO_GRAD_170_6 on OKO_GRAD_170(OPPDRAGS_LINJE_150_ID);
create index IDX_OKO_OPPDRAG_110_6 on OKO_OPPDRAG_110(AVSTEMMING115_ID);
create index IDX_OKO_REFUSJONSINFO_156_6 on OKO_REFUSJONSINFO_156(OPPDRAGS_LINJE_150_ID);
create index IDX_OPPGAVE_BEHANDLING_KOBLI_6 on OPPGAVE_BEHANDLING_KOBLING(OPPGAVE_AARSAK);
create index IDX_RES_BEREGNINGSRESULTAT_F_6 on RES_BEREGNINGSRESULTAT_FP(BEHANDLING_ID);
create index IDX_RES_BEREGNINGSRESULTAT_F_7 on RES_BEREGNINGSRESULTAT_FP(BEREGNINGSRESULTAT_FP_ID);
create index IDX_SOEKNAD_6 on SOEKNAD(DEKNINGSGRAD_ID);
create index IDX_SOEKNAD_7 on SOEKNAD(FORDELING_ID);
create index IDX_SOEKNAD_8 on SOEKNAD(OPPGITT_OPPTJENING_ID);
create index IDX_SOEKNAD_9 on SOEKNAD(RETTIGHET_ID);
create index IDX_SOEKNAD_10 on SOEKNAD(FAR_SOEKER_TYPE);
create index IDX_STARTPUNKT_TYPE_6 on STARTPUNKT_TYPE(BEHANDLING_STEG);
create index IDX_VILKAR_6 on VILKAR(AVSLAG_KODE);
create index IDX_VILKAR_7 on VILKAR(VILKAR_UTFALL_MERKNAD);
CREATE INDEX IDX_VURDER_PAA_NYTT_AARSAK_6 ON VURDER_PAA_NYTT_AARSAK(AKSJONSPUNKT_ID);
create index IDX_VURDER_PAA_NYTT_AARSAK_7 on VURDER_PAA_NYTT_AARSAK(AARSAK_TYPE);