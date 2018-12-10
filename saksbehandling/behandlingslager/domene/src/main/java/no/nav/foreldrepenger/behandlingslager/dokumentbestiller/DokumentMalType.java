package no.nav.foreldrepenger.behandlingslager.dokumentbestiller;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabell;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "DokumentMalType")
@Table(name = "DOKUMENT_MAL_TYPE")
public class DokumentMalType extends KodeverkTabell {

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "generisk", nullable = false)
    private boolean generisk;

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "DOKUMENT_MAL_RESTRIKSJON", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'"
            + DokumentMalRestriksjon.DISCRIMINATOR + "'"))})
    private DokumentMalRestriksjon dokumentMalRestriksjon = DokumentMalRestriksjon.INGEN;

    @Column(name = "doksys_kode", nullable = false, updatable = false, insertable = false)
    private String doksysKode;

    public static final String REVURDERING_DOK = "REVURD";
    public static final String INNTEKTSMELDING_FOR_TIDLIG_DOK = "INNTID";

    DokumentMalType() {
        // Hibernate trenger default konstruktør
    }

    public String getDoksysKode() {
        return doksysKode;
    }

    public DokumentMalRestriksjon getDokumentMalRestriksjon() {
        return dokumentMalRestriksjon;
    }

    public boolean erGenerisk() {
        return generisk;
    }
}
