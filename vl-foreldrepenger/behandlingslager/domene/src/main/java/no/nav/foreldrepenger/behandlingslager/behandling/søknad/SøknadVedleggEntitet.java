package no.nav.foreldrepenger.behandlingslager.behandling.søknad;

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

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.behandling.Innsendingsvalg;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

/**
 * Entitetsklasse for vedlegg.
 *
 * Implementert iht. builder pattern (ref. "Effective Java, 2. ed." J.Bloch).
 * Non-public constructors og setters, dvs. immutable.
 *
 * OBS: Legger man til nye felter så skal dette oppdateres mange steder:
 * builder, equals, hashcode etc.
 */

@Entity(name = "SøknadVedlegg")
@Table(name = "SOEKNAD_VEDLEGG")
public class SøknadVedleggEntitet extends BaseEntitet implements SøknadVedlegg {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SOEKNAD_VEDLEGG")
    private Long id;

    @Column(name = "skjemanummer")
    private String skjemanummer;

    @Column(name = "tilleggsinfo")
    private String tilleggsinfo;

    @Column(name = "VEDLEGG_PAKREVD", nullable = false)
    @Convert(converter = BooleanToStringConverter.class)
    private boolean erVedleggPåkrevdISøknadsdialog;

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "innsendingsvalg", referencedColumnName = "kode", nullable=false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + Innsendingsvalg.DISCRIMINATOR + "'")) })
    private Innsendingsvalg innsendingsvalg = Innsendingsvalg.IKKE_VALGT;

    @ManyToOne(optional = false)
    @JoinColumn(name = "soeknad_id", nullable = false, updatable=false)
    private SøknadEntitet søknad;

    SøknadVedleggEntitet(){
        // Hibernate
    }

    SøknadVedleggEntitet(SøknadVedlegg søknadVedleggMal){
        this.skjemanummer = søknadVedleggMal.getSkjemanummer();
        this.tilleggsinfo = søknadVedleggMal.getTilleggsinfo();
        this.erVedleggPåkrevdISøknadsdialog = søknadVedleggMal.isErPåkrevdISøknadsdialog();
        this.setInnsendingsvalg(søknadVedleggMal.getInnsendingsvalg());
    }

    @Override
    public String getSkjemanummer() {
        return skjemanummer;
    }

    void setSkjemanummer(String skjemanummer) {
        this.skjemanummer = skjemanummer;
    }

    @Override
    public String getTilleggsinfo() {
        return tilleggsinfo;
    }

    void setTilleggsinfo(String tilleggsinfo) {
        this.tilleggsinfo = tilleggsinfo;
    }

    @Override
    public boolean isErPåkrevdISøknadsdialog() {
        return erVedleggPåkrevdISøknadsdialog;
    }

    void setErPåkrevdISøknadsdialog(boolean erPåkrevdISøknadsdialog) {
        this.erVedleggPåkrevdISøknadsdialog = erPåkrevdISøknadsdialog;
    }

    @Override
    public Innsendingsvalg getInnsendingsvalg() {
        return innsendingsvalg;
    }

    void setInnsendingsvalg(Innsendingsvalg innsendingsvalg) {
        this.innsendingsvalg = innsendingsvalg;
    }

    void setSøknad(SøknadEntitet søknad) {
        this.søknad = søknad;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof SøknadVedleggEntitet)) {
            return false;
        }
        SøknadVedleggEntitet other = (SøknadVedleggEntitet) obj;
        return Objects.equals(this.isErPåkrevdISøknadsdialog(), other.isErPåkrevdISøknadsdialog())
                && Objects.equals(this.getInnsendingsvalg(), other.getInnsendingsvalg())
                && Objects.equals(this.getSkjemanummer(), other.getSkjemanummer())
                && Objects.equals(this.getTilleggsinfo(), other.getTilleggsinfo());
    }

    @Override
    public int hashCode() {
        return Objects.hash(erVedleggPåkrevdISøknadsdialog, getInnsendingsvalg(), skjemanummer, tilleggsinfo);
    }

    public static class Builder {
        private SøknadVedleggEntitet søknadVedleggMal;

        public Builder(){
            søknadVedleggMal = new SøknadVedleggEntitet();
        }

        public Builder(SøknadVedlegg søknadVedlegg){
            if (søknadVedlegg != null){
                søknadVedleggMal = new SøknadVedleggEntitet(søknadVedlegg);
            } else {
                søknadVedleggMal = new SøknadVedleggEntitet();
            }
        }

        public Builder medErPåkrevdISøknadsdialog(boolean erPåkrevdISøknadsdialog){
            søknadVedleggMal.erVedleggPåkrevdISøknadsdialog = erPåkrevdISøknadsdialog;
            return this;
        }

        public Builder medInnsendingsvalg(Innsendingsvalg innsendingsvalg){
            søknadVedleggMal.setInnsendingsvalg(innsendingsvalg);
            return this;
        }

        public Builder medSkjemanummer(String skjemanummer){
            søknadVedleggMal.skjemanummer = skjemanummer;
            return this;
        }

        public Builder medTilleggsinfo(String tilleggsinfo){
            søknadVedleggMal.tilleggsinfo = tilleggsinfo;
            return this;
        }

        public SøknadVedlegg build(){
            return søknadVedleggMal;
        }
    }
}
