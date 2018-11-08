package no.nav.foreldrepenger.inngangsvilkaar.medlemskap;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørInntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapDekningType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapPerioderBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.inngangsvilkår.VilkårData;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
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
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class MedlemskapsvilkårTest {

    public static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider,
        new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
        new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0), Period.of(0, 6, 0), Period.of(1, 0, 0)),
        Period.of(0, 3, 0),
        Period.of(0, 10, 0));
    private BasisPersonopplysningTjeneste personopplysningTjeneste = new BasisPersonopplysningTjenesteImpl(repositoryProvider, skjæringstidspunktTjeneste);
    private InngangsvilkårOversetter oversetter = new InngangsvilkårOversetter(repositoryProvider,
        new MedlemskapPerioderTjenesteImpl(12, 6, skjæringstidspunktTjeneste), skjæringstidspunktTjeneste, personopplysningTjeneste,
        new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)));

    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());
    private InngangsvilkårMedlemskap vurderMedlemskapsvilkarEngangsstonad = new InngangsvilkårMedlemskap(
        oversetter);
    private YrkesaktivitetBuilder yrkesaktivitetBuilder;

    /**
     * Input:
     * - bruker manuelt avklart som ikke medlem (FP VK 2.13) = JA
     * <p>
     * Forventet: Ikke oppfylt, avslagsid 1020
     */
    @Test
    public void skal_vurdere_manuell_avklart_ikke_medlem_pga_endringer_i_tps_som_vilkår_ikke_oppfylt() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = lagTestScenario(MedlemskapDekningType.FTL_2_7_a, Landkoder.NOR, PersonstatusType.BOSA, true);
        scenario.medMedlemskap().medMedlemsperiodeManuellVurdering(MedlemskapManuellVurderingType.SAKSBEHANDLER_SETTER_OPPHØR_AV_MEDL_PGA_ENDRINGER_I_TPS);
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        VilkårData vilkårData = vurderMedlemskapsvilkarEngangsstonad.vurderVilkår(behandling);

        // Assert
        assertThat(vilkårData.getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkårData.getUtfallType()).isEqualTo(VilkårUtfallType.IKKE_OPPFYLT);
    }

    /**
     * Input:
     * - bruker manuelt avklart som ikke medlem (FP VK 2.13) = JA
     * <p>
     * Forventet: Ikke oppfylt, avslagsid 1020
     */
    @Test
    public void skal_vurdere_manuell_avklart_ikke_medlem_som_vilkår_ikke_oppfylt() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = lagTestScenario(MedlemskapDekningType.FTL_2_7_a, Landkoder.NOR, PersonstatusType.BOSA, true);
        scenario.medMedlemskap().medMedlemsperiodeManuellVurdering(MedlemskapManuellVurderingType.UNNTAK);
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        VilkårData vilkårData = vurderMedlemskapsvilkarEngangsstonad.vurderVilkår(behandling);

        // Assert
        assertThat(vilkårData.getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkårData.getUtfallType()).isEqualTo(VilkårUtfallType.IKKE_OPPFYLT);
        assertThat(vilkårData.getVilkårUtfallMerknad()).isEqualTo(VilkårUtfallMerknad.VM_1020);
    }

    /**
     * Input:
     * - bruker registrert som ikke medlem (FP VK 2.13) = JA
     * <p>
     * Forventet: Ikke oppfylt, avslagsid 1020
     */
    @Test
    public void skal_vurdere_maskinelt_avklart_ikke_medlem_som_vilkår_ikke_oppfylt() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = lagTestScenario(MedlemskapDekningType.FTL_2_6, Landkoder.NOR, PersonstatusType.BOSA, true);
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        VilkårData vilkårData = vurderMedlemskapsvilkarEngangsstonad.vurderVilkår(behandling);

        // Assert
        assertThat(vilkårData.getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkårData.getUtfallType()).isEqualTo(VilkårUtfallType.IKKE_OPPFYLT);
        assertThat(vilkårData.getVilkårUtfallMerknad()).isEqualTo(VilkårUtfallMerknad.VM_1020);
    }

    /**
     * Input:
     * - bruker registrert som ikke medlem (FP VK 2.13) = NEI
     * - bruker avklart som pliktig eller frivillig medlem (FP VK 2.2) = JA
     * <p>
     * Forventet: Oppfylt
     *
     * @throws IOException
     * @throws JsonProcessingException
     */
    @Test
    public void skal_vurdere_avklart_pliktig_medlem_som_vilkår_oppfylt() throws JsonProcessingException, IOException {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = lagTestScenario(MedlemskapDekningType.FTL_2_7_a, Landkoder.NOR, PersonstatusType.BOSA, true);
        scenario.medMedlemskap().medMedlemsperiodeManuellVurdering(MedlemskapManuellVurderingType.MEDLEM);
        leggTilSøker(scenario, PersonstatusType.BOSA, Region.UDEFINERT, Landkoder.SWE);
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        VilkårData vilkårData = vurderMedlemskapsvilkarEngangsstonad.vurderVilkår(behandling);


        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.readTree(vilkårData.getRegelInput());
        String personStatusType = jsonNode.get("personStatusType").asText();

        // Assert
        assertThat(vilkårData.getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkårData.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);
        assertThat(vilkårData.getRegelInput()).isNotEmpty();
        assertThat(personStatusType).isEqualTo("BOSA");
    }

    /**
     * Input:
     * - bruker registrert som ikke medlem (FP VK 2.13) = NEI
     * - bruker avklart som pliktig eller frivillig medlem (FP VK 2.2) = NEI
     * - bruker registrert som utvandret (FP VK 2.1) = JA
     * - bruker har relevant arbeidsforhold og inntekt som dekker skjæringstidspunkt (FP_VK_2.2.1) = NEI
     * <p>
     * Forventet: Ikke oppfylt, avslagsid 1021
     */
    @Test
    public void skal_vurdere_utvandret_som_vilkår_ikke_oppfylt_ingen_relevant_arbeid_og_inntekt() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = lagTestScenario(MedlemskapDekningType.UNNTATT, Landkoder.NOR, PersonstatusType.UTVA, false);
        scenario.medMedlemskap().medMedlemsperiodeManuellVurdering(MedlemskapManuellVurderingType.IKKE_RELEVANT);
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        VilkårData vilkårData = vurderMedlemskapsvilkarEngangsstonad.vurderVilkår(behandling);

        // Assert
        assertThat(vilkårData.getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkårData.getUtfallType()).isEqualTo(VilkårUtfallType.IKKE_OPPFYLT);
        assertThat(vilkårData.getVilkårUtfallMerknad()).isEqualTo(VilkårUtfallMerknad.VM_1021);
    }

    /**
     * Input:
     * - bruker registrert som ikke medlem (FP VK 2.13) = NEI
     * - bruker avklart som pliktig eller frivillig medlem (FP VK 2.2) = NEI
     * - bruker registrert som utvandret (FP VK 2.1) = JA
     * - bruker har relevant arbeidsforhold og inntekt som dekker skjæringstidspunkt (FP_VK_2.2.1) = JA
     * <p>
     * Forventet: Ikke oppfylt, avslagsid 1021
     */
    @Test
    public void skal_vurdere_utvandret_som_vilkår_oppfylt_når_relevant_arbeid_og_inntekt_finnes() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = lagTestScenario(MedlemskapDekningType.UNNTATT, Landkoder.NOR, PersonstatusType.UTVA, true);
        scenario.medMedlemskap().medMedlemsperiodeManuellVurdering(MedlemskapManuellVurderingType.IKKE_RELEVANT);
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        VilkårData vilkårData = vurderMedlemskapsvilkarEngangsstonad.vurderVilkår(behandling);

        // Assert
        assertThat(vilkårData.getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkårData.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);
    }

    /**
     * Input:
     * - bruker registrert som ikke medlem (FP VK 2.13) = NEI
     * - bruker avklart som pliktig eller frivillig medlem (FP VK 2.2) = NEI
     * - bruker registrert som utvandret (FP VK 2.1) = NEI
     * - bruker avklart som ikke bosatt = JA
     * - bruker har relevant arbeidsforhold og inntekt som dekker skjæringstidspunkt (FP_VK_2.2.1) = JA
     * <p>
     * Forventet: Ikke oppfylt, avslagsid 1025
     */
    @Test
    public void skal_vurdere_avklart_ikke_bosatt_som_vilkår_når_bruker_har_relevant_arbeid_og_inntekt() {
        // Arrange
        Landkoder landkode = kodeverkRepository.finn(Landkoder.class, "POL");
        ScenarioMorSøkerEngangsstønad scenario = lagTestScenario(MedlemskapDekningType.UNNTATT, landkode, PersonstatusType.BOSA, true);
        scenario.medMedlemskap().medBosattVurdering(false).medMedlemsperiodeManuellVurdering(MedlemskapManuellVurderingType.IKKE_RELEVANT);
        ;
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        VilkårData vilkårData = vurderMedlemskapsvilkarEngangsstonad.vurderVilkår(behandling);

        // Assert
        assertThat(vilkårData.getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkårData.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);
    }

    /**
     * Input:
     * - bruker registrert som ikke medlem (FP VK 2.13) = NEI
     * - bruker avklart som pliktig eller frivillig medlem (FP VK 2.2) = NEI
     * - bruker registrert som utvandret (FP VK 2.1) = NEI
     * - bruker avklart som ikke bosatt = JA
     * - bruker har relevant arbeidsforhold og inntekt som dekker skjæringstidspunkt (FP_VK_2.2.1) = NEI
     * <p>
     * Forventet: Ikke oppfylt, avslagsid 1025
     */
    @Test
    public void skal_vurdere_avklart_ikke_bosatt_som_vilkår_når_bruker_har_ingen_relevant_arbeid_og_inntekt() {
        // Arrange
        Landkoder landkode = kodeverkRepository.finn(Landkoder.class, "POL");
        ScenarioMorSøkerEngangsstønad scenario = lagTestScenario(MedlemskapDekningType.UNNTATT, landkode, PersonstatusType.BOSA, false);
        scenario.medMedlemskap().medBosattVurdering(false).medMedlemsperiodeManuellVurdering(MedlemskapManuellVurderingType.IKKE_RELEVANT);
        ;
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        VilkårData vilkårData = vurderMedlemskapsvilkarEngangsstonad.vurderVilkår(behandling);

        // Assert
        assertThat(vilkårData.getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkårData.getUtfallType()).isEqualTo(VilkårUtfallType.IKKE_OPPFYLT);
        assertThat(vilkårData.getVilkårUtfallMerknad()).isEqualTo(VilkårUtfallMerknad.VM_1025);
    }

    /**
     * Input:
     * - bruker registrert som ikke medlem (FP VK 2.13) = NEI
     * - bruker avklart som pliktig eller frivillig medlem (FP VK 2.2) = NEI
     * - bruker registrert som utvandret (FP VK 2.1) = NEI
     * - bruker avklart som ikke bosatt = NEI
     * - bruker oppgir opphold i norge (FP VK 2.3) = JA
     * - bruker oppgir opphold norge minst 12 mnd (FP VK 2.5) = JA
     * - bruker norsk/nordisk statsborger i TPS (FP VK 2.11) = JA
     * <p>
     * Forventet: oppfylt
     */
    @Test
    public void skal_vurdere_norsk_nordisk_statsborger_som_vilkår_oppfylt() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = lagTestScenario(MedlemskapDekningType.UDEFINERT, Landkoder.NOR, PersonstatusType.BOSA, true);
        leggTilSøker(scenario, PersonstatusType.BOSA, Region.UDEFINERT, Landkoder.NOR);
        scenario.medMedlemskap().medBosattVurdering(true);
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        VilkårData vilkårData = vurderMedlemskapsvilkarEngangsstonad.vurderVilkår(behandling);

        // Assert
        assertThat(vilkårData.getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkårData.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);
    }

    /**
     * Input:
     * - bruker registrert som ikke medlem (FP VK 2.13) = NEI
     * - bruker avklart som pliktig eller frivillig medlem (FP VK 2.2) = NEI
     * - bruker registrert som utvandret (FP VK 2.1) = NEI
     * - bruker avklart som ikke bosatt = NEI
     * - bruker oppgir opphold i norge (FP VK 2.3) = JA
     * - bruker oppgir opphold norge minst 12 mnd (FP VK 2.5) = JA
     * - bruker norsk/nordisk statsborger i TPS (FP VK 2.11) = NEI
     * - bruker EU/EØS statsborger = JA
     * - bruker har avklart oppholdsrett (FP VK 2.12) = JA
     * <p>
     * Forventet: oppfylt
     */
    @Test
    public void skal_vurdere_eøs_statsborger_med_oppholdsrett_som_vilkår_oppfylt() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = lagTestScenario(Landkoder.NOR, PersonstatusType.BOSA);
        leggTilSøker(scenario, PersonstatusType.BOSA, Region.EOS, Landkoder.SWE);
        scenario.medMedlemskap().medBosattVurdering(true).medOppholdsrettVurdering(true);
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        VilkårData vilkårData = vurderMedlemskapsvilkarEngangsstonad.vurderVilkår(behandling);

        // Assert
        assertThat(vilkårData.getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkårData.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);
    }

    /**
     * Input:
     * - bruker registrert som ikke medlem (FP VK 2.13) = NEI
     * - bruker avklart som pliktig eller frivillig medlem (FP VK 2.2) = NEI
     * - bruker registrert som utvandret (FP VK 2.1) = NEI
     * - bruker avklart som ikke bosatt = NEI
     * - bruker oppgir opphold i norge (FP VK 2.3) = JA
     * - bruker oppgir opphold norge minst 12 mnd (FP VK 2.5) = JA
     * - bruker norsk/nordisk statsborger i TPS (FP VK 2.11) = NEI
     * - bruker EU/EØS statsborger = JA
     * - bruker har avklart oppholdsrett (FP VK 2.12) = NEI
     * <p>
     * Forventet: Ikke oppfylt, avslagsid 1024
     */
    @Test
    public void skal_vurdere_eøs_statsborger_uten_oppholdsrett_som_vilkår_ikke_oppfylt() {
        // Arrange
        Landkoder landkodeEOS = kodeverkRepository.finn(Landkoder.class, "POL");
        ScenarioMorSøkerEngangsstønad scenario = lagTestScenario(landkodeEOS, PersonstatusType.BOSA);
        scenario.medMedlemskap().medBosattVurdering(true).medOppholdsrettVurdering(false);
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        VilkårData vilkårData = vurderMedlemskapsvilkarEngangsstonad.vurderVilkår(behandling);

        // Assert
        assertThat(vilkårData.getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkårData.getUtfallType()).isEqualTo(VilkårUtfallType.IKKE_OPPFYLT);
        assertThat(vilkårData.getVilkårUtfallMerknad()).isEqualTo(VilkårUtfallMerknad.VM_1024);
    }

    /**
     * Input:
     * - bruker registrert som ikke medlem (FP VK 2.13) = NEI
     * - bruker avklart som pliktig eller frivillig medlem (FP VK 2.2) = NEI
     * - bruker registrert som utvandret (FP VK 2.1) = NEI
     * - bruker avklart som ikke bosatt = NEI
     * - bruker oppgir opphold i norge (FP VK 2.3) = JA
     * - bruker oppgir opphold norge minst 12 mnd (FP VK 2.5) = JA
     * - bruker norsk/nordisk statsborger i TPS (FP VK 2.11) = NEI
     * - bruker EU/EØS statsborger = NEI
     * - bruker har avklart lovlig opphold (FP VK 2.12) = NEI
     * <p>
     * Forventet: Ikke oppfylt, avslagsid 1023
     */
    @Test
    public void skal_vurdere_annen_statsborger_uten_lovlig_opphold_som_vilkår_ikke_oppfylt() {
        // Arrange
        Landkoder land = kodeverkRepository.finn(Landkoder.class, "ARG");
        ScenarioMorSøkerEngangsstønad scenario = lagTestScenario(MedlemskapDekningType.UNNTATT, land, PersonstatusType.BOSA, true);
        scenario.medMedlemskap().medBosattVurdering(true).medLovligOppholdVurdering(false)
            .medMedlemsperiodeManuellVurdering(MedlemskapManuellVurderingType.IKKE_RELEVANT);
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        VilkårData vilkårData = vurderMedlemskapsvilkarEngangsstonad.vurderVilkår(behandling);

        // Assert
        assertThat(vilkårData.getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkårData.getUtfallType()).isEqualTo(VilkårUtfallType.IKKE_OPPFYLT);
        assertThat(vilkårData.getVilkårUtfallMerknad()).isEqualTo(VilkårUtfallMerknad.VM_1023);
    }

    /**
     * Input:
     * - bruker registrert som ikke medlem (FP VK 2.13) = NEI
     * - bruker avklart som pliktig eller frivillig medlem (FP VK 2.2) = NEI
     * - bruker registrert som utvandret (FP VK 2.1) = NEI
     * - bruker avklart som ikke bosatt = NEI
     * - bruker oppgir opphold i norge (FP VK 2.3) = JA
     * - bruker oppgir opphold norge minst 12 mnd (FP VK 2.5) = JA
     * - bruker norsk/nordisk statsborger i TPS (FP VK 2.11) = NEI
     * - bruker EU/EØS statsborger = NEI
     * - bruker har avklart lovlig opphold (FP VK 2.12) = JA
     * <p>
     * Forventet: oppfylt
     */
    @Test
    public void skal_vurdere_annen_statsborger_med_lovlig_opphold_som_vilkår_oppfylt() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = lagTestScenario(Landkoder.NOR, PersonstatusType.BOSA);
        leggTilSøker(scenario, PersonstatusType.BOSA, Region.UDEFINERT, Landkoder.USA);
        scenario.medMedlemskap().medBosattVurdering(true).medLovligOppholdVurdering(true);
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        VilkårData vilkårData = vurderMedlemskapsvilkarEngangsstonad.vurderVilkår(behandling);

        // Assert
        assertThat(vilkårData.getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkårData.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);
    }

    /**
     * - bruker har relevant arbeidsforhold og inntekt som dekker skjæringstidspunkt (FP_VK_2.2.1) = NEI
     */
    @Test
    public void skal_få_medlemskapsvilkåret_satt_til_ikke_oppfylt_når_saksbehandler_setter_personstatus_til_utvandert_og_ingen_relevant_arbeid_og_inntekt() {
        // Arrange

        ScenarioMorSøkerEngangsstønad scenario = lagTestScenario(MedlemskapDekningType.FTL_2_9_1_c, Landkoder.NOR, PersonstatusType.UREG, false);
        scenario.medMedlemskap().medMedlemsperiodeManuellVurdering(MedlemskapManuellVurderingType.IKKE_RELEVANT);

        leggTilSøker(scenario, PersonstatusType.UREG, Region.NORDEN, Landkoder.SWE);

        Behandling behandling = scenario.lagre(repositoryProvider);

        final PersonInformasjonBuilder personInformasjonBuilder = repositoryProvider.getPersonopplysningRepository().opprettBuilderForOverstyring(behandling);
        LocalDate utvandretDato = LocalDate.now().minusYears(10);
        personInformasjonBuilder.leggTil(personInformasjonBuilder.getPersonstatusBuilder(behandling.getAktørId(), DatoIntervallEntitet.fraOgMed(utvandretDato))
            .medPersonstatus(PersonstatusType.UTVA));
        repositoryProvider.getPersonopplysningRepository().lagre(behandling, personInformasjonBuilder);

        // Act
        VilkårData vilkårData = vurderMedlemskapsvilkarEngangsstonad.vurderVilkår(behandling);

        // Assert
        assertThat(vilkårData.getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkårData.getUtfallType()).isEqualTo(VilkårUtfallType.IKKE_OPPFYLT);
        assertThat(vilkårData.getVilkårUtfallMerknad()).isEqualTo(VilkårUtfallMerknad.VM_1021);
    }

    /**
     * - bruker har relevant arbeidsforhold og inntekt som dekker skjæringstidspunkt (FP_VK_2.2.1) = JA
     */
    @Test
    public void skal_få_medlemskapsvilkåret_satt_til_ikke_oppfylt_når_saksbehandler_setter_personstatus_til_utvandert_og_relevant_arbeid_og_inntekt_finnes() {
        // Arrange

        ScenarioMorSøkerEngangsstønad scenario = lagTestScenario(MedlemskapDekningType.FTL_2_9_1_c, Landkoder.NOR, PersonstatusType.UREG, true);
        scenario.medMedlemskap().medMedlemsperiodeManuellVurdering(MedlemskapManuellVurderingType.IKKE_RELEVANT);

        leggTilSøker(scenario, PersonstatusType.UREG, Region.NORDEN, Landkoder.SWE);

        Behandling behandling = scenario.lagre(repositoryProvider);

        final PersonInformasjonBuilder personInformasjonBuilder = repositoryProvider.getPersonopplysningRepository().opprettBuilderForOverstyring(behandling);
        LocalDate utvandretDato = LocalDate.now().minusYears(10);
        personInformasjonBuilder.leggTil(personInformasjonBuilder.getPersonstatusBuilder(behandling.getAktørId(), DatoIntervallEntitet.fraOgMed(utvandretDato))
            .medPersonstatus(PersonstatusType.UTVA));
        repositoryProvider.getPersonopplysningRepository().lagre(behandling, personInformasjonBuilder);

        // Act
        VilkårData vilkårData = vurderMedlemskapsvilkarEngangsstonad.vurderVilkår(behandling);

        // Assert
        assertThat(vilkårData.getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkårData.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);
    }

    /**
     * Lager minimalt testscenario med en medlemsperiode som indikerer om søker er medlem eller ikke.
     */
    private ScenarioMorSøkerEngangsstønad lagTestScenario(MedlemskapDekningType dekningType, Landkoder statsborgerskap,
                                                          PersonstatusType personstatusType, boolean harArbeidOgInntektSomDekkerSkjæringstidspunkt) {
        return lagTestScenario(dekningType, statsborgerskap, personstatusType, Region.NORDEN, harArbeidOgInntektSomDekkerSkjæringstidspunkt);
    }

    private ScenarioMorSøkerEngangsstønad lagTestScenario(MedlemskapDekningType dekningType, Landkoder statsborgerskap,
                                                          PersonstatusType personstatusType, Region region, boolean harArbeidOgInntektSomDekkerSkjæringstidspunkt) {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse()
            .medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
                .medTermindato(SKJÆRINGSTIDSPUNKT)
                .medNavnPå("navn navnesen")
                .medUtstedtDato(LocalDate.now()));
        if (dekningType != null) {
            scenario.leggTilMedlemskapPeriode(new MedlemskapPerioderBuilder()
                .medDekningType(dekningType)
                .medMedlemskapType(MedlemskapType.ENDELIG)
                .medPeriode(LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(1))
                .build());
        }

        Builder builderForRegisteropplysninger = scenario.opprettBuilderForRegisteropplysninger();
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();

        PersonInformasjon søker = builderForRegisteropplysninger
            .medPersonas()
            .kvinne(søkerAktørId, SivilstandType.GIFT, region)
            .personstatus(personstatusType)
            .statsborgerskap(statsborgerskap)
            .build();
        scenario.medRegisterOpplysninger(søker);


        if (harArbeidOgInntektSomDekkerSkjæringstidspunkt) {
            opprettArbeidOgInntektForBehandling(scenario, SKJÆRINGSTIDSPUNKT.minusMonths(5), SKJÆRINGSTIDSPUNKT.plusDays(2));
        } else {
            opprettArbeidOgInntektForBehandling(scenario, SKJÆRINGSTIDSPUNKT.minusMonths(5), SKJÆRINGSTIDSPUNKT.minusDays(2));
        }

        return scenario;
    }

    private void leggTilSøker(ScenarioMorSøkerEngangsstønad scenario, PersonstatusType personstatus, Region region, Landkoder statsborgerskapLand) {
        Builder builderForRegisteropplysninger = scenario.opprettBuilderForRegisteropplysninger();
        AktørId barnAktørId = new AktørId("123");
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();

        PersonInformasjon fødtBarn = builderForRegisteropplysninger
            .medPersonas()
            .fødtBarn(barnAktørId, LocalDate.now().plusDays(7))
            .relasjonTil(søkerAktørId, RelasjonsRolleType.MORA, null)
            .build();

        PersonInformasjon søker = builderForRegisteropplysninger
            .medPersonas()
            .kvinne(søkerAktørId, SivilstandType.GIFT, region)
            .statsborgerskap(statsborgerskapLand)
            .personstatus(personstatus)
            .relasjonTil(barnAktørId, RelasjonsRolleType.BARN, null)
            .build();
        scenario.medRegisterOpplysninger(søker);
        scenario.medRegisterOpplysninger(fødtBarn);
    }


    private ScenarioMorSøkerEngangsstønad lagTestScenario(Landkoder statsborgerskap, PersonstatusType personstatusType) {
        return lagTestScenario(null, statsborgerskap, personstatusType, true);
    }

    private InntektArbeidYtelseAggregatBuilder opprettArbeidOgInntektForBehandling(AbstractTestScenario scenario, LocalDate fom, LocalDate tom) {

        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder().medNavn("OrgA").medOrgnr("42").oppdatertOpplysningerNå().build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet);

        InntektArbeidYtelseAggregatBuilder aggregatBuilder = scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd();

        lagAktørArbeid(aggregatBuilder, scenario.getDefaultBrukerAktørId(), virksomhet, fom, tom, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, Optional.empty());
        for (LocalDate dt = fom; dt.isBefore(tom); dt = dt.plusMonths(1)) {
            lagInntekt(aggregatBuilder, scenario.getDefaultBrukerAktørId(), virksomhet, dt, dt.plusMonths(1));
        }

        return aggregatBuilder;
    }

    private AktørArbeid lagAktørArbeid(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, AktørId aktørId, Virksomhet virksomhet,
                                       LocalDate fom, LocalDate tom, ArbeidType arbeidType, Optional<String> arbeidsforholdRef) {
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder
            .getAktørArbeidBuilder(aktørId);

        Opptjeningsnøkkel opptjeningsnøkkel;
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(virksomhet);
        if (arbeidsforholdRef.isPresent()) {
            opptjeningsnøkkel = new Opptjeningsnøkkel(arbeidsforholdRef.get(), arbeidsgiver.getIdentifikator(), null);
        } else {
            opptjeningsnøkkel = Opptjeningsnøkkel.forOrgnummer(virksomhet.getOrgnr());
        }


        yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(opptjeningsnøkkel, arbeidType);
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale =
            aktivitetsAvtaleBuilder.medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom));

        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(aktivitetsAvtale)
            .medArbeidType(arbeidType)
            .medArbeidsgiver(arbeidsgiver);

        yrkesaktivitetBuilder.medArbeidsforholdId(arbeidsforholdRef.isPresent() ? ArbeidsforholdRef.ref(arbeidsforholdRef.get()) : null);

        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        return aktørArbeidBuilder.build();
    }

    private void lagInntekt(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, AktørId aktørId, Virksomhet virksomhet,
                            LocalDate fom, LocalDate tom) {
        Opptjeningsnøkkel opptjeningsnøkkel = Opptjeningsnøkkel.forOrgnummer(virksomhet.getOrgnr());

        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(aktørId);

        Stream.of(InntektsKilde.INNTEKT_BEREGNING, InntektsKilde.INNTEKT_SAMMENLIGNING, InntektsKilde.INNTEKT_OPPTJENING).forEach(kilde -> {
            AktørInntektEntitet.InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(kilde, opptjeningsnøkkel);
            InntektEntitet.InntektspostBuilder inntektspost = InntektEntitet.InntektspostBuilder.ny()
                .medBeløp(BigDecimal.valueOf(35000))
                .medPeriode(fom, tom)
                .medInntektspostType(InntektspostType.LØNN);
            inntektBuilder.leggTilInntektspost(inntektspost).medArbeidsgiver(yrkesaktivitetBuilder.build().getArbeidsgiver());
            aktørInntektBuilder.leggTilInntekt(inntektBuilder);
            inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
        });
    }
}
