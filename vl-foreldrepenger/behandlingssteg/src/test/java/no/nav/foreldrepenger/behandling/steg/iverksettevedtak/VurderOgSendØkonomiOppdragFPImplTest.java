package no.nav.foreldrepenger.behandling.steg.iverksettevedtak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Avstemming115;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.TfradragTillegg;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKomponent;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeStatusLinje;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiTypeSats;
import no.nav.foreldrepenger.økonomistøtte.api.ØkonomioppdragApplikasjonTjeneste;
import no.nav.foreldrepenger.økonomistøtte.ØkonomioppdragRepository;
import no.nav.foreldrepenger.økonomistøtte.ØkonomioppdragRepositoryImpl;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;

@RunWith(CdiRunner.class)
public class VurderOgSendØkonomiOppdragFPImplTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private Repository repository = repoRule.getRepository();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private final BeregningsresultatFPRepository beregningsresultatFPRepository = repositoryProvider.getBeregningsresultatFPRepository();
    private ØkonomioppdragRepository økonomioppdragRepository = new ØkonomioppdragRepositoryImpl(entityManager);

    private ØkonomioppdragApplikasjonTjeneste økonomioppdragApplikasjonTjeneste;
    private VurderOgSendØkonomiOppdrag vurderOgSendØkonomiOppdrag;

    @FagsakYtelseTypeRef("FP")
    @Inject
    private Instance<VurderØkonomiOppdrag> vurderØkonomiOppdragInstance;

    @Before
    public void setup() {
        økonomioppdragApplikasjonTjeneste = mock(ØkonomioppdragApplikasjonTjeneste.class);

        VurderØkonomiOppdragProvider vurderØkonomiOppdragProvider = new VurderØkonomiOppdragProvider(repositoryProvider, vurderØkonomiOppdragInstance);
        vurderOgSendØkonomiOppdrag = new VurderOgSendØkonomiOppdragImpl(repositoryProvider, økonomioppdragApplikasjonTjeneste, vurderØkonomiOppdragProvider);
    }

    private Behandling lagBehandling(BehandlingType behandlingType, VedtakResultatType vedtakResultatType, Avslagsårsak avslagsårsak, boolean isBeslutningsvedtak) {
        return lagBehandling(behandlingType, vedtakResultatType, avslagsårsak, isBeslutningsvedtak, true);
    }

    private Behandling lagBehandling(BehandlingType behandlingType, VedtakResultatType vedtakResultatType, Avslagsårsak avslagsårsak,
                                     boolean isBeslutningsvedtak, boolean medBehandlingVedtak) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBehandlingType(behandlingType);
        Behandling behandling = scenario.lagre(repositoryProvider);

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        Behandlingsresultat behandlingsresultat = opprettBehandlingsresultat(avslagsårsak, behandling);
        BehandlingVedtak behandlingVedtak = opprettBehandlingVedtak(behandlingsresultat, vedtakResultatType, isBeslutningsvedtak);

        behandlingRepository.lagre(behandling.getBehandlingsresultat().getVilkårResultat(), lås);
        repository.lagre(behandling.getBehandlingsresultat());
        if (medBehandlingVedtak) {
            repositoryProvider.getBehandlingVedtakRepository().lagre(behandlingVedtak, lås);
        }
        repository.flush();

        return behandling;
    }

    private Behandlingsresultat opprettBehandlingsresultat(Avslagsårsak avslagsårsak, Behandling behandling) {
        Behandlingsresultat.Builder builder = Behandlingsresultat.builderForInngangsvilkår();
        if (avslagsårsak != null) {
            builder.medAvslagsårsak(avslagsårsak);
        }
        return builder.buildFor(behandling);
    }

    private BehandlingVedtak opprettBehandlingVedtak(Behandlingsresultat behandlingsresultat, VedtakResultatType resultatType, boolean isBeslutningsvedtak) {
        return BehandlingVedtak.builder()
            .medVedtaksdato(LocalDate.now().minusDays(3))
            .medAnsvarligSaksbehandler("E2354345")
            .medVedtakResultatType(resultatType)
            .medBehandlingsresultat(behandlingsresultat)
            .medBeslutning(isBeslutningsvedtak)
            .build();
    }

    @Test
    public void skalSendeOppdragNårFørstegangsbehandlingVedtakResultatInnvilgetOgHarTilkjentYtelse() {
        // Arrange
        //BehandlingType: Førstegangssøknad, VedtakResultat: Innvilget, Tilkjent ytelse: Ja, finnesForrigeOppdrag: N/A, Beslutningsvedtak: N/A
        Long behandlingId = oppsettBehandling(BehandlingType.FØRSTEGANGSSØKNAD, VedtakResultatType.INNVILGET, true, false, false);

        // Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);
        // Assert
        assertThat(skalSende).isTrue();
    }

    @Test
    public void skalIkkeSendeOppdragNårFørstegangsbehandlingVedtakResultatInnvilgetOgIkkeHarTilkjentYtelse() {
        // Arrange
        //BehandlingType: Førstegangssøknad, VedtakResultat: Innvilget, Tilkjent ytelse: Nei, finnesForrigeOppdrag: N/A, Beslutningsvedtak: N/A
        Long behandlingId = oppsettBehandling(BehandlingType.FØRSTEGANGSSØKNAD, VedtakResultatType.INNVILGET, false, false, false);

        // Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        // Assert
        assertThat(skalSende).isFalse();
    }

    @Test
    public void skalIkkeSendeOppdragNårFørstegangssøknadHarVedtakAvslagOgIkkeHarTilkjentYtelse() {
        // Arrange
        //BehandlingType: Førstegangssøknad, VedtakResultat: Innvilget, Tilkjent ytelse: Nei, finnesForrigeOppdrag: N/A, Beslutningsvedtak: N/A
        Long behandlingId = oppsettBehandling(BehandlingType.FØRSTEGANGSSØKNAD, VedtakResultatType.AVSLAG, false, false, false);

        // Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        // Assert
        assertThat(skalSende).isFalse();
    }

    @Test
    public void skalIkkeSendeOppdragNårFørstegangssøknadIkkeHarBehandlingVedtak() {
        // Arrange
        //BehandlingType: Førstegangssøknad, BehandlingVedtak: Nei
        Long behandlingId = lagBehandling(BehandlingType.FØRSTEGANGSSØKNAD, VedtakResultatType.UDEFINERT, null, false, false).getId();

        // Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        // Assert
        assertThat(skalSende).isFalse();
    }

    @Test
    public void skalIkkeSendeOppdragNårRevurderingHarBeslutningsvedtakOgHarTilkjentYtelse() {
        // Arrange
        //BehandlingType: Revurdering, VedtakResultat: Innvilget, Tilkjent ytelse: Ja, finnesForrigeOppdrag: Ja, Beslutningsvedtak: Ja
        Long behandlingId = oppsettBehandling(BehandlingType.REVURDERING, VedtakResultatType.INNVILGET, true, true, true);

        //Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        //Assert
        assertThat(skalSende).isFalse();
    }

    @Test
    public void skalIkkeSendeOppdragNårRevurderingHarBeslutningsvedtakHarTilkjentYtelseOgIkkeHarForrigeOppdrag() {
        // Arrange
        //BehandlingType: Revurdering, VedtakResultat: Innvilget, Tilkjent ytelse: Ja, finnesForrigeOppdrag: Nei, Beslutningsvedtak: Ja
        Long behandlingId = oppsettBehandling(BehandlingType.REVURDERING, VedtakResultatType.INNVILGET, true, false, true);

        //Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        //Assert
        assertThat(skalSende).isFalse();
    }

    @Test
    public void skalIkkeSendeOppdragNårRevurderingHarBesutningsvedtakOgHarIkkeTilkjentYtelse() {
        // Arrange
        //BehandlingType: Revurdering, VedtakResultat: Innvilget, Tilkjent ytelse: Nei, finnesForrigeOppdrag: Ja, Beslutningsvedtak: Ja
        Long behandlingId = oppsettBehandling(BehandlingType.REVURDERING, VedtakResultatType.INNVILGET, false, true, true);

        //Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        //Assert
        assertThat(skalSende).isFalse();
    }

    @Test
    public void skalIkkeSendeOppdragNårRevurderingHarBesutningsvedtakOgIkkeHarForrigeOppdrag() {
        // Arrange
        //BehandlingType: Revurdering, VedtakResultat: Innvilget, Tilkjent ytelse: Nei, finnesForrigeOppdrag: Nei, Beslutningsvedtak: Ja
        Long behandlingId = oppsettBehandling(BehandlingType.REVURDERING, VedtakResultatType.INNVILGET, false, false, true);

        //Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        //Assert
        assertThat(skalSende).isFalse();
    }

    @Test
    public void skalIkkeSendeOppdragNårRevurderingHarBeslutningsvedtakMedResultatAvslagOgHarTilkjentYtelse() {
        // Arrange
        //BehandlingType: Revurdering, VedtakResultat: Avslag, Tilkjent ytelse: Ja, finnesForrigeOppdrag: Ja, Beslutningsvedtak: Ja
        Long behandlingId = oppsettBehandling(BehandlingType.REVURDERING, VedtakResultatType.AVSLAG, true, true, true);

        //Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        //Assert
        assertThat(skalSende).isFalse();
    }

    @Test
    public void skalIkkeSendeOppdragNårRevurderingHarBeslutningsvedtakMedResultatAvslagHarTilkjentYtelseOgIkkeHarForrigeOppdrag() {
        // Arrange
        //BehandlingType: Revurdering, VedtakResultat: Avslag, Tilkjent ytelse: Ja, finnesForrigeOppdrag: Nei, Beslutningsvedtak: Ja
        Long behandlingId = oppsettBehandling(BehandlingType.REVURDERING, VedtakResultatType.AVSLAG, true, false, true);

        //Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        //Assert
        assertThat(skalSende).isFalse();
    }

    @Test
    public void skalIkkeSendeOppdragNårRevurderingHarBeslutningsvedtakMedResultatAvslagOgIkkeHarTilkjentYtelse() {
        // Arrange
        //BehandlingType: Revurdering, VedtakResultat: Avslag, Tilkjent ytelse: Nei, finnesForrigeOppdrag: Ja, Beslutningsvedtak: Ja
        Long behandlingId = oppsettBehandling(BehandlingType.REVURDERING, VedtakResultatType.AVSLAG, false, true, true);

        //Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        //Assert
        assertThat(skalSende).isFalse();
    }

    @Test
    public void skalIkkeSendeOppdragNårRevurderingHarBeslutningsvedtakMedResultatAvslagIkkeHarTilkjentYtelseOgHarIkkeForrigeOppdrag() {
        // Arrange
        //BehandlingType: Revurdering, VedtakResultat: Avslag, Tilkjent ytelse: Nei, finnesForrigeOppdrag: Nei, Beslutningsvedtak: Ja
        Long behandlingId = oppsettBehandling(BehandlingType.REVURDERING, VedtakResultatType.AVSLAG, false, false, true);

        //Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        //Assert
        assertThat(skalSende).isFalse();
    }

    @Test
    public void skalSendeOppdragNårRevurderingHarVedtakInnvilgetOgHarTilkjentYtelse() {
        // Arrange
        //BehandlingType: Revurdering, VedtakResultat: Innvilget, Tilkjent ytelse: Ja, finnesForrigeOppdrag: Ja, Beslutningsvedtak: Nei
        Long behandlingId = oppsettBehandling(BehandlingType.REVURDERING, VedtakResultatType.INNVILGET, true, true, false);

        //Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        //Assert
        assertThat(skalSende).isTrue();
    }

    @Test
    public void skalSendeOppdragNårRevurderingHarVedtakInnvilgetHarTilkjentYtelseOgIkkeHarForrigeOppdrag() {
        // Arrange
        //BehandlingType: Revurdering, VedtakResultat: Innvilget, Tilkjent ytelse: Ja, finnesForrigeOppdrag: Nei, Beslutningsvedtak: Nei
        Long behandlingId = oppsettBehandling(BehandlingType.REVURDERING, VedtakResultatType.INNVILGET, true, false, false);

        //Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        //Assert
        assertThat(skalSende).isTrue();
    }

    @Test
    public void skalIkkeSendeOppdragNårRevurderingHarVedtakInnvilgetHarForrigeOppdragOgIkkeHarTilkjentYtelse() {
        // Arrange
        //BehandlingType: Revurdering, VedtakResultat: Innvilget, Tilkjent ytelse: Nei, finnesForrigeOppdrag: Ja, Beslutningsvedtak: Nei
        Long behandlingId = oppsettBehandling(BehandlingType.REVURDERING, VedtakResultatType.INNVILGET, false, true, false);

        //Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        //Assert
        assertThat(skalSende).isFalse();
    }

    @Test
    public void skalIkkeSendeOppdragNårRevurderingHarVedtakInnvilgetIkkeHarForrigeOppdragOgIkkeHarTilkjentYtelse() {
        // Arrange
        //BehandlingType: Revurdering, VedtakResultat: Innvilget, Tilkjent ytelse: Nei, finnesForrigeOppdrag: Nei, Beslutningsvedtak: Nei
        Long behandlingId = oppsettBehandling(BehandlingType.REVURDERING, VedtakResultatType.INNVILGET, false, false, false);

        //Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        //Assert
        assertThat(skalSende).isFalse();
    }

    @Test
    public void skalSendeOppdragNårRevurderingHarVedtakAvslagOgHarForrigeOppdragForInnvilgetYtelse() {
        // Arrange
        //BehandlingType: Revurdering, VedtakResultat: Avslag, Tilkjent ytelse: Ja, finnesForrigeOppdrag: Ja, Beslutningsvedtak: Nei
        Long behandlingId = oppsettBehandling(BehandlingType.REVURDERING, VedtakResultatType.AVSLAG, true, true, false);

        //Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        //Assert
        assertThat(skalSende).isTrue();
    }

    @Test
    public void skalSendeOppdragNårRevurderingHarVedtakAvslagOgHarForrigeOppdragForAvslåttYtelse() {
        // Arrange
        //BehandlingType: Revurdering, VedtakResultat: Avslag, Tilkjent ytelse: Ja, finnesForrigeOppdrag: Ja, gjelderForrigeOppdragInnvilgetYtelse= Nei, Beslutningsvedtak: Nei
        Long behandlingId = oppsettBehandling(BehandlingType.REVURDERING, VedtakResultatType.AVSLAG, true,
            true, false, false);

        //Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        //Assert
        assertThat(skalSende).isTrue();
    }

    @Test
    public void skalSendeOppdragNårRevurderingHarVedtakAvslagIkkeHarTilkjentYtelseOgHarForrigeOppdragForInnvilgetYtelse() {
        // Arrange
        //BehandlingType: Revurdering, VedtakResultat: Avslag, Tilkjent ytelse: Nei, finnesForrigeOppdrag: Ja, Beslutningsvedtak: Nei
        Long behandlingId = oppsettBehandling(BehandlingType.REVURDERING, VedtakResultatType.AVSLAG, false, true, false);

        //Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        //Assert
        assertThat(skalSende).isTrue();
    }

    @Test
    public void skalIkkeSendeOppdragNårRevurderingHarVedtakAvslagIkkeHarTilkjentYtelseOgHarForrigeOppdragForAvslåttYtelse() {
        // Arrange
        //BehandlingType: Revurdering, VedtakResultat: Avslag, Tilkjent ytelse: Nei, finnesForrigeOppdrag: Ja, gjelderForrigeOppdragInnvilgetYtelse= Nei, Beslutningsvedtak: Nei
        Long behandlingId = oppsettBehandling(BehandlingType.REVURDERING, VedtakResultatType.AVSLAG, false,
            true, false, false);

        //Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        //Assert
        assertThat(skalSende).isFalse();
    }

    @Test
    public void skalIkkeSendeOppdragNårRevurderingHarVedtakAvslagIkkeHarTilkjentYtelseOgIkkeHarForrigeOppdrag() {
        // Arrange
        //BehandlingType: Revurdering, VedtakResultat: Avslag, Tilkjent ytelse: Nei, finnesForrigeOppdrag: Nei, Beslutningsvedtak: Nei
        Long behandlingId = oppsettBehandling(BehandlingType.REVURDERING, VedtakResultatType.AVSLAG, false, false, false);

        //Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        //Assert
        assertThat(skalSende).isFalse();
    }

    @Test
    public void skalIkkeSendeOppdragNårRevurderingIkkeHarBehandlingVedtak() {
        // Arrange
        //BehandlingType: Revurdering, BehandlingVedtak: Nei
        Long behandlingId = lagBehandling(BehandlingType.REVURDERING, VedtakResultatType.UDEFINERT, null, false, false).getId();

        // Act
        boolean skalSende = vurderOgSendØkonomiOppdrag.skalSendeOppdrag(behandlingId);

        // Assert
        assertThat(skalSende).isFalse();
    }

    private Long oppsettBehandling(BehandlingType behandlingType, VedtakResultatType vedtakResultatType, boolean finnesTilkjentYtelse, boolean finnesForrigeOppdrag, boolean erBeslutningsvedtak) {
        return oppsettBehandling(behandlingType, vedtakResultatType, finnesTilkjentYtelse, finnesForrigeOppdrag, erBeslutningsvedtak, true);
    }

    private Long oppsettBehandling(BehandlingType behandlingType, VedtakResultatType vedtakResultatType, boolean finnesTilkjentYtelse,
                                   boolean finnesForrigeOppdrag, boolean erBeslutningsvedtak, boolean gjelderForrigeOppdragInnvilgetYtelse) {
        Behandling behandling;
        if (vedtakResultatType.equals(VedtakResultatType.AVSLAG)) {
            behandling = lagBehandling(behandlingType, vedtakResultatType, Avslagsårsak.FAR_HAR_IKKE_OMSORG_FOR_BARNET, erBeslutningsvedtak);
        } else {
            behandling = lagBehandling(behandlingType, vedtakResultatType, null, erBeslutningsvedtak);
        }
        Long behandlingId = behandling.getId();
        oppsettBeregningsresultatFP(behandling, finnesTilkjentYtelse);
        if (finnesForrigeOppdrag) {
            Saksnummer saksnummer = behandling.getFagsak().getSaksnummer();
            Oppdragskontroll oppdragskontroll = lagOppdragskontroll(saksnummer, gjelderForrigeOppdragInnvilgetYtelse);
            økonomioppdragRepository.lagre(oppdragskontroll);
        }
        return behandlingId;
    }

    private void oppsettBeregningsresultatFP(Behandling behandling, boolean finnesTilkjentYtelse) {
        int dagsats = finnesTilkjentYtelse ? 2100 : 0;
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatFP(dagsats);
        beregningsresultatFPRepository.lagre(behandling, beregningsresultatFP);
    }

    private BeregningsresultatFP buildBeregningsresultatFP(int dagsats) {
        BeregningsresultatFP beregningsresultatFP = BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
        BeregningsresultatPeriode brPeriode1 = buildBeregningsresultatPeriode(beregningsresultatFP, 1, 20);
        buildBeregningsresultatAndel(brPeriode1, dagsats);
        BeregningsresultatPeriode brPeriode2 = buildBeregningsresultatPeriode(beregningsresultatFP, 21, 30);
        buildBeregningsresultatAndel(brPeriode2, 0);

        return beregningsresultatFP;
    }

    private BeregningsresultatPeriode buildBeregningsresultatPeriode(BeregningsresultatFP beregningsresultatFP, int fom, int tom) {
        return BeregningsresultatPeriode.builder()
            .medBeregningsresultatPeriodeFomOgTom(LocalDate.now().plusDays(fom), LocalDate.now().plusDays(tom))
            .build(beregningsresultatFP);
    }

    private void buildBeregningsresultatAndel(BeregningsresultatPeriode beregningsresultatPeriode, int dagsats) {
        BeregningsresultatAndel.builder()
            .medBrukerErMottaker(true)
            .medDagsats(dagsats)
            .medDagsatsFraBg(dagsats)
            .medStillingsprosent(BigDecimal.valueOf(100))
            .medUtbetalingsgrad(BigDecimal.valueOf(100))
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .build(beregningsresultatPeriode);
    }

    private Oppdragskontroll lagOppdragskontroll(Saksnummer saksnummer, boolean gjelderForrigeOppdragInnvilgetYtelse) {
        Oppdragskontroll oppdragskontroll = Oppdragskontroll.builder()
            .medBehandlingId(159L)
            .medSaksnummer(saksnummer)
            .medVenterKvittering(Boolean.TRUE)
            .medProsessTaskId(579L)
            .medSimulering(false)
            .build();
        Avstemming115 avstemming115 = lagAvstemming115();
        Oppdrag110 oppdrag110 = lagOppdrag110(oppdragskontroll, avstemming115);
        lagOppdragslinje150(oppdrag110, gjelderForrigeOppdragInnvilgetYtelse);
        return oppdragskontroll;
    }

    private Avstemming115 lagAvstemming115() {
        return Avstemming115.builder()
            .medKodekomponent(ØkonomiKodeKomponent.VLFP.getKodeKomponent())
            .medNokkelAvstemming(LocalDateTime.now())
            .medTidspnktMelding(LocalDateTime.now())
            .build();
    }

    private Oppdrag110 lagOppdrag110(Oppdragskontroll oppdragskontroll, Avstemming115 avstemming115) {
        return Oppdrag110.builder()
            .medKodeAksjon("kode")
            .medKodeEndring("kodeEndr")
            .medKodeFagomrade("kodeFag")
            .medFagSystemId(123L)
            .medUtbetFrekvens("Frekvens")
            .medOppdragGjelderId("id")
            .medDatoOppdragGjelderFom(LocalDate.now())
            .medSaksbehId("saksBehId")
            .medOppdragskontroll(oppdragskontroll)
            .medAvstemming115(avstemming115)
            .build();
    }

    private Oppdragslinje150 lagOppdragslinje150(Oppdrag110 oppdrag110, boolean gjelderForrigeOppdragInnvilgetYtelse) {
        Oppdragslinje150.Builder oppdragslinje150Builder = Oppdragslinje150.builder()
            .medKodeEndringLinje("NY")
            .medDatoStatusFom(LocalDate.now())
            .medVedtakId("345")
            .medDelytelseId(64L)
            .medKodeKlassifik("FPATORD")
            .medVedtakFomOgTom(LocalDate.now(), LocalDate.now())
            .medSats(61122L)
            .medFradragTillegg(TfradragTillegg.F.value())
            .medTypeSats(ØkonomiTypeSats.DAG.name())
            .medBrukKjoreplan("B")
            .medSaksbehId("F2365245")
            .medUtbetalesTilId("123456789")
            .medOppdrag110(oppdrag110)
            .medHenvisning(43L);
        if (!gjelderForrigeOppdragInnvilgetYtelse) {
            oppdragslinje150Builder.medKodeStatusLinje(ØkonomiKodeStatusLinje.OPPH.name());
        }
        return oppdragslinje150Builder.build();
    }
}
