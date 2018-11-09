package no.nav.foreldrepenger.vedtak.xml;

import static java.time.Month.JANUARY;
import static no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn.KVINNE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.KONTROLLER_OPPLYSNINGER_OM_MEDLEMSKAP;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.SØKERS_OPPLYSNINGSPLIKT_MANU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageAvvistÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageMedholdÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioKlageEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.BehandlingsresultatXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.beregningsgrunnlag.BeregningsgrunnlagXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.ytelse.YtelseXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.vilkår.VilkårsgrunnlagXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.personopplysninger.PersonopplysningXmlTjenesteEngangsstønad;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;

@RunWith(CdiRunner.class)
public class VedtakXmlTjenesteEngangsstønadTest {
    private static final String KLAGE_BEGRUNNELSE = "Begrunnelse for klagevurdering er bla.bla.bla.";
    private static final String BEHANDLENDE_ENHET_ID = "1234";
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    @Inject
    InternalManipulerBehandling manipulerInternBehandling;
    private Repository repository = repoRule.getRepository();
    private LocalDate morsFødseldato;
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    private BeregningRepository beregningRepository = new BeregningRepositoryImpl(repoRule.getEntityManager());

    private FagsakRepository fagsakRepository = new FagsakRepositoryImpl(entityManager);
    @Mock
    private PersoninfoAdapter personinfoAdapter;
    private Personinfo personinfoMor;
    private Søknad søknad;
    private SøknadRepository søknadRepository;
    private AksjonspunktRepository aksjonspunktRepository = new AksjonspunktRepositoryImpl(repoRule.getEntityManager());

    //XML tjenestene
    @Inject
    @FagsakYtelseTypeRef("ES")
    private VedtakXmlTjeneste tjeneste;
    @Inject
    private PersonopplysningXmlTjenesteEngangsstønad personopplysningXmlTjeneste;
    @Inject
    @FagsakYtelseTypeRef("ES")
    private BehandlingsresultatXmlTjeneste behandlingsgrunnlagXmlTjeneste;
    @Inject
    @FagsakYtelseTypeRef("ES")
    private VilkårsgrunnlagXmlTjeneste vilkårsgrunnlagXmlTjeneste;
    @Inject
    @FagsakYtelseTypeRef("ES")
    private BeregningsgrunnlagXmlTjeneste beregningsgrunnlagXmlTjeneste;

    @Inject
    @FagsakYtelseTypeRef("ES")
    private YtelseXmlTjeneste ytelseXmlTjeneste;

    @Before
    public void oppsett() {
        morsFødseldato = LocalDate.of(1990, JANUARY, 1);
        personinfoMor = new Personinfo.Builder()
            .medAktørId(new AktørId("123"))
            .medPersonIdent(new PersonIdent("12345678901"))
            .medNavn("Kari Nordmann")
            .medFødselsdato(morsFødseldato)
            .medNavBrukerKjønn(KVINNE)
            .medForetrukketSpråk(Språkkode.nb)
            .build();
        søknadRepository = mock(SøknadRepository.class);

        søknad = new SøknadEntitet.Builder().medMottattDato(LocalDate.now()).medSøknadsdato(LocalDate.now()).build();
        when(søknadRepository.hentSøknadHvisEksisterer(any(Behandling.class))).thenReturn(Optional.ofNullable(søknad));
        when(personinfoAdapter.innhentSaksopplysningerForSøker(any(AktørId.class))).thenReturn(personinfoMor);

    }

