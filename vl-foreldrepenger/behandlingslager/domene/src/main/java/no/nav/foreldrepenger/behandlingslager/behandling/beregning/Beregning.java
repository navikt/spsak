package no.nav.foreldrepenger.behandlingslager.behandling.beregning;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "Beregning")
@Table(name = "BEREGNING")
public class Beregning extends BaseEntitet {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEREGNING_RESULTAT")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false, columnDefinition = "NUMERIC", length = 19)
    private long versjon;

    @ManyToOne(optional = false)
    @JoinColumn(name = "beregning_resultat_id", nullable = false, updatable = false)
    private BeregningResultat beregningResultat;

    @Column(name = "sats_verdi", nullable = false, columnDefinition = "NUMERIC", length = 19)
    private long satsVerdi;

    @Column(name = "antall_barn", nullable = false, columnDefinition = "NUMERIC", length = 19)
    private long antallBarn;

    @Column(name = "beregnet_tilkjent_ytelse", nullable = false, columnDefinition = "NUMERIC", length = 19)
    private long beregnetTilkjentYtelse;

    @Column(name = "oppr_beregnet_tilkjent_ytelse", columnDefinition = "NUMERIC", length = 19)
    private Long opprinneligBeregnetTilkjentYtelse;

    @Column(name = "beregnet_tidspunkt", nullable = false)
    private LocalDateTime beregnetTidspunkt;

    /**
     * Hvorvidt hele beregning er overstyrt av Saksbehandler. (fra SF3).
     */
    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "overstyrt", nullable = false)
    private boolean overstyrt = false;

    @SuppressWarnings("unused")
    private Beregning() {
        // for hibernate
    }

    public Beregning(long satsVerdi, long antallBarn, long beregnetTilkjentYtelse, LocalDateTime beregnetTidspunkt, boolean overstyrt, Long opprinneligBeregnetTilkjentYtelse) {
        this(null, satsVerdi, antallBarn, beregnetTilkjentYtelse, beregnetTidspunkt, overstyrt, opprinneligBeregnetTilkjentYtelse);
    }

    public Beregning(long satsVerdi, long antallBarn, long beregnetTilkjentYtelse, LocalDateTime beregnetTidspunkt) {
        this(null, satsVerdi, antallBarn, beregnetTilkjentYtelse, beregnetTidspunkt, false, null);
    }

    Beregning(BeregningResultat beregningResultat, long satsVerdi, long antallBarn, long beregnetTilkjentYtelse, LocalDateTime beregnetTidspunkt, boolean overstyrt, Long opprinneligBeregnetTilkjentYtelse) {
        Objects.requireNonNull(beregnetTidspunkt, "beregnetTidspunkt må være satt");
        this.beregningResultat = beregningResultat;
        this.satsVerdi = satsVerdi;
        this.antallBarn = antallBarn;
        this.beregnetTilkjentYtelse = beregnetTilkjentYtelse;
        this.beregnetTidspunkt = beregnetTidspunkt;
        this.overstyrt = overstyrt;
        this.opprinneligBeregnetTilkjentYtelse = opprinneligBeregnetTilkjentYtelse;
    }

    public boolean isOverstyrt() {
        return overstyrt;
    }

    public Long getId() {
        return id;
    }

    public long getSatsVerdi() {
        return satsVerdi;
    }

    public long getAntallBarn() {
        return antallBarn;
    }

    public long getBeregnetTilkjentYtelse() {
        return beregnetTilkjentYtelse;
    }

    public LocalDateTime getBeregnetTidspunkt() {
        return beregnetTidspunkt;
    }

    public Long getOpprinneligBeregnetTilkjentYtelse() {
        return opprinneligBeregnetTilkjentYtelse;
    }

    public BeregningResultat getBeregningResultat() {
        return beregningResultat;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<>";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Beregning beregning = (Beregning) o;
        return Objects.equals(this.overstyrt, beregning.overstyrt)
            && Objects.equals(this.satsVerdi, beregning.satsVerdi)
            && Objects.equals(this.beregnetTilkjentYtelse, beregning.beregnetTilkjentYtelse)
            && Objects.equals(this.beregnetTidspunkt, beregning.beregnetTidspunkt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(overstyrt, satsVerdi, beregnetTilkjentYtelse, beregnetTidspunkt);
    }

}
