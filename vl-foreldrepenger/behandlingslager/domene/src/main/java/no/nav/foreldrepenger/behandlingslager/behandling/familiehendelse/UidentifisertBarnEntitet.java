package no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;

@Entity(name = "UidentifisertBarn")
@Table(name = "FH_UIDENTIFISERT_BARN")
public class UidentifisertBarnEntitet extends BaseEntitet implements UidentifisertBarn, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_UIDENTIFISERT_BARN")
    private Long id;

    @Column(name = "foedsel_dato", nullable = false)
    @ChangeTracked
    private LocalDate fødselsdato;

    @Column(name = "doedsdato")
    @ChangeTracked
    private LocalDate dødsdato;

    @Column(name = "barn_nummer")
    private Integer barnNummer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "familie_hendelse_id", nullable = false, updatable = false)
    private FamilieHendelseEntitet familieHendelse;

    UidentifisertBarnEntitet() {
        // Hibernate
    }

    /**
     * Bruker ikke builder pattern siden få felter.
     * Denne constructor får være public.
     */
    public UidentifisertBarnEntitet(LocalDate fødselsdato) {
        this.fødselsdato = fødselsdato;
    }

    /**
     * Bruker ikke builder pattern siden få felter.
     * Denne constructor får være public.
     */
    public UidentifisertBarnEntitet(UidentifisertBarn barn) {
        this.barnNummer = barn.getBarnNummer();
        this.fødselsdato = barn.getFødselsdato();
        barn.getDødsdato().ifPresent(this::setDødsdato);
    }

    public UidentifisertBarnEntitet(LocalDate fødselsDato, Integer barnNummer) {
        this(fødselsDato);
        this.barnNummer = barnNummer;
    }

    public UidentifisertBarnEntitet(Integer barnNummer, LocalDate fødselsDato, LocalDate dødsdato) {
        this(fødselsDato);
        this.barnNummer = barnNummer;
        this.dødsdato = dødsdato;
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(fødselsdato, barnNummer);
    }

    void deepCopy(UidentifisertBarn barn) {
        this.fødselsdato = barn.getFødselsdato();
    }

    public Long getId() {
        return id;
    }

    @Override
    public LocalDate getFødselsdato() {
        return fødselsdato;
    }

    void setFødselsdato(LocalDate fødselsdato) {
        this.fødselsdato = fødselsdato;
    }

    @Override
    public Optional<LocalDate> getDødsdato() {
        return Optional.ofNullable(dødsdato);
    }

    void setDødsdato(LocalDate dødsdato) {
        this.dødsdato = dødsdato;
    }

    @Override
    public Integer getBarnNummer() {
        return barnNummer;
    }

    public FamilieHendelseEntitet getFamilieHendelse() {
        return familieHendelse;
    }

    void setFamilieHendelse(FamilieHendelseEntitet familieHendelse) {
        this.familieHendelse = familieHendelse;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof UidentifisertBarnEntitet)) {
            return false;
        }
        UidentifisertBarnEntitet other = (UidentifisertBarnEntitet) obj;
        return Objects.equals(this.fødselsdato, other.getFødselsdato())
            && Objects.equals(this.barnNummer, other.getBarnNummer());
    }

    @Override
    public int hashCode() {
        return Objects.hash(fødselsdato, barnNummer);
    }

    @Override
    public String toString() {
        return "UidentifisertBarnEntitet{" +
            "fødselsdato=" + fødselsdato +
            ", dødsdato=" + dødsdato +
            ", barnNummer=" + barnNummer +
            '}';
    }
}
