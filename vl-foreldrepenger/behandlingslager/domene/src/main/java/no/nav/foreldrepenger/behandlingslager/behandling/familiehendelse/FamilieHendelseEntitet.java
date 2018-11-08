package no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Table(name = "FH_FAMILIE_HENDELSE")
@Entity(name = "FamilieHendelse")
public class FamilieHendelseEntitet extends BaseEntitet implements FamilieHendelse {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FAMILIE_HENDELSE")
    private Long id;

    @ChangeTracked
    @OneToOne(mappedBy = "familieHendelse")
    private AdopsjonEntitet adopsjon;

    @ChangeTracked
    @OneToOne(mappedBy = "familieHendelse")
    private TerminbekreftelseEntitet terminbekreftelse;

    @ChangeTracked
    @OneToMany(mappedBy = "familieHendelse")
    private List<UidentifisertBarnEntitet> barna = new ArrayList<>();

    @ChangeTracked
    @Column(name = "antall_barn")
    private Integer antallBarn;

    @ChangeTracked
    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "mor_for_syk_ved_fodsel")
    private Boolean morForSykVedFødsel;

    @ChangeTracked
    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "familie_hendelse_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + FamilieHendelseType.DISCRIMINATOR + "'"))
    private FamilieHendelseType type = FamilieHendelseType.UDEFINERT;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    FamilieHendelseEntitet() {
    }

    FamilieHendelseEntitet(FamilieHendelseType type) {
        this.type = type;
    }

    FamilieHendelseEntitet(FamilieHendelse hendelse) {
        this.type = hendelse.getType();
        this.antallBarn = hendelse.getAntallBarn();
        this.morForSykVedFødsel = hendelse.erMorForSykVedFødsel();

        hendelse.getAdopsjon().ifPresent(it -> {
            final AdopsjonEntitet nyAdopsjon = new AdopsjonEntitet(it);
            nyAdopsjon.setFamilieHendelse(this);
            this.setAdopsjon(nyAdopsjon);
        });
        hendelse.getTerminbekreftelse().ifPresent(it -> {
            final TerminbekreftelseEntitet nyTerminbekreftelse = new TerminbekreftelseEntitet(it);
            nyTerminbekreftelse.setFamilieHendelse(this);
            this.setTerminbekreftelse(nyTerminbekreftelse);
        });


        Map<Integer, UidentifisertBarnEntitet> barnRelatertTilHendelse = this.barna.stream().collect(Collectors.toMap(UidentifisertBarn::getBarnNummer, Function.identity()));
        for (UidentifisertBarn barn : hendelse.getBarna()) {
            UidentifisertBarnEntitet lagretBarn = barnRelatertTilHendelse.get(barn.getBarnNummer());
            if (lagretBarn == null) {
                UidentifisertBarnEntitet kopi = new UidentifisertBarnEntitet(barn);
                kopi.setFamilieHendelse(this);
                this.barna.add(kopi);
            } else {
                lagretBarn.deepCopy(barn);
            }
        }
    }

    Long getId() {
        return id;
    }

    @Override
    public Integer getAntallBarn() {
        return antallBarn;
    }

    void setAntallBarn(Integer antallBarn) {
        this.antallBarn = antallBarn;
    }

    @Override
    public List<UidentifisertBarn> getBarna() {
        return Collections.unmodifiableList(barna);
    }

    void leggTilBarn(UidentifisertBarn barn) {
        final UidentifisertBarnEntitet barnEntitet = (UidentifisertBarnEntitet) barn;
        this.barna.add(barnEntitet);
        barnEntitet.setFamilieHendelse(this);
    }

    void clearBarn() {
        this.barna.clear();
    }

    @Override
    public Optional<Terminbekreftelse> getTerminbekreftelse() {
        return Optional.ofNullable(terminbekreftelse);
    }

    void setTerminbekreftelse(TerminbekreftelseEntitet terminbekreftelse) {
        this.terminbekreftelse = terminbekreftelse;
        if (terminbekreftelse != null) {
            this.terminbekreftelse.setFamilieHendelse(this);
        }
    }

    @Override
    public Optional<Adopsjon> getAdopsjon() {
        return Optional.ofNullable(adopsjon);
    }

    void setAdopsjon(AdopsjonEntitet adopsjon) {
        this.adopsjon = adopsjon;
        if (adopsjon != null) {
            this.adopsjon.setFamilieHendelse(this);
        }
    }

    @Override
    public FamilieHendelseType getType() {
        return type;
    }

    void setType(FamilieHendelseType type) {
        this.type = type;
    }

    @Override
    public Optional<LocalDate> getFødselsdato() {
        if (type.equals(FamilieHendelseType.FØDSEL)) {
            return barna.stream().map(UidentifisertBarnEntitet::getFødselsdato).findFirst();
        }
        return Optional.empty();
    }

    @Override
    public boolean getInnholderDøfødtBarn() {
        if (type.equals(FamilieHendelseType.FØDSEL)) {
            return barna.stream().anyMatch(barn -> barn.getDødsdato().isPresent()
                && barn.getDødsdato().get().equals(barn.getFødselsdato()));
        }
        return false;
    }

    @Override
    public boolean getInnholderDødtBarn() {
        if (type.equals(FamilieHendelseType.FØDSEL)) {
            return barna.stream().anyMatch(barn -> barn.getDødsdato().isPresent());
        }
        return false;
    }

    @Override
    public Boolean erMorForSykVedFødsel() {
        return morForSykVedFødsel;
    }

    @Override
    public LocalDate getSkjæringstidspunkt() {
        if (FamilieHendelseType.TERMIN.equals(type)) {
            return terminbekreftelse.getTermindato();
        }
        if (FamilieHendelseType.FØDSEL.equals(type)) {
            return getFødselsdato().orElse(null);
        }
        if (FamilieHendelseType.ADOPSJON.equals(type) || FamilieHendelseType.OMSORG.equals(type)) {
            return adopsjon.getOmsorgsovertakelseDato();
        }
        throw new IllegalStateException("Utvikler feil: ukjent hendelsestype.");
    }

    @Override
    public boolean getGjelderFødsel() {
        return FamilieHendelseType.FØDSEL.equals(type) || FamilieHendelseType.TERMIN.equals(type);
    }

    @Override
    public boolean getGjelderAdopsjon() {
        return FamilieHendelseType.ADOPSJON.equals(type) || FamilieHendelseType.OMSORG.equals(type);
    }

    void setMorForSykVedFødsel(Boolean erMorForSykVedFødsel) {
        this.morForSykVedFødsel = erMorForSykVedFødsel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FamilieHendelseEntitet that = (FamilieHendelseEntitet) o;
        return Objects.equals(antallBarn, that.antallBarn) &&
            Objects.equals(adopsjon, that.adopsjon) &&
            Objects.equals(terminbekreftelse, that.terminbekreftelse) &&
            Objects.equals(barna, that.barna) &&
            Objects.equals(type, that.type) &&
            Objects.equals(morForSykVedFødsel, that.morForSykVedFødsel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adopsjon, terminbekreftelse, barna, antallBarn, type, morForSykVedFødsel);
    }

    @Override
    public String toString() {
        return "FamilieHendelseEntitet{" +
            "id=" + id +
            ", adopsjon=" + adopsjon +
            ", terminbekreftelse=" + terminbekreftelse +
            ", barna=" + barna +
            ", antallBarn=" + antallBarn +
            ", type=" + type +
            ", morForSykVedFødsel=" + morForSykVedFødsel +
            '}';
    }
}
