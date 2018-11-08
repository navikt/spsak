package no.nav.foreldrepenger.behandling.steg.uttak.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import no.finn.unleash.FakeUnleash;
import no.finn.unleash.Unleash;
import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
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
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjon;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjeneste;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.UttakStillingsprosentTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.beregnkontoer.BeregnStønadskontoerTjeneste;
import no.nav.foreldrepenger.domene.uttak.beregnkontoer.impl.BeregnStønadskontoerTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl.ArbeidTidslinjeTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl.FastsettePerioderRegelAdapter;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl.FastsettePerioderRegelGrunnlagByggerImpl;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl.FastsettePerioderRegelResultatKonvertererImpl;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl.FastsettePerioderTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.db.Repository;


public class UttakStegImplTest {

    public static final String ORGNR = "21542512";
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private Repository repository = repoRule.getRepository();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider,
        new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
        new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0), Period.of(0, 6, 0), Period.of(1, 0, 0)),
        Period.of(0, 3, 0),
        Period.of(0, 10, 0));
    private AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, skjæringstidspunktTjeneste);
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null,
        skjæringstidspunktTjeneste, apOpptjening);
    private MottatteDokumentRepository mottatteDokumentRepository = repositoryProvider.getMottatteDokumentRepository();
    private UttakArbeidTjeneste uttakArbeidTjeneste = new UttakArbeidTjenesteImpl(inntektArbeidYtelseTjeneste, behandling -> Collections.emptyList());
    private FastsettePerioderTjenesteImpl fastsettePerioderTjeneste = periodeTjeneste();

    private BeregnStønadskontoerTjeneste beregnStønadskontoerTjeneste;

    private UttakStegImpl steg;
    private BehandlingskontrollKontekst kontekst;
    private YtelsesFordelingRepository ytelsesFordelingRepository = new YtelsesFordelingRepositoryImpl(entityManager);


    private UttakRepository uttakRepository = new UttakRepositoryImpl(entityManager);
    private FastsettUttakManueltAksjonspunktUtleder utleder = new FastsettUttakManueltAksjonspunktUtleder(uttakRepository, repositoryProvider.getAksjonspunktRepository(), uttakArbeidTjeneste);

    private String ARBEIDSFORHOLD_ID = "arbeid123";

    private AktørId AKTØRID = new AktørId("1");
    private Fagsak fagsak;

    private Behandling behandling;

    @Before
    public void setUp() {
        fagsak = FagsakBuilder.nyForeldrepengerForMor().medBrukerPersonInfo(new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(AKTØRID)
            .medFødselsdato(LocalDate.now().minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(PersonIdent.fra("12312312312"))
            .medForetrukketSpråk(Språkkode.nb)
            .build()).build();

        repositoryProvider.getFagsakRepository().opprettNy(fagsak);
        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(fagsak, Dekningsgrad._100);
        behandling = byggBehandlingForElektroniskSøknadOmFødsel(fagsak, LocalDate.now(), LocalDate.now());
        byggArbeidForBehandling(behandling);
        opprettUttaksperiodegrense(LocalDate.now(), behandling);

        beregnStønadskontoerTjeneste = new BeregnStønadskontoerTjenesteImpl(repositoryProvider);

        repository.flushAndClear();
    }

    @Test
    public void skal_utføre_uten_aksjonspunkt_når_det_ikke_er_noe_som_skal_fastsettes_manuelt() {
        initKontekst();

        steg = new UttakStegImpl(beregnStønadskontoerTjeneste, repositoryProvider, fastsettePerioderTjeneste, utleder);

        // Act
        BehandleStegResultat behandleStegResultat = steg.utførSteg(kontekst);

        assertThat(behandleStegResultat).isNotNull();
        assertThat(behandleStegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(behandleStegResultat.getAksjonspunktListe()).isEmpty();
    }

    private void initKontekst() {
        kontekst = new BehandlingskontrollKontekst(fagsak.getId(),fagsak.getAktørId(), behandlingRepository.taSkriveLås(behandling));
    }

    @Test
    public void skal_ha_aksjonspunkt_når_resultat_må_manuelt_fastsettes_her_pga_tomt_på_konto() {

        initKontekst();

        steg = new UttakStegImpl(beregnStønadskontoerTjeneste, repositoryProvider, fastsettePerioderTjeneste, utleder);

        ytelsesFordelingRepository.lagreOverstyrtFordeling(behandling, søknad4ukerFPFF());

        OppgittDekningsgrad dekningsgrad = OppgittDekningsgradEntitet.bruk100();
        ytelsesFordelingRepository.lagre(behandling, dekningsgrad);


        // Act
        BehandleStegResultat behandleStegResultat = steg.utførSteg(kontekst);

        assertThat(behandleStegResultat).isNotNull();
        assertThat(behandleStegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(behandleStegResultat.getAksjonspunktListe()).containsExactly(AksjonspunktDefinisjon.FASTSETT_UTTAKPERIODER);
    }

    @Test
    public void skalKunBeregneStønadskontoVedFørsteBehandlingForFørsteForelder() {
        Fagsak fagsakForFar = FagsakBuilder.nyForeldrepengesak(RelasjonsRolleType.FARA).build();
        repositoryProvider.getFagsakRepository().opprettNy(fagsakForFar);

        Behandling farsBehandling = byggBehandlingForElektroniskSøknadOmFødsel(fagsakForFar, LocalDate.now(), LocalDate.now());
        byggArbeidForBehandling(farsBehandling);
        opprettUttaksperiodegrense(LocalDate.now(), farsBehandling);

        repositoryProvider.getFagsakRelasjonRepository().kobleFagsaker(fagsak, fagsakForFar);
        repoRule.getRepository().flushAndClear();

        initKontekst();
        steg = new UttakStegImpl(beregnStønadskontoerTjeneste, repositoryProvider, fastsettePerioderTjeneste, utleder);

        // Act -- behandler mors behandling først
        steg.utførSteg(kontekst);
        FagsakRelasjon morsFagsakRelasjon = repositoryProvider.getFagsakRelasjonRepository().finnRelasjonFor(behandling.getFagsak());

        // Assert - stønadskontoer skal ha blitt opprettet
        assertThat(morsFagsakRelasjon.getStønadskontoberegning()).isPresent();
        Stønadskontoberegning førsteStønadskontoberegning = morsFagsakRelasjon.getStønadskontoberegning().get();

        // Act -- kjører steget på nytt for mor
        steg.utførSteg(kontekst);
        morsFagsakRelasjon = repositoryProvider.getFagsakRelasjonRepository().finnRelasjonFor(behandling.getFagsak());

        // Assert -- fortsatt innenfor første behandling -- skal beregne stønadskontoer på nytt
        assertThat(morsFagsakRelasjon.getStønadskontoberegning()).isPresent();
        Stønadskontoberegning andreStønadskontoberegning = morsFagsakRelasjon.getStønadskontoberegning().get();
        assertThat(andreStønadskontoberegning.getId()).isNotEqualTo(førsteStønadskontoberegning.getId());


        // Avslutter mors behandling
        Behandling lagretBehandling = behandlingRepository.hentBehandling(this.behandling.getId());
        lagretBehandling.avsluttBehandling();
        behandlingRepository.lagre(lagretBehandling, behandlingRepository.taSkriveLås(lagretBehandling));
        repoRule.getRepository().flushAndClear();


        // Act -- behandler fars behandling, skal ikke opprette stønadskontoer på nytt
        BehandlingskontrollKontekst kontekstForFarsBehandling = new BehandlingskontrollKontekst(fagsak.getId(),fagsak.getAktørId(), behandlingRepository.taSkriveLås(farsBehandling));
        steg.utførSteg(kontekstForFarsBehandling);

        FagsakRelasjon nyLagretFagsakRelasjon = repositoryProvider.getFagsakRelasjonRepository().finnRelasjonFor(fagsakForFar);
        Stønadskontoberegning stønadskontoberegningFar = nyLagretFagsakRelasjon.getStønadskontoberegning().get();

        // Assert
        assertThat(stønadskontoberegningFar.getId()).isEqualTo(andreStønadskontoberegning.getId());
    }

    @Ignore("TODO SOMMERFUGL Behandlingskontroll kjører hoppOver selv om det hoppes til utgangen av steget. Må enten endre behandlingskontroll, flytte opprydding til tidligere steg, eller flytte overstyr-AP til senere steg")
    @Test
    public void skal_ikke_ha_opprinnelige_eller_overstyrte_perioder_etter_tilbakehopp_over_steget() {
        initKontekst();

        steg = new UttakStegImpl(beregnStønadskontoerTjeneste, repositoryProvider, fastsettePerioderTjeneste, utleder);
        steg.utførSteg(kontekst);

        // Act
        steg.vedHoppOverBakover(null, behandling, null, null, null);

        //assert
        UttakResultatEntitet resultat = uttakRepository.hentUttakResultat(behandling);
        assertThat(resultat.getOpprinneligPerioder()).isNull();
        assertThat(resultat.getOverstyrtPerioder()).isNull();
    }

    @Test
    public void skal_ha_aktiv_uttak_resultat_etter_tilbakehopp_til_steget() {
        initKontekst();

        steg = new UttakStegImpl(beregnStønadskontoerTjeneste, repositoryProvider, fastsettePerioderTjeneste, utleder);
        steg.utførSteg(kontekst);

        // Act
        steg.vedTransisjon(null, behandling, null, BehandlingSteg.TransisjonType.HOPP_OVER_BAKOVER, BehandlingStegType.VURDER_UTTAK, BehandlingStegType.FATTE_VEDTAK, BehandlingSteg.TransisjonType.FØR_INNGANG);

        //assert
        Optional<UttakResultatEntitet> resultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        assertThat(resultat.isPresent()).isTrue();
    }

    @Test
    public void skal_ikke_ha_aktiv_uttak_resultat_etter_tilbakehopp_over_steget() {
        initKontekst();

        steg = new UttakStegImpl(beregnStønadskontoerTjeneste, repositoryProvider, fastsettePerioderTjeneste, utleder);
        steg.utførSteg(kontekst);

        // Act
        steg.vedTransisjon(null, behandling, null, BehandlingSteg.TransisjonType.HOPP_OVER_BAKOVER, BehandlingStegType.SØKERS_RELASJON_TIL_BARN, BehandlingStegType.FATTE_VEDTAK, BehandlingSteg.TransisjonType.FØR_INNGANG);

        //assert
        Optional<UttakResultatEntitet> resultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        assertThat(resultat.isPresent()).isFalse();
    }

    @Test
    public void skal_ikke_ha_aktiv_uttak_resultat_etter_fremoverhopp_over_steget() {
        initKontekst();

        steg = new UttakStegImpl(beregnStønadskontoerTjeneste, repositoryProvider, fastsettePerioderTjeneste, utleder);
        steg.utførSteg(kontekst);

        // Act
        steg.vedTransisjon(null, behandling, null, BehandlingSteg.TransisjonType.HOPP_OVER_FRAMOVER, BehandlingStegType.FATTE_VEDTAK, BehandlingStegType.VURDER_MEDLEMSKAPVILKÅR, BehandlingSteg.TransisjonType.FØR_INNGANG);

        //assert
        Optional<UttakResultatEntitet> resultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        assertThat(resultat.isPresent()).isFalse();
    }

    private OppgittFordelingEntitet søknad4ukerFPFF() {
        LocalDate fødselsdato = LocalDate.now();
        OppgittPeriode periode1 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
            .medPeriode(fødselsdato.minusWeeks(10), fødselsdato.minusDays(1))
            .medVirksomhet(virksomhet())
            .build();
        return new OppgittFordelingEntitet(Collections.singletonList(periode1), true);
    }

    private Behandling byggBehandlingForElektroniskSøknadOmFødsel(Fagsak fagsak, LocalDate fødselsdato, LocalDate mottattDato) {
        Behandling behandling = Behandling.forFørstegangssøknad(fagsak).build();

        behandling.setAnsvarligSaksbehandler("VL");
        repository.lagre(behandling);

        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.INNVILGET).buildFor(behandling);
        repository.lagre(behandlingsresultat);

        VilkårResultat vilkårResultat = VilkårResultat.builder().medVilkårResultatType(VilkårResultatType.INNVILGET).buildFor(behandling);
        repository.lagre(vilkårResultat);
        repository.flushAndClear();

        final FamilieHendelseBuilder søknadHendelse = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medAntallBarn(1)
            .medFødselsDato(fødselsdato);
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadHendelse);

        final FamilieHendelseBuilder bekreftetHendelse = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medAntallBarn(1)
            .medFødselsDato(fødselsdato);
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, bekreftetHendelse);

        OppgittFordelingEntitet fordeling;
        if (fagsak.getRelasjonsRolleType().equals(RelasjonsRolleType.MORA)) {
            OppgittPeriode periode0 = OppgittPeriodeBuilder.ny()
                .medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
                .medPeriode(fødselsdato.minusWeeks(3), fødselsdato.minusDays(1))
                .medVirksomhet(virksomhet())
                .build();

            OppgittPeriode periode1 = OppgittPeriodeBuilder.ny()
                .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
                .medPeriode(fødselsdato, fødselsdato.plusWeeks(6))
                .medVirksomhet(virksomhet())
                .build();

            OppgittPeriode periode2 = OppgittPeriodeBuilder.ny()
                .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
                .medPeriode(fødselsdato.plusWeeks(6).plusDays(1), fødselsdato.plusWeeks(10))
                .medVirksomhet(virksomhet())
                .build();


            fordeling = new OppgittFordelingEntitet(Arrays.asList(periode0, periode1, periode2), true);
        } else {
            OppgittPeriode periodeFK = OppgittPeriodeBuilder.ny()
                .medPeriodeType(UttakPeriodeType.FEDREKVOTE)
                .medPeriode(fødselsdato.plusWeeks(10).plusDays(1), fødselsdato.plusWeeks(20))
                .medVirksomhet(virksomhet())
                .build();

            fordeling = new OppgittFordelingEntitet(Collections.singletonList(periodeFK), true);
        }

        ytelsesFordelingRepository.lagre(behandling, fordeling);

        OppgittDekningsgrad dekningsgrad = OppgittDekningsgradEntitet.bruk100();
        ytelsesFordelingRepository.lagre(behandling, dekningsgrad);

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

    private void byggArbeidForBehandling(Behandling behandling) {
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = inntektArbeidYtelseRepository.opprettBuilderFor(behandling, VersjonType.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder.getAktørArbeidBuilder(AKTØRID);
        YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(new Opptjeningsnøkkel(ARBEIDSFORHOLD_ID, ORGNR, null),
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();

        LocalDate fraOgMed = LocalDate.now().minusYears(1);
        LocalDate tilOgMed = LocalDate.now().plusYears(10);

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed))
            .medProsentsats(BigDecimal.TEN)
            .medAntallTimer(BigDecimal.valueOf(20.4d))
            .medAntallTimerFulltid(BigDecimal.valueOf(10.2d));

        Virksomhet virksomhet = virksomhet(fraOgMed);

        yrkesaktivitetBuilder
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet))
            .medArbeidsforholdId(ArbeidsforholdRef.ref(ARBEIDSFORHOLD_ID))
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

        Inntektsmelding im = InntektsmeldingBuilder.builder()
            .medVirksomhet(virksomhet)
            .medArbeidsforholdId(ARBEIDSFORHOLD_ID)
            .medBeløp(BigDecimal.valueOf(100000))
            .medMottattDokument(mottattDokument)
            .medInnsendingstidspunkt(LocalDateTime.now())
            .medStartDatoPermisjon(LocalDate.now())
            .build();
        inntektArbeidYtelseRepository.lagre(behandling, im);
    }

    private Virksomhet virksomhet(LocalDate fraOgMed) {
        final VirksomhetRepository virksomhetRepository = repositoryProvider.getVirksomhetRepository();
        final Optional<Virksomhet> hent = virksomhetRepository.hent(ORGNR);
        if (hent.isPresent()) {
            return hent.get();
        }
        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(ORGNR)
            .medNavn("Virksomheten")
            .medRegistrert(fraOgMed.minusYears(2L))
            .medOppstart(fraOgMed.minusYears(1L))
            .oppdatertOpplysningerNå()
            .build();
        virksomhetRepository.lagre(virksomhet);
        return virksomhet;
    }

    private Virksomhet virksomhet() {
        final VirksomhetRepository virksomhetRepository = repositoryProvider.getVirksomhetRepository();
        final Optional<Virksomhet> hent = virksomhetRepository.hent(ORGNR);
        if (hent.isPresent()) {
            return hent.get();
        }
        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(ORGNR)
            .medNavn("Virksomheten")
            .medRegistrert(LocalDate.now().minusYears(2L))
            .medOppstart(LocalDate.now().minusYears(1L))
            .oppdatertOpplysningerNå()
            .build();
        virksomhetRepository.lagre(virksomhet);
        return virksomhet;
    }

    private void opprettUttaksperiodegrense(LocalDate mottattDato, Behandling behandling) {
        Uttaksperiodegrense uttaksperiodegrense = new Uttaksperiodegrense.Builder(behandling)
            .medMottattDato(mottattDato)
            .medFørsteLovligeUttaksdag(mottattDato.withDayOfMonth(1).minusMonths(3))
            .build();

        uttakRepository.lagreUttaksperiodegrense(behandling, uttaksperiodegrense);
    }

    private FastsettePerioderTjenesteImpl periodeTjeneste() {
        FastsettePerioderRegelGrunnlagByggerImpl regelGrunnlagBygger = new FastsettePerioderRegelGrunnlagByggerImpl(repositoryProvider,
            new ArbeidTidslinjeTjenesteImpl(repositoryProvider, new UttakStillingsprosentTjenesteImpl(uttakArbeidTjeneste), behandling -> Collections.emptyList(), uttakArbeidTjeneste),
            new RelatertBehandlingTjenesteImpl(repositoryProvider), uttakArbeidTjeneste);
        FastsettePerioderRegelResultatKonvertererImpl regelResultatKonverterer = new FastsettePerioderRegelResultatKonvertererImpl(repositoryProvider);
        return new FastsettePerioderTjenesteImpl(repositoryProvider,
            (fagsak, opprinnelig, perioder) -> {
            },
            new FastsettePerioderRegelAdapter(regelGrunnlagBygger, regelResultatKonverterer, disableAllUnleash()));
    }

    private Unleash disableAllUnleash() {
        FakeUnleash fakeUnleash = new FakeUnleash();
        fakeUnleash.disableAll();
        return fakeUnleash;
    }

}
