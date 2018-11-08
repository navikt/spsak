package no.nav.foreldrepenger.behandling.steg.uttak.impl;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import no.nav.foreldrepenger.behandling.steg.uttak.FastsettUttaksgrunnlagOgVurderSøknadsfristSteg;
import no.nav.foreldrepenger.behandling.søknadsfrist.SøknadsfristForeldrepengerTjeneste;
import no.nav.foreldrepenger.behandling.søknadsfrist.impl.SøknadsfristForeldrepengerTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittDekningsgrad;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittDekningsgradEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.FastsettUttaksgrunnlagTjeneste;
import no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.impl.EndringsdatoFørstegangsbehandlingUtleder;
import no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.impl.EndringsdatoRevurderingUtleder;
import no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.impl.FastsettUttaksgrunnlagTjenesteImpl;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class FastsettUttaksgrunnlagOgVurderSøknadsfristStegImplTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    private BehandlingRepositoryProvider behandlingRepositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private BehandlingRepository behandlingRepository = new BehandlingRepositoryImpl(repoRule.getEntityManager());
    private YtelsesFordelingRepository ytelsesFordelingRepository = new YtelsesFordelingRepositoryImpl(repoRule.getEntityManager());

    @Mock
    private EndringsdatoRevurderingUtleder endringsdatoRevurderingUtleder;

    private AktørId AKTØRID = new AktørId("1");
    private Behandling behandling;
    private FastsettUttaksgrunnlagOgVurderSøknadsfristSteg fastsettUttaksgrunnlagOgVurderSøknadsfristSteg;

    @Before
    public void setup() {
        Personinfo personinfo = new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(AKTØRID)
            .medFødselsdato(LocalDate.now().minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(PersonIdent.fra("12312312312"))
            .medForetrukketSpråk(Språkkode.nb)
            .build();
        Fagsak fagsak = FagsakBuilder.nyForeldrepengerForMor().medBrukerPersonInfo(personinfo).build();
        repository.lagre(fagsak.getNavBruker());
        repository.lagre(fagsak);

        Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(fagsak);

        behandling = behandlingBuilder.build();
        behandling.setAnsvarligSaksbehandler("VL");
        repository.lagre(behandling);

        Behandlingsresultat behandlingsresultat = Behandlingsresultat.opprettFor(behandling);
        repository.lagre(behandlingsresultat);
        repository.flushAndClear();

        SøknadsfristForeldrepengerTjeneste søknadsfristForeldrepengerTjeneste = new SøknadsfristForeldrepengerTjenesteImpl(behandlingRepositoryProvider, 3);
        EndringsdatoFørstegangsbehandlingUtleder endringsdatoFørstegangsbehandlingUtleder = new EndringsdatoFørstegangsbehandlingUtleder(ytelsesFordelingRepository);
        FastsettUttaksgrunnlagTjeneste fastsettUttaksgrunnlagTjeneste = new FastsettUttaksgrunnlagTjenesteImpl(behandlingRepositoryProvider, endringsdatoFørstegangsbehandlingUtleder, endringsdatoRevurderingUtleder);
        fastsettUttaksgrunnlagOgVurderSøknadsfristSteg = new FastsettUttaksgrunnlagOgVurderSøknadsfristStegImpl(behandlingRepositoryProvider, søknadsfristForeldrepengerTjeneste, fastsettUttaksgrunnlagTjeneste);
    }

    @Test
    public void skalOppretteAksjonspunktForÅVurdereSøknadsfristHvisSøktePerioderUtenforSøknadsfrist() {
        LocalDate mottattDato = LocalDate.now();
        LocalDate førsteUttaksdato = mottattDato.with(DAY_OF_MONTH, 1).minusMonths(3).minusDays(1); //En dag forbi søknadsfrist
        OppgittPeriode periode1 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(førsteUttaksdato, førsteUttaksdato.plusWeeks(6))
            .build();

        OppgittPeriode periode2 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medPeriode(førsteUttaksdato.plusWeeks(6).plusDays(1), førsteUttaksdato.plusWeeks(10))
            .build();

        OppgittDekningsgrad dekningsgrad = OppgittDekningsgradEntitet.bruk100();
        ytelsesFordelingRepository.lagre(behandling, dekningsgrad);

        OppgittFordelingEntitet fordeling = new OppgittFordelingEntitet(Arrays.asList(periode1, periode2), true);
        ytelsesFordelingRepository.lagre(behandling, fordeling);

        final Søknad søknad = opprettSøknad(førsteUttaksdato, mottattDato, dekningsgrad, fordeling);
        behandlingRepositoryProvider.getSøknadRepository().lagreOgFlush(behandling, søknad);
        repository.flushAndClear();
        Fagsak fagsak = behandling.getFagsak();
        // Act
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingRepository.taSkriveLås(behandling));
        BehandleStegResultat behandleStegResultat = fastsettUttaksgrunnlagOgVurderSøknadsfristSteg.utførSteg(kontekst);
        repository.flushAndClear();

        // Assert
        assertThat(behandleStegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(behandleStegResultat.getAksjonspunktListe()).hasSize(1);
        assertThat(behandleStegResultat.getAksjonspunktListe().get(0)).isEqualTo(AksjonspunktDefinisjon.MANUELL_VURDERING_AV_SØKNADSFRIST_FORELDREPENGER);

        Behandling lagretBehandling = behandlingRepository.hentBehandling(behandling.getId());
        Optional<Uttaksperiodegrense> gjeldendeUttaksperiodegrense = lagretBehandling.getBehandlingsresultat().getGjeldendeUttaksperiodegrense();
        assertThat(gjeldendeUttaksperiodegrense).isPresent();
        assertThat(gjeldendeUttaksperiodegrense.get().getFørsteLovligeUttaksdag()).isEqualTo(førsteUttaksdato.plusDays(1));
        assertThat(gjeldendeUttaksperiodegrense.get().getMottattDato()).isEqualTo(mottattDato);
    }

    @Test
    public void skalIkkeOppretteAksjonspunktHvisSøktePerioderInnenforSøknadsfrist() {
        LocalDate førsteUttaksdato = LocalDate.now().with(DAY_OF_MONTH, 1).minusMonths(3);
        LocalDate mottattDato = LocalDate.now();
        OppgittPeriode periode1 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(førsteUttaksdato, førsteUttaksdato.plusWeeks(6))
            .build();

        OppgittPeriode periode2 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medPeriode(førsteUttaksdato.plusWeeks(6).plusDays(1), førsteUttaksdato.plusWeeks(10))
            .build();

        OppgittDekningsgrad dekningsgrad = OppgittDekningsgradEntitet.bruk100();
        ytelsesFordelingRepository.lagre(behandling, dekningsgrad);

        OppgittFordelingEntitet fordeling = new OppgittFordelingEntitet(Arrays.asList(periode1, periode2), true);
        ytelsesFordelingRepository.lagre(behandling, fordeling);

        final Søknad søknad = opprettSøknad(førsteUttaksdato, mottattDato, dekningsgrad, fordeling);
        behandlingRepositoryProvider.getSøknadRepository().lagreOgFlush(behandling, søknad);
        repository.flushAndClear();

        Fagsak fagsak = behandling.getFagsak();
        // Act
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingRepository.taSkriveLås(behandling));
        BehandleStegResultat behandleStegResultat = fastsettUttaksgrunnlagOgVurderSøknadsfristSteg.utførSteg(kontekst);
        repository.flushAndClear();

        // Assert
        assertThat(behandleStegResultat.getAksjonspunktListe()).hasSize(0);
        assertThat(behandleStegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);

        Behandling lagretBehandling = behandlingRepository.hentBehandling(behandling.getId());
        Optional<Uttaksperiodegrense> gjeldendeUttaksperiodegrense = lagretBehandling.getBehandlingsresultat().getGjeldendeUttaksperiodegrense();
        assertThat(gjeldendeUttaksperiodegrense).isPresent();
        assertThat(gjeldendeUttaksperiodegrense.get().getFørsteLovligeUttaksdag()).isEqualTo(førsteUttaksdato);
        assertThat(gjeldendeUttaksperiodegrense.get().getMottattDato()).isEqualTo(mottattDato);
    }

    private Søknad opprettSøknad(LocalDate fødselsdato, LocalDate mottattDato, OppgittDekningsgrad dekningsgrad, OppgittFordelingEntitet fordeling) {
        final FamilieHendelseBuilder søknadHendelse = behandlingRepositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medAntallBarn(1)
            .medFødselsDato(fødselsdato);
        behandlingRepositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadHendelse);


        return new SøknadEntitet.Builder()
            .medSøknadsdato(LocalDate.now())
            .medMottattDato(mottattDato)
            .medElektroniskRegistrert(true)
            .medDekningsgrad(dekningsgrad)
            .medFordeling(fordeling)
            .medFamilieHendelse(behandlingRepositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling).getSøknadVersjon())
            .build();
    }


}
