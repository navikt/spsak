package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Permisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.PermisjonsbeskrivelseType;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.foreldrepenger.domene.typer.Stillingsprosent;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "Permisjon")
@Table(name = "IAY_PERMISJON")
public class PermisjonEntitet extends BaseEntitet implements Permisjon, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PERMISJON")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "yrkesaktivitet_id", nullable = false, updatable = false, unique = true)
    private YrkesaktivitetEntitet yrkesaktivitet;

    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "beskrivelse_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + PermisjonsbeskrivelseType.DISCRIMINATOR + "'"))
    private PermisjonsbeskrivelseType permisjonsbeskrivelseType;

    @Embedded
    private DatoIntervallEntitet periode;

    @ChangeTracked
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "prosentsats")))
    private Stillingsprosent prosentsats;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public PermisjonEntitet() {
        // hibernate
    }

    /**
     * Deep copy ctor
     */
    PermisjonEntitet(Permisjon permisjon) {
        this.permisjonsbeskrivelseType = permisjon.getPermisjonsbeskrivelseType();
        this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(permisjon.getFraOgMed(), permisjon.getTilOgMed());
        this.prosentsats = permisjon.getProsentsats();
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(periode, getPermisjonsbeskrivelseType());
    }

    @Override
    public PermisjonsbeskrivelseType getPermisjonsbeskrivelseType() {
        return permisjonsbeskrivelseType;
    }

    public void setPermisjonsbeskrivelseType(PermisjonsbeskrivelseType permisjonsbeskrivelseType) {
        this.permisjonsbeskrivelseType = permisjonsbeskrivelseType;
    }

    public void setPeriode(LocalDate fraOgMed, LocalDate tilOgMed) {
        if (tilOgMed != null) {
            this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed);
        } else {
            this.periode = DatoIntervallEntitet.fraOgMed(fraOgMed);
        }
    }

    @Override
    public LocalDate getFraOgMed() {
        return periode.getFomDato();
    }

    @Override
    public LocalDate getTilOgMed() {
        return periode.getTomDato();
    }

    @Override
    public Stillingsprosent getProsentsats() {
        return prosentsats;
    }

    public void setProsentsats(Stillingsprosent prosentsats) {
        this.prosentsats = prosentsats;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof PermisjonEntitet)) {
            return false;
        }
        PermisjonEntitet other = (PermisjonEntitet) obj;
        return Objects.equals(this.getPermisjonsbeskrivelseType(), other.getPermisjonsbeskrivelseType())
            && Objects.equals(this.getFraOgMed(), other.getFraOgMed())
            && Objects.equals(this.getTilOgMed(), other.getTilOgMed());
    }

    @Override
    public int hashCode() {
        return Objects.hash(permisjonsbeskrivelseType, periode);
    }

    @Override
    public String toString() {
        return "Adopsjon{" + //$NON-NLS-1$
            "permisjonsbeskrivelseType=" + permisjonsbeskrivelseType + //$NON-NLS-1$
            ", fraOgMed=" + periode.getFomDato() + //$NON-NLS-1$
            ", tilOgMed=" + periode.getTomDato() + //$NON-NLS-1$
            ", v=" + prosentsats + //$NON-NLS-1$
            '}';
    }

    public YrkesaktivitetEntitet getYrkesaktivitet() {
        return yrkesaktivitet;
    }

    void setYrkesaktivitet(YrkesaktivitetEntitet yrkesaktivitet) {
        this.yrkesaktivitet = yrkesaktivitet;
    }

    boolean hasValues() {
        return permisjonsbeskrivelseType != null || periode.getFomDato() != null || prosentsats != null;
    }
}
