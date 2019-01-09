package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntektspost;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.foreldrepenger.domene.typer.Beløp;

@Entity(name = "Inntekt")
@Table(name = "IAY_INNTEKT")
public class InntektEntitet extends BaseEntitet implements Inntekt, IndexKey {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INNTEKT")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aktoer_inntekt_id", nullable = false, updatable = false)
    private AktørInntektEntitet aktørInntekt;

    @Embedded
    private Arbeidsgiver arbeidsgiver;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "kilde", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + InntektsKilde.DISCRIMINATOR + "'"))})
    private InntektsKilde inntektsKilde;

    @OneToMany(mappedBy = "inntekt")
    @ChangeTracked
    private Set<InntektspostEntitet> inntektspost = new LinkedHashSet<>();

    InntektEntitet() {
        // hibernate
    }

    /**
     * Deep copy.
     */
    InntektEntitet(Inntekt inntektMal) {
        this.inntektsKilde = inntektMal.getInntektsKilde();
        this.arbeidsgiver = inntektMal.getArbeidsgiver();
        this.inntektspost = inntektMal.getInntektspost().stream().map(ip -> {
            InntektspostEntitet inntektspostEntitet = new InntektspostEntitet(ip);
            inntektspostEntitet.setInntekt(this);
            return inntektspostEntitet;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(getArbeidsgiver(), getInntektsKilde());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !(obj instanceof InntektEntitet)) {
            return false;
        }
        InntektEntitet other = (InntektEntitet) obj;
        return Objects.equals(this.getInntektsKilde(), other.getInntektsKilde())
            && Objects.equals(this.getArbeidsgiver(), other.getArbeidsgiver());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInntektsKilde(), getArbeidsgiver());
    }

    @Override
    public InntektsKilde getInntektsKilde() {
        return inntektsKilde;
    }

    void setInntektsKilde(InntektsKilde inntektsKilde) {
        this.inntektsKilde = inntektsKilde;
    }

    @Override
    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    void setArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    @Override
    public Collection<Inntektspost> getInntektspost() {
        return Collections.unmodifiableSet(inntektspost.stream()
            .filter(InntektspostEntitet::skalMedEtterSkjæringstidspunktVurdering)
            .collect(Collectors.toSet()));
    }

    void leggTilInntektspost(Inntektspost inntektspost) {
        InntektspostEntitet inntektspostEntitet = (InntektspostEntitet) inntektspost;
        inntektspostEntitet.setInntekt(this);
        this.inntektspost.add(inntektspostEntitet);
    }

    public AktørInntektEntitet getAktørInntekt() {
        return aktørInntekt;
    }

    void setAktørInntekt(AktørInntektEntitet aktørInntekt) {
        this.aktørInntekt = aktørInntekt;
    }

    public InntektspostBuilder getInntektspostBuilder() {
        return InntektspostBuilder.ny();
    }

    public boolean hasValues() {
        return arbeidsgiver != null || inntektsKilde != null || inntektspost != null;
    }

    boolean erPersistert() {
        return id != null;
    }

    void setSkjæringstidspunkt(LocalDate skjæringstidspunkt, boolean ventreSide) {
        for (InntektspostEntitet inntektspostEntitet : inntektspost) {
            inntektspostEntitet.setSkjæringstidspunkt(skjæringstidspunkt, ventreSide);
        }
    }

    public static class InntektspostBuilder {
        private InntektspostEntitet inntektspostEntitet;

        InntektspostBuilder(InntektspostEntitet inntektspostEntitet) {
            this.inntektspostEntitet = inntektspostEntitet;
        }

        public static InntektspostBuilder ny() {
            return new InntektspostBuilder(new InntektspostEntitet());
        }

        public InntektspostBuilder medInntektspostType(InntektspostType inntektspostType) {
            this.inntektspostEntitet.setInntektspostType(inntektspostType);
            return this;
        }

        public InntektspostBuilder medPeriode(LocalDate fraOgMed, LocalDate tilOgMed) {
            this.inntektspostEntitet.setPeriode(fraOgMed, tilOgMed);
            return this;
        }

        public InntektspostBuilder medBeløp(BigDecimal verdi) {
            this.inntektspostEntitet.setBeløp(new Beløp(verdi));
            return this;
        }

        public InntektspostBuilder medYtelse(YtelseType offentligYtelseType) {
            this.inntektspostEntitet.setYtelse(offentligYtelseType);
            return this;
        }

        public Inntektspost build() {
            if (inntektspostEntitet.hasValues()) {
                return inntektspostEntitet;
            }
            throw new IllegalStateException();
        }
    }
}
