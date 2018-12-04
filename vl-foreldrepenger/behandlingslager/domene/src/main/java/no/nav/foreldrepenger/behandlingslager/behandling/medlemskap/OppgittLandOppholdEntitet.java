package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.time.LocalDate;
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

import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

/**
 * Entitetsklasse for opphold.
 *
 * Implementert iht. builder pattern (ref. "Effective Java, 2. ed." J.Bloch).
 * Non-public constructors og setters, dvs. immutable.
 *
 * OBS: Legger man til nye felter så skal dette oppdateres mange steder:
 * builder, equals, hashcode etc.
 */
@Entity(name = "OppgittLandOpphold")
@Table(name = "MEDLEMSKAP_OPPG_LAND")
public class OppgittLandOppholdEntitet extends BaseEntitet implements OppgittLandOpphold, IndexKey {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MEDLEMSKAP_OPPG_LAND")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "land", referencedColumnName = "kode", nullable = false)),
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + Landkoder.DISCRIMINATOR + "'")) })
    private Landkoder land = Landkoder.UDEFINERT;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fomDato", column = @Column(name = "periode_fom")),
        @AttributeOverride(name = "tomDato", column = @Column(name = "periode_tom"))
    })
    private DatoIntervallEntitet periode;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "tidligere_opphold", nullable = false)
    private boolean tidligereOpphold;

    @ManyToOne(optional = false)
    @JoinColumn(name = "medlemskap_oppg_tilknyt_id", nullable = false, updatable = false)
    private OppgittTilknytningEntitet oppgittTilknytning;

    OppgittLandOppholdEntitet() {
        // Hibernate
    }

    OppgittLandOppholdEntitet(OppgittLandOpphold utlandsopphold) {
        this.setLand(utlandsopphold.getLand());
        this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(
            utlandsopphold.getPeriodeFom(),
            utlandsopphold.getPeriodeTom()
        );
        this.tidligereOpphold = utlandsopphold.isTidligereOpphold();

        // kopier ikke oppgitt tilknytning. Det settes p.t. separat i builder (setOppgittTilknytning) for å knytte til OppgittTilknytningEntitet
    }
    
    @Override
    public String getIndexKey() {
        return IndexKey.createKey(this.land, periode);
    }

    @Override
    public Landkoder getLand() {
        return Objects.equals(Landkoder.UDEFINERT, land) ? null : land;
    }

    @Override
    public LocalDate getPeriodeFom() {
        return periode != null ? periode.getFomDato() : null;
    }

    @Override
    public LocalDate getPeriodeTom() {
        return periode != null ? periode.getTomDato() : null;
    }

    @Override
    public boolean isTidligereOpphold() {
        return tidligereOpphold;
    }

    void setLand(Landkoder land) {
        this.land = land == null ? Landkoder.UDEFINERT : land;
    }

    void setPeriode(LocalDate periodeFom, LocalDate periodeTom) {
        this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(periodeFom, periodeTom);
    }

    void setTidligereOpphold(boolean tidligereOpphold) {
        this.tidligereOpphold = tidligereOpphold;
    }

    void setOppgittTilknytning(OppgittTilknytningEntitet oppgittTilknytning) {
        this.oppgittTilknytning = oppgittTilknytning;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof OppgittLandOppholdEntitet)) {
            return false;
        }
        OppgittLandOppholdEntitet other = (OppgittLandOppholdEntitet) obj;
        return Objects.equals(this.getLand(), other.getLand())
            && Objects.equals(this.periode, other.periode)
            && Objects.equals(this.tidligereOpphold, other.isTidligereOpphold());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLand(), periode, tidligereOpphold);
    }

    public static class Builder {
        private OppgittLandOppholdEntitet oppholdMal;

        public Builder() {
            oppholdMal = new OppgittLandOppholdEntitet();
        }

        public Builder(OppgittLandOppholdEntitet utlandsopphold) {
            if (utlandsopphold != null) {
                oppholdMal = new OppgittLandOppholdEntitet(utlandsopphold);
            } else {
                oppholdMal = new OppgittLandOppholdEntitet();
            }
        }

        public Builder medLand(Landkoder land) {
            oppholdMal.setLand(land);
            return this;
        }

        public Builder medPeriode(LocalDate periodeStartdato, LocalDate periodeSluttdato) {
            oppholdMal.periode = DatoIntervallEntitet.fraOgMedTilOgMed(periodeStartdato, periodeSluttdato);
            return this;
        }

        public Builder erTidligereOpphold(boolean tidligereOpphold) {
            oppholdMal.tidligereOpphold = tidligereOpphold;
            return this;
        }

        public OppgittLandOpphold build() {
            return oppholdMal;
        }
    }
}
