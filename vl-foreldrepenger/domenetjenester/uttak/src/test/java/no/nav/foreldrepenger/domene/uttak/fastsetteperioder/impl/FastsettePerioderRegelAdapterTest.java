package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;

import org.junit.Rule;
import org.junit.Test;

import no.finn.unleash.FakeUnleash;
import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.HendelseVersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.GraderingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoerEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PeriodeUtenOmsorgEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PerioderUtenOmsorg;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PerioderUtenOmsorgEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OppholdÅrsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.Dekningsgrad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.uttak.GraderingAvslagÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.InnvilgetÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjeneste;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.UttakRevurderingTestUtil;
import no.nav.foreldrepenger.domene.uttak.UttakStillingsprosentTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.ArbeidTidslinjeTjeneste;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.FastsettePerioderRegelGrunnlagBygger;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.FastsettePerioderRegelResultatKonverterer;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.RelatertBehandlingTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.jpa.tid.IntervalUtils;

public class FastsettePerioderRegelAdapterTest {

    private final LocalDate termindato = LocalDate.of(2018, 6, 22);
    private final LocalDate fødselsdato = LocalDate.of(2018, 6, 22);
    private final LocalDate mottattDato = LocalDate.of(2018, 6, 22);

    private final Konfigurasjon konfigurasjon = StandardKonfigurasjon.KONFIGURASJON;
    private final int virkedagar_i_ei_veke = 5;
    private final int veker_før_fødsel_fellesperiode_grense = konfigurasjon.getParameter(Parametertype.LOVLIG_UTTAK_FØR_FØDSEL_UKER, fødselsdato);
    private final int veker_før_fødsel_foreldrepenger_grense = konfigurasjon.getParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER, fødselsdato);
    private final int veker_etter_fødsel_mødrekvote_grense = konfigurasjon.getParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, fødselsdato);
    private final int maxDagerMødrekvote = 75;
    private final int maxDagerForeldrepengerFørFødsel = 15;
    private final int maxDagerFedrekvote = 75;
    private final int maxDagerFellesperiode = 80;
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
    private RelatertBehandlingTjeneste relatertBehandlingTjeneste = new RelatertBehandlingTjenesteImpl(repositoryProvider);
    private LocalDate førsteLovligeUttaksdato = mottattDato.withDayOfMonth(1).minusMonths(3);

    private AktivitetIdentifikator arbeidsforhold = AktivitetIdentifikator.forArbeid("1234", "123");
    private final SkjæringstidspunktTjenesteImpl skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider,
        new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
        new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0), Period.of(0, 6, 0), Period.of(1, 0, 0)),
        Period.of(0, 3, 0),
        Period.of(0, 10, 0));
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, new AksjonspunktutlederForVurderOpptjening(repositoryProvider, skjæringstidspunktTjeneste));

    private static UttakResultatPeriodeEntitet finnPeriode(List<UttakResultatPeriodeEntitet> perioder, LocalDate fom, LocalDate tom) {
        for (UttakResultatPeriodeEntitet uttakResultatPeriode : perioder) {
            if (uttakResultatPeriode.getFom().equals(fom) && uttakResultatPeriode.getTom().equals(tom)) {
                return uttakResultatPeriode;
            }
        }
        throw new AssertionError("Fant ikke uttakresultatperiode med fom " + fom + " tom " + tom + " blant " + perioder);
    }

    @Test
    public void skalReturnerePlanMedMødrekvotePeriode() {
        // Arrange
        BigDecimal arbeidsprosent = BigDecimal.valueOf(50.0);
        Virksomhet virksomhet = virksomhet();
        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(termindato, termindato.plusWeeks(5))
            .medVirksomhet(virksomhet)
            .medErArbeidstaker(true)
            .medArbeidsprosent(arbeidsprosent)
            .build();
        Behandling behandling = setupMor(oppgittPeriode, virksomhet, termindato, termindato.plusWeeks(5), arbeidsprosent);
        FastsettePerioderRegelAdapter adapter = adapter();
        UttakResultatPerioderEntitet resultat = adapter.fastsettePerioder(behandling);

        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = resultat.getPerioder();

        assertThat(uttakResultatPerioder).hasSize(3);

        UttakResultatPeriodeEntitet fpffPeriode = finnPeriode(uttakResultatPerioder, termindato.minusWeeks(3), termindato.minusDays(1));
        UttakResultatPeriodeEntitet mkPeriode = finnPeriode(uttakResultatPerioder, termindato, termindato.plusWeeks(5));
        UttakResultatPeriodeEntitet manglendeSøktPeriode = finnPeriode(uttakResultatPerioder, mkPeriode.getTom().plusDays(1), termindato.plusWeeks(6).minusDays(1));

        //manglene søkt periode
        assertThat(fpffPeriode.getPeriodeResultatType()).isEqualTo(PeriodeResultatType.AVSLÅTT);
        assertThat(fpffPeriode.getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_TAR_IKKE_ALLE_UKENE);
        assertThat(fpffPeriode.getAktiviteter()).hasSize(1);
        assertThat(fpffPeriode.getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.FORELDREPENGER_FØR_FØDSEL);
        assertThat(fpffPeriode.getAktiviteter().get(0).getTrekkdager()).isEqualTo(15);
        //mødrekvote innvilget
        assertThat(mkPeriode.getPeriodeResultatType()).isEqualTo(PeriodeResultatType.INNVILGET);
        assertThat(mkPeriode.getAktiviteter()).hasSize(1);
        assertThat(mkPeriode.getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.MØDREKVOTE);
        assertThat(mkPeriode.getAktiviteter().get(0).getTrekkdager()).isEqualTo(new IntervalUtils(termindato, termindato.plusWeeks(5)).antallArbeidsdager());
        //manglene søkt periode.. manuell behandling og trekk dager
        assertThat(manglendeSøktPeriode.getPeriodeResultatType()).isEqualTo(PeriodeResultatType.MANUELL_BEHANDLING);
        assertThat(manglendeSøktPeriode.getAktiviteter()).hasSize(1);
        assertThat(manglendeSøktPeriode.getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.MØDREKVOTE);
        assertThat(manglendeSøktPeriode.getAktiviteter().get(0).getTrekkdager()).isEqualTo(new IntervalUtils(termindato.plusWeeks(5).plusDays(1),
            termindato.plusWeeks(6).minusDays(1)).antallArbeidsdager());
    }

    @Test
    public void skalReturnerePlanMedHeleForeldrepengerFørFødselPeriode() {
        // Arrange
        Virksomhet virksomhet = virksomhet();
        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
            .medPeriode(termindato.minusWeeks(veker_før_fødsel_foreldrepenger_grense), termindato.minusDays(1))
            .medVirksomhet(virksomhet)
            .medSamtidigUttak(false)
            .build();

        Behandling behandling = setupMor(oppgittPeriode, virksomhet, termindato.minusWeeks(veker_før_fødsel_foreldrepenger_grense),
            termindato.minusDays(1));

        // Act
        FastsettePerioderRegelAdapter adapter = adapter();
        UttakResultatPerioderEntitet resultat = adapter.fastsettePerioder(behandling);
        // Assert
        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = resultat.getPerioder();
        assertThat(uttakResultatPerioder).hasSize(2);

        List<UttakResultatPeriodeEntitet> foreldrepengerUttakPerioder = uttakResultatPerioder.stream()
            .filter(uttakResultatPeriode -> uttakResultatPeriode.getAktiviteter().get(0).getTrekkonto().getKode().equals(StønadskontoType.FORELDREPENGER_FØR_FØDSEL.getKode()))
            .collect(Collectors.toList());
        assertThat(foreldrepengerUttakPerioder.size()).isEqualTo(1);

        UttakResultatPeriodeEntitet foreldrePengerUttakPeriode = foreldrepengerUttakPerioder.get(0);
        assertThat(foreldrePengerUttakPeriode.getFom()).isEqualTo(termindato.minusWeeks(3));
        assertThat(foreldrePengerUttakPeriode.getTom()).isEqualTo(termindato.minusDays(1));

        int gjenståendeDager_foreldrepenger = veker_før_fødsel_foreldrepenger_grense * virkedagar_i_ei_veke - maxDagerForeldrepengerFørFødsel;
        assertThat(maxDagerForeldrepengerFørFødsel - foreldrePengerUttakPeriode.getAktiviteter().get(0).getTrekkdager()).isEqualTo(gjenståendeDager_foreldrepenger);

        UttakResultatPeriodeEntitet fpffPeriode = finnPeriode(uttakResultatPerioder, termindato.minusWeeks(3), termindato.minusDays(1));
        UttakResultatPeriodeEntitet mangledeSøktmkPeriode = finnPeriode(uttakResultatPerioder, termindato, termindato.plusWeeks(veker_etter_fødsel_mødrekvote_grense).minusDays(1));

        assertThat(mangledeSøktmkPeriode.getAktiviteter().get(0).getTrekkdager()).isEqualTo(veker_etter_fødsel_mødrekvote_grense * 5);
        assertThat(fpffPeriode.getAktiviteter().get(0).getTrekkdager()).isEqualTo(15);
    }

    @Test
    public void skalReturnereManuellBehandlingForPlanMedForTidligOppstartAvFedrekvote() {
        LocalDate startDato = termindato.plusWeeks(veker_etter_fødsel_mødrekvote_grense).minusWeeks(2);
        LocalDate forventetKnekk = termindato.plusWeeks(veker_etter_fødsel_mødrekvote_grense);
        LocalDate sluttDato = termindato.plusWeeks(veker_etter_fødsel_mødrekvote_grense).plusWeeks(2);

        // Arrange
        Virksomhet virksomhet = virksomhet();
        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FEDREKVOTE)
            .medPeriode(startDato, sluttDato)
            .medVirksomhet(virksomhet)
            .medSamtidigUttak(false)
            .build();

        Behandling behandling = setupFar(oppgittPeriode, virksomhet, startDato, sluttDato);

        // Act
        FastsettePerioderRegelAdapter adapter = adapter();
        UttakResultatPerioderEntitet resultat = adapter.fastsettePerioder(behandling);
        // Assert
        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = resultat.getPerioder();
        assertThat(uttakResultatPerioder).hasSize(2);

        List<UttakResultatPeriodeEntitet> manuellePerioder = uttakResultatPerioder.stream()
            .filter(uttakResultatPeriode -> uttakResultatPeriode.getAktiviteter().get(0).getTrekkonto().getKode().equals(StønadskontoType.FEDREKVOTE.getKode()) &&
                uttakResultatPeriode.getPeriodeResultatType().equals(PeriodeResultatType.MANUELL_BEHANDLING))
            .collect(Collectors.toList());
        assertThat(manuellePerioder.size()).isEqualTo(2);

        UttakResultatPeriodeEntitet periode1 = manuellePerioder.get(0);
        assertThat(periode1.getFom()).isEqualTo(startDato);
        assertThat(periode1.getTom()).isEqualTo(forventetKnekk.minusDays(1));


        UttakResultatPeriodeEntitet periode2 = manuellePerioder.get(1);
        assertThat(periode2.getFom()).isEqualTo(forventetKnekk);
        assertThat(periode2.getTom()).isEqualTo(sluttDato);
    }

    @Test
    public void skalReturnerePlanMedManuelleFellesperiodeFørFødselNårSøkerErFar() {
        LocalDate startDatoFellesperiode = termindato.minusWeeks(veker_før_fødsel_foreldrepenger_grense).minusWeeks(5);
        LocalDate sluttDatoFellesperiode = termindato.minusWeeks(veker_før_fødsel_foreldrepenger_grense).minusWeeks(2);

        // Arrange
        Virksomhet virksomhet = virksomhet();
        OppgittPeriode fellesperiode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medPeriode(startDatoFellesperiode, sluttDatoFellesperiode)
            .medVirksomhet(virksomhet)
            .medSamtidigUttak(false)
            .build();

        Behandling behandling = setupFar(fellesperiode, virksomhet, startDatoFellesperiode, sluttDatoFellesperiode);

        // Act
        FastsettePerioderRegelAdapter adapter = adapter();
        UttakResultatPerioderEntitet resultat = adapter.fastsettePerioder(behandling);
        // Assert
        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = resultat.getPerioder();

        Stream<UttakResultatPeriodeEntitet> manuelleFellesPerioderStream = uttakResultatPerioder.stream().
            filter(uttakResultatPeriode -> uttakResultatPeriode.getAktiviteter().get(0).getTrekkonto().getKode().equals(UttakPeriodeType.FELLESPERIODE.getKode()) &&
                uttakResultatPeriode.getPeriodeResultatType().equals(PeriodeResultatType.MANUELL_BEHANDLING));
        List<UttakResultatPeriodeEntitet> manuelleFellesPerioder = manuelleFellesPerioderStream.collect(Collectors.toList());
        assertThat(manuelleFellesPerioder.size()).isEqualTo(1);

        UttakResultatPeriodeEntitet manuellFellesPeriode = manuelleFellesPerioder.get(0);
        assertThat(manuellFellesPeriode.getFom()).isEqualTo(startDatoFellesperiode);
        assertThat(manuellFellesPeriode.getTom()).isEqualTo(sluttDatoFellesperiode);
    }

    @Test
    public void morSøkerFellesperiodeFørFødselMedOppholdFørForeldrepenger() {
        LocalDate startDatoFellesperiode = termindato.minusWeeks(veker_før_fødsel_foreldrepenger_grense).minusWeeks(5);
        LocalDate sluttDatoFellesperiode = termindato.minusWeeks(veker_før_fødsel_foreldrepenger_grense).minusWeeks(2);


        // Arrange
        Virksomhet virksomhet = virksomhet();
        OppgittPeriode fellesperiode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medPeriode(startDatoFellesperiode, sluttDatoFellesperiode)
            .medVirksomhet(virksomhet)
            .medSamtidigUttak(false)
            .build();

        Behandling behandling = setupMor(fellesperiode, virksomhet, startDatoFellesperiode, sluttDatoFellesperiode);

        // Act
        FastsettePerioderRegelAdapter adapter = adapter();
        UttakResultatPerioderEntitet resultat = adapter.fastsettePerioder(behandling);
        // Assert
        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = resultat.getPerioder();


        UttakResultatPeriodeEntitet tidligUttakFP = finnPeriode(uttakResultatPerioder, startDatoFellesperiode, sluttDatoFellesperiode);
        UttakResultatPeriodeEntitet manglendeSøktPeriodeFP = finnPeriode(uttakResultatPerioder, sluttDatoFellesperiode.plusDays(1), termindato.minusWeeks(3).minusDays(1));
        UttakResultatPeriodeEntitet manglendeSøktPeriodeFPFF = finnPeriode(uttakResultatPerioder, termindato.minusWeeks(3), termindato.minusDays(1));
        UttakResultatPeriodeEntitet manglendeSøktPeriodeMK = finnPeriode(uttakResultatPerioder, termindato, termindato.plusWeeks(6).minusDays(1));

        assertThat(tidligUttakFP.getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.FELLESPERIODE);
        assertThat(tidligUttakFP.getPeriodeResultatType()).isEqualTo(PeriodeResultatType.INNVILGET);
        assertThat(manglendeSøktPeriodeFP.getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.FELLESPERIODE);
        //assertThat(manglendeSøktPeriodeFP.getPeriodeResultatType()).isEqualTo(PeriodeResultatType.UGYLDIG_UTSETTELSE);
        assertThat(manglendeSøktPeriodeFPFF.getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.FORELDREPENGER_FØR_FØDSEL);
        //assertThat(manglendeSøktPeriodeFPFF.getPeriodeResultatType()).isEqualTo(PeriodeResultatType.UGYLDIG_UTSETTELSE);
        assertThat(manglendeSøktPeriodeMK.getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.MØDREKVOTE);
        assertThat(manglendeSøktPeriodeMK.getPeriodeResultatType()).isEqualTo(PeriodeResultatType.MANUELL_BEHANDLING);
    }

    @Test
    public void morSøkerFellesperiodeFørFødselMedOppholdInniPerioden() {
        LocalDate startDatoFellesperiode1 = termindato.minusWeeks(veker_før_fødsel_fellesperiode_grense);
        LocalDate sluttDatoFellesperiode1 = termindato.minusWeeks(veker_før_fødsel_fellesperiode_grense).plusWeeks(2);

        LocalDate startDatoFellesperiode2 = termindato.minusWeeks(veker_før_fødsel_foreldrepenger_grense).minusWeeks(3);
        LocalDate sluttDatoFellesperiode2 = termindato.minusWeeks(veker_før_fødsel_foreldrepenger_grense).minusDays(1);

        // Arrange
        Virksomhet virksomhet = virksomhet();
        OppgittPeriode fellesperiode1 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medVirksomhet(virksomhet)
            .medPeriode(startDatoFellesperiode1, sluttDatoFellesperiode1)
            .medSamtidigUttak(false)
            .build();

        OppgittPeriode fellesperiode2 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medVirksomhet(virksomhet)
            .medPeriode(startDatoFellesperiode2, sluttDatoFellesperiode2)
            .medSamtidigUttak(false)
            .build();

        Behandling behandling = setupMor(Arrays.asList(fellesperiode1, fellesperiode2), virksomhet,
            startDatoFellesperiode1, sluttDatoFellesperiode2, null);

        // Act
        FastsettePerioderRegelAdapter adapter = adapter();
        UttakResultatPerioderEntitet resultat = adapter.fastsettePerioder(behandling);
        // Assert
        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = resultat.getPerioder();
        assertThat(uttakResultatPerioder).hasSize(5);

        UttakResultatPeriodeEntitet tidligUttakFP1 = finnPeriode(uttakResultatPerioder, startDatoFellesperiode1, sluttDatoFellesperiode1);
        UttakResultatPeriodeEntitet manglendeSøktPeriodeFP = finnPeriode(uttakResultatPerioder, sluttDatoFellesperiode1.plusDays(1), startDatoFellesperiode2.minusDays(1));
        UttakResultatPeriodeEntitet tidligUttakFP2 = finnPeriode(uttakResultatPerioder, startDatoFellesperiode2, sluttDatoFellesperiode2);
        UttakResultatPeriodeEntitet manglendeSøktPeriodeFPFF = finnPeriode(uttakResultatPerioder, termindato.minusWeeks(3), termindato.minusDays(1));
        UttakResultatPeriodeEntitet manglendeSøktPeriodeMK = finnPeriode(uttakResultatPerioder, termindato, termindato.plusWeeks(6).minusDays(1));

        assertThat(tidligUttakFP1.getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.FELLESPERIODE);
        assertThat(tidligUttakFP1.getPeriodeResultatType()).isEqualTo(PeriodeResultatType.INNVILGET);
        assertThat(manglendeSøktPeriodeFP.getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.FELLESPERIODE);
        //hull fører til manuell behandling .. som igjen fører til manuell behandling av påfølgende perioder
        assertThat(manglendeSøktPeriodeFP.getPeriodeResultatType()).isEqualTo(PeriodeResultatType.MANUELL_BEHANDLING);
        assertThat(tidligUttakFP2.getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.FELLESPERIODE);
        assertThat(tidligUttakFP2.getPeriodeResultatType()).isEqualTo(PeriodeResultatType.MANUELL_BEHANDLING);
        assertThat(manglendeSøktPeriodeFPFF.getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.FORELDREPENGER_FØR_FØDSEL);
        assertThat(manglendeSøktPeriodeFPFF.getPeriodeResultatType()).isEqualTo(PeriodeResultatType.MANUELL_BEHANDLING);
        assertThat(manglendeSøktPeriodeMK.getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.MØDREKVOTE);
        assertThat(manglendeSøktPeriodeMK.getPeriodeResultatType()).isEqualTo(PeriodeResultatType.MANUELL_BEHANDLING);
    }

    @Test
    public void søknadMedOppholdForAnnenForelderFellesperiodeOgIkkeNokDagerPåKontoTest() {
        LocalDate startDatoMødrekvote = termindato;
        LocalDate sluttDatoMødrekvote = termindato.plusWeeks(veker_etter_fødsel_mødrekvote_grense).minusDays(1);

        LocalDate startDatoOpphold = sluttDatoMødrekvote.plusDays(1);
        LocalDate sluttDatoOpphold = sluttDatoMødrekvote.plusWeeks(5);

        // Arrange
        Virksomhet virksomhet = virksomhet();

        OppgittPeriode førFødsel = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
            .medPeriode(startDatoMødrekvote.minusWeeks(3), startDatoMødrekvote.minusDays(1))
            .medSamtidigUttak(false)
            .medVirksomhet(virksomhet)
            .build();

        OppgittPeriode mødrekvote = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(startDatoMødrekvote, sluttDatoMødrekvote)
            .medSamtidigUttak(false)
            .medVirksomhet(virksomhet)
            .build();

        // Angitt periode for annen forelder
        OppgittPeriode opphold = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.ANNET)
            .medÅrsak(OppholdÅrsak.KVOTE_FELLESPERIODE_ANNEN_FORELDER)
            .medPeriode(startDatoOpphold, sluttDatoOpphold)
            .build();

        OppgittPeriode fellesperiode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medPeriode(sluttDatoOpphold.plusDays(1), sluttDatoOpphold.plusWeeks(16))
            .medSamtidigUttak(false)
            .medVirksomhet(virksomhet)
            .build();

        Behandling behandling = setupMor(Arrays.asList(førFødsel, mødrekvote, opphold, fellesperiode), virksomhet, startDatoMødrekvote, sluttDatoOpphold, null);

        // Act
        FastsettePerioderRegelAdapter adapter = adapter();
        UttakResultatPerioderEntitet resultat = adapter.fastsettePerioder(behandling);
        // Assert
        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = resultat.getPerioder();
        assertThat(uttakResultatPerioder).hasSize(4);

        UttakResultatPeriodeEntitet periode = uttakResultatPerioder.get(0);
        assertThat(periode.getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.FORELDREPENGER_FØR_FØDSEL);
        assertThat(periode.getPeriodeResultatType()).isEqualTo(PeriodeResultatType.INNVILGET);

        periode = uttakResultatPerioder.get(1);
        assertThat(periode.getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.MØDREKVOTE);
        assertThat(periode.getPeriodeResultatType()).isEqualTo(PeriodeResultatType.INNVILGET);

        // Periode knukket pga ikke nok dager igjen på konto.
        // Oppholdsperioder for annen forelder skal ikke returneres som en uttakResultatPeriode.
        periode = uttakResultatPerioder.get(2);
        assertThat(periode.getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.FELLESPERIODE);
        assertThat(periode.getPeriodeResultatType()).isEqualTo(PeriodeResultatType.INNVILGET);
        assertThat(periode.getFom()).isEqualTo(fellesperiode.getFom());
        assertThat(periode.getTom()).isEqualTo(sluttDatoMødrekvote.plusWeeks(16));

        periode = uttakResultatPerioder.get(3);
        assertThat(periode.getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.FELLESPERIODE);
        assertThat(periode.getPeriodeResultatType()).isEqualTo(PeriodeResultatType.MANUELL_BEHANDLING);
        assertThat(periode.getFom()).isEqualTo(sluttDatoMødrekvote.plusWeeks(16).plusDays(1));
        assertThat(periode.getTom()).isEqualTo(fellesperiode.getTom());

    }

    @Test
    public void morSøkerMødrekvoteOgFedrekvote_FårInnvilgetMødrekvoteOgFedrekvoteGårTilManuellBehandling() {
        LocalDate startDatoMødrekvote = termindato;
        LocalDate sluttDatoMødrekvote = termindato.plusWeeks(veker_etter_fødsel_mødrekvote_grense).minusDays(1);

        LocalDate startDatoFedrekvote = termindato.plusWeeks(veker_etter_fødsel_mødrekvote_grense).plusWeeks(5);
        LocalDate sluttDatoFedrekvote = startDatoFedrekvote.plusWeeks(5);

        // Arrange
        Virksomhet virksomhet = virksomhet();
        OppgittPeriode mødrekvote = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(startDatoMødrekvote, sluttDatoMødrekvote)
            .medVirksomhet(virksomhet)
            .medSamtidigUttak(false)
            .build();

        OppgittPeriode fedrekvote = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FEDREKVOTE)
            .medPeriode(startDatoFedrekvote, sluttDatoFedrekvote)
            .medVirksomhet(virksomhet)
            .medSamtidigUttak(false)
            .build();

        Behandling behandling = setupMor(Arrays.asList(mødrekvote, fedrekvote), virksomhet, startDatoMødrekvote, sluttDatoFedrekvote, null);

        // Act
        FastsettePerioderRegelAdapter adapter = adapter();
        UttakResultatPerioderEntitet resultat = adapter.fastsettePerioder(behandling);
        // Assert
        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = resultat.getPerioder();


        List<UttakResultatPeriodeEntitet> fedrekvoteList = uttakResultatPerioder.stream().
            filter(uttakResultatPeriode -> uttakResultatPeriode.getAktiviteter().get(0).getTrekkonto().getKode().equals(StønadskontoType.FEDREKVOTE.getKode())).collect(Collectors.toList());
        assertThat(fedrekvoteList.size()).isEqualTo(1);
        assertThat(fedrekvoteList.get(0).getFom()).isEqualTo(startDatoFedrekvote);
        assertThat(fedrekvoteList.get(0).getTom()).isEqualTo(sluttDatoFedrekvote);

        assertThat(fedrekvoteList.get(0).getPeriodeResultatType()).isEqualTo(PeriodeResultatType.MANUELL_BEHANDLING);
        assertThat(fedrekvoteList.get(0).getPeriodeResultatÅrsak()).isEqualTo(PeriodeResultatÅrsak.UKJENT);
        assertThat(fedrekvoteList.get(0).getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.FEDREKVOTE);

        List<UttakResultatPeriodeEntitet> mødrekvoteList = uttakResultatPerioder.stream().
            filter(uttakResultatPeriode -> uttakResultatPeriode.getAktiviteter().get(0).getTrekkonto().getKode().equals(StønadskontoType.MØDREKVOTE.getKode())).collect(Collectors.toList());
        assertThat(mødrekvoteList.size()).isEqualTo(1);
        assertThat(mødrekvoteList.get(0).getFom()).isEqualTo(startDatoMødrekvote);
        assertThat(mødrekvoteList.get(0).getTom()).isEqualTo(sluttDatoMødrekvote);
        assertThat(mødrekvoteList.get(0).getPeriodeResultatType()).isEqualTo(PeriodeResultatType.INNVILGET);
        assertThat(mødrekvoteList.get(0).getPeriodeResultatÅrsak().getKode()).isEqualTo("2001");
        assertThat(mødrekvoteList.get(0).getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.MØDREKVOTE);
    }

    @Test
    public void skal_til_manuell_behandling_når_far_søker_fedrekvote_uten_å_ha_omsorg() {
        // Arrange
        Virksomhet virksomhet = virksomhet();
        LocalDate fom = termindato.plusWeeks(10);
        LocalDate tom = termindato.plusWeeks(15);
        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FEDREKVOTE)
            .medPeriode(fom, tom)
            .medVirksomhet(virksomhet)
            .medSamtidigUttak(false)
            .build();

        PerioderUtenOmsorgEntitet perioderUtenOmsorg = new PerioderUtenOmsorgEntitet();
        perioderUtenOmsorg.leggTil(new PeriodeUtenOmsorgEntitet(fom, tom));

        Behandling behandling = setupFar(oppgittPeriode, virksomhet, fom, tom, null, perioderUtenOmsorg);


        FastsettePerioderRegelAdapter adapter = adapter();
        UttakResultatPerioderEntitet resultat = adapter.fastsettePerioder(behandling);
        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = resultat.getPerioder();
        assertThat(uttakResultatPerioder).hasSize(1);

        UttakResultatPeriodeEntitet periode = uttakResultatPerioder.iterator().next();
        assertThat(periode.getPeriodeResultatType()).isEqualTo(PeriodeResultatType.MANUELL_BEHANDLING);
    }

    @Test
    public void skal_ta_med_arbeidsforholdprosent_når_gradering_er_opplyst() {
        LocalDate start = termindato.plusWeeks(20);
        LocalDate slutt = termindato.plusWeeks(25).minusDays(1);
        BigDecimal arbeidsprosent = BigDecimal.valueOf(60);
        Virksomhet virksomhet = virksomhet();
        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medVirksomhet(virksomhet)
            .medPeriodeType(UttakPeriodeType.FEDREKVOTE)
            .medPeriode(start, slutt)
            .medErArbeidstaker(true)
            .medArbeidsprosent(arbeidsprosent).build();

        Behandling behandling = setupFar(oppgittPeriode, virksomhet, start, slutt, arbeidsprosent);

        FastsettePerioderRegelAdapter adapter = adapter();
        UttakResultatPerioderEntitet resultat = adapter.fastsettePerioder(behandling);

        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = resultat.getPerioder();

        assertThat(uttakResultatPerioder).hasSize(1);

        UttakResultatPeriodeEntitet fkPeriode = finnPeriode(uttakResultatPerioder, start, slutt);
        assertThat(fkPeriode.getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.FEDREKVOTE);
        assertThat(fkPeriode.getAktiviteter().get(0).getTrekkdager()).isEqualTo((int) (5 * 5 * 0.4));
        assertThat(fkPeriode.getAktiviteter().get(0).getArbeidsprosent()).isEqualTo(arbeidsprosent);
    }

    @Test
    public void graderingSkalSettesRiktigEtterKjøringAvRegler() {
        BigDecimal arbeidsprosent = BigDecimal.valueOf(50);
        Virksomhet virksomhetSomGradereresHos = virksomhet("orgnr1");
        Virksomhet annenVirksomhet = virksomhet("orgnr2");
        LocalDate termindato = LocalDate.of(2018, 10, 1);
        OppgittPeriode oppgittFPFF = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
            .medPeriode(termindato.minusWeeks(3), termindato.minusDays(1))
            .build();
        OppgittPeriode oppgittMødrekvote = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(oppgittFPFF.getTom().plusDays(1), oppgittFPFF.getTom().plusWeeks(6)).build();
        OppgittPeriode oppgittGradertMødrekvote = OppgittPeriodeBuilder.ny()
            .medVirksomhet(virksomhetSomGradereresHos)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(oppgittMødrekvote.getTom().plusDays(1), oppgittMødrekvote.getTom().plusDays(1).plusWeeks(2))
            .medErArbeidstaker(true)
            .medArbeidsprosent(arbeidsprosent).build();
        AktørId aktørId = new AktørId("12345");
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel(false, aktørId);
        OppgittFordelingEntitet oppgittFordeling = new OppgittFordelingEntitet(Arrays.asList(oppgittFPFF, oppgittMødrekvote, oppgittGradertMødrekvote), true);
        scenario.medSøknad().medSøknadsdato(termindato).medMottattDato(termindato.minusWeeks(2));
        scenario.medFordeling(oppgittFordeling);
        scenario.medBehandlingVedtak()
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("saksbehandler")
            .medVedtaksdato(LocalDate.now());
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale1 = YrkesaktivitetEntitet.AktivitetsAvtaleBuilder.ny()
            .medProsentsats(BigDecimal.valueOf(50))
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.of(2016, 1, 1), LocalDate.of(2020, 1, 1)));
        YrkesaktivitetBuilder yrkesaktivitet1 = YrkesaktivitetBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhetSomGradereresHos))
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .leggTilAktivitetsAvtale(aktivitetsAvtale1);
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale2 = YrkesaktivitetEntitet.AktivitetsAvtaleBuilder.ny()
            .medProsentsats(BigDecimal.valueOf(50))
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.of(2016, 1, 1), LocalDate.of(2020, 1, 1)));
        YrkesaktivitetBuilder yrkesaktivitet2 = YrkesaktivitetBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(Arbeidsgiver.virksomhet(annenVirksomhet))
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .leggTilAktivitetsAvtale(aktivitetsAvtale2);
        scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd()
            .leggTilAktørArbeid(InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
                .medAktørId(aktørId)
                .leggTilYrkesaktivitet(yrkesaktivitet1))
            .leggTilAktørArbeid(InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
                .medAktørId(aktørId)
                .leggTilYrkesaktivitet(yrkesaktivitet2));
        scenario.medSøknadHendelse().medFødselsDato(termindato);
        scenario.medOppgittRettighet(new OppgittRettighetEntitet(true, true, false));
        Behandling behandling = scenario.lagre(repositoryProvider);
        lagreStønadskontoer(behandling);
        lagreUttaksperiodegrense(behandling);
        lagreInntektsmelding(behandling, virksomhetSomGradereresHos, arbeidsprosent, oppgittGradertMødrekvote.getFom(),
            oppgittGradertMødrekvote.getTom());
        lagreInntektsmelding(behandling, annenVirksomhet);

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjenete = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjenete.leggTilOrdinærtArbeid(virksomhetSomGradereresHos, null);
        beregningsandelTjenete.leggTilOrdinærtArbeid(annenVirksomhet, null);
        FastsettePerioderRegelAdapter adapter = adapter(beregningsandelTjenete);
        UttakResultatPerioderEntitet resultat = adapter.fastsettePerioder(behandling);
        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = resultat.getPerioder();

        UttakResultatPeriodeEntitet mødrekvote = finnPeriode(uttakResultatPerioder, oppgittMødrekvote.getFom(), oppgittMødrekvote.getTom());
        UttakResultatPeriodeAktivitetEntitet aktivitetMørdrekvoteVirksomhet1 = aktivitetForVirksomhet(mødrekvote.getAktiviteter(), virksomhetSomGradereresHos);
        UttakResultatPeriodeAktivitetEntitet aktivitetMørdrekvoteVirksomhet2 = aktivitetForVirksomhet(mødrekvote.getAktiviteter(), annenVirksomhet);
        assertThat(mødrekvote.getPeriodeResultatÅrsak()).isInstanceOf(InnvilgetÅrsak.class);
        assertThat(mødrekvote.isGraderingInnvilget()).isFalse();
        assertThat(mødrekvote.getGraderingAvslagÅrsak()).isEqualTo(GraderingAvslagÅrsak.UKJENT);
        assertThat(aktivitetMørdrekvoteVirksomhet1.getTrekkdager()).isEqualTo(5 * 6);
        assertThat(aktivitetMørdrekvoteVirksomhet1.getArbeidsprosent()).isEqualTo(BigDecimal.ZERO);
        assertThat(aktivitetMørdrekvoteVirksomhet1.isGraderingInnvilget()).isFalse();
        assertThat(aktivitetMørdrekvoteVirksomhet2.getTrekkdager()).isEqualTo(5 * 6);
        assertThat(aktivitetMørdrekvoteVirksomhet2.getArbeidsprosent()).isEqualTo(BigDecimal.ZERO);
        assertThat(aktivitetMørdrekvoteVirksomhet2.isGraderingInnvilget()).isFalse();

        UttakResultatPeriodeEntitet gradertMødrekvote = finnPeriode(uttakResultatPerioder, oppgittGradertMødrekvote.getFom(), oppgittGradertMødrekvote.getTom());
        UttakResultatPeriodeAktivitetEntitet aktivitetGradertMørdrekvoteVirksomhet1 = aktivitetForVirksomhet(gradertMødrekvote.getAktiviteter(), virksomhetSomGradereresHos);
        UttakResultatPeriodeAktivitetEntitet aktivitetGradertMørdrekvoteVirksomhet2 = aktivitetForVirksomhet(gradertMødrekvote.getAktiviteter(), annenVirksomhet);
        assertThat(gradertMødrekvote.getPeriodeResultatÅrsak()).isInstanceOf(InnvilgetÅrsak.class);
        assertThat(gradertMødrekvote.isGraderingInnvilget()).isTrue();
        assertThat(gradertMødrekvote.getGraderingAvslagÅrsak()).isEqualTo(GraderingAvslagÅrsak.UKJENT);
        assertThat(aktivitetGradertMørdrekvoteVirksomhet1.getTrekkdager()).isEqualTo(5); //rundes ned
        assertThat(aktivitetGradertMørdrekvoteVirksomhet1.getArbeidsprosent()).isEqualTo(arbeidsprosent);
        assertThat(aktivitetGradertMørdrekvoteVirksomhet1.isGraderingInnvilget()).isTrue();
        assertThat(aktivitetGradertMørdrekvoteVirksomhet1.isSøktGradering()).isTrue();
        assertThat(aktivitetGradertMørdrekvoteVirksomhet2.getTrekkdager()).isEqualTo(11);
        assertThat(aktivitetGradertMørdrekvoteVirksomhet2.getArbeidsprosent()).isEqualTo(BigDecimal.ZERO);
        assertThat(aktivitetGradertMørdrekvoteVirksomhet2.isGraderingInnvilget()).isFalse();
        assertThat(aktivitetGradertMørdrekvoteVirksomhet2.isSøktGradering()).isFalse();
    }

    private UttakResultatPeriodeAktivitetEntitet aktivitetForVirksomhet(List<UttakResultatPeriodeAktivitetEntitet> aktiviteter, Virksomhet virksomhet) {
        return aktiviteter.stream().filter(aktivitet -> aktivitet.getUttakAktivitet().getVirksomhet().equals(virksomhet)).findFirst().get();
    }

    private void lagreInntektsmelding(Behandling behandling, Virksomhet virksomhet) {
        lagreInntektsmelding(behandling, virksomhet, null, null, null);
    }

    @Test
    public void graderingSkalSettesRiktigVedAvslagAvGraderingEtterKjøringAvRegler() {
        //Søker gradert fpff slik at gradering avslås
        BigDecimal arbeidsprosent = BigDecimal.valueOf(50);
        Virksomhet virksomhet = virksomhet();
        OppgittPeriode oppgittGradertFPFF = OppgittPeriodeBuilder.ny()
            .medArbeidsprosent(arbeidsprosent)
            .medErArbeidstaker(true)
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
            .medPeriode(termindato.minusWeeks(3), termindato.minusDays(1))
            .medVirksomhet(virksomhet)
            .build();

        Behandling behandling = setupMor(oppgittGradertFPFF, virksomhet, oppgittGradertFPFF.getFom(), oppgittGradertFPFF.getTom(), arbeidsprosent);

        FastsettePerioderRegelAdapter adapter = adapter();
        UttakResultatPerioderEntitet resultat = adapter.fastsettePerioder(behandling);

        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = resultat.getPerioder();

        UttakResultatPeriodeEntitet mødrekvote = finnPeriode(uttakResultatPerioder, oppgittGradertFPFF.getFom(), oppgittGradertFPFF.getTom());
        assertThat(mødrekvote.getAktiviteter().get(0).getTrekkdager()).isEqualTo(3 * 5);
        assertThat(mødrekvote.getAktiviteter().get(0).getArbeidsprosent()).isEqualTo(arbeidsprosent);
        assertThat(mødrekvote.getAktiviteter().get(0).isGraderingInnvilget()).isFalse();
        assertThat(mødrekvote.getAktiviteter().get(0).isSøktGradering()).isTrue();
        assertThat(mødrekvote.isGraderingInnvilget()).isFalse();
        assertThat(mødrekvote.getGraderingAvslagÅrsak()).isNotEqualTo(GraderingAvslagÅrsak.UKJENT);
    }

    private Virksomhet virksomhet() {
        return virksomhet(arbeidsforhold.getOrgNr());
    }

    private Virksomhet virksomhet(String orgnr) {
        Optional<Virksomhet> optional = repositoryProvider.getVirksomhetRepository().hent(orgnr);
        if (optional.isPresent()) {
            return optional.get();
        }
        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder()
            .oppdatertOpplysningerNå()
            .medOrgnr(orgnr).build();
        repoRule.getEntityManager().persist(virksomhet);
        return virksomhet;
    }

    @Test
    public void skal_ta_med_arbeidsforholdprosent_når_gradering_er_opplyst_også_når_periode_avviker_med_lørdag_søndag() {

        LocalDate start = mandag(termindato.plusWeeks(20));
        LocalDate slutt = start.plusWeeks(5).minusDays(1);
        assertThat(start.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(slutt.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);

        BigDecimal arbeidsprosent = BigDecimal.valueOf(60);
        Virksomhet virksomhet = virksomhet();
        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FEDREKVOTE)
            .medPeriode(start, slutt)
            .medVirksomhet(virksomhet)
            .medErArbeidstaker(true)
            .medArbeidsprosent(arbeidsprosent).build();

        Behandling behandling = setupFar(oppgittPeriode, virksomhet, start, slutt, arbeidsprosent);

        FastsettePerioderRegelAdapter adapter = adapter();

        UttakResultatPerioderEntitet resultat = adapter.fastsettePerioder(behandling);
        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = resultat.getPerioder();

        assertThat(uttakResultatPerioder).hasSize(1);

        UttakResultatPeriodeEntitet fkPeriode = finnPeriode(uttakResultatPerioder, start, slutt);
        assertThat(fkPeriode.getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.FEDREKVOTE);
        assertThat(fkPeriode.getAktiviteter().get(0).getTrekkdager()).isEqualTo((int) (5 * 5 * 0.4));
        assertThat(fkPeriode.getAktiviteter().get(0).getArbeidsprosent()).isEqualTo(arbeidsprosent);
    }

    @Test
    public void utbetalingsprosentSkalHa2Desimaler() {
        LocalDate start = mandag(termindato.plusWeeks(20));
        LocalDate slutt = start.plusWeeks(5).minusDays(1);
        assertThat(start.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(slutt.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);

        BigDecimal arbeidsprosent = BigDecimal.valueOf(53);
        Virksomhet virksomhet = virksomhet();
        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FEDREKVOTE)
            .medPeriode(start, slutt)
            .medVirksomhet(virksomhet)
            .medErArbeidstaker(true)
            .medArbeidsprosent(arbeidsprosent).build();

        Behandling behandling = setupFar(oppgittPeriode, virksomhet, start, slutt, arbeidsprosent);

        FastsettePerioderRegelAdapter adapter = adapter();

        UttakResultatPerioderEntitet resultat = adapter.fastsettePerioder(behandling);
        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = resultat.getPerioder();

        assertThat(uttakResultatPerioder).hasSize(1);

        UttakResultatPeriodeEntitet fkPeriode = finnPeriode(uttakResultatPerioder, start, slutt);
        // Utbetalingsgrad (i %) = (stillingsprosent – arbeidsprosent) x 100 / stillingsprosent
        assertThat(fkPeriode.getAktiviteter().get(0).getUtbetalingsprosent()).isEqualTo(new BigDecimal("47.00"));
    }

    @Test
    public void samtidigUttaksprosentSkalHa2Desimaler() {
        LocalDate start = mandag(termindato.plusWeeks(20));
        LocalDate slutt = start.plusWeeks(5).minusDays(1);
        assertThat(start.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(slutt.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
        BigDecimal arbeidsprosent = BigDecimal.valueOf(60);
        BigDecimal samtidigUttaksprosent = BigDecimal.valueOf(53.33);
        Virksomhet virksomhet = virksomhet();
        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FEDREKVOTE)
            .medPeriode(start, slutt)
            .medVirksomhet(virksomhet)
            .medErArbeidstaker(true)
            .medSamtidigUttak(true)
            .medSamtidigUttaksprosent(samtidigUttaksprosent)
            .medArbeidsprosent(arbeidsprosent).build();

        Behandling behandling = setupFar(oppgittPeriode, virksomhet, start, slutt, arbeidsprosent);

        FastsettePerioderRegelAdapter adapter = adapter();

        UttakResultatPerioderEntitet resultat = adapter.fastsettePerioder(behandling);
        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = resultat.getPerioder();

        assertThat(uttakResultatPerioder).hasSize(1);

        UttakResultatPeriodeEntitet fkPeriode = finnPeriode(uttakResultatPerioder, start, slutt);
        assertThat(fkPeriode.getSamtidigUttaksprosent()).isEqualTo(new BigDecimal("53.33"));
    }

    @Test
    public void skal_håndtere_manuell_behandling_av_for_tidlig_gradering() {
        Virksomhet virksomhet = virksomhet();
        OppgittPeriode fpff = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
            .medPeriode(termindato.minusWeeks(3), termindato.minusDays(1))
            .medVirksomhet(virksomhet)
            .medSamtidigUttak(false)
            .build();


        OppgittPeriode mk = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(termindato, termindato.plusWeeks(6).minusDays(1))
            .medVirksomhet(virksomhet)
            .medErArbeidstaker(true)
            .medArbeidsprosent(BigDecimal.valueOf(50)).build();

        Behandling behandling = setupMor(Arrays.asList(fpff, mk), virksomhet, termindato,
            termindato.plusWeeks(6).minusDays(1), BigDecimal.valueOf(50));

        FastsettePerioderRegelAdapter adapter = adapter();

        UttakResultatPerioderEntitet resultat = adapter.fastsettePerioder(behandling);

        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = resultat.getPerioder();

        assertThat(uttakResultatPerioder).hasSize(2);

        UttakResultatPeriodeEntitet fpffPeriode =
            uttakResultatPerioder.stream().filter(p -> p.getAktiviteter().get(0).getTrekkonto().equals(StønadskontoType.FORELDREPENGER_FØR_FØDSEL)).findFirst().get();
        UttakResultatPeriodeEntitet mkPeriode =
            uttakResultatPerioder.stream().filter(p -> p.getAktiviteter().get(0).getTrekkonto().equals(StønadskontoType.MØDREKVOTE)).findFirst().get();

        //Innvilget FPFF
        assertThat(fpffPeriode.getPeriodeResultatType()).isEqualTo(PeriodeResultatType.INNVILGET);
        assertThat(fpffPeriode.getAktiviteter().get(0).getTrekkdager()).isEqualTo(15);
        //Innvilget mødrekvote, men avslag på gradering.
        assertThat(mkPeriode.getPeriodeResultatType()).isEqualTo(PeriodeResultatType.INNVILGET);
        assertThat(mkPeriode.getGraderingAvslagÅrsak()).isEqualTo(GraderingAvslagÅrsak.GRADERING_FØR_UKE_7);
        assertThat(mkPeriode.getAktiviteter().get(0).getTrekkdager()).isEqualTo(new IntervalUtils(termindato,
            termindato.plusWeeks(6).minusDays(1)).antallArbeidsdager());
    }

    @Test
    public void skalPrependeUttaksresultatPerioderFørEndringsdatoVedRevurdering() {
        // Lager opprinnelig uttak

        UttakRevurderingTestUtil uttakRevurderingTestUtil = new UttakRevurderingTestUtil(repoRule, repositoryProvider);
        UttakResultatPeriodeEntitet opprinneligFpff = new UttakResultatPeriodeEntitet.Builder(fødselsdato.minusWeeks(3).minusDays(1), fødselsdato.minusDays(1))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder().medUttakArbeidType(UttakArbeidType.FRILANS).build();
        UttakResultatPeriodeAktivitetEntitet aktivitet1 = new UttakResultatPeriodeAktivitetEntitet.Builder(opprinneligFpff, uttakAktivitet)
            .medTrekkonto(StønadskontoType.FORELDREPENGER_FØR_FØDSEL)
            .medErSøktGradering(false)
            .medArbeidsprosent(BigDecimal.TEN)
            .medTrekkdager(new IntervalUtils(opprinneligFpff.getFom(), opprinneligFpff.getTom()).antallArbeidsdager())
            .medUtbetalingsprosent(BigDecimal.TEN)
            .build();
        UttakResultatPeriodeEntitet opprinneligMødrekvote = new UttakResultatPeriodeEntitet.Builder(fødselsdato, fødselsdato.plusWeeks(8))
            .medPeriodeResultat(PeriodeResultatType.AVSLÅTT, PeriodeResultatÅrsak.UKJENT)
            .build();
        UttakResultatPeriodeAktivitetEntitet aktivitet2 = new UttakResultatPeriodeAktivitetEntitet.Builder(opprinneligMødrekvote, uttakAktivitet)
            .medTrekkonto(StønadskontoType.MØDREKVOTE)
            .medErSøktGradering(false)
            .medArbeidsprosent(BigDecimal.TEN)
            .medTrekkdager(new IntervalUtils(opprinneligMødrekvote.getFom(), opprinneligMødrekvote.getTom()).antallArbeidsdager())
            .medUtbetalingsprosent(BigDecimal.TEN)
            .build();
        opprinneligFpff.leggTilAktivitet(aktivitet1);
        opprinneligMødrekvote.leggTilAktivitet(aktivitet2);

        OppgittPeriode revurderingSøknadsperiodeFellesperiode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medPeriode(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(12))
            .build();

        FamilieHendelseBuilder hendelseBuilder = FamilieHendelseBuilder.oppdatere(Optional.empty(), HendelseVersjonType.BEKREFTET)
            .medAntallBarn(1)
            .medFødselsDato(fødselsdato);
        FamilieHendelseBuilder.TerminbekreftelseBuilder terminbekreftelse = hendelseBuilder.getTerminbekreftelseBuilder()
            .medTermindato(fødselsdato)
            .medUtstedtDato(fødselsdato.minusWeeks(4))
            .medNavnPå("adwaw");
        hendelseBuilder.medTerminbekreftelse(terminbekreftelse);

        Behandling revurdering = uttakRevurderingTestUtil.opprettRevurdering(UttakRevurderingTestUtil.AKTØR_ID_MOR, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER,
            Arrays.asList(opprinneligFpff, opprinneligMødrekvote), new OppgittFordelingEntitet(Collections.singletonList(revurderingSøknadsperiodeFellesperiode),
                true), hendelseBuilder);

        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(revurdering.getFagsak(), Dekningsgrad._100);
        repositoryProvider.getFagsakRelasjonRepository().lagre(revurdering, new Stønadskontoberegning.Builder()
            .medRegelEvaluering("sdawd")
            .medRegelInput("sdawd")
            .medStønadskonto(new Stønadskonto.Builder().medMaxDager(100).medStønadskontoType(StønadskontoType.MØDREKVOTE).build())
            .medStønadskonto(new Stønadskonto.Builder().medMaxDager(100).medStønadskontoType(StønadskontoType.FORELDREPENGER_FØR_FØDSEL).build())
            .medStønadskonto(new Stønadskonto.Builder().medMaxDager(100).medStønadskontoType(StønadskontoType.FELLESPERIODE).build())
            .build());

        LocalDate endringsdato = revurderingSøknadsperiodeFellesperiode.getFom();
        AvklarteUttakDatoerEntitet avklarteUttakDatoer = new AvklarteUttakDatoerEntitet(LocalDate.of(2018, 1, 1), endringsdato);
        repositoryProvider.getYtelsesFordelingRepository().lagre(revurdering, avklarteUttakDatoer);

        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilFrilans();
        UttakResultatPerioderEntitet resultatRevurdering = adapter(beregningsandelTjeneste).fastsettePerioder(revurdering);

        assertThat(resultatRevurdering.getPerioder()).hasSize(3);
        assertThat(resultatRevurdering.getPerioder().get(0).getAktiviteter()).hasSize(1);
        assertThat(resultatRevurdering.getPerioder().get(1).getAktiviteter()).hasSize(1);
        assertThat(resultatRevurdering.getPerioder().get(2).getAktiviteter()).hasSize(1);
        assertThat(resultatRevurdering.getPerioder().get(0).getFom()).isEqualTo(opprinneligFpff.getFom());
        assertThat(resultatRevurdering.getPerioder().get(0).getTom()).isEqualTo(opprinneligFpff.getTom());
        assertThat(resultatRevurdering.getPerioder().get(1).getFom()).isEqualTo(opprinneligMødrekvote.getFom());
        assertThat(resultatRevurdering.getPerioder().get(1).getTom()).isEqualTo(revurderingSøknadsperiodeFellesperiode.getFom().minusDays(1));
        assertThat(resultatRevurdering.getPerioder().get(2).getFom()).isEqualTo(revurderingSøknadsperiodeFellesperiode.getFom());
        assertThat(resultatRevurdering.getPerioder().get(2).getTom()).isEqualTo(revurderingSøknadsperiodeFellesperiode.getTom());
        assertThat(resultatRevurdering.getPerioder().get(0).getAktiviteter().get(0).getTrekkdager()).isEqualTo(opprinneligFpff.getAktiviteter().get(0).getTrekkdager());
        assertThat(resultatRevurdering.getPerioder().get(0).getAktiviteter().get(0).getUttakAktivitet()).isEqualTo(opprinneligFpff.getAktiviteter().get(0).getUttakAktivitet());
        assertThat(resultatRevurdering.getPerioder().get(0).getAktiviteter().get(0).getArbeidsprosent()).isEqualTo(opprinneligFpff.getAktiviteter().get(0).getArbeidsprosent());
        assertThat(resultatRevurdering.getPerioder().get(0).getAktiviteter().get(0).getTrekkonto()).isEqualTo(opprinneligFpff.getAktiviteter().get(0).getTrekkonto());
        assertThat(resultatRevurdering.getPerioder().get(1).getAktiviteter().get(0).getTrekkdager())
            .isEqualTo(new IntervalUtils(opprinneligMødrekvote.getFom(), revurderingSøknadsperiodeFellesperiode.getFom().minusDays(1)).antallArbeidsdager());
        assertThat(resultatRevurdering.getPerioder().get(1).getAktiviteter().get(0).getTrekkonto()).isEqualTo(opprinneligMødrekvote.getAktiviteter().get(0).getTrekkonto());
        assertThat(resultatRevurdering.getPerioder().get(2).getAktiviteter().get(0).getTrekkdager())
            .isEqualTo(new IntervalUtils(revurderingSøknadsperiodeFellesperiode.getFom(), revurderingSøknadsperiodeFellesperiode.getTom()).antallArbeidsdager());
        assertThat(resultatRevurdering.getPerioder().get(2).getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.FELLESPERIODE);

    }

    private LocalDate mandag(LocalDate dato) {
        return dato.minusDays(dato.getDayOfWeek().getValue() - 1);
    }

    private void lagreStønadskontoer(Behandling behandling) {
        Stønadskonto mødrekvote = Stønadskonto.builder().medStønadskontoType(StønadskontoType.MØDREKVOTE)
            .medMaxDager(maxDagerMødrekvote).build();
        Stønadskonto fellesperiode = Stønadskonto.builder().medStønadskontoType(StønadskontoType.FELLESPERIODE)
            .medMaxDager(maxDagerFellesperiode).build();
        Stønadskonto fedrekvote = Stønadskonto.builder().medStønadskontoType(StønadskontoType.FEDREKVOTE)
            .medMaxDager(maxDagerFedrekvote).build();
        Stønadskonto foreldrepengerFørFødsel = Stønadskonto.builder().medStønadskontoType(StønadskontoType.FORELDREPENGER_FØR_FØDSEL)
            .medMaxDager(maxDagerForeldrepengerFørFødsel).build();


        Stønadskontoberegning stønadskontoberegning = Stønadskontoberegning.builder()
            .medStønadskonto(mødrekvote)
            .medStønadskonto(fedrekvote)
            .medStønadskonto(fellesperiode)
            .medStønadskonto(foreldrepengerFørFødsel)
            .medRegelEvaluering(" ")
            .medRegelInput(" ")
            .build();
        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(behandling.getFagsak(), Dekningsgrad._100);
        repositoryProvider.getFagsakRelasjonRepository().lagre(behandling, stønadskontoberegning);
    }

    private void lagreUttaksperiodegrense(Behandling behandling) {
        Uttaksperiodegrense grense = new Uttaksperiodegrense.Builder(behandling)
            .medFørsteLovligeUttaksdag(førsteLovligeUttaksdato).medMottattDato(mottattDato).build();
        repositoryProvider.getUttakRepository().lagreUttaksperiodegrense(behandling, grense);
    }

    private FastsettePerioderRegelAdapter adapter() {
        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet(), null);
        return adapter(beregningsandelTjeneste);
    }

    private FastsettePerioderRegelAdapter adapter(UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste) {
        UttakArbeidTjeneste uttakArbeidTjeneste = new UttakArbeidTjenesteImpl(inntektArbeidYtelseTjeneste, beregningsandelTjeneste);
        ArbeidTidslinjeTjeneste arbeidTidslinjeTjeneste = new ArbeidTidslinjeTjenesteImpl(repositoryProvider,
            new UttakStillingsprosentTjenesteImpl(uttakArbeidTjeneste), beregningsandelTjeneste, uttakArbeidTjeneste);
        return new FastsettePerioderRegelAdapter(regelGrunnlagBygger(repositoryProvider, arbeidTidslinjeTjeneste), regelResultatKonverterer(repositoryProvider),
            disableAllUnleash());
    }

    private FakeUnleash disableAllUnleash() {
        FakeUnleash unleash = new FakeUnleash();
        unleash.disableAll();
        return unleash;
    }

    private FastsettePerioderRegelResultatKonverterer regelResultatKonverterer(BehandlingRepositoryProvider repositoryProvider) {
        return new FastsettePerioderRegelResultatKonvertererImpl(repositoryProvider);
    }

    private FastsettePerioderRegelGrunnlagBygger regelGrunnlagBygger(BehandlingRepositoryProvider repositoryProvider,
                                                                     ArbeidTidslinjeTjeneste arbeidTidslinjeTjeneste) {
        return new FastsettePerioderRegelGrunnlagByggerImpl(repositoryProvider, arbeidTidslinjeTjeneste, relatertBehandlingTjeneste, new UttakArbeidTjenesteImpl(inntektArbeidYtelseTjeneste, new UttakBeregningsandelTjenesteTestUtil()));
    }

    private void lagreInntektsmelding(Behandling behandling,
                                      Virksomhet virksomhet,
                                      BigDecimal arbeidstidsprosent,
                                      LocalDate fom,
                                      LocalDate tom) {


        MottattDokument mottattDokument = new MottattDokument.Builder()
            .medBehandlingId(behandling.getId())
            .medFagsakId(behandling.getFagsakId())
            .medDokumentTypeId(DokumentTypeId.INNTEKTSMELDING)
            .medDokumentId("foo")
            .build();

        repositoryProvider.getMottatteDokumentRepository().lagre(mottattDokument);

        InntektsmeldingBuilder inntektsmelding = InntektsmeldingBuilder.builder()
            .medVirksomhet(virksomhet)
            .medBeløp(BigDecimal.valueOf(35000))
            .medMottattDokument(mottattDokument)
            .medInnsendingstidspunkt(LocalDateTime.now())
            .medStartDatoPermisjon(LocalDate.now());
        if (arbeidstidsprosent != null) {
            Gradering gradering = new GraderingEntitet(fom, tom, arbeidstidsprosent);
            inntektsmelding.leggTil(gradering);
        }
        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, inntektsmelding.build());
    }

    private Behandling setupMor(OppgittPeriode oppgittPeriode,
                                Virksomhet virksomhet,
                                LocalDate arbeidFom,
                                LocalDate arbeidTom,
                                BigDecimal arbeidstidsprosent) {
        return setupMor(Collections.singletonList(oppgittPeriode), virksomhet, arbeidFom, arbeidTom, arbeidstidsprosent);
    }

    private Behandling setupMor(OppgittPeriode oppgittPeriode,
                                Virksomhet virksomhet,
                                LocalDate arbeidFom,
                                LocalDate arbeidTom) {
        return setupMor(oppgittPeriode, virksomhet, arbeidFom, arbeidTom, null);
    }

    private Behandling setupMor(List<OppgittPeriode> oppgittPerioder,
                                Virksomhet virksomhet,
                                LocalDate arbeidFom,
                                LocalDate arbeidTom,
                                BigDecimal arbeidstidsprosent) {
        return setup(oppgittPerioder, virksomhet, arbeidFom, arbeidTom, arbeidstidsprosent,
            ScenarioMorSøkerForeldrepenger.forFødsel(), null);
    }

    private Behandling setupFar(OppgittPeriode oppgittPeriode,
                                Virksomhet virksomhet,
                                LocalDate arbeidFom,
                                LocalDate arbeidTom,
                                BigDecimal arbeidstidsprosent) {
        return setup(Collections.singletonList(oppgittPeriode), virksomhet, arbeidFom, arbeidTom, arbeidstidsprosent,
            ScenarioFarSøkerForeldrepenger.forFødsel(), null);
    }

    private Behandling setupFar(OppgittPeriode oppgittPeriode,
                                Virksomhet virksomhet,
                                LocalDate arbeidFom,
                                LocalDate arbeidTom,
                                BigDecimal arbeidstidsprosent,
                                PerioderUtenOmsorgEntitet perioderUtenOmsorg) {
        return setup(Collections.singletonList(oppgittPeriode), virksomhet, arbeidFom, arbeidTom, arbeidstidsprosent,
            ScenarioFarSøkerForeldrepenger.forFødsel(), perioderUtenOmsorg);
    }

    private Behandling setupFar(OppgittPeriode oppgittPeriode,
                                Virksomhet virksomhet,
                                LocalDate arbeidFom,
                                LocalDate arbeidTom) {
        return setupFar(oppgittPeriode, virksomhet, arbeidFom, arbeidTom, null);
    }

    private Behandling setup(List<OppgittPeriode> oppgittPerioder,
                             Virksomhet virksomhet,
                             LocalDate arbeidFom,
                             LocalDate arbeidTom,
                             BigDecimal arbeidstidsprosent,
                             AbstractTestScenario<?> scenario,
                             PerioderUtenOmsorg perioderUtenOmsorg) {
        return setup(oppgittPerioder, virksomhet, arbeidFom, arbeidTom, arbeidstidsprosent,
            scenario, perioderUtenOmsorg, BigDecimal.valueOf(100));
    }

    private Behandling setup(List<OppgittPeriode> oppgittPerioder,
                             Virksomhet virksomhet,
                             LocalDate arbeidFom,
                             LocalDate arbeidTom,
                             BigDecimal arbeidstidsprosent,
                             AbstractTestScenario<?> scenario,
                             PerioderUtenOmsorg perioderUtenOmsorg,
                             BigDecimal stillingsprosent) {

        scenario.medFordeling(new OppgittFordelingEntitet(oppgittPerioder, true));
        scenario.medPerioderUtenOmsorg(perioderUtenOmsorg);
        scenario.medOppgittRettighet(new OppgittRettighetEntitet(true, true, true));
        scenario.medBehandlingVedtak()
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("saksbehandler")
            .medVedtaksdato(LocalDate.now());
        scenario.medBekreftetHendelse().medFødselsDato(fødselsdato);
        scenario.medSøknad().medMottattDato(LocalDate.now().minusWeeks(2));

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = YrkesaktivitetBuilder.nyAktivitetsAvtaleBuilder()
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(arbeidFom, arbeidTom))
            .medProsentsats(arbeidstidsprosent != null ? arbeidstidsprosent : BigDecimal.ZERO);
        YrkesaktivitetBuilder yrkesaktivitet = YrkesaktivitetBuilder.oppdatere(Optional.empty())
            .leggTilAktivitetsAvtale(aktivitetsAvtale)
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet));

        scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd()
            .leggTilAktørArbeid(InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
                .medAktørId(new AktørId("12345"))
                .leggTilYrkesaktivitet(yrkesaktivitet));


        Behandling behandling = scenario.lagre(repositoryProvider);

        lagreUttaksperiodegrense(behandling);
        lagreStønadskontoer(behandling);
        lagreYrkesAktiviter(behandling, Arbeidsgiver.virksomhet(virksomhet), arbeidFom, arbeidTom, stillingsprosent);

        lagreInntektsmelding(behandling, virksomhet, arbeidstidsprosent, arbeidFom, arbeidTom);
        return behandling;
    }

    private void lagreYrkesAktiviter(Behandling behandling, Arbeidsgiver virksomhet, LocalDate fom, LocalDate tom, BigDecimal stillingsprosent) {
        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder
            .oppdatere(Optional.empty(), VersjonType.REGISTER);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(behandling.getAktørId());

        YrkesaktivitetBuilder yrkesaktivitetBuilder = YrkesaktivitetBuilder.oppdatere(Optional.empty());
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom))
            .medProsentsats(stillingsprosent)
            .medAntallTimer(BigDecimal.valueOf(20.4d))
            .medAntallTimerFulltid(BigDecimal.valueOf(10.2d));

        yrkesaktivitetBuilder
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .medArbeidsgiver(virksomhet)
            .leggTilAktivitetsAvtale(aktivitetsAvtale);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);

        builder.leggTilAktørArbeid(aktørArbeid);

        repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, builder);
    }
}
