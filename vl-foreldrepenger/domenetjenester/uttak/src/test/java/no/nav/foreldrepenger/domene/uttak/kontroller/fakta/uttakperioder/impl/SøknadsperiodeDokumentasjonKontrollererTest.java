package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.impl;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.GraderingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.UtsettelsePeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PeriodeUttakDokumentasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.UttakDokumentasjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidFeil;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjeneste;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.ArbeidPåHeltidTjeneste;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakBeregningsandelTjeneste;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl.ArbeidPåHeltidTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaPeriode;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class SøknadsperiodeDokumentasjonKontrollererTest {

    private static final LocalDate FOM = LocalDate.of(2018, 1, 14);
    private static final LocalDate TOM = LocalDate.of(2018, 1, 31);
    private final LocalDate enDag = LocalDate.of(2018, 3, 15);
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
    @Inject
    private VirksomhetRepository virksomhetRepository;

    @Before
    public void oppsett() {
    }

    @Test
    public void skal_si_at_utsettelse_pga_sykdom_trenger_bekreftelse() {
        assertThat(kontrollerUtenInntektsmeldinger(arbeidstakerPeriodeMedUtsettelseSykdom()).erBekreftet()).isFalse();
        assertThat(kontrollerUtenInntektsmeldingerIkkeArbeidstaker(frilansNæringsdrivendePeriodeMedUtsettelseSykdom()).erBekreftet()).isFalse();
    }

    @Test
    public void skal_si_at_periode_er_bekreftet_når_saksbehandler_allerede_er_vurdert() {
        OppgittPeriodeBuilder vurdertPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(FOM, TOM)
            .medVurdering(UttakPeriodeVurderingType.PERIODE_OK);
        KontrollerFaktaPeriode kontrollert = kontrollerUtenInntektsmeldinger(vurdertPeriode.build());

        assertThat(kontrollert.erBekreftet()).isTrue();
    }

    @Test
    public void skal_ta_med_dokumentasjonsperioder_når_saksbehandler_har_behandlet_perioden() {
        Virksomhet virksomhet = virksomhet("orgnr");
        OppgittPeriode søktPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medÅrsak(UtsettelseÅrsak.SYKDOM)
            .medPeriode(enDag, enDag.plusDays(7))
            .medBegrunnelse("erstatter")
            .medVurdering(UttakPeriodeVurderingType.PERIODE_OK)
            .medVirksomhet(virksomhet)
            .medErArbeidstaker(true)
            .build();

        List<PeriodeUttakDokumentasjon> dokumentasjonPerioder = Arrays.asList(
            lagDokumentasjon(enDag, enDag.plusDays(1), UttakDokumentasjonType.SYK_SØKER),
            lagDokumentasjon(enDag.plusDays(4), enDag.plusDays(7), UttakDokumentasjonType.SYK_SØKER)
        );

        SøknadsperiodeDokumentasjonKontrollerer kontrollerer = new SøknadsperiodeDokumentasjonKontrollerer(dokumentasjonPerioder,
            Collections.emptyList(), null, null, true);
        KontrollerFaktaPeriode resultat = kontrollerer.kontrollerSøknadsperiode(søktPeriode);

        assertThat(resultat.getBegrunnelse()).isEqualTo("erstatter");
        assertThat(resultat.erBekreftet()).isTrue();
        assertThat(resultat.getDokumentertePerioder()).hasSize(2);
        assertThat(resultat.getDokumentertePerioder().get(0).getDokumentasjonType()).isEqualTo(UttakDokumentasjonType.SYK_SØKER);
        assertThat(resultat.getDokumentertePerioder().get(0).getPeriode().getFomDato()).isEqualTo(enDag);
        assertThat(resultat.getDokumentertePerioder().get(0).getPeriode().getTomDato()).isEqualTo(enDag.plusDays(1));
        assertThat(resultat.getDokumentertePerioder().get(1).getDokumentasjonType()).isEqualTo(UttakDokumentasjonType.SYK_SØKER);
        assertThat(resultat.getDokumentertePerioder().get(1).getPeriode().getFomDato()).isEqualTo(enDag.plusDays(4));
        assertThat(resultat.getDokumentertePerioder().get(1).getPeriode().getTomDato()).isEqualTo(enDag.plusDays(7));
    }

    private PeriodeUttakDokumentasjon lagDokumentasjon(LocalDate fom, LocalDate tom, UttakDokumentasjonType type) {
        PeriodeUttakDokumentasjon periode = Mockito.mock(PeriodeUttakDokumentasjon.class);
        when(periode.getDokumentasjonType()).thenReturn(type);
        when(periode.getPeriode()).thenReturn(DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom));
        return periode;
    }

    @Test
    public void skal_håndtere_utsettelse_pga_arbeid_uten_inntektsmelding() {
        //må til manuelt når de ikke finnes inntektsmelding
        assertThat(kontrollerUtenInntektsmeldinger(arbeidstakerPeriodeMedUtsettelseArbeid()).erBekreftet()).isFalse();
        assertThat(kontrollerUtenInntektsmeldingerIkkeArbeidstaker(frilansNæringsdrivendePeriodeMedUtsettelseArbeid()).erBekreftet()).isTrue();
    }

    @Test
    public void feriePeriodeFraSøknadOgInntektsmeldlingStemmer() {
        Virksomhet virksomhet = virksomhet("orgnr");
        OppgittPeriode feriePeriode = OppgittPeriodeBuilder.ny()
            .medÅrsak(UtsettelseÅrsak.FERIE)
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medVirksomhet(virksomhet)
            .medErArbeidstaker(true)
            .medPeriode(LocalDate.of(2018, 1, 13), LocalDate.of(2018, 1, 20))
            .build();

        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet)
            .leggTil(UtsettelsePeriodeEntitet.ferie(LocalDate.of(2018, 1, 15), LocalDate.of(2018, 1, 19)))
            .build();

        KontrollerFaktaPeriode kontrollert = kontroller(feriePeriode, Collections.singletonList(inntektsmelding));
        assertThat(kontrollert.erBekreftet()).isTrue();
    }

    @Test
    public void flereArbeidsforholdOgInntektsmeldingerMenKunEnGyldig() {
        Virksomhet virksomhet = virksomhet("org");
        OppgittPeriode feriePeriode = OppgittPeriodeBuilder.ny()
            .medVirksomhet(virksomhet)
            .medÅrsak(UtsettelseÅrsak.ARBEID)
            .medErArbeidstaker(true)
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medPeriode(LocalDate.of(2018, 1, 13), LocalDate.of(2018, 1, 20))
            .build();

        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet)
            .leggTil(UtsettelsePeriodeEntitet.utsettelse(LocalDate.of(2018, 1, 15), LocalDate.of(2018, 1, 19), UtsettelseÅrsak.ARBEID))
            .build();

        ArbeidPåHeltidTjeneste arbeidPåHeltidTjeneste = lagArbeidPåHeltidTjeneste(LocalDate.of(2017, 01, 01), LocalDate.of(2019, 01, 01), BigDecimal.valueOf(50L),
            LocalDate.of(2017, 01, 01), LocalDate.of(2018, 01, 14), BigDecimal.valueOf(50L), virksomhet);

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(feriePeriode, Collections.singletonList(inntektsmelding), arbeidPåHeltidTjeneste, true);
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isTrue();
    }

    @Test
    public void flereArbeidsforholdOgInntektsmeldingerBeggeGyldige() {
        Virksomhet virksomhet = virksomhet("orgnr123");

        OppgittPeriode feriePeriode = OppgittPeriodeBuilder.ny()
            .medÅrsak(UtsettelseÅrsak.ARBEID)
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medVirksomhet(virksomhet)
            .medErArbeidstaker(true)
            .medPeriode(LocalDate.of(2018, 1, 13), LocalDate.of(2018, 1, 20))
            .build();

        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet)
            .leggTil(UtsettelsePeriodeEntitet.utsettelse(LocalDate.of(2018, 1, 15), LocalDate.of(2018, 1, 19), UtsettelseÅrsak.ARBEID))
            .build();

        ArbeidPåHeltidTjeneste arbeidPåHeltidTjeneste = lagArbeidPåHeltidTjeneste(LocalDate.of(2017, 01, 01), LocalDate.of(2019, 01, 01), BigDecimal.valueOf(50L),
            LocalDate.of(2017, 01, 01), LocalDate.of(2018, 01, 20), BigDecimal.valueOf(50L), virksomhet);

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(feriePeriode, Collections.singletonList(inntektsmelding), arbeidPåHeltidTjeneste, true);
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isTrue();
    }

    private Virksomhet virksomhet(String virksomhetId) {
        return new VirksomhetEntitet.Builder().medOrgnr(virksomhetId).oppdatertOpplysningerNå().build();
    }

    private Virksomhet virksomhet() {
        return virksomhet(UUID.randomUUID().toString());
    }

    @Test
    public void feriePeriodeFraSøknadOgInntektsmeldingAvviker() {
        Virksomhet virksomhet = virksomhet("orgnr");
        OppgittPeriode feriePeriode = OppgittPeriodeBuilder.ny()
            .medÅrsak(UtsettelseÅrsak.FERIE)
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medPeriode(FOM, TOM)
            .medErArbeidstaker(true)
            .medVirksomhet(virksomhet)
            .build();

        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet)
            .leggTil(UtsettelsePeriodeEntitet.ferie(FOM.plusWeeks(1), TOM))
            .build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(feriePeriode, singletonList(inntektsmelding));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isFalse();
    }

    @Test
    public void søktOmGraderingIngenGraderingPåInntektsmelding() {
        Virksomhet virksomhet = virksomhet("orgnr");
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medErArbeidstaker(true)
            .medVirksomhet(virksomhet)
            .medArbeidsprosent(BigDecimal.valueOf(50))
            .build();

        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet).build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(graderingPeriode, singletonList(inntektsmelding));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isFalse();
    }

    @Test
    public void farEllerMedmorSøktOmTidligOppstartFellesperiodeEllerFedrekvote() {
        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medPeriode(LocalDate.now(), LocalDate.now().plusWeeks(1))
            .build();

        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet("orgnr")).build();

        SøknadsperiodeDokumentasjonKontrollerer kontrollerer = new SøknadsperiodeDokumentasjonKontrollerer(Collections.emptyList(),
            Collections.singletonList(inntektsmelding), lagArbeidPåHeltidTjeneste(), LocalDate.now().minusWeeks(5), false);

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontrollerer.kontrollerSøknadsperiode(oppgittPeriode);
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isFalse();
        assertThat(kontrollerFaktaPeriode.isTidligOppstart()).isTrue();
    }

    @Test
    public void farEllerMedmorIkkeSøktOmTidligOppstartFellesperiodeEllerFedrekvote() {
        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FEDREKVOTE)
            .medPeriode(LocalDate.now(), LocalDate.now().plusWeeks(1))
            .build();

        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet("orgnr")).build();

        SøknadsperiodeDokumentasjonKontrollerer kontrollerer = new SøknadsperiodeDokumentasjonKontrollerer(Collections.emptyList(),
            Collections.singletonList(inntektsmelding), lagArbeidPåHeltidTjeneste(), LocalDate.now().minusWeeks(7), false);

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontrollerer.kontrollerSøknadsperiode(oppgittPeriode);
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isTrue();
        assertThat(kontrollerFaktaPeriode.isTidligOppstart()).isFalse();
    }

    @Test
    public void graderingPåInntektsmeldingenMenIkkeSøktOmGradering() {
        Virksomhet virksomhet = virksomhet("orgnr");
        OppgittPeriode periode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .build();

        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .leggTil(new GraderingEntitet(FOM, TOM, BigDecimal.TEN))
            .medVirksomhet(virksomhet).build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(periode, singletonList(inntektsmelding));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isFalse();
    }

    @Test
    public void graderingPåInntektsmeldingenMenIkkeSøktOmGraderingSøknadsperiodeOmslutterInntektsmelding() {
        Virksomhet virksomhet = virksomhet("orgnr");
        OppgittPeriode periode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM.minusWeeks(1), TOM.plusWeeks(1))
            .build();

        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .leggTil(new GraderingEntitet(FOM, TOM, BigDecimal.TEN))
            .medVirksomhet(virksomhet).build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(periode, singletonList(inntektsmelding));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isFalse();
    }

    @Test
    public void utsettelseHosBareEnVirksomhetIInntektsmeldingenMenIkkeSøktOm() {
        Virksomhet virksomhet = virksomhet("orgnr1");
        OppgittPeriode periode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .build();

        Inntektsmelding inntektsmelding1 = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet)
            .build();

        Inntektsmelding inntektsmelding2 = getInntektsmeldingBuilder()
            .leggTil(new GraderingEntitet(FOM.plusDays(4), TOM, BigDecimal.TEN))
            .medVirksomhet(virksomhet("orgnr2"))
            .build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(periode, Arrays.asList(inntektsmelding1, inntektsmelding2));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isFalse();
    }

    @Test
    public void søktOmGraderingPeriodeStemmerIkkeMedInntektsmelding() {
        Virksomhet virksomhet = virksomhet("orgnr");
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medArbeidsprosent(BigDecimal.valueOf(60))
            .medVirksomhet(virksomhet)
            .medErArbeidstaker(true)
            .build();

        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet)
            .leggTil(new GraderingEntitet(LocalDate.of(2018, 1, 14),
                LocalDate.of(2018, 2, 10),
                BigDecimal.valueOf(40)))
            .build();
        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(graderingPeriode, singletonList(inntektsmelding));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isFalse();
    }

    @Test
    public void ikkeSøktOmGraderingMenGraderingIInntektsmeldingen() {
        Virksomhet virksomhet = virksomhet("orgnr");
        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .build();

        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet)
            .leggTil(new GraderingEntitet(FOM, TOM, BigDecimal.valueOf(40)))
            .build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(oppgittPeriode, singletonList(inntektsmelding));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isFalse();
    }

    @Test
    public void graderingPåSøknadsperiodeOgInntektsmeldingStemmerOverens() {
        Virksomhet virksomhet = virksomhet("orgnr");
        BigDecimal arbeidsprosent = BigDecimal.valueOf(60);
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medVirksomhet(virksomhet)
            .medArbeidsprosent(arbeidsprosent)
            .medErArbeidstaker(true)
            .build();

        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet)
            .leggTil(new GraderingEntitet(FOM, TOM, arbeidsprosent))
            .build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(graderingPeriode, singletonList(inntektsmelding));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isTrue();
    }

    @Test
    public void gradertFrilansUtenVirksomhetOgHarITilleggEttArbeidsforhold() {
        Virksomhet virksomhet = virksomhet("orgnr");
        BigDecimal arbeidsprosent = BigDecimal.valueOf(60);
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medVirksomhet(null)
            .medArbeidsprosent(arbeidsprosent)
            .medErArbeidstaker(false)
            .build();

        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet)
            .build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontrollerIkkeArbeidstaker(graderingPeriode, singletonList(inntektsmelding));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isTrue();
    }

    @Test
    public void gradertFrilansMedVirksomhetOgHarITilleggEttArbeidsforhold() {
        Virksomhet virksomhet1 = virksomhet("orgnr1");
        Virksomhet virksomhet2 = virksomhet("orgnr2");
        BigDecimal arbeidsprosent = BigDecimal.valueOf(60);
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medVirksomhet(virksomhet1)
            .medArbeidsprosent(arbeidsprosent)
            .medErArbeidstaker(false)
            .build();

        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet2)
            .build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontrollerIkkeArbeidstaker(graderingPeriode, singletonList(inntektsmelding));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isTrue();
    }

    @Test
    public void graderingPåSøknadsperiodeOgInntektsmeldingStemmerOverensMedFlereInntektsmeldinger() {
        Virksomhet virksomhet = virksomhet("orgnr");
        BigDecimal arbeidsprosent = BigDecimal.valueOf(60);
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medVirksomhet(virksomhet)
            .medArbeidsprosent(arbeidsprosent)
            .build();

        Inntektsmelding inntektsmelding1 = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet)
            .leggTil(new GraderingEntitet(FOM, TOM, arbeidsprosent))
            .build();

        Inntektsmelding inntektsmelding2 = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet)
            .leggTil(new GraderingEntitet(TOM.plusDays(1), TOM.plusWeeks(1), BigDecimal.TEN))
            .build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(graderingPeriode, Arrays.asList(inntektsmelding1, inntektsmelding2));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isTrue();
    }

    @Test
    public void ikkeGraderingPåSøknadsperiodeOgInntektsmeldingStemmerOverensOgInntektsmeldingOmslutterSøknadsperiode() {
        Virksomhet virksomhet = virksomhet("orgnr");
        BigDecimal arbeidsprosent = BigDecimal.valueOf(60);
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medVirksomhet(virksomhet)
            .medArbeidsprosent(arbeidsprosent)
            .build();

        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet)
            .leggTil(new GraderingEntitet(FOM.minusWeeks(1), TOM.plusWeeks(1), arbeidsprosent))
            .build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(graderingPeriode, singletonList(inntektsmelding));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isTrue();
    }

    @Test
    public void toInntektsmeldingerHarForskjelligeUtsetteler() {
        Virksomhet virksomhet1 = virksomhet("orgnr1");
        Virksomhet virksomhet2 = virksomhet("orgnr2");

        Inntektsmelding inntektsmelding1 = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet1)
            .leggTil(UtsettelsePeriodeEntitet.ferie(FOM, TOM))
            .build();

        Inntektsmelding inntektsmelding2 = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet2)
            .leggTil(UtsettelsePeriodeEntitet.ferie(FOM.plusWeeks(1), TOM))
            .build();

        OppgittPeriode oppgittUtsettelse = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medÅrsak(UtsettelseÅrsak.FERIE)
            .medVirksomhet(virksomhet1)
            .medErArbeidstaker(true)
            .build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(oppgittUtsettelse, Arrays.asList(inntektsmelding1, inntektsmelding2));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isFalse();
    }

    @Test
    public void utsettelseUtenVirksomhetISøknadStemmerMedInntektsmeldingEnArbeidsgiver() {
        Virksomhet virksomhet = virksomhet("orgnr1");

        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet)
            .leggTil(UtsettelsePeriodeEntitet.ferie(FOM, TOM))
            .build();

        OppgittPeriode oppgittUtsettelse = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medÅrsak(UtsettelseÅrsak.FERIE)
            .build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(oppgittUtsettelse, singletonList(inntektsmelding));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isTrue();
    }

    @Test
    public void arbeidsprosentPåInntektsmeldingStemmerIkkeMedGraderingsprosent() {
        Virksomhet virksomhet = virksomhet("orgnr");
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medArbeidsprosent(BigDecimal.valueOf(40))
            .medVirksomhet(virksomhet)
            .medErArbeidstaker(true)
            .build();

        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet)
            .leggTil(new GraderingEntitet(LocalDate.of(2018, 1, 14),
                LocalDate.of(2018, 1, 21),
                BigDecimal.valueOf(60)))
            .build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(graderingPeriode, singletonList(inntektsmelding));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isFalse();
    }

    @Test
    public void ikkeSøktOmGraderingMenInntektsmeldingHarGraderingSomOmslutterSøknadsperiodeSkalGiAksjonspunkt() {
        Virksomhet virksomhet = virksomhet("orgnr");
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .build();

        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet)
            .leggTil(new GraderingEntitet(FOM.minusWeeks(1),
                TOM.plusWeeks(1),
                BigDecimal.valueOf(60)))
            .build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(graderingPeriode, singletonList(inntektsmelding));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isFalse();
    }

    @Test
    public void ikkeSøktOmGraderingMenInntektsmeldingHarGraderingSomStarterIOgSlutterEtterISøknadsperiodeSkalGiAksjonspunkt() {
        Virksomhet virksomhet = virksomhet("orgnr");
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(FOM, TOM)
            .build();

        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet)
            .leggTil(new GraderingEntitet(FOM.plusWeeks(1),
                TOM.plusWeeks(1),
                BigDecimal.valueOf(60)))
            .build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(graderingPeriode, singletonList(inntektsmelding));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isFalse();
    }

    @Test
    public void søktOmGraderingErIkkeArbeidstaker() {
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medArbeidsprosent(BigDecimal.valueOf(60))
            .medErArbeidstaker(false)
            .build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontrollerIkkeArbeidstaker(graderingPeriode, Collections.emptyList());
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isTrue();
    }

    @Test
    public void søktOmGraderingSelvstendigNæringsdrivende() {
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medArbeidsprosent(BigDecimal.valueOf(60))
            .medErArbeidstaker(false)
            .build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontrollerIkkeArbeidstaker(graderingPeriode, Collections.emptyList());
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isTrue();
    }

    @Test
    public void arbeidsprosentPåInntektsmeldingStemmerIkkeMedGraderingsprosentPåAlleArbeidsforhold() {
        Virksomhet virksomhet = virksomhet("orgnr");
        BigDecimal arbeidsprosent1 = BigDecimal.valueOf(60);
        BigDecimal arbeidsprosent2 = BigDecimal.valueOf(45);
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medVirksomhet(virksomhet)
            .medArbeidsprosent(arbeidsprosent1)
            .medErArbeidstaker(true)
            .build();

        Inntektsmelding inntektsmelding1 = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet)
            .leggTil(new GraderingEntitet(FOM, TOM, arbeidsprosent1))
            .build();

        Inntektsmelding inntektsmelding2 = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet)
            .leggTil(new GraderingEntitet(FOM, TOM, arbeidsprosent2))
            .build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(graderingPeriode, Arrays.asList(inntektsmelding1, inntektsmelding2));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isFalse();
    }

    @Test
    public void virksomhetStemmerIkkeOverensMedInntektsmeldingen() {
        Virksomhet virksomhet = virksomhet("orgnr1");
        BigDecimal arbeidsprosent = BigDecimal.valueOf(50);
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medVirksomhet(virksomhet)
            .medArbeidsprosent(arbeidsprosent)
            .medErArbeidstaker(true)
            .build();

        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .leggTil(new GraderingEntitet(FOM, TOM, arbeidsprosent))
            .medVirksomhet(virksomhet("orgnr2")).build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(graderingPeriode, singletonList(inntektsmelding));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isFalse();
    }

    @Test
    public void graderingPåSøknadUtenInntektsmeldingerSkalGiAksjonspunkt() {
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medVirksomhet(virksomhet("orgnr"))
            .medArbeidsprosent(BigDecimal.valueOf(60))
            .medErArbeidstaker(true)
            .build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(graderingPeriode, Collections.emptyList());
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isFalse();
    }

    @Test
    public void graderingPåSøknadOgPåToInntektsmeldingerMedForskjelligVirksomhetSkalGiAksjonspunkt() {
        BigDecimal arbeidsprosent = BigDecimal.valueOf(60);
        Virksomhet virksomhet1 = virksomhet("orgnr1");
        Virksomhet virksomhet2 = virksomhet("orgnr2");
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medVirksomhet(virksomhet1)
            .medArbeidsprosent(arbeidsprosent)
            .medErArbeidstaker(true)
            .build();

        Inntektsmelding inntektsmelding1 = getInntektsmeldingBuilder()
            .leggTil(new GraderingEntitet(FOM, TOM, arbeidsprosent))
            .medVirksomhet(virksomhet1).build();
        Inntektsmelding inntektsmelding2 = getInntektsmeldingBuilder()
            .leggTil(new GraderingEntitet(FOM, TOM, arbeidsprosent))
            .medVirksomhet(virksomhet2).build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(graderingPeriode, Arrays.asList(inntektsmelding1, inntektsmelding2));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isFalse();
    }

    @Test
    public void utsettelseArbeidPåSøknadUtenInntektsmeldingerSkalGiAksjonspunkt() {
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medÅrsak(UtsettelseÅrsak.ARBEID)
            .medErArbeidstaker(true)
            .medVirksomhet(virksomhet())
            .build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(graderingPeriode, Collections.emptyList());
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isFalse();
    }

    @Test
    public void utsettelseArbeidPåSøknadOgPåToInntektsmeldingerMedForskjelligVirksomhetSkalIkkeGiAksjonspunkt() {
        Virksomhet virksomhet1 = virksomhet("orgnr1");
        Virksomhet virksomhet2 = virksomhet("orgnr2");
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medÅrsak(UtsettelseÅrsak.ARBEID)
            .medErArbeidstaker(true)
            .medVirksomhet(virksomhet2)
            .build();

        Inntektsmelding inntektsmelding1 = getInntektsmeldingBuilder()
            .leggTil(UtsettelsePeriodeEntitet.utsettelse(FOM, TOM, UtsettelseÅrsak.ARBEID))
            .medVirksomhet(virksomhet1).build();
        Inntektsmelding inntektsmelding2 = getInntektsmeldingBuilder()
            .leggTil(UtsettelsePeriodeEntitet.utsettelse(FOM, TOM, UtsettelseÅrsak.ARBEID))
            .medVirksomhet(virksomhet2).build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(graderingPeriode, Arrays.asList(inntektsmelding1, inntektsmelding2));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isTrue();
    }

    @Test
    public void utsettelseFeriePåSøknadUtenInntektsmeldingerSkalGiAksjonspunkt() {
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medÅrsak(UtsettelseÅrsak.ARBEID)
            .medVirksomhet(virksomhet())
            .medErArbeidstaker(true)
            .build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(graderingPeriode, Collections.emptyList());
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isFalse();
    }

    @Test
    public void utsettelseFeriePåSøknadOgPåToInntektsmeldingerMedForskjelligVirksomhetIkkeSkalGiAksjonspunkt() {
        Virksomhet virksomhet1 = virksomhet("orgnr1");
        Virksomhet virksomhet2 = virksomhet("orgnr2");
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medÅrsak(UtsettelseÅrsak.FERIE)
            .medVirksomhet(virksomhet1)
            .medErArbeidstaker(true)
            .build();

        Inntektsmelding inntektsmelding1 = getInntektsmeldingBuilder()
            .leggTil(UtsettelsePeriodeEntitet.utsettelse(FOM, TOM, UtsettelseÅrsak.FERIE))
            .medVirksomhet(virksomhet1).build();
        Inntektsmelding inntektsmelding2 = getInntektsmeldingBuilder()
            .leggTil(UtsettelsePeriodeEntitet.utsettelse(FOM, TOM, UtsettelseÅrsak.FERIE))
            .medVirksomhet(virksomhet2).build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(graderingPeriode, Arrays.asList(inntektsmelding1, inntektsmelding2));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isTrue();
    }

    @Test
    public void utsettelseFeriePåSøknadMenUtsettelseArbeidPåInntektsmeldingSkalGiAksjonspunkt() {
        Virksomhet virksomhet1 = virksomhet("orgnr1");
        Virksomhet virksomhet2 = virksomhet("orgnr2");
        OppgittPeriode graderingPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(FOM, TOM)
            .medÅrsak(UtsettelseÅrsak.FERIE)
            .medVirksomhet(virksomhet1)
            .medErArbeidstaker(true)
            .build();

        Inntektsmelding inntektsmelding1 = getInntektsmeldingBuilder()
            .leggTil(UtsettelsePeriodeEntitet.utsettelse(FOM, TOM, UtsettelseÅrsak.FERIE))
            .medVirksomhet(virksomhet1).build();
        Inntektsmelding inntektsmelding2 = getInntektsmeldingBuilder()
            .leggTil(UtsettelsePeriodeEntitet.utsettelse(FOM, TOM, UtsettelseÅrsak.ARBEID))
            .medVirksomhet(virksomhet2).build();

        KontrollerFaktaPeriode kontrollerFaktaPeriode = kontroller(graderingPeriode, Arrays.asList(inntektsmelding1, inntektsmelding2));
        assertThat(kontrollerFaktaPeriode.erBekreftet()).isFalse();
    }

    private InntektsmeldingBuilder getInntektsmeldingBuilder() {
        return InntektsmeldingBuilder.builder().medInnsendingstidspunkt(LocalDateTime.now());
    }

    private ArbeidPåHeltidTjeneste lagArbeidPåHeltidTjeneste() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        Behandling behandling = scenario.lagre(repositoryProvider);
        lagAktørArbeid(scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd(), behandling.getAktørId());

        final SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = mock(SkjæringstidspunktTjeneste.class);
        AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, skjæringstidspunktTjeneste);
        InntektArbeidYtelseTjenesteImpl ytelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null,
            skjæringstidspunktTjeneste, apOpptjening);
        InntektArbeidYtelseGrunnlag ytelseGrunnlag = ytelseTjeneste.hentAggregat(behandling);
        UttakArbeidTjeneste uttakArbeidTjeneste = new UttakArbeidTjenesteImpl(ytelseTjeneste, uttakBeregningsandelTjeneste(ytelseGrunnlag, behandling));
        return new ArbeidPåHeltidTjenesteImpl(behandling, uttakArbeidTjeneste);
    }

    private UttakBeregningsandelTjeneste uttakBeregningsandelTjeneste(InntektArbeidYtelseGrunnlag ytelseGrunnlag, Behandling behandling) {
        UttakBeregningsandelTjeneste mock = mock(UttakBeregningsandelTjeneste.class);
        when(mock.hentAndeler(behandling)).thenReturn(lagAndelerForAlleInntekter(ytelseGrunnlag, behandling.getAktørId()));
        return mock;
    }

    private List<BeregningsgrunnlagPrStatusOgAndel> lagAndelerForAlleInntekter(InntektArbeidYtelseGrunnlag ytelseGrunnlag, AktørId aktørId) {
        List<BeregningsgrunnlagPrStatusOgAndel> beregningsgrunnlagPrStatusOgAndeler = new ArrayList<>();
        List<Yrkesaktivitet> yrkesAktiviteter = hentYrkesaggregatEtterStp(ytelseGrunnlag, aktørId);
        for (Yrkesaktivitet yrkesaktivitet:yrkesAktiviteter) {
            BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
                .builder()
                .medArbeidsgiver(yrkesaktivitet.getArbeidsgiver())
                .medArbforholdRef(yrkesaktivitet.getArbeidsforholdRef().isPresent() ? yrkesaktivitet.getArbeidsforholdRef().get().getReferanse() : null);
            beregningsgrunnlagPrStatusOgAndeler.add(new BeregningsgrunnlagPrStatusOgAndel.Builder()
                .medBGAndelArbeidsforhold(bga)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(new BeregningsgrunnlagPeriode()));
        }
        return beregningsgrunnlagPrStatusOgAndeler;
    }

    private List<Yrkesaktivitet> hentYrkesaggregatEtterStp(InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag, AktørId aktørId) {
        InntektArbeidYtelseAggregat aggregat = inntektArbeidYtelseGrunnlag.getOpplysningerEtterSkjæringstidspunkt()
            .orElseGet(() -> inntektArbeidYtelseGrunnlag.getOpplysningerFørSkjæringstidspunkt()
                .orElseThrow(() -> FeilFactory.create(UttakArbeidFeil.class).manglendeYrkesAktiviteter().toException()));

        return aggregat.getAktørArbeid()
            .stream()
            .filter(aktørArbeid -> Objects.equals(aktørArbeid.getAktørId(), aktørId))
            .flatMap(aktørArbeid -> aktørArbeid.getYrkesaktiviteter().stream())
            .collect(Collectors.toList());

    }


    private void lagAktørArbeid(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
                                AktørId aktørId) {

        final YrkesaktivitetBuilder oppdatere = YrkesaktivitetBuilder.oppdatere(Optional.empty());
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale1 = oppdatere.getAktivitetsAvtaleBuilder()
            .medProsentsats(BigDecimal.valueOf(100L))
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.of(2017, 01, 01), LocalDate.of(2019, 01, 01)));

        YrkesaktivitetBuilder yrkesaktivitet = oppdatere
            .medArbeidType(ArbeidType.UDEFINERT)
            .leggTilAktivitetsAvtale(aktivitetsAvtale1);

        final Optional<Virksomhet> hent = virksomhetRepository.hent("123123123");
        VirksomhetEntitet virksomhet;
        if (!hent.isPresent()) {
            virksomhet = new VirksomhetEntitet.Builder().medNavn("Hei").medOrgnr("123123123").oppdatertOpplysningerNå().build();
            virksomhetRepository.lagre(virksomhet);
        } else {
            virksomhet = (VirksomhetEntitet) hent.get();
        }
        yrkesaktivitet.medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet));

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder.getAktørArbeidBuilder(aktørId);
        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitet);

        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
    }

    private ArbeidPåHeltidTjeneste lagArbeidPåHeltidTjeneste(LocalDate fom1,
                                                             LocalDate tom1,
                                                             BigDecimal prosent1,
                                                             LocalDate fom2,
                                                             LocalDate tom2,
                                                             BigDecimal prosent2,
                                                             Virksomhet virksomhet) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        lagToAktørArbeid(scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd(), fom1, tom1, prosent1, fom2, tom2, prosent2, virksomhet);
        Behandling behandling = scenario.lagre(repositoryProvider);


        final SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = mock(SkjæringstidspunktTjeneste.class);
        AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, skjæringstidspunktTjeneste);
        InntektArbeidYtelseTjenesteImpl inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider,
            null, null, null, skjæringstidspunktTjeneste, apOpptjening);
        InntektArbeidYtelseGrunnlag ytelseGrunnlag = inntektArbeidYtelseTjeneste.hentAggregat(behandling);

        return new ArbeidPåHeltidTjenesteImpl(behandling, new UttakArbeidTjenesteImpl(inntektArbeidYtelseTjeneste, uttakBeregningsandelTjenesteMedTomAndeler(ytelseGrunnlag, behandling)));
    }

    private UttakBeregningsandelTjeneste uttakBeregningsandelTjenesteMedTomAndeler(InntektArbeidYtelseGrunnlag ytelseGrunnlag, Behandling behandling) {
        UttakBeregningsandelTjeneste mock = mock(UttakBeregningsandelTjeneste.class);
        when(mock.hentAndeler(behandling)).thenReturn(Collections.emptyList());
        return mock;
    }

    private KontrollerFaktaPeriode kontrollerUtenInntektsmeldinger(OppgittPeriode oppgittPeriode) {
        return kontroller(oppgittPeriode, Collections.emptyList());
    }

    private KontrollerFaktaPeriode kontrollerUtenInntektsmeldingerIkkeArbeidstaker(OppgittPeriode oppgittPeriode) {
        return kontrollerIkkeArbeidstaker(oppgittPeriode, Collections.emptyList());
    }

    private KontrollerFaktaPeriode kontroller(OppgittPeriode oppgittPeriode, List<Inntektsmelding> inntektsmeldinger) {
        return kontroller(oppgittPeriode, inntektsmeldinger, lagArbeidPåHeltidTjeneste(), true);
    }

    private KontrollerFaktaPeriode kontrollerIkkeArbeidstaker(OppgittPeriode oppgittPeriode, List<Inntektsmelding> inntektsmeldinger) {
        return kontroller(oppgittPeriode, inntektsmeldinger, lagArbeidPåHeltidTjeneste(), false);
    }

    private KontrollerFaktaPeriode kontroller(OppgittPeriode oppgittPeriode, List<Inntektsmelding> inntektsmeldinger, ArbeidPåHeltidTjeneste arbeidPåHeltidTjeneste, boolean erArbeidstaker) {
        SøknadsperiodeDokumentasjonKontrollerer kontrollerer = new SøknadsperiodeDokumentasjonKontrollerer(Collections.emptyList(),
            inntektsmeldinger, arbeidPåHeltidTjeneste, null, erArbeidstaker);
        return kontrollerer.kontrollerSøknadsperiode(oppgittPeriode);
    }

    private OppgittPeriode arbeidstakerPeriodeMedUtsettelseSykdom() {
        return arbeidstakerPeriodeMedUtsettelse(UtsettelseÅrsak.SYKDOM);
    }

    private OppgittPeriode arbeidstakerPeriodeMedUtsettelseArbeid() {
        return arbeidstakerPeriodeMedUtsettelse(UtsettelseÅrsak.ARBEID);
    }

    private OppgittPeriode frilansNæringsdrivendePeriodeMedUtsettelseSykdom() {
        return frilansNæringsdrivendePeriodeMedUtsettelse(UtsettelseÅrsak.SYKDOM);
    }

    private OppgittPeriode frilansNæringsdrivendePeriodeMedUtsettelseArbeid() {
        return frilansNæringsdrivendePeriodeMedUtsettelse(UtsettelseÅrsak.ARBEID);
    }

    private OppgittPeriode arbeidstakerPeriodeMedUtsettelse(UtsettelseÅrsak årsak) {
        return periodeMedUtsettelse(true, virksomhet(), årsak);
    }

    private OppgittPeriode frilansNæringsdrivendePeriodeMedUtsettelse(UtsettelseÅrsak utsettelseÅrsak) {
        return periodeMedUtsettelse(false, null, utsettelseÅrsak);
    }

    private OppgittPeriode periodeMedUtsettelse(Boolean arbeidstaker, Virksomhet virksomhet, UtsettelseÅrsak utsettelseÅrsak) {
        return OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medErArbeidstaker(arbeidstaker)
            .medVirksomhet(virksomhet)
            .medÅrsak(utsettelseÅrsak).medPeriode(enDag, enDag).build();
    }

    private void lagToAktørArbeid(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
                                  LocalDate fom1,
                                  LocalDate tom1,
                                  BigDecimal prosent1,
                                  LocalDate fom2,
                                  LocalDate tom2,
                                  BigDecimal prosent2,
                                  Virksomhet virksomhet) {

        final YrkesaktivitetBuilder oppdatere = YrkesaktivitetBuilder.oppdatere(Optional.empty());
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale1 = oppdatere.getAktivitetsAvtaleBuilder().medProsentsats(prosent1)
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fom1, tom1));

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale2 = oppdatere.getAktivitetsAvtaleBuilder().medProsentsats(prosent2)
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fom2, tom2));

        YrkesaktivitetBuilder yrkesaktivitet = oppdatere
            .medArbeidType(ArbeidType.UDEFINERT)
            .leggTilAktivitetsAvtale(aktivitetsAvtale1)
            .leggTilAktivitetsAvtale(aktivitetsAvtale2);

        yrkesaktivitet.medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet));

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder.getAktørArbeidBuilder(new AktørId("444"));
        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitet);

        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
    }
}
