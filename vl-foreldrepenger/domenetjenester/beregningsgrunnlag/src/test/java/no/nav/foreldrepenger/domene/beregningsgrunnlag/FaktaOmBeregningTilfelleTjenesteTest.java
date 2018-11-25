package no.nav.foreldrepenger.domene.beregningsgrunnlag;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.FaktaOmBeregningTilfelle;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FaktaOmBeregningTilfelleTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.KontrollerFaktaBeregningFrilanserTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.KontrollerFaktaBeregningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Beløp;

public class FaktaOmBeregningTilfelleTjenesteTest {

    private static final AktørId AKTØR_ID = new AktørId("210195");
    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final Beløp GRUNNBELØP = new Beløp(600000);

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private BehandlingRepositoryProvider repositoryProvider = Mockito.spy(new BehandlingRepositoryProviderImpl(entityManager));

    private KontrollerFaktaBeregningFrilanserTjeneste kontrollerFaktaBeregningFrilanserTjeneste = mock(KontrollerFaktaBeregningFrilanserTjeneste.class);
    private KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste = mock(KontrollerFaktaBeregningTjeneste.class);

    private FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste;

    private Behandling behandling;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;


    @Before
    public void setup() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forAktør(AKTØR_ID);
        behandling = scenario.lagre(repositoryProvider);
        Beregningsgrunnlag bg = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(bg);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(1L)
            .medLagtTilAvSaksbehandler(true)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);
        beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        beregningsgrunnlagRepository.lagre(behandling, bg, BeregningsgrunnlagTilstand.OPPRETTET);
        faktaOmBeregningTilfelleTjeneste = new FaktaOmBeregningTilfelleTjeneste(repositoryProvider,
            kontrollerFaktaBeregningTjeneste, kontrollerFaktaBeregningFrilanserTjeneste);
        when(kontrollerFaktaBeregningFrilanserTjeneste.erBrukerArbeidstakerOgFrilanserISammeOrganisasjon(behandling)).thenReturn(false);
        when(kontrollerFaktaBeregningTjeneste.brukerMedAktivitetStatusTY(behandling)).thenReturn(false);
        when(kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling)).thenReturn(false);
    }

    @Test
    public void skal_gi_kun_tilstøtende_ytelse_når_kun_tilstøtende_ytelse() {
        when(kontrollerFaktaBeregningTjeneste.brukerMedAktivitetStatusTY(behandling)).thenReturn(true);
        faktaOmBeregningTilfelleTjeneste.utledOgLagreFaktaOmBeregningTilfeller(behandling);

        Optional<BeregningsgrunnlagGrunnlagEntitet> bgEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(behandling);
        assertThat(bgEntitet.isPresent()).isTrue();
        assertThat(bgEntitet.get().getBeregningsgrunnlagTilstand()).isEqualTo(BeregningsgrunnlagTilstand.OPPRETTET);
        Beregningsgrunnlag bg = bgEntitet.get().getBeregningsgrunnlag();
        assertThat(bg.getFaktaOmBeregningTilfeller()).containsExactlyInAnyOrder(FaktaOmBeregningTilfelle.TILSTØTENDE_YTELSE);
    }

    @Test
    public void skal_gi_kun_atfl_i_samme_org_når_atfl_i_samme_org() {
        when(kontrollerFaktaBeregningFrilanserTjeneste.erBrukerArbeidstakerOgFrilanserISammeOrganisasjon(behandling)).thenReturn(true);
        faktaOmBeregningTilfelleTjeneste.utledOgLagreFaktaOmBeregningTilfeller(behandling);

        Optional<BeregningsgrunnlagGrunnlagEntitet> bgEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(behandling);
        assertThat(bgEntitet.isPresent()).isTrue();
        assertThat(bgEntitet.get().getBeregningsgrunnlagTilstand()).isEqualTo(BeregningsgrunnlagTilstand.OPPRETTET);
        Beregningsgrunnlag bg = bgEntitet.get().getBeregningsgrunnlag();
        assertThat(bg.getFaktaOmBeregningTilfeller()).containsExactlyInAnyOrder(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON);
    }

}
