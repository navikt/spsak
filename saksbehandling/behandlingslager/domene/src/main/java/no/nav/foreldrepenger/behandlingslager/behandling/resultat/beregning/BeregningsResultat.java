package no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;


@Entity(name = "BeregningsResultat")
@Table(name = "RES_BEREGNINGSRESULTAT")
public class BeregningsResultat extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE ,generator="SEQ_RES_BEREGNINGSRESULTAT_FP")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "behandling_resultat_id", nullable = false, updatable = false, unique = true)
    private Behandlingsresultat behandlingsresultat;

    @OneToOne(optional = false)
    @JoinColumn(name = "beregningsresultat_id", nullable = false, updatable = false, unique = true)
    private BeregningsresultatPerioder beregningsresultat;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    BeregningsResultat() {
        // NOSONAR
    }

    public BeregningsResultat(Behandlingsresultat behandlingsresultat, BeregningsresultatPerioder beregningsresultat) {
        this.behandlingsresultat = behandlingsresultat;
        this.beregningsresultat = beregningsresultat;
    }

    public Long getId() {
        return id;
    }

    public Behandlingsresultat getBehandlingsresultat() {
        return behandlingsresultat;
    }

    public BeregningsresultatPerioder getBeregningsresultat() {
        return beregningsresultat;
    }

    public boolean erAktivt() {
        return aktiv;
    }

    public void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }
}
