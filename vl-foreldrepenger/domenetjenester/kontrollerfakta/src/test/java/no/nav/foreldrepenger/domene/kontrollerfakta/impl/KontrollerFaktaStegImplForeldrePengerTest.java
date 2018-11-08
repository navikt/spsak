package no.nav.foreldrepenger.domene.kontrollerfakta.impl;

import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_ADOPSJONSDOKUMENTAJON;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.StartpunktRef;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittLandOpphold;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittLandOppholdEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoerEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighetEntitet;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonAdresse;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon.Builder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class KontrollerFaktaStegImplForeldrePengerTest {

    private static final LocalDate FØDSELSDATO_BARN = LocalDate.of(2017, Month.JANUARY, 1);
    private static final AktørId AKTØR_ID_MOR = new AktørId("3");
    private static final AktørId AKTØR_ID_FAR = new AktørId("4");
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private Behandling behandling;
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    @Inject
    @FagsakYtelseTypeRef("FP")
    @BehandlingTypeRef
    @StartpunktRef
    private KontrollerFaktaTjeneste tjeneste;
    public static final AktørId AKTØR_ID = new AktørId("1");

    private ScenarioFarSøkerEngangsstønad byggBehandlingMedFarSøkerType(FarSøkerType farSøkerType) {
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad
            .forAdopsjon();
        scenario.medBruker(AKTØR_ID, NavBrukerKjønn.MANN);
        scenario.medSøknad()
            .medFarSøkerType(farSøkerType);
        scenario.medSøknadHendelse()
            .medFødselsDato(FØDSELSDATO_BARN);

        PersonInformasjon søker = scenario.opprettBuilderForRegisteropplysninger()
            .medPersonas()
            .mann(AKTØR_ID, SivilstandType.UOPPGITT, Region.NORDEN)
            .statsborgerskap(Landkoder.NOR)
            .build();

        scenario.medRegisterOpplysninger(søker);

        return scenario;
    }

    @Before
    public void oppsett() {
        ScenarioFarSøkerEngangsstønad scenario = byggBehandlingMedFarSøkerType(FarSøkerType.ADOPTERER_ALENE);
        scenario.medSøknadHendelse().medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder().medOmsorgsovertakelseDato(LocalDate.now()));
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
            .containsExactlyInAnyOrder(AVKLAR_ADOPSJONSDOKUMENTAJON,
                AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN);
    }

    @Test
    public void skal_utledede_aksjonspunkt_basert_på_fakta_om_foreldrepenger_til_mor() {
        // oppsett
        byggBehandingMedMorSøkerTypeOgHarAleneOmsorg();
        // Act
        BehandlingskontrollKontekst kontekst = lagKontekst();

        List<AksjonspunktResultat> resultat = tjeneste.utledAksjonspunkter(kontekst.getBehandlingId());

        // Assert
        assertThat(resultat.stream()
            .map(AksjonspunktResultat::getAksjonspunktDefinisjon)
            .collect(toList()))
            .containsExactlyInAnyOrder(AVKLAR_TERMINBEKREFTELSE);

    }

    private void byggBehandingMedMorSøkerTypeOgHarAleneOmsorg() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(AKTØR_ID_MOR);
        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(false, true, true);
        scenario.medAvklarteUttakDatoer(new AvklarteUttakDatoerEntitet(LocalDate.now(), null));

        Builder builderForRegisteropplysninger = scenario.opprettBuilderForRegisteropplysninger();

        no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonAdresse.Builder bostedsadresse = PersonAdresse.builder().adresselinje1("Portveien 2").postnummer("7000").land(Landkoder.NOR);

        PersonInformasjon annenPrt = builderForRegisteropplysninger
            .medPersonas()
            .mann(AKTØR_ID_FAR, SivilstandType.GIFT)
            .bostedsadresse(bostedsadresse)
            .relasjonTil(AKTØR_ID_MOR, RelasjonsRolleType.EKTE, true)
            .build();
        scenario.medRegisterOpplysninger(annenPrt);

        PersonInformasjon søker = builderForRegisteropplysninger
            .medPersonas()
            .kvinne(AKTØR_ID_MOR, SivilstandType.GIFT, Region.NORDEN)
            .bostedsadresse(bostedsadresse)
            .statsborgerskap(Landkoder.NOR)
            .relasjonTil(AKTØR_ID_FAR, RelasjonsRolleType.EKTE, true)
            .build();

        OppgittLandOpphold oppholdNorgeNestePeriode = new OppgittLandOppholdEntitet.Builder()
            .erTidligereOpphold(false)
            .medLand(Landkoder.NOR)
            .medPeriode(LocalDate.now(), LocalDate.now().plusYears(1))
            .build();

        scenario.medRegisterOpplysninger(søker);
        scenario.medOppgittTilknytning()
            .medOpphold(Arrays.asList(oppholdNorgeNestePeriode))
            .medOppholdNå(true);

        scenario.medSøknadAnnenPart()
            .medAktørId(AKTØR_ID_FAR);

        scenario.medSøknad().medMottattDato(LocalDate.now());
        scenario.medOppgittRettighet(rettighet);
        final FamilieHendelseBuilder hendelseBuilder = scenario.medSøknadHendelse();
        hendelseBuilder.medTerminbekreftelse(hendelseBuilder.getTerminbekreftelseBuilder()
            .medNavnPå("asdf")
            .medUtstedtDato(LocalDate.now())
            .medTermindato(LocalDate.now().plusDays(35)));

        behandling = scenario.lagre(repositoryProvider);
    }

    private BehandlingskontrollKontekst lagKontekst() {
        Fagsak fagsak = behandling.getFagsak();
        return new BehandlingskontrollKontekst(fagsak.getId(),fagsak.getAktørId(), behandlingRepository.taSkriveLås(behandling));
    }


}
