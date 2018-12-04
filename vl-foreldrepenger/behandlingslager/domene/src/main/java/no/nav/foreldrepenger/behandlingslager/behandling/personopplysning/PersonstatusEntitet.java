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

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "PersonopplysningPersonstatus")
@Table(name = "PO_PERSONSTATUS")
class PersonstatusEntitet extends BaseEntitet implements Personstatus, IndexKey {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PO_PERSONSTATUS")
    private Long id;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "aktoer_id", updatable = false, nullable=false)))
    private AktørId aktørId;

    @Embedded
    private DatoIntervallEntitet periode;

    @ChangeTracked
    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "personstatus", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + PersonstatusType.DISCRIMINATOR + "'"))
    private PersonstatusType personstatus = PersonstatusType.UDEFINERT;

    @ManyToOne(optional = false)
    @JoinColumn(name = "po_informasjon_id", nullable = false, updatable = false)
    private PersonInformasjonEntitet personopplysningInformasjon;

    PersonstatusEntitet() {
    }

    PersonstatusEntitet(Personstatus personstatus) {
        this.aktørId = personstatus.getAktørId();
        this.periode = personstatus.getPeriode();
        this.personstatus = personstatus.getPersonstatus();
    }
    
    @Override
    public String getIndexKey() {
        return IndexKey.createKey(aktørId, personstatus, periode);
    }

    void setPersonInformasjon(PersonInformasjonEntitet personInformasjon) {
        this.personopplysningInformasjon = personInformasjon;
    }

    void setId(Long id) {
        this.id = id;
    }

    void setPersonstatus(PersonstatusType personstatus) {
        this.personstatus = personstatus;
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
    public PersonstatusType getPersonstatus() {
        return personstatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonstatusEntitet entitet = (PersonstatusEntitet) o;
        return Objects.equals(aktørId, entitet.aktørId) &&
            Objects.equals(periode, entitet.periode) &&
            Objects.equals(personstatus, entitet.personstatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId, periode, personstatus);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PersonstatusEntitet{");
        sb.append("gyldighetsperiode=").append(periode);
        sb.append(", personstatus=").append(personstatus);
        sb.append('}');
        return sb.toString();
    }

}
