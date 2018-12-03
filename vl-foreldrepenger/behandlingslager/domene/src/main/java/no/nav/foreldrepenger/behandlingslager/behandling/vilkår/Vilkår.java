package no.nav.foreldrepenger.behandlingslager.behandling.vilkår;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.Type;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.vedtak.felles.jpa.converters.PropertiesToStringConverter;

@Entity(name = "Vilkar")
@Table(name = "VILKAR")
public class Vilkår extends BaseEntitet implements IndexKey {

    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "avslag_kode", referencedColumnName = "kode"))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + Avslagsårsak.DISCRIMINATOR + "'"))
    private Avslagsårsak avslagsårsak = Avslagsårsak.UDEFINERT;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VILKAR")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vilkar_resultat_id", nullable = false, updatable = false)
    private VilkårResultat vilkårResultat;

    @Convert(converter = PropertiesToStringConverter.class)
    @Column(name = "merknad_parametere")
    private Properties merknadParametere = new Properties();

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "vilkar_type", referencedColumnName = "kode", nullable = false)),
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + VilkårType.DISCRIMINATOR
                + "'")) })
    private VilkårType vilkårType;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "vilkar_utfall", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + VilkårUtfallType.DISCRIMINATOR + "'"))
    private VilkårUtfallType vilkårUtfall = VilkårUtfallType.UDEFINERT;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "vilkar_utfall_manuell", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + VilkårUtfallType.DISCRIMINATOR + "'"))
    private VilkårUtfallType vilkårUtfallManuelt = VilkårUtfallType.UDEFINERT;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "vilkar_utfall_overstyrt", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + VilkårUtfallType.DISCRIMINATOR + "'"))
    private VilkårUtfallType vilkårUtfallOverstyrt = VilkårUtfallType.UDEFINERT;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "vilkar_utfall_merknad", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + VilkårUtfallMerknad.DISCRIMINATOR + "'"))
    private VilkårUtfallMerknad vilkårUtfallMerknad = VilkårUtfallMerknad.UDEFINERT;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "regel_evaluering")
    @Basic(fetch = FetchType.LAZY)
    private String regelEvaluering;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "regel_input")
    @Basic(fetch = FetchType.LAZY)
    private String regelInput;

    Vilkår() {
        // for hibernate og builder
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(getVilkårType());
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Vilkår)) {
            return false;
        }
        Vilkår other = (Vilkår) object;
        return Objects.equals(getVilkårType(), other.getVilkårType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVilkårType());
    }

    public boolean erIkkeOppfylt() {
        return Objects.equals(VilkårUtfallType.IKKE_OPPFYLT, getGjeldendeVilkårUtfall());
    }

    public boolean erManueltVurdert() {
        return !asList(VilkårUtfallType.UDEFINERT, VilkårUtfallType.IKKE_VURDERT).contains(vilkårUtfallManuelt);
    }

    public boolean erOverstyrt() {
        return !asList(VilkårUtfallType.UDEFINERT, VilkårUtfallType.IKKE_VURDERT).contains(vilkårUtfallOverstyrt);
    }

    public Avslagsårsak getAvslagsårsak() {
        return Objects.equals(Avslagsårsak.UDEFINERT, avslagsårsak) ? null : avslagsårsak;
    }

    /**
     * Returnerer {@link Avslagsårsak} dersom vilkår ikke er oppfylt.
     *
     * Som et resultat av automatisk regelvurdering kan {@link Avslagsårsak} persisteres på vilkår.
     * Men vilkårutfallet kan være overstyrt ift automatisk regelvurdering. Ettersom kun én {@link Avslagsårsak} lagres,
     * må det logisk evalueres at gjeldende utfall er IKKE_OPPFYLT for at {@link Avslagsårsak} skal være gyldig.
     *
     * @return Avslagsårsak
     */
    public Avslagsårsak getGjeldendeAvslagsårsak() {
        if (getGjeldendeVilkårUtfall().equals(VilkårUtfallType.IKKE_OPPFYLT)) {
            return getAvslagsårsak();
        }
        return null;
    }

    public Long getId() {
        return id;
    }

    public Properties getMerknadParametere() {
        return merknadParametere;
    }

    public VilkårType getVilkårType() {
        return vilkårType;
    }

    /**
     * Gir det mest relevante utfallet avhengig av hva som er registrert.
     * <ol>
     * <li>Automatisk utfall</li>
     * <li>Manuelt utfall</li>
     * <li>Overstyrt utfall</li>
     * </ol>
     *
     * @return VilkårUtfallType
     */
    public VilkårUtfallType getGjeldendeVilkårUtfall() {
        if (!vilkårUtfallOverstyrt.equals(VilkårUtfallType.UDEFINERT)) {
            return vilkårUtfallOverstyrt;
        } if (!vilkårUtfallManuelt.equals(VilkårUtfallType.UDEFINERT)) {
            return vilkårUtfallManuelt;
        }
        return vilkårUtfall;
    }

    public VilkårUtfallType getVilkårUtfallManuelt() {
        return vilkårUtfallManuelt;
    }

    public VilkårUtfallType getVilkårUtfallOverstyrt() {
        return vilkårUtfallOverstyrt;
    }

    public VilkårUtfallMerknad getVilkårUtfallMerknad() {
        return Objects.equals(vilkårUtfallMerknad, VilkårUtfallMerknad.UDEFINERT) ? null : vilkårUtfallMerknad;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<"
            + "type=" + getVilkårType()
            + ", utfall=" + vilkårUtfall
            + ", utfallManuelt=" + vilkårUtfallManuelt
            + ", utfallOverstyrt=" + vilkårUtfallOverstyrt
            + ">";
    }

    public VilkårResultat getVilkårResultat() {
        return vilkårResultat;
    }

    void setVilkårType(VilkårType vilkårType) {
        this.vilkårType = vilkårType;
    }

    void setAvslagsårsak(Avslagsårsak avslagsårsak) {
        this.avslagsårsak = Optional.ofNullable(avslagsårsak).orElse(Avslagsårsak.UDEFINERT);
    }

    void setVilkårResultat(VilkårResultat vilkårResultat) {
        this.vilkårResultat = vilkårResultat;
    }

    public String getRegelEvaluering() {
        return regelEvaluering;
    }

    void setRegelEvaluering(String regelEvaluering) {
        this.regelEvaluering = regelEvaluering;
    }

    public String getRegelInput() {
        return regelInput;
    }

    void setRegelInput(String regelInput) {
        this.regelInput = regelInput;
    }

    void setVilkårUtfallManuelt(VilkårUtfallType vilkårUtfallManuelt) {
        this.vilkårUtfallManuelt = vilkårUtfallManuelt;
    }

    void setVilkårUtfallOverstyrt(VilkårUtfallType vilkårUtfallOverstyrt) {
        this.vilkårUtfallOverstyrt = vilkårUtfallOverstyrt;
    }

    void setMerknadParametere(Properties merknadParametere) {
        if (merknadParametere != null) {
            this.merknadParametere.clear();
            final Properties mineParametere = this.merknadParametere;
            // FIXME (FC): quick-fix, støtter ikke merkand parmetere som ikke er string p.t. da det skrives i property file format
            merknadParametere.entrySet().stream()
                .filter(e -> e.getValue() instanceof String) // ikke annet enn string
                .forEach(e -> mineParametere.setProperty((String) e.getKey(), (String) e.getValue()));
        }
    }

    void setVilkårUtfall(VilkårUtfallType vilkårUtfall) {
        this.vilkårUtfall = vilkårUtfall;
    }

    void setVilkårUtfallMerknad(VilkårUtfallMerknad vilkårUtfallMerknad) {
        this.vilkårUtfallMerknad = vilkårUtfallMerknad == null ? VilkårUtfallMerknad.UDEFINERT : vilkårUtfallMerknad;
    }

    @SuppressWarnings("rawtypes")
    List tuples() {
        // returnerer liste av alle felter i et vilkår for enklere sammenligning
        return asList(
            this.getGjeldendeVilkårUtfall(),
            this.getVilkårUtfallManuelt(),
            this.getVilkårUtfallOverstyrt(),
            this.getAvslagsårsak(),
            this.getMerknadParametere(),
            this.getVilkårUtfallMerknad(),
            this.getRegelInput(),
            this.getRegelEvaluering());
    }
}
