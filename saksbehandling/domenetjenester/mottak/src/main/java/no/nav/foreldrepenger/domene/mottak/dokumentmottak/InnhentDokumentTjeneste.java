package no.nav.foreldrepenger.domene.mottak.dokumentmottak;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;

public interface InnhentDokumentTjeneste {

    void utfør(InngåendeSaksdokument mottattDokument, BehandlingÅrsakType behandlingÅrsakType);
}
