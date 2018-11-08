package no.nav.foreldrepenger.behandlingslager.økonomioppdrag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * Denne klassen er en ren avbildning fra Oppdragsløsningens meldingsformater.
 * Navngivning følger ikke nødvendigvis Vedtaksløsningens navnestandarder.
 */
@Entity(name = "Oppdrag110")
@Table(name = "OKO_OPPDRAG_110")
public class Oppdrag110 extends BaseEntitet{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OKO_OPPDRAG_110")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Column(name = "kode_aksjon", nullable = false)
    private String kodeAksjon;

    @Column(name = "kode_endring", nullable = false)
    private String kodeEndring;

    @Column(name = "kode_fagomrade", nullable = false)
    private String kodeFagomrade;

    @Column(name = "fagsystem_id", nullable = false)
    private long fagsystemId;

    @Column(name = "utbet_frekvens", nullable = false)
    private String utbetFrekvens;

    @Column(name = "oppdrag_gjelder_id", nullable = false)
    private String oppdragGjelderId;

    @Column(name = "dato_Oppdrag_Gjelder_Fom", nullable = false)
    private LocalDate datoOppdragGjelderFom;

    @Column(name = "saksbeh_id", nullable = false)
    private String saksbehId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "oppdrags_kontroll_id", nullable = false, updatable=false)
    private Oppdragskontroll oppdragskontroll;

    /* bruker @ManyToOne siden JPA ikke støtter OneToOne join på non-PK column. */
    @ManyToOne(cascade = { CascadeType.PERSIST }, optional=false)
    @JoinColumn(name = "avstemming115_id", nullable = false)
    private Avstemming115 avstemming115;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "oppdrag110", cascade = CascadeType.PERSIST)
    private List<Oppdragslinje150> oppdragslinje150Liste = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "oppdrag110", cascade = CascadeType.PERSIST)
    private List<Oppdragsenhet120> oppdragsenhet120Liste = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "oppdrag110", cascade = CascadeType.PERSIST)
    private List<OppdragKvittering> oppdragKvitteringListe = new ArrayList<>();

    public Oppdrag110() { }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKodeAksjon() {
        return kodeAksjon;
    }

    public void setKodeAksjon(String kodeAksjon) {
        this.kodeAksjon = kodeAksjon;
    }

    public String getKodeEndring() {
        return kodeEndring;
    }

    public void setKodeEndring(String kodeEndring) {
        this.kodeEndring = kodeEndring;
    }

    public String getKodeFagomrade() {
        return kodeFagomrade;
    }

    public void setKodeFagomrade(String kodeFagomrade) {
        this.kodeFagomrade = kodeFagomrade;
    }

    public long getFagsystemId() {
        return fagsystemId;
    }

    public void setFagsystemId(long fagsystemId) {
        this.fagsystemId = fagsystemId;
    }

    public String getUtbetFrekvens() {
        return utbetFrekvens;
    }

    public void setUtbetFrekvens(String utbetFrekvens) {
        this.utbetFrekvens = utbetFrekvens;
    }

    public String getOppdragGjelderId() {
        return oppdragGjelderId;
    }

    public void setOppdragGjelderId(String oppdragGjelderId) {
        this.oppdragGjelderId = oppdragGjelderId;
    }

    public LocalDate getDatoOppdragGjelderFom() { return datoOppdragGjelderFom; }

    public void setDatoOppdragGjelderFom(LocalDate datoOppdragGjelderFom) { this.datoOppdragGjelderFom = datoOppdragGjelderFom; }

    public String getSaksbehId() {
        return saksbehId;
    }

    public void setSaksbehId(String saksbehId) {
        this.saksbehId = saksbehId;
    }

    public Oppdragskontroll getOppdragskontroll() { return oppdragskontroll; }

    public void setOppdragskontroll(Oppdragskontroll oppdragskontroll) { this.oppdragskontroll = oppdragskontroll; }

    public List<Oppdragslinje150> getOppdragslinje150Liste(){ return oppdragslinje150Liste; }

    protected void addOppdragslinje150(Oppdragslinje150 oppdragslinje150) {
        Objects.requireNonNull(oppdragslinje150, "oppdragslinje150");
        oppdragslinje150Liste.add(oppdragslinje150);
    }

    public List<Oppdragsenhet120> getOppdragsenhet120Liste() {
        return oppdragsenhet120Liste;
    }

    protected void addOppdragsenhet120(Oppdragsenhet120 oppdragsenhet120){
        Objects.requireNonNull(oppdragsenhet120, "oppdragsenhet120");
        oppdragsenhet120Liste.add(oppdragsenhet120);
    }

    public List<OppdragKvittering> getOppdragKvitteringListe(){ return oppdragKvitteringListe; }

    protected void addOppdragKvittering(OppdragKvittering oppdragKvittering) {
        Objects.requireNonNull(oppdragKvittering, "oppdragKvittering");
        oppdragKvitteringListe.add(oppdragKvittering);
    }

    public Avstemming115 getAvstemming115() {
        return avstemming115;
    }

    public void setAvstemming115(Avstemming115 avstemming115) {
        this.avstemming115 = avstemming115;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Oppdrag110)) {
            return false;
        }
        Oppdrag110 oppdr110 = (Oppdrag110) object;
        return Objects.equals(kodeAksjon, oppdr110.getKodeAksjon())
                && Objects.equals(kodeEndring, oppdr110.getKodeEndring())
                && Objects.equals(kodeFagomrade, oppdr110.getKodeFagomrade())
                && Objects.equals(fagsystemId, oppdr110.getFagsystemId())
                && Objects.equals(utbetFrekvens, oppdr110.getUtbetFrekvens())
                && Objects.equals(oppdragGjelderId, oppdr110.getOppdragGjelderId())
                && Objects.equals(datoOppdragGjelderFom, oppdr110.getDatoOppdragGjelderFom())
                && Objects.equals(saksbehId, oppdr110.getSaksbehId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(kodeAksjon, kodeEndring, kodeFagomrade, fagsystemId, utbetFrekvens, oppdragGjelderId,
                datoOppdragGjelderFom, saksbehId);
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        private String kodeAksjon;
        private String kodeEndring;
        private String kodeFagomrade;
        private Long fagsystemId;
        private String utbetFrekvens;
        private String oppdragGjelderId;
        private LocalDate datoOppdragGjelderFom;
        private String saksbehId;
        private Oppdragskontroll oppdragskontroll;
        private Avstemming115 avstemming115;

        public Builder medKodeAksjon(String kodeAksjon) { this.kodeAksjon = kodeAksjon; return this; }

        public Builder medKodeEndring(String kodeEndring) { this.kodeEndring = kodeEndring; return this; }

        public Builder medKodeFagomrade(String kodeFagomrade) { this.kodeFagomrade = kodeFagomrade; return this; }

        public Builder medFagSystemId(long fagsystemId) { this.fagsystemId = fagsystemId; return this; }

        public Builder medUtbetFrekvens(String utbetFrekvens) { this.utbetFrekvens = utbetFrekvens; return this; }

        public Builder medOppdragGjelderId(String oppdragGjelderId) { this.oppdragGjelderId = oppdragGjelderId; return this; }

        public Builder medDatoOppdragGjelderFom(LocalDate datoOppdrGjelderFom){ this.datoOppdragGjelderFom = datoOppdrGjelderFom; return this; }

        public Builder medSaksbehId(String saksbehId) { this.saksbehId = saksbehId; return this; }

        public Builder medOppdragskontroll(Oppdragskontroll oppdragskontroll) { this.oppdragskontroll = oppdragskontroll; return this; }

        public Builder medAvstemming115(Avstemming115 avstemming115) { this.avstemming115 = avstemming115; return this; }

        public Oppdrag110 build() {
            verifyStateForBuild();
            Oppdrag110 oppdrag110 = new Oppdrag110();
            oppdrag110.kodeAksjon = kodeAksjon;
            oppdrag110.kodeEndring = kodeEndring;
            oppdrag110.kodeFagomrade = kodeFagomrade;
            oppdrag110.fagsystemId = fagsystemId;
            oppdrag110.utbetFrekvens = utbetFrekvens;
            oppdrag110.oppdragGjelderId = oppdragGjelderId;
            oppdrag110.datoOppdragGjelderFom = datoOppdragGjelderFom;
            oppdrag110.saksbehId = saksbehId;
            oppdrag110.oppdragskontroll = oppdragskontroll;
            oppdrag110.avstemming115 = avstemming115;
            oppdragskontroll.addOppdrag110(oppdrag110);
            return oppdrag110;
        }


        public void verifyStateForBuild() {
            Objects.requireNonNull(kodeAksjon, "kodeAksjon");
            Objects.requireNonNull(kodeEndring, "kodeEndring");
            Objects.requireNonNull(kodeFagomrade, "kodeFagomrade");
            Objects.requireNonNull(fagsystemId, "fagsystemId");
            Objects.requireNonNull(utbetFrekvens, "utbetFrekvens");
            Objects.requireNonNull(oppdragGjelderId, "oppdragGjelderId");
            Objects.requireNonNull(datoOppdragGjelderFom, "datoOppdragGjelderFom");
            Objects.requireNonNull(saksbehId, "saksbehId");
            Objects.requireNonNull(oppdragskontroll, "oppdragskontroll");
            Objects.requireNonNull(avstemming115, "avstemming115");
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                (id != null ? "id=" + id + ", " : "") //$NON-NLS-1$ //$NON-NLS-2$
                + "kodeAksjon=" + kodeAksjon + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "kodeEndring=" + kodeEndring + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "kodeFagomrade=" + kodeFagomrade + "," //$NON-NLS-1$ //$NON-NLS-2$
                + "fagsystemId=" + fagsystemId + "," //$NON-NLS-1$ //$NON-NLS-2$
                + "utbetFrekvens=" + utbetFrekvens + "," //$NON-NLS-1$ //$NON-NLS-2$
                + "oppdragGjelderId=" + oppdragGjelderId + "," //$NON-NLS-1$ //$NON-NLS-2$
                + "datoOppdragGjelderFom=" + datoOppdragGjelderFom + "," //$NON-NLS-1$ //$NON-NLS-2$
                + "saksbehId=" + saksbehId + "," //$NON-NLS-1$ //$NON-NLS-2$
                + "opprettetTs=" + getOpprettetTidspunkt() //$NON-NLS-1$
                + ">"; //$NON-NLS-1$
    }
}
