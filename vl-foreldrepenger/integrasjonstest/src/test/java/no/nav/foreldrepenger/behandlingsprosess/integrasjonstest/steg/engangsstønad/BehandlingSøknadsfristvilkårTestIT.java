package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.engangsstønad;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestUtils.hentFødselsdatoFraFnr;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.VilkårTestutfall.resultat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
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
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.DokumentmottakTestUtil;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.SoknadsfristAksjonspunktDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Vedlegg;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;

@RunWith(CdiRunner.class)
public class BehandlingSøknadsfristvilkårTestIT {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();
    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    @Inject
    private FagsakRepository fagsakRepository;
    @Inject
    private ProsessTaskRepository prosessTaskRepository;
    @Inject
    private BehandlingRepository behandlingRepository;
    @Inject
    private AksjonspunktApplikasjonTjeneste applikasjonstjeneste;
    @Inject
    private DokumentmottakTestUtil hjelper;
    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);
    private BehandlingskontrollKontekst kontekst;

    @Test
    public void fødselsvilkår_hvor_søknadsfrist_er_oppfylt() throws Exception {
        // Arrange
        LocalDate fødselsdato = hentFødselsdatoFraFnr(TpsRepo.STD_BARN_FNR);
        LocalDate mottattDato = fødselsdato.plusMonths(6); // <= 6 mnd oppfyller søknadsfristvilkåret
        boolean elektroniskSøknad = true;
        Fagsak fagsak = byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadMorFødsel(fødselsdato, mottattDato, 1, fagsak.getAktørId(), emptyList()), false,
            elektroniskSøknad);

        // Trinn 1: Kjør behandling -> Innvilges direkte, ettersom barn er registrert i TPS OG søknadsfrist er oppfylt
        utførProsessSteg(behandlingId);
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT),
                resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT),
                resultat(VilkårType.SØKNADSFRISTVILKÅRET, VilkårUtfallType.OPPFYLT)));
        assertBehandlingVedtak(VedtakResultatType.INNVILGET);
    }

    @Test
    public void fødselsvilkår_hvor_søknadsfrist_ikke_er_oppfylt() throws Exception {
        // Arrange
        LocalDate fødselsdato = hentFødselsdatoFraFnr(TpsRepo.STD_BARN_FNR);
        LocalDate mottattDato = fødselsdato.plusMonths(6).plusDays(1); // > 6 mnd oppfyller ikke søknadsfristvilkåret
        boolean elektroniskSøknad = true;
        Fagsak fagsak = byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadMorFødsel(fødselsdato, mottattDato, 1, fagsak.getAktørId(), emptyList()), false,
            elektroniskSøknad);

        // Trinn 1: Kjør behandling -> Ikke fastsatt resultat; søknadsfrist ikke er oppfylt og settes til IKKE_VURDERT
        utførProsessSteg(behandlingId);
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT),
                resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT),
                resultat(VilkårType.SØKNADSFRISTVILKÅRET, VilkårUtfallType.IKKE_VURDERT, VilkårUtfallMerknad.VM_5007)));
        assertThat(repository.hentAlle(BehandlingVedtak.class)).hasSize(0);

        Vilkår søknadsvilkåret = hentSøknadsfristVilkår();
        assertThat(søknadsvilkåret.getMerknadParametere()).contains(entry("antallDagerSoeknadLevertForSent", "1"));

        // Trinn 2: Bekreft søknadsfristvilkåret fra GUI -> Søknadsfristvilkåret innvilges
        applikasjonstjeneste.bekreftAksjonspunkter(singletonList(
            byggSoknadsfristAksjonspunktDto(behandlingId, true)), behandlingId);
        utførProsessSteg(behandlingId);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT),
                resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT),
                resultat(VilkårType.SØKNADSFRISTVILKÅRET, VilkårUtfallType.OPPFYLT)));
    }

    @Test
    public void fødselsvilkår_hvor_søknadsfrist_manuelt_registrert_er_oppfylt() throws Exception {
        // Arrange
        LocalDate fødselsdato = hentFødselsdatoFraFnr(TpsRepo.STD_BARN_FNR);
        LocalDate mottattDato = fødselsdato.plusMonths(6).plusDays(2); // <= 6 mnd + 2 dager for man. registrert søknad
        boolean elektroniskSøknad = false;
        Fagsak fagsak = byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadMorFødsel(fødselsdato, mottattDato, 1, fagsak.getAktørId(), emptyList()), false,
            elektroniskSøknad);

        // Trinn 1: Kjør behandling -> Innvilges direkte, ettersom barn er registrert i TPS OG søknadsfrist er oppfylt
        utførProsessSteg(behandlingId);
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT),
                resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT),
                resultat(VilkårType.SØKNADSFRISTVILKÅRET, VilkårUtfallType.OPPFYLT)));
        assertBehandlingVedtak(VedtakResultatType.INNVILGET);
    }

    @Test
    public void fødselsvilkår_hvor_søknadsfrist_manuelt_registrert_ikke_er_oppfylt() throws Exception {
        // Arrange
        LocalDate fødselsdato = hentFødselsdatoFraFnr(TpsRepo.STD_BARN_FNR);
        LocalDate mottattDato = fødselsdato.plusMonths(6)
            .plusDays(3); // > 6 mnd + 2 dager for man. registrert søknad

        boolean elektroniskSøknad = false;
        Fagsak fagsak = byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadMorFødsel(fødselsdato, mottattDato, 1, fagsak.getAktørId(), emptyList()), false,
            elektroniskSøknad);

        // Trinn 1: Kjør behandling -> Ikke fastsatt resultat; søknadsfrist ikke er oppfylt og settes til IKKE_VURDERT
        utførProsessSteg(behandlingId);
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT),
                resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT),
                resultat(VilkårType.SØKNADSFRISTVILKÅRET, VilkårUtfallType.IKKE_VURDERT, VilkårUtfallMerknad.VM_5007)));
        assertThat(repository.hentAlle(BehandlingVedtak.class)).hasSize(0);

        Vilkår søknadsvilkåret = hentSøknadsfristVilkår();
        assertThat(søknadsvilkåret.getMerknadParametere()).contains(entry("antallDagerSoeknadLevertForSent", "1"));

        // Trinn 2: Repetisjon som for elektronisk søknad
    }

    private Vilkår hentSøknadsfristVilkår() {
        return repository.hentAlle(VilkårResultat.class).stream()
            .flatMap(res -> res.getVilkårene().stream())
            .filter(v -> v.getVilkårType().equals(VilkårType.SØKNADSFRISTVILKÅRET))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Skal ikke havne her"));
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

    private Fagsak byggFagsak(AktørId aktørId, RelasjonsRolleType rolle, NavBrukerKjønn kjønn) {
        NavBruker navBruker = new NavBrukerBuilder()
            .medAktørId(aktørId)
            .medKjønn(kjønn)
            .build();
        Fagsak fagsak = FagsakBuilder.nyEngangstønad(rolle)
            .medBruker(navBruker)
            .medSaksnummer(new Saksnummer("123"))
            .build();
        fagsakRepository.opprettNy(fagsak);

        return fagsak;
    }

    private SoknadsfristAksjonspunktDto byggSoknadsfristAksjonspunktDto(Long behandlingId, boolean vilkårOk) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Stream<Aksjonspunkt> streamAp = behandling.getAksjonspunkter().stream();
        Long aksjonspunktId = streamAp.filter(aksjonspunkt -> aksjonspunkt.getAksjonspunktDefinisjon().equals(MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET))
            .map(Aksjonspunkt::getId)
            .findFirst()
            .orElse(null);
        assertThat(aksjonspunktId).isNotNull();

        return new SoknadsfristAksjonspunktDto("Grunn", vilkårOk);
    }

    private void utførProsessSteg(Long behandlingId) {
        kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);

        repository.flushAndClear();
    }

    private void assertBehandlingVedtak(VedtakResultatType vedtakResultatType) {
        List<BehandlingVedtak> vedtakene = repository.hentAlle(BehandlingVedtak.class);
        assertThat(vedtakene).hasSize(1);
        assertThat(vedtakene.get(0).getVedtakResultatType()).isEqualTo(vedtakResultatType);
    }
}
