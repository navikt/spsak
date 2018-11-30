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
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Table(name = "IAY_AKTOER_YTELSE")
@Entity(name = "AktørYtelse")
class AktørYtelseEntitet extends BaseEntitet implements AktørYtelse, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AKTOER_YTELSE")
    private Long id;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "aktoer_id", nullable = false, updatable = false)))
    private AktørId aktørId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inntekt_arbeid_ytelser_id", nullable = false, updatable = false)
    private InntektArbeidYtelseAggregatEntitet inntektArbeidYtelser;

    @OneToMany(mappedBy = "aktørYtelse")
    @ChangeTracked
    private Set<YtelseEntitet> ytelser = new LinkedHashSet<>();

    public AktørYtelseEntitet() {
        // hibernate
    }

    /**
     * Deep copy ctor
     */
    AktørYtelseEntitet(AktørYtelse aktørYtelse) {
        this.aktørId = aktørYtelse.getAktørId();
        this.ytelser = aktørYtelse.getYtelser().stream().map(ytelse -> {
            YtelseEntitet ytelseEntitet = new YtelseEntitet(ytelse);
            ytelseEntitet.setAktørYtelse(this);
            return ytelseEntitet;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(getAktørId());
    }

    @Override
    public AktørId getAktørId() {
        return aktørId;
    }

    void setAktørId(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    @Override
    public Collection<Ytelse> getYtelser() {
        return Collections.unmodifiableSet(ytelser.stream().filter(YtelseEntitet::skalMedEtterSkjæringstidspunktVurdering).collect(Collectors.toSet()));
    }

    void setInntektArbeidYtelser(InntektArbeidYtelseAggregatEntitet inntektArbeidYtelser) {
        this.inntektArbeidYtelser = inntektArbeidYtelser;
    }

    boolean hasValues() {
        return aktørId != null || ytelser != null && !ytelser.isEmpty();
    }

    YtelseBuilder getYtelseBuilderForType(Fagsystem fagsystem, RelatertYtelseType type, Saksnummer saksnummer) {
        Optional<Ytelse> ytelse = getYtelser().stream()
            .filter(ya -> ya.getKilde().equals(fagsystem) && ya.getRelatertYtelseType().equals(type) && (saksnummer.equals(ya.getSaksnummer())))
            .findFirst();
        return YtelseBuilder.oppdatere(ytelse).medYtelseType(type).medKilde(fagsystem).medSaksnummer(saksnummer);
    }

    YtelseBuilder getYtelseBuilderForType(Fagsystem fagsystem, RelatertYtelseType type, Saksnummer saksnummer, LocalDate fom) {
        Optional<Ytelse> ytelse = getYtelser().stream()
            .filter(ya -> ya.getKilde().equals(fagsystem) && ya.getRelatertYtelseType().equals(type) && (saksnummer.equals(ya.getSaksnummer())
                && fom.equals(ya.getPeriode().getFomDato())))
            .findFirst();
        return YtelseBuilder.oppdatere(ytelse).medYtelseType(type).medKilde(fagsystem).medSaksnummer(saksnummer);
    }

    YtelseBuilder getYtelseBuilderForType(Fagsystem fagsystem, RelatertYtelseType type, TemaUnderkategori typeKategori, DatoIntervallEntitet periode) {
        Optional<Ytelse> ytelse = getYtelser().stream()
            .filter(ya -> ya.getKilde().equals(fagsystem) && ya.getRelatertYtelseType().equals(type)
                && ya.getBehandlingsTema().equals(typeKategori) && (periode.getFomDato().equals(ya.getPeriode().getFomDato())))
            .findFirst();
        return YtelseBuilder.oppdatere(ytelse).medYtelseType(type).medKilde(fagsystem).medPeriode(periode);
    }

    void leggTilYtelse(Ytelse ytelse) {
        YtelseEntitet ytelseEntitet = (YtelseEntitet) ytelse;
        this.ytelser.add(ytelseEntitet);
        ytelseEntitet.setAktørYtelse(this);
    }

    void fjernYtelse(Ytelse ytelse) {
        YtelseEntitet ytelseEntitet = (YtelseEntitet) ytelse;
        ytelseEntitet.setAktørYtelse(null);
        this.ytelser.remove(ytelseEntitet);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof AktørYtelseEntitet)) {
            return false;
        }
        AktørYtelseEntitet other = (AktørYtelseEntitet) obj;
        return Objects.equals(this.getAktørId(), other.getAktørId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" +
            "aktørId=" + aktørId +
            ", ytelser=" + ytelser +
            '>';
    }

    void setSkjæringstidspunkt(LocalDate skjæringstidspunkt, boolean ventreSide) {
        for (YtelseEntitet ytelseEntitet : ytelser) {
            ytelseEntitet.setSkjæringstidspunkt(skjæringstidspunkt, ventreSide);
        }
    }
}
