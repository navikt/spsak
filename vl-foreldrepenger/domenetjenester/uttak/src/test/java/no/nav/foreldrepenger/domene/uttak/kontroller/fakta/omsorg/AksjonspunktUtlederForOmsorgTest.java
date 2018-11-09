package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.omsorg;

import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_KONTROLL_AV_OM_BRUKER_HAR_OMSORG;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoerEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon.Builder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.person.TpsAdapter;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.impl.PersonopplysningTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;

public class AksjonspunktUtlederForOmsorgTest {

    private static final LocalDate TERMINDATO = LocalDate.now().plusMonths(3);
    private static final LocalDate FØDSELSDATO_NÅ = LocalDate.now();
    private static final int ANTALL_UKER_FORBEHOLDT_MOR_ETTER_FØDSEL = 6;
    private static AktørId GITT_MOR_AKTØR_ID = new AktørId("44");
    private static AktørId GITT_BARN_ID = new AktørId("123");
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final PersonopplysningTjeneste personopplysningTjeneste = new PersonopplysningTjenesteImpl(repositoryProvider,
        mock(TpsAdapter.class), mock(NavBrukerRepository.class), new SkjæringstidspunktTjenesteImpl(repositoryProvider,
        new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
        new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0)),
        Period.of(0, 3, 0),
        Period.of(0, 10, 0)));
    private AksjonspunktUtlederForOmsorg aksjonspunktUtleder;

    @Before
    public void oppsett() {
        BehandlingRepositoryProvider behandlingRepositoryMock = Mockito.spy(repositoryProvider);

        aksjonspunktUtleder = Mockito
            .spy(new AksjonspunktUtlederForOmsorg(behandlingRepositoryMock, personopplysningTjeneste, ANTALL_UKER_FORBEHOLDT_MOR_ETTER_FØDSEL));
    }

    @Test
    public void ingen_aksjonspunkt_desrom_bruker_oppgitt_omsorg_til_barnet_men_barnet_er_ikke_født() {
        // Arrange
        Behandling behandling = opprettBehandlingMedOppgittTermin(TERMINDATO);
        // Act
        List<AksjonspunktResultat> aksjonspunktResultater = aksjonspunktUtleder.utledAksjonspunkterFor(behandling);

        // Assert
        Assertions.assertThat(aksjonspunktResultater.isEmpty()).isTrue();
    }

    @Test
    public void aksjonspunkt_dersom_mor_søker_og_oppgitt_omsorg_til_barnet_og_fødsel_registrert_i_TPS_men_barn_har_ikke_sammebosted() {
        // Arrange
        Behandling behandling = opprettBehandlingForFødselRegistrertITps(FØDSELSDATO_NÅ, 1);

        // Act
        List<AksjonspunktResultat> utledeteAksjonspunkter = aksjonspunktUtleder.utledAksjonspunkterFor(behandling);
        // Assert
        Assertions.assertThat(utledeteAksjonspunkter).containsExactly(AksjonspunktResultat.opprettForAksjonspunkt(MANUELL_KONTROLL_AV_OM_BRUKER_HAR_OMSORG));
    }

    @Test
    public void aksjonspunkt_dersom_mor_søker_og_ikke_oppgitt_omsorg_til_barnet_med_lengre_søknadsperioden() {
        // Arrange
        OppgittPeriode periode1 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(FØDSELSDATO_NÅ, FØDSELSDATO_NÅ.plusWeeks(6))
            .build();

        OppgittPeriode periode2 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medPeriode(FØDSELSDATO_NÅ.plusWeeks(6).plusDays(1), FØDSELSDATO_NÅ.plusWeeks(10))
            .build();
        Behandling behandling = opprettBehandlingForBekreftetFødselMedSøknadsperioder(FØDSELSDATO_NÅ, 1, Arrays.asList(periode1, periode2));
        // Act
        List<AksjonspunktResultat> aksjonspunktResultater = aksjonspunktUtleder.utledAksjonspunkterFor(behandling);

        // Assert
        Assertions.assertThat(aksjonspunktResultater).containsExactly(AksjonspunktResultat.opprettForAksjonspunkt(MANUELL_KONTROLL_AV_OM_BRUKER_HAR_OMSORG));
    }

    @Test
    public void ingen_aksjonspunkt_dersom_mor_søker_og_ikke_oppgitt_omsorg_til_barnet_med_kortere_søknadsperioden() {
        // Arrange
        OppgittPeriode periode1 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(FØDSELSDATO_NÅ, FØDSELSDATO_NÅ.plusWeeks(5))
            .build();

        Behandling behandling = opprettBehandlingForBekreftetFødselMedSøknadsperioder(FØDSELSDATO_NÅ, 1, Arrays.asList(periode1));
        // Act
        List<AksjonspunktResultat> aksjonspunktResultater = aksjonspunktUtleder.utledAksjonspunkterFor(behandling);

        // Assert
        Assertions.assertThat(aksjonspunktResultater.isEmpty()).isTrue();
    }

    private Behandling opprettBehandlingMedOppgittTermin(LocalDate termindato) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger
            .forFødselMedGittAktørId(GITT_MOR_AKTØR_ID);
        scenario.medSøknadHendelse().medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
            .medUtstedtDato(LocalDate.now())
            .medTermindato(termindato)
            .medNavnPå("LEGEN MIN"));
        scenario.medAvklarteUttakDatoer(new AvklarteUttakDatoerEntitet(termindato, null));

        PersonInformasjon søker = scenario.opprettBuilderForRegisteropplysninger()
            .medPersonas()
            .kvinne(GITT_MOR_AKTØR_ID, SivilstandType.GIFT, Region.NORDEN)
            .statsborgerskap(Landkoder.NOR)
            .build();
        scenario.medRegisterOpplysninger(søker);

        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(true, true, false);
        scenario.medOppgittRettighet(rettighet);
        return scenario.lagre(repositoryProvider);
    }

    private Behandling opprettBehandlingForFødselRegistrertITps(LocalDate fødselsdato, int antallBarnSøknad) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger
            .forFødselMedGittAktørId(GITT_MOR_AKTØR_ID);
        scenario.medSøknadHendelse()
            .medFødselsDato(fødselsdato)
            .medAntallBarn(antallBarnSøknad);
        scenario.medBekreftetHendelse().medFødselsDato(fødselsdato).medAntallBarn(antallBarnSøknad);

        scenario.medAvklarteUttakDatoer(new AvklarteUttakDatoerEntitet(fødselsdato, null));
        Builder builderForRegisteropplysninger = scenario.opprettBuilderForRegisteropplysninger();

        PersonInformasjon fødtBarn = builderForRegisteropplysninger
            .medPersonas()
            .fødtBarn(GITT_BARN_ID, fødselsdato)
            .relasjonTil(GITT_MOR_AKTØR_ID, RelasjonsRolleType.MORA, false)
            .build();
        scenario.medRegisterOpplysninger(fødtBarn);

        PersonInformasjon søker = builderForRegisteropplysninger
            .medPersonas()
            .kvinne(GITT_MOR_AKTØR_ID, SivilstandType.GIFT, Region.NORDEN)
            .statsborgerskap(Landkoder.NOR)
            .relasjonTil(GITT_BARN_ID, RelasjonsRolleType.BARN, false)
            .build();

        scenario.medRegisterOpplysninger(søker);

        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(true, true, false);
        scenario.medOppgittRettighet(rettighet);

        return scenario.lagre(repositoryProvider);
    }

    private Behandling opprettBehandlingForBekreftetFødselMedSøknadsperioder(LocalDate fødselsdato, int antallBarn, List<OppgittPeriode> søknadsPerioder) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger
            .forFødselMedGittAktørId(GITT_MOR_AKTØR_ID);
        scenario.medSøknadHendelse()
            .medFødselsDato(fødselsdato)
            .medAntallBarn(antallBarn);
        scenario.medBekreftetHendelse()
            .medFødselsDato(fødselsdato)
            .medAntallBarn(antallBarn);

        Builder builderForRegisteropplysninger = scenario.opprettBuilderForRegisteropplysninger();

        PersonInformasjon fødtBarn = builderForRegisteropplysninger
            .medPersonas()
            .fødtBarn(GITT_BARN_ID, fødselsdato)
            .relasjonTil(GITT_MOR_AKTØR_ID, RelasjonsRolleType.MORA, false)
            .build();
        scenario.medRegisterOpplysninger(fødtBarn);

        PersonInformasjon søker = builderForRegisteropplysninger
            .medPersonas()
            .kvinne(GITT_MOR_AKTØR_ID, SivilstandType.GIFT, Region.NORDEN)
            .statsborgerskap(Landkoder.NOR)
            .relasjonTil(GITT_BARN_ID, RelasjonsRolleType.BARN, false)
            .build();
        scenario.medRegisterOpplysninger(søker);

        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(true, false, false);
        scenario.medOppgittRettighet(rettighet);

        OppgittFordeling fordeling = new OppgittFordelingEntitet(søknadsPerioder, true);
        scenario.medFordeling(fordeling);

        return scenario.lagre(repositoryProvider);
    }

}
