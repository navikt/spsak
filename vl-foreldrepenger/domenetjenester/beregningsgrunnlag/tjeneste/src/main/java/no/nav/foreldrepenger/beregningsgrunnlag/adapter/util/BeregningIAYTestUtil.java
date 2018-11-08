package no.nav.foreldrepenger.beregningsgrunnlag.adapter.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørInntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdHandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdOverstyringBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseStørrelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.FrilansEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@ApplicationScoped
public class BeregningIAYTestUtil {

    public static final AktørId AKTØR_ID = new AktørId("210195");

    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private BeregningArbeidsgiverTestUtil beregningArbeidsgiverTestUtil;
    private YtelsesFordelingRepository ytelsesFordelingRepository;

    BeregningIAYTestUtil() {
        // for CDI
    }

    @Inject
    public BeregningIAYTestUtil(BehandlingRepositoryProvider repositoryProvider,
                                BeregningArbeidsgiverTestUtil beregningArbeidsgiverTestUtil,
                                InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste) {
        inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        ytelsesFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();
        this.beregningArbeidsgiverTestUtil = beregningArbeidsgiverTestUtil;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
    }

    /**
     * Lager oppgitt opptjening for Selvstending næringsdrivende 6 måneder før skjæringstidspunkt med endringsdato en måned før skjæringstidspunkt.
     *
     * Setter virksomhetstype til udefinert som mapper til inntektskategori SELVSTENDING_NÆRINGSDRIVENDE.
     *
     * @param behandling aktuell behandling
     * @param skjæringstidspunktOpptjening skjæringstidpunkt for opptjening
     * @param nyIArbeidslivet spesifiserer om bruker er ny i arbeidslivet
     */
    public void lagOppgittOpptjeningForSN(Behandling behandling, LocalDate skjæringstidspunktOpptjening, boolean nyIArbeidslivet) {
        lagOppgittOpptjeningForSN(behandling, skjæringstidspunktOpptjening, nyIArbeidslivet, VirksomhetType.UDEFINERT);
    }

