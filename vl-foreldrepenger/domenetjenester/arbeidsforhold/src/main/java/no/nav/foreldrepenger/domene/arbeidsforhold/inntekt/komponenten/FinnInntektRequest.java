package no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten;

import java.time.YearMonth;
import java.util.Objects;

import no.nav.foreldrepenger.domene.typer.AktørId;

public class FinnInntektRequest {

    private AktørId aktørId;
    private YearMonth fom;
    private YearMonth tom;
    private Long behandlingId;
    private Long fagsakId;

    public FinnInntektRequest(AktørId aktørId, YearMonth fom, YearMonth tom, Long behandlingId, Long fagsakId) {
        this.aktørId = aktørId;
        this.fom = fom;
        this.tom = tom;
        this.behandlingId = behandlingId;
        this.fagsakId = fagsakId;
    }

    public AktørId getAktørId() {
        return aktørId;
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
        return Objects.equals(this.aktørId, other.aktørId)
            && Objects.equals(this.fom, other.fom)
            && Objects.equals(this.tom, other.tom)
            && Objects.equals(this.behandlingId, other.behandlingId)
            && Objects.equals(this.fagsakId, other.fagsakId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId, fom, tom, behandlingId, fagsakId);
    }

}
