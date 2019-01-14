package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.verge;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "Verge")
@Table(name = "VE_VERGE")
public class VergeEntitet extends BaseEntitet implements Verge {

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "bruker_id", nullable = false)
    NavBruker bruker;

    @Column(name = "vedtaksdato")
    LocalDate vedtaksdato;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fomDato", column = @Column(name = "gyldig_fom")),
        @AttributeOverride(name = "tomDato", column = @Column(name = "gyldig_tom"))
    })
    DatoIntervallEntitet gyldigPeriode;

    @Column(name = "mandat_tekst", length = 2000)
    String mandatTekst;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "verge_type", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + VergeType.DISCRIMINATOR + "'"))})
    VergeType vergeType = VergeType.UDEFINERT;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "brev_mottaker", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + BrevMottaker.DISCRIMINATOR + "'"))})
    BrevMottaker brevMottaker = BrevMottaker.UDEFINERT;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "stoenad_mottaker", nullable = false)
    boolean stønadMottaker;

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VERGE")
    private Long id;

    public VergeEntitet() {
    }

    // deep copy
    public VergeEntitet(Verge verge) {
        Objects.requireNonNull(verge, "verge");
        this.vergeType = verge.getVergeType();
        this.bruker = verge.getBruker();
        this.vedtaksdato = verge.getVedtaksdato();
        this.gyldigPeriode = DatoIntervallEntitet.fraOgMedTilOgMed(verge.getGyldigFom(), verge.getGyldigTom());
        this.mandatTekst = verge.getMandatTekst();
        this.stønadMottaker = verge.getStønadMottaker();
        this.brevMottaker = verge.getBrevMottaker();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VergeEntitet that = (VergeEntitet) o;
        return stønadMottaker == that.stønadMottaker &&
            Objects.equals(bruker, that.bruker) &&
            Objects.equals(vedtaksdato, that.vedtaksdato) &&
            Objects.equals(gyldigPeriode, that.gyldigPeriode) &&
            Objects.equals(mandatTekst, that.mandatTekst) &&
            Objects.equals(vergeType, that.vergeType) &&
            Objects.equals(brevMottaker, that.brevMottaker);
    }

    @Override
    public int hashCode() {

        return Objects.hash(bruker, vedtaksdato, gyldigPeriode, mandatTekst, vergeType, brevMottaker, stønadMottaker);
    }

    @Override
    public VergeType getVergeType() {
        return vergeType;
    }

    @Override
    public LocalDate getVedtaksdato() {
        return vedtaksdato;
    }

    @Override
    public LocalDate getGyldigFom() {
        return gyldigPeriode.getFomDato();
    }

    @Override
    public LocalDate getGyldigTom() {
        return gyldigPeriode.getTomDato();
    }

    @Override
    public String getMandatTekst() {
        return mandatTekst;
    }

    @Override
    public NavBruker getBruker() {
        return bruker;
    }

    @Override
    public BrevMottaker getBrevMottaker() {
        return brevMottaker;
    }

    @Override
    public boolean getStønadMottaker() {
        return stønadMottaker;
    }

    public Long getId() {
        return id;
    }
}
