package no.nav.foreldrepenger.behandlingslager.behandling.resultat.fravær;

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
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "FraværResultat")
@Table(name = "RES_FRAVAER")
public class FraværResultatEntitet extends BaseEntitet implements FraværResultat {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RES_FRAVAER")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "behandlingresultat_id", nullable = false, updatable = false)
    private Behandlingsresultat behandlingsresultat;

    @ChangeTracked
    @ManyToOne
    @JoinColumn(name = "perioder_id", nullable = false, updatable = false)
    private FraværPerioderEntitet perioder;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    FraværResultatEntitet() {
    }

    FraværResultatEntitet(FraværResultat resultat) {
        this.perioder = (FraværPerioderEntitet) resultat.getPerioder();
    }

    @Override
    public FraværPerioder getPerioder() {
        return perioder;
    }

    void setPerioder(FraværPerioderEntitet perioder) {
        this.perioder = perioder;
    }

    void setBehandlingsresultat(Behandlingsresultat behandlingsresultat) {
        this.behandlingsresultat = behandlingsresultat;
    }

    void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    @Override
    public String toString() {
        return "FraværResultatEntitet{" +
            "id=" + id +
            ", behandlingsresultat=" + behandlingsresultat +
            ", perioder=" + perioder +
            ", versjon=" + versjon +
            ", aktiv=" + aktiv +
            '}';
    }
}
