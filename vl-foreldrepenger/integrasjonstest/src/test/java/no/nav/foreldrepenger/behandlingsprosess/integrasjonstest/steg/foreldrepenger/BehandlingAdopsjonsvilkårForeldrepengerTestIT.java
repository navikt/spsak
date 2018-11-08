package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.foreldrepenger;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENTER_PÅ_KOMPLETT_SØKNAD;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_ADOPSJONSDOKUMENTAJON;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.UTFØRT;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder.journalpostInntektsmeldingBuilder;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder.journalpostSøknadBuilder;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.VilkårTestutfall.resultat;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.RegisterKontekst;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.søknad.SøknadTestdataBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.ArbeidsforholdTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InntektTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.TpsTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsPerson;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper.AksjonspunktRestTjenesteTestAPI;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper.FordelRestTjenesteTestAPI;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.BekreftedeAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.BekreftDokumentertDatoAksjonspunktDto;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;
import no.seres.xsd.nav.inntektsmelding_m._20180924.InntektsmeldingM;

@RunWith(CdiRunner.class)
public class BehandlingAdopsjonsvilkårForeldrepengerTestIT {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();
    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);

    @Inject
    private BehandlingRepository behandlingRepository;
    @Inject
    private BehandlingRepositoryProvider repositoryProvider;
    @Inject
    private FordelRestTjenesteTestAPI fordelRestTjenesteAPI;
    @Inject
    private AksjonspunktRestTjenesteTestAPI aksjonspunktRestTjenesteAPI;
    @Inject
    private RegisterKontekst registerKontekst;

    @Before
    public void setup() {
        registerKontekst.intialiser();
        // setter verdien slik at regler blir kjørt
        System.setProperty("dato.for.nye.beregningsregler", "2010-01-01");
    }

    @After
    public void teardown() {
        registerKontekst.nullstill();
        System.setProperty("dato.for.nye.beregningsregler", "2019-01-01");
    }

    @Test
    public void adopsjon_kvinne_happy_case() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(1000);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_ADOPSJON);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        LocalDate overtakelseDato = LocalDate.now();

        Soeknad soeknad = søknadAdopsjon(ForeldreType.MOR, mor.getAktørId(), fødselsdatoBarn, overtakelseDato, true)
            .build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();

        // Arrange steg 2: Send inn inntektsmelding -> skal tas av vent og opprette aksjonspunkter for adopsjon
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        assertUnikFagsak(RelasjonsRolleType.MORA);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(
                resultat(VilkårType.OPPTJENINGSPERIODEVILKÅR, VilkårUtfallType.IKKE_VURDERT),
                resultat(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.IKKE_VURDERT),
                resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_VURDERT),
                resultat(VilkårType.ADOPSJONSVILKARET_FORELDREPENGER, VilkårUtfallType.IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(AVKLAR_ADOPSJONSDOKUMENTAJON, AksjonspunktStatus.OPPRETTET),
            AksjonspunktTestutfall.resultat(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN, AksjonspunktStatus.OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
    }

    @Test
    public void adopsjon_mann_happy_case() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(1000);
        TpsPerson far = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getMedforelder().get();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(far.getFnr());
        InntektTestSett.inntekt36mnd40000kr(far.getPersonIdent());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(far.getAktørId(), BehandlingTema.FORELDREPENGER_ADOPSJON);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        LocalDate adopsjonsdato = LocalDate.now();
        Soeknad soeknad = søknadAdopsjon(ForeldreType.FAR, far.getAktørId(), fødselsdatoBarn, adopsjonsdato, false).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();

        // Arrange steg 2: Send inn inntektsmelding -> skal tas av vent og opprette aksjonspunkter for adopsjon
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(far.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        assertUnikFagsak(RelasjonsRolleType.FARA);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(
                resultat(VilkårType.OPPTJENINGSPERIODEVILKÅR, VilkårUtfallType.IKKE_VURDERT),
                resultat(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.IKKE_VURDERT),
                resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_VURDERT),
                resultat(VilkårType.ADOPSJONSVILKARET_FORELDREPENGER, VilkårUtfallType.IKKE_VURDERT)));

        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(AVKLAR_ADOPSJONSDOKUMENTAJON, AksjonspunktStatus.OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        // Steg 3: Avklar fakta fra GUI
        List<BekreftetAksjonspunktDto> aksjonspunktDtoer = byggAvklarFaktaDtoer(behandlingId, adopsjonsdato, fødselsdatoBarn);
        BekreftedeAksjonspunkterDto dto = BekreftedeAksjonspunkterDto.lagDto(behandlingId, behandling.getVersjon(), aksjonspunktDtoer);

        // Act
        aksjonspunktRestTjenesteAPI.bekreft(dto);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(VilkårType.ADOPSJONSVILKARET_FORELDREPENGER, VilkårUtfallType.OPPFYLT)));
    }

    @Test
    public void adopsjon_avslås_når_mann_adoptere_ektefelles_barn() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(1000);
        TpsPerson far = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getMedforelder().get();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(far.getFnr());
        InntektTestSett.inntekt36mnd40000kr(far.getPersonIdent());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(far.getAktørId(), BehandlingTema.FORELDREPENGER_ADOPSJON);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        LocalDate adopsjonsdato = LocalDate.now();
        Soeknad soeknad = søknadAdopsjon(ForeldreType.FAR, far.getAktørId(), fødselsdatoBarn, adopsjonsdato, true).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();

        // Arrange steg 2: Send inn inntektsmelding -> skal tas av vent og opprette aksjonspunkter for adopsjon
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(far.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        assertUnikFagsak(RelasjonsRolleType.FARA);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(
                resultat(VilkårType.OPPTJENINGSPERIODEVILKÅR, VilkårUtfallType.IKKE_VURDERT),
                resultat(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.IKKE_VURDERT),
                resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_VURDERT),
                resultat(VilkårType.ADOPSJONSVILKARET_FORELDREPENGER, VilkårUtfallType.IKKE_VURDERT)));

        /*assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(AVKLAR_ADOPSJONSDOKUMENTAJON, AksjonspunktStatus.OPPRETTET),
            AksjonspunktTestutfall.resultat(AVKLAR_OM_SØKER_ER_MANN_SOM_ADOPTERER_ALENE, AksjonspunktStatus.OPPRETTET),
            AksjonspunktTestutfall.resultat(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN, AksjonspunktStatus.OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertThat(repository.hentAlle(Beregning.class)).hasSize(0);

        // Steg 2: Avklar fakta fra GUI
        List<BekreftetAksjonspunktDto> aksjonspunktDtoer = byggAvklarFaktaDtoer(behandlingId, adopsjonsdato, fødselsdato);
        applikasjonstjeneste.bekreftAksjonspunkter(aksjonspunktDtoer, behandlingId);
        utførProsessSteg(behandlingId);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.AVSLÅTT,
            asList(resultat(VilkårType.ADOPSJONSVILKARET_FORELDREPENGER, VilkårUtfallType.IKKE_OPPFYLT, VilkårUtfallMerknad.VM_1005)));*/
    }

    @Test
    public void adopsjon_avslås_når_barn_over_15_år() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusYears(16);
        TpsPerson far = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getMedforelder().get();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(far.getFnr());
        InntektTestSett.inntekt36mnd40000kr(far.getPersonIdent());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(far.getAktørId(), BehandlingTema.FORELDREPENGER_ADOPSJON);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        LocalDate adopsjonsdato = LocalDate.now();
        Soeknad soeknad = søknadAdopsjon(ForeldreType.FAR, far.getAktørId(), fødselsdatoBarn, adopsjonsdato, false).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();

        // Arrange steg 2: Send inn inntektsmelding -> skal tas av vent og opprette aksjonspunkter for adopsjon
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(far.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        assertUnikFagsak(RelasjonsRolleType.FARA);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(
                resultat(VilkårType.OPPTJENINGSPERIODEVILKÅR, VilkårUtfallType.IKKE_VURDERT),
                resultat(VilkårType.OPPTJENINGSVILKÅRET, VilkårUtfallType.IKKE_VURDERT),
                resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_VURDERT),
                resultat(VilkårType.ADOPSJONSVILKARET_FORELDREPENGER, VilkårUtfallType.IKKE_VURDERT)
                ));

        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(AVKLAR_ADOPSJONSDOKUMENTAJON, AksjonspunktStatus.OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        // Steg 3: Avklar fakta fra GUI
        List<BekreftetAksjonspunktDto> aksjonspunktDtoer = byggAvklarFaktaDtoer(behandlingId, adopsjonsdato, fødselsdatoBarn);
        BekreftedeAksjonspunkterDto dto = BekreftedeAksjonspunkterDto.lagDto(behandlingId, behandling.getVersjon(), aksjonspunktDtoer);

        // Act
        aksjonspunktRestTjenesteAPI.bekreft(dto);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.AVSLÅTT,
            asList(resultat(VilkårType.ADOPSJONSVILKARET_FORELDREPENGER, VilkårUtfallType.IKKE_OPPFYLT, VilkårUtfallMerknad.VM_1004)));
    }

    private SøknadTestdataBuilder søknadAdopsjon(ForeldreType type, AktørId aktørId, LocalDate fødselsdato, LocalDate adopsjonsdato, boolean ektefellesBarn) {
        return new SøknadTestdataBuilder().søknadForeldrepenger()
            .medSøker(type, aktørId)
            .medMottattdato(LocalDate.now())
            .medAdopsjon(new SøknadTestdataBuilder.AdopsjonBuilder()
                .medAdopsjonsdato(adopsjonsdato)
                .medEktefellesBarn(ektefellesBarn)
                .medFoedselsdatoer(Collections.singletonList(fødselsdato)))
            .medFordeling(new SøknadTestdataBuilder.FordelingBuilder()
                .leggTilPeriode(adopsjonsdato, adopsjonsdato.plusWeeks(25).minusDays(1), UttakPeriodeType.FELLESPERIODE)
                .setAnnenForelderErInformert(true));
    }

    private List<BekreftetAksjonspunktDto> byggAvklarFaktaDtoer(Long behandlingId,
                                                                LocalDate omsorgsovertakelseDato, LocalDate fødselsdato) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        Map<Integer, LocalDate> fødselsdatoer = new HashMap<>();
        final FamilieHendelseGrunnlag familieHendelseGrunnlag = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling);
        Optional<UidentifisertBarn> søknadAdopsjonBarn = familieHendelseGrunnlag.getGjeldendeBarna().stream().findFirst();
        if (søknadAdopsjonBarn.isPresent()) {
            int barnId = søknadAdopsjonBarn.get().getBarnNummer();
            fødselsdatoer.put(barnId, fødselsdato);
            return asList(
                new BekreftDokumentertDatoAksjonspunktDto("Grunn", omsorgsovertakelseDato, fødselsdatoer));
        }
        return emptyList();
    }

    private void assertUnikFagsak(RelasjonsRolleType brukerrolle) {
        List<Fagsak> fagsaker = repository.hentAlle(Fagsak.class);
        assertThat(fagsaker).hasSize(1);
        assertThat(fagsaker.get(0).getRelasjonsRolleType()).isEqualTo(brukerrolle);
    }

}
