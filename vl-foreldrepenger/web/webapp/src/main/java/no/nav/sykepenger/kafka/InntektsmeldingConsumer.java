package no.nav.sykepenger.kafka;

import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.FagsakTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.SaksbehandlingDokumentmottakTjeneste;
import no.nav.vedtak.felles.AktiverContextOgTransaksjon;
import no.nav.vedtak.felles.jpa.Transaction;

@ApplicationScoped
@AktiverContextOgTransaksjon
public class InntektsmeldingConsumer extends KafkaConsumer {

    private SaksbehandlingDokumentmottakTjeneste dokumentmottakTjeneste;
    private DokumentArkivTjeneste dokumentArkivTjeneste;
    private FagsakTjeneste fagsakTjeneste;
    private KodeverkRepository kodeverkRepository;
    private BehandlingRepository behandlingRepository;

    InntektsmeldingConsumer() {
        // CDI
    }

    @Inject
    public InntektsmeldingConsumer(SaksbehandlingDokumentmottakTjeneste dokumentmottakTjeneste,
                                   DokumentArkivTjeneste dokumentArkivTjeneste,
                                   FagsakTjeneste fagsakTjeneste,
                                   BehandlingRepositoryProvider repositoryProvider) {
        super("inntektsmelding");

        this.dokumentmottakTjeneste = dokumentmottakTjeneste;
        this.dokumentArkivTjeneste = dokumentArkivTjeneste;
        this.fagsakTjeneste = fagsakTjeneste;
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
    }

    @Override
    protected void handleMessage(String key, String payload) {
        System.out.println("KEY=" + key + ", PAYLOAD=" + payload);
    }

}
