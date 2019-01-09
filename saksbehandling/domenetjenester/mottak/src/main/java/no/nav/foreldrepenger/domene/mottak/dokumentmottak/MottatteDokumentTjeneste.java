package no.nav.foreldrepenger.domene.mottak.dokumentmottak;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;

public interface MottatteDokumentTjeneste {

    void persisterDokumentinnhold(Behandling behandling, InngåendeSaksdokument mottattDokument, Optional<LocalDate> gjelderFra);

    /**
     * Beregnes fra og med vedtaksdato
     */
    boolean harFristForInnsendingAvDokGåttUt(Fagsak sak);
}
