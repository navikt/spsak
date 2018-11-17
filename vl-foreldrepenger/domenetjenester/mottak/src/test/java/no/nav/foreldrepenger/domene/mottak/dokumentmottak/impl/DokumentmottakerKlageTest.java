package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import static java.time.LocalDate.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Properties;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepository;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.BehandlendeEnhetTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class DokumentmottakerKlageTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Inject
    private BehandlingModellRepository behandlingModellRepository;

    @Inject
    private ProsessTaskRepository prosessTaskRepository;

    @Inject
    private BeregningRepository beregningRepository;

    @Inject
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    private DokumentmottakerKlage dokumentmottaker;
    private BehandlingRepository behandlingRepository;

    @Before
    public void oppsett() {
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        MottatteDokumentTjeneste mottatteDokumentTjeneste = mock(MottatteDokumentTjeneste.class);
        prosessTaskRepository = mock(ProsessTaskRepository.class);
        BehandlendeEnhetTjeneste behandlendeEnhetTjeneste = mock(BehandlendeEnhetTjeneste.class);
        historikkinnslagTjeneste = mock(HistorikkinnslagTjeneste.class);
        when(behandlendeEnhetTjeneste.sjekkEnhetVedNyAvledetBehandling(any(), any())).thenReturn(new OrganisasjonsEnhet("4802", "NAV Bærum"));

        BehandlingskontrollTjeneste behandlingskontrollTjeneste = DokumentmottakTestUtil.lagBehandlingskontrollTjenesteMock(repositoryProvider, behandlingModellRepository);

        DokumentmottakerFelles dokumentmottakerFelles = new DokumentmottakerFelles(repositoryProvider, prosessTaskRepository,
            behandlendeEnhetTjeneste, historikkinnslagTjeneste);
        dokumentmottakerFelles = Mockito.spy(dokumentmottakerFelles);

        dokumentmottaker = new DokumentmottakerKlage(repositoryProvider, behandlingskontrollTjeneste, dokumentmottakerFelles, mottatteDokumentTjeneste);
        dokumentmottaker = Mockito.spy(dokumentmottaker);
    }

    @Test
    public void skal_starte_behandling_av_klage() {
        //Arrange
        Behandling behandling = byggAvsluttetSøknadsbehandlingForFødsel(1);
        Fagsak fagsak = behandling.getFagsak();
        Long fagsakId = fagsak.getId();
        DokumentTypeId dokumentTypeId = DokumentTypeId.KLAGE_DOKUMENT;

        MottattDokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, fagsakId, "", now(), true, "123");

        //Act
        dokumentmottaker.mottaDokument(mottattDokument, fagsak, dokumentTypeId, BehandlingÅrsakType.UDEFINERT);

        //Assert
        verify(dokumentmottaker).startBehandlingAvKlage(mottattDokument, fagsak);
    }

    private Behandling byggAvsluttetSøknadsbehandlingForFødsel(int antallBarn) {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();

        Behandling behandling = scenario
            .medBehandlingStegStart(BehandlingStegType.FATTE_VEDTAK)
            .lagre(repositoryProvider);
        Fagsak fagsak = behandling.getFagsak();
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingRepository.taSkriveLås(behandling));

        Behandlingsresultat.builderForInngangsvilkår()
            .medBehandlingResultatType(BehandlingResultatType.INNVILGET)
            .buildFor(behandling);
        VilkårResultat.builder()
            .leggTilVilkårResultat(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT,
                null, new Properties(), null, false, false, "", "")
            .medVilkårResultatType(VilkårResultatType.INNVILGET)
            .buildFor(behandling);
        BeregningResultat.builder()
            .medBeregning(new Beregning(48500L, 1L, 48500L, LocalDateTime.now()))
            .buildFor(behandling);
        BehandlingVedtak vedtak = BehandlingVedtak.builder()
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medBehandlingsresultat(behandling.getBehandlingsresultat())
            .medIverksettingStatus(IverksettingStatus.IVERKSATT)
            .medVedtaksdato(LocalDate.now())
            .medAnsvarligSaksbehandler("VL")
            .build();
        behandling.avsluttBehandling();
        BehandlingLås lås = kontekst.getSkriveLås();
        behandlingRepository.lagre(behandling.getBehandlingsresultat().getVilkårResultat(), lås);
        beregningRepository.lagre(behandling.getBehandlingsresultat().getBeregningResultat(), lås);
        behandlingRepository.lagre(behandling, lås);
        repositoryProvider.getBehandlingVedtakRepository().lagre(vedtak, lås);
        return behandling;
    }
}
