package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.engangsstønad;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_FØDSELREGISTRERING;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.OPPRETTET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.UTFØRT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.FØDSELSVILKÅRET_MOR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.MEDLEMSKAPSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.SØKERSOPPLYSNINGSPLIKT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.SØKNADSFRISTVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad.VM_1002;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_OPPFYLT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_VURDERT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.OPPFYLT;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.VilkårTestutfall.resultat;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.FagsakTjeneste;
import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.UidentifisertBarnEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.søknad.SøknadTestdataBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.KjørProsessTasks;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.engangsstønad.søknad.SoeknadsskjemaEngangsstoenadTestdataBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestUtils;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.DokumentmottakTestUtil;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.RelatertBehandlingTjeneste;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.foreldrepenger.web.app.tjenester.behandling.BehandlingRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsprosessApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.GjenopptaBehandlingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingDtoTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;

@RunWith(CdiRunner.class)
public class BehandlingFødselsvilkårTestIT {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();
    private BehandlingskontrollKontekst kontekst;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    @Inject
    private FagsakRepository fagsakRepository;

    @Inject
    private BehandlingRepository behandlingRepository;

    @Inject
    private BehandlingsutredningApplikasjonTjeneste behandlingutredningTjeneste;

    @Inject
    private BehandlingsprosessApplikasjonTjeneste behandlingsprosessTjeneste;

    @Inject
    private ProsessTaskRepository prosessTaskRepository;

    @Inject
    private FagsakTjeneste fagsakTjeneste;

    @Inject
    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;

    @Inject
    private BehandlingDtoTjeneste behandlingDtoTjeneste;

    @Inject
    private DokumentmottakTestUtil hjelper;

    @Inject
    private RelatertBehandlingTjeneste relatertBehandlingTjeneste;

    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);

    private Fagsak fagsak;

    private BehandlingRestTjeneste behandlingRestTjeneste;

    @Before
    public void setup() {
        behandlingRestTjeneste = new BehandlingRestTjeneste(repositoryProvider,
            behandlingutredningTjeneste,
            behandlingsprosessTjeneste,
            fagsakTjeneste,
            henleggBehandlingTjeneste,
            behandlingDtoTjeneste,
            relatertBehandlingTjeneste);
    }

    @Test
    public void fødsel_happy_case_med_barn_i_tps() throws Exception {
        // Arrange
        byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        LocalDate fødselsdato = IntegrasjonstestUtils.hentFødselsdatoFraFnr(TpsRepo.STD_BARN_FNR);
        LocalDate søknadsdato = fødselsdato;
        int antallBarnFraSøknad = 1;
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadFødsel(fødselsdato, søknadsdato, antallBarnFraSøknad));

        // Act
        utførProsessSteg(behandlingId);
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        // Assert
        assertUnikFagsak(RelasjonsRolleType.MORA);
        assertUnikFødsel(fødselsdato, behandlingId);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(FØDSELSVILKÅRET_MOR, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT),
                resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(SØKNADSFRISTVILKÅRET, OPPFYLT)));
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        assertUnikBeregning();
    }

    @Test
    public void fødsel_happy_case_med_barn_i_tps_gammelt_søknadsformat() throws Exception {
        // Arrange
        byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        LocalDate fødselsdato = IntegrasjonstestUtils.hentFødselsdatoFraFnr(TpsRepo.STD_BARN_FNR);
        int antallBarnFraSøknad = 1;
        Long behandlingId = hjelper.byggBehandlingGammeltSøknadsformat(fagsak, søknadFødselGammeltFormat(fødselsdato, antallBarnFraSøknad));

        // Act
        utførProsessSteg(behandlingId);
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        // Assert
        assertUnikFagsak(RelasjonsRolleType.MORA);
        assertUnikFødsel(fødselsdato, behandlingId);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(FØDSELSVILKÅRET_MOR, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT),
                resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(SØKNADSFRISTVILKÅRET, OPPFYLT)));
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        assertUnikBeregning();
    }

    @Test
    public void fødsel_med_avvik_i_antall_barn_mellom_tps_og_søknad() throws Exception {
        // Arrange
        byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        LocalDate fødselsdato = IntegrasjonstestUtils.hentFødselsdatoFraFnr(TpsRepo.STD_BARN_FNR);
        LocalDate søknadsdato = fødselsdato;
        int antallBarnFraSøknad = 2;
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadFødsel(fødselsdato, søknadsdato, antallBarnFraSøknad));

        // Act
        utførProsessSteg(behandlingId);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        utførProsessSteg(behandlingId);

        // Assert
        assertUnikFagsak(RelasjonsRolleType.MORA);
        assertUnikFødsel(fødselsdato, behandlingId);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        assertThat(repository.hentAlle(Beregning.class)).hasSize(0);
    }

    @Test
    public void søknad_med_kun_søknadsdato_oppgitt() throws Exception {
        // Arrange
        byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        LocalDate søknadsdato = LocalDate.now();
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadMedKunFødselsdato(søknadsdato));

        // Act
        utførProsessSteg(behandlingId);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        utførProsessSteg(behandlingId);

        // Assert
