package no.nav.foreldrepenger.domene.mottak.dokumentpersiterer;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.MottattDokumentWrapper;

@SuppressWarnings("rawtypes")
public interface DokumentPersistererTjeneste {

    MottattDokumentWrapper xmlTilWrapper(MottattDokument dokument);

    void persisterDokumentinnhold(MottattDokumentWrapper wrapper, MottattDokument dokument, Behandling behandling, Optional<LocalDate> gjelderFra);

    void persisterDokumentinnhold(MottattDokument dokument, Behandling behandling);
}
