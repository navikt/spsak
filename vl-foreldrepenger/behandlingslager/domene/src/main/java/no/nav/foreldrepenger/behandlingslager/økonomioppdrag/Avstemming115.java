package no.nav.foreldrepenger.behandlingslager.økonomioppdrag;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

/**
 * Denne klassen er en ren avbildning fra Oppdragsløsningens meldingsformater.
 * Navngivning følger ikke nødvendigvis Vedtaksløsningens navnestandarder.
 */
@Entity(name = "Avstemming115")
@Table(name = "OKO_AVSTEMMING_115")
public class Avstemming115 extends BaseEntitet{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OKO_AVSTEMMING_115")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Column(name = "kode_komponent", nullable = false)
    private String kodekomponent;

    // Avstemmingsnøkkel - brukes til å matche oppdragsmeldinger i avstemmingen
    @Column(name = "nokkel_avstemming", nullable = false)
    private LocalDateTime nokkelAvstemming;

    @Column(name = "tidspnkt_melding", nullable = false)
    private LocalDateTime tidspnktMelding;

    public Avstemming115() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKodekomponent() {
        return kodekomponent;
    }

    public void setKodekomponent(String kodekomponent) {
        this.kodekomponent = kodekomponent;
    }

    public LocalDateTime getNokkelAvstemming() {
        return nokkelAvstemming;
    }

    public void setNokkelAvstemming(LocalDateTime nokkelAvstemming) {
        this.nokkelAvstemming = nokkelAvstemming;
    }

    public LocalDateTime getTidspnktMelding() {
        return tidspnktMelding;
    }

    public void setTidspnktMelding(LocalDateTime tidspnktMelding) {
        this.tidspnktMelding = tidspnktMelding;
    }


    @Override
    public boolean equals(Object object){
        if (object == this) {
            return true;
        }
        if (!(object instanceof Avstemming115)) {
            return false;
        }
        Avstemming115 avstemnokler115 = (Avstemming115) object;
        return Objects.equals(kodekomponent, avstemnokler115.getKodekomponent())
                && Objects.equals(nokkelAvstemming, avstemnokler115.getNokkelAvstemming())
                && Objects.equals(tidspnktMelding, avstemnokler115.getTidspnktMelding());
    }

    @Override
    public int hashCode() {
        return Objects.hash(kodekomponent, nokkelAvstemming, tidspnktMelding);
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        private String kodekomponent;
        private LocalDateTime nokkelAvstemming;
        private LocalDateTime tidspnktMelding;


        public Builder medKodekomponent(String kodekomponent) { this.kodekomponent = kodekomponent; return this; }

        public Builder medNokkelAvstemming(LocalDateTime nokkelAvstemming) { this.nokkelAvstemming = nokkelAvstemming; return this; }

        public Builder medTidspnktMelding(LocalDateTime tidspnktMelding) { this.tidspnktMelding = tidspnktMelding; return this; }


        public Avstemming115 build() {
            verifyStateForBuild();
            Avstemming115 avstemming115 = new Avstemming115();
            avstemming115.kodekomponent = kodekomponent;
            avstemming115.nokkelAvstemming = nokkelAvstemming;
            avstemming115.tidspnktMelding = tidspnktMelding;

            return avstemming115;
        }


        public void verifyStateForBuild() {
            Objects.requireNonNull(kodekomponent, "kodekomponent");
            Objects.requireNonNull(nokkelAvstemming, "nokkelAvstemming");
            Objects.requireNonNull(tidspnktMelding, "tidspnktMelding");
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                (id != null ? "id=" + id + ", " : "") //$NON-NLS-1$ //$NON-NLS-2$
                + "kodekomponent=" + kodekomponent + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "nokkelAvstemming=" + nokkelAvstemming + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "tidspnktMelding=" + tidspnktMelding + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "opprettetTs=" + getOpprettetTidspunkt() //$NON-NLS-1$
                + ">"; //$NON-NLS-1$
    }
}