    private Fagsak opprettFagsak() {
        NavBruker søker = NavBruker.opprettNy(personinfoMor);
        final Fagsak fagsak = Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, søker);
        fagsakRepository.opprettNy(fagsak);
        return fagsak;
    }

    @Test
    public void skal_opprette_xml_med_termindato() {

        Behandling behandling = opprettBehandlingMedTermindato(BehandlingStegType.FATTE_VEDTAK);
        oppdaterMedBehandlingsresultat(behandling, true);

        try {
            // Act
            String xml = tjeneste.opprettVedtakXml(behandling.getId());
            // Assert
            assertNotNull(xml);
        } catch (VLException e) {
            fail();
        }
    }

    @Test
    public void skal_opprette_xml_med_termindato_avslag() {

        Behandling behandling = opprettBehandlingMedTermindato(BehandlingStegType.FATTE_VEDTAK);
        oppdaterMedBehandlingsresultat(behandling, false);

        try {
            // Act
            String xml = tjeneste.opprettVedtakXml(behandling.getId());
            // Assert
            assertNotNull(xml);
        } catch (VLException e) {
            fail();
        }
    }

    @Test
    public void skal_opprette_xml_med_adopsjon() {

        Behandling behandling = opprettBehandlingMedAdopsjon(BehandlingStegType.FATTE_VEDTAK);
        oppdaterMedBehandlingsresultat(behandling, true);

        try {
            // Act
            String xml = tjeneste.opprettVedtakXml(behandling.getId());
            // Assert
            assertNotNull(xml);
        } catch (VLException e) {
            fail();
        }
    }

    @Test
    public void skal_opprette_xml_med_fødsel() {

        Behandling behandling = opprettBehandlingMedFødsel(BehandlingStegType.FATTE_VEDTAK);
        oppdaterMedBehandlingsresultat(behandling, true);

        try {
            // Act
            String xml = tjeneste.opprettVedtakXml(behandling.getId());
            // Assert
            assertNotNull(xml);
        } catch (VLException e) {
            fail();
        }
    }

    @Test
    public void skal_opprette_xml_med_klage_avvist() {
        // Arrange
        Behandling behandling = opprettKlageBehandling(ScenarioKlageEngangsstønad.forAvvistNFP(ScenarioMorSøkerEngangsstønad.forAdopsjon()), KlageAvvistÅrsak.KLAGET_FOR_SENT, null);
        try {
            // Act
            String xml = tjeneste.opprettVedtakXml(behandling.getId());
            // Assert
            assertNotNull(xml);
        } catch (VLException e) {
            fail();
        }
    }

    @Test
    public void skal_opprette_xml_med_klage_medhold() {
        // Arrange
        Behandling behandling = opprettKlageBehandling(ScenarioKlageEngangsstønad.forMedholdNFP(ScenarioMorSøkerEngangsstønad.forAdopsjon()), null, KlageMedholdÅrsak.NYE_OPPLYSNINGER);
        try {
            // Act
            String xml = tjeneste.opprettVedtakXml(behandling.getId());
            // Assert
            assertNotNull(xml);
        } catch (VLException e) {
            fail();
        }
    }

    @Test
    public void skal_opprette_xml_med_klage_oppheve_ytelsesvedtak() {
        // Arrange
        Behandling behandling = opprettKlageBehandling(ScenarioKlageEngangsstønad.forOpphevetNK(ScenarioMorSøkerEngangsstønad.forAdopsjon())
            , null, KlageMedholdÅrsak.PROSESSUELL_FEIL);
        try {
            // Act
            String xml = tjeneste.opprettVedtakXml(behandling.getId());
            // Assert
            assertNotNull(xml);
        } catch (VLException e) {
            fail();
        }
    }

    @Test
    public void skal_opprette_xml_med_klage_stadfeste_ytelsesvedtak() {
        // Arrange
        final ScenarioMorSøkerEngangsstønad adopsjon = ScenarioMorSøkerEngangsstønad.forAdopsjon();
        adopsjon.medSøknadHendelse()
            .medAdopsjon(adopsjon.medSøknadHendelse().getAdopsjonBuilder()
                .medOmsorgsovertakelseDato(LocalDate.now().plusDays(40)))
            .medAntallBarn(3);
        Behandling behandling = opprettKlageBehandling(ScenarioKlageEngangsstønad.forStadfestetNK(adopsjon)
            , null, null);


        try {
            // Act
            String xml = tjeneste.opprettVedtakXml(behandling.getId());
            // Assert
            assertNotNull(xml);
        } catch (VLException e) {
            fail();
        }
    }

    private Behandling opprettKlageBehandling(ScenarioKlageEngangsstønad scenario, KlageAvvistÅrsak klageAvvistÅrsak, KlageMedholdÅrsak klageMedholdÅrsak) {
        Behandling behandling = scenario.medKlageAvvistÅrsak(klageAvvistÅrsak).medKlageMedholdÅrsak(klageMedholdÅrsak)
            .medBegrunnelse(KLAGE_BEGRUNNELSE).medBehandlendeEnhet(BEHANDLENDE_ENHET_ID).lagre(repositoryProvider);

        Behandlingsresultat behandlingsresultat = opprettBehandlingsresultat(behandling, BehandlingResultatType.INNVILGET);
        repository.lagre(behandlingsresultat);

        Beregning beregning = new Beregning(1L, 1L, 1L, LocalDateTime.now());
        BeregningResultat beregningResultat = BeregningResultat.builder().medBeregning(beregning).buildFor(behandling);
        beregningRepository.lagre(beregningResultat, behandlingRepository.taSkriveLås(behandling));

        return behandling;
    }

    private Behandling opprettBehandlingMedTermindato(BehandlingStegType behandlingStegType) {
        Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(opprettFagsak());

        final Behandling behandling = behandlingBuilder.medBehandlendeEnhet(new OrganisasjonsEnhet(BEHANDLENDE_ENHET_ID, null)).build();
        Behandlingsresultat behandlingsresultat = opprettBehandlingsresultat(behandling, BehandlingResultatType.INNVILGET);
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        opprettBehandlingsvedtak(behandling, behandlingsresultat);
        Beregning beregning = new Beregning(1L, 1L, 1L, LocalDateTime.now());
        BeregningResultat beregningResultat = BeregningResultat.builder().medBeregning(beregning).buildFor(behandling);
        beregningRepository.lagre(beregningResultat, behandlingRepository.taSkriveLås(behandling));
        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, behandlingStegType);
        final FamilieHendelseRepository fgRepo = repositoryProvider.getFamilieGrunnlagRepository();
        final FamilieHendelseBuilder søknadVersjon = fgRepo.opprettBuilderFor(behandling);
        søknadVersjon
            .medTerminbekreftelse(søknadVersjon.getTerminbekreftelseBuilder()
                .medNavnPå("Legen min")
                .medTermindato(LocalDate.now().plusDays(40))
                .medUtstedtDato(LocalDate.now()))
            .medAntallBarn(1);
        fgRepo.lagre(behandling, søknadVersjon);
        final FamilieHendelseBuilder hendelseBuilder = fgRepo.opprettBuilderFor(behandling);
        hendelseBuilder
            .medTerminbekreftelse(hendelseBuilder.getTerminbekreftelseBuilder()
                .medTermindato(LocalDate.now().plusDays(40))
                .medNavnPå("Legen min")
                .medUtstedtDato(LocalDate.now().minusDays(7)))
            .medAntallBarn(1);
        fgRepo.lagre(behandling, hendelseBuilder);

        Søknad søknad = new SøknadEntitet.Builder()
            .medSøknadsdato(LocalDate.now())
            .medFamilieHendelse(fgRepo.hentAggregat(behandling).getSøknadVersjon())
            .build();
        repositoryProvider.getSøknadRepository().lagreOgFlush(behandling, søknad);

        return behandling;
    }

    private void oppdaterMedBehandlingsresultat(Behandling behandling, boolean innvilget) {
        VilkårResultat.builder()
            .leggTilVilkårResultat(VilkårType.FØDSELSVILKÅRET_MOR, innvilget ? VilkårUtfallType.OPPFYLT : VilkårUtfallType.IKKE_OPPFYLT, null, new Properties(), null, false, false, null, null)
            .medVilkårResultatType(innvilget ? VilkårResultatType.INNVILGET : VilkårResultatType.AVSLÅTT)
            .buildFor(behandling);
        if (innvilget) {
            BeregningResultat.builder()
                .medBeregning(new Beregning(48500L, 1L, 48500L, LocalDateTime.now()))
                .buildFor(behandling);
        }
    }

    private Behandling opprettBehandlingMedFødsel(BehandlingStegType stegType) {
        LocalDate fødselsdato = LocalDate.now().minusWeeks(2);
        Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(opprettFagsak());

        final Behandling behandling = behandlingBuilder.medBehandlendeEnhet(new OrganisasjonsEnhet(BEHANDLENDE_ENHET_ID, null))
        .build();

        utførAksjonspunkt(behandling, SØKERS_OPPLYSNINGSPLIKT_MANU);
        utførAksjonspunkt(behandling, KONTROLLER_OPPLYSNINGER_OM_MEDLEMSKAP);

        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, stegType);
        Behandlingsresultat behandlingsresultat = opprettBehandlingsresultat(behandling, BehandlingResultatType.INNVILGET);
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        opprettBehandlingsvedtak(behandling, behandlingsresultat);
        Beregning beregning = new Beregning(1L, 1L, 1L, LocalDateTime.now());
        BeregningResultat beregningResultat = BeregningResultat.builder().medBeregning(beregning).buildFor(behandling);
        beregningRepository.lagre(beregningResultat, behandlingRepository.taSkriveLås(behandling));
        final FamilieHendelseBuilder hendelseBuilder = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medFødselsDato(fødselsdato)
            .medAntallBarn(1);
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, hendelseBuilder);
        final FamilieHendelseBuilder builder = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medFødselsDato(fødselsdato);
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, builder);

        final Søknad søknad = new SøknadEntitet.Builder()
            .medSøknadsdato(LocalDate.now())
            .medFamilieHendelse(repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling).getSøknadVersjon())
            .build();

        repositoryProvider.getSøknadRepository().lagreOgFlush(behandling, søknad);

        return behandling;
    }

    private Behandlingsresultat opprettBehandlingsresultat(Behandling behandling, BehandlingResultatType behandlingResultatType) {
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder().medBehandlingResultatType(behandlingResultatType).buildFor(behandling);
        return behandlingsresultat;
    }

    private void opprettBehandlingsvedtak(Behandling behandling, Behandlingsresultat behandlingsresultat) {
        BehandlingVedtakRepository behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        BehandlingVedtak vedtak = BehandlingVedtak.builder()
            .medAnsvarligSaksbehandler("saksbehandler gundersen")
            .medIverksettingStatus(IverksettingStatus.IVERKSATT)
            .medVedtaksdato(LocalDate.now())
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medBehandlingsresultat(behandlingsresultat)
            .build();
        behandlingVedtakRepository.lagre(vedtak, behandlingRepository.taSkriveLås(behandling));
    }

    private void utførAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        Aksjonspunkt aksjonspunkt1 = aksjonspunktRepository.leggTilAksjonspunkt(behandling, aksjonspunktDefinisjon);
        aksjonspunktRepository.setTilUtført(aksjonspunkt1, "");
    }

    private Behandling opprettBehandlingMedAdopsjon(BehandlingStegType stegType) {
        LocalDate fødselsdato = LocalDate.now().minusMonths(8);
        Map<Integer, LocalDate> map = new HashMap<>();
        map.put(1, fødselsdato);
        map.put(2, fødselsdato.minusYears(2));

        Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(opprettFagsak());

        final Behandling behandling = behandlingBuilder.medBehandlendeEnhet(new OrganisasjonsEnhet(BEHANDLENDE_ENHET_ID, null))
        .build();
        Behandlingsresultat behandlingsresultat = opprettBehandlingsresultat(behandling,BehandlingResultatType.INNVILGET);
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        opprettBehandlingsvedtak(behandling, behandlingsresultat);
        Beregning beregning = new Beregning(1L, 1L, 1L, LocalDateTime.now());
        BeregningResultat beregningResultat = BeregningResultat.builder().medBeregning(beregning).buildFor(behandling);
        beregningRepository.lagre(beregningResultat, behandlingRepository.taSkriveLås(behandling));
        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, stegType);

        final FamilieHendelseBuilder søknadVersjon = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling);
        søknadVersjon
            .medAdopsjon(søknadVersjon.getAdopsjonBuilder()
                .medOmsorgsovertakelseDato(LocalDate.now().plusDays(50)))
            .leggTilBarn(fødselsdato)
            .leggTilBarn(fødselsdato.minusYears(2))
            .medAntallBarn(2);
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadVersjon);
        final FamilieHendelseBuilder hendelseBuilder = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling);
        hendelseBuilder
            .medAdopsjon(hendelseBuilder.getAdopsjonBuilder()
                .medAdoptererAlene(false)
                .medErEktefellesBarn(false))
            .leggTilBarn(fødselsdato).leggTilBarn(fødselsdato.minusYears(2));
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, hendelseBuilder);

        final Søknad søknad = new SøknadEntitet.Builder()
            .medSøknadsdato(LocalDate.now())
            .medFamilieHendelse(repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling).getSøknadVersjon())
            .build();
        repositoryProvider.getSøknadRepository().lagreOgFlush(behandling, søknad);

        return behandling;
    }

    @Test
    public void testTilCalendar() {
        LocalDate localDate = LocalDate.of(2017, Month.APRIL, 18);
        Calendar calendar = VedtakXmlUtil.tilCalendar(localDate);
        assertEquals(2017, calendar.get(Calendar.YEAR));
        assertEquals(3, calendar.get(Calendar.MONTH));
        assertEquals(18, calendar.get(Calendar.DAY_OF_MONTH));
    }
}
