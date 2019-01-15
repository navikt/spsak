package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InnhentDokumentTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.PayloadType;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.json.JacksonJsonConfig;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class HåndterMottattDokumentTaskTest {

    private static final JournalpostId JOURNALPOST_ID = new JournalpostId("2");
    private static final DokumentTypeId DOKUMENTTYPE = DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
    private static final LocalDate FORSENDELSE_MOTTATT = LocalDate.now();
    private static final String PAYLOAD_XML = "<test></test>";
    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private long FAGSAK_ID = 1L;
    private InnhentDokumentTjeneste innhentDokumentTjeneste;
    private HåndterMottattDokumentTask håndterMottattDokumentTask;
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());

    @Before
    public void before() {
        innhentDokumentTjeneste = mock(InnhentDokumentTjeneste.class);
        håndterMottattDokumentTask = new HåndterMottattDokumentTask(innhentDokumentTjeneste, repositoryProvider);
        final Personinfo personinfo = new Personinfo.Builder()
            .medAktørId(new AktørId("1"))
            .medPersonIdent(new PersonIdent("12345678901"))
            .medNavn("Kari Nordmann")
            .medFødselsdato(LocalDate.of(1999, 3, 3))
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medForetrukketSpråk(Språkkode.nb)
            .build();
        FAGSAK_ID = repositoryProvider.getFagsakRepository().opprettNy(Fagsak.opprettNy(NavBruker.opprettNy(personinfo)));
    }

    @Test
    public void skal_kalle_InnhentDokumentTjeneste_med_argumenter_fra_ProsessTask() throws Exception {
        // Arrange
        InngåendeSaksdokument mottattDokument = new InngåendeSaksdokument.Builder()
            .medFagsakId(FAGSAK_ID)
            .medJournalpostId(JOURNALPOST_ID)
            .medDokumentTypeId(DOKUMENTTYPE)
            .medMottattDato(FORSENDELSE_MOTTATT)
            .medPayload(PayloadType.XML, PAYLOAD_XML)
            .build();


        final BehandlingTema behandlingTema = kodeverkRepository.finn(BehandlingTema.class, BehandlingTema.SYKEPENGER);
        ProsessTaskData prosessTask = new ProsessTaskData(HåndterMottattDokumentTaskProperties.TASKTYPE);
        prosessTask.setFagsakId(FAGSAK_ID);
        prosessTask.setPayload(JacksonJsonConfig.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(mottattDokument));
        prosessTask.setProperty(HåndterMottattDokumentTaskProperties.BEHANDLINGSTEMA_OFFISIELL_KODE_KEY, behandlingTema.getOffisiellKode());
        prosessTask.setProperty(HåndterMottattDokumentTaskProperties.BEHANDLING_ÅRSAK_TYPE_KEY, BehandlingÅrsakType.UDEFINERT.getKode());
        ArgumentCaptor<InngåendeSaksdokument> captor = ArgumentCaptor.forClass(InngåendeSaksdokument.class);

        // Act
        håndterMottattDokumentTask.doTask(prosessTask);

        // Assert
        verify(innhentDokumentTjeneste).utfør(captor.capture(), any(BehandlingÅrsakType.class));
    }
}
