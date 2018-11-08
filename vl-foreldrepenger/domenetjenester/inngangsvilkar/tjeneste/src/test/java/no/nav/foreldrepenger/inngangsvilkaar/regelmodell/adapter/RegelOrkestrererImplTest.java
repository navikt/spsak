package no.nav.foreldrepenger.inngangsvilkaar.regelmodell.adapter;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_OPPFYLT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_VURDERT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.OPPFYLT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.inngangsvilkår.RegelResultat;
import no.nav.foreldrepenger.behandlingslager.inngangsvilkår.VilkårData;
import no.nav.foreldrepenger.inngangsvilkaar.InngangsvilkårTjeneste;
import no.nav.vedtak.exception.TekniskException;

public class RegelOrkestrererImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private RegelOrkestrererImpl orkestrerer;

    private InngangsvilkårTjeneste inngangsvilkårTjeneste;

    private Fagsak fagsak;

    @Before
    public void oppsett() {
        inngangsvilkårTjeneste = Mockito.mock(InngangsvilkårTjeneste.class);
        orkestrerer = new RegelOrkestrererImpl(inngangsvilkårTjeneste);
        fagsak = mock(Fagsak.class);
    }

    @Test
    public void skal_kalle_regeltjeneste_for_fødselsvilkåret_og_oppdatere_vilkårresultat() {
        // Arrange
        VilkårType vilkårType = VilkårType.FØDSELSVILKÅRET_MOR;
        VilkårData vilkårData = new VilkårData(vilkårType, OPPFYLT, emptyList());
        when(inngangsvilkårTjeneste.finnVilkår(vilkårType)).thenReturn((b) -> vilkårData);

        Behandling behandling = byggBehandlingMedVilkårresultat(VilkårResultatType.IKKE_FASTSATT, vilkårType);

        // Act
        RegelResultat regelResultat = orkestrerer.vurderInngangsvilkår(
                singletonList(vilkårType), behandling);

        // Assert
        assertThat(regelResultat.getVilkårResultat().getVilkårene()).hasSize(1);
        assertThat(regelResultat.getVilkårResultat().getVilkårene().iterator().next().getVilkårType())
                .isEqualTo(vilkårType);
    }

    @Test
    public void skal_kalle_regeltjeneste_for_adopsjonsvilkåret_og_oppdatere_vilkårresultat() {
        // Arrange
        VilkårType vilkårType = VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD;
        VilkårData vilkårData = new VilkårData(vilkårType, OPPFYLT, emptyList());
        when(inngangsvilkårTjeneste.finnVilkår(vilkårType)).thenReturn((b) -> vilkårData);
        Behandling behandling = byggBehandlingMedVilkårresultat(VilkårResultatType.IKKE_FASTSATT, vilkårType);

        // Act
        RegelResultat regelResultat = orkestrerer.vurderInngangsvilkår(
                singletonList(vilkårType), behandling);

        // Assert
        assertThat(regelResultat.getVilkårResultat().getVilkårene()).hasSize(1);
        assertThat(regelResultat.getVilkårResultat().getVilkårene().iterator().next().getVilkårType())
                .isEqualTo(vilkårType);
    }

    @Test
    public void skal_kalle_regeltjeneste_for_medlemskapvilkåret_og_oppdatere_vilkårresultat() {
        // Arrange
        VilkårType vilkårType = VilkårType.MEDLEMSKAPSVILKÅRET;
        VilkårData vilkårData = new VilkårData(vilkårType, OPPFYLT, emptyList());
        when(inngangsvilkårTjeneste.finnVilkår(vilkårType)).thenReturn((b) -> vilkårData);
        Behandling behandling = byggBehandlingMedVilkårresultat(VilkårResultatType.IKKE_FASTSATT, vilkårType);

        // Act
        RegelResultat regelResultat = orkestrerer.vurderInngangsvilkår(
                singletonList(vilkårType), behandling);

        // Assert
        assertThat(regelResultat.getVilkårResultat().getVilkårene()).hasSize(1);
        assertThat(regelResultat.getVilkårResultat().getVilkårene().iterator().next().getVilkårType())
                .isEqualTo(vilkårType);
    }

    @Test
    public void skal_kalle_regeltjeneste_for_søknadsfristvilkåret_og_oppdatere_vilkårresultat() {
        // Arrange
        VilkårType vilkårType = VilkårType.SØKNADSFRISTVILKÅRET;
        VilkårData vilkårData = new VilkårData(vilkårType, OPPFYLT, emptyList());
        when(inngangsvilkårTjeneste.finnVilkår(vilkårType)).thenReturn((b) -> vilkårData);
        Behandling behandling = byggBehandlingMedVilkårresultat(VilkårResultatType.IKKE_FASTSATT, vilkårType);

        // Act
        RegelResultat regelResultat = orkestrerer.vurderInngangsvilkår(
                singletonList(vilkårType), behandling);

        // Assert
        assertThat(regelResultat.getVilkårResultat().getVilkårene()).hasSize(1);
        assertThat(regelResultat.getVilkårResultat().getVilkårene().iterator().next().getVilkårType())
                .isEqualTo(vilkårType);
    }

    @Test
    public void skal_kalle_regeltjeneste_for_omsorgsvilkåret_og_oppdatere_vilkårresultat() {
        // Arrange
        VilkårType vilkårType = VilkårType.OMSORGSVILKÅRET;
        VilkårData vilkårData = new VilkårData(vilkårType, OPPFYLT, emptyList());
        when(inngangsvilkårTjeneste.finnVilkår(vilkårType)).thenReturn((b) -> vilkårData);
        Behandling behandling = byggBehandlingMedVilkårresultat(VilkårResultatType.IKKE_FASTSATT, vilkårType);

        // Act
        RegelResultat regelResultat = orkestrerer.vurderInngangsvilkår(
                singletonList(vilkårType), behandling);

        // Assert
        assertThat(regelResultat.getVilkårResultat().getVilkårene()).hasSize(1);
        assertThat(regelResultat.getVilkårResultat().getVilkårene().iterator().next().getVilkårType())
                .isEqualTo(vilkårType);
    }

    @Test
    public void skal_kalle_regeltjeneste_for_foreldreansvarsvilkår1_og_oppdatere_vilkårresultat() {
        // Arrange
        VilkårType vilkårType = VilkårType.FORELDREANSVARSVILKÅRET_2_LEDD;
        VilkårData vilkårData = new VilkårData(vilkårType, OPPFYLT, emptyList());
        when(inngangsvilkårTjeneste.finnVilkår(vilkårType)).thenReturn((b) -> vilkårData);
        Behandling behandling = byggBehandlingMedVilkårresultat(VilkårResultatType.IKKE_FASTSATT, vilkårType);

        // Act
        RegelResultat regelResultat = orkestrerer.vurderInngangsvilkår(
                singletonList(vilkårType), behandling);

        // Assert
        assertThat(regelResultat.getVilkårResultat().getVilkårene()).hasSize(1);
        assertThat(regelResultat.getVilkårResultat().getVilkårene().iterator().next().getVilkårType())
                .isEqualTo(vilkårType);
    }

    @Test
    public void skal_kalle_regeltjeneste_for_foreldreansvarsvilkår2_og_oppdatere_vilkårresultat() {
        // Arrange
        VilkårType vilkårType = VilkårType.FORELDREANSVARSVILKÅRET_4_LEDD;
        VilkårData vilkårData = new VilkårData(vilkårType, OPPFYLT, emptyList());
        when(inngangsvilkårTjeneste.finnVilkår(vilkårType)).thenReturn((b) -> vilkårData);
        Behandling behandling = byggBehandlingMedVilkårresultat(VilkårResultatType.IKKE_FASTSATT, vilkårType);

        // Act
        RegelResultat regelResultat = orkestrerer.vurderInngangsvilkår(
                singletonList(vilkårType), behandling);

        // Assert
        assertThat(regelResultat.getVilkårResultat().getVilkårene()).hasSize(1);
        assertThat(regelResultat.getVilkårResultat().getVilkårene().iterator().next().getVilkårType())
                .isEqualTo(vilkårType);
    }

    @Test
    public void skal_ikke_returnere_aksjonspunkter_fra_regelmotor_dersom_allerede_overstyrt() {
        // Arrange
        Behandling behandling = Behandling.forFørstegangssøknad(fagsak).build();
        VilkårType vilkårType = VilkårType.FØDSELSVILKÅRET_MOR;

        boolean erOverstyrt = true;
        VilkårResultat.builder()
                .leggTilVilkårResultat(vilkårType, OPPFYLT, null, null, null, false, erOverstyrt, null, null)
                .buildFor(behandling);

        VilkårData vilkårData = new VilkårData(vilkårType, OPPFYLT, singletonList(MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET));
        when(inngangsvilkårTjeneste.finnVilkår(vilkårType)).thenReturn((b) -> vilkårData);

        // Act
        RegelResultat regelResultat = orkestrerer.vurderInngangsvilkår(
                singletonList(vilkårType), behandling);

        // Assert
        assertThat(regelResultat.getAksjonspunktDefinisjoner()).hasSize(0);
    }

    @Test
    public void skal_returnere_aksjonspunkter_fra_regelmotor_dersom_allerede_manuelt_vurdert() {
        // Arrange
        Behandling behandling = Behandling.forFørstegangssøknad(fagsak).build();
        VilkårType vilkårType = VilkårType.FØDSELSVILKÅRET_MOR;

        boolean manueltVurdert = true;
        VilkårResultat.builder()
            .leggTilVilkårResultat(vilkårType, OPPFYLT, null, null, null, manueltVurdert, false, null, null)
            .buildFor(behandling);

        VilkårData vilkårData = new VilkårData(vilkårType, OPPFYLT, singletonList(MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET));
        when(inngangsvilkårTjeneste.finnVilkår(vilkårType)).thenReturn((b) -> vilkårData);

        // Act
        RegelResultat regelResultat = orkestrerer.vurderInngangsvilkår(
            singletonList(vilkårType), behandling);

        // Assert
        assertThat(regelResultat.getAksjonspunktDefinisjoner()).containsExactly(MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET);
    }

    @Test
    public void skal_bare_vurdere_vilkår_som_er_støttet_og_finnes_på_behandlingen() {
        // Arrange
        Behandling behandling = Behandling.forFørstegangssøknad(fagsak).build();

        VilkårType adopsjonsvilkårType = VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD;
        VilkårType fødselsvilkårType = VilkårType.FØDSELSVILKÅRET_MOR;
        VilkårType søknadsfristvilkårType = VilkårType.SØKNADSFRISTVILKÅRET;
        List<VilkårType> vilkårStøttetAvSteg = asList(adopsjonsvilkårType, fødselsvilkårType);

        // Legg til vilkårne som automatiske, dvs hverken manuelt vurdert eller overstyrt
        VilkårResultat.builder()
                .leggTilVilkårResultat(søknadsfristvilkårType, IKKE_VURDERT, null, null, null, false, false, null, null)
                .leggTilVilkårResultat(adopsjonsvilkårType, IKKE_VURDERT, null, null, null, false, false, null, null)
                .buildFor(behandling);

        VilkårData vilkårData = new VilkårData(adopsjonsvilkårType, OPPFYLT, emptyList());
        when(inngangsvilkårTjeneste.finnVilkår(Mockito.eq(adopsjonsvilkårType))).thenReturn((b) -> vilkårData);

        // Act
        RegelResultat regelResultat = orkestrerer.vurderInngangsvilkår(vilkårStøttetAvSteg, behandling);

        // Assert
        verify(inngangsvilkårTjeneste).finnVilkår(adopsjonsvilkårType);
        verify(inngangsvilkårTjeneste, never()).finnVilkår(fødselsvilkårType);
        verify(inngangsvilkårTjeneste, never()).finnVilkår(søknadsfristvilkårType);

        Vilkår søknadsfristvilkår = regelResultat.getVilkårResultat().getVilkårene().stream()
                .filter(vilkår -> vilkår.getVilkårType().equals(søknadsfristvilkårType))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Skal ikke kunne komme her"));
        Vilkår adopsjonsvilkår = regelResultat.getVilkårResultat().getVilkårene().stream()
                .filter(vilkår -> vilkår.getVilkårType().equals(adopsjonsvilkårType))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Skal ikke kunne komme her"));
        assertThat(søknadsfristvilkår.getGjeldendeVilkårUtfall()).isEqualTo(VilkårUtfallType.IKKE_VURDERT);
        assertThat(adopsjonsvilkår.getGjeldendeVilkårUtfall()).isEqualTo(VilkårUtfallType.OPPFYLT);
        assertThat(regelResultat.getVilkårResultat().getVilkårResultatType()).isEqualTo(VilkårResultatType.IKKE_FASTSATT);
    }

    @Test
    public void skal_sammenstille_individuelle_vilkårsutfall_til_ett_samlet_vilkårresultat() {
        // Enkelt vilkårutfall
        assertThat(orkestrerer.utledInngangsvilkårUtfall(singletonList(IKKE_OPPFYLT))).isEqualTo(VilkårResultatType.AVSLÅTT);
        assertThat(orkestrerer.utledInngangsvilkårUtfall(singletonList(IKKE_VURDERT))).isEqualTo(VilkårResultatType.IKKE_FASTSATT);
        assertThat(orkestrerer.utledInngangsvilkårUtfall(singletonList(OPPFYLT))).isEqualTo(VilkårResultatType.INNVILGET);

        // Sammensatt vilkårutfall
        assertThat(orkestrerer.utledInngangsvilkårUtfall(asList(IKKE_OPPFYLT, IKKE_VURDERT))).isEqualTo(VilkårResultatType.AVSLÅTT);
        assertThat(orkestrerer.utledInngangsvilkårUtfall(asList(IKKE_OPPFYLT, OPPFYLT))).isEqualTo(VilkårResultatType.AVSLÅTT);

        assertThat(orkestrerer.utledInngangsvilkårUtfall(asList(IKKE_VURDERT, OPPFYLT))).isEqualTo(VilkårResultatType.IKKE_FASTSATT);
    }

    @Test
    public void skal_kaste_feil_dersom_vilkårsresultat_ikke_kan_utledes() {
        expectedException.expect(TekniskException.class);

        orkestrerer.utledInngangsvilkårUtfall(emptyList());
    }

    private Behandling byggBehandlingMedVilkårresultat(VilkårResultatType vilkårResultatType, VilkårType vilkårType) {
        Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(fagsak);
        Behandling behandling = behandlingBuilder.build();
        VilkårResultat.builder().medVilkårResultatType(vilkårResultatType)
            .leggTilVilkår(vilkårType, IKKE_VURDERT).buildFor(behandling);
        return behandling;
    }
}
