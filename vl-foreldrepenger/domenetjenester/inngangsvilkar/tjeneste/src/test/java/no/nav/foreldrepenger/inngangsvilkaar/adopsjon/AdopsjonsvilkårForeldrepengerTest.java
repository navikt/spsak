package no.nav.foreldrepenger.inngangsvilkaar.adopsjon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.inngangsvilkår.VilkårData;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon.Builder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.medlem.impl.MedlemskapPerioderTjenesteImpl;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.impl.BasisPersonopplysningTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.foreldrepenger.inngangsvilkaar.impl.InngangsvilkårOversetter;

public class AdopsjonsvilkårForeldrepengerTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider,
        new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
        new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0), Period.of(0, 6, 0), Period.of(1, 0, 0)),
        Period.of(0, 3, 0),
        Period.of(0, 10, 0));
    private BasisPersonopplysningTjeneste personopplysningTjeneste = new BasisPersonopplysningTjenesteImpl(repositoryProvider, skjæringstidspunktTjeneste);
    private MedlemskapPerioderTjenesteImpl medlemskapPerioderTjeneste = new MedlemskapPerioderTjenesteImpl(12, 6, skjæringstidspunktTjeneste);
    private InngangsvilkårOversetter oversetter = new InngangsvilkårOversetter(repositoryProvider,
        medlemskapPerioderTjeneste, skjæringstidspunktTjeneste, personopplysningTjeneste,
        new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)));

    @Test
    public void skal_gi_avslag_barn_adopteres_er_over_15_år_på_overtakelsesdato() {
        Behandling behandling = settOppAdopsjonBehandlingFor(16, false, NavBrukerKjønn.KVINNE, false, LocalDate.of(2018, 1, 1));

        VilkårData data = new InngangsvilkårForeldrepengerAdopsjon(oversetter).vurderVilkår(behandling);

        assertThat(data.getVilkårType()).isEqualTo(VilkårType.ADOPSJONSVILKARET_FORELDREPENGER);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.IKKE_OPPFYLT);
        assertThat(data.getVilkårUtfallMerknad()).isEqualTo(VilkårUtfallMerknad.VM_1004);
    }

    @Test
    public void skal_gi_avslag_dersom_stønadsperiode_for_annen_forelder_er_brukt_opp() throws Exception {
        LocalDate maksdatoForeldrepenger = LocalDate.of(2018, 8, 1);
        LocalDate omsorgsovertakelsedato = LocalDate.of(2018, 9, 1);

        BeregnMorsMaksdatoTjenesteImpl beregnMorsMaksdatoTjenesteMock = Mockito.mock(BeregnMorsMaksdatoTjenesteImpl.class);
        Mockito.when(beregnMorsMaksdatoTjenesteMock.beregnMaksdatoForeldrepenger(any(Behandling.class))).thenReturn(Optional.of(maksdatoForeldrepenger));

        InngangsvilkårOversetter oversetter = new InngangsvilkårOversetter(repositoryProvider,
            medlemskapPerioderTjeneste, skjæringstidspunktTjeneste, personopplysningTjeneste,
            beregnMorsMaksdatoTjenesteMock);

        Behandling behandling = settOppAdopsjonBehandlingFor(10, true, NavBrukerKjønn.KVINNE, false, omsorgsovertakelsedato);

        VilkårData data = new InngangsvilkårForeldrepengerAdopsjon(oversetter).vurderVilkår(behandling);

        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.readTree(data.getRegelInput());
        String ektefellesBarn = jsonNode.get("ektefellesBarn").asText();

        assertThat(data.getVilkårType()).isEqualTo(VilkårType.ADOPSJONSVILKARET_FORELDREPENGER);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.IKKE_OPPFYLT);
        assertThat(data.getVilkårUtfallMerknad()).isEqualTo(VilkårUtfallMerknad.VM_1051);
        assertThat(data.getRegelInput()).isNotEmpty();
        assertThat(ektefellesBarn).isEqualTo("true");
    }

    @Test
    public void skal_gi_innvilgelse_dersom_stønadsperiode_for_annen_forelder_ikke_er_brukt_opp() throws Exception {
        LocalDate maksdatoForeldrepenger = LocalDate.of(2018, 6, 1);
        LocalDate omsorgsovertakelsedato = LocalDate.of(2018, 5, 1);

        BeregnMorsMaksdatoTjenesteImpl beregnMorsMaksdatoTjenesteMock = Mockito.mock(BeregnMorsMaksdatoTjenesteImpl.class);
        Mockito.when(beregnMorsMaksdatoTjenesteMock.beregnMaksdatoForeldrepenger(any(Behandling.class))).thenReturn(Optional.of(maksdatoForeldrepenger));

        InngangsvilkårOversetter oversetter = new InngangsvilkårOversetter(repositoryProvider,
            medlemskapPerioderTjeneste, skjæringstidspunktTjeneste, personopplysningTjeneste,
            beregnMorsMaksdatoTjenesteMock);

        Behandling behandling = settOppAdopsjonBehandlingFor(
            10, true, NavBrukerKjønn.KVINNE, false, omsorgsovertakelsedato);

        VilkårData data = new InngangsvilkårForeldrepengerAdopsjon(oversetter).vurderVilkår(behandling);

        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.readTree(data.getRegelInput());
        String ektefellesBarn = jsonNode.get("ektefellesBarn").asText();

        assertThat(data.getVilkårType()).isEqualTo(VilkårType.ADOPSJONSVILKARET_FORELDREPENGER);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);
        assertThat(data.getVilkårUtfallMerknad()).isNull();
        assertThat(data.getRegelInput()).isNotEmpty();
        assertThat(ektefellesBarn).isEqualTo("true");
    }

    @Test
    public void skal_gi_innvilgelse_dersom_kvinne_adopterer_barn_10år_som_ikke_tilhører_ektefelle_eller_samboer() {
        Behandling behandling = settOppAdopsjonBehandlingFor(
            10, false, NavBrukerKjønn.KVINNE, false, LocalDate.of(2018, 1, 1));

        VilkårData data = new InngangsvilkårForeldrepengerAdopsjon(oversetter).vurderVilkår(behandling);

        assertThat(data.getVilkårType()).isEqualTo(VilkårType.ADOPSJONSVILKARET_FORELDREPENGER);
        System.out.println(data.getVilkårUtfallMerknad());
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);
        assertThat(data.getVilkårUtfallMerknad()).isNull();
    }

    @Test
    public void skal_gi_innvilgelse_dersom_mann_alene_adopterer_barn_10år_som_ikke_tilhører_ektefelle_eller_samboer() {
        Behandling behandling = settOppAdopsjonBehandlingFor(
            10, false, NavBrukerKjønn.MANN, true, LocalDate.of(2018, 1, 1));

        VilkårData data = new InngangsvilkårForeldrepengerAdopsjon(oversetter).vurderVilkår(behandling);

        assertThat(data.getVilkårType()).isEqualTo(VilkårType.ADOPSJONSVILKARET_FORELDREPENGER);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);
        assertThat(data.getVilkårUtfallMerknad()).isNull();
    }

    private Behandling settOppAdopsjonBehandlingFor(int alder, boolean ektefellesBarn, NavBrukerKjønn kjønn,
                                                    boolean adoptererAlene, LocalDate omsorgsovertakelsedato) {

        AbstractTestScenario<?> scenario = kjønn.equals(NavBrukerKjønn.KVINNE) ? ScenarioMorSøkerEngangsstønad.forAdopsjon()
            : ScenarioFarSøkerEngangsstønad.forAdopsjon();

        leggTilSøker(scenario, kjønn);
        scenario.medSøknadHendelse()
            .medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder()
                .medOmsorgsovertakelseDato(omsorgsovertakelsedato)
                .medErEktefellesBarn(ektefellesBarn)
                .medAdoptererAlene(adoptererAlene))
            .leggTilBarn(omsorgsovertakelsedato.minusYears(alder));
        scenario.medBekreftetHendelse()
            .medAdopsjon(scenario.medBekreftetHendelse().getAdopsjonBuilder()
                .medOmsorgsovertakelseDato(omsorgsovertakelsedato)
                .medErEktefellesBarn(ektefellesBarn)
                .medAdoptererAlene(adoptererAlene))
            .leggTilBarn(omsorgsovertakelsedato.minusYears(alder));
        return scenario.lagre(repositoryProvider);
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
