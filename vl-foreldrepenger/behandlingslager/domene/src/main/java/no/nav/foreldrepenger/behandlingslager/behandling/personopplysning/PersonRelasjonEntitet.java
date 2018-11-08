package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
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
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "PersonopplysningRelasjon")
@Table(name = "PO_RELASJON")
class PersonRelasjonEntitet extends BaseEntitet implements PersonRelasjon, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PO_RELASJON")
    private Long id;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "fra_aktoer_id", updatable = false, nullable=false)))
    @ChangeTracked
    private AktørId fraAktørId;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "til_aktoer_id", updatable = false, nullable=false)))
    @ChangeTracked
    private AktørId tilAktørId;

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "relasjonsrolle", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + RelasjonsRolleType.DISCRIMINATOR
            + "'"))})
    @ChangeTracked
    private RelasjonsRolleType relasjonsrolle;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "har_samme_bosted")
    @ChangeTracked
    private Boolean harSammeBosted;

    @ManyToOne(optional = false)
    @JoinColumn(name = "po_informasjon_id", nullable = false, updatable = false)
    private PersonInformasjonEntitet personopplysningInformasjon;

    PersonRelasjonEntitet() {
    }

    PersonRelasjonEntitet(PersonRelasjon relasjon) {
        this.fraAktørId = relasjon.getAktørId();
        this.tilAktørId = relasjon.getTilAktørId();
        this.relasjonsrolle = relasjon.getRelasjonsrolle();
        this.harSammeBosted = relasjon.getHarSammeBosted();
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(fraAktørId, this.relasjonsrolle, this.tilAktørId);
    }

    void setFraAktørId(AktørId fraAktørId) {
        this.fraAktørId = fraAktørId;
    }

    void setTilAktørId(AktørId tilAktørId) {
        this.tilAktørId = tilAktørId;
    }

    void setHarSammeBosted(Boolean harSammeBosted) {
        this.harSammeBosted = harSammeBosted;
    }

    void setRelasjonsrolle(RelasjonsRolleType relasjonsrolle) {
        this.relasjonsrolle = relasjonsrolle;
    }

    void setPersonopplysningInformasjon(PersonInformasjonEntitet personopplysningInformasjon) {
        this.personopplysningInformasjon = personopplysningInformasjon;
    }

    @Override
    public AktørId getAktørId() {
        return fraAktørId;
    }

    @Override
    public AktørId getTilAktørId() {
        return tilAktørId;
    }

    @Override
    public RelasjonsRolleType getRelasjonsrolle() {
        return relasjonsrolle;
    }

    @Override
    public Boolean getHarSammeBosted() {
        return harSammeBosted;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PersonRelasjonEntitet{");
        sb.append("relasjonsrolle=").append(relasjonsrolle);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonRelasjonEntitet entitet = (PersonRelasjonEntitet) o;
        return Objects.equals(fraAktørId, entitet.fraAktørId) &&
            Objects.equals(tilAktørId, entitet.tilAktørId) &&
            Objects.equals(harSammeBosted, entitet.harSammeBosted) &&
            Objects.equals(relasjonsrolle, entitet.relasjonsrolle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fraAktørId, tilAktørId, harSammeBosted, relasjonsrolle);
    }

}
