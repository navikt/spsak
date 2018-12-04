package no.nav.foreldrepenger.behandlingslager.uttak;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.Type;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity
@Table(name = "UTTAK_RESULTAT_DOK_REGEL")
public class UttakResultatDokRegelEntitet extends BaseEntitet {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_UTTAK_RESULTAT_DOK_REGEL")
    private Long id;

    @OneToOne
    @JoinColumn(name = "uttak_resultat_periode_id", updatable = false, nullable = false)
    private UttakResultatPeriodeEntitet periode;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "manuell_behandling_aarsak", referencedColumnName = "kode")),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + ManuellBehandlingÅrsak.DISCRIMINATOR + "'")) })
    private ManuellBehandlingÅrsak manuellBehandlingÅrsak = ManuellBehandlingÅrsak.UKJENT;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "regel_input", updatable = false)
    private String regelInput;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "regel_evaluering", updatable = false)
    private String regelEvaluering;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "til_manuell_behandling", nullable = false, updatable = false)
    private boolean tilManuellBehandling;

    public Long getId() {
        return id;
    }

    public ManuellBehandlingÅrsak getManuellBehandlingÅrsak() {
        return manuellBehandlingÅrsak;
    }

    public String getRegelInput() {
        return regelInput;
    }

    public String getRegelEvaluering() {
        return regelEvaluering;
    }

    public boolean isTilManuellBehandling() {
        return tilManuellBehandling;
    }

    @Override
    public String toString() {
        return "UttakResultatDokRegelEntitet{" +
            "id=" + id +
            ", manuellBehandlingÅrsak=" + manuellBehandlingÅrsak.getKode() +
            ", tilManuellVurdering=" + tilManuellBehandling +
            '}';
    }

    public static Builder medManuellBehandling(ManuellBehandlingÅrsak manuellBehandlingÅrsak) {
        return new Builder(true, manuellBehandlingÅrsak);
    }

    public static Builder utenManuellBehandling() {
        return new Builder(false, null);
    }

    public void setPeriode(UttakResultatPeriodeEntitet periode) {
        this.periode = periode;
    }

    public static class Builder {

        private UttakResultatDokRegelEntitet kladd = new UttakResultatDokRegelEntitet();

        private Builder(boolean tilManuellBehandling, ManuellBehandlingÅrsak manuellBehandlingÅrsak) {
            if (manuellBehandlingÅrsak != null) {
                kladd.manuellBehandlingÅrsak = manuellBehandlingÅrsak;
            }
            kladd.tilManuellBehandling = tilManuellBehandling;
        }

        public Builder medRegelInput(String regelInput) {
            kladd.regelInput = regelInput;
            return this;
        }

        public Builder medRegelEvaluering(String regelEvaluering) {
            kladd.regelEvaluering = regelEvaluering;
            return this;
        }

        public UttakResultatDokRegelEntitet build() {
            return kladd;
        }
    }
}
