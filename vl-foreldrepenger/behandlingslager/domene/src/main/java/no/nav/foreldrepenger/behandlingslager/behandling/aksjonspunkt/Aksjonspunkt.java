package no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.InternalUtil;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "Aksjonspunkt")
@Table(name = "AKSJONSPUNKT")
public class Aksjonspunkt extends BaseEntitet {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AKSJONSPUNKT")
    private Long id;

    @Column(name = "frist_tid")
    private LocalDateTime fristTid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @BatchSize(size = 10)
    @JoinColumn(name = "aksjonspunkt_def", nullable = false, updatable = false)
    private AksjonspunktDefinisjon aksjonspunktDefinisjon;

    @ManyToOne()
    @JoinColumn(name = "behandling_steg_funnet")
    private BehandlingStegType behandlingSteg;

    @ManyToOne(optional = false)
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false)
    private Behandling behandling;

    @ManyToOne()
    @JoinColumnOrFormula(column = @JoinColumn(name = "aksjonspunkt_status", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + AksjonspunktStatus.DISCRIMINATOR + "'"))
    private AksjonspunktStatus status;

    @ManyToOne()
    @JoinColumnOrFormula(column = @JoinColumn(name = "vent_aarsak", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + Venteårsak.DISCRIMINATOR + "'"))
    private Venteårsak venteårsak = Venteårsak.UDEFINERT;

    @Version
    @Column(name = "versjon", nullable = false)
    private Long versjon;

    /**
     * Saksbehandler begrunnelse som settes ifm at et aksjonspunkt settes til utført.
     */
    @Column(name = "begrunnelse")
    private String begrunnelse;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fomDato", column = @Column(name = "periode_fom")),
        @AttributeOverride(name = "tomDato", column = @Column(name = "periode_tom"))
    })
    private DatoIntervallEntitet periode;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "TOTRINN_BEHANDLING", nullable = false)
    private boolean toTrinnsBehandling;

    /**
     * Definerer hvorvidt Aksjonspunktet slettes ved tilbakehopp som krever registerinnhentinger
     *
     * @deprecated Skal ikke ha dette på Aksjonspunkt
     */
    @Deprecated
    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "SLETTES_VED_REGISTERINNHENTING", nullable = false)
    private boolean slettesVedRegisterinnhenting = true;

    /**
     * Angir om aksjonspunktet er aktivt. NB: Ikke samme som status.
     * Inaktive aksjonspunkter er historiske som ble kopiert når en revurdering ble opprettet. De eksisterer for å kunne vise den opprinnelige begrunnelsen, uten at saksbehandler må ta stilling til det på nytt..
     */
    @ManyToOne()
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "REAKTIVERING_STATUS", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + ReaktiveringStatus.DISCRIMINATOR
            + "'"))})
    private ReaktiveringStatus reaktiveringStatus = ReaktiveringStatus.AKTIV;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "MANUELT_OPPRETTET", nullable = false)
    private boolean manueltOpprettet;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "REVURDERING", nullable = false)
    private boolean revurdering = false;

    Aksjonspunkt() {
        // for hibernate
    }

    protected Aksjonspunkt(AksjonspunktDefinisjon aksjonspunktDef, BehandlingStegType behandlingStegFunnet) {
        Objects.requireNonNull(behandlingStegFunnet, "behandlingStegFunnet"); //$NON-NLS-1$
        Objects.requireNonNull(aksjonspunktDef, "aksjonspunktDef"); //$NON-NLS-1$
        this.behandlingSteg = behandlingStegFunnet;
        this.aksjonspunktDefinisjon = aksjonspunktDef;
        this.toTrinnsBehandling = aksjonspunktDef.getDefaultTotrinnBehandling();
        this.status = AksjonspunktStatus.OPPRETTET;
    }

    protected Aksjonspunkt(AksjonspunktDefinisjon aksjonspunktDef) {
        Objects.requireNonNull(aksjonspunktDef, "aksjonspunktDef"); //$NON-NLS-1$
        this.aksjonspunktDefinisjon = aksjonspunktDef;
        this.toTrinnsBehandling = aksjonspunktDef.getDefaultTotrinnBehandling();
        this.status = AksjonspunktStatus.OPPRETTET;
    }

    public Long getId() {
        return id;
    }

    public boolean erManuell() {
        return getAksjonspunktDefinisjon() != null && AksjonspunktType.MANUELL.equals(getAksjonspunktDefinisjon().getAksjonspunktType());
    }

    public boolean erOverstyrt() {
        return getAksjonspunktDefinisjon() != null && AksjonspunktType.OVERSTYRING.equals(getAksjonspunktDefinisjon().getAksjonspunktType());
    }

    /**
     * Hvorvidt et Aksjonspunkt er av typen Autopunkt.
     * <p>
     * NB: Ikke bruk dette til å styre på vent eller lignende. Bruk
     * egenskapene til Aksjonspunktet i stedet (eks. hvorvidt det har en frist).
     */
    public boolean erAutopunkt() {
        return getAksjonspunktDefinisjon() != null && AksjonspunktType.AUTOPUNKT.equals(getAksjonspunktDefinisjon().getAksjonspunktType());
    }

    public boolean erAktivt() {
        return reaktiveringStatus.equals(ReaktiveringStatus.AKTIV);
    }

    public ReaktiveringStatus getReaktiveringStatus() {
        return reaktiveringStatus;
    }

    public boolean erManueltOpprettet() {
        return manueltOpprettet;
    }

    void setManueltOpprettet(boolean manueltOpprettet) {
        this.manueltOpprettet = manueltOpprettet;
    }

    public boolean erRevurdering() {
        return revurdering;
    }

    private void setRevurdering(boolean revurdering) {
        this.revurdering = revurdering;
    }

    private void setBehandlingsresultat(Behandling behandling) {
        // brukes kun internt for å koble sammen aksjonspunkt og behandling
        this.behandling = behandling;
    }

    public Behandling getBehandling() {
        return behandling;
    }

    public AksjonspunktDefinisjon getAksjonspunktDefinisjon() {
        return aksjonspunktDefinisjon;
    }

    public AksjonspunktStatus getStatus() {
        return status;
    }

    public boolean tilbakehoppVedGjenopptakelse() {
        return aksjonspunktDefinisjon.tilbakehoppVedGjenopptakelse();
    }

    /**
     * Sett til utført med gitt begrunnelse. Returner true dersom ble endret, false dersom allerede var utfør og hadde
     * samme begrunnelse.
     *
     * @return true hvis status eller begrunnelse er endret.
     */
    boolean setStatus(AksjonspunktStatus nyStatus, String begrunnelse) {
        boolean statusEndret = !Objects.equals(getStatus(), nyStatus);

        if (statusEndret) {
            if (Objects.equals(nyStatus, AksjonspunktStatus.UTFØRT)) {
                validerIkkeAvbruttAllerede();
            }

            this.status = nyStatus;
        }

        boolean begrunnelseEndret = !Objects.equals(getBegrunnelse(), begrunnelse);
        if (begrunnelseEndret) {
            setBegrunnelse(begrunnelse);
        }

        return begrunnelseEndret || statusEndret;
    }

    public BehandlingStegType getBehandlingStegFunnet() {
        return behandlingSteg;
    }

    public LocalDateTime getFristTid() {
        return fristTid;
    }

    void setFristTid(LocalDateTime fristTid) {
        this.fristTid = fristTid;
    }

    public boolean erOpprettet() {
        return Objects.equals(getStatus(), AksjonspunktStatus.OPPRETTET);
    }

    public boolean erÅpentAksjonspunkt() {
        return status.erÅpentAksjonspunkt();
    }

    public boolean erBehandletAksjonspunkt() {
        return status.erBehandletAksjonspunkt();
    }

    static Optional<Aksjonspunkt> finnEksisterende(Behandling behandling, AksjonspunktDefinisjon ap) {
        return behandling.getAlleAksjonspunkterInklInaktive().stream()
            .filter(a -> a.getAksjonspunktDefinisjon().equals(ap))
            .findFirst();
    }

    /**
     * Returner liste av abstraktpunkt definisjon koder.
     */
    public static List<String> getKoder(List<Aksjonspunkt> abstraktpunkter) {
        return abstraktpunkter.stream().map(ap -> ap.getAksjonspunktDefinisjon().getKode()).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Aksjonspunkt)) {
            return false;
        }
        Aksjonspunkt kontrollpunkt = (Aksjonspunkt) object;
        return Objects.equals(getAksjonspunktDefinisjon(), kontrollpunkt.getAksjonspunktDefinisjon())
            && Objects.equals(getBehandling(), kontrollpunkt.getBehandling())
            && Objects.equals(getStatus(), kontrollpunkt.getStatus())
            && Objects.equals(getPeriode(), kontrollpunkt.getPeriode())
            && Objects.equals(getFristTid(), kontrollpunkt.getFristTid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAksjonspunktDefinisjon(), getBehandling(), getStatus(), getPeriode(), getFristTid());
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    public boolean isToTrinnsBehandling() {
        return toTrinnsBehandling || aksjonspunktDefinisjon.getDefaultTotrinnBehandling();
    }

    void settToTrinnsFlag() {
        validerIkkeUtførtAvbruttAllerede();
        this.setToTrinnsBehandling(true);
    }

    void fjernToTrinnsFlagg() {
        validerIkkeUtførtAvbruttAllerede();
        this.setToTrinnsBehandling(false);
    }

    private void validerIkkeUtførtAvbruttAllerede() {
        if (erUtført() || erAvbrutt()) {
            // TODO (FC): håndteres av låsing allerede? Kaster exception nå for å se om GUI kan være ute av synk.
            throw new IllegalStateException("Forsøkte å bekrefte et allerede lukket aksjonspunkt:" + this); //$NON-NLS-1$
        }
    }

    private void validerIkkeAvbruttAllerede() {
        if (erAvbrutt()) {
            throw new IllegalStateException("Forsøkte å bekrefte et allerede lukket aksjonspunkt:" + this); //$NON-NLS-1$
        }
    }


    public Venteårsak getVenteårsak() {
        return venteårsak;
    }

    void setVenteårsak(Venteårsak venteårsak) {
        this.venteårsak = venteårsak;
    }

    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    /**
     * (optional)
     * Periode aksjonspunktet peker på.
     */
    void setPeriode(DatoIntervallEntitet periode) {
        this.periode = periode;
    }

    /**
     * Intern Builder. Bruk {@link AksjonspunktRepository} til å legge til og endre {@link Aksjonspunkt}.
     */
    static class Builder {
        private Aksjonspunkt opprinneligAp;
        private Aksjonspunkt aksjonspunkt;
        private boolean slettet = false;

        Builder(AksjonspunktDefinisjon aksjonspunktDefinisjon, BehandlingStegType behandlingStegFunnet) {
            this.aksjonspunkt = new Aksjonspunkt(aksjonspunktDefinisjon, behandlingStegFunnet);
        }

        Builder(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
            this.aksjonspunkt = new Aksjonspunkt(aksjonspunktDefinisjon);
        }

        Builder(Aksjonspunkt opprinneligAp) {
            this.opprinneligAp = opprinneligAp;
            this.aksjonspunkt = new Aksjonspunkt(opprinneligAp.getAksjonspunktDefinisjon());
        }

        /**
         * Angir om aksjonspunktet gjelder en spesifikk periode. Forutsetter at opplysningene aksjonspunktet er
         * opprettet for er periodisert.
         * <p>
         * NB: Skal kun brukes for aksjonspunkt som kan repteres flere multipliser for en behandling (eks. per periode i
         * utgangsvilkår).
         */
        Aksjonspunkt.Builder medPeriode(DatoIntervallEntitet status) {
            sjekkTilstand();
            this.aksjonspunkt.setPeriode(status);
            return this;
        }

        private void sjekkTilstand() {
            if (aksjonspunkt == null) {
                throw new IllegalStateException("Aksjonpunkt ikke definert"); //$NON-NLS-1$
            }
        }

        Aksjonspunkt buildFor(Behandling behandling) {
            Aksjonspunkt ap = this.aksjonspunkt;
            if (this.opprinneligAp != null) {
                if (behandling.erRevurdering()) {
                    kopierAlleFelter(opprinneligAp, ap, false);
                } else {
                    kopierAlleFelter(opprinneligAp, ap, true);
                }
            }
            Optional<Aksjonspunkt> eksisterende = finnEksisterende(behandling, ap.aksjonspunktDefinisjon);
            if (slettet) {
                InternalUtil.fjernAksjonspunkt(behandling, ap);
                return ap;
            } else if (eksisterende.isPresent()) {
                // Oppdater eksisterende. Aktiver dersom ikke allerede aktivt.
                Aksjonspunkt eksisterendeAksjonspunkt = eksisterende.get();
                kopierBasisfelter(ap, eksisterendeAksjonspunkt);
                reaktiverAksjonspunkt(eksisterendeAksjonspunkt);
                return eksisterendeAksjonspunkt;
            } else {
                // Opprett ny og knytt til behandlingsresultat
                ap.setBehandlingsresultat(behandling);
                InternalUtil.leggTilAksjonspunkt(behandling, ap);
                return ap;
            }
        }

        private void kopierAlleFelter(Aksjonspunkt fra, Aksjonspunkt til, boolean medTotrinnsfelter) {
            kopierBasisfelter(fra, til);
            if (medTotrinnsfelter) {
                til.setToTrinnsBehandling(fra.isToTrinnsBehandling());
            }
            til.setBehandlingSteg(fra.getBehandlingStegFunnet());
            til.setSlettesVedRegisterinnhenting(fra.getSlettesVedRegisterinnhenting());
            til.setManueltOpprettet(fra.erManueltOpprettet());
        }

        // Der
        private void reaktiverAksjonspunkt(Aksjonspunkt eksisterendeAksjonspunkt) {
            if (!eksisterendeAksjonspunkt.erAktivt()) {
                eksisterendeAksjonspunkt.setAktivStatus(ReaktiveringStatus.AKTIV);
            }
        }

        private void kopierBasisfelter(Aksjonspunkt fra, Aksjonspunkt til) {
            til.setBegrunnelse(fra.getBegrunnelse());
            til.setPeriode(fra.getPeriode());
            til.setVenteårsak(fra.getVenteårsak());
            til.setFristTid(fra.getFristTid());
            til.setStatus(fra.getStatus(), fra.getBegrunnelse());
        }

        Aksjonspunkt.Builder medFristTid(LocalDateTime fristTid) {
            aksjonspunkt.setFristTid(fristTid);
            return this;
        }

        Aksjonspunkt.Builder medVenteårsak(Venteårsak venteårsak) {
            aksjonspunkt.setVenteårsak(venteårsak);
            return this;
        }

        Aksjonspunkt.Builder medSletting() {
            slettet = true;
            return this;
        }

        Aksjonspunkt.Builder medReaktiveringsstatus(ReaktiveringStatus reaktiveringStatus) {
            aksjonspunkt.setAktivStatus(reaktiveringStatus);
            return this;
        }

        Aksjonspunkt.Builder manueltOpprettet() {
            aksjonspunkt.setManueltOpprettet(true);
            return this;
        }

        Aksjonspunkt.Builder medRevurdering() {
            aksjonspunkt.setRevurdering(true);
            return this;
        }

        Aksjonspunkt.Builder medTotrinnskontroll(boolean toTrinnsbehandling) {
            aksjonspunkt.setToTrinnsBehandling(toTrinnsbehandling);
            return this;
        }
    }

    public boolean erUtført() {
        return Objects.equals(status, AksjonspunktStatus.UTFØRT);
    }

    public boolean erAvbrutt() {
        return Objects.equals(status, AksjonspunktStatus.AVBRUTT);
    }

    /**
     * Brukes for gammelt status LUKKET (nå fjernet).
     *
     * @deprecated
     */
    @Deprecated
    public boolean erLukket() {
        return erBehandletAksjonspunkt() || erAvbrutt();
    }

    @Override
    public String toString() {
        return "Aksjonspunkt{" +
            "id=" + id +
            ", aksjonspunktDefinisjon=" + getAksjonspunktDefinisjon() +
            ", status=" + status +
            ", reaktiveringStatus=" + reaktiveringStatus +
            ", manueltOpprettet=" + manueltOpprettet +
            ", behandlingStegFunnet=" + getBehandlingStegFunnet() +
            ", versjon=" + versjon +
            ", begrunnelse='" + begrunnelse + '\'' +
            ", periode=" + periode +
            ", toTrinnsBehandling=" + isToTrinnsBehandling() +
            ", fristTid=" + getFristTid() +
            ", revurdering=" + revurdering +
            '}';
    }

    void setBehandlingSteg(BehandlingStegType stegType) {
        this.behandlingSteg = stegType;
    }

    private void setToTrinnsBehandling(boolean toTrinnsBehandling) {
        this.toTrinnsBehandling = toTrinnsBehandling;
    }

    /**
     * @deprecated Skal ikke ha dette per Aksjonspunkt
     */
    @Deprecated
    void setSlettesVedRegisterinnhenting(boolean slettesVedRegisterinnhenting) {
        this.slettesVedRegisterinnhenting = slettesVedRegisterinnhenting;
    }

    /**
     * @deprecated Skal ikke ha dette per Aksjonspunkt
     */
    @Deprecated
    public boolean getSlettesVedRegisterinnhenting() {
        return slettesVedRegisterinnhenting;
    }

    void setAktivStatus(ReaktiveringStatus reaktiveringStatus) {
        this.reaktiveringStatus = reaktiveringStatus;
    }
}
