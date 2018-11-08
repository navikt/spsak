package no.nav.foreldrepenger.behandlingslager.aktør.historikk;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.vedtak.konfig.Tid;

public class Gyldighetsperiode {

    private LocalDate fom;
    private LocalDate tom;

    private Gyldighetsperiode(LocalDate fom, LocalDate tom) {
        // Fom er null om perioden for en opplysning gjelder fra personen ble født
        // Setter da fom til tidenes begynnelse for å slippe et ekstra kall for å hente fødselsdato
        if (fom == null) {
            fom = Tid.TIDENES_BEGYNNELSE;
        }
        if (tom == null) {
            tom = Tid.TIDENES_ENDE;
        }

        this.fom = fom;
        this.tom = tom;
    }

    public static Gyldighetsperiode innenfor(LocalDate fom, LocalDate tom) {
        return new Gyldighetsperiode(fom, tom);
    }

    public static Gyldighetsperiode fraTilTidenesEnde(LocalDate fom) {
        return new Gyldighetsperiode(fom, Tid.TIDENES_ENDE);
    }

    public LocalDate getFom() {
        return this.fom;
    }

    public LocalDate getTom() {
        return this.tom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gyldighetsperiode that = (Gyldighetsperiode) o;
        return Objects.equals(fom, that.fom) &&
            Objects.equals(tom, that.tom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fom, tom);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Gyldighetsperiode{");
        sb.append("fom=").append(fom);
        sb.append(", tom=").append(tom);
        sb.append('}');
        return sb.toString();
    }
}
