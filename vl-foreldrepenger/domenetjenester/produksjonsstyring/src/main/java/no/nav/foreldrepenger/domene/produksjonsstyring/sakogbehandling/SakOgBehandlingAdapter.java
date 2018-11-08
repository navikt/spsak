package no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling;

public interface SakOgBehandlingAdapter {

    void behandlingOpprettet(OpprettetBehandlingStatus status);
    void behandlingAvsluttet(AvsluttetBehandlingStatus status);
}
