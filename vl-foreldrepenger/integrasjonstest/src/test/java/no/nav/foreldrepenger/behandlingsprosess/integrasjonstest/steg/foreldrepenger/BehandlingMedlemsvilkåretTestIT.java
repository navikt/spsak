package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.foreldrepenger;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_FORTSATT_MEDLEMSKAP;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.KONTROLL_AV_MANUELT_OPPRETTET_REVURDERINGSBEHANDLING;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.OPPRETTET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.UTFØRT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.BEREGNINGSGRUNNLAGVILKÅR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.FØDSELSVILKÅRET_MOR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.MEDLEMSKAPSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.OPPTJENINGSPERIODEVILKÅR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.OPPTJENINGSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.SØKERSOPPLYSNINGSPLIKT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_OPPFYLT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.OPPFYLT;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder.journalpostInntektsmeldingBuilder;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder.journalpostSøknadBuilder;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.VilkårTestutfall.resultat;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.task.StartBehandlingTask;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapDekningType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapKildeType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapPerioderBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapVilkårPeriodeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapsvilkårPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapsvilkårPeriodeGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapsvilkårPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapsvilkårPerioderEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskapBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittDekningsgradEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.RegisterKontekst;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.ArbeidsforholdTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.FørstegangssøknadTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InntektTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.MedlTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.TpsTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsPerson;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.KjørProsessTasks;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper.AksjonspunktRestTjenesteTestAPI;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper.FordelRestTjenesteTestAPI;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.medlem.api.MedlemskapsperiodeKoder;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.BekreftedeAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.AvklarFortsattMedlemskapDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.AvklarStartdatoForFPperiodenDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;
import no.seres.xsd.nav.inntektsmelding_m._20180924.InntektsmeldingM;

/*
Tester verdikjede fra mottak av foreldrepenger-søknad for Medlemskapsvilkåret inklusiv utføring av aksjonspunkter.
 */
