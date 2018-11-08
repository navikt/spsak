package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Journalpost;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.OpprettSakDtoBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.KjørProsessTasks;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.foreldrepenger.kontrakter.fordel.OpprettSakDto;
import no.nav.foreldrepenger.web.app.tjenester.fordeling.FordelRestTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

/**
 * Wrapper rundt SUT {@link FordelRestTjeneste} for testformål.
 * Kaller tjenesten som utenfra, i tillegg fasiliteres prosesskjøring og testbuildere for DTO-er
 */
@ApplicationScoped
public class FordelRestTjenesteTestAPI {

    private BehandlingRepository behandlingRepository;


    private FordelRestTjeneste fordelRestTjeneste;
    private ProsessTaskRepository prosessTaskRepository;
    private BehandlingRepositoryProvider repoProvider;
    private FagsakRepository fagsakRepository;

    public FordelRestTjenesteTestAPI() {
        // For CDI
    }

    @Inject
    public FordelRestTjenesteTestAPI(FordelRestTjeneste fordelRestTjeneste, ProsessTaskRepository prosessTaskRepository, BehandlingRepositoryProvider provider) {
        this.behandlingRepository = provider.getBehandlingRepository();
        this.fordelRestTjeneste = fordelRestTjeneste;
        this.prosessTaskRepository = prosessTaskRepository;
        this.repoProvider = provider;
        this.fagsakRepository = provider.getFagsakRepository();
    }

    public Fagsak opprettSak(AktørId aktørId, BehandlingTema behandlingTema) {
        OpprettSakDto dto = OpprettSakDtoBuilder.builder(repoProvider)
            .medBehandlingstema(behandlingTema)
            .medAktørId(aktørId)
            .build();

        // SUT
        fordelRestTjeneste.opprettSak(dto);

        JournalpostId jornalpostId = dto.getJournalpostId()
            .map(journalId -> new JournalpostId(journalId))
            .orElseThrow(() -> new IllegalStateException("Journalpost må oppgis  i test"));
        return fagsakRepository.hentJournalpost(jornalpostId)
            .map(Journalpost::getFagsak)
            .orElseThrow(() -> new IllegalStateException("Finner ikke fagsak i testoppsett"));
    }

    public Long mottaJournalpost(JournalpostMottakDtoBuilder dtoBuilder) {
        JournalpostMottakDto dto = dtoBuilder.build();

        // SUT
        fordelRestTjeneste.mottaJournalpost(dto);
        new KjørProsessTasks(prosessTaskRepository).utførAlleTasks();

        Fagsak fagsak = fagsakRepository.hentSakGittSaksnummer(new Saksnummer(dto.getSaksnummer()))
            .orElseThrow(() -> new IllegalStateException("Fant ikke fagsak i testoppsett for mottak av journalpost"));
        Behandling behandling = behandlingRepository.hentSisteBehandlingForFagsakId(fagsak.getId())
            .orElseThrow(() -> new IllegalStateException("Fant ikke behandling i testoppsett for mottak av journalpost"));

        return behandling.getId();
    }

    public boolean validerProsessTaskProperty(String tasktype, String property, String value) {
       return new KjørProsessTasks(prosessTaskRepository).validerTaskProperty(tasktype, property, value);
    }
}
