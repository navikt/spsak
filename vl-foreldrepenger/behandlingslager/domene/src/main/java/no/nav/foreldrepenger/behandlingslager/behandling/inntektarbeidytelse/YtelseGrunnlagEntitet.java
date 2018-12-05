package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseStørrelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.domene.typer.Stillingsprosent;

@Entity(name = "YtelseGrunnlagEntitet")
@Table(name = "IAY_YTELSE_GRUNNLAG")
public class YtelseGrunnlagEntitet extends BaseEntitet implements YtelseGrunnlag {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YTELSE_GRUNNLAG")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "ytelse_id", nullable = false, updatable = false, unique = true)
    private YtelseEntitet ytelse;

    @OneToMany(mappedBy = "ytelseGrunnlag")
    @ChangeTracked
    private List<YtelseStørrelseEntitet> ytelseStørrelse = new ArrayList<>();

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "arbeidskategori", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + Arbeidskategori.DISCRIMINATOR + "'"))})
    private Arbeidskategori arbeidskategori = Arbeidskategori.UDEFINERT;

    @ChangeTracked
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "dekningsgrad_prosent")))
    private Stillingsprosent dekngradProsent;

    @ChangeTracked
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "gradering_prosent")))
    private Stillingsprosent graderingProsent;

    @ChangeTracked
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "inntektsgrunnlag_prosent")))
    private Stillingsprosent inntektProsent;

    @Column(name = "opprinnelig_identdato")
    @ChangeTracked
    private LocalDate opprinneligIdentdato;

    public YtelseGrunnlagEntitet() {
        // hibernate
    }

    public YtelseGrunnlagEntitet(YtelseGrunnlag ytelseGrunnlag) {
        this.arbeidskategori = ytelseGrunnlag.getArbeidskategori().orElse(null);
        this.dekngradProsent = ytelseGrunnlag.getDekningsgradProsent().orElse(null);
        this.graderingProsent = ytelseGrunnlag.getGraderingProsent().orElse(null);
        this.inntektProsent = ytelseGrunnlag.getInntektsgrunnlagProsent().orElse(null);
        this.opprinneligIdentdato = ytelseGrunnlag.getOpprinneligIdentdato().orElse(null);
        this.ytelseStørrelse = ytelseGrunnlag.getYtelseStørrelse().stream().map(ys -> {
            YtelseStørrelseEntitet ytelseStørrelseEntitet= new YtelseStørrelseEntitet(ys);
            ytelseStørrelseEntitet.setYtelseGrunnlag(this);
            return ytelseStørrelseEntitet;
        }).collect(Collectors.toList());
    }

    @Override
    public Optional<Arbeidskategori> getArbeidskategori() {
        return Optional.ofNullable(arbeidskategori);
    }

    void setArbeidskategori(Arbeidskategori arbeidskategori) {
        this.arbeidskategori = arbeidskategori;
    }

    @Override
    public Optional<Stillingsprosent> getDekningsgradProsent() {
        return Optional.ofNullable(dekngradProsent);
    }

    void setDekningsgradProsent(Stillingsprosent prosent) {
        this.dekngradProsent = prosent;
    }

    @Override
    public Optional<Stillingsprosent> getGraderingProsent() {
        return Optional.ofNullable(graderingProsent);
    }

    void setGraderingProsent(Stillingsprosent prosent) {
        this.graderingProsent = prosent;
    }

    @Override
    public Optional<Stillingsprosent> getInntektsgrunnlagProsent() {
        return Optional.ofNullable(inntektProsent);
    }

    void setInntektsgrunnlagProsent(Stillingsprosent prosent) {
        this.inntektProsent = prosent;
    }

    @Override
    public Optional<LocalDate> getOpprinneligIdentdato() {
        return Optional.ofNullable(opprinneligIdentdato);
    }

    void setOpprinneligIdentdato(LocalDate dato) {
        this.opprinneligIdentdato = dato;
    }

    @Override
    public List<YtelseStørrelse> getYtelseStørrelse() {
        return Collections.unmodifiableList(ytelseStørrelse);
    }

    void leggTilYtelseStørrelse(YtelseStørrelse ytelseStørrelse) {
        YtelseStørrelseEntitet ytelseStørrelseEntitet = (YtelseStørrelseEntitet) ytelseStørrelse;
        ytelseStørrelseEntitet.setYtelseGrunnlag(this);
        this.ytelseStørrelse.add(ytelseStørrelseEntitet);

    }

    void tilbakestillStørrelse() {
        ytelseStørrelse.clear();
    }


    void setYtelse(YtelseEntitet ytelse) {
        this.ytelse = ytelse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YtelseGrunnlagEntitet that = (YtelseGrunnlagEntitet) o;
        return Objects.equals(arbeidskategori, that.arbeidskategori) &&
            Objects.equals(dekngradProsent, that.dekngradProsent) &&
            Objects.equals(graderingProsent, that.graderingProsent) &&
            Objects.equals(inntektProsent, that.inntektProsent) &&
            Objects.equals(opprinneligIdentdato, that.opprinneligIdentdato);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidskategori, dekngradProsent, graderingProsent, inntektProsent, opprinneligIdentdato);
    }

    @Override
    public String toString() {
        return "YtelseGrunnlagEntitet{" +
            "arbeidskategori=" + arbeidskategori +
            ", dekngradProsent=" + dekngradProsent +
            ", graderingProsent=" + graderingProsent +
            ", inntektProsent=" + inntektProsent +
            ", opprinneligIdentdato=" + opprinneligIdentdato +
            '}';
    }
}
