package no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;


@Entity(name = "BeregningsgrunnlagGrunnlagEntitet")
@Table(name = "GR_BEREGNINGSGRUNNLAG")
public class BeregningsgrunnlagGrunnlagEntitet extends BaseEntitet {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE ,generator="SEQ_GR_BEREGNINGSGRUNNLAG")
    private Long id;

    @OneToOne
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false, unique = true)
    private Behandling behandling;

    @OneToOne(optional = false)
    @JoinColumn(name = "beregningsgrunnlag_id", nullable = false, updatable = false, unique = true)
    private Beregningsgrunnlag beregningsgrunnlag;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "steg_opprettet", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + BeregningsgrunnlagTilstand.DISCRIMINATOR
            + "'")) })
    private BeregningsgrunnlagTilstand beregningsgrunnlagTilstand;

    BeregningsgrunnlagGrunnlagEntitet() {
        // NOSONAR
    }

    public BeregningsgrunnlagGrunnlagEntitet(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        this.behandling = behandling;
        this.beregningsgrunnlag = beregningsgrunnlag;
        this.beregningsgrunnlagTilstand = beregningsgrunnlagTilstand;
    }

    public Long getId() {
        return id;
    }

    public Behandling getBehandling() {
        return behandling;
    }

    public Beregningsgrunnlag getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    public BeregningsgrunnlagTilstand getBeregningsgrunnlagTilstand() {
        return beregningsgrunnlagTilstand;
    }

    public boolean erAktivt() {
        return aktiv;
    }

    public void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }
}
