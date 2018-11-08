package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.engangsstønad;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType.KLAGE_AVVIST;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType.KLAGE_YTELSESVEDTAK_OPPHEVET;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType.KLAGE_YTELSESVEDTAK_STADFESTET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.FATTER_VEDTAK;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.FORESLÅ_VEDTAK;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_VURDERING_AV_KLAGE_NFP;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_VURDERING_AV_KLAGE_NK;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.AVBRUTT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.OPPRETTET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.UTFØRT;
import static no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageAvvistÅrsak.KLAGET_FOR_SENT;
import static no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdering.AVVIS_KLAGE;
import static no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdering.OPPHEVE_YTELSESVEDTAK;
import static no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdering.STADFESTE_YTELSESVEDTAK;
import static no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv.NFP;
import static no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv.NK;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestUtils.hentFødselsdatoFraFnr;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.HendelseVersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageAvvistÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageMedholdÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurderingResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioKlageEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.KjørProsessTasks;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.DokumentmottakTestUtil;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InnhentDokumentTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.AksjonspunktRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.BekreftedeAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.AksjonspunktGodkjenningDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FatterVedtakAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.ForeslaVedtakAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.KlageVurderingResultatAksjonspunktDto.KlageVurderingResultatNfpAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.KlageVurderingResultatAksjonspunktDto.KlageVurderingResultatNkAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;

