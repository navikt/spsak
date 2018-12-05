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

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.AnnenAktivitet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;


@Table(name = "IAY_ANNEN_AKTIVITET")
@Entity(name = "AnnenAktivitet")
public class AnnenAktivitetEntitet extends BaseEntitet implements AnnenAktivitet, IndexKey {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_ANNEN_AKTIVITET")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "oppgitt_opptjening_id", nullable = false, updatable = false)
    private OppgittOpptjeningEntitet oppgittOpptjening;

    @Embedded
    @ChangeTracked
    DatoIntervallEntitet periode;

    @ManyToOne
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "arbeid_type", referencedColumnName = "kode", nullable = false)),
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + ArbeidType.DISCRIMINATOR + "'"))})
    @ChangeTracked
    private ArbeidType arbeidType;

    public AnnenAktivitetEntitet(DatoIntervallEntitet periode, ArbeidType arbeidType) {
        this.periode = periode;
        this.arbeidType = arbeidType;
    }

    public AnnenAktivitetEntitet() {
        // hibernate
    }
    
    @Override
    public String getIndexKey() {
        return IndexKey.createKey(periode, arbeidType);
    }

    @Override
    public ArbeidType getArbeidType() {
        return arbeidType;
    }

    @Override
    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    public void setOppgittOpptjening(OppgittOpptjeningEntitet oppgittOpptjening) {
        this.oppgittOpptjening = oppgittOpptjening;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnenAktivitetEntitet that = (AnnenAktivitetEntitet) o;
        return Objects.equals(periode, that.periode) &&
                Objects.equals(arbeidType, that.arbeidType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, arbeidType);
    }

    @Override
    public String toString() {
        return "AnnenAktivitetEntitet{" +
                "id=" + id +
                ", periode=" + periode +
                ", arbeidType=" + arbeidType +
                '}';
    }
}
