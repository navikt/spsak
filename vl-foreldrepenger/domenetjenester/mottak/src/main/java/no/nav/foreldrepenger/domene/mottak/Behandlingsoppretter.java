package no.nav.foreldrepenger.domene.mottak;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;

public interface Behandlingsoppretter {

    boolean erKompletthetssjekkPassert(Behandling behandling);

    Behandling opprettFørstegangsbehandling(Fagsak fagsak, BehandlingÅrsakType behandlingÅrsakType);

    Behandling henleggOgOpprettNyFørstegangsbehandling(Fagsak fagsak, Behandling behandling, BehandlingÅrsakType behandlingÅrsakType);

    Behandling opprettNyFørstegangsbehandling(BehandlingÅrsakType behandlingÅrsakType, Fagsak fagsak);

    Behandling opprettRevurdering(Fagsak fagsak, BehandlingÅrsakType revurderingsÅrsak);

    Behandling oppdaterBehandlingViaHenleggelse(Behandling behandling, BehandlingÅrsakType revurderingsÅrsak);

    void henleggBehandling(Behandling sisteYtelseBehandling);

    boolean harMottattFørstegangssøknad(Behandling behandling);

    Behandling opprettKøetBehandling(Fagsak fagsak, BehandlingÅrsakType eksternÅrsak);

    Behandling opprettBerørtBehandling(Fagsak fagsak);

    Behandling finnEllerOpprettFørstegangsbehandling(Fagsak fagsak);

    void settSomKøet(Behandling nyKøetBehandling);

    boolean erAvslåttFørstegangsbehandling(Behandling behandling);

    void opprettNyFørstegangsbehandling(InngåendeSaksdokument mottattDokument, Fagsak fagsak, Behandling avsluttetBehandling);
}