@RunWith(CdiRunner.class)
public class BehandlingHåndtereKlageTestIT {
    private static final String FØRSTEGANGSSØKNAD_ENHETS_ID = "4833";
    private static final String KLAGEINSTANS_ENHETS_ID = "4205";

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();
    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);

    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    @Inject
    private AksjonspunktApplikasjonTjeneste applikasjonstjeneste;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Inject
    private BehandlingRepository behandlingRepository;

    @Inject
    private BehandlingsutredningApplikasjonTjeneste behandlingsutredningApplikasjonTjeneste;

    @Inject
    private ProsessTaskRepository prosessTaskRepository;

    @Inject
    private InnhentDokumentTjeneste innhentDokumentTjeneste;

    @Inject
    private TotrinnTjeneste totrinnTjeneste;

    private AksjonspunktRestTjeneste aksjonspunktRestTjeneste;

    @Before
    public void setup() {
        aksjonspunktRestTjeneste = new AksjonspunktRestTjeneste(applikasjonstjeneste, repositoryProvider.getBehandlingRepository(),
            behandlingsutredningApplikasjonTjeneste, totrinnTjeneste);
    }

    @Test
    public void skal_velge_resultat_fra_nk_når_nfp_og_nk_får_godkjent_i_totrinnskontroll() throws Exception {
        // Arrange - Oppretter klageBehandling
        ScenarioMorSøkerEngangsstønad fødselsøknadMor = ScenarioMorSøkerEngangsstønad.forFødsel().medBruker(TpsRepo.STD_KVINNE_AKTØR_ID,
            NavBrukerKjønn.KVINNE);

        Behandling klageBehandling = ScenarioKlageEngangsstønad.forUtenVurderingResultat(fødselsøknadMor)
            .medBehandlendeEnhet(FØRSTEGANGSSØKNAD_ENHETS_ID)
            .medBehandlingStegStart(null) // Starter å kjøre behandling helt fra begynnelsen.
            .lagre(repositoryProvider);
        Long klageBehandlingId = klageBehandling.getId();

        // Arrange - oppretter søknad
        LocalDate fødselsdato = hentFødselsdatoFraFnr(TpsRepo.STD_BARN_FNR);
        LocalDate mottattDato = fødselsdato.plusDays(14 + 1); // > 14 dager unngår AP-7002 (Sett på vent)

        FamilieHendelseBuilder familieHendelse = FamilieHendelseBuilder.oppdatere(Optional.empty(), HendelseVersjonType.SØKNAD)
            .medFødselsDato(fødselsdato);
        repositoryProvider.getFamilieGrunnlagRepository().lagre(klageBehandling, familieHendelse);
        FamilieHendelseGrunnlag familieHendelseGrunnlag = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(klageBehandling);

        final Søknad søknad = new SøknadEntitet.Builder()
            .medSøknadsdato(LocalDate.now())
            .medMottattDato(LocalDate.now())
            .medElektroniskRegistrert(true)
            .medFamilieHendelse(familieHendelseGrunnlag.getSøknadVersjon())
            .build();

        repositoryProvider.getSøknadRepository().lagreOgFlush(klageBehandling, søknad);

        // Act
        innhentDokumentTjeneste.utfør(DokumentmottakTestUtil.lagMottattDokument(DokumentTypeId.KLAGE_DOKUMENT, klageBehandling.getFagsakId(),
            null, mottattDato, false, null), BehandlingÅrsakType.UDEFINERT);
        utførProsessSteg(klageBehandlingId);

        // Assert
        Behandling klageBehandlingTrinn1 = behandlingRepository.hentBehandling(klageBehandlingId);
        assertThat(klageBehandlingTrinn1.getBehandlendeEnhet()).isEqualTo(FØRSTEGANGSSØKNAD_ENHETS_ID);
        assertThat(klageBehandlingTrinn1.getAktivtBehandlingSteg()).isNotNull();
        assertThat(klageBehandlingTrinn1.hentKlageVurderingResultat(KlageVurdertAv.NFP)).isEmpty();
        assertUtil.assertAksjonspunkter(AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_KLAGE_NFP, AksjonspunktStatus.OPPRETTET));

        // Arrange trinn 2: Bekrefte nfp/Nav Familie pensjon klage manuelt -> blir sendt til NK/Nav klageinstans for
        // videre vurdering
        KlageVurderingResultatNfpAksjonspunktDto nfpKlageVurderingStadfestet = byggNfpAksjonspunktDto(STADFESTE_YTELSESVEDTAK,
            null);

        // Act
        bekreftAksjonspunkt(klageBehandlingTrinn1, nfpKlageVurderingStadfestet);

        // Assert
        Behandling klageBehandlingTrinn2 = behandlingRepository.hentBehandling(klageBehandlingId);
        assertThat(klageBehandlingTrinn2.getBehandlendeEnhet()).isEqualTo(KLAGEINSTANS_ENHETS_ID);
        assertThat(klageBehandlingTrinn2.getBehandlingsresultat().getBehandlingResultatType()).isEqualTo(KLAGE_YTELSESVEDTAK_STADFESTET);
        assertKlageVurderingResultat(klageBehandlingId, NFP, STADFESTE_YTELSESVEDTAK, null, null);
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_KLAGE_NFP, UTFØRT),
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_KLAGE_NK, OPPRETTET)));

        // Arrange trinn 3: Bekrefte Nk/Nav Klageinstans manuelt -> blir sendt til foreslå vedtak
        KlageVurderingResultatNkAksjonspunktDto nkKlageVurderingAvvist = byggNkAksjonspunktDto(AVVIS_KLAGE,
            KLAGET_FOR_SENT);

        // Act
        bekreftAksjonspunkt(klageBehandlingTrinn2, nkKlageVurderingAvvist);

        // Assert
        assertThat(behandlingRepository.hentBehandling(klageBehandlingId).getBehandlingsresultat().getBehandlingResultatType())
            .isEqualTo(KLAGE_AVVIST);
        assertKlageVurderingResultat(klageBehandlingId, NK, AVVIS_KLAGE, null, KLAGET_FOR_SENT);
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_KLAGE_NFP, UTFØRT),
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_KLAGE_NK, UTFØRT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, OPPRETTET)));

        // Arrange trinn 4: Foreslå vedtak -> blir sendt til beslutter
        ForeslaVedtakAksjonspunktDto foreslåDto = byggForeslåVedtakDto();

        // Act
        bekreftAksjonspunkt(klageBehandlingTrinn1, foreslåDto);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_KLAGE_NFP, UTFØRT),
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_KLAGE_NK, UTFØRT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, UTFØRT),
            AksjonspunktTestutfall.resultat(FATTER_VEDTAK, OPPRETTET)));

        // Arrange trinn 5: Beslutter godkjenner både nfp og nk
        AksjonspunktGodkjenningDto nfpGodkjent = byggGodkjenningDto(MANUELL_VURDERING_AV_KLAGE_NFP, true);
        AksjonspunktGodkjenningDto nkGodkjent = byggGodkjenningDto(MANUELL_VURDERING_AV_KLAGE_NK, true);
        FatterVedtakAksjonspunktDto fatteDto = byggFatteVedtakDto(asList(nfpGodkjent, nkGodkjent));

        // Act
        bekreftAksjonspunkt(klageBehandlingTrinn1, fatteDto);

        // Assert
        Behandling klagebehandlingTrinn4 = behandlingRepository.hentBehandling(klageBehandlingId);
        assertThat(klagebehandlingTrinn4.getBehandlingsresultat().getBehandlingResultatType()).isEqualTo(KLAGE_AVVIST);
        assertThat(klagebehandlingTrinn4.getStatus()).isEqualTo(BehandlingStatus.AVSLUTTET);
        assertThat(behandlingRepository.hentBehandling(klageBehandlingId).getBehandlendeEnhet()).isEqualTo(KLAGEINSTANS_ENHETS_ID);
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_KLAGE_NFP, UTFØRT),
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_KLAGE_NK, UTFØRT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, UTFØRT),
            AksjonspunktTestutfall.resultat(FATTER_VEDTAK, UTFØRT)));
    }

    @Test
    public void tilbakehopp_til_nk_når_nk_ikke_får_godkjent_i_totrinnskontroll() throws Exception {
        // Arrange - Starter fra foreslå vedtak. Oppretter klage behandling med NFP og NK stadfestet.
        ScenarioKlageEngangsstønad scenarioKlage = ScenarioKlageEngangsstønad.forStadfestetNK(ScenarioMorSøkerEngangsstønad.forFødsel()
            .medBruker(TpsRepo.STD_KVINNE_AKTØR_ID, NavBrukerKjønn.KVINNE)
            .medBehandlendeEnhet(FØRSTEGANGSSØKNAD_ENHETS_ID));
        Behandling klageBehandling = scenarioKlage.medBehandlendeEnhet(KLAGEINSTANS_ENHETS_ID)
            .lagre(repositoryProvider);
        Long klageBehandlingId = klageBehandling.getId();

        // Act
        utførProsessSteg(klageBehandlingId);
        ForeslaVedtakAksjonspunktDto foreslåDto = byggForeslåVedtakDto();

        bekreftAksjonspunkt(klageBehandling, foreslåDto);

        // Assert
        assertThat(behandlingRepository.hentBehandling(klageBehandlingId).getBehandlingsresultat().getBehandlingResultatType())
            .isEqualTo(KLAGE_YTELSESVEDTAK_STADFESTET);
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_KLAGE_NFP, UTFØRT),
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_KLAGE_NK, UTFØRT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, UTFØRT),
            AksjonspunktTestutfall.resultat(FATTER_VEDTAK, OPPRETTET)));

        // Arrange trinn 2: Beslutter godkjenner NFP men ikke NK -> blir sendt tilbake til NK for revurdering
        AksjonspunktGodkjenningDto nfpGodkjent = byggGodkjenningDto(MANUELL_VURDERING_AV_KLAGE_NFP, true);
        AksjonspunktGodkjenningDto nkIkkeGodkjent = byggGodkjenningDto(MANUELL_VURDERING_AV_KLAGE_NK, false);
        FatterVedtakAksjonspunktDto fatteDto = byggFatteVedtakDto(asList(nfpGodkjent, nkIkkeGodkjent));
        Behandling oppdatertBehandling = behandlingRepository.hentBehandling(klageBehandling.getId());

        // Act
        bekreftAksjonspunkt(oppdatertBehandling, fatteDto);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_KLAGE_NFP, UTFØRT),
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_KLAGE_NK, OPPRETTET),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, AVBRUTT),
            AksjonspunktTestutfall.resultat(FATTER_VEDTAK, AVBRUTT)));

        // Arrange trinn 3: NK bekrefte oppheve klage manuelt -> blir sendt til foreslå vedtak.
        KlageVurderingResultatNkAksjonspunktDto nkKlageVurderingAvvist2 = byggNkAksjonspunktDto(OPPHEVE_YTELSESVEDTAK,
            null);
        Behandling oppdatertBehandling2 = behandlingRepository.hentBehandling(klageBehandling.getId());

        // Act
        bekreftAksjonspunkt(oppdatertBehandling2, nkKlageVurderingAvvist2);

        // Assert
        Behandling klageBehandlingTrinn3 = behandlingRepository.hentBehandling(klageBehandlingId);
        assertThat(klageBehandlingTrinn3.getBehandlendeEnhet()).isEqualTo(KLAGEINSTANS_ENHETS_ID);
        assertThat(klageBehandlingTrinn3.getBehandlingsresultat().getBehandlingResultatType()).isEqualTo(KLAGE_YTELSESVEDTAK_OPPHEVET);
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_KLAGE_NFP, UTFØRT),
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_KLAGE_NK, UTFØRT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, OPPRETTET),
            AksjonspunktTestutfall.resultat(FATTER_VEDTAK, AVBRUTT)));
    }

    private void assertKlageVurderingResultat(Long behandlingId, KlageVurdertAv vurdertAv, KlageVurdering vurdering,
                                              KlageMedholdÅrsak medholdÅrsak, KlageAvvistÅrsak avvistÅrsak) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Optional<KlageVurderingResultat> klageVurderingResultat = behandling.hentKlageVurderingResultat(vurdertAv);

        if (klageVurderingResultat.isPresent()) {
            assertThat(klageVurderingResultat.get().getKlageVurdering()).isEqualTo(vurdering);
            if (medholdÅrsak != null) {
                assertThat(klageVurderingResultat.get().getKlageMedholdÅrsak()).isEqualTo(medholdÅrsak);
            } else if (avvistÅrsak != null) {
                assertThat(klageVurderingResultat.get().getKlageAvvistÅrsak()).isEqualTo(avvistÅrsak);
            }
        }
    }

    private AksjonspunktGodkjenningDto byggGodkjenningDto(AksjonspunktDefinisjon apDef, boolean godkjentAp) {
        AksjonspunktGodkjenningDto apGodkjenningDto = new AksjonspunktGodkjenningDto();
        apGodkjenningDto.setGodkjent(godkjentAp);
        apGodkjenningDto.setBegrunnelse("Begrunnelse apGodkjenningDto.");
        apGodkjenningDto.setAksjonspunktKode(apDef);

        return apGodkjenningDto;
    }

    private FatterVedtakAksjonspunktDto byggFatteVedtakDto(List<AksjonspunktGodkjenningDto> apGodkjenningDtosList) {
        return new FatterVedtakAksjonspunktDto("Fattet vedtak begrunnelse bla.", apGodkjenningDtosList);
    }

    private ForeslaVedtakAksjonspunktDto byggForeslåVedtakDto() {
        return new ForeslaVedtakAksjonspunktDto("begrunnelse i foreslå vedtak bla.", null, null, false);
    }

    private KlageVurderingResultatNfpAksjonspunktDto byggNfpAksjonspunktDto(KlageVurdering vurdering, KlageMedholdÅrsak medholdÅrsak) {

        KlageVurdering klageVurdering = vurdering;
        return new KlageVurderingResultatNfpAksjonspunktDto("Klage nfp begrunnelse bla. bla.", klageVurdering,
            medholdÅrsak, null, LocalDate.now());
    }

    private KlageVurderingResultatNkAksjonspunktDto byggNkAksjonspunktDto(KlageVurdering vurdering, KlageAvvistÅrsak avvistÅrsak) {

        KlageVurdering klageVurdering = vurdering;
        return new KlageVurderingResultatNkAksjonspunktDto("Klage nk begrunnelse bla. bla.", klageVurdering,
            null, avvistÅrsak, LocalDate.now());
    }

    private void utførProsessSteg(Long behandlingId) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);

        repository.flushAndClear();
    }

    private void bekreftAksjonspunkt(Behandling behandling, BekreftetAksjonspunktDto dto) throws URISyntaxException {
        aksjonspunktRestTjeneste.bekreft(BekreftedeAksjonspunkterDto.lagDto(behandling.getId(), behandling.getVersjon(), asList(dto)));
        kjørProsessTasks();
    }

    private void kjørProsessTasks() {
        new KjørProsessTasks(prosessTaskRepository).utførTasks();
    }
}
