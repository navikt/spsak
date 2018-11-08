package no.nav.foreldrepenger.behandlingslager.aktør;

import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;

import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

@MappedSuperclass
public abstract class Aktør extends BaseEntitet {

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "aktoer_id", unique=true, nullable = false, updatable = false)))
    private AktørId aktørId;

    @SuppressWarnings("unused")
    private Aktør() {
        // For Hibernate
    }

    public Aktør(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Aktør)
                || !(getClass().isAssignableFrom(object.getClass()) || object.getClass().isAssignableFrom(getClass()))) {
            return false;
        }
        Aktør other = (Aktør) object;
        return Objects.equals(other.aktørId, this.aktørId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId);
    }

}
