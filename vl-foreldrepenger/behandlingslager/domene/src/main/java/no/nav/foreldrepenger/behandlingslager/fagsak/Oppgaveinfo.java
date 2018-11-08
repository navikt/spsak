package no.nav.foreldrepenger.behandlingslager.fagsak;

import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveÅrsak;

public class Oppgaveinfo{

    public static final Oppgaveinfo VURDER_KONST_YTELSE_FORELDREPENGER = new Oppgaveinfo("VUR_KONS_YTE_FOR", null);
    public static final Oppgaveinfo VURDER_DOKUMENT = new Oppgaveinfo(OppgaveÅrsak.VURDER_DOKUMENT.getKode(), null);

    private String oppgaveType;
    private String status;

    public Oppgaveinfo(String oppgaveType, String status) {
        this.oppgaveType = oppgaveType;
        this.status = status;
    }

    public String getOppgaveType() {
        return oppgaveType;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof  Oppgaveinfo){
            return oppgaveType.equals(((Oppgaveinfo)obj).getOppgaveType());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(oppgaveType);
    }
}
