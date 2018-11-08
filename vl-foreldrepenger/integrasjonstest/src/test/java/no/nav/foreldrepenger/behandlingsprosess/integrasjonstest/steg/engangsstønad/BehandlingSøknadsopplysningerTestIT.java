package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.engangsstønad;


import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.REGISTRER_PAPIRSØKNAD_ENGANGSSTØNAD;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.SØKERS_OPPLYSNINGSPLIKT_MANU;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.OPPRETTET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.UTFØRT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType.IKKE_FASTSATT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.SØKERSOPPLYSNINGSPLIKT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_VURDERT;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall.resultat;
import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.DokumentmottakTestUtil;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.VilkårTestutfall;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InnhentDokumentTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.AksjonspunktRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.BekreftedeAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringEngangsstonadDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjeneste;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;

@RunWith(CdiRunner.class)
public class BehandlingSøknadsopplysningerTestIT {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);

    @Inject
    private AksjonspunktApplikasjonTjeneste applikasjonstjeneste;
    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    @Inject
    private BehandlingRepository behandlingRepository;
    @Inject
    private BehandlingsutredningApplikasjonTjeneste behandlingsutredningApplikasjonTjeneste;
    @Inject
    private InnhentDokumentTjeneste innhentDokumentTjeneste;
    @Inject
    private FagsakRepository fagsakRepository;
    @Inject
    private MottatteDokumentRepository mottatteDokumentRepository;
    @Inject
    private TotrinnTjeneste totrinnTjeneste;

    private AksjonspunktRestTjeneste aksjonspunktRestTjeneste;

    @Before
    public void setup() {
        aksjonspunktRestTjeneste = new AksjonspunktRestTjeneste(applikasjonstjeneste, behandlingRepository, behandlingsutredningApplikasjonTjeneste, totrinnTjeneste);
    }

    @Test
    public void skal_opprette_aksjonspunkt_REGISTRER_PAPIRSØKNAD_ENGANGSSTØNAD_og_hoppe_til_søkers_opplysningsplikt_når_saksbehandler_velger_å_lukke_sak_på_bakgrunn_av_manglende_dokumentasjon() throws Exception {
        // Arrange steg 1: Opprett søknad som er papirsøknad (payload  er tom)
        Fagsak fagsak = byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        MottattDokument mottattDokument = mottatteDokumentRepository.lagre(byggDokumentRequest(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL, fagsak.getId()));

        // Act
        innhentDokumentTjeneste.utfør(mottattDokument, BehandlingÅrsakType.UDEFINERT);
        Behandling behandling = behandlingRepository.hentSisteBehandlingForFagsakId(fagsak.getId())
            .orElseThrow(() -> new IllegalStateException("Skal ikke kunne havne her"));
        utførProsessSteg(behandling.getId());

        // Assert
        assertUtil.assertAksjonspunkter(
            resultat(REGISTRER_PAPIRSØKNAD_ENGANGSSTØNAD, OPPRETTET));

        // Arrange steg 2: Saksbehandler huker av for ufullstendig søknad -> Skal fremoverhoppe til Søkers opplysningsp.
        behandling = behandlingRepository.hentBehandling(behandling.getId());
        ManuellRegistreringDto manuellRegistreringDto = new ManuellRegistreringEngangsstonadDto();
        manuellRegistreringDto.setUfullstendigSoeknad(true);

        // Act
        aksjonspunktRestTjeneste
                .bekreft(BekreftedeAksjonspunkterDto.lagDto(behandling.getId(), behandling.getVersjon(), asList(manuellRegistreringDto)));

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            resultat(REGISTRER_PAPIRSØKNAD_ENGANGSSTØNAD, UTFØRT),
            resultat(SØKERS_OPPLYSNINGSPLIKT_MANU, OPPRETTET)));
        assertUtil.assertVilkårresultatOgRegelmerknad(IKKE_FASTSATT,
            VilkårTestutfall.resultat(SØKERSOPPLYSNINGSPLIKT, IKKE_VURDERT));

        Behandling sisteVersjon = behandlingRepository.hentBehandling(behandling.getId());
        assertThat(sisteVersjon.getAktivtBehandlingSteg()).isEqualTo(BehandlingStegType.KONTROLLERER_SØKERS_OPPLYSNINGSPLIKT);
    }

    private MottattDokument byggDokumentRequest(DokumentTypeId dokumentTypeId, Long fagsakId) {
        return DokumentmottakTestUtil.lagMottattDokument(dokumentTypeId, fagsakId, null, now(), false, null);
    }

    private Fagsak byggFagsak(AktørId aktørId, RelasjonsRolleType rolle, NavBrukerKjønn kjønn) {
        NavBruker navBruker = new NavBrukerBuilder()
            .medAktørId(aktørId)
            .medKjønn(kjønn)
            .build();
        Fagsak fagsak = FagsakBuilder.nyEngangstønad(rolle)
            .medSaksnummer(new Saksnummer("123"))
            .medBruker(navBruker).build();
        fagsakRepository.opprettNy(fagsak);
        return fagsak;
    }

    private void utførProsessSteg(Long behandlingId) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);

        repository.flushAndClear();
    }
}
