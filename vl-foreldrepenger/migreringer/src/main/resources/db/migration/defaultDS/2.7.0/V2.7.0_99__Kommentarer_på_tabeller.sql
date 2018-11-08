begin
  execute immediate 'DROP TABLE OMSORGSOVERTAKELSE';
  exception when others then
  if sqlcode != -942 then
    raise;
  end if;
end;
/

begin
  execute immediate 'DROP TABLE BARN';
  exception when others then
  if sqlcode != -942 then
    raise;
  end if;
end;
/

begin
  execute immediate 'DROP TABLE RELATERTE_YTELSER';
  exception when others then
  if sqlcode != -942 then
    raise;
  end if;
end;
/

begin
  execute immediate 'DROP TABLE BEHANDLING_REL_YTELSER';
  exception when others then
  if sqlcode != -942 then
    raise;
  end if;
end;
/

alter table MEDLEMSKAP_REGISTRERT drop column KILDE;
alter table MEDLEMSKAP_PERIODER add constraint FK_MEDLEMSKAP_PERIODER_1 foreign key (MEDLEMSKAP_REGISTRERT_ID) references MEDLEMSKAP_REGISTRERT;

-- Legger inn manglednde primærenøkler

COMMENT ON TABLE MEDLEMSKAP_VILKAR_PERIODE IS 'Aggregat for periodisering av vilkårsvurderingen for medlemskap';
COMMENT ON TABLE GR_MEDLEMSKAP_VILKAR_PERIODE IS 'Grunnlag for periodisering av vilkårsvurderingen for medlemskap';

COMMENT ON COLUMN MEDLEMSKAP_PERIODER.MEDLEMSKAP_REGISTRERT_ID IS 'FK til MEDLEMSKAP_REGISTRERT aggregatet';
COMMENT ON COLUMN MEDLEMSKAP_OPPG_TILKNYT.OPPGITT_DATO IS 'Datoen opplysningen ble oppgitt';
COMMENT ON COLUMN STARTPUNKT_TYPE.BESKRIVELSE IS 'Beskrivelse av startpunktet';

-- Tabeller og kolonner i beregningsgrunnlag, tilkjent ytelse og økonomioppdrag

COMMENT ON TABLE SAMMENLIGNINGSGRUNNLAG IS 'Sammenligningsgrunnlag';
COMMENT ON TABLE OPPDRAG_KVITTERING IS 'Kvittering fra økonomioppdrag';
COMMENT ON TABLE OKO_REFUSJONSINFO_156 IS 'Refusjonsinformasjon for økonomioppdrag';
COMMENT ON TABLE OKO_GRAD_170 IS 'Graderingsinformasjon for økonomioppdrag';
COMMENT ON TABLE BG_PR_STATUS_OG_ANDEL IS 'Beregningsgrunnlag pr status og andel';
COMMENT ON TABLE BG_AKTIVITET_STATUS IS 'Aktivitetsstatus i beregningsgrunnlag';
COMMENT ON TABLE BEREGNINGSRESULTAT_PERIODE IS 'Periode i tilkjent ytelse';
COMMENT ON TABLE BEREGNINGSRESULTAT_FP IS 'Aggregat for tilkjent ytelse';
COMMENT ON TABLE BEREGNINGSRESULTAT_ANDEL IS 'Andel i tilkjent ytelse';
COMMENT ON TABLE BEREGNINGSGRUNNLAG_PERIODE IS 'Beregningsgrunnlagsperiode';
COMMENT ON TABLE BEREGNINGSGRUNNLAG IS 'Aggregat for beregningsgrunnlag';

