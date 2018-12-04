package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

/**
 * Entitetsklasse for medlemskap perioder.
 * <p>
 * Implementert iht. builder pattern (ref. "Effective Java, 2. ed." J.Bloch).
 * Non-public constructors og setters, dvs. immutable.
 * <p>
 * OBS: Legger man til nye felter så skal dette oppdateres mange steder:
 * builder, equals, hashcode etc.
 */

@Entity(name = "MedlemskapPerioder")
@Table(name = "MEDLEMSKAP_PERIODER")
class MedlemskapPerioderEntitet extends BaseEntitet implements RegistrertMedlemskapPerioder, IndexKey {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MEDLEMSKAP_PERIODER")
    private Long id;

    @Embedded
    @ChangeTracked
    private DatoIntervallEntitet periode;

    @ChangeTracked
    @Column(name = "beslutningsdato")
    private LocalDate beslutningsdato;

    @ChangeTracked
    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "er_medlem", nullable = false)
    private boolean erMedlem;

    @ChangeTracked
    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "lovvalg_land", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + Landkoder.DISCRIMINATOR + "'"))
    private Landkoder lovvalgLand = Landkoder.UDEFINERT;

    @ChangeTracked
    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "studie_land", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + Landkoder.DISCRIMINATOR + "'"))
    private Landkoder studieLand = Landkoder.UDEFINERT;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "medlemskap_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + MedlemskapType.DISCRIMINATOR + "'"))
    private MedlemskapType medlemskapType = MedlemskapType.UDEFINERT;

    @ChangeTracked
    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "dekning_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + MedlemskapDekningType.DISCRIMINATOR + "'"))
    private MedlemskapDekningType dekningType = MedlemskapDekningType.UDEFINERT;

    @ChangeTracked
    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "kilde_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + MedlemskapKildeType.DISCRIMINATOR + "'"))
    private MedlemskapKildeType kildeType = MedlemskapKildeType.UDEFINERT;

    @Column(name = "medl_id", columnDefinition = "NUMERIC")
    private Long medlId;

    MedlemskapPerioderEntitet() {
        // hibernate
    }

    /**
     * Deep copy.
     */
    public MedlemskapPerioderEntitet(RegistrertMedlemskapPerioder medlemskapPerioderMal) {
        this.periode = medlemskapPerioderMal.getPeriode();
        this.beslutningsdato = medlemskapPerioderMal.getBeslutningsdato();
        this.erMedlem = medlemskapPerioderMal.getErMedlem();
        this.setLovvalgLand(medlemskapPerioderMal.getLovvalgLand());
        this.setStudieland(medlemskapPerioderMal.getStudieland());
        this.setMedlemskapType(medlemskapPerioderMal.getMedlemskapType());
        this.setDekningType(medlemskapPerioderMal.getDekningType());
        this.setKildeType(medlemskapPerioderMal.getKildeType());
        this.setMedlId(medlemskapPerioderMal.getMedlId());
    }
    
    @Override
    public String getIndexKey() {
        //redusert fra equals
        return IndexKey.createKey(periode, medlId, dekningType, kildeType);
    }

    @Override
    public LocalDate getFom() {
        return periode != null ? periode.getFomDato() : null;
    }

    @Override
    public LocalDate getTom() {
        return periode != null ? periode.getTomDato() : null;
    }

    @Override
    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    public void setPeriode(LocalDate fom, LocalDate tom) {
        this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    @Override
    public LocalDate getBeslutningsdato() {
        return beslutningsdato;
    }

    void setBeslutningsdato(LocalDate beslutningsdato) {
        this.beslutningsdato = beslutningsdato;
    }

    @Override
    public boolean getErMedlem() {
        return erMedlem;
    }

    void setErMedlem(boolean erMedlem) {
        this.erMedlem = erMedlem;
    }

    @Override
    public MedlemskapType getMedlemskapType() {
        return medlemskapType == null || Objects.equals(MedlemskapType.UDEFINERT, medlemskapType) ? null : medlemskapType;
    }

    void setMedlemskapType(MedlemskapType medlemskapType) {
        this.medlemskapType = medlemskapType == null ? MedlemskapType.UDEFINERT : medlemskapType;
    }

    @Override
    public MedlemskapDekningType getDekningType() {
        return dekningType == null || Objects.equals(MedlemskapDekningType.UDEFINERT, dekningType) ? null : dekningType;
    }

    void setDekningType(MedlemskapDekningType dekningType) {
        this.dekningType = dekningType == null ? MedlemskapDekningType.UDEFINERT : dekningType;
    }

    @Override
    public MedlemskapKildeType getKildeType() {
        return kildeType == null || Objects.equals(kildeType, MedlemskapKildeType.UDEFINERT) ? null : kildeType;
    }

    void setKildeType(MedlemskapKildeType kildeType) {
        this.kildeType = kildeType == null ? MedlemskapKildeType.UDEFINERT : kildeType;
    }

    @Override
    public Landkoder getLovvalgLand() {
        return lovvalgLand == null || Objects.equals(lovvalgLand, Landkoder.UDEFINERT) ? null : lovvalgLand;
    }

    void setLovvalgLand(Landkoder lovvalgsland) {
        this.lovvalgLand = lovvalgsland == null ? Landkoder.UDEFINERT : lovvalgsland;
    }

    @Override
    public Landkoder getStudieland() {
        return studieLand == null || Objects.equals(studieLand, Landkoder.UDEFINERT) ? null : studieLand;
    }

    void setStudieland(Landkoder studieland) {
        this.studieLand = studieland == null ? Landkoder.UDEFINERT : studieland;
    }

    @Override
    public Long getMedlId() {
        return medlId;
    }

    void setMedlId(Long medlId) {
        this.medlId = medlId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof MedlemskapPerioderEntitet)) {
            return false;
        }
        // minste sett med felter som angir ett medlemskap periode(uten 'muterbare' felter)
        MedlemskapPerioderEntitet other = (MedlemskapPerioderEntitet) obj;
        return Objects.equals(this.getFom(), other.getFom())
            && Objects.equals(this.getTom(), other.getTom())
            && Objects.equals(this.getDekningType(), other.getDekningType())
            && Objects.equals(this.getKildeType(), other.getKildeType())
            && Objects.equals(this.getMedlId(), other.getMedlId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFom(), getTom(), getDekningType(), getKildeType(), getMedlId());
    }

    @Override
    public int compareTo(RegistrertMedlemskapPerioder other) {
        return other.getMedlId().compareTo(this.getMedlId());
    }
}