    /**
     * Lager oppgitt opptjening for Selvstending næringsdrivende 6 måneder før skjæringstidspunkt med endringsdato en måned før skjæringstidspunkt.
     *
     * @param behandling aktuell behandling
     * @param skjæringstidspunktOpptjening skjæringstidpunkt for opptjening
     * @param nyIArbeidslivet spesifiserer om bruker er ny i arbeidslivet
     * @param virksomhetType spesifiserer virksomhetstype for næringsvirksomheten
     */
    public void lagOppgittOpptjeningForSN(Behandling behandling, LocalDate skjæringstidspunktOpptjening, boolean nyIArbeidslivet, VirksomhetType virksomhetType) {
        OppgittOpptjeningBuilder.EgenNæringBuilder egenNæringBuilder = OppgittOpptjeningBuilder.EgenNæringBuilder.ny()
            .medBruttoInntekt(BigDecimal.valueOf(10000))
            .medNyIArbeidslivet(nyIArbeidslivet)
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(skjæringstidspunktOpptjening.minusMonths(6), skjæringstidspunktOpptjening))
            .medVirksomhetType(virksomhetType)
            .medEndringDato(skjæringstidspunktOpptjening.minusMonths(1));
        OppgittOpptjeningBuilder opptjeningBuilder = OppgittOpptjeningBuilder.ny()
            .leggTilEgneNæringer(Collections.singletonList(egenNæringBuilder));
        inntektArbeidYtelseRepository.lagre(behandling, opptjeningBuilder);
    }


    /**
     * Lager oppgitt opptjening for frilans.
     *
     * @param behandling aktuell behandling
     * @param erNyOppstartet spesifiserer om frilans er nyoppstartet
     */
    public void leggTilOppgittOpptjeningForFL(Behandling behandling, boolean erNyOppstartet) {
        OppgittOpptjeningBuilder oppgittOpptjeningBuilder = OppgittOpptjeningBuilder.ny();
        FrilansEntitet frilans = new FrilansEntitet();
        frilans.setErNyoppstartet(erNyOppstartet);
        oppgittOpptjeningBuilder
            .leggTilFrilansOpplysninger(frilans);
        inntektArbeidYtelseRepository.lagre(behandling, oppgittOpptjeningBuilder);
    }

    /**
     * Legger til oppgitt opptjening for FL og SN
     *
     * Legger til eit frilans arbeidsforhold.
     *
     * Legger til ein næringsvirksomhet.
     *
     * @param behandling aktuell behandling
     * @param skjæringstidspunktOpptjening skjæringstidspunkt for opptjening
     * @param erNyOppstartet spesifiserer om frilans er nyoppstartet
     * @param nyIArbeidslivet spesifiserer om bruker med selvstendig næring er ny i arbeidslivet
     */
    public void leggTilOppgittOpptjeningForFLOgSN(Behandling behandling, LocalDate skjæringstidspunktOpptjening ,boolean erNyOppstartet, boolean nyIArbeidslivet) {

        OppgittOpptjeningBuilder oppgittOpptjeningBuilder = OppgittOpptjeningBuilder.ny();
        OppgittOpptjeningBuilder.EgenNæringBuilder egenNæringBuilder = OppgittOpptjeningBuilder.EgenNæringBuilder.ny()
            .medBruttoInntekt(BigDecimal.valueOf(10000))
            .medNyIArbeidslivet(nyIArbeidslivet)
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(skjæringstidspunktOpptjening.minusMonths(6), skjæringstidspunktOpptjening))
            .medEndringDato(skjæringstidspunktOpptjening.minusMonths(1));
        FrilansEntitet frilans = new FrilansEntitet();
        frilans.setErNyoppstartet(erNyOppstartet);
        oppgittOpptjeningBuilder
            .leggTilFrilansOpplysninger(frilans).leggTilEgneNæringer(Collections.singletonList(egenNæringBuilder));
        inntektArbeidYtelseRepository.lagre(behandling, oppgittOpptjeningBuilder);
    }

    public void leggTilAktørytelse(Behandling behandling, LocalDate fom, LocalDate tom,  // NOSONAR - brukes bare til test
                                   RelatertYtelseTilstand relatertYtelseTilstand, String saksnummer, RelatertYtelseType ytelseType,
                                   List<YtelseStørrelse> ytelseStørrelseList, Arbeidskategori arbeidskategori) {
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder;
        inntektArbeidYtelseAggregatBuilder = inntektArbeidYtelseTjeneste.opprettBuilderForRegister(behandling);
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = inntektArbeidYtelseAggregatBuilder
            .getAktørYtelseBuilder(AKTØR_ID);
        YtelseBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(Fagsystem.INFOTRYGD, ytelseType,
            Saksnummer.infotrygd(saksnummer));
        ytelseBuilder.medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom));
        ytelseBuilder.medStatus(relatertYtelseTilstand);
        YtelseGrunnlagBuilder ytelseGrunnlagBuilder = ytelseBuilder.getGrunnlagBuilder()
            .medArbeidskategori(arbeidskategori);
        ytelseStørrelseList.forEach(størrelse -> ytelseGrunnlagBuilder.medYtelseStørrelse(størrelse));
        ytelseBuilder.medYtelseGrunnlag(ytelseGrunnlagBuilder.build());
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
        inntektArbeidYtelseRepository.lagre(behandling, inntektArbeidYtelseAggregatBuilder);
    }

    public void byggArbeidForBehandling(Behandling behandling, LocalDate skjæringstidspunktOpptjening, LocalDate fraOgMed, LocalDate tilOgMed, String arbId, Arbeidsgiver arbeidsgiver) {
        byggArbeidForBehandling(behandling, skjæringstidspunktOpptjening, fraOgMed, tilOgMed, arbId, arbeidsgiver, BigDecimal.TEN);
    }

    public void byggArbeidForBehandling(Behandling behandling, LocalDate skjæringstidspunktOpptjening, Periode periode, String arbId, Arbeidsgiver arbeidsgiver, BigDecimal inntektPrMnd) {
        byggArbeidForBehandling(behandling, skjæringstidspunktOpptjening, periode.getFom(), periode.getTomOrNull(), arbId, arbeidsgiver, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, false, inntektPrMnd, false);
    }

    public void byggArbeidForBehandling(Behandling behandling, LocalDate skjæringstidspunktOpptjening, LocalDate fraOgMed, LocalDate tilOgMed, String arbId, Arbeidsgiver arbeidsgiver, BigDecimal inntektPrMnd) {
        byggArbeidForBehandling(behandling, skjæringstidspunktOpptjening, fraOgMed, tilOgMed, arbId, arbeidsgiver, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, false, inntektPrMnd, false);
    }

    public void byggArbeidForBehandling(Behandling behandling, // NOSONAR - brukes bare til test
                                        LocalDate skjæringstidspunktOpptjening,
                                        LocalDate fraOgMed,
                                        LocalDate tilOgMed,
                                        String arbId,
                                        Arbeidsgiver arbeidsgiver, boolean medLønnsendring) {
        byggArbeidForBehandling(behandling, skjæringstidspunktOpptjening, fraOgMed, tilOgMed, arbId, arbeidsgiver, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, medLønnsendring, BigDecimal.TEN, false);
    }

    public void byggArbeidForBehandlingMedVirksomhetPåInntekt(Behandling behandling,
                                                              LocalDate skjæringstidspunktOpptjening,
                                                              LocalDate fraOgMed,
                                                              LocalDate tilOgMed,
                                                              String arbId,
                                                              Arbeidsgiver arbeidsgiver, BigDecimal inntektPrMnd) {
        byggArbeidForBehandling(behandling, skjæringstidspunktOpptjening, fraOgMed, tilOgMed, arbId,arbeidsgiver, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, false, inntektPrMnd, true);
    }

    public void byggArbeidForBehandling(Behandling behandling, // NOSONAR - brukes bare til test
                                        LocalDate skjæringstidspunktOpptjening,
                                        LocalDate fraOgMed,
                                        LocalDate tilOgMed,
                                        String arbId,
                                        Arbeidsgiver arbeidsgiver, ArbeidType arbeidType,
                                        boolean medLønnsendring,
                                        BigDecimal inntektPrMnd,
                                        boolean virksomhetPåInntekt) {
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder;
        inntektArbeidYtelseAggregatBuilder = inntektArbeidYtelseTjeneste.opprettBuilderForRegister(behandling);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder.getAktørArbeidBuilder(behandling.getAktørId());
        YrkesaktivitetBuilder yrkesaktivitetBuilder = hentYABuilder(arbId, arbeidsgiver, arbeidType, aktørArbeidBuilder);

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder()
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed))
            .medProsentsats(BigDecimal.TEN)
            .medSisteLønnsendringsdato(medLønnsendring ? skjæringstidspunktOpptjening.minusMonths(2L) : null)
            .medAntallTimer(BigDecimal.valueOf(20.4d))
            .medAntallTimerFulltid(BigDecimal.valueOf(10.2d));
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder arbeidsperiode = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder()
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed));

        yrkesaktivitetBuilder
            .medArbeidType(arbeidType)
            .medArbeidsgiver(arbeidsgiver)
            .leggTilAktivitetsAvtale(aktivitetsAvtale)
            .leggTilAktivitetsAvtale(arbeidsperiode);
        if (arbId != null) {
            yrkesaktivitetBuilder.medArbeidsforholdId(ArbeidsforholdRef.ref(arbId));
        }

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);

        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeid);
        byggInntektForBehandling(behandling, skjæringstidspunktOpptjening, inntektArbeidYtelseAggregatBuilder, inntektPrMnd, virksomhetPåInntekt, arbeidsgiver);
        inntektArbeidYtelseTjeneste.lagre(behandling, inntektArbeidYtelseAggregatBuilder);
        opprettYtelsesFordeling(behandling, skjæringstidspunktOpptjening);
        if (medLønnsendring) {
            InntektArbeidYtelseGrunnlag grunnlag = inntektArbeidYtelseTjeneste.hentAggregat(behandling);
            Optional<AktørArbeid> aktørInntektForFørStp = grunnlag.getAktørArbeidFørStp(behandling.getAktørId());
            if (aktørInntektForFørStp.isPresent()) {
                Yrkesaktivitet yrkesaktivitet = aktørInntektForFørStp.get().getYrkesaktiviteter() // NOSONAR
                    .stream()
                    .filter(ya -> ya.getArbeidsgiver().equals(arbeidsgiver))
                    .findFirst().get();
                final ArbeidsforholdInformasjonBuilder informasjonBuilder = inntektArbeidYtelseRepository.opprettInformasjonBuilderFor(behandling);
                final ArbeidsforholdOverstyringBuilder overstyringBuilderFor = informasjonBuilder.getOverstyringBuilderFor(yrkesaktivitet.getArbeidsgiver(), yrkesaktivitet.getArbeidsforholdRef().orElse(ArbeidsforholdRef.ref(null)));
                overstyringBuilderFor.medHandling(ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING);
                informasjonBuilder.leggTil(overstyringBuilderFor);
                inntektArbeidYtelseRepository.lagre(behandling, informasjonBuilder);
            }
        }
    }

    private YrkesaktivitetBuilder hentYABuilder(String arbId, Arbeidsgiver arbeidsgiver, ArbeidType arbeidType, InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder) {
        if (arbId == null) {
            return aktørArbeidBuilder.getYrkesaktivitetBuilderForType(arbeidType);
        } else return arbeidsgiver.getErVirksomhet()
            ? aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(new Opptjeningsnøkkel(arbId, arbeidsgiver.getVirksomhet().getOrgnr(), null), arbeidType)
            : aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(new Opptjeningsnøkkel(arbId, null, arbeidsgiver.getAktørId().getId()), arbeidType);

    }

    private void opprettYtelsesFordeling(Behandling behandling, LocalDate skjæringstidspunktOpptjening) {
        OppgittPeriode uttakPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(skjæringstidspunktOpptjening.plusDays(1), skjæringstidspunktOpptjening.plusMonths(6))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .build();
        ytelsesFordelingRepository.lagre(behandling, new OppgittFordelingEntitet(Collections.singletonList(uttakPeriode), true));
    }

    private void byggInntektForBehandling(Behandling behandling, LocalDate skjæringstidspunktOpptjening,
                                          InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, BigDecimal inntektPrMnd, boolean virksomhetPåInntekt, Arbeidsgiver arbeidsgiver) {

        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntekt = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(behandling.getAktørId());

        AktørInntektEntitet.InntektBuilder inntektBeregningBuilder = aktørInntekt
            .getInntektBuilder(InntektsKilde.INNTEKT_BEREGNING, Opptjeningsnøkkel.forArbeidsforholdIdMedArbeidgiver(null, arbeidsgiver));

        // Lager et år (12 mnd) med inntekt for beregning
        byggInntekt(inntektBeregningBuilder, skjæringstidspunktOpptjening, inntektPrMnd, virksomhetPåInntekt, arbeidsgiver);
        aktørInntekt.leggTilInntekt(inntektBeregningBuilder);


        AktørInntektEntitet.InntektBuilder inntektSammenligningBuilder = aktørInntekt
            .getInntektBuilder(InntektsKilde.INNTEKT_SAMMENLIGNING, Opptjeningsnøkkel.forArbeidsforholdIdMedArbeidgiver(null, arbeidsgiver));

        // Lager et år (12 mnd) med inntekt for sammenligningsgrunnlag
        byggInntekt(inntektSammenligningBuilder, skjæringstidspunktOpptjening, inntektPrMnd, virksomhetPåInntekt, arbeidsgiver);
        aktørInntekt.leggTilInntekt(inntektSammenligningBuilder);

        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntekt);
        inntektArbeidYtelseTjeneste.lagre(behandling, inntektArbeidYtelseAggregatBuilder);
        }
    private void byggInntekt(AktørInntektEntitet.InntektBuilder builder, LocalDate skjæringstidspunktOpptjening, BigDecimal inntektPrMnd, boolean virksomhetPåInntekt, Arbeidsgiver arbeidsgiver) {
        if (virksomhetPåInntekt) {
            for (int i = 0; i<=12; i++) {
                builder.leggTilInntektspost(lagInntektspost(skjæringstidspunktOpptjening.minusMonths(i+1L), skjæringstidspunktOpptjening.minusMonths(i), inntektPrMnd)).medArbeidsgiver(arbeidsgiver);
            }
        } else {
            for (int i = 0; i<=12; i++) {
                builder.leggTilInntektspost(lagInntektspost(skjæringstidspunktOpptjening.minusMonths(i+1L), skjæringstidspunktOpptjening.minusMonths(i), inntektPrMnd));
            }
        }
    }

    private InntektEntitet.InntektspostBuilder lagInntektspost(LocalDate fom, LocalDate tom, BigDecimal lønn) {
        return InntektEntitet.InntektspostBuilder.ny()
            .medBeløp(lønn)
            .medPeriode(fom, tom)
            .medInntektspostType(InntektspostType.LØNN);
    }
}
