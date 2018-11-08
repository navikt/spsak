package no.nav.foreldrepenger.domene.kontrollerfakta.impl.es;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.OmsorgsovertakelseVilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon.Builder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaTjeneste;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaUtledereTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class KontrollerFaktaTjenesteEngangsstønadTest {
    private static final LocalDate FØDSELSDATO_BARN = LocalDate.of(2017, Month.JANUARY, 1);

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    @Inject
    @FagsakYtelseTypeRef("ES")
    private KontrollerFaktaTjeneste faktaTjenesteEngangsstønad;

    @Inject
    @FagsakYtelseTypeRef("ES")
    private KontrollerFaktaUtledereTjeneste aksjonspunktUtlederTjeneste;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    private ScenarioFarSøkerEngangsstønad byggBehandlingMedFarSøkerType(FarSøkerType farSøkerType, ScenarioFarSøkerEngangsstønad scenario) {
        AktørId aktørId = new AktørId("1");
        scenario.medBruker(aktørId, NavBrukerKjønn.MANN);
        scenario.medSøknad()
            .medFarSøkerType(farSøkerType);

        // Søker må være lagret i BekreftetForeldre
        leggTilSøker(scenario, NavBrukerKjønn.MANN);

        return scenario;
    }

    @Test
    public void skal_få_med_alle_aksjonspunkt_utledere_for_adopsjon() {
        final ScenarioFarSøkerEngangsstønad scenarioFarSøkerEngangsstønad = byggBehandlingMedFarSøkerType(FarSøkerType.UDEFINERT, ScenarioFarSøkerEngangsstønad
            .forAdopsjon());
        scenarioFarSøkerEngangsstønad.medSøknadHendelse()
            .medAdopsjon(scenarioFarSøkerEngangsstønad.medSøknadHendelse().getAdopsjonBuilder().medOmsorgsovertakelseDato(LocalDate.now()));
        final Behandling behandling = scenarioFarSøkerEngangsstønad.lagre(repositoryProvider);

        final List<AksjonspunktUtleder> aksjonspunktUtledere = aksjonspunktUtlederTjeneste.utledUtledereFor(behandling);
        List<String> utledere = utledUtledereKlasserFra(aksjonspunktUtledere);

        assertThat(utledere).containsExactlyInAnyOrder("AksjonspunktUtlederForEngangsstønadAdopsjon",
            "AksjonspunktUtlederForTilleggsopplysninger",
            "AksjonspunktUtlederForTidligereMottattEngangsstønad",
            "AksjonspunktutlederForMedlemskapSkjæringstidspunkt");
    }

    @Test
    public void skal_få_med_alle_aksjonspunkt_utledere_for_fødsel() {
        final ScenarioFarSøkerEngangsstønad scenarioFarSøkerEngangsstønad = byggBehandlingMedFarSøkerType(FarSøkerType.UDEFINERT, ScenarioFarSøkerEngangsstønad
            .forFødsel());
        scenarioFarSøkerEngangsstønad.medSøknadHendelse().medFødselsDato(FØDSELSDATO_BARN);
        final Behandling behandling = scenarioFarSøkerEngangsstønad.lagre(repositoryProvider);

        final List<AksjonspunktUtleder> aksjonspunktUtledere = aksjonspunktUtlederTjeneste.utledUtledereFor(behandling);
        List<String> utledere = utledUtledereKlasserFra(aksjonspunktUtledere);

        assertThat(utledere).containsExactlyInAnyOrder("AksjonspunktUtlederForEngangsstønadFødsel",
            "AksjonspunktUtlederForTilleggsopplysninger",
            "AksjonspunktUtlederForTidligereMottattEngangsstønad",
            "AksjonspunktutlederForMedlemskapSkjæringstidspunkt");
    }

    @Test
    public void skal_få_med_alle_aksjonspunkt_utledere_for_omsorg() {
        final ScenarioFarSøkerEngangsstønad scenarioFarSøkerEngangsstønad = byggBehandlingMedFarSøkerType(FarSøkerType.OVERTATT_OMSORG, ScenarioFarSøkerEngangsstønad
            .forFødsel());
        scenarioFarSøkerEngangsstønad.medSøknadHendelse().medAdopsjon(scenarioFarSøkerEngangsstønad.medSøknadHendelse().getAdopsjonBuilder()
            .medOmsorgovertalseVilkårType(OmsorgsovertakelseVilkårType.OMSORGSVILKÅRET))
            .medFødselsDato(FØDSELSDATO_BARN);
        final Behandling behandling = scenarioFarSøkerEngangsstønad.lagre(repositoryProvider);

        final List<AksjonspunktUtleder> aksjonspunktUtledere = aksjonspunktUtlederTjeneste.utledUtledereFor(behandling);
        List<String> utledere = utledUtledereKlasserFra(aksjonspunktUtledere);

        assertThat(utledere).containsExactlyInAnyOrder("AksjonspunktUtlederForOmsorgsovertakelse",
            "AksjonspunktUtlederForTilleggsopplysninger",
            "AksjonspunktUtlederForTidligereMottattEngangsstønad",
            "AksjonspunktutlederForMedlemskapSkjæringstidspunkt");
    }

    private List<String> utledUtledereKlasserFra(List<AksjonspunktUtleder> aksjonspunktUtledere) {
        List<String> utledere = new ArrayList<>();
        for (AksjonspunktUtleder aksjonspunktUtleder : aksjonspunktUtledere) {
            String className = aksjonspunktUtleder.getClass().getSimpleName();
            if (className.contains("WeldClientProxy")) {
                className = aksjonspunktUtleder.getClass().getSuperclass().getSimpleName();
            }
            utledere.add(className);
        }
        return utledere;
    }

    private void leggTilSøker(AbstractTestScenario<?> scenario, NavBrukerKjønn kjønn) {
        Builder builderForRegisteropplysninger = scenario.opprettBuilderForRegisteropplysninger();
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();
        PersonInformasjon søker = builderForRegisteropplysninger
            .medPersonas()
            .voksenPerson(søkerAktørId, SivilstandType.UOPPGITT, kjønn, Region.UDEFINERT)
            .build();
        scenario.medRegisterOpplysninger(søker);
    }
}
