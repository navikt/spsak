package no.nav.foreldrepenger.domene.typer;

import static no.nav.vedtak.util.Objects.check;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.foreldrepenger.behandlingslager.diff.TraverseValue;

@Embeddable
public class Prosentsats implements Serializable, IndexKey, TraverseValue {

    private static final RoundingMode AVRUNDINGSMODUS = RoundingMode.HALF_EVEN;

    @Column(name = "verdi", scale = 2, nullable = false)
    @ChangeTracked
    private BigDecimal verdi;

    protected Prosentsats() {
        // for hibernate
    }

    public Prosentsats(BigDecimal verdi) {
        this.verdi = verdi;
        validerRange(this.verdi);
    }

    // Beleilig å kunne opprette gjennom int
    public Prosentsats(Integer verdi) {
        this.verdi = verdi == null ? null : new BigDecimal(verdi);
        validerRange(this.verdi);
    }

    // Beleilig å kunne opprette gjennom string
    public Prosentsats(String verdi) {
        this.verdi = verdi == null ? null : new BigDecimal(verdi);
        validerRange(this.verdi);
    }

    @Override
    public String getIndexKey() {
        return skalertVerdi().toString();
    }

    public BigDecimal getVerdi() {
        return verdi;
    }

    private BigDecimal skalertVerdi() {
        return verdi.setScale(2, AVRUNDINGSMODUS);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        Prosentsats other = (Prosentsats) obj;
        return Objects.equals(skalertVerdi(), other.skalertVerdi());
    }

    @Override
    public int hashCode() {
        return Objects.hash(skalertVerdi());
    }

    @Override
    public String toString() {
        return "Stillingsprosent{" +
            "verdi=" + verdi +
            ", skalertVerdi=" + skalertVerdi() +
            '}';
    }

    private static void validerRange(BigDecimal verdi) {
        if (verdi == null) {
            return;
        }
        check(verdi.compareTo(BigDecimal.ZERO) >= 0, "Prosent må være >= 0"); //$NON-NLS-1$
        check(verdi.compareTo(BigDecimal.valueOf(200)) <= 0, "Prosent må være <= 200"); //$NON-NLS-1$
    }

    public boolean erNulltall() {
        return verdi != null && verdi.intValue() == 0;
    }
}
