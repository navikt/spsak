package no.nav.foreldrepenger.domene.mottak;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.hendelser.Forretningshendelse;
import no.nav.foreldrepenger.behandlingslager.hendelser.ForretningshendelseType;

public interface ForretningshendelseHåndterer<T extends Forretningshendelse> {

    List<Fagsak> finnRelaterteFagsaker(T forretningshendelse);

    void håndterÅpenBehandling(Behandling åpenBehandling, ForretningshendelseType forretningshendelseType);

    void håndterAvsluttetBehandling(Behandling avsluttetBehandling, ForretningshendelseType forretningshendelseType);

    void håndterKøetBehandling(Fagsak fagsak, ForretningshendelseType forretningshendelseType);

}
