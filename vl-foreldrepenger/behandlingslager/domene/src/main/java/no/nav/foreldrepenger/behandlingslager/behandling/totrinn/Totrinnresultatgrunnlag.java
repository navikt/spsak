package no.nav.foreldrepenger.behandlingslager.behandling.totrinn;

import java.util.Optional;

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
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "Totrinnresultatgrunnlag")
@Table(name = "TOTRINNRESULTATGRUNNLAG")
public class Totrinnresultatgrunnlag extends BaseEntitet {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TOTRINNRESULTATGRUNNLAG")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false)
    private Behandling behandling;

    @Column(name = "inntekt_arbeid_grunnlag_id", updatable = false, columnDefinition = "NUMERIC")
    private Long inntektArbeidYtelseGrunnlagId;

    @Column(name = "uttak_resultat_id", updatable = false, columnDefinition = "NUMERIC")
    private Long uttakResultatEntitetId;

    @Column(name = "beregningsgrunnlag_id", updatable = false, columnDefinition = "NUMERIC")
    private Long beregningsgrunnlagId;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false, columnDefinition = "NUMERIC", length = 19)
    private long versjon;


    Totrinnresultatgrunnlag() {
        // for hibernate
    }


    public Totrinnresultatgrunnlag(Behandling behandling, Long inntektArbeidYtelseGrunnlagId, Long uttakResultatEntitetId,
                            Long beregningsgrunnlagId) {
        this.behandling = behandling;
        this.inntektArbeidYtelseGrunnlagId = inntektArbeidYtelseGrunnlagId;
        this.uttakResultatEntitetId = uttakResultatEntitetId;
        this.beregningsgrunnlagId = beregningsgrunnlagId;
    }

    public Long getId() {
        return id;
    }

    public Behandling getBehandling() {
        return behandling;
    }

    public void setBehandling(Behandling behandling) {
        this.behandling = behandling;
    }

    public Optional<Long> getInntektArbeidYtelseGrunnlagId() {
        return Optional.ofNullable(inntektArbeidYtelseGrunnlagId);
    }

    public void setInntektArbeidYtelseGrunnlagId(Long inntektArbeidYtelseGrunnlagId) {
        this.inntektArbeidYtelseGrunnlagId = inntektArbeidYtelseGrunnlagId;
    }

    public Optional<Long> getUttakResultatEntitetId() {
        return Optional.ofNullable(uttakResultatEntitetId);
    }

    public void setUttakResultatEntitetId(Long uttakResultatEntitetId) {
        this.uttakResultatEntitetId = uttakResultatEntitetId;
    }

    public boolean isAktiv() {
        return aktiv;
    }

    public void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    public Optional<Long> getBeregningsgrunnlagId() {
        return Optional.ofNullable(beregningsgrunnlagId);
    }

    public void setBeregningsgrunnlag(Long beregningsgrunnlagId) {
        this.beregningsgrunnlagId = beregningsgrunnlagId;
    }

}
