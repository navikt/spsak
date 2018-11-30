package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseAnvist;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.FagsystemUnderkategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "YtelseEntitet")
@Table(name = "IAY_RELATERT_YTELSE")
public class YtelseEntitet extends BaseEntitet implements Ytelse, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YTELSE")
    private Long id;

    @OneToOne(mappedBy = "ytelse")
    private YtelseGrunnlagEntitet ytelseGrunnlag;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "ytelse_type", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + RelatertYtelseType.DISCRIMINATOR + "'"))})
    private RelatertYtelseType relatertYtelseType;

    @Embedded
    @ChangeTracked
    private DatoIntervallEntitet periode;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "status", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + RelatertYtelseTilstand.DISCRIMINATOR + "'"))})
    @ChangeTracked
    private RelatertYtelseTilstand status;

    /**
     * Saksnummer (fra Arena, Infotrygd, ..).
     */
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "saksnummer", column = @Column(name = "saksnummer")))
    private Saksnummer saksnummer;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "kilde", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + Fagsystem.DISCRIMINATOR + "'"))})
    @ChangeTracked
    private Fagsystem kilde;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "FAGSYSTEM_UNDERKATEGORI", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + FagsystemUnderkategori.DISCRIMINATOR + "'"))})
    @ChangeTracked
    private FagsystemUnderkategori fagsystemUnderkategori = FagsystemUnderkategori.UDEFINERT;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "temaUnderkategori", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'"
            + TemaUnderkategori.DISCRIMINATOR
            + "'"))})
    @ChangeTracked
    private TemaUnderkategori temaUnderkategori = TemaUnderkategori.UDEFINERT;

    @OneToMany(mappedBy = "ytelse")
    @ChangeTracked
    private Set<YtelseAnvistEntitet> ytelseAnvist = new LinkedHashSet<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "aktoer_ytelse_id", nullable = false, updatable = false)
    private AktørYtelseEntitet aktørYtelse;

    @Transient
    private LocalDate skjæringstidspunkt;
    @Transient
    private boolean ventreSideAvSkjæringstidspunkt;

    public YtelseEntitet() {
        // hibernate
    }

    public YtelseEntitet(Ytelse ytelse) {
        this.relatertYtelseType = ytelse.getRelatertYtelseType();
        this.status = ytelse.getStatus();
        this.periode = ytelse.getPeriode();
        this.saksnummer = ytelse.getSaksnummer();
        this.temaUnderkategori = ytelse.getBehandlingsTema();
        this.kilde = ytelse.getKilde();
        this.fagsystemUnderkategori = ytelse.getFagsystemUnderkategori();
        ytelse.getYtelseGrunnlag().ifPresent(yg -> {
            YtelseGrunnlagEntitet ygn = new YtelseGrunnlagEntitet(yg);
            ygn.setYtelse(this);
            this.ytelseGrunnlag = ygn;
        });
        this.ytelseAnvist = ytelse.getYtelseAnvist().stream().map(ya -> {
            YtelseAnvistEntitet ytelseAnvistEntitet = new YtelseAnvistEntitet(ya);
            ytelseAnvistEntitet.setYtelse(this);
            return ytelseAnvistEntitet;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(periode, relatertYtelseType, saksnummer);
    }

    void setAktørYtelse(AktørYtelseEntitet aktørYtelse) {
        this.aktørYtelse = aktørYtelse;
    }

    @Override
    public RelatertYtelseType getRelatertYtelseType() {
        return relatertYtelseType;
    }

    void setRelatertYtelseType(RelatertYtelseType relatertYtelseType) {
        this.relatertYtelseType = relatertYtelseType;
    }

    @Override
    public TemaUnderkategori getBehandlingsTema() {
        return temaUnderkategori;
    }

    void setBehandlingsTema(TemaUnderkategori behandlingsTema) {
        this.temaUnderkategori = behandlingsTema;
    }

    @Override
    public RelatertYtelseTilstand getStatus() {
        return status;
    }

    void setStatus(RelatertYtelseTilstand status) {
        this.status = status;
    }

    @Override
    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    void setPeriode(DatoIntervallEntitet periode) {
        this.periode = periode;
    }

    @Override
    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    void medSakId(Saksnummer saksnummer) {
        this.saksnummer = saksnummer;
    }

    @Override
    public Fagsystem getKilde() {
        return kilde;
    }

    void setKilde(Fagsystem kilde) {
        this.kilde = kilde;
    }

    @Override
    public FagsystemUnderkategori getFagsystemUnderkategori() {
        return fagsystemUnderkategori;
    }

    void setFagsystemUnderkategori(FagsystemUnderkategori fagsystemUnderkategori) {
        this.fagsystemUnderkategori = fagsystemUnderkategori;
    }

    @Override
    public Optional<YtelseGrunnlag> getYtelseGrunnlag() {
        return Optional.ofNullable(ytelseGrunnlag);
    }

    void setYtelseGrunnlag(YtelseGrunnlag ytelseGrunnlag) {
        if (ytelseGrunnlag != null) {
            YtelseGrunnlagEntitet ytelseGrunnlagEntitet = (YtelseGrunnlagEntitet) ytelseGrunnlag;
            ytelseGrunnlagEntitet.setYtelse(this);
            this.ytelseGrunnlag = ytelseGrunnlagEntitet;
        }
    }

    @Override
    public Collection<YtelseAnvist> getYtelseAnvist() {
        return Collections.unmodifiableCollection(ytelseAnvist);
    }

    void leggTilYtelseAnvist(YtelseAnvist ytelseAnvist) {
        YtelseAnvistEntitet ytelseAnvistEntitet = (YtelseAnvistEntitet) ytelseAnvist;
        ytelseAnvistEntitet.setYtelse(this);
        this.ytelseAnvist.add(ytelseAnvistEntitet);

    }

    void tilbakestillAnvisteYtelser() {
        ytelseAnvist.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        YtelseEntitet that = (YtelseEntitet) o;
        return Objects.equals(relatertYtelseType, that.relatertYtelseType) &&
            Objects.equals(temaUnderkategori, that.temaUnderkategori) &&
            (Objects.equals(periode, that.periode) || Objects.equals(periode.getFomDato(), that.periode.getFomDato())) &&
            Objects.equals(saksnummer, that.saksnummer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relatertYtelseType, periode, saksnummer);
    }

    @Override
    public String toString() {
        return "YtelseEntitet{" + //$NON-NLS-1$
            "relatertYtelseType=" + relatertYtelseType + //$NON-NLS-1$
            ", typeUnderkategori=" + temaUnderkategori + //$NON-NLS-1$
            ", periode=" + periode + //$NON-NLS-1$
            ", relatertYtelseStatus=" + status + //$NON-NLS-1$
            ", saksNummer='" + saksnummer + '\'' + //$NON-NLS-1$
            '}';
    }

    void setSkjæringstidspunkt(LocalDate skjæringstidspunkt, boolean ventreSide) {
        this.skjæringstidspunkt = skjæringstidspunkt;
        this.ventreSideAvSkjæringstidspunkt = ventreSide;
    }

    boolean skalMedEtterSkjæringstidspunktVurdering() {
        if (skjæringstidspunkt != null) {
            if (ventreSideAvSkjæringstidspunkt) {
                return periode.getFomDato().isBefore(skjæringstidspunkt.plusDays(1));
            } else {
                return periode.getFomDato().isAfter(skjæringstidspunkt) ||
                    periode.getFomDato().isBefore(skjæringstidspunkt.plusDays(1)) && periode.getTomDato().isAfter(skjæringstidspunkt);
            }
        }
        return true;
    }
}
