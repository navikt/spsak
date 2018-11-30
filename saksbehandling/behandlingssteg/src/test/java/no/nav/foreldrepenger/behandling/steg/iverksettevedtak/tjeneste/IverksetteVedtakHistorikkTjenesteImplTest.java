package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.IverksetteVedtakHistorikkTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class IverksetteVedtakHistorikkTjenesteImplTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final Repository repository = repoRule.getRepository();
    private final Saksnummer saksnummer = new Saksnummer("3");
    private final Fagsak fagsak = FagsakBuilder.nyFagsak().medBruker(NavBruker.opprettNy(new AktørId("99"))).medSaksnummer(saksnummer).build();

    private final EntityManager entityManager = repoRule.getEntityManager();
    private final HistorikkRepository historikkRepository = new HistorikkRepositoryImpl(entityManager);

    private IverksetteVedtakHistorikkTjeneste iverksetteVedtakHistorikkTjeneste = new IverksetteVedtakHistorikkTjenesteImpl(historikkRepository);


    @Test
    public void skal_opprette_historikkinnslag_når_iverksettelse_på_vent_pga_annen_behandling() {

        //Arrange
        Behandling behandling = opprettOgLagreBehandling();

        //Act
        iverksetteVedtakHistorikkTjeneste.opprettHistorikkinnslagNårIverksettelsePåVent(behandling, false, false);

        //Assert
        Historikkinnslag historikkinnslag = historikkRepository.hentHistorikk(behandling.getId()).get(0);
        assertThat(historikkinnslag.getHistorikkinnslagDeler().size()).isEqualTo(1);
        assertThat(historikkinnslag.getHistorikkinnslagDeler().get(0).getHendelse()).hasValueSatisfying(hendelse ->
            assertThat(hendelse.getNavn()).as("navn").isEqualTo(HistorikkinnslagType.IVERKSETTELSE_VENT.getKode()));
        assertThat(historikkinnslag.getHistorikkinnslagDeler().get(0).getAarsak().get()).isEqualTo(Venteårsak.VENT_INFOTRYGD.getKode());
    }

    private Behandling opprettOgLagreBehandling() {
        repository.lagre(fagsak.getNavBruker());
        repository.lagre(fagsak);
        Behandling behandling = Behandling.forFørstegangssøknad(fagsak).build();
        repository.lagre(behandling);
        repository.flush();
        return behandling;
    }

    @Test
    public void skal_opprette_historikk_når_iverksettelse_på_vent_pga_infotrygd() {

        Behandling behandling = opprettOgLagreBehandling();

        //Act
        iverksetteVedtakHistorikkTjeneste.opprettHistorikkinnslagNårIverksettelsePåVent(behandling, true, true);

        //Assert
        Historikkinnslag historikkinnslag = historikkRepository.hentHistorikk(behandling.getId()).get(0);
        assertThat(historikkinnslag.getHistorikkinnslagDeler().size()).isEqualTo(1);
        assertThat(historikkinnslag.getHistorikkinnslagDeler().get(0).getHendelse()).hasValueSatisfying(hendelse ->
            assertThat(hendelse.getNavn()).as("navn").isEqualTo(HistorikkinnslagType.IVERKSETTELSE_VENT.getKode()));
        assertThat(historikkinnslag.getHistorikkinnslagDeler().get(0).getAarsak().get()).isEqualTo(Venteårsak.VENT_TIDLIGERE_BEHANDLING.getKode());
    }

    @Test
    public void skal_opprette_historikkinnslag_når_iverksettelse_på_vent_pga_både_infotrygd_og_annen_behandling() {

        //Arrange
        Behandling behandling = opprettOgLagreBehandling();

        //Act
        iverksetteVedtakHistorikkTjeneste.opprettHistorikkinnslagNårIverksettelsePåVent(behandling, true, false);

        //Assert
        Historikkinnslag historikkinnslag = historikkRepository.hentHistorikk(behandling.getId()).get(0);
        assertThat(historikkinnslag.getHistorikkinnslagDeler().size()).isEqualTo(2);
        assertThat(historikkinnslag.getHistorikkinnslagDeler().get(0).getHendelse()).hasValueSatisfying(hendelse ->
            assertThat(hendelse.getNavn()).as("navn").isEqualTo(HistorikkinnslagType.IVERKSETTELSE_VENT.getKode()));
        assertThat(historikkinnslag.getHistorikkinnslagDeler().get(0).getAarsak().get()).isEqualTo(Venteårsak.VENT_TIDLIGERE_BEHANDLING.getKode());
        assertThat(historikkinnslag.getHistorikkinnslagDeler().get(1).getAarsak().get()).isEqualTo(Venteårsak.VENT_INFOTRYGD.getKode());
    }

}
