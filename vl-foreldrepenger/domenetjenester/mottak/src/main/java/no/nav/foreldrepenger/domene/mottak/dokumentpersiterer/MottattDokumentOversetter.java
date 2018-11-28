package no.nav.foreldrepenger.domene.mottak.dokumentpersiterer;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.MottattDokumentWrapper;

public interface MottattDokumentOversetter<T extends MottattDokumentWrapper<?>> {

    void trekkUtDataOgPersister(T wrapper, InngåendeSaksdokument mottattDokument, Behandling behandling, Optional<LocalDate> gjelderFra);
}
