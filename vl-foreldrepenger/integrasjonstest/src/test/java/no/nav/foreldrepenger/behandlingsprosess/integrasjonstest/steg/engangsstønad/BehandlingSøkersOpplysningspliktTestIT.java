package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.engangsstønad;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENTER_PÅ_KOMPLETT_SØKNAD;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.SØKERS_OPPLYSNINGSPLIKT_MANU;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.SØKERS_OPPLYSNINGSPLIKT_OVST;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.VEDTAK_UTEN_TOTRINNSKONTROLL;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.AVBRUTT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.OPPRETTET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.UTFØRT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.FØDSELSVILKÅRET_MOR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.MEDLEMSKAPSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.SØKERSOPPLYSNINGSPLIKT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.SØKNADSFRISTVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_OPPFYLT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_VURDERT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.OPPFYLT;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestUtils.hentFødselsdatoFraFnr;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.VilkårTestutfall.resultat;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.søknad.SøknadTestdataBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.KjørProsessTasks;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.DokumentmottakTestUtil;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.AksjonspunktRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.BekreftedeAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.OverstyrteAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftSokersOpplysningspliktManuDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringSokersOpplysingspliktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Vedlegg;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Innsendingstype;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;

@RunWith(CdiRunner.class)
public class BehandlingSøkersOpplysningspliktTestIT {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();
    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);

    @Inject
    private AksjonspunktApplikasjonTjeneste applikasjonstjeneste;

    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    @Inject
    private FagsakRepository fagsakRepository;

    @Inject
    private BehandlingRepository behandlingRepository;

    @Inject
    BehandlingsutredningApplikasjonTjeneste behandlingsutredningApplikasjonTjeneste;

    @Inject
    private DokumentmottakTestUtil hjelper;

    @Inject
    private ProsessTaskRepository prosessTaskRepository;

    @Inject
    private TotrinnTjeneste totrinnTjeneste;

    private Fagsak fagsak;

    private AksjonspunktRestTjeneste aksjonspunktRestTjeneste;
    private BehandlingskontrollKontekst kontekst;

    @Before
    public void settOpp() {
        aksjonspunktRestTjeneste = new AksjonspunktRestTjeneste(applikasjonstjeneste, behandlingRepository, behandlingsutredningApplikasjonTjeneste, totrinnTjeneste);
    }

    @Test
    public void case_for_automatisk_innvilget_vilkår_ikke_elektronisk_søknad() throws Exception {
        // Arrange
        byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        LocalDate fødselsdato = hentFødselsdatoFraFnr(TpsRepo.STD_BARN_FNR);
        LocalDate mottattDato = fødselsdato.plusDays(14 + 1); // > 14 dager unngår AP-7002 (Sett på vent)
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadMorFødsel(fødselsdato, mottattDato, 1, fagsak.getAktørId(), emptyList()));

        // Act
        utførProsessSteg(behandlingId);
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        // Assert
        assertUtil.assertAksjonspunkter(asList(AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(FØDSELSVILKÅRET_MOR, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT),
                resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(SØKNADSFRISTVILKÅRET, OPPFYLT)));
        assertBehandlingVedtak(VedtakResultatType.INNVILGET);
    }

    @Test
    public void case_for_automatisk_innvilget_ukomplett_søknad_gjelder_fødsel_med_barn_i_tps() throws Exception {
        // Arrange
        byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        LocalDate fødselsdato = hentFødselsdatoFraFnr(TpsRepo.STD_BARN_FNR);
        LocalDate mottattDato = fødselsdato.plusDays(14 + 1); // > 14 dager unngår AP-7002 (Sett på vent)
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadMorFødsel(fødselsdato, mottattDato, 1, fagsak.getAktørId(), emptyList()));

        // Act
        utførProsessSteg(behandlingId);
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        // Assert
        assertUtil.assertAksjonspunkter(asList(AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(FØDSELSVILKÅRET_MOR, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT),
                resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(SØKNADSFRISTVILKÅRET, OPPFYLT)));
        assertBehandlingVedtak(VedtakResultatType.INNVILGET);
    }

    @Test
    public void skal_avslå_søknad_pga_søkers_opplysningsplikt_for_ikke_komplett_søknad_og_barnet_ikke_finnes_i_tps() throws Exception {
        // Arrange steg 1
        byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        LocalDate fødselsdato = hentFødselsdatoFraFnr("111111");
        int antallBarnFraSøknad = 1;
        Long behandlingId = hjelper.byggBehandling(fagsak,
            søknadEngangstønadMorFødsel(fødselsdato, fødselsdato, antallBarnFraSøknad, fagsak.getAktørId(), singletonList(opplastetVedleggSomIkkeFinnes())),
            true, true);

        // Act
        utførProsessSteg(behandlingId);

        // Assert
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, OPPRETTET));

        // Arrange steg 2 - simulere at behandling har ventet i 14 dager
        Behandling gjenopptattBehandling = triggerGjenopptaBehandling(behandlingId);

        // Act
        utførProsessSteg(gjenopptattBehandling.getId());
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        utførProsessSteg(behandlingId);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, OPPRETTET),
            AksjonspunktTestutfall.resultat(SØKERS_OPPLYSNINGSPLIKT_MANU, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        // Arrange steg 3 - Er nå i riktig tilstand før vi får kjørt den koden vi er ute etter
        boolean erVilkarOk = false;
        Behandling oppdatertBehandling = behandlingRepository.hentBehandling(behandlingId);
        BekreftSokersOpplysningspliktManuDto avslåSøkersOpplysningsplikt = new BekreftSokersOpplysningspliktManuDto(
            "bare tull", erVilkarOk, Collections.emptyList());

        Assertions.assertThat(behandlingRepository.hentBehandling(behandlingId).getAktivtBehandlingSteg())
            .isEqualTo(BehandlingStegType.KONTROLLERER_SØKERS_OPPLYSNINGSPLIKT);
        Assertions.assertThat(behandlingRepository.hentBehandling(behandlingId).getBehandlingStegTilstand().get().getBehandlingStegStatus())
            .isEqualTo(BehandlingStegStatus.UTGANG);

        // Act
        bekreftAksjonspunkt(oppdatertBehandling, avslåSøkersOpplysningsplikt);

        // Assert
        Assertions.assertThat(behandlingRepository.hentBehandling(behandlingId).getAktivtBehandlingSteg())
            .isEqualTo(BehandlingStegType.FORESLÅ_VEDTAK);
        Assertions.assertThat(behandlingRepository.hentBehandling(behandlingId).getStatus()).isEqualTo(BehandlingStatus.UTREDES);
        Assertions.assertThat(behandlingRepository.hentBehandling(behandlingId).getBehandlingsresultat().getBehandlingResultatType())
            .isEqualTo(BehandlingResultatType.AVSLÅTT);

        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, AVBRUTT),
            AksjonspunktTestutfall.resultat(SØKERS_OPPLYSNINGSPLIKT_MANU, UTFØRT),
            AksjonspunktTestutfall.resultat(VEDTAK_UTEN_TOTRINNSKONTROLL, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.AVSLÅTT,
            asList(resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKERSOPPLYSNINGSPLIKT, IKKE_OPPFYLT, Avslagsårsak.MANGLENDE_DOKUMENTASJON),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
    }

    @Test
    public void godkjenn_søkers_opplysningsplikt_for_ikke_komplett_søknad_og_barnet_ikke_finnes_i_tps() throws Exception {
        // Arrange
        byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        LocalDate fødselsdato = hentFødselsdatoFraFnr("111111");
        int antallBarnFraSøknad = 1;
        Long behandlingId = hjelper.byggBehandling(fagsak,
            søknadEngangstønadMorFødsel(fødselsdato, fødselsdato, antallBarnFraSøknad, fagsak.getAktørId(), singletonList(opplastetVedleggSomIkkeFinnes())),
            false, true);

        utførProsessSteg(behandlingId);

        // simulere at behandling har ventet i 14 dager
        Behandling gjenopptattBehandling = triggerGjenopptaBehandling(behandlingId);
        utførProsessSteg(gjenopptattBehandling.getId());
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        utførProsessSteg(behandlingId);

        // Er nå i riktig tilstand før vi får kjørt den koden vi er ute etter
        // Act

        boolean erVilkarOk = true;
        Behandling oppdatertBehandling = behandlingRepository.hentBehandling(behandlingId);
        BekreftSokersOpplysningspliktManuDto bekreftSokersOpplysningspliktManuDto = new BekreftSokersOpplysningspliktManuDto(
            "bare tull", erVilkarOk, Collections.emptyList());

        bekreftAksjonspunkt(oppdatertBehandling, bekreftSokersOpplysningspliktManuDto);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            AksjonspunktTestutfall.resultat(SØKERS_OPPLYSNINGSPLIKT_MANU, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
    }

    @Test
    public void overstyring_av_søkers_opplysningsplikt_for_fødsel_med_avvik_i_antall_barn_mellom_tps_og_søknad() throws Exception {
        // Arrange steg 1: Legg inn søknad om flere barn enn hva som er registrert i TPS
        byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        LocalDate fødselsdato = hentFødselsdatoFraFnr(TpsRepo.STD_BARN_FNR);
        int antallBarnFraSøknad = 2;
        Long behandlingId = hjelper.byggBehandling(fagsak,
            søknadEngangstønadMorFødsel(fødselsdato, fødselsdato, antallBarnFraSøknad, fagsak.getAktørId(), emptyList()));

        // Act
        utførProsessSteg(behandlingId);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        utførProsessSteg(behandlingId);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        // Arrange steg 2: Sett søkers opplysningsplikt som ikke oppfylt
        Behandling oppdatertBehandling = behandlingRepository.hentBehandling(behandlingId);
        OverstyringSokersOpplysingspliktDto overstyringSokersOpplysingspliktDto = new OverstyringSokersOpplysingspliktDto(false, "bare tull");

        // Act
        overstyrAksjonspunkt(oppdatertBehandling, overstyringSokersOpplysingspliktDto);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(SØKERS_OPPLYSNINGSPLIKT_OVST, UTFØRT),
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, AVBRUTT),
            AksjonspunktTestutfall.resultat(VEDTAK_UTEN_TOTRINNSKONTROLL, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.AVSLÅTT,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, IKKE_OPPFYLT, Avslagsårsak.MANGLENDE_DOKUMENTASJON, true),
                resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));

        // Arrange steg 3: Saksbehandler ombestemmer seg og setter søkers opplysningsplikt som oppfylt likevel
        oppdatertBehandling = behandlingRepository.hentBehandling(behandlingId);
        overstyringSokersOpplysingspliktDto = new OverstyringSokersOpplysingspliktDto(true, "bare tull");

        overstyrAksjonspunkt(oppdatertBehandling, overstyringSokersOpplysingspliktDto);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(SØKERS_OPPLYSNINGSPLIKT_OVST, UTFØRT),
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT, true),
                resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
    }

    private void overstyrAksjonspunkt(Behandling behandling, OverstyringAksjonspunktDto dto)
            throws URISyntaxException {
        aksjonspunktRestTjeneste.overstyr(OverstyrteAksjonspunkterDto.lagDto(behandling.getId(), behandling.getVersjon(),
            singletonList(dto)));
        new KjørProsessTasks(prosessTaskRepository).utførTasks();
    }


    private void bekreftAksjonspunkt(Behandling behandling, BekreftetAksjonspunktDto dto) throws URISyntaxException {
        aksjonspunktRestTjeneste.bekreft(BekreftedeAksjonspunkterDto.lagDto(behandling.getId(), behandling.getVersjon(),
            singletonList(dto)));
        new KjørProsessTasks(prosessTaskRepository).utførTasks();
    }

    private Behandling triggerGjenopptaBehandling(Long behandlingId) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        behandlingskontrollTjeneste.settAutopunkterTilUtført(kontekst, false);
        return behandlingRepository.hentBehandling(behandlingId);
    }

    private void byggFagsak(AktørId aktørId, RelasjonsRolleType rolle, NavBrukerKjønn kjønn) {
        NavBruker navBruker = new NavBrukerBuilder()
            .medAktørId(aktørId)
            .medKjønn(kjønn)
            .build();
        fagsak = FagsakBuilder.nyEngangstønad(rolle)
            .medBruker(navBruker)
            .medSaksnummer(new Saksnummer("123"))
            .build();
        fagsakRepository.opprettNy(fagsak);
    }

    private void assertBehandlingVedtak(VedtakResultatType vedtakResultatType) {
        List<BehandlingVedtak> vedtakene = repository.hentAlle(BehandlingVedtak.class);
        assertThat(vedtakene).hasSize(1);
        assertThat(vedtakene.get(0).getVedtakResultatType()).isEqualTo(vedtakResultatType);
    }

    private void utførProsessSteg(Long behandlingId) {
        kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);

        repository.flushAndClear();
    }

    private Soeknad søknadEngangstønadMorFødsel(LocalDate fødselsdato, LocalDate mottattDato, int antallBarnFraSøknad, AktørId søkerAktørid,
                                                List<Vedlegg> vedlegg) {
        return new SøknadTestdataBuilder()
            .medPåkrevdeVedleggListe(vedlegg) // Kan denne defaultes?
            .engangsstønad(new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapOppholdNorge())
            .medMottattdato(mottattDato)
            .medSøker(ForeldreType.MOR, søkerAktørid)
            .medFødsel(new SøknadTestdataBuilder.FødselBuilder()
                .medFoedselsdato(fødselsdato)
                .medAntallBarn(antallBarnFraSøknad))
            .build();
    }

    private Vedlegg opplastetVedleggSomIkkeFinnes() {
        Vedlegg vedlegg = new Vedlegg();
        vedlegg.setId("abc");
        vedlegg.setSkjemanummer("I000042");
        Innsendingstype innsendingstype = new Innsendingstype();
        innsendingstype.setKode("LASTET_OPP");
        vedlegg.setInnsendingstype(innsendingstype);
        vedlegg.setTilleggsinformasjon("bla bla bla");
        return vedlegg;
    }

}
