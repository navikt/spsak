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
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InnhentDokumentTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.DokumentPersistererTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.DokumentPersistererTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class HåndterMottattDokumentTaskTest {

    private static final JournalpostId JOURNALPOST_ID = new JournalpostId("2");
    private static final DokumentTypeId DOKUMENTTYPE = DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL;
    private static final LocalDate FORSENDELSE_MOTTATT = LocalDate.now();
    private static final String PAYLOAD_XML = "<test></test>";
    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private long FAGSAK_ID = 1L;
    private InnhentDokumentTjeneste innhentDokumentTjeneste;
    private HåndterMottattDokumentTask håndterMottattDokumentTask;
    private MottatteDokumentTjeneste mottatteDokumentTjeneste;
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());
    private MottatteDokumentRepository mottatteDokumentRepository = new MottatteDokumentRepositoryImpl(repoRule.getEntityManager());
    private DokumentPersistererTjeneste dokumentPersistererTjeneste = new DokumentPersistererTjenesteImpl();

    @Before
    public void before() {
        final Integer FRIST_INNSENDING_DOK = 6;
        innhentDokumentTjeneste = mock(InnhentDokumentTjeneste.class);
        mottatteDokumentTjeneste = new MottatteDokumentTjenesteImpl(FRIST_INNSENDING_DOK, dokumentPersistererTjeneste, mottatteDokumentRepository, repositoryProvider);
        håndterMottattDokumentTask = new HåndterMottattDokumentTask(innhentDokumentTjeneste, mottatteDokumentTjeneste, repositoryProvider);
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
        MottattDokument mottattDokument = new MottattDokument.Builder()
            .medFagsakId(FAGSAK_ID)
            .medJournalPostId(JOURNALPOST_ID)
            .medDokumentTypeId(DOKUMENTTYPE)
            .medMottattDato(FORSENDELSE_MOTTATT)
            .medXmlPayload(PAYLOAD_XML)
            .medElektroniskRegistrert(true)
            .build();

        Long dokumentId = mottatteDokumentTjeneste.lagreMottattDokumentPåFagsak(FAGSAK_ID, mottattDokument);

        final BehandlingTema behandlingTema = kodeverkRepository.finn(BehandlingTema.class, BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        ProsessTaskData prosessTask = new ProsessTaskData(HåndterMottattDokumentTaskProperties.TASKTYPE);
        prosessTask.setFagsakId(FAGSAK_ID);
        prosessTask.setProperty(HåndterMottattDokumentTaskProperties.MOTTATT_DOKUMENT_ID_KEY, dokumentId.toString());
        prosessTask.setProperty(HåndterMottattDokumentTaskProperties.BEHANDLINGSTEMA_OFFISIELL_KODE_KEY, behandlingTema.getOffisiellKode());
        prosessTask.setProperty(HåndterMottattDokumentTaskProperties.BEHANDLING_ÅRSAK_TYPE_KEY, BehandlingÅrsakType.UDEFINERT.getKode());
        ArgumentCaptor<MottattDokument> captor = ArgumentCaptor.forClass(MottattDokument.class);

        // Act
        håndterMottattDokumentTask.doTask(prosessTask);

        // Assert
        verify(innhentDokumentTjeneste).utfør(captor.capture(), any(BehandlingÅrsakType.class));
    }
}
