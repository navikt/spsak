package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.Frilansoppdrag;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;


@Table(name = "SO_OPPGITT_FRILANSOPPDRAG")
@Entity(name = "Frilansoppdrag")
public class FrilansoppdragEntitet extends BaseEntitet implements Frilansoppdrag, IndexKey {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SO_OPPGITT_FRILANSOPPDRAG")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "frilans_id", nullable = false, updatable = false)
    private FrilansEntitet frilans;

    @ChangeTracked
    private String oppdragsgiver;

    @Embedded
    @ChangeTracked
    private DatoIntervallEntitet periode;


    FrilansoppdragEntitet() {
    }

    public FrilansoppdragEntitet(String oppdragsgiver, DatoIntervallEntitet periode) {
        this.oppdragsgiver = oppdragsgiver;
        this.periode = periode;
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(periode, oppdragsgiver);
    }

    public void setOppgittOpptjening(FrilansEntitet frilans) {
        this.frilans = frilans;
    }

    void setPeriode(DatoIntervallEntitet periode) {
        this.periode = periode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FrilansoppdragEntitet that = (FrilansoppdragEntitet) o;
        return Objects.equals(frilans, that.frilans) &&
            Objects.equals(oppdragsgiver, that.oppdragsgiver) &&
            Objects.equals(periode, that.periode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(frilans, oppdragsgiver, periode);
    }

    @Override
    public String toString() {
        return "FrilansoppdragEntitet{" +
            "frilans=" + frilans +
            ", oppdragsgiver='" + oppdragsgiver + '\'' +
            ", periode=" + periode +
            '}';
    }

    @Override
    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    @Override
    public String getOppdragsgiver() {
        return oppdragsgiver;
    }

    public void setFrilans(FrilansEntitet frilans) {
        this.frilans = frilans;
    }
}
