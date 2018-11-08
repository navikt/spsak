package no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse;

import java.time.LocalDate;
import java.util.Objects;

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
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

/**
 * Entitetsklasse for bekreftet informasjon angående adopsjon.
 * <p>
 * Implementert iht. builder pattern (ref. "Effective Java, 2. ed." J.Bloch).
 * Non-public constructors og setters, dvs. immutable.
 * <p>
 * OBS: Legger man til nye felter så skal dette oppdateres mange steder:
 * builder, equals, hashcode etc.
 */
@Entity(name = "Adopsjon")
@Table(name = "FH_ADOPSJON")
public class AdopsjonEntitet extends BaseEntitet implements Adopsjon {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_ADOPSJON")
    private Long id;

    @ChangeTracked
    @Column(name = "omsorgsovertakelse_dato")
    private LocalDate omsorgsovertakelseDato;

    @ChangeTracked
    @Column(name = "ankomst_norge_dato")
    private LocalDate ankomstNorge;

    @ChangeTracked
    @Column(name = "foreldreansvar_oppfylt_dato")
    private LocalDate foreldreansvarDato;

    @ChangeTracked
    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "ektefelles_barn")
    private Boolean erEktefellesBarn;

    @ChangeTracked
    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "adopterer_alene")
    private Boolean adoptererAlene;

    @OneToOne(optional = false)
    @JoinColumn(name = "familie_hendelse_id", nullable = false, updatable = false, unique = true)
    private FamilieHendelseEntitet familieHendelse;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @ChangeTracked
    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "omsorg_vilkaar_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'"
        + OmsorgsovertakelseVilkårType.DISCRIMINATOR + "'"))
    private OmsorgsovertakelseVilkårType omsorgsovertakelseVilkårType = OmsorgsovertakelseVilkårType.UDEFINERT;


    AdopsjonEntitet() {
        // Hibernate
    }

    /**
     * Deep copy ctor
     */
    AdopsjonEntitet(Adopsjon adopsjon) {
        this.adoptererAlene = adopsjon.getAdoptererAlene();
        this.erEktefellesBarn = adopsjon.getErEktefellesBarn();
        this.omsorgsovertakelseDato = adopsjon.getOmsorgsovertakelseDato();
        this.ankomstNorge = adopsjon.getAnkomstNorgeDato();
        this.foreldreansvarDato = adopsjon.getForeldreansvarDato();
    }

    @Override
    public LocalDate getOmsorgsovertakelseDato() {
        return omsorgsovertakelseDato;
    }

    void setOmsorgsovertakelseDato(LocalDate omsorgsovertakelseDato) {
        this.omsorgsovertakelseDato = omsorgsovertakelseDato;
    }

    @Override
    public LocalDate getAnkomstNorgeDato() {
        return ankomstNorge;
    }

    void setAnkomstNorgeDato(LocalDate ankomstNorgeDato) {
        this.ankomstNorge = ankomstNorgeDato;
    }

    @Override
    public LocalDate getForeldreansvarDato() {
        return foreldreansvarDato;
    }

    void setForeldreansvarDato(LocalDate foreldreansvarDato){
        this.foreldreansvarDato = foreldreansvarDato;
    }

    @Override
    public Boolean getErEktefellesBarn() {
        return erEktefellesBarn;
    }

    void setErEktefellesBarn(boolean erEktefellesBarn) {
        this.erEktefellesBarn = erEktefellesBarn;
    }

    @Override
    public Boolean getAdoptererAlene() {
        return adoptererAlene;
    }

    void setAdoptererAlene(boolean adoptererAlene) {
        this.adoptererAlene = adoptererAlene;
    }

    @Override
    public OmsorgsovertakelseVilkårType getOmsorgovertakelseVilkår() {
        return omsorgsovertakelseVilkårType;
    }

    void setOmsorgsovertakelseVilkårType(OmsorgsovertakelseVilkårType omsorgsovertakelseVilkårType) {
        this.omsorgsovertakelseVilkårType = omsorgsovertakelseVilkårType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof AdopsjonEntitet)) {
            return false;
        }
        AdopsjonEntitet other = (AdopsjonEntitet) obj;
        return Objects.equals(this.getOmsorgsovertakelseDato(), other.getOmsorgsovertakelseDato())
            && Objects.equals(this.getAnkomstNorgeDato(), other.getAnkomstNorgeDato())
            && Objects.equals(this.getErEktefellesBarn(), other.getErEktefellesBarn())
            && Objects.equals(this.getAdoptererAlene(), other.getAdoptererAlene());
    }

    @Override
    public int hashCode() {
        return Objects.hash(omsorgsovertakelseDato, ankomstNorge, adoptererAlene, erEktefellesBarn);
    }

    @Override
    public String toString() {
        return "Adopsjon{" +
            "omsorgsovertakelseDato=" + omsorgsovertakelseDato +
            ", ankomstNorge=" + ankomstNorge +
            ", erEktefellesBarn=" + erEktefellesBarn +
            ", adoptererAlene=" + adoptererAlene +
            '}';
    }

    public FamilieHendelseEntitet getFamilieHendelse() {
        return familieHendelse;
    }

    void setFamilieHendelse(FamilieHendelseEntitet familieHendelse) {
        this.familieHendelse = familieHendelse;
    }

    public static class Builder {
        private AdopsjonEntitet mal;

        public Builder() {
            this.mal = new AdopsjonEntitet();
        }

        public Builder(Adopsjon adopsjon) {
            if (adopsjon != null) {
                this.mal = new AdopsjonEntitet(adopsjon);
            } else {
                this.mal = new AdopsjonEntitet();
            }
        }

        public Builder medAdoptererAlene(boolean adoptererAlene) {
            this.mal.adoptererAlene = adoptererAlene;
            return this;
        }

        public Builder medErEktefellesBarn(boolean erEktefellesBarn) {
            this.mal.erEktefellesBarn = erEktefellesBarn;
            return this;
        }

        public Builder medOmsorgsovertakelseDato(LocalDate omsorgsovertakelseDato) {
            this.mal.omsorgsovertakelseDato = omsorgsovertakelseDato;
            return this;
        }

        public Builder medAnkomstNorgeDato(LocalDate ankomstNorge) {
            this.mal.ankomstNorge = ankomstNorge;
            return this;
        }

        public Adopsjon build() {
            return this.mal;
        }

    }
}