@RunWith(CdiRunner.class)
public class BehandlingMedlemsvilkåretTestIT {
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();
    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);
    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private BehandlingskontrollKontekst kontekst;
    @Inject
    private BehandlingRepository behandlingRepository;
    @Inject
    private BehandlingRepositoryProvider repositoryProvider;
    @Inject
    private YtelsesFordelingRepository ytelsesFordelingRepository;
    @Inject
    private ProsessTaskRepository prosessTaskRepository;
    @Inject
    private MedlemskapVilkårPeriodeRepository medlemskapVilkårPeriodeRepository;
    @Inject
    private BehandlingVedtakRepository behandlingVedtakRepository;
    @Inject
    @FagsakYtelseTypeRef("FP")
    private RevurderingTjeneste revurderingTjeneste;

    @Inject
    @ProsessTask(StartBehandlingTask.TASKTYPE)
    private StartBehandlingTask startBehandlingTask;

    @Inject
    private RegisterKontekst registerKontekst;
    @Inject
    private AksjonspunktRestTjenesteTestAPI aksjonspunktRestTjenesteAPI;
    @Inject
    private FordelRestTjenesteTestAPI fordelRestTjenesteAPI;

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
    public void skal_få_medlemskapsvilkår_oppfylt_dersom_søker_er_registrert_som_utvandret_med_relevant_arbeidsforhold_og_pensjonsgivende_inntekt_i_stp() throws URISyntaxException {
        // Pre-Arrange:
        // Registerdata + fagsak
        // Mor som utvandret med Løpende arbeidsforhold med pensjonsgivende inntekt
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFarMedMorsPersonstatus(fødselsdatoBarn, PersonstatusType.UTVA).getBruker();
        ArbeidsforholdTestSett.løpendeForhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());
        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselStandardUttak(mor.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();

        // Arrange steg 2: Send inn inntektsmelding -> skal tas av vent og innvilge stønad
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // aksjonspunkt 5045 opprettes
        AvklarStartdatoForFPperiodenDto dto = new AvklarStartdatoForFPperiodenDto("Ser greit ut", fødselsdatoBarn);
        BekreftedeAksjonspunkterDto dtoer = BekreftedeAksjonspunkterDto.lagDto(behandlingId, behandling.getVersjon(), singletonList(dto));
        aksjonspunktRestTjenesteAPI.bekreft(dtoer);

        assertUtil.assertVilkårresultatOgRegelmerknad(
            VilkårResultatType.INNVILGET,
            asList(resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT)));
    }

    @Test
    public void skal_få_medlemskapsvilkår_avslått_dersom_søker_er_registrert_som_utvandret_uten_relevant_arbeidsforhold_og_pensjonsgivende_inntekt_i_stp() throws URISyntaxException {
        // Pre-Arrange:
        // Registerdata + fagsak
        // Mor som utvandret med løpende arbeidsforhold uten pensjonsgivende inntekt som dekker skjæringstidspunktet
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFarMedMorsPersonstatus(fødselsdatoBarn, PersonstatusType.UTVA).getBruker();
        ArbeidsforholdTestSett.løpendeForhold100prosent40timer(mor.getFnr());
        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselStandardUttak(mor.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();

        // Arrange steg 2: Send inn inntektsmelding -> skal tas av vent og innvilge stønad
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // aksjonspunkt 5045 opprettes
        AvklarStartdatoForFPperiodenDto dto = new AvklarStartdatoForFPperiodenDto("Bare gå videre", fødselsdatoBarn);
        BekreftedeAksjonspunkterDto dtoer = BekreftedeAksjonspunkterDto.lagDto(behandlingId, behandling.getVersjon(), singletonList(dto));
        aksjonspunktRestTjenesteAPI.bekreft(dtoer);

        assertUtil.assertVilkårresultatOgRegelmerknad(
            VilkårResultatType.AVSLÅTT, resultat(MEDLEMSKAPSVILKÅRET, IKKE_OPPFYLT, VilkårUtfallMerknad.VM_1021)
        );
    }

    @Test
    public void skal_få_medlemskapsvilkår_avslått_dersom_søker_er_registrert_som_utvandret_uten_arbeidsforhold_i_stp() throws URISyntaxException {
        // Pre-Arrange:
        // Registerdata + fagsak
        // Mor som utvandret (ingen arbeidsforhold)
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFarMedMorsPersonstatus(fødselsdatoBarn, PersonstatusType.UTVA).getBruker();
        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselStandardUttak(mor.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        assertUtil.assertVilkårresultatOgRegelmerknad(
            VilkårResultatType.AVSLÅTT, resultat(MEDLEMSKAPSVILKÅRET, IKKE_OPPFYLT, VilkårUtfallMerknad.VM_1021)
        );
    }

    @Test
    public void skal_få_medlemskapsvilkår_avslått_dersom_søker_er_er_avklart_som_ikke_bosatt_med_ingen_relevant_inntekt() throws URISyntaxException {
        // Pre-Arrange:
        // Registerdata + fagsak
        // Mor som utvandret med løpende arbeidsforhold uten pensjonsgivende inntekt som dekker skjæringstidspunktet
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        ArbeidsforholdTestSett.løpendeForhold100prosent40timer(mor.getFnr());
        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselStandardUttak(mor.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();

        // Arrange steg 2: Send inn inntektsmelding -> skal tas av vent og innvilge stønad
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);
        simulerBrukerAvklartIkkeBosatt(behandlingId);

        // aksjonspunkt 5045 opprettes
        AvklarStartdatoForFPperiodenDto dto = new AvklarStartdatoForFPperiodenDto("Bare gå videre", fødselsdatoBarn);
        BekreftedeAksjonspunkterDto dtoer = BekreftedeAksjonspunkterDto.lagDto(behandlingId, behandling.getVersjon(), singletonList(dto));
        aksjonspunktRestTjenesteAPI.bekreft(dtoer);

        assertUtil.assertVilkårresultatOgRegelmerknad(
            VilkårResultatType.AVSLÅTT, resultat(MEDLEMSKAPSVILKÅRET, IKKE_OPPFYLT, VilkårUtfallMerknad.VM_1025)
        );
    }

    @Test
    public void skal_få_medlemskapsvilkår_oppfylt_dersom_søker_er_er_avklart_som_ikke_bosatt_med_relevant_arbeidsforhold_og_inntekt() throws URISyntaxException {
        // Pre-Arrange:
        // Registerdata + fagsak
        // Mor som utvandret med løpende arbeidsforhold uten pensjonsgivende inntekt som dekker skjæringstidspunktet
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        ArbeidsforholdTestSett.løpendeForhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());
        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselStandardUttak(mor.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();

        // Arrange steg 2: Send inn inntektsmelding -> skal tas av vent og innvilge stønad
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);
        simulerBrukerAvklartIkkeBosatt(behandlingId);

        // aksjonspunkt 5045 opprettes
        AvklarStartdatoForFPperiodenDto dto = new AvklarStartdatoForFPperiodenDto("Bare gå videre", fødselsdatoBarn);
        BekreftedeAksjonspunkterDto dtoer = BekreftedeAksjonspunkterDto.lagDto(behandlingId, behandling.getVersjon(), singletonList(dto));
        aksjonspunktRestTjenesteAPI.bekreft(dtoer);

        assertUtil.assertVilkårresultatOgRegelmerknad(
            VilkårResultatType.INNVILGET, resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT)
        );
    }


    @Test
    public void skal_ikke_opprette_aksjonspunkt_dersom_det_ikke_er_endring_i_medlemskap_fortsatt_medlem() {
        // Pre-Arrange: Registerdata
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timerV2(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());

        LocalDate fom = fødselsdatoBarn.minusYears(1);
        LocalDate tom = fødselsdatoBarn.plusYears(1);
        MedlTestSett.dekningUavklart(mor.getPersonIdent(), fom, tom, MedlemskapsperiodeKoder.Lovvalg.UAVK);

        // Arrange
        // Opprinnelig fom/tom for dekning er uendret
        Behandling originalBehandling = opprettOgAvsluttFørstegangsbehandling(mor, fødselsdatoBarn, fom, tom);

        // Act - oppretter manuell revurdering
        // TODO (essv): Jan, kall må erstattes med eksternt api
        revurderingTjeneste.opprettManuellRevurdering(originalBehandling.getFagsak(), BehandlingÅrsakType.RE_OPPLYSNINGER_OM_MEDLEMSKAP);
        kjørProsesstaskStartBehandling(originalBehandling);

        // Assert - Vi har ingen endringer i medlemskapsperioder, så verifiser at aksjonspunktet for avklar fortsatt medlemskap IKKE er opprettet.
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT),
            AksjonspunktTestutfall.resultat(AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE, OPPRETTET),
            AksjonspunktTestutfall.resultat(KONTROLL_AV_MANUELT_OPPRETTET_REVURDERINGSBEHANDLING, OPPRETTET)
            );
    }

    @Ignore("OJR: fix når du har løfte ny løsning")
    @Test
    public void skal_opprette_aksjonspunkt_dersom_endring_i_medlemskap_fortsatt_medlem() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now();
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timerV2(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());
        LocalDate fom = fødselsdatoBarn.minusYears(1);
        LocalDate tom = fødselsdatoBarn.minusDays(1);
        MedlTestSett.dekningUavklart(mor.getPersonIdent(), fom, tom, MedlemskapsperiodeKoder.Lovvalg.UAVK);

        // Arrange
        // Opprinnelig fom/tom for dekning er endret
        LocalDate opprinneligTom = fødselsdatoBarn.plusYears(1);
        Behandling originalBehandling = opprettOgAvsluttFørstegangsbehandling(mor, fødselsdatoBarn, fom, opprinneligTom);

        // For å simulere endring i RegistrertMedlemskapPerioder så endrer vi medlemskapet lagret lokat slik at vi får diff mot registeret.
