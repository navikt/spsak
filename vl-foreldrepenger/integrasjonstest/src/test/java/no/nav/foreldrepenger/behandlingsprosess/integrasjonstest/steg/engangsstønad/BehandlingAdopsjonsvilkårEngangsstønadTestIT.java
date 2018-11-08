package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.engangsstønad;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_ADOPSJONSDOKUMENTAJON;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_OM_SØKER_ER_MANN_SOM_ADOPTERER_ALENE;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall.resultat;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.VilkårTestutfall.resultat;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.søknad.SøknadTestdataBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.KjørProsessTasks;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.engangsstønad.søknad.SoeknadsskjemaEngangsstoenadTestdataBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.DokumentmottakTestUtil;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.GrunnlagForAnsvarsovertakelse;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftEktefelleAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftMannAdoptererAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.BekreftDokumentertDatoAksjonspunktDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;

@RunWith(CdiRunner.class)
public class BehandlingAdopsjonsvilkårEngangsstønadTestIT {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();
    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);
    private BehandlingskontrollKontekst kontekst;

    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    @Inject
    private AksjonspunktApplikasjonTjeneste applikasjonstjeneste;

    @Inject
    private FagsakRepository fagsakRepository;

    @Inject
    private BehandlingRepository behandlingRepository;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;
    @Inject
    private ProsessTaskRepository prosessTaskRepository;

    @Inject
    private DokumentmottakTestUtil hjelper;

    @Test
    public void adopsjon_kvinne_happy_case() {
        // Arrange
        Fagsak fagsak = byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        LocalDate fødselsdato = LocalDate.now().minusDays(10000);
        LocalDate mottattDato = LocalDate.now().plusDays(7L);
        LocalDate overtakelseDato = LocalDate.now();

        Long behandlingId = hjelper.byggBehandling(fagsak, søknadMorAdopsjon(fødselsdato, mottattDato, overtakelseDato));

        // Act
        utførProsessSteg(behandlingId);
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        // Assert
        assertUnikFagsak(RelasjonsRolleType.MORA);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
                asList(resultat(VilkårType.SØKNADSFRISTVILKÅRET, VilkårUtfallType.IKKE_VURDERT),
                        resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_VURDERT),
                        resultat(VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD, VilkårUtfallType.IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(asList(
                resultat(AVKLAR_ADOPSJONSDOKUMENTAJON, AksjonspunktStatus.OPPRETTET),
                resultat(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN, AksjonspunktStatus.OPPRETTET),
                resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, AksjonspunktStatus.UTFØRT)));
        assertThat(repository.hentAlle(Beregning.class)).hasSize(0);
    }

    @Test
    public void adopsjon_mann_happy_case() {
        // Arrange
        Fagsak fagsak = byggFagsak(TpsRepo.STD_MANN_AKTØR_ID, RelasjonsRolleType.FARA, NavBrukerKjønn.MANN);
        LocalDate fødselsdato = LocalDate.now().minusDays(1000);
        LocalDate mottattDato = LocalDate.now().plusDays(7L);
        LocalDate adopsjonsdato = LocalDate.now();

        Long behandlingId = hjelper.byggBehandling(fagsak, søknadFarAdopsjon(fødselsdato, mottattDato, adopsjonsdato));

        // Steg 1: Kjør behandling -> resultat abstraktpunkt for dokumentasjon, ektefelles barn, adopterer alene
        utførProsessSteg(behandlingId);
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        assertUnikFagsak(RelasjonsRolleType.FARA);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
                asList(resultat(VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD, VilkårUtfallType.IKKE_VURDERT),
                        resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_VURDERT),
                        resultat(VilkårType.SØKNADSFRISTVILKÅRET, VilkårUtfallType.IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(asList(
                resultat(AVKLAR_ADOPSJONSDOKUMENTAJON, AksjonspunktStatus.OPPRETTET),
                resultat(AVKLAR_OM_SØKER_ER_MANN_SOM_ADOPTERER_ALENE, AksjonspunktStatus.OPPRETTET),
                resultat(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN, AksjonspunktStatus.OPPRETTET),
                resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, AksjonspunktStatus.UTFØRT)));
        assertThat(repository.hentAlle(Beregning.class)).hasSize(0);

        // Steg 2: Avklar fakta fra GUI -> Søknadsfristvilkåret innvilges
        List<BekreftetAksjonspunktDto> aksjonspunktDtoer = byggAvklarFaktaDtoer(behandlingId, adopsjonsdato, false);
        applikasjonstjeneste.bekreftAksjonspunkter(aksjonspunktDtoer, behandlingId);

        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
                asList(resultat(VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD, VilkårUtfallType.OPPFYLT),
                        resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT),
                        resultat(VilkårType.SØKNADSFRISTVILKÅRET, VilkårUtfallType.OPPFYLT)));
    }

    @Test
    public void adopsjon_mann_happy_case_gammelt_søknadsformat() {
        // Arrange
        Fagsak fagsak = byggFagsak(TpsRepo.STD_MANN_AKTØR_ID, RelasjonsRolleType.FARA, NavBrukerKjønn.MANN);
        LocalDate fødselsdato = LocalDate.now().minusDays(1000);
        LocalDate.now().plusDays(7L);
        LocalDate omsorgsovertakelseDato = LocalDate.now();

        Long behandlingId = hjelper.byggBehandlingGammeltSøknadsformat(fagsak, søknadFarGammeltFormat(GrunnlagForAnsvarsovertakelse.ADOPTERER_ALENE,
            fødselsdato, omsorgsovertakelseDato));

        // Steg 1: Kjør behandling -> resultat abstraktpunkt for dokumentasjon, ektefelles barn, adopterer alene
        utførProsessSteg(behandlingId);
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        assertUnikFagsak(RelasjonsRolleType.FARA);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD, VilkårUtfallType.IKKE_VURDERT),
                resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_VURDERT),
                resultat(VilkårType.SØKNADSFRISTVILKÅRET, VilkårUtfallType.IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(asList(
            resultat(AVKLAR_ADOPSJONSDOKUMENTAJON, AksjonspunktStatus.OPPRETTET),
            resultat(AVKLAR_OM_SØKER_ER_MANN_SOM_ADOPTERER_ALENE, AksjonspunktStatus.OPPRETTET),
            resultat(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN, AksjonspunktStatus.OPPRETTET),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, AksjonspunktStatus.UTFØRT)));
        assertThat(repository.hentAlle(Beregning.class)).hasSize(0);

        // Steg 2: Avklar fakta fra GUI -> Søknadsfristvilkåret innvilges
        List<BekreftetAksjonspunktDto> aksjonspunktDtoer = byggAvklarFaktaDtoer(behandlingId, omsorgsovertakelseDato, false);
        applikasjonstjeneste.bekreftAksjonspunkter(aksjonspunktDtoer, behandlingId);

        new KjørProsessTasks(prosessTaskRepository).utførTasks();


        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD, VilkårUtfallType.OPPFYLT),
                resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT),
                resultat(VilkårType.SØKNADSFRISTVILKÅRET, VilkårUtfallType.OPPFYLT)));
    }

    @Test
    public void adopsjon_avslås_når_mann_adoptere_ektefelles_barn() {
        // Arrange
        Fagsak fagsak = byggFagsak(TpsRepo.STD_MANN_AKTØR_ID, RelasjonsRolleType.FARA, NavBrukerKjønn.MANN);
        LocalDate fødselsdato = LocalDate.now().minusDays(1000);
        LocalDate søknadsdato = LocalDate.now().plusDays(7L);
        LocalDate adopsjonsdato = LocalDate.now();

        Long behandlingId = hjelper.byggBehandling(fagsak, søknadFarAdopsjon(fødselsdato, søknadsdato, adopsjonsdato));

        // Steg 1: Kjør behandling -> resultat abstraktpunkt for dokumentasjon, ektefelles barn, adopterer alene
        utførProsessSteg(behandlingId);
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        // Steg 2: Avklar fakta fra GUI -> Adopsjon avslås fordi mann søker om å adoptere ektefelles barn
        List<BekreftetAksjonspunktDto> aksjonspunktDtoer = byggAvklarFaktaDtoer(behandlingId, adopsjonsdato, true);
        applikasjonstjeneste.bekreftAksjonspunkter(aksjonspunktDtoer, behandlingId);
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.AVSLÅTT,
                asList(resultat(VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD, VilkårUtfallType.IKKE_OPPFYLT, VilkårUtfallMerknad.VM_1005),
                        resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_VURDERT),
                        resultat(VilkårType.SØKNADSFRISTVILKÅRET, VilkårUtfallType.IKKE_VURDERT)));
    }

    private void utførProsessSteg(Long behandlingId) {
        kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);
        repository.flush();
    }

    private List<BekreftetAksjonspunktDto> byggAvklarFaktaDtoer(Long behandlingId,
                                                                             LocalDate omsorgsovertakelseDato, boolean ektefellesBarn) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        Map<Integer, LocalDate> fødselsdatoer = new HashMap<>();
        final FamilieHendelseGrunnlag familieHendelseGrunnlag = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling);
        Optional<UidentifisertBarn> søknadAdopsjonBarn = familieHendelseGrunnlag.getGjeldendeBarna().stream().findFirst();
        if (søknadAdopsjonBarn.isPresent()) {
            int barnId = søknadAdopsjonBarn.get().getBarnNummer();
            fødselsdatoer.put(barnId, LocalDate.now());
            return asList(
                    new BekreftDokumentertDatoAksjonspunktDto("Grunn", omsorgsovertakelseDato, fødselsdatoer),
                    new BekreftEktefelleAksjonspunktDto("Grunn", ektefellesBarn),
                    new BekreftMannAdoptererAksjonspunktDto("Grunn", true));
        }
        return emptyList();
    }

    private Soeknad søknadMorAdopsjon(LocalDate fødselsdato, LocalDate søknadsdato, LocalDate adopsjonsdato) {
        return new SøknadTestdataBuilder().søknadEngangsstønadMor()
            .medMottattdato(søknadsdato)
            .medAdopsjon(new SøknadTestdataBuilder.AdopsjonBuilder()
                .medAdopsjonsdato(adopsjonsdato)
                .medFoedselsdatoer(Collections.singletonList(fødselsdato)))
            .build();
    }

    private Soeknad søknadFarAdopsjon(LocalDate fødselsdato, LocalDate søknadsdato, LocalDate adopsjonsdato) {
        return new SøknadTestdataBuilder().søknadEngangsstønadFar()
            .medMottattdato(søknadsdato)
            .medAdopsjon(new SøknadTestdataBuilder.AdopsjonBuilder()
                .medAdopsjonsdato(adopsjonsdato)
                .medFoedselsdatoer(Collections.singletonList(fødselsdato)))
            .build();
    }

    private SoeknadsskjemaEngangsstoenad søknadFarGammeltFormat(GrunnlagForAnsvarsovertakelse grunnlagForAnsvarsovertakelse,
                                                                LocalDate fødselsdato,
                                                                LocalDate omsorgsovertakelseDato) {
        return new SoeknadsskjemaEngangsstoenadTestdataBuilder()
            .adopsjon()
            .engangsstønadFar()
            .medGrunnlagForAnsvarsovertakelse(grunnlagForAnsvarsovertakelse)
            .medPersonidentifikator(TpsRepo.STD_MANN_FNR)
            .medVedleggsliste(emptyList()) // Kan denne defaultes?
            .medOmsorgsovertakelsesdato(omsorgsovertakelseDato)
            .medFødselsdatoer(Collections.singletonList(fødselsdato))
            .medTidligereOppholdNorge(true)
            .medOppholdNorgeNå(true)
            .medFremtidigOppholdNorge(true)
            .build();
    }

    private Fagsak byggFagsak(AktørId aktørId, RelasjonsRolleType rolle, NavBrukerKjønn kjønn) {
        NavBruker navBruker = new NavBrukerBuilder()
            .medAktørId(aktørId)
            .medKjønn(kjønn)
            .build();
        Fagsak fagsak = FagsakBuilder.nyEngangstønad(rolle)
            .medBruker(navBruker)
            .medSaksnummer(new Saksnummer("132"))
            .build();

        fagsakRepository.opprettNy(fagsak);
        return fagsak;
    }

    private void assertUnikFagsak(RelasjonsRolleType brukerrolle) {
        List<Fagsak> fagsaker = repository.hentAlle(Fagsak.class);
        assertThat(fagsaker).hasSize(1);
        assertThat(fagsaker.get(0).getRelasjonsRolleType()).isEqualTo(brukerrolle);
    }
}
