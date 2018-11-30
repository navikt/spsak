

package no.nav.foreldrepenger.domene.medlem.impl;

import static no.nav.foreldrepenger.domene.medlem.impl.MedlemResultat.AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapDekningType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapPerioderBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.medlem.api.MedlemskapPerioderTjeneste;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class AvklarGyldigPeriodeTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider provider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    @Inject
    private MedlemskapPerioderTjeneste medlemskapPerioderTjeneste;

    private AvklarGyldigPeriode avklarGyldigPeriode;

    @Before
    public void setUp() {
        this.avklarGyldigPeriode = new AvklarGyldigPeriode(provider, medlemskapPerioderTjeneste);
    }

    @Test
    public void skal_ikke_opprette_Aksjonspunkt_ved_gyldig_periode() {
        // Arrange
        LocalDate fødselsdato = LocalDate.now();
        RegistrertMedlemskapPerioder gyldigPeriodeUnderFødsel = new MedlemskapPerioderBuilder()
            .medDekningType(MedlemskapDekningType.FTL_2_7_a) // hjemlet i bokstav a
            .medMedlemskapType(MedlemskapType.ENDELIG) // gyldig
            .medPeriode(fødselsdato, fødselsdato)
            .build();
        Set<RegistrertMedlemskapPerioder> medlemskapPerioder = new HashSet<>();
        medlemskapPerioder.add(gyldigPeriodeUnderFødsel);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        medlemskapPerioder.forEach(scenario::leggTilMedlemskapPeriode);
        Behandling behandling = scenario.lagre(provider);

        // Act
        Optional<MedlemResultat> medlemResultat = avklarGyldigPeriode.utled(behandling, fødselsdato);

        // Assert
        assertThat(medlemResultat).isEmpty();
    }

    @Test
    public void skalIkkeOppretteAksjonspunktVedIngenTreffMedl() {
        // Arrange
        LocalDate fødselsdato = LocalDate.now();
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        Behandling behandling = scenario.lagre(provider);

        // Act
        Optional<MedlemResultat> medlemResultat = avklarGyldigPeriode.utled(behandling, fødselsdato);

        // Assert
        assertThat(medlemResultat).isEmpty();
    }

    @Test
    public void skalIkkeOppretteAksjonspunktVedIngenUavklartPeriode() {
        // Arrange
        LocalDate fødselsdato = LocalDate.now();
        RegistrertMedlemskapPerioder lukketPeriodeFørFødselsdato = new MedlemskapPerioderBuilder()
            .medDekningType(MedlemskapDekningType.FTL_2_7_b) // ikke hjemlet i bokstav a eller c
            .medMedlemskapType(MedlemskapType.ENDELIG)
            .medPeriode(fødselsdato, fødselsdato)
            .build();
        Set<RegistrertMedlemskapPerioder> medlemskapPerioder = new HashSet<>();
        medlemskapPerioder.add(lukketPeriodeFørFødselsdato);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        medlemskapPerioder.forEach(scenario::leggTilMedlemskapPeriode);
        Behandling behandling = scenario.lagre(provider);

        // Act
        Optional<MedlemResultat> medlemResultat = avklarGyldigPeriode.utled(behandling, fødselsdato);

        // Assert
        assertThat(medlemResultat).isEmpty();
    }

    @Test
    public void skalOppretteAksjonspunktVedUavklartPeriode() {
        // Arrange
        LocalDate fødselsdato = LocalDate.now();
        RegistrertMedlemskapPerioder medlemskapPeriodeUnderAvklaring = new MedlemskapPerioderBuilder()
            .medDekningType(MedlemskapDekningType.FTL_2_7_a) // hjemlet i bokstav a
            .medMedlemskapType(MedlemskapType.UNDER_AVKLARING)
            .medPeriode(fødselsdato, fødselsdato)
            .build();
        Set<RegistrertMedlemskapPerioder> medlemskapPerioder = new HashSet<>();
        medlemskapPerioder.add(medlemskapPeriodeUnderAvklaring);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        medlemskapPerioder.forEach(scenario::leggTilMedlemskapPeriode);
        Behandling behandling = scenario.lagre(provider);

        // Act
        Optional<MedlemResultat> medlemResultat = avklarGyldigPeriode.utled(behandling, fødselsdato);

        // Assert
        assertThat(medlemResultat).contains(AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE);
    }

    @Test
    public void skalOppretteAksjonspunktVedÅpenPeriode() {
        // Arrange
        LocalDate fødselsdato = LocalDate.now();
        RegistrertMedlemskapPerioder åpenPeriode = new MedlemskapPerioderBuilder()
            .medDekningType(MedlemskapDekningType.FTL_2_7_a) // hjemlet i bokstav a
            .medMedlemskapType(MedlemskapType.FORELOPIG)
            .medPeriode(fødselsdato, null) // åpen periode
            .build();
        Set<RegistrertMedlemskapPerioder> medlemskapPerioder = new HashSet<>();
        medlemskapPerioder.add(åpenPeriode);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        medlemskapPerioder.forEach(scenario::leggTilMedlemskapPeriode);
        Behandling behandling = scenario.lagre(provider);

        // Act
        Optional<MedlemResultat> medlemResultat = avklarGyldigPeriode.utled(behandling, fødselsdato);

        // Assert
        assertThat(medlemResultat).contains(AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE);
    }
}
