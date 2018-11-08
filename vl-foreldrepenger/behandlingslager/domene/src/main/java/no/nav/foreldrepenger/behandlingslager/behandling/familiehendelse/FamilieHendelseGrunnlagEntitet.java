package no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;


@Entity(name = "FamilieHendelseGrunnlag")
@Table(name = "GR_FAMILIE_HENDELSE")
public class FamilieHendelseGrunnlagEntitet extends BaseEntitet implements FamilieHendelseGrunnlag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GR_FAMILIE_HENDELSE")
    private Long id;

    @OneToOne
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false, unique = true)
    private Behandling behandling;

    @OneToOne(optional = false)
    @JoinColumn(name = "soeknad_familie_hendelse_id", nullable = false, updatable = false, unique = true)
    @ChangeTracked
    private FamilieHendelseEntitet søknadHendelse;

    @OneToOne
    @JoinColumn(name = "bekreftet_familie_hendelse_id", updatable = false, unique = true)
    @ChangeTracked
    private FamilieHendelseEntitet bekreftetHendelse;

    @OneToOne
    @JoinColumn(name = "overstyrt_familie_hendelse_id", updatable = false, unique = true)
    @ChangeTracked
    private FamilieHendelseEntitet overstyrtHendelse;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    FamilieHendelseGrunnlagEntitet() {
    }

    FamilieHendelseGrunnlagEntitet(Behandling behandling) {
        this.behandling = behandling;
    }


    FamilieHendelseGrunnlagEntitet(FamilieHendelseGrunnlag grunnlag) {
        this.søknadHendelse = (FamilieHendelseEntitet) grunnlag.getSøknadVersjon();
        grunnlag.getBekreftetVersjon().ifPresent(nyBekreftetVersjon -> this.setBekreftetHendelse((FamilieHendelseEntitet) nyBekreftetVersjon));
        grunnlag.getOverstyrtVersjon().ifPresent(nyOverstyrtVersjon -> this.setOverstyrtHendelse((FamilieHendelseEntitet) nyOverstyrtVersjon));
    }


    public Long getId() {
        return id;
    }

    @Override
    public FamilieHendelse getSøknadVersjon() {
        return søknadHendelse;
    }

    @Override
    public Optional<FamilieHendelse> getBekreftetVersjon() {
        return Optional.ofNullable(bekreftetHendelse);
    }

    public Behandling getBehandling() {
        return behandling;
    }

    void setBehandling(Behandling behandling) {
        this.behandling = behandling;
    }

    @Override
    public Optional<FamilieHendelse> getOverstyrtVersjon() {
        return Optional.ofNullable(overstyrtHendelse);
    }

    @Override
    public boolean getHarBekreftedeData() {
        return getGjeldendeBekreftetVersjon().isPresent();
    }

    @Override
    public boolean getHarOverstyrteData() {
        return getOverstyrtVersjon().isPresent();
    }

    @Override
    public FamilieHendelse getGjeldendeVersjon() {
        if (getOverstyrtVersjon().isPresent()) {
            return overstyrtHendelse;
        } else if (getBekreftetVersjon().isPresent()) {
            return bekreftetHendelse;
        }
        return søknadHendelse;
    }

    @Override
    public Optional<Adopsjon> getGjeldendeAdopsjon() {
        final Optional<Adopsjon> overstyrt = getOverstyrtVersjon().flatMap(FamilieHendelse::getAdopsjon);
        if (overstyrt.isPresent()) {
            return overstyrt;
        }
        final Optional<Adopsjon> bekreftet = getBekreftetVersjon().flatMap(FamilieHendelse::getAdopsjon);
        if (bekreftet.isPresent()) {
            return bekreftet;
        }
        return getSøknadVersjon().getAdopsjon();
    }

    @Override
    public Optional<Terminbekreftelse> getGjeldendeTerminbekreftelse() {
        final Optional<Terminbekreftelse> overstyrt = getOverstyrtVersjon().flatMap(FamilieHendelse::getTerminbekreftelse);
        if (overstyrt.isPresent()) {
            return overstyrt;
        }
        final Optional<Terminbekreftelse> bekreftet = getBekreftetVersjon().flatMap(FamilieHendelse::getTerminbekreftelse);
        if (bekreftet.isPresent()) {
            return bekreftet;
        }
        return getSøknadVersjon().getTerminbekreftelse();
    }

    @Override
    public List<UidentifisertBarn> getGjeldendeBarna() {
        final Optional<List<UidentifisertBarn>> overstyrt = getOverstyrtVersjon().map(FamilieHendelse::getBarna);
        if (overstyrt.isPresent() && !overstyrt.get().isEmpty()) {
            return overstyrt.get();
        }
        final Optional<List<UidentifisertBarn>> bekreftet = getBekreftetVersjon().map(FamilieHendelse::getBarna);
        if(bekreftet.isPresent() && !bekreftet.get().isEmpty()) {
            return bekreftet.get();
        }
        return søknadHendelse.getBarna();
    }

    @Override
    public Integer getGjeldendeAntallBarn() {
        final Optional<Integer> overstyrt = getOverstyrtVersjon().map(FamilieHendelse::getAntallBarn);
        if (overstyrt.isPresent()) {
            return overstyrt.get();
        }
        final Optional<Integer> bekreftet = getBekreftetVersjon().map(FamilieHendelse::getAntallBarn);
        return bekreftet.orElseGet(() -> getSøknadVersjon().getAntallBarn());
    }

    @Override
    public Optional<FamilieHendelse> getGjeldendeBekreftetVersjon() {
        if (getOverstyrtVersjon().isPresent()) {
            return Optional.of(overstyrtHendelse);
        }
        return Optional.ofNullable(bekreftetHendelse);
    }

    @Override
    public LocalDate finnGjeldendeFødselsdato() {
        final Optional<FamilieHendelse> bekreftetVersjon = getGjeldendeBekreftetVersjon();
        if (bekreftetVersjon.isPresent() && !bekreftetVersjon.get().getBarna().isEmpty()) {
            return bekreftetVersjon.get().getBarna().stream().map(UidentifisertBarn::getFødselsdato).findFirst().get();  // NOSONAR
        }
        final Optional<Terminbekreftelse> terminbekreftelse = getGjeldendeBekreftetVersjon().flatMap(FamilieHendelse::getTerminbekreftelse);
        if(terminbekreftelse.isPresent()) {
            return terminbekreftelse.get().getTermindato();
        }
        if (!søknadHendelse.getBarna().isEmpty()) {
            return søknadHendelse.getBarna().stream().map(UidentifisertBarn::getFødselsdato).findFirst().get();  // NOSONAR
        }
        return søknadHendelse.getTerminbekreftelse().get().getTermindato(); // NOSONAR
    }

    public boolean getErAktivt() {
        return aktiv;
    }

    void setSøknadHendelse(FamilieHendelseEntitet soeknadHendelse) {
        this.søknadHendelse = soeknadHendelse;
    }

    void setBekreftetHendelse(FamilieHendelseEntitet registerHendelse) {
        this.bekreftetHendelse = registerHendelse;
    }

    void setOverstyrtHendelse(FamilieHendelseEntitet overstyrtHendelse) {
        this.overstyrtHendelse = overstyrtHendelse;
    }

    void setAktiv(boolean aktivt) {
        this.aktiv = aktivt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FamilieHendelseGrunnlagEntitet that = (FamilieHendelseGrunnlagEntitet) o;
        return aktiv == that.aktiv &&
            Objects.equals(søknadHendelse, that.søknadHendelse) &&
            Objects.equals(bekreftetHendelse, that.bekreftetHendelse) &&
            Objects.equals(overstyrtHendelse, that.overstyrtHendelse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(søknadHendelse, bekreftetHendelse, overstyrtHendelse, aktiv);
    }
}
