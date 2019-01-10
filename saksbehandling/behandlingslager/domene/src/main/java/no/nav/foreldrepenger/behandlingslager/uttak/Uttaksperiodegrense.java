package no.nav.foreldrepenger.behandlingslager.uttak;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Type;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity
@Table(name = "UTTAKSPERIODEGRENSE")
public class Uttaksperiodegrense extends BaseEntitet {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_UTTAKSPERIODEGRENSE")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "behandling_resultat_id", nullable = false)
    private Behandlingsresultat behandlingsresultat;

    @Column(name = "MOTTATTDATO", nullable = false)
    private LocalDate mottattDato;

    @Column(name = "FOERSTE_LOVLIGE_UTTAKSDAG", nullable = false)
    private LocalDate førsteLovligeUttaksdag;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "SPORING_INPUT")
    private String sporingInput;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "SPORING_REGEL")
    private String sporingRegel;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    Long getId(){return id;}

    public boolean getErAktivt() {
        return aktiv;
    }

    void setAktiv(boolean aktivt) {
        this.aktiv = aktivt;
    }

    public LocalDate getMottattDato() {
        return mottattDato;
    }

    public LocalDate getFørsteLovligeUttaksdag() {
        return førsteLovligeUttaksdag;
    }

    public static Builder fraEksisterende(Uttaksperiodegrense uttaksperiodegrense) {
        return new Builder(uttaksperiodegrense.behandlingsresultat)
            .medMottattDato(uttaksperiodegrense.getMottattDato())
            .medFørsteLovligeUttaksdag(uttaksperiodegrense.getFørsteLovligeUttaksdag())
            .medSporingInput(uttaksperiodegrense.sporingInput)
            .medSporingInput(uttaksperiodegrense.sporingRegel);
    }

    public static class Builder {
        Uttaksperiodegrense kladd;

        public Builder(Behandlingsresultat behandlingsresultat) {
            kladd = new Uttaksperiodegrense();
            kladd.behandlingsresultat = behandlingsresultat;
        }

        public Builder medSporingInput(String sporingInput) {
            kladd.sporingInput = sporingInput;
            return this;
        }

        public Builder medSporingRegel(String sporingRegel) {
            kladd.sporingRegel = sporingRegel;
            return this;
        }

        public Builder medMottattDato(LocalDate mottattDato){
            Objects.requireNonNull(mottattDato);
            kladd.mottattDato = mottattDato;
            return this;
        }

        public Builder medFørsteLovligeUttaksdag(LocalDate førsteLovligeUttaksdag){
            Objects.requireNonNull(førsteLovligeUttaksdag);
            kladd.førsteLovligeUttaksdag = førsteLovligeUttaksdag;
            return this;
        }

        public Uttaksperiodegrense build() {
            return kladd;
        }
    }
}
