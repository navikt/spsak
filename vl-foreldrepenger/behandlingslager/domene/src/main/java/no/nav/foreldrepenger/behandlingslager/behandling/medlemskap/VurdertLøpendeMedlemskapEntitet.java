package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
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

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

/**
 * Entitetsklasse for løpende medlemskap.
 *
 * Implementert iht. builder pattern (ref. "Effective Java, 2. ed." J.Bloch).
 * Non-public constructors og setters, dvs. immutable.
 *
 * OBS: Legger man til nye felter så skal dette oppdateres mange steder:
 * builder, equals, hashcode etc.
 */

@Entity(name = "Medlemskap")
@Table(name = "MEDLEMSKAP_VURDERING_LOPENDE")
public class VurdertLøpendeMedlemskapEntitet extends BaseEntitet implements VurdertLøpendeMedlemskap{

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MEDLEMSKAP_VL")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vurdert_periode_id", nullable = false, updatable = false)
    private VurdertMedlemskapPeriodeEntitet periodeHolder;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "oppholdsrett_vurdering")
    private Boolean oppholdsrettVurdering;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "lovlig_opphold_vurdering")
    private Boolean lovligOppholdVurdering;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "bosatt_vurdering")
    private Boolean bosattVurdering;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "er_eos_borger")
    private Boolean erEøsBorger;

    @Column(name = "vurderingsdato", nullable = false, updatable = false)
    private LocalDate vuderingsdato;

    @ManyToOne
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "manuell_vurd", referencedColumnName = "kode", nullable = false)),
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'"
                    + MedlemskapManuellVurderingType.DISCRIMINATOR + "'")) })
    private MedlemskapManuellVurderingType medlemsperiodeManuellVurdering = MedlemskapManuellVurderingType.UDEFINERT;

    VurdertLøpendeMedlemskapEntitet() {
        // hibernate
    }

    /**
     * Deep copy.
     */
    public VurdertLøpendeMedlemskapEntitet(VurdertLøpendeMedlemskap medlemskapMal) {
        deepCopyFra(medlemskapMal);
    }

    /** Skal kun aksesseres internt fra Builder.!!! */
    public void deepCopyFra(VurdertLøpendeMedlemskap medlemskap) {
        this.oppholdsrettVurdering = medlemskap.getOppholdsrettVurdering();
        this.lovligOppholdVurdering = medlemskap.getLovligOppholdVurdering();
        this.bosattVurdering = medlemskap.getBosattVurdering();
        this.setMedlemsperiodeManuellVurdering(medlemskap.getMedlemsperiodeManuellVurdering());
        this.erEøsBorger = medlemskap.getErEøsBorger();
        this.vuderingsdato = medlemskap.getVurderingsdato();
    }

    @Override
    public Boolean getOppholdsrettVurdering() {
        return oppholdsrettVurdering;
    }

    void setOppholdsrettVurdering(Boolean oppholdsrettVurdering) {
        this.oppholdsrettVurdering = oppholdsrettVurdering;
    }

    void setVuderingsdato(LocalDate vuderingsdato) {
        this.vuderingsdato = vuderingsdato;
    }

    @Override
    public Boolean getLovligOppholdVurdering() {
        return lovligOppholdVurdering;
    }

    void setLovligOppholdVurdering(Boolean lovligOppholdVurdering) {
        this.lovligOppholdVurdering = lovligOppholdVurdering;
    }

    @Override
    public LocalDate getVurderingsdato() {
        return vuderingsdato;
    }

    @Override
    public Boolean getBosattVurdering() {
        return bosattVurdering;
    }

    void setBosattVurdering(Boolean bosattVurdering) {
        this.bosattVurdering = bosattVurdering;
    }

    @Override
    public MedlemskapManuellVurderingType getMedlemsperiodeManuellVurdering() {
        return Objects.equals(medlemsperiodeManuellVurdering, MedlemskapManuellVurderingType.UDEFINERT) ? null
                : medlemsperiodeManuellVurdering;
    }

    void setMedlemsperiodeManuellVurdering(MedlemskapManuellVurderingType medlemsperiodeManuellVurdering) {
        this.medlemsperiodeManuellVurdering = medlemsperiodeManuellVurdering == null ? MedlemskapManuellVurderingType.UDEFINERT
                : medlemsperiodeManuellVurdering;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof VurdertLøpendeMedlemskapEntitet)) {
            return false;
        }
        VurdertLøpendeMedlemskapEntitet other = (VurdertLøpendeMedlemskapEntitet) obj;
        return Objects.equals(this.oppholdsrettVurdering, other.getOppholdsrettVurdering())
                && Objects.equals(this.lovligOppholdVurdering, other.getLovligOppholdVurdering())
                && Objects.equals(this.bosattVurdering, other.getBosattVurdering())
                && Objects.equals(this.erEøsBorger, other.getErEøsBorger())
                && Objects.equals(this.getMedlemsperiodeManuellVurdering(), other.getMedlemsperiodeManuellVurdering())
                && Objects.equals(this.getVurderingsdato(), other.getVurderingsdato());
    }

    @Override
    public int hashCode() {
        return Objects.hash(oppholdsrettVurdering, lovligOppholdVurdering, bosattVurdering, getMedlemsperiodeManuellVurdering(),
                erEøsBorger, getVurderingsdato());
    }

    @Override
    public Boolean getErEøsBorger() {
        return erEøsBorger;
    }


    void setErEøsBorger(Boolean erEøsBorger) {
        this.erEøsBorger = erEøsBorger;
    }

    public VurdertMedlemskapPeriodeEntitet getPeriodeHolder() {
        return periodeHolder;
    }

    public void setPeriodeHolder(VurdertMedlemskapPeriodeEntitet periodeHolder) {
        this.periodeHolder = periodeHolder;
    }
}
