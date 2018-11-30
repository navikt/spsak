package no.nav.foreldrepenger.behandling.steg.inngangsvilkår;


import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner.FREMHOPP_TIL_UTTAKSPLAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.TransisjonIdentifikator;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.inngangsvilkaar.RegelOrkestrerer;
import no.nav.foreldrepenger.domene.inngangsvilkaar.RegelResultat;
import no.nav.vedtak.felles.testutilities.Whitebox;

@SuppressWarnings("deprecation")
public class InngangsvilkårStegImplTest {

    private final VilkårType sutVilkårType = VilkårType.MEDLEMSKAPSVILKÅRET;

    @Mock
    private BehandlingskontrollKontekst kontekst;

    @Mock
    private BehandlingStegModell modell;

    @Mock
    private RegelOrkestrerer regelOrkestrerer;

    @Before
    public void oppsett() {
        initMocks(this);
    }

    @Test
    public void skal_hoppe_til_uttak_ved_avslag_for_foreldrepenger_ved_revurdering() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør()
            .medBehandlingType(BehandlingType.REVURDERING)
            .medVilkårResultatType(VilkårResultatType.IKKE_FASTSATT)
            .leggTilVilkår(sutVilkårType, VilkårUtfallType.IKKE_OPPFYLT);
        Behandling behandling = scenario.lagMocked();

        BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        Behandlingsresultat.builderEndreEksisterende(behandling.getBehandlingsresultat()).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
        when(kontekst.getBehandlingId()).thenReturn(behandling.getId());

        RegelResultat val = new RegelResultat(behandling.getBehandlingsresultat().getVilkårResultat(), emptyList(), emptyMap());
        when(regelOrkestrerer.vurderInngangsvilkår(singletonList(sutVilkårType), behandling)).thenReturn(val);

        // Act
        BehandleStegResultat stegResultat = new SutInngangsvilkårSteg(repositoryProvider).utførSteg(kontekst);

        // Assert
        assertThat(stegResultat.getTransisjon()).isEqualTo(TransisjonIdentifikator.forId(FREMHOPP_TIL_UTTAKSPLAN.getId()));

        VilkårResultat vilkårResultat = behandling.getBehandlingsresultat().getVilkårResultat();
        assertThat(vilkårResultat.getVilkårResultatType()).isEqualTo(VilkårResultatType.IKKE_FASTSATT);
        assertThat(vilkårResultat.getVilkårene().stream().map(Vilkår::getGjeldendeVilkårUtfall).collect(toList()))
            .containsExactly(VilkårUtfallType.IKKE_OPPFYLT);
    }

    @Test
    public void skal_ved_tilbakehopp_rydde_vilkårresultat_og_vilkår_og_behandlingsresultattype() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad
            .forDefaultAktør()
            .medVilkårResultatType(VilkårResultatType.INNVILGET)
            .leggTilVilkår(sutVilkårType, VilkårUtfallType.OPPFYLT);
        Behandling behandling = scenario.lagMocked();
        BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        Behandlingsresultat.builderEndreEksisterende(behandling.getBehandlingsresultat()).medBehandlingResultatType(BehandlingResultatType.INNVILGET);

        // Act
        new SutInngangsvilkårSteg(repositoryProvider)
            .vedTransisjon(kontekst, behandling, modell, BehandlingSteg.TransisjonType.HOPP_OVER_BAKOVER, null, null, BehandlingSteg.TransisjonType.FØR_INNGANG);

        // Assert
        VilkårResultat vilkårResultat = behandling.getBehandlingsresultat().getVilkårResultat();
        assertThat(vilkårResultat.getVilkårResultatType()).isEqualTo(VilkårResultatType.IKKE_FASTSATT);
        assertThat(vilkårResultat.getVilkårene().stream().map(Vilkår::getGjeldendeVilkårUtfall).collect(toList()))
            .containsExactly(VilkårUtfallType.IKKE_VURDERT);
        assertThat(behandling.getBehandlingsresultat().getBehandlingResultatType())
            .isEqualTo(BehandlingResultatType.IKKE_FASTSATT);
    }

    @Test
    public void skal_ved_fremoverhopp_rydde_avklarte_fakta_og_vilkår() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør()
            .medVilkårResultatType(VilkårResultatType.INNVILGET)
            .leggTilVilkår(sutVilkårType, VilkårUtfallType.OPPFYLT);

        Behandling behandling = scenario.lagMocked();
        Whitebox.setInternalState(behandling.getBehandlingsresultat().getVilkårResultat().getVilkårene().get(0),
            "vilkårUtfallOverstyrt", VilkårUtfallType.IKKE_VURDERT);
        BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        MedlemskapRepository mockMedlemskapRepository = scenario.mockMedlemskapRepository();

        // Act
        new SutInngangsvilkårSteg(repositoryProvider)
            .vedTransisjon(kontekst, behandling, modell, BehandlingSteg.TransisjonType.HOPP_OVER_FRAMOVER, null, null, BehandlingSteg.TransisjonType.FØR_INNGANG);

        // Assert
        verify(mockMedlemskapRepository).slettAvklarteMedlemskapsdata(eq(behandling), any());

        VilkårResultat vilkårResultat = behandling.getBehandlingsresultat().getVilkårResultat();
        assertThat(vilkårResultat.getVilkårResultatType()).isEqualTo(VilkårResultatType.INNVILGET);
        assertThat(vilkårResultat.getVilkårene().stream().map(Vilkår::getGjeldendeVilkårUtfall).collect(toList()))
            .containsExactly(VilkårUtfallType.IKKE_VURDERT);
    }

    // ***** Testklasser *****
    class SutInngangsvilkårSteg extends InngangsvilkårStegImpl {

        SutInngangsvilkårSteg(BehandlingRepositoryProvider repositoryProvider) {
            super(repositoryProvider, regelOrkestrerer, BehandlingStegType.VURDER_MEDLEMSKAPVILKÅR);
        }

        @Override
        public List<VilkårType> vilkårHåndtertAvSteg() {
            return singletonList(sutVilkårType);
        }
    }
}
