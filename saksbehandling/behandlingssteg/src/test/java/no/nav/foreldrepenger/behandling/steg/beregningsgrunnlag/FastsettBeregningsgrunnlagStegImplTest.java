package no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.impl.FastsettBeregningsgrunnlagStegImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FullføreBeregningsgrunnlag;
import no.nav.vedtak.util.Tuple;

public class FastsettBeregningsgrunnlagStegImplTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();
    @Mock
    private FullføreBeregningsgrunnlag fullføreBeregningsgrunnlag;
    @Mock
    private BehandlingskontrollKontekst kontekst;

    private GrunnlagRepositoryProvider grunnlagRepositoryProvider;
    private Behandling behandling;
    private FastsettBeregningsgrunnlagStegImpl steg;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private ResultatRepositoryProvider resultatRepositoryProvider;

    @Before
    public void setUp() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        behandling = scenario.lagMocked();
        Tuple<GrunnlagRepositoryProvider, ResultatRepositoryProvider> providerTuple = scenario.mockBehandlingRepositoryProvider();
        this.grunnlagRepositoryProvider = providerTuple.getElement1();
        resultatRepositoryProvider = providerTuple.getElement2();
        beregningsgrunnlagRepository = this.resultatRepositoryProvider.getBeregningsgrunnlagRepository();
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(LocalDate.now()).build();
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, null);
        when(kontekst.getBehandlingId()).thenReturn(behandling.getId());
        steg = new FastsettBeregningsgrunnlagStegImpl(this.grunnlagRepositoryProvider, resultatRepositoryProvider, fullføreBeregningsgrunnlag);
    }

    @Test
    public void stegUtførtUtenAksjonspunkter() {
        opprettVilkårResultatForBehandling(VilkårResultatType.INNVILGET);
        BehandleStegResultat resultat = steg.utførSteg(kontekst);
        assertThat(resultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(resultat.getAksjonspunktListe()).hasSize(0);
    }

    @Test
    public void stegFremoppTilForeslåVedtak() {
        // alle VilkårResultatType kan brukes for testen untatt INNVILGET
        // som brukes i stegUtførtUtenAksjonspunkter testen
        opprettVilkårResultatForBehandling(VilkårResultatType.AVSLÅTT);
        BehandleStegResultat resultat = steg.utførSteg(kontekst);
        assertThat(resultat.getTransisjon()).isEqualTo(FellesTransisjoner.FREMHOPP_TIL_FORESLÅ_VEDTAK);
        assertThat(resultat.getAksjonspunktListe()).hasSize(0);
    }

    private void opprettVilkårResultatForBehandling(VilkårResultatType resultatType) {
        VilkårResultat vilkårResultat = VilkårResultat.builder().medVilkårResultatType(resultatType)
            .buildFor(behandling);
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.opprettFor(behandling);
        behandlingsresultat.medOppdatertVilkårResultat(vilkårResultat);
    }
}
