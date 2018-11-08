package no.nav.foreldrepenger.domene.kontrollerfakta.impl;

import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_ADOPSJONSDOKUMENTAJON;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_OM_SØKER_ER_MANN_SOM_ADOPTERER_ALENE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.OmsorgsovertakelseVilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon.Builder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class KontrollerFaktaStegImplTest {

    private static final LocalDate FØDSELSDATO_BARN = LocalDate.of(2017, Month.JANUARY, 1);
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private Behandling behandling;
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    @Inject
    @FagsakYtelseTypeRef("ES")
    private KontrollerFaktaTjeneste tjeneste;
    public static final AktørId AKTØR_ID = new AktørId("1");


    private ScenarioFarSøkerEngangsstønad byggBehandlingMedFarSøkerType(FarSøkerType farSøkerType) {
        NavBrukerKjønn kjønn = NavBrukerKjønn.MANN;
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad
            .forAdopsjon();
        scenario.medBruker(AKTØR_ID, kjønn);
        scenario.medSøknad()
            .medFarSøkerType(farSøkerType);
        scenario.medSøknadHendelse()
            .medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder()
                .medOmsorgsovertakelseDato(LocalDate.now())
                .medAdoptererAlene(farSøkerType.equals(FarSøkerType.ADOPTERER_ALENE))
                .medOmsorgovertalseVilkårType(!farSøkerType.equals(FarSøkerType.ADOPTERER_ALENE) ? OmsorgsovertakelseVilkårType.OMSORGSVILKÅRET : OmsorgsovertakelseVilkårType.UDEFINERT))
            .medFødselsDato(FØDSELSDATO_BARN);

        // Søker må være lagret i BekreftetForeldre
        leggTilSøker(scenario, kjønn);

        return scenario;
    }

    @Before
    public void oppsett() {
        ScenarioFarSøkerEngangsstønad scenario = byggBehandlingMedFarSøkerType(FarSøkerType.ADOPTERER_ALENE);
        scenario.medBruker(AKTØR_ID, NavBrukerKjønn.MANN);
        behandling = scenario.lagre(repositoryProvider);
    }

    @Test
    public void skal_utledede_aksjonspunkt_basert_på_fakta_om_engangsstønad_til_far() {
        // Act
        BehandlingskontrollKontekst kontekst = lagKontekst();

        List<AksjonspunktResultat> resultat = tjeneste.utledAksjonspunkter(kontekst.getBehandlingId());

        // Assert
        assertThat(resultat.stream()
            .map(AksjonspunktResultat::getAksjonspunktDefinisjon)
            .collect(toList()))
            .containsExactlyInAnyOrder(
                AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN,
                AVKLAR_ADOPSJONSDOKUMENTAJON,
                AVKLAR_OM_SØKER_ER_MANN_SOM_ADOPTERER_ALENE);
    }

    private BehandlingskontrollKontekst lagKontekst() {
        Fagsak fagsak = behandling.getFagsak();
        return new BehandlingskontrollKontekst(fagsak.getId(),fagsak.getAktørId(), behandlingRepository.taSkriveLås(behandling));
    }

    private void leggTilSøker(AbstractTestScenario<?> scenario, NavBrukerKjønn kjønn) {
        Builder builderForRegisteropplysninger = scenario.opprettBuilderForRegisteropplysninger();
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();
        PersonInformasjon søker = builderForRegisteropplysninger
            .medPersonas()
            .voksenPerson(søkerAktørId, SivilstandType.UOPPGITT, kjønn, Region.NORDEN)
            .statsborgerskap(Landkoder.NOR)
            .build();
        scenario.medRegisterOpplysninger(søker);
    }

}
