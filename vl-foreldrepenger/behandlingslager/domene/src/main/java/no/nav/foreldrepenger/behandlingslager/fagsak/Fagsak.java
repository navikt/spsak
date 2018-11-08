package no.nav.foreldrepenger.behandlingslager.fagsak;

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
import javax.persistence.PreRemove;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "Fagsak")
@Table(name = "FAGSAK")
public class Fagsak extends BaseEntitet {

    private static final Logger LOGGER = LoggerFactory.getLogger(Fagsak.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FAGSAK")
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "ytelse_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + FagsakYtelseType.DISCRIMINATOR + "'"))
    private FagsakYtelseType ytelseType = FagsakYtelseType.UDEFINERT;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bruker_id", nullable = false)
    private NavBruker navBruker;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "bruker_rolle", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + RelasjonsRolleType.DISCRIMINATOR + "'"))
    private RelasjonsRolleType brukerRolle = RelasjonsRolleType.UDEFINERT;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "fagsak_status", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + FagsakStatus.DISCRIMINATOR + "'"))
    private FagsakStatus fagsakStatus = FagsakStatus.DEFAULT;

    /**
     * Offisielt tildelt saksnummer fra GSAK.
     */
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "saksnummer", column = @Column(name = "saksnummer", unique = true)))
    private Saksnummer saksnummer;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "til_infotrygd", nullable = false)
    private boolean skalTilInfotrygd = false;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    Fagsak() {
        // Hibernate
    }

    private Fagsak(FagsakYtelseType ytelseType, NavBruker søker) {
        this(ytelseType, søker, null, null);
    }

    public Fagsak(FagsakYtelseType ytelseType, NavBruker søker, RelasjonsRolleType rolle, Saksnummer saksnummer) {
        Objects.requireNonNull(ytelseType, "ytelseType");
        this.ytelseType = ytelseType;
        this.navBruker = søker;
        if (rolle != null) {
            this.brukerRolle = rolle;
        }
        if (saksnummer != null) {
            setSaksnummer(saksnummer);
        }
    }

    public static Fagsak opprettNy(FagsakYtelseType ytelseType, NavBruker bruker) {
        return new Fagsak(ytelseType, bruker);
    }

    public static Fagsak opprettNy(FagsakYtelseType ytelseType, NavBruker bruker, RelasjonsRolleType rolle) {
        return new Fagsak(ytelseType, bruker, rolle, null);
    }

    public static Fagsak opprettNy(FagsakYtelseType ytelseType, NavBruker bruker, RelasjonsRolleType rolle, Saksnummer saksnummer) {
        return new Fagsak(ytelseType, bruker, rolle, saksnummer);
    }

    public Long getId() {
        return id;
    }

    /**
     * @deprecated Kun for test!.
     */
    @Deprecated
    public void setId(Long id) {
        this.id = id;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    void setSaksnummer(Saksnummer saksnummer) {
        this.saksnummer = saksnummer;
    }

    public NavBruker getNavBruker() {
        return navBruker;
    }

    public boolean erÅpen() {
        return !getFagsakStatus().equals(FagsakStatus.AVSLUTTET);
    }

    public RelasjonsRolleType getRelasjonsRolleType() {
        return brukerRolle;
    }

    void setRelasjonsRolleType(RelasjonsRolleType rolle) {
        if (brukerRolle == null) {
            this.brukerRolle = rolle;
        } else if (!rolle.equals(RelasjonsRolleType.UDEFINERT) && !brukerRolle.equals(rolle)) {
            if (!brukerRolle.equals(RelasjonsRolleType.UDEFINERT)) {
                FagsakFeil.FACTORY.brukerHarSkiftetRolle(brukerRolle.getKode(), rolle.getKode()).log(LOGGER);
            }
            this.brukerRolle = rolle;
        }
    }

    public FagsakStatus getStatus() {
        return getFagsakStatus();
    }

    public void setAvsluttet() {
        oppdaterStatus(FagsakStatus.AVSLUTTET);
    }

    void oppdaterStatus(FagsakStatus status) {
        this.setFagsakStatus(status);
    }

    public FagsakYtelseType getYtelseType() {
        return ytelseType;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Fagsak)) {
            return false;
        }
        Fagsak fagsak = (Fagsak) object;
        return Objects.equals(saksnummer, fagsak.saksnummer)
            && Objects.equals(ytelseType, fagsak.ytelseType)
            && Objects.equals(navBruker, fagsak.navBruker)
            && Objects.equals(getYtelseType(), fagsak.getYtelseType());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" //$NON-NLS-1$
            + (id == null ? "" : "id=" + id + ",") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            + " bruker=" + navBruker //$NON-NLS-1$
            + ">"; //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        return Objects.hash(ytelseType, navBruker);
    }

    public AktørId getAktørId() {
        return getNavBruker().getAktørId();
    }

    private FagsakStatus getFagsakStatus() {
        return fagsakStatus;
    }

    private void setFagsakStatus(FagsakStatus fagsakStatus) {
        this.fagsakStatus = fagsakStatus;
    }

    public boolean getSkalTilInfotrygd() {
        return skalTilInfotrygd;
    }

    public void setSkalTilInfotrygd(boolean tilInfotrygd) {
        this.skalTilInfotrygd = tilInfotrygd;
    }

    public long getVersjon() {
        return versjon;
    }

    @PreRemove
    protected void onDelete() {
        // FIXME: FPFEIL-2799 (FrodeC): Fjern denne når FPFEIL-2799 er godkjent
        throw new IllegalStateException("Skal aldri kunne slette fagsak. [id=" + id + ", status=" + getFagsakStatus() + ", type=" + ytelseType + "]");
    }
}
