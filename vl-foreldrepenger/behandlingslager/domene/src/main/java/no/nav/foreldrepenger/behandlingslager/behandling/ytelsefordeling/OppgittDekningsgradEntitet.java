package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

@Entity(name = "YtelseDekningsgrad")
@Table(name = "SO_DEKNINGSGRAD")
public class OppgittDekningsgradEntitet extends BaseEntitet implements OppgittDekningsgrad {

    public static final int HUNDRE_PROSENT = 100;
    public static final int ÅTTI_PROSENT = 80;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SO_DEKNINGSGRAD")
    private Long id;

    @Column(name = "dekningsgrad", nullable = false)
    @Max(value = 100)
    @Min(value = 0)
    @ChangeTracked
    private int dekningsgrad;

    OppgittDekningsgradEntitet() {
        // Hibernate
    }

    private OppgittDekningsgradEntitet(int dekningsgrad) {
        this.dekningsgrad = dekningsgrad;
    }

    public static OppgittDekningsgrad bruk80() {
        return new OppgittDekningsgradEntitet(ÅTTI_PROSENT);
    }

    public static OppgittDekningsgrad bruk100() {
        return new OppgittDekningsgradEntitet(HUNDRE_PROSENT);
    }

    @Override
    public int getDekningsgrad() {
        return dekningsgrad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OppgittDekningsgradEntitet that = (OppgittDekningsgradEntitet) o;
        return Objects.equals(dekningsgrad, that.dekningsgrad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dekningsgrad);
    }

    @Override
    public String toString() {
        return "OppgittDekningsgrad{" +
            "id=" + id +
            ", dekningsgrad=" + dekningsgrad +
            '}';
    }
}
