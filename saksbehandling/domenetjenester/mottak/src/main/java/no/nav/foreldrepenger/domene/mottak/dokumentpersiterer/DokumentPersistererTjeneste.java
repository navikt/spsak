package no.nav.foreldrepenger.domene.mottak.dokumentpersiterer;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.MottattDokumentWrapper;

@SuppressWarnings("rawtypes")
public interface DokumentPersistererTjeneste {

    MottattDokumentWrapper payloadTilWrapper(InngåendeSaksdokument dokument);

    void persisterDokumentinnhold(MottattDokumentWrapper wrapper, InngåendeSaksdokument dokument, Behandling behandling, Optional<LocalDate> gjelderFra);

    void persisterDokumentinnhold(InngåendeSaksdokument dokument, Behandling behandling);
}
