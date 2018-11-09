package no.nav.foreldrepenger.behandling.steg.beregnytelse.es;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Sats;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.SatsType;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.Tuple;

@RunWith(CdiRunner.class)
public class BeregneYtelseStegImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private Repository repository = repoRule.getRepository();
    @Inject
    @KonfigVerdi(value = "maks.stønadsalder.adopsjon")
    private int maksStønadsalder;

    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private BeregningRepository beregningRepository = new BeregningRepositoryImpl(repoRule.getEntityManager());
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider,
        new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
        new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0)),
        Period.of(0, 3, 0),
        Period.of(0, 10, 0));

    private BeregneYtelseEngangsstønadStegImpl beregneYtelseSteg;
    private Fagsak fagsak = FagsakBuilder.nyEngangstønadForMor().build();

    private Sats sats;
    private Sats sats2017;

    @Before
    public void oppsett() {
        repository.lagre(fagsak.getNavBruker());
        repository.lagre(fagsak);
        repository.flush();

        sats = repository.hentAlle(Sats.class).stream()
            .filter(sats -> sats.getSatsType().equals(SatsType.ENGANG) &&
                sats.getPeriode().overlapper(DatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now().plusDays(1))))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Feil i testdataoppsett"));

        sats2017 = repository.hentAlle(Sats.class).stream()
            .filter(sats -> sats.getSatsType().equals(SatsType.ENGANG) &&
                sats.getPeriode().overlapper(DatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.of(2017,10, 1), LocalDate.of(2017,11, 1))))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Feil i testdataoppsett"));

        beregneYtelseSteg = new BeregneYtelseEngangsstønadStegImpl(repositoryProvider, beregningRepository, maksStønadsalder, skjæringstidspunktTjeneste);
    }

    @Test
    public void skal_beregne_sats_basert_på_antall_barn() {
        // Arrange
        int antallBarn = 2;
        BehandlingskontrollKontekst kontekst = byggBehandlingsgrunnlagForFødsel(antallBarn, LocalDate.now());

        // Act
        beregneYtelseSteg.utførSteg(kontekst);

        // Assert
        BeregningResultat beregningResultat = repository.hent(Behandling.class, kontekst.getBehandlingId())
            .getBehandlingsresultat().getBeregningResultat();
        assertThat(beregningResultat.getBeregninger().get(0)).isNotNull();

        Beregning beregning = beregningResultat.getBeregninger().get(0);
        assertThat(beregning.getSatsVerdi()).isEqualTo(sats.getVerdi());
        assertThat(beregning.getBeregnetTilkjentYtelse()).isEqualTo(sats.getVerdi() * antallBarn);
    }

    @Test
    public void skal_beregne_sats_for_fødsel_i_2017() {
        // Arrange
        int antallBarn = 2;
        BehandlingskontrollKontekst kontekst = byggBehandlingsgrunnlagForFødsel(antallBarn, LocalDate.of(2017,10, 1));

        // Act
        beregneYtelseSteg.utførSteg(kontekst);

        // Assert
        BeregningResultat beregningResultat = repository.hent(Behandling.class, kontekst.getBehandlingId())
            .getBehandlingsresultat().getBeregningResultat();
        assertThat(beregningResultat.getBeregninger().get(0)).isNotNull();

        Beregning beregning = beregningResultat.getBeregninger().get(0);
        assertThat(beregning.getSatsVerdi()).isEqualTo(sats2017.getVerdi());
        assertThat(beregning.getBeregnetTilkjentYtelse()).isEqualTo(sats2017.getVerdi() * antallBarn);
    }

    @Test
    public void skal_kaste_feil_dersom_eksakt_sats_ikke_kan_identifiseres() {
        // Arrange
        Sats satsSomOverlapperEksisterendeSats = new Sats(SatsType.ENGANG, DatoIntervallEntitet.fraOgMed(LocalDate.now()), 123L);
        repository.lagre(satsSomOverlapperEksisterendeSats);

        int antallBarn = 1;
        BehandlingskontrollKontekst kontekst = byggBehandlingsgrunnlagForFødsel(antallBarn, LocalDate.now());

        expectedException.expect(TekniskException.class);

        // Act
        beregneYtelseSteg.utførSteg(kontekst);
    }

    @Test
    public void skal_ved_tilbakehopp_fremover_rydde_avklarte_fakta() {
        // Arrange
        int antallBarn = 1;
        Tuple<Behandling, BehandlingskontrollKontekst> behandlingKontekst = byggGrunnlag(antallBarn, LocalDate.now());
        Behandling behandling = behandlingKontekst.getElement1();
        BehandlingskontrollKontekst kontekst = behandlingKontekst.getElement2();
        BeregningResultat beregningResultat = BeregningResultat.builder()
            .medBeregning(new Beregning(1000L, antallBarn, 1000L, LocalDateTime.now()))
            .buildFor(behandling);
        beregningRepository.lagre(beregningResultat, kontekst.getSkriveLås());

        // Act
        beregneYtelseSteg.vedTransisjon(kontekst, behandling, null, BehandlingSteg.TransisjonType.HOPP_OVER_BAKOVER, null, null, BehandlingSteg.TransisjonType.FØR_INNGANG);

        // Assert
        Behandlingsresultat behandlingsresultat = repository.hent(Behandling.class, kontekst.getBehandlingId()).getBehandlingsresultat();
        assertThat(behandlingsresultat.getBeregningResultat().getBeregninger()).isEmpty();
    }

    @Test
    public void skal_ved_tilbakehopp_fremover_ikke_rydde_overstyrte_beregninger() {
        // Arrange
        int antallBarn = 1;
        Tuple<Behandling, BehandlingskontrollKontekst> behandlingKontekst = byggGrunnlag(antallBarn, LocalDate.now());
        Behandling behandling = behandlingKontekst.getElement1();
        BehandlingskontrollKontekst kontekst = behandlingKontekst.getElement2();
        BeregningResultat beregningResultat = BeregningResultat.builder()
            .medBeregning(new Beregning(1000L, antallBarn, 1000L, LocalDateTime.now(), false, null))
            .medBeregning(new Beregning(500L, antallBarn, 1000L, LocalDateTime.now(), true, 1000L))
            .buildFor(behandling);
        beregningRepository.lagre(beregningResultat, kontekst.getSkriveLås());

        // Act
        beregneYtelseSteg.vedTransisjon(kontekst, behandling, null, BehandlingSteg.TransisjonType.HOPP_OVER_BAKOVER, null, null, BehandlingSteg.TransisjonType.FØR_INNGANG);

        // Assert
        Behandlingsresultat behandlingsresultat = repository.hent(Behandling.class, kontekst.getBehandlingId()).getBehandlingsresultat();
        assertThat(behandlingsresultat.getBeregningResultat().getBeregninger().size()).isEqualTo(2);
        assertThat(behandlingsresultat.getBeregningResultat().isOverstyrt()).isTrue();
    }

    @Test
    public void skal_ved_fremhopp_rydde_avklarte_fakta_inkludert_overstyrte_beregninger() {
        // Arrange
        int antallBarn = 1;
        Tuple<Behandling, BehandlingskontrollKontekst> behandlingKontekst = byggGrunnlag(antallBarn, LocalDate.now());
        Behandling behandling = behandlingKontekst.getElement1();
        BehandlingskontrollKontekst kontekst = behandlingKontekst.getElement2();
        BeregningResultat beregningResultat = BeregningResultat.builder()
            .medBeregning(new Beregning(1000L, antallBarn, 1000L, LocalDateTime.now(), false, null))
            .medBeregning(new Beregning(500L, antallBarn, 1000L, LocalDateTime.now(), true, 1000L))
            .buildFor(behandling);
        beregningRepository.lagre(beregningResultat, kontekst.getSkriveLås());

        // Act
        beregneYtelseSteg.vedTransisjon(kontekst, behandling, null, BehandlingSteg.TransisjonType.HOPP_OVER_FRAMOVER, null, null, BehandlingSteg.TransisjonType.FØR_INNGANG);

        // Assert
        Behandlingsresultat behandlingsresultat = repository.hent(Behandling.class, kontekst.getBehandlingId()).getBehandlingsresultat();
        assertThat(behandlingsresultat.getBeregningResultat().getBeregninger()).isEmpty();
    }

    private Tuple<Behandling, BehandlingskontrollKontekst> byggGrunnlag(int antallBarn, LocalDate fødselsdato) {
        Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(fagsak);
        Behandling behandling = behandlingBuilder.build();

        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        final FamilieHendelseBuilder søknadVersjon = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medFødselsDato(fødselsdato)
            .medAntallBarn(antallBarn);
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadVersjon);
        final FamilieHendelseBuilder bekreftetVersjon = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medAntallBarn(antallBarn).tilbakestillBarn();
        IntStream.range(0, antallBarn).forEach(it -> bekreftetVersjon.medFødselsDato(fødselsdato));
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, bekreftetVersjon);
        Søknad søknad = new SøknadEntitet.Builder()
            .medSøknadsdato(LocalDate.now())
            .medFamilieHendelse(repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling).getSøknadVersjon())
            .build();
        repositoryProvider.getSøknadRepository().lagreOgFlush(behandling, søknad);

        return new Tuple<>(behandling, kontekst);
    }

    private BehandlingskontrollKontekst byggBehandlingsgrunnlagForFødsel(int antallBarn, LocalDate fødselsdato) {
        Tuple<Behandling, BehandlingskontrollKontekst> behandlingskontekst = byggGrunnlag(antallBarn, fødselsdato);
        return behandlingskontekst.getElement2();
    }

}
