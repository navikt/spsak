package no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten;

import java.time.YearMonth;
import java.util.Objects;

public class FinnInntektRequest {

    private String fnr;
    private YearMonth fom;
    private YearMonth tom;
    private Long behandlingId;
    private Long fagsakId;

    public FinnInntektRequest(String fnr, YearMonth fom, YearMonth tom, Long behandlingId, Long fagsakId) {
        this.fnr = fnr;
        this.fom = fom;
        this.tom = tom;
        this.behandlingId = behandlingId;
        this.fagsakId = fagsakId;
    }

    public String getFnr() {
        return fnr;
    }

    public YearMonth getFom() {
        return fom;
    }

    public YearMonth getTom() {
        return tom;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public Long getFagsakId() {
        return fagsakId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof FinnInntektRequest)) {
            return false;
        }
        FinnInntektRequest other = (FinnInntektRequest) obj;
        return Objects.equals(this.fnr, other.fnr)
            && Objects.equals(this.fom, other.fom)
            && Objects.equals(this.tom, other.tom)
            && Objects.equals(this.behandlingId, other.behandlingId)
            && Objects.equals(this.fagsakId, other.fagsakId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fnr, fom, tom, behandlingId, fagsakId);
    }

}
