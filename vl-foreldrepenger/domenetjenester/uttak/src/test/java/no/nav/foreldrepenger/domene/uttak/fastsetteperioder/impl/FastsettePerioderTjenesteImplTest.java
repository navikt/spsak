package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;


import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl.FastsettePeriodeFeatureToggles.FORELDREPENGER_FØDSEL_FEATURE_TOGGLE_NAVN;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.finn.unleash.FakeUnleash;
import no.finn.unleash.Unleash;
import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.GraderingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittDekningsgrad;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittDekningsgradEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Dekningsgrad;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakLåsRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakLåsRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.behandlingslager.uttak.InnvilgetÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjeneste;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.UttakStillingsprosentTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPeriode;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPeriodeAktivitet;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPerioder;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.validering.OverstyrUttakResultatValidator;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class FastsettePerioderTjenesteImplTest {
    public static final String ORGNR = "21542512";
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private Repository repository = repoRule.getRepository();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    private YtelsesFordelingRepository ytelsesFordelingRepository = new YtelsesFordelingRepositoryImpl(entityManager);
    private FagsakLåsRepository fagsakLåsRepository = new FagsakLåsRepositoryImpl(entityManager);
    private FagsakRelasjonRepository relasjonRepository = new FagsakRelasjonRepositoryImpl(entityManager, ytelsesFordelingRepository, fagsakLåsRepository);
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
    private MottatteDokumentRepository mottatteDokumentRepository = repositoryProvider.getMottatteDokumentRepository();
    private UttakRepository uttakRepository = new UttakRepositoryImpl(entityManager);
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider, new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
        new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0), Period.of(0, 6, 0), Period.of(1, 0, 0)),
        Period.of(0, 3, 0),
        Period.of(0, 10, 0));
    private AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, skjæringstidspunktTjeneste);
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);

    @Test
    public void skalInnvilgeFedrekvoteForMedmor() {
        // Setup
        LocalDate mottattDato = LocalDate.now();
        LocalDate fødselsdato = LocalDate.now().minusWeeks(6);
        AktørId aktørId = new AktørId("2");

        OppgittPeriode fedrekvote = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FEDREKVOTE)
            .medPeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(20).minusDays(1))
            .medVirksomhet(virksomhet())
            .build();

        Fagsak fagsak = opprettFagsak(RelasjonsRolleType.MEDMOR, aktørId);
        repositoryProvider.getFagsakRepository().opprettNy(fagsak);
        relasjonRepository.opprettRelasjon(fagsak, Dekningsgrad._100);

        Behandling behandling = byggBehandlingForElektroniskSøknadOmFødsel(fagsak, fødselsdato, mottattDato, Collections.singletonList(fedrekvote));
        byggArbeidForBehandling(behandling, aktørId);
        opprettStønadskontoerForFarOgMor(behandling);

        opprettUttaksperiodegrense(mottattDato, behandling);
        repository.flushAndClear();

        // Act
        FastsettePerioderTjenesteImpl fastsettePerioderTjeneste = tjeneste();
        fastsettePerioderTjeneste.fastsettePerioder(behandling);
        repository.flushAndClear();

        // Assert
        behandling = behandlingRepository.hentBehandling(behandling.getId());

        Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        assertThat(uttakResultat).isPresent();
        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = uttakResultat.get().getOpprinneligPerioder().getPerioder();
        assertThat(uttakResultatPerioder).hasSize(1);

        UttakResultatPeriodeEntitet resultatPeriode = uttakResultatPerioder.iterator().next();
        assertThat(resultatPeriode.getAktiviteter().get(0).getTrekkonto()).isEqualTo(StønadskontoType.FEDREKVOTE);
        assertThat(resultatPeriode.getPeriodeResultatType()).isEqualTo(PeriodeResultatType.INNVILGET);
    }

    private OverstyrUttakResultatValidator validator() {
        return Mockito.mock(OverstyrUttakResultatValidator.class);
    }

    @Test
    public void oppretterOgLagrerUttakResultatPlanOgUttakPerioderPerArbeidsgiver() {
        // Setup
        LocalDate mottattDato = LocalDate.now();
        LocalDate fødselsdato = LocalDate.now();
        AktørId aktørId = new AktørId("1");

        OppgittPeriode periode1 = OppgittPeriodeBuilder.ny()
            .medVirksomhet(virksomhet("orgnr1"))
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))
            .build();

        OppgittPeriode periode2 = OppgittPeriodeBuilder.ny()
            .medVirksomhet(virksomhet("orgnr2"))
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medPeriode(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1))
            .build();

        Fagsak fagsak = opprettFagsak(aktørId);

        Behandling behandling = byggBehandlingForElektroniskSøknadOmFødsel(fagsak, fødselsdato, mottattDato, asList(periode1, periode2));
        byggArbeidForBehandling(behandling, aktørId);
        opprettStønadskontoerForFarOgMor(behandling);

        opprettUttaksperiodegrense(mottattDato, behandling);
        repository.flushAndClear();

        // Act
        FastsettePerioderTjenesteImpl fastsettePerioderTjeneste = tjeneste();
        fastsettePerioderTjeneste.fastsettePerioder(behandling);
        repository.flushAndClear();

        // Assert
        behandling = behandlingRepository.hentBehandling(behandling.getId());


        Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        assertThat(uttakResultat).isPresent();
        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = uttakResultat.get().getOpprinneligPerioder().getPerioder();
        assertThat(uttakResultatPerioder).hasSize(3);
    }

    @Test
    public void arbeidstidsprosentOgUtbetalingsprosentSkalHa2Desimaler() {
        // Setup
        LocalDate mottattDato = LocalDate.now().minusWeeks(1);
        LocalDate fødselsdato = LocalDate.now().minusWeeks(8);
        AktørId aktørId = new AktørId("1");

        BigDecimal arbeidsprosent = new BigDecimal("50.55");

        OppgittPeriode periode1 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
            .medPeriode(fødselsdato.minusWeeks(3), fødselsdato.minusDays(1))
            .build();

        OppgittPeriode periode2 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(fødselsdato, fødselsdato.plusWeeks(7))
            .build();

        VirksomhetEntitet virksomhet = virksomhet(ORGNR);
        OppgittPeriode periode3 = OppgittPeriodeBuilder.ny()
            .medVirksomhet(virksomhet)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(fødselsdato.plusWeeks(7).plusDays(1), fødselsdato.plusWeeks(8))
            .medErArbeidstaker(true)
            .medArbeidsprosent(arbeidsprosent)
            .build();

        Fagsak fagsak = opprettFagsak(aktørId);

        Behandling behandling = byggBehandlingForElektroniskSøknadOmFødsel(fagsak, fødselsdato, mottattDato, Arrays.asList(periode1, periode2, periode3));
        GraderingEntitet gradering = new GraderingEntitet(DatoIntervallEntitet.fraOgMedTilOgMed(fødselsdato.plusWeeks(7).plusDays(1),
            fødselsdato.plusWeeks(8)), arbeidsprosent);
        byggArbeidForBehandling(behandling, aktørId, Collections.singletonList(gradering));
        opprettStønadskontoerForFarOgMor(behandling);

        opprettUttaksperiodegrense(mottattDato, behandling);

        // Act
        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, null);
        FastsettePerioderTjenesteImpl fastsettePerioderTjeneste = tjeneste(beregningsandelTjeneste);
        fastsettePerioderTjeneste.fastsettePerioder(behandling);

        // Assert
        behandling = behandlingRepository.hentBehandling(behandling.getId());

        Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        assertThat(uttakResultat).isPresent();
        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = uttakResultat.get().getOpprinneligPerioder().getPerioder();
        assertThat(uttakResultatPerioder.get(3).getAktiviteter().get(0).getArbeidsprosent()).isEqualTo(arbeidsprosent);
        assertThat(uttakResultatPerioder.get(3).getAktiviteter().get(0).getUtbetalingsprosent()).isEqualTo(new BigDecimal("49.45"));
    }

    @Test
    public void sletterGammeltResultatOgOppretterNyttResultatDersomOpprinneligResultatFinnesFraFør() {
        // Steg 1: Opprett uttaksplan med perioder
        LocalDate mottattDato = LocalDate.now();
        LocalDate fødselsdato = LocalDate.now().minusMonths(3).withDayOfMonth(1);
        AktørId aktørId = new AktørId("1");

        OppgittPeriode periode1 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medVirksomhet(virksomhet())
            .medPeriode(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1))
            .build();

        OppgittPeriode periode2 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medVirksomhet(virksomhet())
            .medPeriode(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1))
            .build();

        OppgittPeriode periode3 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(FORELDREPENGER_FØR_FØDSEL)
            .medVirksomhet(virksomhet())
            .medPeriode(fødselsdato.minusWeeks(3), fødselsdato.minusDays(1))
            .build();

        Fagsak fagsak = opprettFagsak(aktørId);

        Behandling behandling = byggBehandlingForElektroniskSøknadOmFødsel(fagsak, fødselsdato, mottattDato, asList(periode1, periode2, periode3));
        byggArbeidForBehandling(behandling, aktørId);
        opprettStønadskontoerForFarOgMor(behandling);

        opprettUttaksperiodegrense(mottattDato, behandling);
        repository.flushAndClear();

        // Act
        FastsettePerioderTjenesteImpl fastsettePerioderTjeneste = tjeneste();
        fastsettePerioderTjeneste.fastsettePerioder(behandling);
        repository.flushAndClear();

        // Assert

        Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        assertThat(uttakResultat).isPresent();
        List<UttakResultatPeriodeEntitet> uttakResultatPerioder = uttakResultat.get().getOpprinneligPerioder().getPerioder();
        assertThat(uttakResultatPerioder).hasSize(3);

        Optional<UttakResultatPeriodeEntitet> mødrekvote = uttakResultatPerioder
            .stream().filter(p -> StønadskontoType.FORELDREPENGER_FØR_FØDSEL.getKode().equals(p.getAktiviteter().get(0).getTrekkonto().getKode()))
            .findFirst();
        assertThat(mødrekvote).isPresent();
        assertThat(mødrekvote.get().getPeriodeResultatType()).isEqualTo(PeriodeResultatType.MANUELL_BEHANDLING);

        // Steg 2: Perioder finnes fra før, skal fastsettes på nytt pga ny mottatt dato
        opprettUttaksperiodegrense(mottattDato.minusMonths(1).withDayOfMonth(1), behandling);
        repository.flushAndClear();

        // Act
        fastsettePerioderTjeneste.fastsettePerioder(behandling);
        repository.flushAndClear();

        uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        assertThat(uttakResultat).isPresent();
        uttakResultatPerioder = uttakResultat.get().getOpprinneligPerioder().getPerioder();
        assertThat(uttakResultatPerioder).hasSize(3);

        Optional<UttakResultatPeriodeEntitet> nyMødrekvote = uttakResultatPerioder
            .stream().filter(p -> StønadskontoType.FORELDREPENGER_FØR_FØDSEL.getKode().equals(p.getAktiviteter().get(0).getTrekkonto().getKode()))
            .findFirst();
        assertThat(nyMødrekvote).isPresent();
        assertThat(nyMødrekvote.get().getPeriodeResultatType()).isEqualTo(PeriodeResultatType.INNVILGET);
    }

    @Test
    public void featuretoggle_foreldrepengerFødsel_av_skal_gi_manuelt() {
        //Skal gå til manuelt hvis toggle er av

        // Setup
        LocalDate mottattDato = LocalDate.now().minusWeeks(1);
        LocalDate fødselsdato = LocalDate.now().minusWeeks(8);
        AktørId aktørId = new AktørId("1");

        OppgittPeriode fpffSøknadsperiode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
            .medPeriode(fødselsdato.minusWeeks(3), fødselsdato.minusDays(1))
            .build();
        OppgittPeriode foreldrepengerSøknadsperiode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(fødselsdato, fødselsdato.plusWeeks(2))
            .build();

        VirksomhetEntitet virksomhet = virksomhet();

        Fagsak fagsak = opprettFagsak(aktørId);

        Behandling behandling = byggBehandlingForElektroniskSøknadOmFødsel(fagsak, fødselsdato, mottattDato, Arrays.asList(fpffSøknadsperiode, foreldrepengerSøknadsperiode));

        ytelsesFordelingRepository.lagre(behandling, new OppgittRettighetEntitet(false, true, true));

        byggArbeidForBehandling(behandling, aktørId, Collections.emptyList());

        Stønadskonto fpffKonto = Stønadskonto.builder()
            .medStønadskontoType(StønadskontoType.FORELDREPENGER_FØR_FØDSEL)
            .medMaxDager(15)
            .build();
        Stønadskonto foreldrepengerKonto = Stønadskonto.builder()
            .medStønadskontoType(StønadskontoType.FORELDREPENGER)
            .medMaxDager(50)
            .build();
        Stønadskontoberegning stønadskontoberegning = Stønadskontoberegning.builder()
            .medRegelEvaluering("evaluering")
            .medRegelInput("grunnlag")
            .medStønadskonto(fpffKonto)
            .medStønadskonto(foreldrepengerKonto)
            .build();
        relasjonRepository.lagre(behandling, stønadskontoberegning);

        opprettUttaksperiodegrense(mottattDato, behandling);

        // Act
        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, null);
        FastsettePerioderTjenesteImpl fastsettePerioderTjeneste = tjeneste(beregningsandelTjeneste, disableAllUnleash());
        fastsettePerioderTjeneste.fastsettePerioder(behandling);

        // Assert
        behandling = behandlingRepository.hentBehandling(behandling.getId());

        Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        UttakResultatPeriodeEntitet resultat = uttakResultat.get().getOpprinneligPerioder().getPerioder().get(1);
        assertThat(resultat.getDokRegel().isTilManuellBehandling()).isTrue();
    }

    @Test
    public void featuretoggle_foreldrepengerFødsel_av_skal_gi_innvilget() {
        //Skal treffe UT1211 i foreldrepenger delregel

        // Setup
        LocalDate mottattDato = LocalDate.now().minusWeeks(1);
        LocalDate fødselsdato = LocalDate.now().minusWeeks(8);
        AktørId aktørId = new AktørId("1");

        OppgittPeriode fpffSøknadsperiode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
            .medPeriode(fødselsdato.minusWeeks(3), fødselsdato.minusDays(1))
            .build();
        OppgittPeriode foreldrepengerSøknadsperiode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(fødselsdato, fødselsdato.plusWeeks(2))
            .build();

        VirksomhetEntitet virksomhet = virksomhet();

        Fagsak fagsak = opprettFagsak(aktørId);

        Behandling behandling = byggBehandlingForElektroniskSøknadOmFødsel(fagsak, fødselsdato, mottattDato, Arrays.asList(fpffSøknadsperiode, foreldrepengerSøknadsperiode));

        ytelsesFordelingRepository.lagre(behandling, new OppgittRettighetEntitet(false, true, true));

        byggArbeidForBehandling(behandling, aktørId, Collections.emptyList());

        Stønadskonto fpffKonto = Stønadskonto.builder()
            .medStønadskontoType(StønadskontoType.FORELDREPENGER_FØR_FØDSEL)
            .medMaxDager(15)
            .build();
        Stønadskonto foreldrepengerKonto = Stønadskonto.builder()
            .medStønadskontoType(StønadskontoType.FORELDREPENGER)
            .medMaxDager(50)
            .build();
        Stønadskontoberegning stønadskontoberegning = Stønadskontoberegning.builder()
            .medRegelEvaluering("evaluering")
            .medRegelInput("grunnlag")
            .medStønadskonto(fpffKonto)
            .medStønadskonto(foreldrepengerKonto)
            .build();
        relasjonRepository.lagre(behandling, stønadskontoberegning);

        opprettUttaksperiodegrense(mottattDato, behandling);

        // Act
        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, null);
        FakeUnleash unleash = new FakeUnleash();
        unleash.enable(FORELDREPENGER_FØDSEL_FEATURE_TOGGLE_NAVN);
        FastsettePerioderTjenesteImpl fastsettePerioderTjeneste = tjeneste(beregningsandelTjeneste, unleash);
        fastsettePerioderTjeneste.fastsettePerioder(behandling);

        // Assert
        behandling = behandlingRepository.hentBehandling(behandling.getId());

        Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        UttakResultatPeriodeEntitet resultat = uttakResultat.get().getOpprinneligPerioder().getPerioder().get(1);
        assertThat(resultat.getDokRegel().isTilManuellBehandling()).isFalse();
        assertThat(resultat.getPeriodeResultatÅrsak()).isInstanceOf(InnvilgetÅrsak.class);
    }

    private VirksomhetEntitet virksomhet() {
        return virksomhet("00000000");
    }

    private VirksomhetEntitet virksomhet(String orgnr) {
        final VirksomhetRepository virksomhetRepository = repositoryProvider.getVirksomhetRepository();
        final Optional<Virksomhet> hent = virksomhetRepository.hent(orgnr);
        if (hent.isPresent()) {
            return (VirksomhetEntitet) hent.get();
        }
        VirksomhetEntitet virksomet = new VirksomhetEntitet.Builder()
            .medOrgnr(orgnr)
            .oppdatertOpplysningerNå()
            .build();
        virksomhetRepository.lagre(virksomet);
        return virksomet;
    }

    private FastsettePerioderTjenesteImpl tjeneste() {
        return tjeneste(new UttakBeregningsandelTjenesteTestUtil());
    }

    private FastsettePerioderTjenesteImpl tjeneste(UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste) {
        return tjeneste(beregningsandelTjeneste, disableAllUnleash());
    }

    private FastsettePerioderTjenesteImpl tjeneste(UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste, Unleash unleash) {
        return new FastsettePerioderTjenesteImpl(repositoryProvider, validator(), regelAdapter(beregningsandelTjeneste, unleash));
    }

    private FastsettePerioderRegelAdapter regelAdapter(UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste, Unleash unleash) {
        UttakArbeidTjeneste uttakArbeidTjeneste = new UttakArbeidTjenesteImpl(inntektArbeidYtelseTjeneste, beregningsandelTjeneste);
        FastsettePerioderRegelGrunnlagByggerImpl bygger = new FastsettePerioderRegelGrunnlagByggerImpl(repositoryProvider,
            new ArbeidTidslinjeTjenesteImpl(repositoryProvider, new UttakStillingsprosentTjenesteImpl(uttakArbeidTjeneste), beregningsandelTjeneste, uttakArbeidTjeneste),
            new RelatertBehandlingTjenesteImpl(repositoryProvider), uttakArbeidTjeneste);
        FastsettePerioderRegelResultatKonvertererImpl konverterer = new FastsettePerioderRegelResultatKonvertererImpl(repositoryProvider);
        return new FastsettePerioderRegelAdapter(bygger, konverterer, unleash);
    }

    private Unleash disableAllUnleash() {
        FakeUnleash fakeUnleash = new FakeUnleash();
        fakeUnleash.disableAll();
        return fakeUnleash;
    }

    @Test
    public void overstyrtSkalLeggesTilOpprinnelig() {
        // Steg 1: Opprett uttaksplan med perioder
        LocalDate mottattDato = LocalDate.now();
        LocalDate fødselsdato = LocalDate.now().minusMonths(3).withDayOfMonth(1);
        AktørId aktørId = new AktørId("2");

        Fagsak fagsak = opprettFagsak(aktørId);

        LocalDate opprinneligMødreKvoteSlutt = fødselsdato.plusWeeks(6).minusDays(1);
        LocalDate opprinneligFellesPeriodeSlutt = opprinneligMødreKvoteSlutt.plusWeeks(4);
        VirksomhetEntitet virksomhet = virksomhet(ORGNR);
        OppgittPeriode opprinneligeMødreKvote = OppgittPeriodeBuilder.ny()
            .medVirksomhet(virksomhet)
            .medPeriode(fødselsdato, opprinneligMødreKvoteSlutt)
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .build();
        OppgittPeriode opprinneligFellesPeriode = OppgittPeriodeBuilder.ny()
            .medVirksomhet(virksomhet)
            .medPeriode(opprinneligMødreKvoteSlutt.plusDays(1), opprinneligFellesPeriodeSlutt)
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .build();
        Behandling behandling = byggBehandlingForElektroniskSøknadOmFødsel(fagsak, fødselsdato, mottattDato,
            Arrays.asList(opprinneligeMødreKvote, opprinneligFellesPeriode));
        byggArbeidForBehandling(behandling, aktørId);
        opprettStønadskontoerForFarOgMor(behandling);

        opprettUttaksperiodegrense(mottattDato, behandling);
        repository.flushAndClear();
        UttakBeregningsandelTjenesteTestUtil beregningsandelTjeneste = new UttakBeregningsandelTjenesteTestUtil();
        beregningsandelTjeneste.leggTilOrdinærtArbeid(virksomhet, null);
        FastsettePerioderTjenesteImpl fastsettePerioderTjeneste = tjeneste(beregningsandelTjeneste);
        fastsettePerioderTjeneste.fastsettePerioder(behandling);

        Optional<UttakResultatEntitet> opprinneligResultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling);

        // Steg 2: Opprett overstyrt uttaksplan med perioder
        UttakResultatPeriodeAktivitet overtstyrtMødrekvote = periodeAktivitet(StønadskontoType.MØDREKVOTE);
        UttakResultatPeriodeAktivitet overstyrtFelleskvote = periodeAktivitet(StønadskontoType.FELLESPERIODE);
        UttakResultatPeriode mødreKkotePeriode = innvilgetPeriode(fødselsdato, opprinneligMødreKvoteSlutt, overtstyrtMødrekvote);
        UttakResultatPeriode fellesKvotePeriode1 = innvilgetPeriode(opprinneligMødreKvoteSlutt.plusDays(1), opprinneligFellesPeriodeSlutt.minusWeeks(2), overstyrtFelleskvote);
        UttakResultatPeriode fellesKvotePeriode2 = innvilgetPeriode(opprinneligFellesPeriodeSlutt.minusWeeks(2).plusDays(1), opprinneligFellesPeriodeSlutt, overstyrtFelleskvote);
        List<UttakResultatPeriode> perioder = new ArrayList<>();
        perioder.add(mødreKkotePeriode);
        perioder.add(fellesKvotePeriode1);
        perioder.add(fellesKvotePeriode2);
        UttakResultatPerioder overstyrer = new UttakResultatPerioder(perioder);

        // Act
        fastsettePerioderTjeneste.manueltFastsettePerioder(behandling, overstyrer);
        repository.flushAndClear();

        // Assert
        Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        assertThat(uttakResultat).isPresent();
        List<UttakResultatPeriodeEntitet> opprinneligePerioder = uttakResultat.get().getOpprinneligPerioder().getPerioder();
        assertThat(opprinneligePerioder).hasSize(opprinneligResultat.get().getOpprinneligPerioder().getPerioder().size());
        List<UttakResultatPeriodeEntitet> overstyrtePerioder = uttakResultat.get().getOverstyrtPerioder().getPerioder()
            .stream()
            .sorted(Comparator.comparing(UttakResultatPeriodeEntitet::getTom))
            .collect(Collectors.toList());
        assertThat(overstyrtePerioder).hasSize(3);
        assertThat(overstyrtePerioder.get(0).getFom()).isEqualTo(fødselsdato);
        assertThat(overstyrtePerioder.get(0).getTom()).isEqualTo(opprinneligMødreKvoteSlutt);
        assertThat(overstyrtePerioder.get(1).getFom()).isEqualTo(opprinneligMødreKvoteSlutt.plusDays(1));
        assertThat(overstyrtePerioder.get(1).getTom()).isEqualTo(opprinneligFellesPeriodeSlutt.minusWeeks(2));
        assertThat(overstyrtePerioder.get(2).getFom()).isEqualTo(opprinneligFellesPeriodeSlutt.minusWeeks(2).plusDays(1));
        assertThat(overstyrtePerioder.get(2).getTom()).isEqualTo(opprinneligFellesPeriodeSlutt);
    }

    private UttakResultatPeriode innvilgetPeriode(LocalDate fom, LocalDate tom, UttakResultatPeriodeAktivitet aktivitet) {
        return new UttakResultatPeriode.Builder().medTidsperiode(new LocalDateInterval(fom, tom))
            .medAktiviteter(Collections.singletonList(aktivitet))
            .medBegrunnelse("begrunnelse")
            .medType(PeriodeResultatType.INNVILGET)
            .build();
    }

    private Fagsak opprettFagsak(AktørId aktørId) {
        Fagsak fagsak = opprettFagsak(RelasjonsRolleType.MORA, aktørId);
        repositoryProvider.getFagsakRepository().opprettNy(fagsak);
        relasjonRepository.opprettRelasjon(fagsak, Dekningsgrad._100);
        return fagsak;
    }

    private UttakResultatPeriodeAktivitet periodeAktivitet(StønadskontoType fellesperiode) {
        return new UttakResultatPeriodeAktivitet.Builder()
            .medTrekkonto(fellesperiode)
            .medArbeidsprosent(BigDecimal.TEN)
            .medUtbetalingsgrad(BigDecimal.ZERO)
            .medTrekkdager(2)
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .medArbeidsforholdOrgnr(ORGNR)
            .build();
    }

    private void opprettUttaksperiodegrense(LocalDate mottattDato, Behandling behandling) {
        Uttaksperiodegrense uttaksperiodegrense = new Uttaksperiodegrense.Builder(behandling)
            .medMottattDato(mottattDato)
            .medFørsteLovligeUttaksdag(mottattDato.withDayOfMonth(1).minusMonths(3))
            .build();

        uttakRepository.lagreUttaksperiodegrense(behandling, uttaksperiodegrense);
    }

    private Fagsak opprettFagsak(RelasjonsRolleType relasjonsRolleType, AktørId aktørId) {
        return FagsakBuilder.nyForeldrepengesak(relasjonsRolleType).medBrukerPersonInfo(new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(aktørId)
            .medFødselsdato(LocalDate.now().minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medFnr("12312312312")
            .medForetrukketSpråk(Språkkode.nb)
            .build()).build();
    }

    private void opprettStønadskontoerForFarOgMor(Behandling behandling) {
        Stønadskonto foreldrepengerFørFødsel = Stønadskonto.builder()
            .medStønadskontoType(StønadskontoType.FORELDREPENGER_FØR_FØDSEL)
            .medMaxDager(15)
            .build();
        Stønadskonto mødrekvote = Stønadskonto.builder()
            .medStønadskontoType(StønadskontoType.MØDREKVOTE)
            .medMaxDager(50)
            .build();
        Stønadskonto fedrekvote = Stønadskonto.builder()
            .medStønadskontoType(StønadskontoType.FEDREKVOTE)
            .medMaxDager(50)
            .build();
        Stønadskonto fellesperiode = Stønadskonto.builder()
            .medStønadskontoType(StønadskontoType.FELLESPERIODE)
            .medMaxDager(50)
            .build();
        Stønadskontoberegning stønadskontoberegning = Stønadskontoberegning.builder()
            .medRegelEvaluering("evaluering")
            .medRegelInput("grunnlag")
            .medStønadskonto(mødrekvote).medStønadskonto(fedrekvote).medStønadskonto(fellesperiode).medStønadskonto(foreldrepengerFørFødsel).build();
        relasjonRepository.lagre(behandling, stønadskontoberegning);
    }

    private Behandling byggBehandlingForElektroniskSøknadOmFødsel(Fagsak fagsak, LocalDate fødselsdato, LocalDate mottattDato, List<OppgittPeriode> perioder) {
        Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(fagsak);

        Behandling behandling = behandlingBuilder.build();
        behandling.setAnsvarligSaksbehandler("VL");
        repository.lagre(behandling);

        VilkårResultat vilkårResultat = VilkårResultat.builder().medVilkårResultatType(VilkårResultatType.INNVILGET).buildFor(behandling);
        repository.lagre(vilkårResultat);

        Behandlingsresultat behandlingsresultat = Behandlingsresultat.opprettFor(behandling);
        behandlingsresultat.medOppdatertVilkårResultat(vilkårResultat);
        repository.lagre(behandlingsresultat);
        repository.flushAndClear();

        final FamilieHendelseBuilder søknadHendelse = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medAntallBarn(1)
            .medFødselsDato(fødselsdato);
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadHendelse);

        final FamilieHendelseBuilder bekreftetHendelse = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medAntallBarn(1)
            .medFødselsDato(fødselsdato);
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, bekreftetHendelse);

        OppgittDekningsgrad dekningsgrad = OppgittDekningsgradEntitet.bruk100();
        ytelsesFordelingRepository.lagre(behandling, dekningsgrad);

        OppgittFordelingEntitet fordeling = new OppgittFordelingEntitet(perioder, true);
        ytelsesFordelingRepository.lagre(behandling, fordeling);

        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(true, true, false);
        ytelsesFordelingRepository.lagre(behandling, rettighet);

        final Søknad søknad = new SøknadEntitet.Builder()
            .medSøknadsdato(LocalDate.now())
            .medMottattDato(mottattDato)
            .medElektroniskRegistrert(true)
            .medDekningsgrad(dekningsgrad)
            .medRettighet(rettighet)
            .medFordeling(fordeling)
            .medFamilieHendelse(repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling).getSøknadVersjon()).build();
        repositoryProvider.getSøknadRepository().lagreOgFlush(behandling, søknad);

        return behandling;
    }

    private Inntektsmelding byggArbeidForBehandling(Behandling behandling, AktørId aktørId) {
        return byggArbeidForBehandling(behandling, aktørId, Collections.emptyList());
    }

    private Inntektsmelding byggArbeidForBehandling(Behandling behandling, AktørId aktørId, List<Gradering> graderinger) {
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder =
            inntektArbeidYtelseRepository.opprettBuilderFor(behandling, VersjonType.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder.getAktørArbeidBuilder(aktørId);
        YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(new Opptjeningsnøkkel(null, null, null),
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();

        LocalDate fraOgMed = LocalDate.now().minusYears(1);
        LocalDate tilOgMed = LocalDate.now().plusYears(10);

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed))
            .medProsentsats(BigDecimal.valueOf(100))
            .medAntallTimer(BigDecimal.valueOf(20.4d))
            .medAntallTimerFulltid(BigDecimal.valueOf(10.2d));

        Virksomhet virksomhet = virksomhet(ORGNR);

        yrkesaktivitetBuilder
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet))
            .leggTilAktivitetsAvtale(aktivitetsAvtale)
            .build();

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeid);

        inntektArbeidYtelseRepository.lagre(behandling, inntektArbeidYtelseAggregatBuilder);

        MottattDokument mottattDokument = new MottattDokument.Builder()
            .medBehandlingId(behandling.getId())
            .medFagsakId(behandling.getFagsakId())
            .medDokumentTypeId(DokumentTypeId.INNTEKTSMELDING)
            .medDokumentId("foo")
            .build();
        mottatteDokumentRepository.lagre(mottattDokument);

        InntektsmeldingBuilder inntektsmeldingBuilder = InntektsmeldingBuilder.builder()
            .medVirksomhet(virksomhet)
            .medBeløp(BigDecimal.valueOf(100000))
            .medMottattDokument(mottattDokument)
            .medInnsendingstidspunkt(LocalDateTime.now())
            .medStartDatoPermisjon(LocalDate.now());
        for (Gradering gradering : graderinger) {
            inntektsmeldingBuilder.leggTil(gradering);
        }
        Inntektsmelding im = inntektsmeldingBuilder
            .build();
        inntektArbeidYtelseRepository.lagre(behandling, im);
        return inntektArbeidYtelseRepository.hentAggregat(behandling, null).getInntektsmeldinger().get().getInntektsmeldinger().get(0);
    }
}