COMMENT ON COLUMN BEREGNINGSGRUNNLAG.REGELINPUT_BRUKERS_STATUS IS 'Input til beregningsregel for brukers status, JSON';
COMMENT ON COLUMN BEREGNINGSGRUNNLAG.REGELINPUT_SKJARINGSTIDSPUNKT IS 'Input til beregningsregel for skjæringstidspunkt, JSON';
COMMENT ON COLUMN BEREGNINGSGRUNNLAG.REGELLOGG_BRUKERS_STATUS IS 'Logg fra beregningsregel for brukers status, JSON';
COMMENT ON COLUMN BEREGNINGSGRUNNLAG.REGELLOGG_SKJARINGSTIDSPUNKT IS 'Logg fra beregningsregel for skjæringstidspunkt, JSON';
COMMENT ON COLUMN BEREGNINGSGRUNNLAG.SKJARINGSTIDSPUNKT IS 'Skjæringstidspunkt for beregning';
COMMENT ON COLUMN BEREGNINGSGRUNNLAG_PERIODE.AVKORTET_PR_AAR IS 'Avkortet beregningsgrunnlag';
COMMENT ON COLUMN BEREGNINGSGRUNNLAG_PERIODE.BG_PERIODE_FOM IS 'Første gyldighetsdag for beregningsgrunnlag';
COMMENT ON COLUMN BEREGNINGSGRUNNLAG_PERIODE.BG_PERIODE_TOM IS 'Siste gyldighetsdag for beregningsgrunnlag';
COMMENT ON COLUMN BEREGNINGSGRUNNLAG_PERIODE.BRUTTO_PR_AAR IS 'Beregningsgrunnlag, brutto';
COMMENT ON COLUMN BEREGNINGSGRUNNLAG_PERIODE.DAGSATS IS 'Dagsats, avrundet';
COMMENT ON COLUMN BEREGNINGSGRUNNLAG_PERIODE.PERIODE_AARSAK IS 'Årsakskode for periodesplitt';
COMMENT ON COLUMN BEREGNINGSGRUNNLAG_PERIODE.REDUSERT_PR_AAR IS 'Beregningsgrunnlag, redusert';
COMMENT ON COLUMN BEREGNINGSGRUNNLAG_PERIODE.REGEL_EVALUERING IS 'Logg fra beregningsregel foreslå beregningsgrunnlag, JSON';
COMMENT ON COLUMN BEREGNINGSGRUNNLAG_PERIODE.REGEL_EVALUERING_FASTSETT IS 'Logg fra beregningsregel fastsette beregningsgrunnlag, JSON';
COMMENT ON COLUMN BEREGNINGSGRUNNLAG_PERIODE.REGEL_INPUT IS 'Input til beregningsregel foreslå beregningsgrunnlag, JSON';
COMMENT ON COLUMN BEREGNINGSGRUNNLAG_PERIODE.REGEL_INPUT_FASTSETT IS 'Input til beregningsregel fastsette beregningsgrunnlag, JSON';
COMMENT ON COLUMN BEREGNINGSRESULTAT_ANDEL.ARBEIDSFORHOLD_ID IS 'Arbeidsforhold';
COMMENT ON COLUMN BEREGNINGSRESULTAT_ANDEL.BRUKER_ER_MOTTAKER IS 'Angir om bruker eller arbeidsgiver er mottaker';
COMMENT ON COLUMN BEREGNINGSRESULTAT_ANDEL.DAGSATS IS 'Dagsats for tilkjent ytelse';
COMMENT ON COLUMN BEREGNINGSRESULTAT_ANDEL.DAGSATS_FRA_BG IS 'Dagsats fra beregningsgrunnlag';
COMMENT ON COLUMN BEREGNINGSRESULTAT_ANDEL.STILLINGSPROSENT IS 'Stillingsprosent';
COMMENT ON COLUMN BEREGNINGSRESULTAT_ANDEL.UTTAKSGRAD IS 'Uttaksgrad';
COMMENT ON COLUMN BEREGNINGSRESULTAT_FP.REGEL_INPUT IS 'Input til beregningsregel for tilkjent ytelse, JSON';
COMMENT ON COLUMN BEREGNINGSRESULTAT_FP.REGEL_SPORING IS 'Logg fra beregningsregel for tilkjent ytelse, JSON';
COMMENT ON COLUMN BEREGNINGSRESULTAT_PERIODE.BR_PERIODE_FOM IS 'Første dag i periode for tilkjent ytelse';
COMMENT ON COLUMN BEREGNINGSRESULTAT_PERIODE.BR_PERIODE_TOM IS 'Siste dag i periode for tilkjent ytelse';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.AARSBELOEP_TILSTOETENDE_YTELSE IS 'Årsbeløp for tilstøtende ytelse';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.ARBEIDSFORHOLD_ID IS 'Arbeidsforhold';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.AVKORTET_BRUKERS_ANDEL_PR_AAR IS 'Brukers andel, avkortet';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.AVKORTET_PR_AAR IS 'Beregningsgrunnlagsandel, avkortet';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.AVKORTET_REFUSJON_PR_AAR IS 'Refusjon til arbeidsgiver, avkortet';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.BEREGNET_PR_AAR IS 'Beregningsgrunnlagsandel, beregnet';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.BEREGNINGSPERIODE_FOM IS 'Første dag i beregningsperiode';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.BEREGNINGSPERIODE_TOM IS 'Siste dag i beregningsperiode';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.BRUTTO_PR_AAR IS 'Beregningsgrunnlagsandel, brutto';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.DAGSATS_ARBEIDSGIVER IS 'Dagsats til arbeidsgiver';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.DAGSATS_BRUKER IS 'Dagsats til bruker';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.MAKSIMAL_BRUKERS_ANDEL_PR_AAR IS 'Maksimalverdi for brukers andel';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.MAKSIMAL_REFUSJON_PR_AAR IS 'Maksimalverdi for refusjon til arbeidsgiver';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.NATURALYTELSE_BORTFALT_PR_AAR IS 'Verdi av bortfalt naturalytelse';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.OVERSTYRT_PR_AAR IS 'Beregningsgrunnlagsandel, overstyrt';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.PGI_SNITT IS 'Gjennomsnittlig pensjonsgivende inntekt';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.PGI1 IS 'Pensjonsgivende inntekt i år 1';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.PGI2 IS 'Pensjonsgivende inntekt i år 2';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.PGI3 IS 'Pensjonsgivende inntekt i år 3';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.REDUSERT_BRUKERS_ANDEL_PR_AAR IS 'Brukers andel, redusert';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.REDUSERT_PR_AAR IS 'Beregningsgrunnlag, redusert';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.REDUSERT_REFUSJON_PR_AAR IS 'Refusjon til arbeidsgiver, redusert';
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.REFUSJONSKRAV_PR_AAR IS 'Arbeidsgivers refusjonskrav';
COMMENT ON COLUMN OKO_GRAD_170.GRAD IS 'Grad, prosent';
COMMENT ON COLUMN OKO_GRAD_170.TYPE_GRAD IS 'Hva slags grad som mottas';
COMMENT ON COLUMN OKO_REFUSJONSINFO_156.DATO_FOM IS 'Dato refusjonsinformasjon gjelder fra';
COMMENT ON COLUMN OKO_REFUSJONSINFO_156.MAKS_DATO IS 'Maks dato for ytelsen';
COMMENT ON COLUMN OKO_REFUSJONSINFO_156.REFUNDERES_ID IS 'Arbeidsgivers organisasjonsnummer';
COMMENT ON COLUMN OPPDRAG_KVITTERING.ALVORLIGHETSGRAD IS '00-Ok, 04-Ok med varsel, 08-Avvist av oppdrag, 12-Intern feil i oppdrag';
COMMENT ON COLUMN OPPDRAG_KVITTERING.BESKR_MELDING IS 'Feiltekst / meldingstekst fra økonomioppdrag';
COMMENT ON COLUMN OPPDRAG_KVITTERING.MELDING_KODE IS 'Feilmeldingskode fra økonomioppdrag';
COMMENT ON COLUMN SAMMENLIGNINGSGRUNNLAG.AVVIK_PROMILLE IS 'Avvik, promille';
COMMENT ON COLUMN SAMMENLIGNINGSGRUNNLAG.RAPPORTERT_PR_AAR IS 'Sammenligningsgrunnlag';
COMMENT ON COLUMN SAMMENLIGNINGSGRUNNLAG.SAMMENLIGNINGSPERIODE_FOM IS 'Første dag i sammenligningsperiode';
COMMENT ON COLUMN SAMMENLIGNINGSGRUNNLAG.SAMMENLIGNINGSPERIODE_TOM IS 'Siste dag i sammenligningsperiode';


