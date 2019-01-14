package no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening;

import java.time.LocalDate;
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
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ReferanseType;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "OpptjeningAktivitet")
@Table(name = "OP_OPPTJENING_AKTIVITET")
public class OpptjeningAktivitet extends BaseEntitet implements IndexKey {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OPPTJENING_AKTIVITET")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @ChangeTracked
    @Embedded
    private DatoIntervallEntitet periode;

    @ChangeTracked
    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "aktivitet_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + OpptjeningAktivitetType.DISCRIMINATOR
            + "'"))
    private OpptjeningAktivitetType aktivitetType;

    @ChangeTracked
    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "referanse_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + ReferanseType.DISCRIMINATOR
            + "'"))
    private ReferanseType aktivitetReferanseType = ReferanseType.UDEFINERT;

    /** Custom aktivitet referanse. Form og innhold avhenger av #aktivitetType . */
    @ChangeTracked
    @Column(name = "aktivitet_referanse", nullable = true)
    private String aktivitetReferanse;

    @ChangeTracked
    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "klassifisering", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'"
            + OpptjeningAktivitetKlassifisering.DISCRIMINATOR + "'"))
    private OpptjeningAktivitetKlassifisering klassifisering;

    OpptjeningAktivitet() {
        // fur hibernate
    }

    public OpptjeningAktivitet(LocalDate fom, LocalDate tom, OpptjeningAktivitetType aktivitetType,
            OpptjeningAktivitetKlassifisering klassifisering) {
        this(fom, tom, aktivitetType, klassifisering, null, null);
    }

    public OpptjeningAktivitet(LocalDate fom, LocalDate tom, OpptjeningAktivitetType aktivitetType,
            OpptjeningAktivitetKlassifisering klassifisering, String aktivitetReferanse, ReferanseType aktivitetReferanseType) {
        Objects.requireNonNull(fom, "fom"); //$NON-NLS-1$
        Objects.requireNonNull(tom, "tom"); //$NON-NLS-1$
        Objects.requireNonNull(aktivitetType, "aktivitetType"); //$NON-NLS-1$
        Objects.requireNonNull(klassifisering, "klassifisering"); //$NON-NLS-1$
        this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);

        this.aktivitetType = aktivitetType;
        this.klassifisering = klassifisering;

        if (aktivitetReferanse != null) {
            Objects.requireNonNull(aktivitetReferanseType, "aktivitetReferanseType");
            this.aktivitetReferanse = aktivitetReferanse;
            this.aktivitetReferanseType = aktivitetReferanseType;
        }
    }

    /** copy constructor - kun data uten metadata som aktiv/endretAv etc. */
    public OpptjeningAktivitet(OpptjeningAktivitet annen) {

        this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(annen.getFom(), annen.getTom());
        this.aktivitetReferanse = annen.getAktivitetReferanse();
        this.aktivitetType = annen.getAktivitetType();
        this.klassifisering = annen.getKlassifisering();
        this.aktivitetReferanseType = annen.getAktivitetReferanseType() == null ? ReferanseType.UDEFINERT
                : annen.getAktivitetReferanseType();

    }

    public LocalDate getFom() {
        return periode.getFomDato();
    }

    public LocalDate getTom() {
        return periode.getTomDato();
    }

    public String getAktivitetReferanse() {
        return aktivitetReferanse;
    }

    public ReferanseType getAktivitetReferanseType() {
        return ReferanseType.UDEFINERT.equals(aktivitetReferanseType) ? null : aktivitetReferanseType;
    }

    public OpptjeningAktivitetType getAktivitetType() {
        return aktivitetType;
    }

    public OpptjeningAktivitetKlassifisering getKlassifisering() {
        return klassifisering;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !(obj.getClass().equals(this.getClass()))) {
            return false;
        }

        OpptjeningAktivitet other = (OpptjeningAktivitet) obj;
        return Objects.equals(periode, other.periode)
                && Objects.equals(aktivitetType, other.aktivitetType)
                && Objects.equals(aktivitetReferanse, other.aktivitetReferanse)
                && Objects.equals(aktivitetReferanseType, other.aktivitetReferanseType)
        // tar ikke med klassifisering, da det ikke er del av dette objektets identitet
        ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, aktivitetType, aktivitetReferanse, aktivitetReferanseType);
    }
    
    @Override
    public String getIndexKey() {
        return IndexKey.createKey(periode, aktivitetType, aktivitetReferanse, aktivitetReferanseType);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "<aktivitetType=" + aktivitetType //$NON-NLS-1$
                + (aktivitetReferanse == null ? "" : ", aktivitetReferanse[" + aktivitetReferanseType + "]=" + aktivitetReferanse) //$NON-NLS-1$ //$NON-NLS-2$
                + ", klassifisering=" + klassifisering
            + " [" + periode.getFomDato() + ", " + periode.getTomDato() + "]" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + ">"; //$NON-NLS-1$
    }

}
