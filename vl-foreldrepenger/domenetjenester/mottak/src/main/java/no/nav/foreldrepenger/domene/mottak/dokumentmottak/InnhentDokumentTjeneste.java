package no.nav.foreldrepenger.domene.mottak.dokumentmottak;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;

public interface InnhentDokumentTjeneste {

    void utfør(MottattDokument mottattDokument, BehandlingÅrsakType behandlingÅrsakType);
}
