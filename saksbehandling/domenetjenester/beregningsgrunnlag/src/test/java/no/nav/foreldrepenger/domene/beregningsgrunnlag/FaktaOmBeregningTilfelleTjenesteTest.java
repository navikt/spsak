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
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.FaktaOmBeregningTilfelle;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Beløp;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;

public class FaktaOmBeregningTilfelleTjenesteTest {

    private static final AktørId AKTØR_ID = new AktørId("210195");
    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final Beløp GRUNNBELØP = new Beløp(600000);

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private GrunnlagRepositoryProvider repositoryProvider = Mockito.spy(new GrunnlagRepositoryProviderImpl(entityManager));
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());

    private KontrollerFaktaBeregningFrilanserTjeneste kontrollerFaktaBeregningFrilanserTjeneste = mock(KontrollerFaktaBeregningFrilanserTjeneste.class);
    private KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste = mock(KontrollerFaktaBeregningTjeneste.class);

    private FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste;

    private Behandling behandling;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;


    @Before
    public void setup() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forAktør(AKTØR_ID);
        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
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
        beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();
        beregningsgrunnlagRepository.lagre(behandling, bg, BeregningsgrunnlagTilstand.OPPRETTET);
        faktaOmBeregningTilfelleTjeneste = new FaktaOmBeregningTilfelleTjeneste(resultatRepositoryProvider,
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
