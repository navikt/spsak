package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import java.util.Objects;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

/**
 * TODO endre javadoc
 */
@Entity(name = "PersonopplysningGrunnlagEntitet")
@Table(name = "GR_PERSONOPPLYSNING")
public class PersonopplysningGrunnlagEntitet extends BaseEntitet implements PersonopplysningGrunnlag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GR_PERSONOPPLYSNING")
    private Long id;

    @ManyToOne(cascade = { /* NONE - Aldri cascade til behandling! */}, fetch = FetchType.LAZY)
    @JoinColumn(name = "behandling_id", updatable = false, nullable = false)
    private Behandling behandling;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private Boolean aktiv = true;

    @ChangeTracked
    @ManyToOne
    @JoinColumn(name = "registrert_informasjon_id", updatable = false)
    private PersonInformasjonEntitet registrertePersonopplysninger;

    @ChangeTracked
    @ManyToOne
    @JoinColumn(name = "overstyrt_informasjon_id", updatable = false)
    private PersonInformasjonEntitet overstyrtePersonopplysninger;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    PersonopplysningGrunnlagEntitet() {
    }

    PersonopplysningGrunnlagEntitet(PersonopplysningGrunnlag behandlingsgrunnlag) {
        if (behandlingsgrunnlag.getOverstyrtVersjon().isPresent()) {
            this.overstyrtePersonopplysninger = (PersonInformasjonEntitet) behandlingsgrunnlag.getOverstyrtVersjon().get();
        }
        this.registrertePersonopplysninger = (PersonInformasjonEntitet) behandlingsgrunnlag.getRegisterVersjon();
    }

    /**
     * Kun synlig for abstract test scenario
     *
     * @return id
     */
    @Override
    public Long getId() {
        return id;
    }

    public Behandling getBehandling() {
        return behandling;
    }

    void setBehandling(Behandling behandling) {
        this.behandling = behandling;
    }

    void setAktiv(final boolean aktiv) {
        this.aktiv = aktiv;
    }

    void setRegistrertePersonopplysninger(PersonInformasjonEntitet registrertePersonopplysninger) {
        this.registrertePersonopplysninger = registrertePersonopplysninger;
    }

    void setOverstyrtePersonopplysninger(PersonInformasjonEntitet overstyrtePersonopplysninger) {
        this.overstyrtePersonopplysninger = overstyrtePersonopplysninger;
    }

    @Override
    public PersonInformasjon getRegisterVersjon() {
        return registrertePersonopplysninger;
    }

    @Override
    public Optional<PersonInformasjon> getOverstyrtVersjon() {
        return Optional.ofNullable(overstyrtePersonopplysninger);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonopplysningGrunnlagEntitet that = (PersonopplysningGrunnlagEntitet) o;
        return Objects.equals(behandling, that.behandling) &&
            Objects.equals(registrertePersonopplysninger, that.registrertePersonopplysninger) &&
            Objects.equals(overstyrtePersonopplysninger, that.overstyrtePersonopplysninger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(behandling, registrertePersonopplysninger, overstyrtePersonopplysninger);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PersonopplysningGrunnlagEntitet{");
        sb.append("id=").append(id);
        sb.append(", aktiv=").append(aktiv);
        sb.append(", registrertePersonopplysninger=").append(registrertePersonopplysninger);
        sb.append(", overstyrtePersonopplysninger=").append(overstyrtePersonopplysninger);
        sb.append('}');
        return sb.toString();
    }
}