COMMENT ON COLUMN AKSJONSPUNKT.SLETTES_VED_REGISTERINNHENTING IS 'Skal aksjonspunktet slettes ved no.nav.foreldrepenger.domene.registerinnhenting';
COMMENT ON COLUMN AKSJONSPUNKT_DEF.LAG_UTEN_HISTORIKK IS 'Skal det ikke lages historikkinnslag ved opprettelse av aksjonspunkt';
COMMENT ON COLUMN AKSJONSPUNKT_DEF.TILBAKEHOPP_VED_GJENOPPTAKELSE IS 'Skal det hoppes tilbake slik at steget aksjonspunktet er koblet til kjøres på nytt';
COMMENT ON COLUMN BEHANDLING_VEDTAK.BESLUTNING IS 'Er det tatt en beslutning i vedtaket';
COMMENT ON COLUMN DOKUMENT_MAL_TYPE.DOKSYS_KODE IS 'Doksys kodeverkskode';
COMMENT ON COLUMN VILKAR.MANUELT_VURDERT IS 'Er vilkåret manuelt vurdert';
COMMENT ON COLUMN VILKAR.MERKNAD_PARAMETERE IS 'Parametere til merknader';
COMMENT ON COLUMN VILKAR.OVERSTYRT IS 'Om vilkåret er overstyrt';
COMMENT ON COLUMN VILKAR.REGEL_INPUT IS 'Input til regel for vurdering av vilkåret';
COMMENT ON COLUMN VILKAR_RESULTAT.OVERSTYRT IS 'Om vilkårsresultatet er overstyrt';

COMMENT ON COLUMN MEDLEMSKAP_OPPG_LAND.TIDLIGERE_OPPHOLD IS 'Tidligere opphold i Norge (boolsk). Ja = Tidligere, Nei = Fremtidig';
