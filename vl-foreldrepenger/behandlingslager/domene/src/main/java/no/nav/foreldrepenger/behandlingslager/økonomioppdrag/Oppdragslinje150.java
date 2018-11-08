
package no.nav.foreldrepenger.behandlingslager.økonomioppdrag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

/**
 * Denne klassen er en ren avbildning fra Oppdragsløsningens meldingsformater.
 * Navngivning følger ikke nødvendigvis Vedtaksløsningens navnestandarder.
 */
@Entity(name = "Oppdragslinje150")
@Table(name = "OKO_OPPDRAG_LINJE_150")
public class Oppdragslinje150 extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OKO_OPPDRAG_LINJE_150")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Column(name = "kode_endring_linje", nullable = false)
    private String kodeEndringLinje;

    @Column(name = "kode_status_linje")
    private String kodeStatusLinje;

    @Column(name = "dato_status_fom")
    private LocalDate datoStatusFom;

    @Column(name = "vedtak_id")
    private String vedtakId;

    @Column(name = "delytelse_id")
    private Long delytelseId;

    @Column(name = "kode_klassifik", nullable = false)
    private String kodeKlassifik;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fomDato", column = @Column(name = "dato_vedtak_fom")),
        @AttributeOverride(name = "tomDato", column = @Column(name = "dato_vedtak_tom"))
    })
    private DatoIntervallEntitet vedtakPeriode;

    @Column(name = "sats", nullable = false)
    private long sats;

    @Column(name = "fradrag_tillegg", nullable = false)
    private String fradragTillegg;

    @Column(name = "type_sats", nullable = false)
    private String typeSats;

    @Column(name = "bruk_kjore_plan", nullable = false)
    private String brukKjoreplan;

    @Column(name = "saksbeh_id", nullable = false)
    private String saksbehId;

    @Column(name = "utbetales_til_id")
    private String utbetalesTilId;

    @Column(name = "henvisning", nullable = false)
    private long henvisning;

    @Column(name = "ref_fagsystem_id")
    private Long refFagsystemId;

    @Column(name = "ref_delytelse_id")
    private Long refDelytelseId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "oppdrag110_id", nullable = false, updatable = false)
    private Oppdrag110 oppdrag110;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "oppdragslinje150", cascade = CascadeType.PERSIST)
    private Refusjonsinfo156 refusjonsinfo156;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "oppdragslinje150", cascade = CascadeType.PERSIST)
    private List<Grad170> grad170Liste = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "oppdragslinje150", cascade = CascadeType.PERSIST)
    private List<Attestant180> attestant180Liste = new ArrayList<>();

    public Oppdragslinje150() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKodeEndringLinje() {
        return kodeEndringLinje;
    }

    public String getKodeStatusLinje() {
        return kodeStatusLinje;
    }

    public boolean gjelderOpphør() {
        return getKodeStatusLinje() != null;
    }

    public LocalDate getDatoStatusFom() {
        return datoStatusFom;
    }

    public String getVedtakId() {
        return vedtakId;
    }

    public Long getDelytelseId() {
        return delytelseId;
    }

    public String getKodeKlassifik() {
        return kodeKlassifik;
    }

    public LocalDate getDatoVedtakFom() {
        return vedtakPeriode.getFomDato();
    }

    public LocalDate getDatoVedtakTom() {
        return vedtakPeriode.getTomDato();
    }

    public long getSats() {
        return sats;
    }

    public void setSats(long sats) {
        this.sats = sats;
    }

    public String getFradragTillegg() {
        return fradragTillegg;
    }

    public String getTypeSats() {
        return typeSats;
    }

    public String getBrukKjoreplan() {
        return brukKjoreplan;
    }

    public String getSaksbehId() {
        return saksbehId;
    }

    public String getUtbetalesTilId() {
        return utbetalesTilId;
    }

    public Long getHenvisning() {
        return henvisning;
    }

    public void setHenvisning(Long henvisning) {
        this.henvisning = henvisning;
    }

    public Long getRefFagsystemId() {
        return refFagsystemId;
    }

    public Long getRefDelytelseId() {
        return refDelytelseId;
    }

    public Oppdrag110 getOppdrag110() {
        return oppdrag110;
    }

    public void setOppdrag110(Oppdrag110 oppdrag110) {
        this.oppdrag110 = oppdrag110;
    }

    public Refusjonsinfo156 getRefusjonsinfo156() {
        return refusjonsinfo156;
    }

    protected void setRefusjonsinfo156(Refusjonsinfo156 refusjonsinfo156) {
        Objects.requireNonNull(refusjonsinfo156, "refusjonsinfo156");
        this.refusjonsinfo156 = refusjonsinfo156;
    }

    public List<Grad170> getGrad170Liste() {
        return grad170Liste;
    }

    protected void addGrad170(Grad170 grad170) {
        Objects.requireNonNull(grad170, "grad170");
        grad170Liste.add(grad170);
    }

    public List<Attestant180> getAttestant180Liste() {
        return attestant180Liste;
    }

    protected void addAttestant180(Attestant180 attestant180) {
        Objects.requireNonNull(attestant180, "attestant180");
        attestant180Liste.add(attestant180);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Oppdragslinje150)) {
            return false;
        }
        Oppdragslinje150 oppdrlinje150 = (Oppdragslinje150) object;
        return Objects.equals(kodeEndringLinje, oppdrlinje150.getKodeEndringLinje())
            && Objects.equals(kodeStatusLinje, oppdrlinje150.getKodeStatusLinje())
            && Objects.equals(datoStatusFom, oppdrlinje150.getDatoStatusFom())
            && Objects.equals(vedtakId, oppdrlinje150.getVedtakId())
            && Objects.equals(delytelseId, oppdrlinje150.getDelytelseId())
            && Objects.equals(kodeKlassifik, oppdrlinje150.getKodeKlassifik())
            && Objects.equals(vedtakPeriode, oppdrlinje150.vedtakPeriode)
            && Objects.equals(sats, oppdrlinje150.getSats())
            && Objects.equals(fradragTillegg, oppdrlinje150.getFradragTillegg())
            && Objects.equals(typeSats, oppdrlinje150.getTypeSats())
            && Objects.equals(brukKjoreplan, oppdrlinje150.getBrukKjoreplan())
            && Objects.equals(saksbehId, oppdrlinje150.getSaksbehId())
            && Objects.equals(utbetalesTilId, oppdrlinje150.getUtbetalesTilId())
            && Objects.equals(refFagsystemId, oppdrlinje150.getRefFagsystemId())
            && Objects.equals(refDelytelseId, oppdrlinje150.getRefDelytelseId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(kodeEndringLinje, kodeStatusLinje, datoStatusFom, vedtakId, delytelseId, kodeKlassifik, vedtakPeriode, sats, fradragTillegg, typeSats, brukKjoreplan, saksbehId, utbetalesTilId, refFagsystemId, refDelytelseId);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String kodeEndringLinje;
        private String kodeStatusLinje;
        private LocalDate datoStatusFom;
        private String vedtakId;
        private Long delytelseId;
        private String kodeKlassifik;
        private DatoIntervallEntitet vedtakPeriode;
        private Long sats;
        private String fradragTillegg;
        private String typeSats;
        private String brukKjoreplan;
        private String saksbehId;
        private String utbetalesTilId;
        private Long henvisning;
        private Long refFagsystemId;
        private Long refDelytelseId;
        private Oppdrag110 oppdrag110;

        public Builder medKodeEndringLinje(String kodeEndringLinje) {
            this.kodeEndringLinje = kodeEndringLinje;
            return this;
        }

        public Builder medKodeStatusLinje(String kodeStatusLinje) {
            this.kodeStatusLinje = kodeStatusLinje;
            return this;
        }

        public Builder medDatoStatusFom(LocalDate datoStatusFom) {
            this.datoStatusFom = datoStatusFom;
            return this;
        }

        public Builder medVedtakId(String vedtakId) {
            this.vedtakId = vedtakId;
            return this;
        }

        public Builder medDelytelseId(Long delytelseId) {
            this.delytelseId = delytelseId;
            return this;
        }

        public Builder medKodeKlassifik(String kodeKlassifik) {
            this.kodeKlassifik = kodeKlassifik;
            return this;
        }

        public Builder medVedtakFomOgTom(LocalDate datoVedtakFom, LocalDate datoVedtakTom) {
            this.vedtakPeriode = DatoIntervallEntitet.fraOgMedTilOgMed(datoVedtakFom, datoVedtakTom);
            return this;
        }

        public Builder medSats(long sats) {
            this.sats = sats;
            return this;
        }

        public Builder medFradragTillegg(String fradragTillegg) {
            this.fradragTillegg = fradragTillegg;
            return this;
        }

        public Builder medTypeSats(String typeSats) {
            this.typeSats = typeSats;
            return this;
        }

        public Builder medBrukKjoreplan(String brukKjoreplan) {
            this.brukKjoreplan = brukKjoreplan;
            return this;
        }

        public Builder medSaksbehId(String saksbehId) {
            this.saksbehId = saksbehId;
            return this;
        }

        public Builder medUtbetalesTilId(String utbetalesTilId) {
            this.utbetalesTilId = utbetalesTilId;
            return this;
        }

        public Builder medHenvisning(Long henvisning) {
            this.henvisning = henvisning;
            return this;
        }

        public Builder medRefFagsystemId(Long refFagsystemId) {
            this.refFagsystemId = refFagsystemId;
            return this;
        }

        public Builder medRefDelytelseId(Long refDelytelseId) {
            this.refDelytelseId = refDelytelseId;
            return this;
        }

        public Builder medOppdrag110(Oppdrag110 oppdrag110) {
            this.oppdrag110 = oppdrag110;
            return this;
        }

        public Oppdragslinje150 build() {
            verifyStateForBuild();
            Oppdragslinje150 oppdragslinje150 = new Oppdragslinje150();
            oppdragslinje150.kodeEndringLinje = kodeEndringLinje;
            oppdragslinje150.kodeStatusLinje = kodeStatusLinje;
            oppdragslinje150.datoStatusFom = datoStatusFom;
            oppdragslinje150.vedtakId = vedtakId;
            oppdragslinje150.delytelseId = delytelseId;
            oppdragslinje150.kodeKlassifik = kodeKlassifik;
            oppdragslinje150.vedtakPeriode = vedtakPeriode;
            oppdragslinje150.sats = sats;
            oppdragslinje150.fradragTillegg = fradragTillegg;
            oppdragslinje150.typeSats = typeSats;
            oppdragslinje150.brukKjoreplan = brukKjoreplan;
            oppdragslinje150.saksbehId = saksbehId;
            oppdragslinje150.utbetalesTilId = utbetalesTilId;
            oppdragslinje150.henvisning = henvisning;
            oppdragslinje150.refFagsystemId = refFagsystemId;
            oppdragslinje150.refDelytelseId = refDelytelseId;
            oppdragslinje150.oppdrag110 = oppdrag110;
            oppdrag110.addOppdragslinje150(oppdragslinje150);

            return oppdragslinje150;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(kodeEndringLinje, "kodeEndringLinje");
            Objects.requireNonNull(kodeKlassifik, "kodeKlassifik");
            Objects.requireNonNull(vedtakPeriode, "vedtakPeriode");
            Objects.requireNonNull(sats, "sats");
            Objects.requireNonNull(fradragTillegg, "fradragTillegg");
            Objects.requireNonNull(typeSats, "typeSats");
            Objects.requireNonNull(brukKjoreplan, "brukKjoreplan");
            Objects.requireNonNull(saksbehId, "saksbehId");
            Objects.requireNonNull(henvisning, "henvisning");
            Objects.requireNonNull(oppdrag110, "oppdrag110");
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
            (id != null ? "id=" + id + ", " : "") //$NON-NLS-1$ //$NON-NLS-2$
            + "kodeEndringLinje=" + kodeEndringLinje + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "kodeStatusLinje=" + kodeStatusLinje + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "datoStatusFom=" + datoStatusFom + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "vedtakId=" + vedtakId + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "delytelseId=" + delytelseId + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "kodeKlassifik=" + kodeKlassifik + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "vedtakPeriode=" + vedtakPeriode + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "sats=" + sats + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "fradragTillegg=" + fradragTillegg + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "typeSats=" + typeSats + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "brukKjoreplan=" + brukKjoreplan + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "saksbehId=" + saksbehId + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "utbetalesTilId=" + utbetalesTilId + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "refFagsystemId=" + refFagsystemId + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "refDelytelseId=" + refDelytelseId + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "opprettetTs=" + getOpprettetTidspunkt() //$NON-NLS-1$
            + ">"; //$NON-NLS-1$
    }
}
