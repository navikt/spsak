package no.nav.foreldrepenger.behandlingslager.behandling.beregning;

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
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;


@Entity(name = "BeregningsresultatFPKobling")
@Table(name = "RES_BEREGNINGSRESULTAT_FP")
public class BeregningsresultatFPKobling extends BaseEntitet {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE ,generator="SEQ_RES_BEREGNINGSRESULTAT_FP")
    private Long id;

    @OneToOne
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false, unique = true)
    private Behandling behandling;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @OneToOne(optional = false)
    @JoinColumn(name = "beregningsresultat_fp_id", nullable = false, updatable = false, unique = true)
    private BeregningsresultatFP beregningsresultatFP;

    BeregningsresultatFPKobling() {
        // NOSONAR
    }

    public BeregningsresultatFPKobling(Behandling behandling, BeregningsresultatFP beregningsresultatFP) {
        this.behandling = behandling;
        this.beregningsresultatFP = beregningsresultatFP;
    }

    public Long getId() {
        return id;
    }

    public Behandling getBehandling() {
        return behandling;
    }

    public BeregningsresultatFP getBeregningsresultatFP() {
        return beregningsresultatFP;
    }

    public boolean erAktivt() {
        return aktiv;
    }

    public void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }
}
