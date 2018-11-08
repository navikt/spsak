package no.nav.foreldrepenger.domene.mottak.dokumentmottak;

import java.time.LocalDate;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl.HåndterMottattDokumentTaskProperties;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class SaksbehandlingDokumentmottakTjenesteImpl implements SaksbehandlingDokumentmottakTjeneste {

    private ProsessTaskRepository prosessTaskRepository;
    private MottatteDokumentTjeneste mottatteDokumentTjeneste;

    public SaksbehandlingDokumentmottakTjenesteImpl() {
        //for CDI, jaja
    }

    @Inject
    public SaksbehandlingDokumentmottakTjenesteImpl(ProsessTaskRepository prosessTaskRepository,
                                                    MottatteDokumentTjeneste mottatteDokumentTjeneste) {
        this.prosessTaskRepository = prosessTaskRepository;
        this.mottatteDokumentTjeneste = mottatteDokumentTjeneste;
    }

    @Override
    public void dokumentAnkommet(InngåendeSaksdokument saksdokument) {
        boolean erElektroniskSøknad = saksdokument.getPayloadXml() != null;

        MottattDokument.Builder builder = new MottattDokument.Builder()
            .medDokumentTypeId(saksdokument.getDokumentTypeId() != null ? saksdokument.getDokumentTypeId() : DokumentTypeId.UDEFINERT)
            .medDokumentKategori(saksdokument.getDokumentKategori() != null ? saksdokument.getDokumentKategori() : DokumentKategori.UDEFINERT)
            .medMottattDato(LocalDate.parse(saksdokument.getForsendelseMottatt().toString()))
            .medXmlPayload(saksdokument.getPayloadXml())
            .medElektroniskRegistrert(erElektroniskSøknad)
            .medFagsakId(saksdokument.getFagsakId())
            .medJournalFørendeEnhet(saksdokument.getJournalEnhet());

        if (saksdokument.getJournalpostId() != null) {
            builder.medJournalPostId(new JournalpostId(saksdokument.getJournalpostId().getVerdi()));
        }
        if (saksdokument.getForsendelseId() != null) {
            builder.medForsendelseId(UUID.fromString(saksdokument.getForsendelseId().toString()));
        }
        MottattDokument mottattDokument = builder.build();

        Long mottattDokumentId = mottatteDokumentTjeneste.lagreMottattDokumentPåFagsak(saksdokument.getFagsakId(), mottattDokument);

        ProsessTaskData prosessTaskData = new ProsessTaskData(HåndterMottattDokumentTaskProperties.TASKTYPE);

        prosessTaskData.setFagsakId(saksdokument.getFagsakId());
        prosessTaskData.setProperty(HåndterMottattDokumentTaskProperties.MOTTATT_DOKUMENT_ID_KEY, mottattDokumentId.toString());
        prosessTaskData.setProperty(HåndterMottattDokumentTaskProperties.BEHANDLINGSTEMA_OFFISIELL_KODE_KEY, saksdokument.getBehandlingTema().getOffisiellKode());
        prosessTaskData.setProperty(HåndterMottattDokumentTaskProperties.BEHANDLING_ÅRSAK_TYPE_KEY, saksdokument.getBehandlingÅrsakType());

        prosessTaskData.setCallIdFraEksisterende();
        prosessTaskRepository.lagre(prosessTaskData);
    }
}
