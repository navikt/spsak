package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;

public interface Dokumentmottaker {

    void mottaDokument(InngåendeSaksdokument mottattDokument, Fagsak fagsak, DokumentTypeId dokumentTypeId, BehandlingÅrsakType behandlingÅrsakType);

    void mottaDokumentForKøetBehandling(InngåendeSaksdokument mottattDokument, Fagsak fagsak, DokumentTypeId dokumentTypeId, BehandlingÅrsakType behandlingÅrsakType);
}
