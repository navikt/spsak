package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.DokumentPersistererTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.MottattDokumentWrapper;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class MottatteDokumentTjenesteImpl implements MottatteDokumentTjeneste {

    private Integer fristForInnsendingAvDokumentasjon;

    private DokumentPersistererTjeneste dokumentPersistererTjeneste;
    private BehandlingRepository behandlingRepository;
    private BehandlingVedtakRepository vedtakRepository;

    MottatteDokumentTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public MottatteDokumentTjenesteImpl(@KonfigVerdi("sak.frist.innsending.dok.uker") Integer fristForInnsendingAvDokumentasjon,
                                        DokumentPersistererTjeneste dokumentPersistererTjeneste,
                                        GrunnlagRepositoryProvider grunnlagRepositoryProvider,
                                        ResultatRepositoryProvider resultatRepositoryProvider) {
        this.fristForInnsendingAvDokumentasjon = fristForInnsendingAvDokumentasjon;
        this.dokumentPersistererTjeneste = dokumentPersistererTjeneste;
        this.behandlingRepository = grunnlagRepositoryProvider.getBehandlingRepository();
        this.vedtakRepository = resultatRepositoryProvider.getVedtakRepository();
    }

    @Override
    public void persisterDokumentinnhold(Behandling behandling, InngåendeSaksdokument dokument, Optional<LocalDate> gjelderFra) {
        if (dokument.getPayload() != null) {
            @SuppressWarnings("rawtypes")
            MottattDokumentWrapper dokumentWrapper = dokumentPersistererTjeneste.payloadTilWrapper(dokument);
            dokumentPersistererTjeneste.persisterDokumentinnhold(dokumentWrapper, dokument, behandling, gjelderFra);
        }
    }

    /**
     * Beregnes fra vedtaksdato
     */
    @Override
    public boolean harFristForInnsendingAvDokGåttUt(Fagsak sak) {
        Objects.requireNonNull(sak, "Fagsak");
        Optional<Behandling> behandlingOptional = behandlingRepository.finnSisteAvsluttedeIkkeHenlagteBehandling(sak.getId());
        Behandling behandling = behandlingOptional.get(); // NOSONAR
        Optional<BehandlingVedtak> behandlingVedtak = vedtakRepository.hentVedtakFor(behandlingRepository.hentResultat(behandling.getId()).getId());
        return behandlingVedtak.map(behandlingVedtak1 -> behandlingVedtak1.getVedtaksdato().isBefore(LocalDate.now().minusWeeks(fristForInnsendingAvDokumentasjon))).orElse(false);
    }

}
