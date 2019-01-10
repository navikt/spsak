package no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.impl.ForeslåBeregningsgrunnlagStegImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.ForeslåBeregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.wrapper.BeregningsgrunnlagRegelResultat;
import no.nav.vedtak.util.Tuple;

public class ForeslåBeregningsgrunnlagStegImplTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();
    @Mock
    private ForeslåBeregningsgrunnlag foreslåBeregningsgrunnlag;
    @Mock
    private BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat;
    @Mock
    private BehandlingskontrollKontekst kontekst;

    private GrunnlagRepositoryProvider grunnlagRepositoryProvider;
    private Behandling behandling;
    private ForeslåBeregningsgrunnlagStegImpl steg;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    @Before
    public void setUp() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        behandling = scenario.lagMocked();
        Tuple<GrunnlagRepositoryProvider, ResultatRepositoryProvider> providerTuple = scenario.mockBehandlingRepositoryProvider();
        grunnlagRepositoryProvider = providerTuple.getElement1();
        ResultatRepositoryProvider resultatRepositoryProvider = providerTuple.getElement2();
        beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(LocalDate.now()).build();
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, null);
        when(kontekst.getBehandlingId()).thenReturn(behandling.getId());
        when(foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag)).thenReturn(beregningsgrunnlagRegelResultat);
        steg = new ForeslåBeregningsgrunnlagStegImpl(resultatRepositoryProvider, foreslåBeregningsgrunnlag);
    }

    @Test
    public void stegUtførtUtenAksjonspunkter() {
        opprettVilkårResultatForBehandling(VilkårResultatType.INNVILGET);
        BehandleStegResultat resultat = steg.utførSteg(kontekst);
        assertThat(resultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(resultat.getAksjonspunktListe()).hasSize(0);
    }

    @Test
    public void stegUtførtNårRegelResultatInneholderAutopunkt() {
        opprettVilkårResultatForBehandling(VilkårResultatType.INNVILGET);
        when(beregningsgrunnlagRegelResultat.getAksjonspunkter()).thenReturn(Collections.singletonList(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS));
        BehandleStegResultat resultat = steg.utførSteg(kontekst);
        assertThat(resultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(resultat.getAksjonspunktListe()).hasSize(1);
        assertThat(resultat.getAksjonspunktListe().get(0)).isEqualTo(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS);
    }

    private void opprettVilkårResultatForBehandling(VilkårResultatType resultatType) {
        BehandlingRepository behandlingRepository = grunnlagRepositoryProvider.getBehandlingRepository();
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.opprettFor(behandling);
        VilkårResultat vilkårResultat = VilkårResultat.builder().medVilkårResultatType(resultatType)
            .buildFor(behandlingsresultat);
        behandlingsresultat.medOppdatertVilkårResultat(vilkårResultat);
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), lås);
        behandlingRepository.lagre(behandlingsresultat, lås);
    }
}
