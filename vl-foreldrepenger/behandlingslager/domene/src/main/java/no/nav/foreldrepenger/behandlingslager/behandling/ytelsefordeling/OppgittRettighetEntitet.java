package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "OppgittRettighet")
@Table(name = "SO_RETTIGHET")
public class OppgittRettighetEntitet extends BaseEntitet implements OppgittRettighet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SO_RETTIGHET")
    private Long id;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "annen_foreldre_rett")
    @ChangeTracked
    private Boolean harAnnenForeldreRett;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "omsorg_i_hele_perioden")
    @ChangeTracked
    private Boolean harOmsorgForBarnetIHelePerioden;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aleneomsorg")
    @ChangeTracked
    private Boolean harAleneomsorgForBarnet;

    OppgittRettighetEntitet() {
    }

    public OppgittRettighetEntitet(Boolean harAnnenForeldreRett, Boolean harOmsorgForBarnetIHelePerioden, Boolean harAleneomsorgForBarnet) {
        this.harAnnenForeldreRett = harAnnenForeldreRett;
        this.harOmsorgForBarnetIHelePerioden = harOmsorgForBarnetIHelePerioden;
        this.harAleneomsorgForBarnet = harAleneomsorgForBarnet;
    }

    @Override
    public Boolean getHarAleneomsorgForBarnet() {
        return harAleneomsorgForBarnet;
    }

    @Override
    public Boolean getHarAnnenForeldreRett() {
        return harAnnenForeldreRett;
    }

    @Override
    public Boolean getHarOmsorgForBarnetIHelePerioden() {
        return harOmsorgForBarnetIHelePerioden;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OppgittRettighetEntitet that = (OppgittRettighetEntitet) o;
        return Objects.equals(harAnnenForeldreRett, that.harAnnenForeldreRett) &&
            Objects.equals(harOmsorgForBarnetIHelePerioden, that.harOmsorgForBarnetIHelePerioden) &&
            Objects.equals(harAleneomsorgForBarnet, that.harAleneomsorgForBarnet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(harAnnenForeldreRett, harOmsorgForBarnetIHelePerioden, harAleneomsorgForBarnet);
    }

    @Override
    public String toString() {
        return "OppgittRettighetEntitet{" +
            "id=" + id +
            ", harAnnenForeldreRett=" + harAnnenForeldreRett +
            ", harOmsorgForBarnetIHelePerioden=" + harOmsorgForBarnetIHelePerioden +
            ", harAleneomsorgForBarnet=" + harAleneomsorgForBarnet +
            '}';
    }
}
