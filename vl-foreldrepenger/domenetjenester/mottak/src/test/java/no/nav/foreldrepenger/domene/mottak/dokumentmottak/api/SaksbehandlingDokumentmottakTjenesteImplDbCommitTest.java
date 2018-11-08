package no.nav.foreldrepenger.domene.mottak.dokumentmottak.api;

import java.time.LocalDate;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.SaksbehandlingDokumentmottakTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.SaksbehandlingDokumentmottakTjenesteImpl;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl.MottatteDokumentTjenesteImpl;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.DokumentPersistererTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.DokumentPersistererTjenesteImpl;
import no.nav.foreldrepenger.domene.mottak.søknad.SoeknadsskjemaEngangsstoenadTestdataBuilder;
import no.nav.foreldrepenger.domene.mottak.søknad.SøknadTestdataBuilder;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.ObjectFactory;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import no.nav.vedtak.felles.testutilities.db.Commit;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;

/**
 * Testen kjøres manuelt for å trigge oppretting av en HåndterMottattDokumentTask i databasen.
 * Kan slettes hvis det blir implementert et REST-grensesnitt mot SaksbehandlingDokumentmottakTjeneste.
 */
@Ignore("Kun for manuell kjøring")
public class SaksbehandlingDokumentmottakTjenesteImplDbCommitTest {

    private static final Long FAGSAK_ID = 201L;
    private static final JournalpostId JOURNALPOST_ID = new JournalpostId("2");
    private static final BehandlingTema BEHANDLINGTEMA = BehandlingTema.ENGANGSSTØNAD_FØDSEL;
    private static final DokumentTypeId DOKUMENTTYPE = DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL;
    private static final LocalDate FORSENDELSE_MOTTATT = LocalDate.now();

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();

    private SaksbehandlingDokumentmottakTjeneste tjeneste;

    @Before
    public void before() {
        final int FRIST_INNSENDING_UKER = 6;

        ProsessTaskRepository prosessTaskRepository = new ProsessTaskRepositoryImpl(entityManager, null);
        MottatteDokumentRepository mottatteDokumentRepository = new MottatteDokumentRepositoryImpl(entityManager);
        DokumentPersistererTjeneste dokumentPersistererTjeneste = new DokumentPersistererTjenesteImpl();
        BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
        MottatteDokumentTjeneste mottatteDokumentTjeneste = new MottatteDokumentTjenesteImpl(FRIST_INNSENDING_UKER, dokumentPersistererTjeneste, mottatteDokumentRepository, repositoryProvider);
        tjeneste = new SaksbehandlingDokumentmottakTjenesteImpl(prosessTaskRepository, mottatteDokumentTjeneste);
    }

    @Test
    @Commit
    public void skal_ta_i_mot_søknad_og_opprette_prosesstask() throws Exception {
        SoeknadsskjemaEngangsstoenad søknad = new SoeknadsskjemaEngangsstoenadTestdataBuilder().søknadAdopsjonEngangsstønadMor().build();
        String søknadXml = JaxbHelper.marshalJaxb(SoeknadsskjemaEngangsstoenad.class, new ObjectFactory().createSoeknadsskjemaEngangsstoenad(søknad));
        InngåendeSaksdokument saksdokument = InngåendeSaksdokument.builder()
                .medFagsakId(FAGSAK_ID)
                .medJournalpostId(JOURNALPOST_ID)
                .medBehandlingTema(BEHANDLINGTEMA)
                .medDokumentTypeId(DOKUMENTTYPE)
                .medForsendelseMottatt(FORSENDELSE_MOTTATT)
                .medPayloadXml(søknadXml)
                .build();
        tjeneste.dokumentAnkommet(saksdokument);
    }

    @Test
    @Commit
    public void test_ny_søknad_xml_engangsstønad() throws Exception {
        Soeknad søknad = new SøknadTestdataBuilder().søknadEngangsstønadMor().build();
        InngåendeSaksdokument saksdokument = InngåendeSaksdokument.builder()
            .medFagsakId(FAGSAK_ID)
            .medJournalpostId(JOURNALPOST_ID)
            .medBehandlingTema(BEHANDLINGTEMA)
            .medDokumentTypeId(DOKUMENTTYPE)
            .medForsendelseMottatt(FORSENDELSE_MOTTATT)
            .medPayloadXml(søknad.toString())
            .build();
        tjeneste.dokumentAnkommet(saksdokument);
    }

    @Test
    @Commit
    public void test_ny_søknad_xml_foreldrepenger() throws Exception {
        Soeknad søknad = new SøknadTestdataBuilder().søknadForeldrepenger().medSøker(ForeldreType.MOR, new AktørId("1")).build();
        InngåendeSaksdokument saksdokument = InngåendeSaksdokument.builder()
            .medFagsakId(FAGSAK_ID)
            .medJournalpostId(JOURNALPOST_ID)
            .medBehandlingTema(BEHANDLINGTEMA)
            .medDokumentTypeId(DOKUMENTTYPE)
            .medForsendelseMottatt(FORSENDELSE_MOTTATT)
            .medPayloadXml(søknad.toString())
            .build();
        tjeneste.dokumentAnkommet(saksdokument);
    }
}
