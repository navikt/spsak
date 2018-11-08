package no.nav.foreldrepenger.økonomistøtte;

import java.util.Objects;

import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.OppdragsmottakerStatus;

public class Oppdragsmottaker {

    private boolean bruker;
    private String fnr;
    private String orgnr;
    private OppdragsmottakerStatus status;

    public Oppdragsmottaker(String id, boolean bruker) {
        Objects.requireNonNull(id, "id");
        this.bruker = bruker;
        if (bruker) {
            this.fnr = id;
        } else {
            this.orgnr = id;
        }
    }

    public boolean erBruker() {
        return bruker;
    }

    public String getFnr() {
        if (!bruker) {
            throw new IllegalStateException("Mottaker er ikke bruker");
        }
        return fnr;
    }

    public String getOrgnr() {
        if (bruker) {
            throw new IllegalStateException("Mottaker er bruker");
        }
        return orgnr;
    }

    public String getId() {
        return (bruker ? fnr : orgnr);
    }

    public OppdragsmottakerStatus getStatus() {
        return status;
    }

    public void setStatus(OppdragsmottakerStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object arg0) {
        if (!(arg0 instanceof Oppdragsmottaker)) return false;
        Oppdragsmottaker other = (Oppdragsmottaker) arg0;
        return bruker == other.bruker
          && Objects.equals(fnr, other.fnr)
          && Objects.equals(orgnr, other.orgnr);
    }

    @Override
    public int hashCode() {
        return bruker ? fnr.hashCode() : orgnr.hashCode();
    }
}
