package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapVilkårPeriodeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapsvilkårPeriodeGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapsvilkårPerioderEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.familiehendelse.dødsfall.OpplysningerOmDødEndringIdentifiserer;
import no.nav.foreldrepenger.domene.uttak.UttakRevurderingTestUtil;

public class AksjonspunktUtlederForAvklarHendelseTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private BehandlingRepository behandlingRepository;
    private MedlemskapVilkårPeriodeRepository medlemskapVilkårPeriodeRepository;

    private UttakRevurderingTestUtil testUtil;

    @Mock
    private OpplysningerOmDødEndringIdentifiserer opplysningerOmDødEndringIdentifiserer;

    private AksjonspunktUtlederForAvklarHendelse aksjonspunktUtlederForAvklarHendelse;

    @Before
    public void before() {
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        medlemskapVilkårPeriodeRepository = repositoryProvider.getMedlemskapVilkårPeriodeRepository();
        testUtil = new UttakRevurderingTestUtil(repoRule, repositoryProvider);
        aksjonspunktUtlederForAvklarHendelse = new AksjonspunktUtlederForAvklarHendelse(opplysningerOmDødEndringIdentifiserer, medlemskapVilkårPeriodeRepository);
    }

    @Test // #1
    public void skal_utlede_aksjonspunkt_for_klage_når_behandling_er_manuelt_opprettet_med_klageårsak() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();
        BehandlingÅrsak.Builder revurderingÅrsak = BehandlingÅrsak.builder(BehandlingÅrsakType.RE_KLAGE_UTEN_END_INNTEKT)
            .medOriginalBehandling(revurdering.getOriginalBehandling().get())
            .medManueltOpprettet(true);
        revurderingÅrsak.buildFor(revurdering);
        behandlingRepository.lagre(revurdering, behandlingRepository.taSkriveLås(revurdering));

        // Act
        List<AksjonspunktResultat> aksjonspunkter = aksjonspunktUtlederForAvklarHendelse.utledAksjonspunkterFor(revurdering);

        // Assert
        assertThat(aksjonspunkter.stream()
            .map(AksjonspunktResultat::getAksjonspunktDefinisjon)
            .collect(Collectors.toList()))
            .contains(AksjonspunktDefinisjon.KONTROLLER_REALITETSBEHANDLING_ELLER_KLAGE);
    }

    @Test // #2.1
    public void skal_utlede_aksjonspunkt_for_medlemskap_når_behandling_er_manuelt_opprettet_med_medlemskapsårsak() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();
        BehandlingÅrsak.Builder revurderingÅrsak = BehandlingÅrsak.builder(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_MEDLEMSKAP)
            .medOriginalBehandling(revurdering.getOriginalBehandling().get())
            .medManueltOpprettet(true);
        revurderingÅrsak.buildFor(revurdering);
        behandlingRepository.lagre(revurdering, behandlingRepository.taSkriveLås(revurdering));

        // Act
        List<AksjonspunktResultat> aksjonspunkter = aksjonspunktUtlederForAvklarHendelse.utledAksjonspunkterFor(revurdering);

        // Assert
        assertThat(aksjonspunkter.stream()
            .map(AksjonspunktResultat::getAksjonspunktDefinisjon)
            .collect(Collectors.toList()))
            .contains(AksjonspunktDefinisjon.KONTROLLER_OPPLYSNINGER_OM_MEDLEMSKAP);
    }

    @Test // #2.2
    public void skal_utlede_aksjonspunkt_for_medlemskap_når_grunnlaget_har_ikke_oppfylt_medlemskapsperiode() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();
        VilkårResultat.builder().buildFor(revurdering);
        MedlemskapsvilkårPeriodeGrunnlag.Builder builder = medlemskapVilkårPeriodeRepository.hentBuilderFor(revurdering);
        MedlemskapsvilkårPerioderEntitet.Builder periode = builder.getBuilderForVurderingsdato(LocalDate.now());
        periode.medVilkårUtfall(VilkårUtfallType.IKKE_OPPFYLT);
        builder.leggTilMedlemskapsvilkårPeriode(periode);

        medlemskapVilkårPeriodeRepository.lagreMedlemskapsvilkår(revurdering, builder);

        // Act
        List<AksjonspunktResultat> aksjonspunkter = aksjonspunktUtlederForAvklarHendelse.utledAksjonspunkterFor(revurdering);

        // Assert
        assertThat(aksjonspunkter.stream()
            .map(AksjonspunktResultat::getAksjonspunktDefinisjon)
            .collect(Collectors.toList()))
            .contains(AksjonspunktDefinisjon.KONTROLLER_OPPLYSNINGER_OM_MEDLEMSKAP);
    }

    @Test // #2.3
    public void skal_utlede_aksjonspunkt_for_medlemskap_når_grunnlaget_til_original_behandling_har_ikke_oppfylt_medlemskapsperiode() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();
        Behandling originalBehandling = revurdering.getOriginalBehandling().get();

        VilkårResultat.builder().buildFor(originalBehandling);
        MedlemskapsvilkårPeriodeGrunnlag.Builder builder = medlemskapVilkårPeriodeRepository.hentBuilderFor(revurdering);
        MedlemskapsvilkårPerioderEntitet.Builder periode = builder.getBuilderForVurderingsdato(LocalDate.now());
        periode.medVilkårUtfall(VilkårUtfallType.IKKE_OPPFYLT);
        builder.leggTilMedlemskapsvilkårPeriode(periode);

        medlemskapVilkårPeriodeRepository.lagreMedlemskapsvilkår(originalBehandling, builder);

        // Act
        List<AksjonspunktResultat> aksjonspunkter = aksjonspunktUtlederForAvklarHendelse.utledAksjonspunkterFor(revurdering);

        // Assert
        assertThat(aksjonspunkter.stream()
            .map(AksjonspunktResultat::getAksjonspunktDefinisjon)
            .collect(Collectors.toList()))
            .contains(AksjonspunktDefinisjon.KONTROLLER_OPPLYSNINGER_OM_MEDLEMSKAP);
    }

    @Test // #3
    public void skal_utlede_aksjonspunkt_for_fordeling_når_behandling_er_manuelt_opprettet_med_fordelingsårsak() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();
        BehandlingÅrsak.Builder revurderingÅrsak = BehandlingÅrsak.builder(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_FORDELING)
            .medOriginalBehandling(revurdering.getOriginalBehandling().get())
            .medManueltOpprettet(true);
        revurderingÅrsak.buildFor(revurdering);
        behandlingRepository.lagre(revurdering, behandlingRepository.taSkriveLås(revurdering));

        // Act
        List<AksjonspunktResultat> aksjonspunkter = aksjonspunktUtlederForAvklarHendelse.utledAksjonspunkterFor(revurdering);

        // Assert
        assertThat(aksjonspunkter.stream()
            .map(AksjonspunktResultat::getAksjonspunktDefinisjon)
            .collect(Collectors.toList()))
            .contains(AksjonspunktDefinisjon.KONTROLLER_OPPLYSNINGER_OM_FORDELING_AV_STØNADSPERIODEN);
    }

    @Test // #4.1
    public void skal_utlede_aksjonspunkt_for_død_når_behandling_er_manuelt_opprettet_med_dødsårsak() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();
        BehandlingÅrsak.Builder revurderingÅrsak = BehandlingÅrsak.builder(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_DØD)
            .medOriginalBehandling(revurdering.getOriginalBehandling().get())
            .medManueltOpprettet(true);
        revurderingÅrsak.buildFor(revurdering);
        behandlingRepository.lagre(revurdering, behandlingRepository.taSkriveLås(revurdering));

        // Act
        List<AksjonspunktResultat> aksjonspunkter = aksjonspunktUtlederForAvklarHendelse.utledAksjonspunkterFor(revurdering);

        // Assert
        assertThat(aksjonspunkter.stream()
            .map(AksjonspunktResultat::getAksjonspunktDefinisjon)
            .collect(Collectors.toList()))
            .contains(AksjonspunktDefinisjon.KONTROLLER_OPPLYSNINGER_OM_DØD);
    }

    @Test // #4.2
    public void skal_utlede_aksjonspunkt_for_død_når_grunnlaget_har_opplysninger_om_død() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();
        when(opplysningerOmDødEndringIdentifiserer.erEndret(revurdering)).thenReturn(true);

        // Act
        List<AksjonspunktResultat> aksjonspunkter = aksjonspunktUtlederForAvklarHendelse.utledAksjonspunkterFor(revurdering);

        // Assert
        assertThat(aksjonspunkter.stream()
            .map(AksjonspunktResultat::getAksjonspunktDefinisjon)
            .collect(Collectors.toList()))
            .contains(AksjonspunktDefinisjon.KONTROLLER_OPPLYSNINGER_OM_DØD);
    }

    @Test // #5
    public void skal_utlede_aksjonspunkt_for_søknadsfrist_når_behandling_er_manuelt_opprettet_med_søknadsfristårsak() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();
        BehandlingÅrsak.Builder revurderingÅrsak = BehandlingÅrsak.builder(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_SØKNAD_FRIST)
            .medOriginalBehandling(revurdering.getOriginalBehandling().get())
            .medManueltOpprettet(true);
        revurderingÅrsak.buildFor(revurdering);
        behandlingRepository.lagre(revurdering, behandlingRepository.taSkriveLås(revurdering));

        // Act
        List<AksjonspunktResultat> aksjonspunkter = aksjonspunktUtlederForAvklarHendelse.utledAksjonspunkterFor(revurdering);

        // Assert
        assertThat(aksjonspunkter.stream()
            .map(AksjonspunktResultat::getAksjonspunktDefinisjon)
            .collect(Collectors.toList()))
            .contains(AksjonspunktDefinisjon.KONTROLLER_OPPLYSNINGER_OM_SØKNADSFRIST);
    }

    @Test // #6
    public void skal_utlede_aksjonspunkt_for_ytelse_innvilget_når_behandling_har_årsak_for_det() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();
        BehandlingÅrsak.Builder revurderingÅrsak = BehandlingÅrsak.builder(BehandlingÅrsakType.RE_TILSTØTENDE_YTELSE_INNVILGET)
            .medOriginalBehandling(revurdering.getOriginalBehandling().get());
        revurderingÅrsak.buildFor(revurdering);
        behandlingRepository.lagre(revurdering, behandlingRepository.taSkriveLås(revurdering));

        // Act
        List<AksjonspunktResultat> aksjonspunkter = aksjonspunktUtlederForAvklarHendelse.utledAksjonspunkterFor(revurdering);

        // Assert
        assertThat(aksjonspunkter.stream()
            .map(AksjonspunktResultat::getAksjonspunktDefinisjon)
            .collect(Collectors.toList()))
            .contains(AksjonspunktDefinisjon.KONTROLLER_TILSTØTENDE_YTELSER_INNVILGET);
    }

    @Test // #7
    public void skal_utlede_aksjonspunkt_for_ytelse_opphørt_når_behandling_har_årsak_for_det() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();
        BehandlingÅrsak.Builder revurderingÅrsak = BehandlingÅrsak.builder(BehandlingÅrsakType.RE_TILSTØTENDE_YTELSE_OPPHØRT)
            .medOriginalBehandling(revurdering.getOriginalBehandling().get());
        revurderingÅrsak.buildFor(revurdering);
        behandlingRepository.lagre(revurdering, behandlingRepository.taSkriveLås(revurdering));

        // Act
        List<AksjonspunktResultat> aksjonspunkter = aksjonspunktUtlederForAvklarHendelse.utledAksjonspunkterFor(revurdering);

        // Assert
        assertThat(aksjonspunkter.stream()
            .map(AksjonspunktResultat::getAksjonspunktDefinisjon)
            .collect(Collectors.toList()))
            .contains(AksjonspunktDefinisjon.KONTROLLER_TILSTØTENDE_YTELSER_OPPHØRT);
    }

    @Test // Felles
    public void skal_ikke_utlede_aksjonspunkter_når_ingen_av_kriteriene_er_oppfylt() {
        // Arrange
        Behandling revurdering = testUtil.opprettRevurdering();

        // Act
        List<AksjonspunktResultat> aksjonspunkter = aksjonspunktUtlederForAvklarHendelse.utledAksjonspunkterFor(revurdering);

        // Assert
        assertThat(aksjonspunkter.stream()
            .map(AksjonspunktResultat::getAksjonspunktDefinisjon)
            .collect(Collectors.toList()))
            .isEmpty();
    }
}
