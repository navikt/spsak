package no.nav.foreldrepenger.behandlingslager.behandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class HistorikkRepositoryImplTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final Repository repository = repoRule.getRepository();
    private final Saksnummer saksnummer  = new Saksnummer("3");
    private final Fagsak fagsak = FagsakBuilder.nyFagsak().medBruker(NavBruker.opprettNy(new AktørId("909"))).medSaksnummer(saksnummer).build();

    private final EntityManager entityManager = repoRule.getEntityManager();
    private final HistorikkRepository historikkRepository = new HistorikkRepositoryImpl(entityManager);

    @Test
    public void lagrerHistorikkinnslag() {
        repository.lagre(fagsak.getNavBruker());
        repository.lagre(fagsak);
        Behandling behandling = Behandling.forFørstegangssøknad(fagsak).build();
        repository.lagre(behandling);
        repository.flush();

        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setAktør(HistorikkAktør.SØKER);
        historikkinnslag.setBehandling(behandling);
        historikkinnslag.setType(HistorikkinnslagType.VEDTAK_FATTET);
        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder()
            .medHendelse(HistorikkinnslagType.VEDTAK_FATTET)
            .medSkjermlenke(SkjermlenkeType.VEDTAK);
        builder.build(historikkinnslag);

        historikkRepository.lagre(historikkinnslag);
        List<Historikkinnslag> historikk = historikkRepository.hentHistorikk(behandling.getId());
        assertThat(historikk).hasSize(1);

        Historikkinnslag lagretHistorikk = historikk.get(0);
        assertThat(lagretHistorikk.getAktør().getKode()).isEqualTo(historikkinnslag.getAktør().getKode());
        assertThat(lagretHistorikk.getType().getKode()).isEqualTo(historikkinnslag.getType().getKode());
    }

    @Test
    public void henterAlleHistorikkinnslagForBehandling() {
        repository.lagre(fagsak.getNavBruker());
        repository.lagre(fagsak);
        Behandling behandling = Behandling.forFørstegangssøknad(fagsak).build();
        repository.lagre(behandling);
        repository.flush();

        Historikkinnslag vedtakFattet = new Historikkinnslag();
        vedtakFattet.setAktør(HistorikkAktør.SØKER);
        vedtakFattet.setBehandling(behandling);
        vedtakFattet.setType(HistorikkinnslagType.VEDTAK_FATTET);
        HistorikkInnslagTekstBuilder vedtakFattetBuilder = new HistorikkInnslagTekstBuilder()
            .medHendelse(HistorikkinnslagType.VEDTAK_FATTET)
            .medSkjermlenke(SkjermlenkeType.VEDTAK);
        vedtakFattetBuilder.build(vedtakFattet);
        historikkRepository.lagre(vedtakFattet);

        Historikkinnslag brevSent = new Historikkinnslag();
        brevSent.setBehandling(behandling);
        brevSent.setType(HistorikkinnslagType.BREV_SENT);
        brevSent.setAktør(HistorikkAktør.SØKER);
        HistorikkInnslagTekstBuilder mottattDokBuilder = new HistorikkInnslagTekstBuilder()
            .medHendelse(HistorikkinnslagType.BREV_SENT);
        mottattDokBuilder.build(brevSent);
        historikkRepository.lagre(brevSent);

        List<Historikkinnslag> historikk = historikkRepository.hentHistorikk(behandling.getId());
        assertThat(historikk).hasSize(2);
        assertThat(historikk.stream().anyMatch(h -> HistorikkinnslagType.VEDTAK_FATTET.equals(h.getType()))).isTrue();
        assertThat(historikk.stream().anyMatch(h -> HistorikkinnslagType.BREV_SENT.equals(h.getType()))).isTrue();
    }
}
