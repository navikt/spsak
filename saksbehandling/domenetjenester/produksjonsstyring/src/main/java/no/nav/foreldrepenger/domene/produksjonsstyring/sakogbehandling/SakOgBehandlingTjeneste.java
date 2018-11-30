package no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling;

public interface SakOgBehandlingTjeneste {

    void behandlingOpprettet(OpprettetBehandlingStatus status);
    void behandlingAvsluttet(AvsluttetBehandlingStatus status);
}
