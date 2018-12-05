package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

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
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "PersonopplysningAdresse")
@Table(name = "PO_ADRESSE")
class PersonAdresseEntitet extends BaseEntitet implements PersonAdresse, IndexKey {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PO_ADRESSE")
    private Long id;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "aktoer_id", updatable = false)))
    private AktørId aktørId;

    @Embedded
    private DatoIntervallEntitet periode;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "adresse_type", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + AdresseType.DISCRIMINATOR
            + "'"))})
    private AdresseType adresseType;

    @ChangeTracked
    @Column(name = "adresselinje1")
    private String adresselinje1;

    @ChangeTracked
    @Column(name = "adresselinje2")
    private String adresselinje2;

    @ChangeTracked
    @Column(name = "adresselinje3")
    private String adresselinje3;

    @ChangeTracked
    @Column(name = "adresselinje4")
    private String adresselinje4;

    @ChangeTracked
    @Column(name = "postnummer")
    private String postnummer;

    @ChangeTracked
    @Column(name = "poststed")
    private String poststed;

    @ChangeTracked
    @Column(name = "land")
    private String land;

    @ManyToOne(optional = false)
    @JoinColumn(name = "po_informasjon_id", nullable = false, updatable = false)
    private PersonInformasjonEntitet personopplysningInformasjon;

    PersonAdresseEntitet() {
    }

    PersonAdresseEntitet(PersonAdresse adresse) {
        this.adresselinje1 = adresse.getAdresselinje1();
        this.adresselinje2 = adresse.getAdresselinje2();
        this.adresselinje3 = adresse.getAdresselinje3();
        this.adresselinje4 = adresse.getAdresselinje4();
        this.adresseType = adresse.getAdresseType();
        this.postnummer = adresse.getPostnummer();
        this.poststed = adresse.getPoststed();
        this.land = adresse.getLand();

        this.aktørId = adresse.getAktørId();
        this.periode = adresse.getPeriode();
    }
    
    @Override
    public String getIndexKey() {
        return IndexKey.createKey(aktørId, adresseType, land, periode);
    }

    void setPersonopplysningInformasjon(PersonInformasjonEntitet personopplysningInformasjon) {
        this.personopplysningInformasjon = personopplysningInformasjon;
    }

    @Override
    public AdresseType getAdresseType() {
        return adresseType;
    }

    void setAdresseType(AdresseType adresseType) {
        this.adresseType = adresseType;
    }

    @Override
    public String getAdresselinje1() {
        return adresselinje1;
    }

    void setAdresselinje1(String adresselinje1) {
        this.adresselinje1 = adresselinje1;
    }

    @Override
    public String getAdresselinje2() {
        return adresselinje2;
    }

    void setAdresselinje2(String adresselinje2) {
        this.adresselinje2 = adresselinje2;
    }

    @Override
    public String getAdresselinje3() {
        return adresselinje3;
    }

    void setAdresselinje3(String adresselinje3) {
        this.adresselinje3 = adresselinje3;
    }

    @Override
    public String getAdresselinje4() {
        return adresselinje4;
    }

    void setAdresselinje4(String adresselinje4) {
        this.adresselinje4 = adresselinje4;
    }

    @Override
    public String getPostnummer() {
        return postnummer;
    }

    void setPostnummer(String postnummer) {
        this.postnummer = postnummer;
    }

    @Override
    public String getPoststed() {
        return poststed;
    }

    void setPoststed(String poststed) {
        this.poststed = poststed;
    }

    @Override
    public String getLand() {
        return land;
    }

    void setLand(String land) {
        this.land = land;
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

    void setPeriode(DatoIntervallEntitet periode) {
        this.periode = periode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonAdresseEntitet entitet = (PersonAdresseEntitet) o;
        return Objects.equals(aktørId, entitet.aktørId) &&
            Objects.equals(periode, entitet.periode) &&
            Objects.equals(adresseType, entitet.adresseType) &&
            Objects.equals(land, entitet.land);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId, periode, adresseType, land);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PersonAdresseEntitet{");
        sb.append("id=").append(id);
        sb.append(", gyldighetsperiode=").append(periode);
        sb.append(", adresseType=").append(adresseType);
        sb.append(", adresselinje1='").append(adresselinje1).append('\'');
        sb.append(", adresselinje2='").append(adresselinje2).append('\'');
        sb.append(", adresselinje3='").append(adresselinje3).append('\'');
        sb.append(", adresselinje4='").append(adresselinje4).append('\'');
        sb.append(", postnummer='").append(postnummer).append('\'');
        sb.append(", poststed='").append(poststed).append('\'');
        sb.append(", land='").append(land).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
