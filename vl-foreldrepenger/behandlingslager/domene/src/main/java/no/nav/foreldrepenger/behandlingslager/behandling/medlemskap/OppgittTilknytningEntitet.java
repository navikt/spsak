package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

/**
 * Entitetsklasse for oppgitt tilknytning.
 * <p>
 * Implementert iht. builder pattern (ref. "Effective Java, 2. ed." J.Bloch).
 * Non-public constructors og setters, dvs. immutable.
 * <p>
 * OBS: Legger man til nye felter så skal dette oppdateres mange steder:
 * builder, equals, hashcode etc.
 */

@Entity(name = "OppgittTilknytning")
@Table(name = "MEDLEMSKAP_OPPG_TILKNYT")
public class OppgittTilknytningEntitet extends BaseEntitet implements OppgittTilknytning {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MEDLEMSKAP_OPPG_TILKNYT")
    private Long id;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "opphold_naa", nullable = false)
    private boolean oppholdNå;

    @Column(name = "oppgitt_dato", nullable = false)
    private LocalDate oppgittDato;


    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "oppgittTilknytning")
    private Set<OppgittLandOppholdEntitet> opphold = new HashSet<>(2);

    public OppgittTilknytningEntitet() {
        // Hibernate
    }

    /**
     * Deep copy
     */
    public OppgittTilknytningEntitet(OppgittTilknytning oppgittTilknytning) {
        this.oppholdNå = oppgittTilknytning.isOppholdNå();
        this.oppgittDato = oppgittTilknytning.getOppgittDato();
        for (OppgittLandOpphold utl : oppgittTilknytning.getOpphold()) {
            OppgittLandOppholdEntitet ue = new OppgittLandOppholdEntitet(utl);
            ue.setOppgittTilknytning(this);
            this.opphold.add(ue);
        }
    }

    @Override
    public boolean isOppholdNå() {
        return oppholdNå;
    }

    void setOppholdNå(boolean oppholdNorgeNå) {
        this.oppholdNå = oppholdNorgeNå;
    }

    @Override
    public LocalDate getOppgittDato() {
        return oppgittDato;
    }

    void setOppgittDato(LocalDate oppgittDato) {
        this.oppgittDato = oppgittDato;
    }

    @Override
    public Set<OppgittLandOpphold> getOpphold() {
        return Collections.unmodifiableSet(opphold);
    }

    @Override
    public boolean isOppholdINorgeSistePeriode() {
        return opphold.stream()
            .anyMatch(o -> o.getLand().equals(Landkoder.NOR) && o.isTidligereOpphold());
    }

    @Override
    public boolean isOppholdINorgeNestePeriode() {
        return opphold.stream()
            .anyMatch(o -> o.getLand().equals(Landkoder.NOR) && !o.isTidligereOpphold());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof OppgittTilknytningEntitet)) {
            return false;
        }
        OppgittTilknytningEntitet other = (OppgittTilknytningEntitet) obj;
        return Objects.equals(this.oppholdNå, other.oppholdNå)
            && Objects.equals(this.opphold, other.opphold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oppholdNå, opphold);
    }

    public static class Builder {
        private OppgittTilknytningEntitet mal;

        public Builder() {
            mal = new OppgittTilknytningEntitet();
        }

        public Builder(OppgittTilknytning oppgittTilknytning) {
            if (oppgittTilknytning != null) {
                mal = new OppgittTilknytningEntitet(oppgittTilknytning);
            } else {
                mal = new OppgittTilknytningEntitet();
            }
        }

        public Builder medOppholdNå(boolean oppholdNorgeNå) {
            mal.setOppholdNå(oppholdNorgeNå);
            return this;
        }

        public Builder medOppgittDato(LocalDate oppgittDato) {
            mal.setOppgittDato(oppgittDato);
            return this;
        }

        public Builder leggTilOpphold(OppgittLandOpphold oppholdUtland) {
            OppgittLandOppholdEntitet ue = new OppgittLandOppholdEntitet(oppholdUtland);
            ue.setOppgittTilknytning(mal);
            mal.opphold.add(ue);
            return this;
        }

        public Builder medOpphold(List<OppgittLandOpphold> opphold) {
            Set<OppgittLandOppholdEntitet> oppholdEntiteter = opphold.stream().map(o -> new OppgittLandOppholdEntitet(o)).collect(Collectors.toCollection(LinkedHashSet::new));
            mal.opphold = oppholdEntiteter;
            return this;
        }

        public OppgittTilknytning build() {
            return mal;
        }
    }
}
