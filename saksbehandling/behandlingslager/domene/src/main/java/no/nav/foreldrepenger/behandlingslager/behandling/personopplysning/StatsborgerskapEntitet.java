package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import no.nav.foreldrepenger.domene.typer.AktørId;
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
import javax.persistence.Transient;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "PersonopplysningStatsborgerskap")
@Table(name = "PO_STATSBORGERSKAP")
class StatsborgerskapEntitet extends BaseEntitet implements Statsborgerskap, IndexKey {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PO_STATSBORGERSKAP")
    private Long id;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "aktoer_id", updatable = false)))
    private AktørId aktørId;

    @Embedded
    private DatoIntervallEntitet periode;

    @ChangeTracked
    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "statsborgerskap", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + Landkoder.DISCRIMINATOR + "'"))
    private Landkoder statsborgerskap = Landkoder.UDEFINERT;

    @ManyToOne(optional = false)
    @JoinColumn(name = "po_informasjon_id", nullable = false, updatable = false)
    private PersonInformasjonEntitet personopplysningInformasjon;

    @Transient
    private Region region;

    StatsborgerskapEntitet() {
    }

    StatsborgerskapEntitet(Statsborgerskap statsborgerskap) {
        this.aktørId = statsborgerskap.getAktørId();
        this.periode = statsborgerskap.getPeriode();
        this.statsborgerskap = statsborgerskap.getStatsborgerskap();
    }
    
    @Override
    public String getIndexKey() {
        return IndexKey.createKey(aktørId, statsborgerskap, periode);
    }

    void setPersonopplysningInformasjon(PersonInformasjonEntitet personopplysningInformasjon) {
        this.personopplysningInformasjon = personopplysningInformasjon;
    }

    void setRegion(Region region) {
        this.region = region;
    }

    @Override
    public AktørId getAktørId() {
        return aktørId;
    }

    void setAktørId(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    @Override
    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    void setPeriode(DatoIntervallEntitet gyldighetsperiode) {
        this.periode = gyldighetsperiode;
    }

    @Override
    public Landkoder getStatsborgerskap() {
        return statsborgerskap;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    void setStatsborgerskap(Landkoder statsborgerskap) {
        this.statsborgerskap = statsborgerskap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatsborgerskapEntitet entitet = (StatsborgerskapEntitet) o;
        return Objects.equals(aktørId, entitet.aktørId) &&
            Objects.equals(periode, entitet.periode) &&
            Objects.equals(statsborgerskap, entitet.statsborgerskap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId, periode, statsborgerskap);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StatsborgerskapEntitet{");
        sb.append("gyldighetsperiode=").append(periode);
        sb.append(", statsborgerskap=").append(statsborgerskap);
        sb.append('}');
        return sb.toString();
    }

}
