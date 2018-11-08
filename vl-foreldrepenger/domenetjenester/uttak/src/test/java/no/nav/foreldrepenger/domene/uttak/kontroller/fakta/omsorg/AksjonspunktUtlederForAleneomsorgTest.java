package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.omsorg;

import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_KONTROLL_AV_OM_BRUKER_HAR_ALENEOMSORG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoerEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.personopplysning.impl.PersonopplysningTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;

public class AksjonspunktUtlederForAleneomsorgTest {

    private static final AktørId AKTØR_ID_MOR = new AktørId("3");
    private static final AktørId AKTØR_ID_FAR = new AktørId("4");
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(AKTØR_ID_MOR);
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());

    private AksjonspunktUtlederForAleneomsorg aksjonspunktUtleder;

    @Before
    public void oppsett() {
        BehandlingRepositoryProvider behandlingRepositoryMock = spy(repositoryProvider);
        final PersonopplysningTjenesteImpl personopplysningTjeneste = new PersonopplysningTjenesteImpl(repositoryProvider, null,
            new NavBrukerRepositoryImpl(repositoryRule.getEntityManager()), new SkjæringstidspunktTjenesteImpl(repositoryProvider,
            new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
            new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0), Period.of(0, 6, 0), Period.of(1, 0, 0)),
            Period.of(0, 3, 0),
            Period.of(0, 10, 0)));
        aksjonspunktUtleder = Mockito.spy(new AksjonspunktUtlederForAleneomsorg(behandlingRepositoryMock, personopplysningTjeneste));

        // default scenario
        scenario.medSøknadHendelse().medAntallBarn(1).medFødselsDato(LocalDate.now());
        scenario.medAvklarteUttakDatoer(new AvklarteUttakDatoerEntitet(LocalDate.now(), null));
        scenario.medSøknad();

    }

    @Test
    public void ingen_aksjonspunkter_dersom_søker_oppgitt_ikke_ha_aleneomsorg() {

        prepScenarioMedSøkerOgEktefelle(RelasjonsRolleType.UDEFINERT, null);
        prepScenarioMedSøknadHendelseOgRettighet(true, false);

        Behandling behandling = scenario.lagre(repositoryProvider);
        List<AksjonspunktResultat> aksjonspunktResultater = aksjonspunktUtleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater.isEmpty()).isTrue();
    }

    @Test
    public void aksjonspunkter_dersom_søker_oppgitt_ha_aleneomsorg_men_oppgitt_annenForeldre_og_ha_samme_address_som_bruker_i_tps() {

        prepScenarioMedSøkerOgEktefelle(RelasjonsRolleType.UDEFINERT, true);
        prepScenarioMedSøknadHendelseOgRettighet(false, true);
        scenario.medSøknadAnnenPart().medAktørId(AKTØR_ID_FAR);

        Behandling behandling = scenario.lagre(repositoryProvider);
        List<AksjonspunktResultat> aksjonspunktResultater = aksjonspunktUtleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater).hasSize(1);
        assertThat(aksjonspunktResultater.get(0).getAksjonspunktDefinisjon()).isEqualTo(MANUELL_KONTROLL_AV_OM_BRUKER_HAR_ALENEOMSORG);
    }

    @Test
    public void aksjonspunkter_dersom_bruker_ikke_oppgitt_annenForeldre_men_er_gift_og_ha_samme_address_som_bruker_i_tps() {

        prepScenarioMedSøkerOgEktefelle(RelasjonsRolleType.EKTE, true);
        prepScenarioMedSøknadHendelseOgRettighet(false, true);

        Behandling behandling = scenario.lagre(repositoryProvider);
        List<AksjonspunktResultat> aksjonspunktResultater = aksjonspunktUtleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater).hasSize(1);
        assertThat(aksjonspunktResultater.get(0).getAksjonspunktDefinisjon()).isEqualTo(MANUELL_KONTROLL_AV_OM_BRUKER_HAR_ALENEOMSORG);
    }

    private void prepScenarioMedSøkerOgEktefelle(RelasjonsRolleType rolle, Boolean sammeBosted) {
        PersonInformasjon.Builder builderForRegisteropplysninger = scenario.opprettBuilderForRegisteropplysninger();

        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();
        AktørId partnerAktørId = AKTØR_ID_FAR;
        PersonInformasjon gift = builderForRegisteropplysninger
            .medPersonas()
            .mann(partnerAktørId, SivilstandType.GIFT)
            .relasjonTil(søkerAktørId, rolle, sammeBosted)
            .build();
        scenario.medRegisterOpplysninger(gift);

        PersonInformasjon søker = builderForRegisteropplysninger
            .medPersonas()
            .kvinne(søkerAktørId, SivilstandType.GIFT)
            .relasjonTil(partnerAktørId, rolle, sammeBosted)
            .build();

        scenario.medRegisterOpplysninger(søker);
    }

    private void prepScenarioMedSøknadHendelseOgRettighet(Boolean harAnnenForeldreRett, Boolean harAleneomsorgForBarnet) {
        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(harAnnenForeldreRett, true, harAleneomsorgForBarnet);
        scenario.medOppgittRettighet(rettighet);
    }

}
