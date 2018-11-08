package no.nav.foreldrepenger.behandling.steg.uttak.impl;

import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_KONTROLL_AV_OM_BRUKER_HAR_ALENEOMSORG;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonAdresse;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaUttakTjeneste;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;


@RunWith(CdiRunner.class)
public class KontrollerfaktaUttakStegImplTest {

    private static AktørId FAR_AKTØR_ID = new AktørId("22");

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private Behandling behandling;
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    @Inject
    @FagsakYtelseTypeRef("FP")
    @BehandlingTypeRef
    private KontrollerFaktaUttakStegImpl steg;

    @Inject
    @FagsakYtelseTypeRef("FP")
    private KontrollerFaktaUttakTjeneste tjeneste;

    private static ScenarioFarSøkerForeldrepenger opprettBehandlingForFarSomSøker() {
        ScenarioFarSøkerForeldrepenger scenario = ScenarioFarSøkerForeldrepenger.forFødselMedGittAktørId(FAR_AKTØR_ID);
        scenario.medSøknadHendelse().medFødselsDato(LocalDate.now());

        PersonInformasjon personInformasjon = scenario
            .opprettBuilderForRegisteropplysninger()
            .medPersonas()
            .mann(FAR_AKTØR_ID, SivilstandType.SAMBOER).statsborgerskap(Landkoder.NOR)
            .build();

        scenario.medRegisterOpplysninger(personInformasjon);

        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(true, false, false);
        scenario.medOppgittRettighet(rettighet);
        LocalDate now = LocalDate.now();
        scenario.medFordeling(new OppgittFordelingEntitet(Collections.singletonList(OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FEDREKVOTE)
            .medPeriode(now.plusWeeks(8), now.plusWeeks(12))
            .build()), true));
        return scenario;
    }

    @Before
    public void oppsett() {
        ScenarioFarSøkerForeldrepenger scenario = opprettBehandlingForFarSomSøker();
        behandling = scenario.lagre(repositoryProvider);
        steg = new KontrollerFaktaUttakStegImpl(repositoryProvider, tjeneste);
    }

    @Test
    public void utførerMedAksjonspunktFaktaForOmsorg() {
        Fagsak fagsak = behandling.getFagsak();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(),fagsak.getAktørId(), lås);

        BehandleStegResultat behandleStegResultat = steg.utførSteg(kontekst);
        assertThat(behandleStegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        List<AksjonspunktDefinisjon> aksjonspunkter = behandleStegResultat.getAksjonspunktListe();
        assertThat(aksjonspunkter.size()).isEqualTo(1);
        assertThat(aksjonspunkter.get(0)).isEqualTo(AksjonspunktDefinisjon.MANUELL_KONTROLL_AV_OM_BRUKER_HAR_OMSORG);

    }

    @Test
    public void skal_utledede_aksjonspunkt_basert_på_fakta_om_foreldrepenger_til_mor() {
        // oppsett
        byggBehandingMedMorSøkerTypeOgHarAleneOmsorg();

        List<AksjonspunktResultat> resultat = tjeneste.utledAksjonspunkter(behandling);

        // Assert
        assertThat(resultat.stream()
            .map(AksjonspunktResultat::getAksjonspunktDefinisjon)
            .collect(toList()))
            .containsExactlyInAnyOrder(MANUELL_KONTROLL_AV_OM_BRUKER_HAR_ALENEOMSORG);

    }

    private Behandling byggBehandingMedMorSøkerTypeOgHarAleneOmsorg() {
        AktørId AKTØR_ID_MOR = new AktørId("3");
        AktørId AKTØR_ID_FAR = new AktørId("4");

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(AKTØR_ID_MOR);
        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(false, true, true);

        PersonInformasjon.Builder builderForRegisteropplysninger = scenario.opprettBuilderForRegisteropplysninger();

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

        scenario.medRegisterOpplysninger(søker);

        scenario.medSøknadAnnenPart()
            .medAktørId(AKTØR_ID_FAR);

        scenario.medSøknad();
        scenario.medOppgittRettighet(rettighet);
        final FamilieHendelseBuilder hendelseBuilder = scenario.medSøknadHendelse();
        LocalDate termindato = LocalDate.now().plusDays(35);
        hendelseBuilder.medTerminbekreftelse(hendelseBuilder.getTerminbekreftelseBuilder()
            .medNavnPå("asdf")
            .medUtstedtDato(LocalDate.now())
            .medTermindato(termindato));

        scenario.medFordeling(new OppgittFordelingEntitet(Collections.singletonList(
            OppgittPeriodeBuilder.ny()
                .medPeriode(termindato, termindato.plusWeeks(6))
                .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
                .build()), true));

        behandling = scenario.lagre(repositoryProvider);
        return behandling;
    }
}