// assertThat(repository.hentAlle(FødselEntitet.class)).hasSize(0);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AVKLAR_TERMINBEKREFTELSE, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        assertThat(repository.hentAlle(Beregning.class)).hasSize(0);
    }

    private void kjørProsessTasks() {
        new KjørProsessTasks(prosessTaskRepository).utførTasks();
    }

    @Test
    public void fødsel_med_fødselsdato_mindre_enn_14_dager_gammel_som_ikke_er_registrert_i_tps() throws Exception {
        // Arrange steg 1: Søkes om stønad < 14 dager etter fødsel, barn ikke registrert i TPS
        byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        LocalDate søknadsdato = LocalDate.now();
        LocalDate fødselsdatoFraSøknad = LocalDate.now().minusDays(14 - 1); // Ingen barn registrert på denne datoen
        int antallBarnFraSøknad = 1;
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadFødsel(fødselsdatoFraSøknad, søknadsdato, antallBarnFraSøknad));

        // Act
        utførProsessSteg(behandlingId);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        utførProsessSteg(behandlingId);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, IKKE_VURDERT),
                resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_FØDSELREGISTRERING, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        assertUtil.assertOppgaveBehandlingKobling();

        // Arrange steg 2: Gjenoppta behandling (simulerer at det er 14 dager senere)
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        simulerAtFødselsdatoOverstiger14dager(fødselsdatoFraSøknad);
        GjenopptaBehandlingDto gjenopptaBehandlingDto = new GjenopptaBehandlingDto();
        gjenopptaBehandlingDto.setBehandlingId(behandling.getId());
        gjenopptaBehandlingDto.setBehandlingVersjon(behandling.getVersjon());

        // Act
        behandlingRestTjeneste.gjenopptaBehandling(gjenopptaBehandlingDto);

        kjørProsessTasks();

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_FØDSELREGISTRERING, UTFØRT),
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
    }

    private void simulerAtFødselsdatoOverstiger14dager(LocalDate fødselsdatoFraSøknad) {
        EntityManager entityManager = repoRule.getEntityManager();
        Query oppdatering = entityManager.createQuery(
            "UPDATE UidentifisertBarn SET fødselsdato=:fødselsdato");
        oppdatering.setParameter("fødselsdato", fødselsdatoFraSøknad.minusDays(7)); //$NON-NLS-1$
        oppdatering.executeUpdate();
        repository.flushAndClear();
    }

    @Test
    public void termin_med_termindato_passert_26_svangerskapsuke_og_ikke_oppfylt_opplysningsplikt() throws Exception {
        // Arrange
        byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        LocalDate søknadsdato = LocalDate.now();
        // TODO (essv): Asmir, disse magiske tallene som du har lagt inn her bør forklares.
        LocalDate termindatoFraSøknad = LocalDate.now().plusDays(((40 - 26) * 7) + 3);
        LocalDate utstedtDatoFraSøknad = LocalDate.now().minusDays(1);
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadTermin(termindatoFraSøknad, søknadsdato, utstedtDatoFraSøknad));

        utførProsessSteg(behandlingId);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        utførProsessSteg(behandlingId);

        // Assert
        assertThat(repository.hentAlle(UidentifisertBarnEntitet.class)).hasSize(0);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AVKLAR_TERMINBEKREFTELSE, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        assertThat(repository.hentAlle(Beregning.class)).hasSize(0);
    }

    @Test
    public void far_som_søker_på_vegne_av_mor() throws Exception {
        // Arrange
        byggFagsak(TpsRepo.STD_MANN_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.MANN);
        LocalDate søknadsdato = LocalDate.now();
        // Long behandlingId = hjelper.byggBehandling(fagsak, søknadMedKunFødselsdato(søknadsdato));
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadMedKunFødselsdato(søknadsdato));

        // Act
        utførProsessSteg(behandlingId);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        utførProsessSteg(behandlingId);

        // Assert
        assertThat(repository.hentAlle(UidentifisertBarnEntitet.class)).hasSize(0);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AVKLAR_TERMINBEKREFTELSE, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        assertThat(repository.hentAlle(Beregning.class)).hasSize(0);
    }

    @Test
    public void søker_er_medmor() throws Exception {
        // Arrange
        byggFagsak(TpsRepo.MEDMOR_AKTØR_ID, RelasjonsRolleType.FARA, NavBrukerKjønn.KVINNE);
        LocalDate søknadsdato = LocalDate.now(); // Samsvarer med registrert fødselsdato i TPS-mock
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadMedKunFødselsdato(søknadsdato));

        // Act
        utførProsessSteg(behandlingId);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        utførProsessSteg(behandlingId);

        // Assert
        assertThat(repository.hentAlle(UidentifisertBarnEntitet.class)).hasSize(1);
        assertThat(repository.hentAlle(Aksjonspunkt.class)).hasSize(1);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.AVSLÅTT,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(FØDSELSVILKÅRET_MOR, IKKE_OPPFYLT, VM_1002), // VM_1002 = "Søker er medmor"
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
    }

    private void utførProsessSteg(Long behandlingId) {
        kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);

        repository.flush();
    }

    private Soeknad søknadTermin(LocalDate søknadsdato, LocalDate termindatoFraSøknad,
                                 LocalDate utstedtDatoFraSøknad) {

        return new SøknadTestdataBuilder().søknadEngangsstønadMor()
            .medMottattdato(søknadsdato)
            .medTermin(new SøknadTestdataBuilder.TerminBuilder()
                .medTermindato(termindatoFraSøknad)
                .medUtsteddato(utstedtDatoFraSøknad))
            .build();
    }

    private Soeknad søknadFødsel(LocalDate fødselsdato, LocalDate mottattDato, int antallBarnFraSøknad) {
        return new SøknadTestdataBuilder().søknadEngangsstønadMor()
            .medMottattdato(mottattDato)
            .medFødsel(new SøknadTestdataBuilder.FødselBuilder()
                .medFoedselsdato(fødselsdato)
                .medAntallBarn(antallBarnFraSøknad))
            .build();
    }

    private Soeknad søknadMedKunFødselsdato(LocalDate søknadsdato) {
        return new SøknadTestdataBuilder().søknadEngangsstønadMor()
            .medMottattdato(søknadsdato)
            .medTermin(new SøknadTestdataBuilder.TerminBuilder()
                .medTermindato(LocalDate.now().plusDays(20))
                .medUtsteddato(LocalDate.now())
                .medAntallBarn(1))
            .build();
    }

    private SoeknadsskjemaEngangsstoenad søknadFødselGammeltFormat(LocalDate fødselsdato, int antallBarnFraSøknad) {
        return new SoeknadsskjemaEngangsstoenadTestdataBuilder()
            .fødsel()
            .engangsstønadMor()
            .medVedleggsliste(emptyList()) // Kan denne defaultes?
            .medFødselsdatoer(singletonList(fødselsdato))
            .medAntallBarn(antallBarnFraSøknad)
            // Skjema defaulter på at søker oppholder seg i Norge
            .medTidligereOppholdNorge(true)
            .medOppholdNorgeNå(true)
            .medFremtidigOppholdNorge(true)
            .build();
    }

    private void byggFagsak(AktørId aktørId, RelasjonsRolleType rolle, NavBrukerKjønn kjønn) {
        NavBruker navBruker = new NavBrukerBuilder()
            .medAktørId(aktørId)
            .medKjønn(kjønn)
            .build();
        fagsak = FagsakBuilder.nyEngangstønad(rolle)
            .medSaksnummer(new Saksnummer("123"))
            .medBruker(navBruker).build();
        fagsakRepository.opprettNy(fagsak);
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

    private void assertUnikBeregning() {
        assertThat(repository.hentAlle(Beregning.class)).hasSize(1);
    }

}
