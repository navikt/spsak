package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
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
    private BehandlingRepositoryProvider behandlingRepositoryProvider;

    MottatteDokumentTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public MottatteDokumentTjenesteImpl(@KonfigVerdi("sak.frist.innsending.dok.uker") Integer fristForInnsendingAvDokumentasjon, DokumentPersistererTjeneste dokumentPersistererTjeneste,
                                        BehandlingRepositoryProvider behandlingRepositoryProvider) {
        this.fristForInnsendingAvDokumentasjon = fristForInnsendingAvDokumentasjon;
        this.dokumentPersistererTjeneste = dokumentPersistererTjeneste;
        this.behandlingRepositoryProvider = behandlingRepositoryProvider;
    }

    @Override
    public void persisterDokumentinnhold(Behandling behandling, InngåendeSaksdokument dokument, Optional<LocalDate> gjelderFra){
        if (dokument.getPayload() != null) {
            @SuppressWarnings("rawtypes")
            MottattDokumentWrapper dokumentWrapper = dokumentPersistererTjeneste.payloadTilWrapper(dokument);
            dokumentPersistererTjeneste.persisterDokumentinnhold(dokumentWrapper, dokument, behandling, gjelderFra);
        }
    }

    @Override
    public boolean erSisteYtelsesbehandlingAvslåttPgaManglendeDokumentasjon(Fagsak sak) {
        Objects.requireNonNull(sak, "Fagsak");
        Optional<Behandling> behandling = behandlingRepositoryProvider.getBehandlingRepository().finnSisteAvsluttedeIkkeHenlagteBehandling(sak.getId());
        if (behandling.isPresent()) {
            return erAvsluttetPgaManglendeDokumentasjon(behandling.get());
        }
        return false;
    }

    /**
     * Beregnes fra vedtaksdato
     */
    @Override
    public boolean harFristForInnsendingAvDokGåttUt(Fagsak sak) {
        Objects.requireNonNull(sak, "Fagsak");
        Optional<Behandling> behandlingOptional = behandlingRepositoryProvider.getBehandlingRepository().finnSisteAvsluttedeIkkeHenlagteBehandling(sak.getId());
        Behandling behandling = behandlingOptional.get(); // NOSONAR
        Optional<BehandlingVedtak> behandlingVedtak = behandlingRepositoryProvider.getBehandlingVedtakRepository().hentBehandlingvedtakForBehandlingId(behandling.getId());
        if (behandlingVedtak.isPresent()) {
            return behandlingVedtak.get().getVedtaksdato().isBefore(LocalDate.now().minusWeeks(fristForInnsendingAvDokumentasjon));
        }
        return false;
    }

    private boolean erAvsluttetPgaManglendeDokumentasjon(Behandling behandling) {
        Objects.requireNonNull(behandling, "Behandling");
        Optional<BehandlingVedtak> behandlingVedtak = behandlingRepositoryProvider.getBehandlingVedtakRepository().hentBehandlingvedtakForBehandlingId(behandling.getId());
        if (behandlingVedtak.isPresent()) {
            return Avslagsårsak.MANGLENDE_DOKUMENTASJON.equals(behandlingVedtak.get().getBehandlingsresultat().getAvslagsårsak());
        }
        return false;
    }

}
