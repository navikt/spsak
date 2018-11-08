package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "SoeknadPerioder")
@Table(name = "YF_FORDELING")
public class OppgittFordelingEntitet extends BaseEntitet implements OppgittFordeling {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YF_FORDELING")
    private Long id;

    @OneToMany(mappedBy = "oppgittFordeling")
    @ChangeTracked
    private List<OppgittPeriodeEntitet> søknadsPerioder;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "annenForelderErInformert", nullable = false)
    @ChangeTracked
    private boolean erAnnenForelderInformert;

    OppgittFordelingEntitet() {
    }

    public OppgittFordelingEntitet(List<OppgittPeriode> søknadsPerioder, boolean erAnnenForelderInformert) {
        this.søknadsPerioder = new ArrayList<>();
        for (OppgittPeriode oppgittPeriode : søknadsPerioder) {
            ((OppgittPeriodeEntitet) oppgittPeriode).setOppgittFordeling(this);
            this.søknadsPerioder.add((OppgittPeriodeEntitet) oppgittPeriode);
        }
        this.erAnnenForelderInformert = erAnnenForelderInformert;
    }

    @Override
    public List<OppgittPeriode> getOppgittePerioder() {
        return new ArrayList<>(søknadsPerioder);
    }

    @Override
    public boolean getErAnnenForelderInformert() {
        return erAnnenForelderInformert;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OppgittFordelingEntitet)) {
            return false;
        }
        OppgittFordelingEntitet that = (OppgittFordelingEntitet) o;
        return Objects.equals(søknadsPerioder, that.søknadsPerioder) &&
            Objects.equals(erAnnenForelderInformert, that.erAnnenForelderInformert);
    }

    @Override
    public int hashCode() {
        return Objects.hash(søknadsPerioder, erAnnenForelderInformert);
    }
}
