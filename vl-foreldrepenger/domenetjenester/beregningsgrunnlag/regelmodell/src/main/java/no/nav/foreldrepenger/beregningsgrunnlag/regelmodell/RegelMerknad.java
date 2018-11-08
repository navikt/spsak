package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell;

import java.util.Objects;

public class RegelMerknad {

    private final String merknadKode;
    private final String merknadTekst;

    public RegelMerknad(String merknadKode, String merknadTekst) {
        super();
        this.merknadKode = merknadKode;
        this.merknadTekst = merknadTekst;
    }

    public String getMerknadKode() {
        return merknadKode;
    }

    public String getMerknadTekst() {
        return merknadTekst;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegelMerknad that = (RegelMerknad) o;
        return Objects.equals(merknadKode, that.merknadKode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(merknadKode);
    }
}