//        simulerEndringIRegistrertMedlemskapPerioder(originalBehandling);

        // Act - oppretter manuell revurdering
        revurderingTjeneste.opprettManuellRevurdering(originalBehandling.getFagsak(), BehandlingÅrsakType.RE_OPPLYSNINGER_OM_MEDLEMSKAP);
        kjørProsesstaskStartBehandling(originalBehandling);

        // Assert - Vi har endring i medlemskapsperioder, så verifiser at nye aksjonspunktet for avklar fortsatt medlemskap er opprettet
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT),
            AksjonspunktTestutfall.resultat(KONTROLL_AV_MANUELT_OPPRETTET_REVURDERINGSBEHANDLING, OPPRETTET),
            AksjonspunktTestutfall.resultat(AVKLAR_FORTSATT_MEDLEMSKAP, OPPRETTET));

        Behandling revurdering = behandlingRepository.hentSisteBehandlingForFagsakId(originalBehandling.getFagsakId())
            .orElseThrow(() -> new IllegalStateException("Finner ikke revurdering behandling."));

        // Avklar fakta fra GUI
        List<BekreftetAksjonspunktDto> aksjonspunktDtoer = byggAksjonspunktSvarDtoer(LocalDate.now().plusYears(2));
        BekreftedeAksjonspunkterDto dtoRot = BekreftedeAksjonspunkterDto.lagDto(revurdering.getId(), revurdering.getVersjon(), aksjonspunktDtoer);
        aksjonspunktRestTjenesteAPI.bekreft(dtoRot);

        // Verifiser at MedlemskapsvilkårPeriodeEntitet er oppdatert. Se VurderMedlemskapvilkårRevurderingStegFPImpl
        Optional<MedlemskapsvilkårPeriodeGrunnlag> funnetAggregat = medlemskapVilkårPeriodeRepository.hentAggregatHvisEksisterer(revurdering);
        assertThat(funnetAggregat).isPresent();
        MedlemskapsvilkårPeriodeGrunnlag aggregat = funnetAggregat.get();

        List<MedlemskapsvilkårPerioder> perioder = aggregat.getMedlemskapsvilkårPeriode().getPerioder().stream()
            .sorted(Comparator.comparing(MedlemskapsvilkårPerioder::getFom))
            .collect(toList());
        assertThat(perioder).hasSize(2);
        assertThat(perioder.get(0).getTom()).isEqualTo(LocalDate.now().plusYears(2).minusDays(1));
        assertThat(perioder.get(1).getFom()).isEqualTo(LocalDate.now().plusYears(2));
    }

    private Behandling opprettOgAvsluttFørstegangsbehandling(TpsPerson mor, LocalDate fødselsdato, LocalDate fom, LocalDate tom){

        ScenarioMorSøkerForeldrepenger førstegangScenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD);
        førstegangScenario
            .medBruker(mor.getAktørId(), NavBrukerKjønn.KVINNE)
            .medSøknadHendelse()
            .medAntallBarn(1)
            .medFødselsDato(fødselsdato);
        førstegangScenario.medSøknad()
            .medRelasjonsRolleType(RelasjonsRolleType.MORA)
            .medMottattDato(fødselsdato);

        RegistrertMedlemskapPerioder medlemskapPeriode = new MedlemskapPerioderBuilder()
            .medPeriode(fom,tom)
            .medMedlId(123L)
            .medLovvalgLand(Landkoder.NOR)
            .medDekningType(MedlemskapDekningType.OPPHOR)
            .medMedlemskapType(MedlemskapType.UNDER_AVKLARING)
            .medKildeType(MedlemskapKildeType.LAANEKASSEN)
            .medErMedlem(true)
            .build();
        førstegangScenario.leggTilMedlemskapPeriode(medlemskapPeriode);

        førstegangScenario.medVilkårResultatType(VilkårResultatType.INNVILGET)
            .leggTilVilkår(FØDSELSVILKÅRET_MOR, OPPFYLT)
            .leggTilVilkår(MEDLEMSKAPSVILKÅRET, OPPFYLT)
            .leggTilVilkår(OPPTJENINGSPERIODEVILKÅR, OPPFYLT)
            .leggTilVilkår(OPPTJENINGSVILKÅRET, OPPFYLT)
            .leggTilVilkår(SØKERSOPPLYSNINGSPLIKT, OPPFYLT)
            .leggTilVilkår(BEREGNINGSGRUNNLAGVILKÅR, OPPFYLT);

        førstegangScenario.medBehandlingVedtak()
            .medVedtaksdato(LocalDate.now())
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("Nav")
            .build();

        førstegangScenario.medFordeling(lagOppgittFordeling(fødselsdato));
        førstegangScenario.medOppgittRettighet(new OppgittRettighetEntitet(true,true,true));

        Behandling behandling =  førstegangScenario.lagre(repositoryProvider);
        ytelsesFordelingRepository.lagre(behandling,OppgittDekningsgradEntitet.bruk100());

        MedlemskapsvilkårPeriodeGrunnlag.Builder builder = medlemskapVilkårPeriodeRepository.hentBuilderFor(behandling);
        MedlemskapsvilkårPerioderEntitet.Builder vurdertMedlemskap = builder.getBuilderForVurderingsdato(LocalDate.now());
        vurdertMedlemskap.medVilkårUtfall(VilkårUtfallType.OPPFYLT);
        builder.leggTilMedlemskapsvilkårPeriode(vurdertMedlemskap);
        medlemskapVilkårPeriodeRepository.lagreMedlemskapsvilkår(behandling, builder);

        avsluttBehandlingOgFagsak(behandling, FagsakStatus.LØPENDE);

        // Assert tilstand

        // Verifiser at VurderMedlemskapvilkårStegFPImpl har lagret skjæringstidspunkt i fom feltet på MedlemskapsvilkårPeriodeEntitet.
        List<MedlemskapsvilkårPeriodeEntitet> aggregater = repository.hentAlle(MedlemskapsvilkårPeriodeEntitet.class);
        assertThat(aggregater).hasSize(1);
        Set<? extends MedlemskapsvilkårPerioder> perioder = aggregater.get(0).getPerioder();
        assertThat(perioder).hasSize(1);
        MedlemskapsvilkårPerioder periode = perioder.iterator().next();
        assertThat(periode.getFom()).as("Forventet at fom-dato og utfall er satt på medlemskapsvilkårPerioden").isNotNull();

        assertUnikFagsak(RelasjonsRolleType.MORA);
        assertUnikFødsel(fødselsdato, behandling.getId());
        return behandling;

    }

    private OppgittFordelingEntitet lagOppgittFordeling(LocalDate fødselsdato) {
        List<OppgittPeriode> oppgittPerioder = asList(OppgittPeriodeBuilder.ny()
            .medPeriode(fødselsdato.minusWeeks(3),fødselsdato.minusDays(1))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL).build(),
            OppgittPeriodeBuilder.ny()
                .medPeriode(fødselsdato,fødselsdato.plusWeeks(10).minusDays(1))
                .medPeriodeType(UttakPeriodeType.MØDREKVOTE).build(),
            OppgittPeriodeBuilder.ny()
                .medPeriode(fødselsdato.plusWeeks(10),fødselsdato.plusWeeks(25))
                .medPeriodeType(UttakPeriodeType.FELLESPERIODE).build());

        return new OppgittFordelingEntitet(oppgittPerioder,false);
    }

    private List<BekreftetAksjonspunktDto> byggAksjonspunktSvarDtoer(LocalDate fom) {
        return asList(new AvklarFortsattMedlemskapDto("Grunn", fom));
    }

    private void avsluttBehandlingOgFagsak(Behandling behandling, FagsakStatus fagsakStatus) {
        behandling.avsluttBehandling();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);

        FagsakRepository fagsakRepository = repositoryProvider.getFagsakRepository();
        fagsakRepository.oppdaterFagsakStatus(behandling.getFagsakId(), fagsakStatus);
    }

    private void utførProsessSteg(Long behandlingId) {
        kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);
        repository.flushAndClear();
    }

    private void kjørProsesstaskStartBehandling(Behandling originalBehandling) {
        repoRule.getEntityManager().flush();
        prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR).stream()
            .filter(p -> StartBehandlingTask.TASKTYPE.equals(p.getTaskType()))
            .forEach(p -> startBehandlingTask.doTask(p));

        // Må ta saken av vent for registeropplysninger
        Behandling revurdering = behandlingRepository.hentSisteBehandlingForFagsakId(originalBehandling.getFagsakId())
            .orElseThrow(() -> new IllegalStateException("Skal ikke kunne havne her"));
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(revurdering.getId());
        utførProsessSteg(revurdering.getId());

        // Medlemsinnhenting foregår i IRYTask så den må kjøres fulgt av FortsettBehandling
        new KjørProsessTasks(prosessTaskRepository).utførTasks();
    }

    private void assertUnikFagsak(RelasjonsRolleType brukerrolle) {
        List<Fagsak> fagsaker = repository.hentAlle(Fagsak.class);
        assertThat(fagsaker).hasSize(1);
        assertThat(fagsaker.get(0).getRelasjonsRolleType().getKode()).isEqualTo(brukerrolle.getKode());
    }

    private void assertUnikFødsel(LocalDate fødselsdato, Long behandlingId) {
        final List<UidentifisertBarn> barna = repositoryProvider.getFamilieGrunnlagRepository()
            .hentAggregat(behandlingRepository.hentBehandling(behandlingId)).getGjeldendeVersjon().getBarna();
        assertThat(barna).hasSize(1);
        assertThat(barna.get(0).getFødselsdato()).isEqualTo(fødselsdato);
    }

    private void simulerBrukerAvklartIkkeBosatt(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        VurdertMedlemskap vurdertMedlemskap = new VurdertMedlemskapBuilder()
            .medOppholdsrettVurdering(true)
            .medLovligOppholdVurdering(true)
            .medBosattVurdering(false)
            .build();

        MedlemskapRepository medlemskapRepository = repositoryProvider.getMedlemskapRepository();

        // Act
        medlemskapRepository.lagreMedlemskapVurdering(behandling, vurdertMedlemskap);
    }
}
